(function () {
    "use strict";

    var site = {
        product: "CT Client",
        caption: "MINECRAFT JAVA LAUNCHER / MOD LOADER",
        version: "2.2.0.1",
        pages: [
            { key: "overview", label: "Overview", href: "index.html" },
            { key: "features", label: "Features", href: "features.html" },
            { key: "start", label: "Get started", href: "getting-started.html" },
            { key: "mods", label: "Mods", href: "mods.html" },
            { key: "cli", label: "CLI", href: "cli.html" },
            { key: "settings", label: "Settings", href: "settings.html" },
            { key: "api", label: "API", href: "api.html" },
            { key: "download", label: "Downloads", href: "release.html" },
            { key: "updates", label: "Updates", href: "updates.html" },
            { key: "faq", label: "FAQ", href: "troubleshooting.html" },
            { key: "security", label: "Security", href: "security.html" },
            { key: "discord", label: "Discord", href: "discord.html" }
        ],
        links: [
            { label: "Downloads", href: "release.html" },
            { label: "Features", href: "features.html" },
            { label: "Mods guide", href: "mods.html" },
            { label: "Troubleshooting", href: "troubleshooting.html" },
            { label: "Discord community", href: "discord.html" }
        ]
    };

    var base = document.body ? (document.body.getAttribute("data-base") || "") : "";
    var section = document.body ? document.body.getAttribute("data-nav") : null;
    var currentPath = window.location.pathname;

    function isActive(page) {
        if (section) {
            return page.key === section;
        }
        try {
            return new URL(base + page.href, window.location.href).pathname === currentPath;
        } catch (error) {
            return page.href.split("/").pop() === (currentPath.split("/").pop() || "index.html");
        }
    }

    function renderHeader() {
        var header = document.querySelector("[data-site-header]");
        if (!header) {
            return;
        }
        var links = site.pages.map(function (page) {
            return '<a class="nav-link" href="' + base + page.href + '"' +
                (isActive(page) ? ' aria-current="page"' : "") + ">" + page.label + "</a>";
        }).join("");
        header.innerHTML =
            '<div class="header-inner">' +
                '<a class="brand" href="' + base + 'index.html" aria-label="' + site.product + ' home">' +
                    '<img src="https://shit.pub/favicon.ico" alt="CT site icon" width="36" height="36">' +
                    "<span>" +
                        '<span class="brand-name">' + site.product + "</span>" +
                        '<span class="brand-caption">' + site.caption + "</span>" +
                    "</span>" +
                "</a>" +
                '<nav class="site-nav" aria-label="Main navigation">' + links + "</nav>" +
            "</div>";
    }

    function renderFooter() {
        var footer = document.querySelector("[data-site-footer]");
        if (!footer) {
            return;
        }
        var note = footer.getAttribute("data-footer-note");
        var label = site.product.toUpperCase() + " " + site.version + (note ? " / " + note : "");
        var links = site.links.map(function (link) {
            return '<a href="' + base + link.href + '">' + link.label + "</a>";
        }).join("");
        footer.innerHTML = '<div class="site-footer-inner"><span>' + label + "</span>" + links + "</div>";
    }

    function applyVersion() {
        document.querySelectorAll("[data-ct-version]").forEach(function (node) {
            node.textContent = site.version;
        });
        document.querySelectorAll("[data-download]").forEach(function (link) {
            link.setAttribute("href", base + "downloads/CT-Client-" + site.version + ".zip");
        });
    }

    function watchScroll() {
        var header = document.querySelector(".site-header");
        if (!header) {
            return;
        }
        var update = function () {
            header.classList.toggle("is-scrolled", window.scrollY > 8);
        };
        update();
        window.addEventListener("scroll", update, { passive: true });
    }

    renderHeader();
    renderFooter();
    applyVersion();
    watchScroll();
})();
