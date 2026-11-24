# HTML mutation report

PIT's HTML reports provide a project overview, package reports, and annotated
source reports. They are static files with local CSS and optional JavaScript;
there are no external fonts, CDNs, or frontend build dependencies.

## Local preview

From the repository root, generate an illustrative report with the production
listener and templates:

```sh
./mvnw -pl pitest-html-report -am test \
  -Dtest=HtmlReportRenderingTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dpit.report.preview=true
```

Open `pitest-html-report/target/report-preview/index.html` in your browser.
You can open the file directly; a web server is optional. For a local server:

```sh
python3 -m http.server 8765 --bind 127.0.0.1 \
  --directory pitest-html-report/target/report-preview
```

Then visit <http://127.0.0.1:8765>.

The preview contains five packages, twelve source files, and examples of killed,
surviving, timed-out, and uncovered mutations. The figures are illustrative;
this is not a mutation analysis of PIT itself. `HtmlReportPreview` is the fixture
entry point and can also be run as a Java main class with the module's test
classpath.

## Report behavior

- Navigate from overview to package to annotated source, with breadcrumbs back.
- Filter package and file names, or sort any coverage-table column.
- Filter source mutations by outcome; source links reveal their target even
  when a filter would otherwise hide it.
- Expand surviving mutations' covering tests with native HTML disclosures.
- Switch light/dark themes. The system preference is used initially; a manual
  choice is remembered when local browser storage is available.
- Coverage numbers use PIT's existing calculations and configured precision.
  Exact numerator/denominator counts accompany percentages. File counts are
  labeled as source files, matching what the report data actually counts.
- Navigation, metric values, source links, and disclosures work when JavaScript
  is disabled, including under restrictive report-hosting policies.

Shared StringTemplate partials (`header`, `footer`, `summary`, `coverage_cells`)
keep all three report levels consistent. `MutationHtmlReportListener` writes
`style.css` and `report.js` alongside the overview. Tables can scroll on small
screens; print styles omit navigation and optional controls.

The layout takes inspiration from the clear navigation and restrained styling
of the [Infection guide](https://infection.github.io/guide/).
