# Release preparation

The agent prepares and validates local changes, then stops before committing.
The user handles the commit, push, release workflow, and publication manually.

## Prepare

1. Review the current version, previous changelog, and changes intended for the
   release, including pending changes in the working tree.
2. Update `versionName` and increment `versionCode` together in
   [app/build.gradle.kts](../app/build.gradle.kts). Follow the existing version
   sequence unless the user specifies a version; do not reuse a published version.
3. Add `metadata/en-US/changelogs/<versionCode>.txt`. Use the numeric version code
   as the filename and preserve earlier notes. Write concise English bullets
   describing actual changes, within 500 characters including newlines. Do not
   claim unverified performance or stability improvements.

## Verify and hand off

Run from the repository root with JDK 21 and the Gradle wrapper:

```sh
git diff --check
./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleRelease
```

Check the changelog filename and length, and confirm the built version in
`app/build/outputs/apk/release/output-metadata.json`. For behavior or UI changes,
verify affected flows on a device or emulator. Rerun affected checks after edits.

Report the prepared version, changed files, checks actually run, and any remaining
validation gaps. Include a one-line commit message in a copy-ready text block:
`Prepare release <versionName>`. Leave the changes for the user to commit; do not stage,
commit, push, create tags, or trigger publication.
