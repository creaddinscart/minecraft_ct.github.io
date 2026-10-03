# CT Official Website

This folder is the complete static website and CT download portal. Publish the contents of `HTML/` to the official website's static hosting root. Keep the `downloads/` folder and `assets/` directory at their relative paths so the site's links continue to work.

## Shared Site Data

`assets/site.js` is the single source of truth for everything repeated across pages: the product name, the current CT version, the navigation bar, the module catalog, and the footer. It renders the header and footer into the `data-site-header` / `data-site-footer` placeholders, fills every `[data-ct-version]` element with the current version, points every `[data-download]` link at `downloads/CT-Main-<version>.jar`, every `[data-source-download]` link at `downloads/CT-Platform-<version>-src.zip`, and renders the module table into `[data-module-table]` from the `site.modules` array.

To ship a new release, edit `version` and, when the module list changed, `site.modules` in `assets/site.js` once. Page prose that names a specific release, such as release notes, still belongs in the page.

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

`data-base` is `""` for pages in `HTML/` and `"../"` for pages in `HTML/docs/`. `data-nav` selects the highlighted navigation item. `data-footer-note` sets the footer section label. A new page needs only these attributes and its own `main` content.

## Website Releases

1. Update the release record under `versions/CT vX.Y.Z[.BUILD]/`.
2. Run `./mvnw package` to build CT-Main and the module jars.
3. Copy `ct-main/target/CT-Main-X.Y.Z.jar` into `HTML/downloads/`, each `ct-module-*/target/ct-module-<name>-X.Y.Z.jar` into `HTML/downloads/modules/`, and refresh `HTML/downloads/CT-Platform-X.Y.Z-src.zip` plus the `HTML/code/` source tree.
4. Update `version` (and `site.modules` when it changed) in `assets/site.js` and adjust page prose that names the release.
5. Publish the updated `HTML/` directory to the official website.
6. Verify the Release page downloads and the direct CT-Main, module jar, and source archive URLs from the public site.

The download set is CT-Main (the bare host), one jar per feature module, and the source archive. It does not contain Minecraft game files. The historical all-in-one `CT-Client-*.zip` packages remain in `downloads/` for older releases.

## Website Capabilities

The static website presents CT releases, the module catalog, and the open-source tree. Browsers cannot install desktop software, access arbitrary local files, or launch Minecraft processes. Download CT-Main and the module jars, run CT-Main locally, and it fetches Minecraft data directly from Mojang's official endpoints.

## Pages

- `index.html`: overview and CT-Main download
- `modules.html`: the module catalog with per-module jar downloads
- `code.html`: the open-source tree and source archive
- `docs/index.html`: wiki index and documentation categories
- `docs/fabric-sodium.html`: Fabric Loader and Sodium installation guide
- `updates.html`: current release notes
- `getting-started.html`: assembling the launcher from modules and launching
- `settings.html`: settings and local directory layout
- `mods.html`: CT-native mod guide
- `api.html`: Java API reference
- `architecture.html`: the module platform, install pipeline, and boundaries
- `cli.html`: the `ct` command line module reference
- `security.html`: authentication, data handling, downloads, and trust boundaries
- `release.html`: CT-Main, module jars, the source archive, and historical release ZIPs
- `discord.html`: CT community Discord server, posting guidance, and quick links
