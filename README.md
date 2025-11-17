# MoveTasker

MoveTasker is an Android tool that watches selected folders and automatically moves new files based on custom rules. Ideal for Samsung users who want Google Photos backup without uploading unwanted images like Facebook, Screenshots, or Screen recordings.

## Why MoveTasker?

- Keep Google Photos clean by relocating screenshots, Facebook downloads, or screen recordings before they sync.
- Build a personal automation workflow using simple, rule-based folder actions.
- Maintain privacy by ensuring sensitive folders are excluded from cloud uploads.

## Samsung + Google Photos Workflow

1. **Prepare the folder:** before adding a rule, open your file manager and create a file named `.nomedia` inside the source folder (e.g., `Internal Storage/DCIM/Facebook`). This hides the folder from Google Photos.
2. **Automate with MoveTasker:** create a rule targeting the same folder so MoveTasker moves every new file to a safe destination automatically.

## Project Structure

```
app/
  src/
    main/
      java/com/example/movetasker/   # Core source, features, view models, services
      res/                           # Android resources (layouts, icons, strings)
  build.gradle.kts                   # Module build configuration
build.gradle.kts                     # Root Gradle setup
settings.gradle.kts                  # Multi-module coordination
gradle/                              # Wrapper scripts and properties
```

## Use-Case Examples

- **Social media cleanup:** move everything added to `DCIM/Facebook` into an archive folder so only camera photos reach Google Photos.
- **Screenshot sorting:** send screenshots from `Pictures/Screenshots` to a work or projects folder the moment they are captured.
- **Screen recordings:** relocate large screen recordings from `Movies/Screen recordings` to external storage automatically to save internal space if applicable.
