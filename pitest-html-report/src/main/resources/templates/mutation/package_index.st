$templates/mutation/header(root="../", title=packageData.packageName, packageView="yes")$
<nav class="breadcrumb" aria-label="Breadcrumb"><a href="../index.html">Project</a><span>/</span><a href="../index.html#packages">Packages</a><span>/</span><span aria-current="page">$packageData.packageName$</span></nav>
<div class="page-heading"><div><div class="eyebrow">PACKAGE REPORT</div><h1 class="code-heading">$packageData.packageName$</h1><p class="page-description">Explore source coverage and the mutations behind it.</p></div><span class="scope-tag">$packageData.totals.numberOfFiles$ source files</span></div>
$templates/mutation/summary(totals=packageData.totals)$
<section class="panel" aria-labelledby="files-title">
    <div class="panel-heading"><div><h2 id="files-title">Source files</h2><p>Select a file to inspect its source and mutation results.</p></div><label class="search-field" data-search-control hidden><span aria-hidden="true">⌕</span><input type="search" data-search placeholder="Filter files…" aria-label="Filter source files"></label></div>
    <div class="table-scroll"><table class="coverage-table" data-coverage-table>
        <caption class="visually-hidden">Coverage breakdown by source file</caption>
        <thead><tr><th scope="col" data-sort="text">Source file</th><th scope="col" data-sort="number">Line coverage</th><th scope="col" data-sort="number">Mutation coverage</th><th scope="col" data-sort="number">Test strength</th></tr></thead>
        <tbody>
        $packageData.summaryData:{ summary |
        <tr data-report-row><td class="name-cell"><a href="./$summary.fileName$.html"><span class="file-icon" aria-hidden="true">⌘</span>$summary.fileName$<span class="row-arrow" aria-hidden="true">↗</span></a></td>
        $templates/mutation/coverage_cells(totals=summary.totals)$
        </tr>
        }$
        </tbody>
    </table></div>
    <p class="empty-state" data-empty hidden>No source files match your search.</p>
    <div class="panel-footnote">Percentages are accompanied by their exact counts.<span data-row-count aria-live="polite"></span></div>
</section>
$errors:{ error |<div class="build-messages">$error$</div>}$
$templates/mutation/footer()$
