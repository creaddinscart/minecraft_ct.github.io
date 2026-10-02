(function () {
    "use strict";

    var site = {
        product: "CT Client",
        caption: "MINECRAFT JAVA LAUNCHER",
        version: "3.0.0.1",
        pages: [
            { key: "overview", label: "Home", href: "index.html" },
            { key: "beginner", label: "Beginner", href: "beginner.html" },
            { key: "features", label: "Features", href: "features.html" },
            { key: "mods", label: "Mods", href: "mods.html" },
            { key: "create", label: "Make a mod", href: "create-a-mod.html" },
            { key: "cli", label: "CLI", href: "cli.html" },
            { key: "settings", label: "Config", href: "settings.html" },
            { key: "api", label: "API", href: "api.html" },
            { key: "download", label: "Downloads", href: "release.html" },
            { key: "faq", label: "FAQ", href: "troubleshooting.html" },
            { key: "about", label: "About", href: "about.html" }
        ],
        links: [
            { label: "Beginner guide", href: "beginner.html" },
            { label: "Features", href: "features.html" },
            { label: "Mods guide", href: "mods.html" },
            { label: "Downloads", href: "release.html" },
            { label: "Troubleshooting", href: "troubleshooting.html" }
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
                    '<img src="https://shit.pub/favicon.ico" alt="CT site icon" width="32" height="32">' +
                    "<span>" +
                        '<span class="brand-name">' + site.product + "</span>" +
                        '<span class="brand-caption">' + site.caption + "</span>" +
                    "</span>" +
                "</a>" +
                '<div class="header-search" role="search">' +
                    '<span class="search-glyph" aria-hidden="true">⌕</span>' +
                    '<input type="search" placeholder="Search guides, settings, API…" aria-label="Search the site" autocomplete="off" spellcheck="false">' +
                    '<div class="search-results" role="listbox"></div>' +
                "</div>" +
                '<button class="nav-toggle" type="button" aria-expanded="false" aria-controls="site-nav">Menu</button>' +
                '<nav class="site-nav" id="site-nav" aria-label="Main navigation">' + links + "</nav>" +
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
        }).join(" · ");
        var license = 'Documentation licensed under <a rel="license" href="' + base + 'license.html">CC BY-SA 4.0</a>';
        footer.innerHTML = '<div class="site-footer-inner"><span>' + label + "</span><span>" + links + "</span><span>" + license + "</span></div>";
    }

    function applyVersion() {
        if (!document.querySelector('link[rel="license"]')) {
            var license = document.createElement("link");
            license.setAttribute("rel", "license");
            license.href = base + "license.html";
            document.head.appendChild(license);
        }
        document.querySelectorAll("[data-ct-version]").forEach(function (node) {
            node.textContent = site.version;
        });
        document.querySelectorAll("[data-download]").forEach(function (link) {
            link.setAttribute("href", base + "downloads/CT-Client-" + site.version + ".zip");
        });
    }

    var index = null;
    var input = null;
    var panel = null;
    var activeHit = -1;

    function loadIndex() {
        if (index) {
            return;
        }
        if (window.CT_SEARCH) {
            index = window.CT_SEARCH.entries || [];
            runSearch();
            return;
        }
        var script = document.createElement("script");
        script.src = base + "assets/search-data.js";
        script.onload = function () {
            index = window.CT_SEARCH ? (window.CT_SEARCH.entries || []) : [];
            runSearch();
        };
        script.onerror = function () {
            index = [];
        };
        document.head.appendChild(script);
    }

    function score(entry, terms) {
        var total = 0;
        var title = entry.t.toLowerCase();
        var keywords = (entry.k || "").toLowerCase();
        for (var i = 0; i < terms.length; i++) {
            if (title.indexOf(terms[i]) !== -1) {
                total += 6;
            }
            if (keywords.indexOf(terms[i]) !== -1) {
                total += 3;
            }
            if (terms[i].length > 2 && (title + keywords).indexOf(terms[i]) === -1) {
                return 0;
            }
        }
        return total;
    }

    function resultsFor(query) {
        var terms = query.toLowerCase().split(/\s+/).filter(Boolean);
        if (!terms.length) {
            return [];
        }
        return index
            .map(function (entry) {
                return { entry: entry, s: score(entry, terms) };
            })
            .filter(function (item) {
                return item.s > 0;
            })
            .sort(function (a, b) {
                return b.s - a.s;
            })
            .slice(0, 14)
            .map(function (item) {
                return item.entry;
            });
    }

    function renderResults(hits) {
        panel.innerHTML = "";
        activeHit = -1;
        if (!hits.length) {
            panel.innerHTML = '<div class="search-empty">No matches. Try "mods", "memory", or "device code".</div>';
            panel.classList.add("open");
            return;
        }
        var lastCategory = null;
        hits.forEach(function (entry) {
            if (entry.c !== lastCategory) {
                lastCategory = entry.c;
                var group = document.createElement("div");
                group.className = "search-group";
                group.textContent = entry.c;
                panel.appendChild(group);
            }
            var link = document.createElement("a");
            link.className = "search-hit";
            link.href = base + entry.u;
            link.innerHTML = "";
            link.textContent = entry.t;
            var sub = document.createElement("small");
            sub.textContent = (entry.k || "").slice(0, 90);
            link.appendChild(sub);
            panel.appendChild(link);
        });
        panel.classList.add("open");
    }

    function runSearch() {
        if (!input || !panel) {
            return;
        }
        var query = input.value.trim();
        if (!query) {
            panel.classList.remove("open");
            return;
        }
        if (!index) {
            loadIndex();
            panel.innerHTML = '<div class="search-empty">Loading search…</div>';
            panel.classList.add("open");
            return;
        }
        renderResults(resultsFor(query));
    }

    function moveActive(delta) {
        var hits = panel.querySelectorAll(".search-hit");
        if (!hits.length) {
            return;
        }
        activeHit = (activeHit + delta + hits.length) % hits.length;
        hits.forEach(function (hit, i) {
            hit.classList.toggle("active", i === activeHit);
        });
        hits[activeHit].scrollIntoView({ block: "nearest" });
    }

    function bindSearch() {
        var wrap = document.querySelector(".header-search");
        if (!wrap) {
            return;
        }
        input = wrap.querySelector("input");
        panel = wrap.querySelector(".search-results");
        input.addEventListener("input", runSearch);
        input.addEventListener("focus", function () {
            loadIndex();
            if (input.value.trim()) {
                runSearch();
            }
        });
        input.addEventListener("keydown", function (event) {
            if (event.key === "ArrowDown") {
                event.preventDefault();
                moveActive(1);
            } else if (event.key === "ArrowUp") {
                event.preventDefault();
                moveActive(-1);
            } else if (event.key === "Enter") {
                var hits = panel.querySelectorAll(".search-hit");
                if (activeHit >= 0 && hits[activeHit]) {
                    window.location.href = hits[activeHit].href;
                } else if (hits.length) {
                    window.location.href = hits[0].href;
                }
            } else if (event.key === "Escape") {
                panel.classList.remove("open");
            }
        });
        document.addEventListener("click", function (event) {
            if (!wrap.contains(event.target)) {
                panel.classList.remove("open");
            }
        });
    }

    function bindNavToggle() {
        var toggle = document.querySelector(".nav-toggle");
        if (!toggle) {
            return;
        }
        toggle.addEventListener("click", function () {
            var open = document.body.classList.toggle("nav-open");
            toggle.setAttribute("aria-expanded", open ? "true" : "false");
        });
    }

    function watchScroll() {
        var header = document.querySelector(".site-header");
        if (!header) {
            return;
        }
        var update = function () {
            header.classList.toggle("is-scrolled", window.scrollY > 8);
            header.style.boxShadow = window.scrollY > 8 ? "0 1px 0 rgba(15,23,42,0.06)" : "";
        };
        update();
        window.addEventListener("scroll", update, { passive: true });
    }

    renderHeader();
    renderFooter();
    applyVersion();
    bindSearch();
    bindNavToggle();
    watchScroll();
})();
