$templates/mutation/header(root="../", title=sourceFile.fileName, sourceView="yes")$
<nav class="breadcrumb" aria-label="Breadcrumb"><a href="../index.html">Project</a><span>/</span><a href="index.html">$packageName$</a><span>/</span><span aria-current="page">$sourceFile.fileName$</span></nav>
<div class="page-heading"><div><div class="eyebrow">SOURCE REPORT</div><h1>$sourceFile.fileName$</h1><p class="page-description">Follow each mutation from source code to test outcome.</p></div><a class="button-link" href="#mutations">Explore mutations ↓</a></div>
$templates/mutation/summary(totals=totals)$
<section id="source" class="panel source-panel" aria-labelledby="source-title">
    <div class="panel-heading"><h2 id="source-title">Source code</h2><div class="legend">$if(showCoverage)$<span><i class="legend-dot covered"></i>Covered line</span><span><i class="legend-dot uncovered"></i>Uncovered line</span>$endif$<span><i class="legend-dot killed"></i>Detected</span><span><i class="legend-dot survived"></i>Undetected</span><span><i class="legend-dot uncertain"></i>Uncertain</span></div></div>
    $if(sourceFile.lines)$
    <div class="table-scroll"><table class="src"><caption class="visually-hidden">Annotated source code. Select a mutation count to see its details.</caption><tbody>
    $sourceFile.lines:{ line |
    <tr id="line-$line.number$"><td class="line-number $line.styles.lineCoverage$"><a href="#line-$line.number$" aria-label="Line $line.number$">$line.number$</a></td><td class="mutation-count $line.styles.mutation$">$if(line.mutations)$<a href="#group-$line.number$" title="View mutations on line $line.number$">$line.numberOfMutationsForDisplay$</a>$endif$</td><td class="source-code $line.styles.code$"><pre><span class="$line.styles.mutation$">$line.text$</span></pre></td></tr>
    }$
    </tbody></table></div>
    $else$<p class="empty-state">Source code could not be located. Mutation details are available below.</p>$endif$
    <div class="panel-footnote">Select a mutation count beside a line to inspect its results.</div>
</section>
<section id="mutations" class="panel" aria-labelledby="mutations-title">
    <div class="panel-heading"><div><h2 id="mutations-title">Mutations</h2><p>Inspect the changes PIT introduced and how your tests responded.</p></div><label class="filter-field" data-mutation-control hidden>Show <select data-mutation-filter aria-label="Filter mutations by outcome"><option value="all">All outcomes</option><option value="SURVIVED">Survived</option><option value="NO_COVERAGE">No coverage</option><option value="KILLED">Killed</option><option value="other">Other outcomes</option></select></label></div>
    <div class="mutation-list">
    $sourceFile.groups:{ group |
    <section class="mutation-group" id="group-$group.id$"><h3>$if(sourceFile.lines)$<a href="#line-$group.id$">Line $group.id$ <span aria-hidden="true">↑</span></a>$else$Line $group.id$$endif$</h3>
        $group.mutations:{ mutation |
        <article class="mutation-item $mutation.status$" data-mutation-status="$mutation.status$"><div class="mutation-description"><span class="status-badge $mutation.status$">$mutation.statusDescription$</span><p>$mutation.details.htmlSafeDescription$</p></div><div class="mutation-meta"><span>Method <code>$mutation.details.location$</code></span><span>Mutator <code>$mutation.details.mutator$</code></span>$if(mutation.killingTests)$<span>Killed by <code>$mutation.killingTestDescription$</code></span>$endif$</div>
        $if(mutation.survived)$<details class="covered-tests"><summary>Covering tests</summary>$if(mutation.coveringTests)$<ul>$mutation.coveringTests:{ test |<li><code>$test$</code></li>}$</ul>$else$<p>No covering tests were recorded for this mutation.</p>$endif$</details>$endif$
        </article>
        }$
    </section>
    }$
    </div>
    $if(sourceFile.groups)$$else$<p class="empty-state">No mutations were recorded for this source file.</p>$endif$
    <p class="empty-state" data-mutation-empty hidden>No mutations match this outcome.</p>
</section>
<div class="detail-grid"><details class="panel detail-panel"><summary>Active mutators</summary><ul>$mutators:{ mutator |<li><code>$mutator$</code></li>}$</ul></details><details class="panel detail-panel"><summary>Tests examined</summary><ul>$tests:{ test |<li><code>$test.name$</code> <span class="muted">($test.time$ ms)</span></li>}$</ul></details></div>
$templates/mutation/footer()$
