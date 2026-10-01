# CT Loader Static Site

This folder is a static documentation website. No build step or server-side runtime is required.

## Preview

Open `index.html` in a browser. The site uses relative links and shared styles in `assets/site.css`.

## Publish

Configure your static host to publish the contents of this `HTML/` directory, with `index.html` as the entry page. Keep the `assets/` directory and all four HTML pages together so the relative links and styling continue to work.

The favicon is loaded from `https://shit.pub/favicon.ico` as requested; the published site needs network access to that URL for the icon to appear.

## Pages

- `index.html`: project overview
- `updates.html`: CT Loader update notes
- `mods.html`: CT mod installation and authoring guide
- `architecture.html`: launch pipeline and implementation boundaries