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
    height : 1.1em;
    width: 130px;
    background: #FAA;
    margin: 0 5px;
    vertical-align: middle;
    border: 1px solid #AAA;
    position : relative;
   }
   
   .coverage_complete {
    display : inline-block;
    height : 100%;
    background: #DFD;
    float: left;
   }
   
   .coverage_ledgend {
        position : absolute;
        height : 100%;
        width : 100%;
        left : 0;
        top : 0;
        text-align : center;
   }
   
   .line, .mut {
        vertical-align : middle;
   }
   
   .coveragePercentage {
    display: inline-block;
    width: 3em;
    text-align: right;
   }
  </style>
  
</head>
<body>

<h1>Pit Test Coverage Report</h1>

<h3>Project Summary</h3>
<table>
    <thead>
        <tr>
            <th>Number of Classes</th>
            <th>Line Coverage</th>
            <th>Mutation Coverage</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>$numberOfClasses$</td>
            <td>$lineCoverage$% <div class="coverage_bar"><div class="coverage_complete" style="width:$lineCoverage$%"></div><div class="coverage_ledgend">$numberOfLinesCovered$/$numberOfLines$</div></div></td>
            <td>$mutationCoverage$% <div class="coverage_bar"><div class="coverage_complete" style="width:$mutationCoverage$%"></div><div class="coverage_ledgend">$numberOfMutationsDetected$/$numberOfMutations$</div></div></td>
        </tr>
    </tbody>
</table>


<h3>Breakdown by Package</h3>
<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Number of Classes</th>
            <th>Line Coverage</th>
            <th>Mutation Coverage</th>
        </tr>
    </thead>
    <tbody>
$packageSummaries:{ summary | 
        <tr>
            <td><a href="./$summary.packageDirectory$/index.html">$summary.packageName$</a></td>
            <td>$summary.numberOfClasses$</td>
            <td><div class="coveragePercentage">$summary.lineCoverage$% </div><div class="coverage_bar"><div class="coverage_complete" style="width:$summary.lineCoverage$%"></div><div class="coverage_ledgend">$summary.numberOfLinesCovered$/$summary.numberOfLines$</div></div></td>
            <td><div class="coveragePercentage">$summary.mutationCoverage$% </div><div class="coverage_bar"><div class="coverage_complete" style="width:$summary.mutationCoverage$%"></div><div class="coverage_ledgend">$summary.numberOfMutationsDetected$/$summary.numberOfMutations$</div></div></td>
        </tr>
}$
     </tbody>
</table>
<br/>


$errors : { error | 
<div class='unit'>
 $error$
</div>
<br/>
}$

<hr/>

</body>
</html>