# Polymarket API contract audit

Checked on 2026-09-19 against the live API, official specifications, official SDK source, and this checkout. This is a dated compatibility audit, not a guarantee that the upstream API cannot change.

## Sources and confidence

Prefer the contract of the exact endpoint over a sample response. A field appearing in every sampled object does not make it required.

- [Live Gamma OpenAPI 3.1](https://gamma-api.polymarket.com/openapi.json) and [Series JSON Schema](https://gamma-api.polymarket.com/schemas/Series.json). These are served by the API itself; response `$schema` links point to this schema family.
- [Documentation Gamma OpenAPI](https://docs.polymarket.com/api-spec/gamma-openapi.yaml) and [event endpoint documentation](https://docs.polymarket.com/api-reference/events/list-events).
- [Official Rust SDK Gamma models](https://github.com/Polymarket/rs-clob-client/blob/main/src/gamma/types/response.rs) and [Data API models](https://github.com/Polymarket/rs-clob-client/blob/main/src/data/types/response.rs). Useful independent checks of optional fields, string numbers, empty strings, and endpoint-specific models; not a Kotlin library to introduce into this app.
- [Data API specification](https://docs.polymarket.com/api-spec/data-openapi.yaml) and [CLOB specification](https://docs.polymarket.com/api-spec/clob-openapi.yaml).

There is no single authoritative model that can be generated unchanged for every endpoint used by this app:

- Live Gamma `Series`, `Event`, `Tag`, and `Comment` require only `id`. The documentation specification marks many fields nullable; the live schema often says optional but does not explicitly allow null. Optional and nullable are separate properties.
- The documentation schema omits `required` lists even for response containers. Live Gamma explicitly requires pagination fields. The Rust SDK requires many Data API value/status fields even though the Data API documentation does not list them as required.
- `/public-search?optimized=true` returns arrays for `outcomes` and `outcomePrices`, and top-level `hasMore`. Full results use JSON-encoded strings and a `pagination` object. They must keep separate DTOs.
- `/api/tags/filteredBySlug` and `/api/profile/userData` are website endpoints. The Gamma schemas describe related entities, not a guaranteed contract for those website routes. They were not migrated during this change.

## Observed failure and response sample

The AI Safety page fails at `data[2].series[0]`: event `79075`, series `10624`, no `recurrence` property. The response is HTTP 200. This is a deserialization failure, not a category request failure. The previous `seriesType` fix (`0801134`) addressed the same class of problem.

Fetched and decoded with the application's JSON configuration:

- 540 full event records (530 unique): AI Safety 20, general feed 100, crypto 100, sports 100, politics 100, recently closed 100, full search 20.
- 12,425 nested market records (11,949 unique). No sampled market lacked the app's remaining required identity/status fields. This observation does not establish an upstream guarantee.
- 278 nested series occurrences (85 unique); 24 occurrences omitted `recurrence`.
- 20 optimized search events; both search variants included 20 profiles and 12 tags.
- 100 tags, 50 standalone series, 22 series comments, 5 leaderboard entries.
- 30 positions, 30 closed positions, 30 activity rows, position value, and traded count for one public leaderboard wallet.

The full response corpus was decoded in a temporary local JUnit test; it is not checked into the repository because it contains large responses and public profile/comment data. The small checked-in regression fixture keeps the exact offending series object and reduces unrelated event fields. Its pagination describes the reduced fixture, not the original page. CLOB was reviewed against its schema; no live CLOB response was decoded in this audit.

## Changes made

| Area | Previous assumption | Handling now |
| --- | --- | --- |
| `SeriesDto` | Required ticker, slug, title, recurrence, three status flags, dates, comment count | Only `id` is required; metadata accepts missing fields and explicit null |
| `MarketDto` | Required `createdAt`, `new`, `restricted`, `ready`, `funded`, `approved`, `manualActivation` | Nullable with null defaults; no fabricated timestamps or status values |
| Full and optimized market thresholds | `groupItemThreshold` parsed as `Int` | Preserve the documented string; derive a finite numeric sort key, including fractions and values beyond `Int`; blank/nonnumeric values have no numeric key |
| Nested profiles | Nullable fields were still mandatory JSON keys | Defaults for comment `displayUsernamePublic`, position `positionSize`, and user association `id`, `creator`, `mod` |
| Current positions | `eventId` required | Optional, matching official SDK; `eventSlug` still supplies navigation |
| Series/tag `publishedAt` | Every string must parse as a timestamp | Preserve the string, as specified; these metadata fields are not used as dates by the UI |
| Event resolution source | Read `resolution_source`, which is absent from the official schema | Read the documented `resolutionSource` property |
| JSON configuration | Tests copied some client settings | HTTP clients and contract tests share `polymarketJson`; decoding policy itself is unchanged |

In Kotlin serialization, `val field: String?` still requires the JSON key by default. Optional metadata needs `val field: String? = null`. `ignoreUnknownKeys` only handles extra keys; it does not protect against missing fields or changed value types.

## Remaining reviewed application requirements

These requirements are stricter than parts of the upstream schemas. They are deliberately retained in this patch, and are enforced/documented by `PolymarketOptionalFieldsTest` so new mandatory fields require review. That test is an application policy guard, not a claim that every listed field is required by Polymarket.

| DTOs | Required fields retained | Reason / remaining risk |
| --- | --- | --- |
| Full / optimized events | `id`, `title`, `slug`, `active`, `closed`, `markets` | Identity, labels, navigation, status and display rows. The live schema permits missing metadata and nullable market arrays. Missing/null fields can still fail the page. |
| Full markets | `id`, `question`, `slug`, `active`, `closed`, `archived` | Identity, labels, navigation and state. No safe unknown-state presentation exists yet. |
| Optimized markets | `question`, `slug`, status flags, `outcomes`, `outcomePrices` | Search result display requires these; separate wire format confirmed by responses. |
| Tags | `id`, `label`, `slug` | Category labels, keys and filters. Incomplete tags need to be excluded or rendered with an explicit fallback. |
| Comments / profiles / comment positions | Comment `id` and `profile`; profile `proxyWallet`; position `tokenId` | Current UI requires a profile for avatar/name/navigation and uses token IDs for position badges. Upstream allows absent profile/address. |
| Search and pagination | Page `data` / `pagination`, `hasMore` / `totalResults`; optimized search `hasMore` | Keep malformed/error responses distinct from legitimate empty pages. Live schema allows null page data; the current app expects a list. |
| Current positions | `eventSlug` | Navigation key. Missing navigation requires disabling the action, not inventing a slug. |
| Closed positions | `outcome`, `eventSlug`, `avgPrice`, `realizedPnl`, `totalBought`, `timestamp` | UI and financial values. Missing values must not silently become zero. |
| Activity | `timestamp`, `asset`, `title`, `eventSlug`, `outcome`, `type`, `size`, `price` | Matches the current UI/SDK assumptions; non-trade rows may contain empty strings, which already decode. Missing data remains an error. |
| Leaderboard | `rank`, `proxyWallet`, `userName`, `verifiedBadge` | Identity/navigation/display. The public sample decoded, but documentation alone does not guarantee every key. |
| Value / traded count | `user` | Identify the subject of the aggregate. Values are already optional. |
| CLOB history | `history`; point `t`, `p` | Never manufacture chart timestamps/prices when data is absent or invalid. |

Also retained: strict parsing of UI-consumed timestamps, existing non-null booleans with defaults (an explicit JSON null still fails), and numeric price types. The API documents some dates as unformatted strings, so unexpected empty/invalid dates remain a separate compatibility risk. `outcomes` and `outcomePrices` can also become mismatched; this change does not redesign their presentation.

## Further hardening

To contain failures from incomplete core fields, introduce a separate transport-to-UI mapping boundary with explicit per-screen policies: disable navigation without a slug/address, show an unknown status, omit a position badge without its token/size, and report unavailable amounts rather than zero. Then incomplete rows can be handled individually with diagnostics and visible partial-result feedback. Silently dropping arbitrary events or converting every error into an empty list would hide data loss and disrupt offset pagination.

For ongoing API drift detection, an optional scheduled read-only compatibility check could refresh a bounded representative corpus and decode it with the app models. Keep that separate from deterministic unit tests and report unknown fields/types; a passing sample still cannot prove all future responses. No recurring job or CI network dependency was added here.

## Regression checks

- `EventDtoSerializationTest`: the original missing-`seriesType` case and the real missing-`recurrence` case, and the documented `resolutionSource` mapping.
- `PolymarketDtoContractTest`: omitted/null/present metadata, string thresholds, numeric ordering, nested profiles, optional position event ID, unrestricted publication strings, rejection of absent identity and malformed numeric prices.
- `PolymarketOptionalFieldsTest`: reviewed required-field sets across 23 DTOs; every nullable field must also be optional on the wire.
- Existing widget tests continue to check shared market ordering after the threshold type correction.

Run `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`, then check AI Safety, Berlin, general feed and search on an emulator. Validation results belong to the specific run; this document does not substitute for rerunning checks after later edits.

Validation on 2026-09-19: the full command above passed; 71 tests passed with no failures or skips. Lint reported no errors and 16 warnings in unchanged build/resource files. The installed debug APK successfully displayed All, AI Safety (including event `79075`), Berlin, and Trump search results. A separate one-off JUnit decoder run passed against the full saved response sample listed above. No release version or release artifact was changed.
