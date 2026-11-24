<section class="metrics" aria-label="Coverage summary">
    <article class="metric-card metric-primary">
        <div class="metric-label">Mutation coverage <span class="metric-icon" aria-hidden="true">◎</span></div>
        <div class="metric-value">$totals.mutationCoverageLabel$<span>%</span></div>
        <div class="metric-track"><div class="coverage_complete width-$totals.mutationCoverage$"></div></div>
        <p><strong>$totals.numberOfMutationsDetected$</strong> of $totals.numberOfMutations$ mutations detected</p>
    </article>
    <article class="metric-card">
        <div class="metric-label">Line coverage <span class="metric-icon" aria-hidden="true">≋</span></div>
        $if(showCoverage)$
        <div class="metric-value">$totals.lineCoverageLabel$<span>%</span></div>
        <div class="metric-track"><div class="coverage_complete width-$totals.lineCoverage$"></div></div>
        <p><strong>$totals.numberOfLinesCovered$</strong> of $totals.numberOfLines$ lines covered</p>
        $else$
        <div class="metric-value metric-unavailable">n/a</div>
        <p>Line coverage is unavailable for this run</p>
        $endif$
    </article>
    <article class="metric-card">
        <div class="metric-label">Test strength <span class="metric-icon" aria-hidden="true">◇</span></div>
        <div class="metric-value">$totals.testStrengthLabel$<span>%</span></div>
        <div class="metric-track"><div class="coverage_complete width-$totals.testStrength$"></div></div>
        <p><strong>$totals.numberOfMutationsDetected$</strong> of $totals.numberOfMutationsWithCoverage$ covered mutations detected</p>
    </article>
</section>
