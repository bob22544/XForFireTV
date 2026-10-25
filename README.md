# X for Fire TV 1.4

An unofficial Android WebView wrapper for X, laid out for a Fire TV remote. It is not affiliated with X and is not a native X client.

## v1.4 changes

- Adds native Fire TV media-key routing for Play/Pause, Play, Pause, Fast Forward, and Rewind, mapped to the active X HTML video while keeping playback inside the app.
- In full-screen playback, D-pad Left/Right and dedicated Fast Forward/Rewind keys seek by 10 seconds; Play/Pause and media transport keys are handled by the app rather than relying on the seek-dot focus. Pressing OK on a playing inline video requests full-screen playback.
- Volume Up preserves Fire OS volume handling and also clears X video mute when a video is muted.
- Improves video selection by prioritising the full-screen/currently-playing video, then the selected post's video.
- Keeps Up/Down as feed navigation in the inline view and as an escape hatch from full-screen controls.

## v1.3 changes

- Adds remote-first post navigation: Up/Down moves between visible X posts instead of making the remote tab through every link and button.
- Automatically highlights and centres the selected post, including posts loaded later as the feed scrolls.
- Hides X's redundant left navigation and right sidebar so the app's own section menu and the feed have less clutter.
- OK on a selected post with a video starts playback; pressing OK again while it is playing requests full-screen playback in the same app.
- Up/Down escapes a focused video seek control and moves to another post. In full-screen mode, Up/Down exits full screen back to the feed; Left/Right remain available for seeking.
- Keeps X's own player and in-app WebView playback. It does not launch an external Android video player.
- Retains the v1.2 full-screen cleanup, persistent dark mode, and release-signing support.

## Important limitations

- This remains a WebView wrapper. X can change its website, restrict embedded login, or alter playback and remote focus at any time.
- Full-screen video depends on X's player exposing Android WebView's standard custom-view callbacks. Some video types may still behave differently.
- Remote focus cannot be guaranteed across every X page, advertisement, video widget, or Fire OS version. The new feed navigation is a best-effort DOM integration and needs testing on the actual device.
- Dark mode requests the corresponding browser colour scheme and changes the app shell; X may apply its own account/site theme rules.
- This app does not collect credentials. Login is handled by X's website inside Android WebView.
- Piko is a patch collection for the official Android X app, not a standalone Fire TV app, so it is not bundled here.

## Publish the APK with GitHub

1. Download and extract this ZIP.
2. In GitHub, create or use a public repository named `XForFireTV`.
3. Upload the contents of the extracted `XForFireTV` folder, including the hidden `.github` folder. Commit the files to the default branch.
4. Open **Actions** and enable workflows if prompted.
5. Open **Releases > Draft a new release**, create a new tag such as `v1.4.0`, and publish it.
6. Wait for the workflow to complete. The release asset will be named `app-fire-tv.apk`.
7. Downloader URL format:
   `https://github.com/YOUR-GITHUB-USERNAME/XForFireTV/releases/latest/download/app-fire-tv.apk`

Replace `YOUR-GITHUB-USERNAME` with your GitHub username.

## Recommended: configure a persistent signing key

Without a persistent key, the workflow makes a debug APK and GitHub's temporary runner may use a different debug key on the next build. Android may reject it as an update. Configure a release key once for reliable in-place updates.

1. On a computer with Java installed, create a keystore **and keep a secure backup**:
   ```bash
   keytool -genkeypair -v -keystore xftv-release.jks -alias xftv -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Base64-encode the keystore. On Linux/macOS:
   ```bash
   base64 -w 0 xftv-release.jks > xftv-release.base64
   ```
   On macOS, if `-w 0` is unsupported, use `base64 xftv-release.jks | tr -d '\n' > xftv-release.base64`.
3. In GitHub repository **Settings > Secrets and variables > Actions**, create these repository secrets:
   - `RELEASE_KEYSTORE_BASE64`: the complete contents of `xftv-release.base64`
   - `RELEASE_STORE_PASSWORD`: the keystore password
   - `RELEASE_KEY_ALIAS`: `xftv` (or the alias you chose)
   - `RELEASE_KEY_PASSWORD`: the key password
4. Never commit the `.jks`, base64 file, or passwords into the repository. Keep the keystore backed up privately. If it is lost, you cannot sign future updates with the same identity.
5. Publish the next release. The workflow will build a release-signed APK if the secrets are configured.

**Migration note:** If the currently installed APK was signed with the old debug key, the first release-signed APK will not install over it. Uninstall the old app once, then install the new APK. This clears the app's local X login cookies. After that, subsequent builds signed with the same release key can install as updates, provided the package ID stays the same and versionCode increases.

## Install using Downloader

1. In Fire TV settings, allow Downloader to install unknown apps. Menu names vary by Fire OS version.
2. Open Downloader and enter the direct URL above with your GitHub username substituted.
3. Download the APK, choose **Install**, then **Open**.
4. Only install APKs from a source you trust.

## Updating

1. Replace the project files in your existing repository with the contents of this ZIP. Keep `.github/workflows/build-release.yml`.
2. Commit the changes.
3. Publish a new release with a new tag, such as `v1.4.0` (use a later unused tag if already taken).
4. Wait for the build to succeed, then use the same `releases/latest/download/app-fire-tv.apk` URL in Downloader.
5. Fire TV may ask you to approve the install. Downloader does not silently update apps in the background.

## Build locally

Install Android Studio and Android SDK API 35, open this project, sync Gradle, then select **Build > Build Bundle(s) / APK(s) > Build APK(s)**. The debug output is `app/build/outputs/apk/debug/app-debug.apk`.

## Details

- Application ID: `tv.xremote`
- Minimum Android API: 23
- Target API: 28
- Version: 1.4 (versionCode 5)
