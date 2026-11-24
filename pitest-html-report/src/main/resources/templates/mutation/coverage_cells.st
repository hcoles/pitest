$if(showCoverage)$
<td class="coverage-cell" data-value="$totals.lineCoverageLabel$"><div class="cell-score">$totals.lineCoverageLabel$% <span>$totals.numberOfLinesCovered$ / $totals.numberOfLines$</span></div><div class="metric-track"><div class="coverage_complete width-$totals.lineCoverage$"></div></div></td>
$else$
<td class="coverage-cell" data-value="-1"><span class="muted">n/a</span></td>
$endif$
<td class="coverage-cell" data-value="$totals.mutationCoverageLabel$"><div class="cell-score">$totals.mutationCoverageLabel$% <span>$totals.numberOfMutationsDetected$ / $totals.numberOfMutations$</span></div><div class="metric-track"><div class="coverage_complete width-$totals.mutationCoverage$"></div></div></td>
<td class="coverage-cell" data-value="$totals.testStrengthLabel$"><div class="cell-score">$totals.testStrengthLabel$% <span>$totals.numberOfMutationsDetected$ / $totals.numberOfMutationsWithCoverage$</span></div><div class="metric-track"><div class="coverage_complete width-$totals.testStrength$"></div></div></td>
