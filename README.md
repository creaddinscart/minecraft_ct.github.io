# CT Official Website

This folder is the complete static website and CT download portal. Publish the contents of `HTML/` to the official website's static hosting root. Keep the `downloads/` folder and `assets/` directory at their relative paths so the site's links continue to work.

## Website Releases

1. Update the release record under `versions/CT vX.Y.Z[.BUILD]/`.
2. Run `./mvnw package` to build the current desktop ZIP into `HTML/downloads/CT-Client-X.Y.Z[.BUILD].zip`.
3. Publish the updated `HTML/` directory to the official website.
4. Verify the Release page download and direct ZIP URL from the public site.

The ZIP contains the GUI JAR, platform launch scripts, English usage instructions, and release records. It does not contain project source code or Minecraft game files.

## Website Capabilities

The static website presents CT releases and downloads. Browsers cannot install desktop software, access arbitrary local files, or launch Minecraft processes. Download and run the CT desktop client to install and launch game versions; the client fetches Minecraft data directly from Mojang's official endpoints.

## Pages

- `index.html`: overview and current release download
- `docs/index.html`: wiki index and documentation categories
- `docs/fabric-sodium.html`: Fabric Loader and Sodium installation guide
- `updates.html`: current release notes
- `getting-started.html`: desktop client setup and version selection
- `settings.html`: settings and local directory layout
- `mods.html`: CT-native mod guide
- `api.html`: Java API reference
- `architecture.html`: launch pipeline and implementation boundaries
- `security.html`: authentication, data handling, downloads, and trust boundaries
- `release.html`: current and historical website-hosted release ZIPs
