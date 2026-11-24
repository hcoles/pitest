/* Optional enhancements. Navigation, source links and test details work without JavaScript. */
(function () {
    "use strict";

    // Loaded from <head> without defer so a saved theme applies before first paint.
    var root = document.documentElement;
    var preferredTheme = window.matchMedia("(prefers-color-scheme: dark)");
    var savedTheme;
    try { savedTheme = localStorage.getItem("pit-report-theme"); } catch (ignored) { /* Storage may be blocked for local files. */ }
    if (savedTheme === "light" || savedTheme === "dark") { root.dataset.theme = savedTheme; }

    function isDark() {
        return root.dataset.theme ? root.dataset.theme === "dark" : preferredTheme.matches;
    }

    function initThemeToggle() {
        var themeToggle = document.querySelector("[data-theme-toggle]");
        if (!themeToggle) { return; }
        function updateThemeLabel() {
            themeToggle.textContent = isDark() ? "Light theme" : "Dark theme";
        }
        themeToggle.hidden = false;
        updateThemeLabel();
        if (preferredTheme.addEventListener) { preferredTheme.addEventListener("change", updateThemeLabel); }
        themeToggle.addEventListener("click", function () {
            root.dataset.theme = isDark() ? "light" : "dark";
            try { localStorage.setItem("pit-report-theme", root.dataset.theme); } catch (ignored) { /* Theme still works for this page. */ }
            updateThemeLabel();
        });
    }

    function initCoverageTable() {
        var table = document.querySelector("[data-coverage-table]");
        if (!table) { return; }
        var rows = Array.from(table.querySelectorAll("[data-report-row]"));
        var search = document.querySelector("[data-search]");
        var empty = document.querySelector("[data-empty]");
        var count = document.querySelector("[data-row-count]");
        document.querySelector("[data-search-control]").hidden = false;
        function filterRows() {
            var query = search.value.trim().toLowerCase();
            var visible = 0;
            rows.forEach(function (row) {
                row.hidden = row.cells[0].textContent.toLowerCase().indexOf(query) === -1;
                if (!row.hidden) { visible++; }
            });
            empty.hidden = visible !== 0 || rows.length === 0;
            count.textContent = visible + " of " + rows.length + " shown";
        }
        search.addEventListener("input", filterRows);
        filterRows();
        table.querySelectorAll("th[data-sort]").forEach(function (heading, index) {
            var button = document.createElement("button");
            var label = heading.textContent;
            button.type = "button";
            button.textContent = label;
            var indicator = document.createElement("span");
            indicator.className = "sort-indicator";
            indicator.setAttribute("aria-hidden", "true");
            indicator.textContent = "↕";
            button.appendChild(indicator);
            button.setAttribute("aria-label", "Sort by " + label.toLowerCase());
            heading.textContent = "";
            heading.appendChild(button);
            button.addEventListener("click", function () {
                var ascending = heading.getAttribute("aria-sort") !== "ascending";
                table.querySelectorAll("th[aria-sort]").forEach(function (other) {
                    other.removeAttribute("aria-sort");
                    other.querySelector(".sort-indicator").textContent = "↕";
                });
                heading.setAttribute("aria-sort", ascending ? "ascending" : "descending");
                indicator.textContent = ascending ? "↑" : "↓";
                rows.sort(function (a, b) {
                    var left = a.cells[index];
                    var right = b.cells[index];
                    var comparison = heading.dataset.sort === "number"
                        ? Number(left.dataset.value) - Number(right.dataset.value)
                        : left.textContent.localeCompare(right.textContent);
                    return ascending ? comparison : -comparison;
                });
                rows.forEach(function (row) { table.tBodies[0].appendChild(row); });
            });
        });
    }

    function initMutationFilter() {
        var outcome = document.querySelector("[data-mutation-filter]");
        if (!outcome) { return; }
        document.querySelector("[data-mutation-control]").hidden = false;
        function applyFilter() {
            var shown = 0;
            document.querySelectorAll(".mutation-group").forEach(function (group) {
                var groupVisible = false;
                group.querySelectorAll("[data-mutation-status]").forEach(function (mutation) {
                    var status = mutation.dataset.mutationStatus;
                    var matches = outcome.value === "all" || outcome.value === status ||
                        (outcome.value === "other" && ["SURVIVED", "NO_COVERAGE", "KILLED"].indexOf(status) === -1);
                    mutation.hidden = !matches;
                    if (matches) { groupVisible = true; shown++; }
                });
                group.hidden = !groupVisible;
            });
            document.querySelector("[data-mutation-empty]").hidden = shown > 0;
        }
        function showAll() {
            outcome.value = "all";
            applyFilter();
        }
        outcome.addEventListener("change", applyFilter);
        // Browsers may restore the previous selection on reload or back navigation.
        if (window.location.hash.indexOf("#group-") === 0) { showAll(); } else { applyFilter(); }
        // Reveal even when the source link points at the current hash.
        document.querySelectorAll(".mutation-count a").forEach(function (link) {
            link.addEventListener("click", showAll);
        });
        // Also handle back/forward navigation to a mutation group.
        window.addEventListener("hashchange", function () {
            if (window.location.hash.indexOf("#group-") === 0) {
                showAll();
                var group = document.getElementById(window.location.hash.slice(1));
                if (group) { group.scrollIntoView(); }
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initThemeToggle();
        initCoverageTable();
        initMutationFilter();
    });
}());
