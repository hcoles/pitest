package org.pitest.mutationtest.tooling;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class PathComparatorTest {

    @Test
    public void identicalPathsCompareAsZero() {
        File base = new File("start/end");
        PathComparator underTest = new PathComparator(base, File.separator);

        assertThat(underTest.compare(base,base)).isEqualTo(0);
    }

    @Test
    public void identicalPathsCompareAsZeroWhenNotUnderBase() {
        PathComparator underTest = new PathComparator(new File("different/path"), File.separator);
        File a = new File("start/end");
        assertThat(underTest.compare(a,a)).isEqualTo(0);
    }

    @Test
    public void sameRootedPathComparesHigher() {
        File base = new File("start/end");
        PathComparator underTest = new PathComparator(base, File.separator);
        File sameRoot = new File("start/end/leaf");
        File differentRoot = new File("start/different/leaf");
        assertThat(underTest.compare(differentRoot, sameRoot)).isEqualTo(1);
        assertThat(underTest.compare(sameRoot, differentRoot)).isEqualTo(-1);
    }

    @Test
    public void sortsPathsClosestToBaseFirst() {
        File a = new File("start/pit-sub-module/sub-module-2/src/main/java");
        File b = new File("start/pit-sub-module/sub-module-2/src/main/java/org");
        File c = new File("start/pit-sub-module/sub-module-1/src/main/java");
        File d = new File("start/pit-sub-module/sub-module-1/src/main/java/org");
        File e = new File("start/pit-sub-module/sub-module-longer/src/main/java/org");

        PathComparator underTest = new PathComparator(new File("start/pit-sub-module/sub-module-1/target/pit-reports"), File.separator);

        List<File> files = asList(a, b, c, d, e);
        files.sort(underTest);
        assertThat(files).containsExactly(c, d, a, b, e);

        underTest = new PathComparator(new File("start/pit-sub-module/sub-module-2/target/pit-reports"), File.separator);
        files.sort(underTest);
        assertThat(files).containsExactly(a, b, c, d, e);

    }

    @Test
    public void worksWithBackSlashSeparator() {
        PathComparator underTest = new PathComparator("a\\b", "\\");
        List<String> paths = asList("a\\z", "a\\b", "a\\b\\c");
        paths.sort(underTest);
        assertThat(paths).containsExactly("a\\b", "a\\b\\c", "a\\z");
    }

    @Test
    public void worksWithLeadingSlashSeparator() {
        PathComparator underTest = new PathComparator("\\a\\b", "\\");
        List<String> paths = asList("\\a\\z", "\\a\\b", "\\a\\b\\c");
        paths.sort(underTest);
        assertThat(paths).containsExactly("\\a\\b", "\\a\\b\\c", "\\a\\z");
    }

}