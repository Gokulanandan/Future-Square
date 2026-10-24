# Building for Google Play

The full step-by-step guide (Play Console, AdMob, policies, testing) lives in the project
notes. This page covers only what the code needs.

## 1. Make an upload key (once)

On any computer with Java or Android Studio installed:

```
keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 upload.jks > upload.jks.b64      # macOS: base64 -i upload.jks -o upload.jks.b64
```

Keep `upload.jks` and its passwords somewhere safe (a password manager and a backup drive).
Never commit them.

## 2. Add GitHub secrets

Repository **Settings > Secrets and variables > Actions > New repository secret**:

| Secret | Value |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | the text inside `upload.jks.b64` |
| `RELEASE_KEYSTORE_PASSWORD` | the keystore password |
| `RELEASE_KEY_ALIAS` | `upload` |
| `RELEASE_KEY_PASSWORD` | the key password |
| `ADMOB_APP_ID` | AdMob app ID, `ca-app-pub-…~…` |
| `ADMOB_BANNER_ID` | AdMob banner unit ID, `ca-app-pub-…/…` |
| `ADMOB_INTERSTITIAL_ID` | AdMob interstitial unit ID, `ca-app-pub-…/…` |

Until the AdMob secrets are set, release builds use Google's test ads (they work but earn
nothing). Debug builds always use test ads, so tapping your own ads on test phones is safe.

## 3. Get the bundle

Every push builds `future-square-play-bundle` (the signed `app-release.aab`) under the run's
**Artifacts** on the Actions tab. Upload that file to Play Console. The version code is the
workflow run number, so each new build can be uploaded over the last one.

## Privacy policy

`docs/privacy-policy.md` is the policy for the store listing. Replace `CONTACT_EMAIL`, then turn
on **Settings > Pages > Deploy from branch > main /docs** and use
`https://gokulanandan.github.io/Future-Square/privacy-policy` as the privacy policy URL.
