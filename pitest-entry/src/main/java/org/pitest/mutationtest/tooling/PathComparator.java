package org.pitest.mutationtest.tooling;

import java.util.Comparator;

/**
 * Comparator to allow ordering of Objects by their closeness to a
 * given root, assuming their toString provides a path like hierarchy.
 *
 * Allows paths in same module as a file to be examined before those in other modules
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
            if (!a[i].equals(baseParts[i])) {
                return i;
            }
        }

        return baseParts.length;
    }
}
