<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=$outputCharset$">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>$title$ · PIT mutation report</title>
    <link rel="stylesheet" href="$root$style.css">
    <script src="$root$report.js"></script>
</head>
<body>
<a class="skip-link" href="#main">Skip to report</a>
<header class="topbar">
    <a class="brand" href="$root$index.html" aria-label="PIT report overview"><span class="brand-mark" aria-hidden="true">p<span>ı</span>t</span><span class="brand-label">Mutation report</span></a>
    <div class="topbar-actions"><span class="report-tag">PIT ${project.version}</span><button class="theme-toggle" type="button" data-theme-toggle hidden>Switch theme</button><a href="https://pitest.org" class="docs-link">Documentation ↗</a></div>
</header>
<div class="app-layout">
<aside class="sidebar" aria-label="Report navigation">
    <div class="nav-label">REPORT</div>
    <nav>
        <a class="nav-item $if(overview)$is-active$endif$" href="$root$index.html"><span aria-hidden="true">◫</span> Overview</a>
        <a class="nav-item $if(packageView)$is-active$endif$" href="$root$index.html#packages"><span aria-hidden="true">▤</span> Packages</a>
        $if(sourceView)$<a class="nav-item is-active" href="#source"><span aria-hidden="true">⌘</span> Source detail</a>$endif$
    </nav>
    <div class="sidebar-guide">
        <div class="nav-label">READING YOUR REPORT</div>
        <p>Line coverage tells you what ran. Mutation testing tells you what your tests caught.</p>
        <a href="$root$index.html#understanding">Understand the metrics <span aria-hidden="true">→</span></a>
    </div>
    <div class="sidebar-bottom"><span class="status-dot"></span> Static HTML report</div>
</aside>
<main id="main" class="main-content">
