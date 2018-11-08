## Creating issues

Please provide as much context as possible. If possible, link to a sample project reproducing your problem (yes this is
often a lot of work, but doing so will make it much easier to pinpoint and fix your problem).

A good way to structure your bug report is to use the following structure:
 - a short summary
 - step-by-step instructions to reproduce the problem
 - what you expected to happen
 - what actually happened
 - more info if appropriate (logs, links to samples, hypothesis, ...)

## Closing Issues

### Closing fixed issues

An issue should be closed right after the PR that fixes it is merged.

A comment should be added to indicate the version that will include the fix. Since the actual version is often unknown
at that time, the recommended format is `> x.y.z` with `x.y.z` the latest _released_ version. That way, it doesn't
matter if the version turns out to be minor, major or a bug fix, it's always easy to know if the version you are
currently running contains the fix or not.

### Closing old bugs reports

At the moment, we have a rather long backlog of issues. Some have already been fixed, some are probably so old that no
one cares anymore. To focus our efforts on the most pressing issues, we close old bugs that do not seem relevant
anymore.

Here is how it's done :
 - bugs without activity for more than a year are concerned
 - old bugs closing starts with the oldest ones
 - a comment is added to ask if the bug is still a problem (the author is pinged, but anyone can answer)
   - hopefully it is not a problem anymore, in that case it is closed
   - if it is still a problem it stays open
   - without answer within a month, the issue is closed
