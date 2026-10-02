# CT Official Website

This folder is the complete static website and CT download portal. Publish the contents of `HTML/` to the official website's static hosting root. Keep the `downloads/` folder and `assets/` directory at their relative paths so the site's links continue to work.

## Shared Site Data

`assets/site.js` is the single source of truth for everything repeated across pages: the product name, the current CT version, the navigation bar, and the footer. It renders the header and footer into the `data-site-header` / `data-site-footer` placeholders, fills every `[data-ct-version]` element with the current version, and points every `[data-download]` link at `downloads/CT-Client-<version>.zip`.

To ship a new release, edit `version` in `assets/site.js` once. Page prose that names a specific release, such as release notes, still belongs in the page.

Shared colors, typography, and component styles live in `assets/site.css`. Page-specific markup should reuse the existing classes; add new classes to `site.css`, not to inline style blocks.

Each page shell is:

```html
<body data-base="" data-nav="overview">
  <header class="site-header" data-site-header></header>
  <main class="page-wrap">...</main>
  <footer class="site-footer" data-site-footer data-footer-note="SECTION"></footer>
  <script src="assets/site.js" defer></script>
</body>
```

`data-base` is `""` for pages in `HTML/` and `"../"` for pages in `HTML/docs/`. `data-nav` selects the highlighted navigation item: `overview`, `download`, `docs`, or `discord`. `data-footer-note` sets the footer section label. A new page needs only these attributes and its own `main` content.

## Website Releases

1. Update the release record under `versions/CT vX.Y.Z[.BUILD]/`.
2. Run `./mvnw package` to build the current desktop ZIP into `HTML/downloads/CT-Client-X.Y.Z[.BUILD].zip`.
3. Update `version` in `assets/site.js` and adjust page prose that names the release.
4. Publish the updated `HTML/` directory to the official website.
5. Verify the Release page download and direct ZIP URL from the public site.

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
- `discord.html`: CT community Discord server, posting guidance, and quick links
