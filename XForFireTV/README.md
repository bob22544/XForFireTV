# X for Fire TV (unofficial starter app)

A landscape Android/Fire TV wrapper around X with a large, D-pad-selectable side menu for Home, Explore, Bookmarks, Notifications, Messages, Profile, and Log in. X renders the timeline and handles account sessions, likes, bookmarks, and media playback where its web app supports them.

## Important limitations

- This is an unofficial wrapper, not an X product and not affiliated with X.
- It is a WebView app, not a native X client. X can change its website or block embedded login. If login loops or refuses to work, this wrapper cannot bypass that restriction.
- Video formats, ads, DRM, remote focus and some controls vary by Fire OS device and X's current website. It has not been tested on a physical Fire TV.
- Piko is a patch collection for the official Android X app, not a standalone Fire TV app, so it is not bundled here.
- No credentials are collected by this app's own code. Sign-in, if supported, is rendered by X.

## Easiest route to a Downloader-compatible APK

This project includes a GitHub Actions workflow that builds the APK and publishes it as a GitHub Release when you push a version tag. I cannot publish it to your GitHub account from this chat, so you need to do the one-time upload below.

1. Download the source ZIP and extract it.
2. Sign in to GitHub and create a **Public** repository named `XForFireTV` (do not initialise it with a README, licence or .gitignore).
3. In the new repository, choose **Add file > Upload files** and upload the *contents* of the extracted `XForFireTV` folder, including the hidden `.github` folder. Commit the files to the `main` branch. If the website does not show hidden `.github`, use GitHub Desktop or the command-line steps below.
4. Open the repository's **Actions** tab and enable workflows if GitHub asks.
5. Create a release tag by opening the repository's **Releases** page, choosing **Draft a new release**, entering tag `v1.0.0`, and publishing it. The workflow will build the APK and attach `app-debug.apk` to the release. If the build fails, open **Actions** and inspect the failed run.
6. Once the release is complete, the APK's public direct-download URL will be:
   `https://github.com/YOUR-GITHUB-USERNAME/XForFireTV/releases/latest/download/app-debug.apk`
   Replace `YOUR-GITHUB-USERNAME` with your GitHub username.

### Alternative upload using GitHub Desktop / terminal

After creating an empty public repository, open a terminal in this folder and run:

```sh
git init
git add .
git commit -m "Initial X for Fire TV app"
git branch -M main
git remote add origin https://github.com/YOUR-GITHUB-USERNAME/XForFireTV.git
git push -u origin main
git tag v1.0.0
git push origin v1.0.0
```

Replace `YOUR-GITHUB-USERNAME` first. The tag push triggers the APK build and release automatically.

## Install with Downloader on Fire TV

1. Wait for the GitHub Actions run to finish successfully and the `v1.0.0` release to appear.
2. On Fire TV, install/open **Downloader** and allow installs from unknown apps for Downloader in Fire TV settings. The exact menu names vary by Fire OS version.
3. In Downloader's URL field, enter the direct-download URL shown above, with your GitHub username substituted:
   `https://github.com/YOUR-GITHUB-USERNAME/XForFireTV/releases/latest/download/app-debug.apk`
4. Download the APK, select **Install**, then **Open**.
5. If Fire TV warns about an unknown app, only proceed if you trust the source. Keep in mind this is an unofficial, untested starter app.

## Build locally instead

Install Android Studio and Android SDK API 35, then open this folder and select **Build > Build Bundle(s) / APK(s) > Build APK(s)**. The output is `app/build/outputs/apk/debug/app-debug.apk`.

## Controls and project details

- Use the remote's directional pad to move between the side menu and X's page controls.
- Select a side menu item to open its page.
- Back returns to the previous X page where possible.
- Package: `tv.xremote`
- Minimum Android API: 23
- Target API: 28 for broad Fire OS compatibility
- Version: 1.0
