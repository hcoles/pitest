package org.pitest.mutationtest.tooling;

import java.util.Comparator;

/**
 * Comparator to allow ordering of Objects by their closeness to a
 * given root, assuming their toString provides a path like hierarchy.
 * <p>
 * Allows paths in same module as a file to be examined before those in other modules.
 *
 * Expects the base path supplied to be deeper than the shared root (e.g.
 * /a/b/c/target/pit-reports when shared root is a/b/c)
 *
 */
class PathComparator implements Comparator<Object> {

    private final String[] baseParts;
    private final String separator;

    PathComparator(Object base, String separator) {
        this.separator = separator.replace("\\", "\\\\");
        this.baseParts = base.toString().split(this.separator);
    }

    @Override
    public int compare(Object o1, Object o2) {
        int a = distanceFromBase(o1.toString());
        int b = distanceFromBase(o2.toString());

        return b - a;
    }

    private int distanceFromBase(String s1) {
        String[] a = s1.split(separator);

        for (int i = 0; i != baseParts.length; i++) {
            if (a.length == i || !a[i].equals(baseParts[i])) {
                return i;
            }

            if (a.length - 1 == i) {
                // horrible fudge. This path is shorter than our base, but matches everything in it.
                // We want to prioritise it above ones that mismatch, so we use a magic 1000. So long
                // as the directory structure is not bizarrely deep, short fully matching paths will appear
                // before longer mismatched paths.
                return i + 1000;
            }
        }

        return a.length;
    }
}
