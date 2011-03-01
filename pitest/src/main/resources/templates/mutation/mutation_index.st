<html>
<head>
  <link media="screen, projection" type="text/css" href="style.css" rel="stylesheet" />
  
    <style type='text/css'>

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
  </style>
  
</head>
<body>

<h1>Detected $numberOfDetectedMutations$ of $numberOfMutations$ mutations in around $duration$ secs</h1>



$summaryList:{ summary | 
<div class='unit'>
  <h1><a href='$summary.fileName$'>$summary.fileName$</a></h1>
  <span class='line'>Line coverage $summary.lineCoverage$%</span> <span class='mut'>Mutation coverage $summary.mutationCoverage$%</span>
<br/>

<span class='tests'>
  <h2>Tests</h2>
  $summary.testClasses : { clazz | $clazz$  }$ 
</span>
<span class='mutees'>
  <h2>Mutated classes</h2>
  $summary.mutatedClasses : { clazz | $clazz.name$ }$ 
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