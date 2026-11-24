$templates/mutation/header(root="", title="Overview", overview="yes")$
<nav class="breadcrumb" aria-label="Breadcrumb"><span>Project</span><span>/</span><span aria-current="page">Overview</span></nav>
<div class="page-heading"><div><div class="eyebrow">MUTATION TESTING</div><h1>Project overview</h1><p class="page-description">A closer look at the effectiveness of your tests.</p></div><span class="scope-tag">$totals.numberOfFiles$ source files</span></div>
$templates/mutation/summary(totals=totals)$
<section id="packages" class="panel" aria-labelledby="packages-title">
    <div class="panel-heading"><div><h2 id="packages-title">Packages</h2><p>Explore your code. Find opportunities to strengthen your tests.</p></div><label class="search-field" data-search-control hidden><span aria-hidden="true">⌕</span><input type="search" data-search placeholder="Filter packages…" aria-label="Filter packages"></label></div>
    <div class="table-scroll"><table class="coverage-table" data-coverage-table>
        <caption class="visually-hidden">Coverage breakdown by package</caption>
        <thead><tr><th scope="col" data-sort="text">Package</th><th scope="col" data-sort="number">Files</th><th scope="col" data-sort="number">Line coverage</th><th scope="col" data-sort="number">Mutation coverage</th><th scope="col" data-sort="number">Test strength</th></tr></thead>
        <tbody>
        $packageSummaries:{ summary |
        <tr data-report-row><td class="name-cell"><a href="./$summary.packageDirectory$/index.html"><span class="file-icon" aria-hidden="true">▤</span>$summary.packageName$<span class="row-arrow" aria-hidden="true">↗</span></a></td><td data-value="$summary.totals.numberOfFiles$">$summary.totals.numberOfFiles$</td>
        $templates/mutation/coverage_cells(totals=summary.totals)$
        </tr>
        }$
        </tbody>
    </table></div>
    <p class="empty-state" data-empty hidden>No packages match your search.</p>
    $if(packageSummaries)$$else$<p class="empty-state">No mutation results were recorded for this run.</p>$endif$
    <div class="panel-footnote">Select a package to explore its source files.<span data-row-count aria-live="polite"></span></div>
</section>
<section id="understanding" class="metric-guide" aria-labelledby="guide-title"><div class="section-heading"><span class="eyebrow">BEYOND CODE COVERAGE</span><h2 id="guide-title">What do these numbers mean?</h2></div><div class="guide-grid"><article><span class="guide-number">01</span><h3>Mutation coverage</h3><p>The share of generated mutations detected by your test suite. Undetected mutations reveal opportunities for better tests.</p></article><article><span class="guide-number">02</span><h3>Line coverage</h3><p>The share of executable source lines reached by tests. A covered line can still contain a mutation your tests miss.</p></article><article><span class="guide-number">03</span><h3>Test strength</h3><p>Mutation coverage for code reached by tests. This excludes mutations with no coverage to focus on test effectiveness.</p></article></div></section>
$if(messages)$<section class="build-messages" aria-label="Run notes"><h2>Run notes</h2><ul>$messages:{ message |<li>$if(message.url)$<a href="$message.url$">$message.text$</a>$else$$message.text$$endif$</li>}$</ul></section>$endif$
$errors:{ error |<div class="build-messages">$error$</div>}$
$if(arcmutateMissing)$<p class="enhancement-note">Extend your mutation testing with <a href="https://www.arcmutate.com/">Arcmutate ↗</a></p>$endif$
$templates/mutation/footer()$
