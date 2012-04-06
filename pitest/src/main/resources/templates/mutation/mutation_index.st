<html>
<head>
  
    <style type='text/css'>
    $css$
   
   .tests{ 
     width : 50%;
     float : left;
   }
   
   .mutees{
     float : right;
     width : 50%;
   }
   
   .unit {
     padding-top : 20px;
     clear : both;
   }
   
   .coverage_bar {
    display : inline-block;
    height : 1.5em;
    width: 130px;
    background: #FAA;
    margin: 0 5px;
    vertical-align: middle;
    border: 1px solid #AAA;
   }
   
   .coverage_complete {
    display : inline-block;
    height : 100%;
    background: #DFD;
    float: left;
   }
   
   .line, .mut {
        vertical-align : middle;
   }
  </style>
  
</head>
<body>

<h1>Detected $numberOfDetectedMutations$ of $numberOfMutations$ mutations in around $duration$ secs</h1>



$summaryList:{ summary | 
<div class='unit'>
  <h1><a href='$summary.fileName$'>$summary.fileName$</a></h1>
  <span>
    <span class='line'>Line coverage $summary.lineCoverage$%</span>
    <div class="coverage_bar">
        <div class="coverage_complete" style="width:$summary.lineCoverage$%"></div>
    </div> 
    <span class='mut'>Mutation coverage $summary.mutationCoverage$%</span>
    <div class="coverage_bar">
        <div class="coverage_complete" style="width:$summary.mutationCoverage$%"></div>
    </div>
</div>
<br/>

<span class='tests'>
  <h2>Tests</h2>
  $summary.testClasses : { clazz | $clazz$  }$ 
</span>
<span class='mutees'>
  <h2>Mutated classes</h2>
  $summary.mutatedClasses : { clazz | $clazz$ }$ 
</span>
</div>
<br/>
}$

$errors : { error | 
<div class='unit'>
 $error$
</div>
<br/>
}$

<hr/>

$mutatorScores.totals : { score | <h2>$score$</h2> }$ 

</body>
</html>