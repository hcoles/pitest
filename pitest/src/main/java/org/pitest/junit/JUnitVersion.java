package org.pitest.junit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JUnitVersion implements Comparable<JUnitVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+).*");

    public static JUnitVersion parse(final String version) {
        final Matcher matcher = VERSION_PATTERN.matcher(version);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version string! Could not parse " + version);
        }

        final int major = Integer.parseInt(matcher.group(1));
        final int minor = Integer.parseInt(matcher.group(2));

        return new JUnitVersion(major, minor);
    }

    private final int major;

    private final int minor;

    private JUnitVersion(final int major, final int minor) {
        this.major = major;
        this.minor = minor;
    }

    @Override
    public int compareTo(final JUnitVersion that) {

        if (that == null) {
            return 1;
        }

        if (this.major != that.major) {
            return this.major - that.major;
        }

        if (this.minor != that.minor) {
            return this.minor - that.minor;
        }

        return 0;
    }

    public boolean is(final JUnitVersion version) {
        return equals(version);
    }

    public boolean isGreaterThan(final JUnitVersion version) {
        return compareTo(version) > 0;
    }

    public boolean isGreaterThanOrEqualTo(final JUnitVersion version) {
        return compareTo(version) >= 0;
    }

    public boolean isLessThan(final JUnitVersion version) {
        return compareTo(version) < 0;
    }

    public boolean isLessThanOrEqualTo(final JUnitVersion version) {
        return compareTo(version) <= 0;
    }

    @Override
    public int hashCode() {
        int result = this.major;
        result = (31 * result) + this.minor;
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JUnitVersion)) {
            return false;
        }

        final JUnitVersion that = (JUnitVersion) o;

        return (this.major == that.major) && (this.minor == that.minor);

    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
}
