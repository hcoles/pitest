/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package sun.pitest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.SideEffect2;

public class CodeCoverageStoreTest {

  @Mock
  private InvokeReceiver receiver;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    CodeCoverageStore.init(this.receiver);
  }

  @After
  public void cleanUp() {
    CodeCoverageStore.resetAllStaticState();
  }

  @Test
  public void shouldRegisterNewClassesWithReceiver() {
    final int id = CodeCoverageStore.registerClass("Foo");
    verify(this.receiver).registerClass(id, "Foo");
  }

  @Test
  public void shouldGenerateNewClassIdForEachClass() {
    final int id = CodeCoverageStore.registerClass("Foo");
    final int id2 = CodeCoverageStore.registerClass("Bar");
    assertFalse(id == id2);
  }

  @Test
  public void shouldCodeAndEncodeWhenClassIdAndLineNumberAreAtMaximum() {
    final long value = CodeCoverageStore.encode(Integer.MAX_VALUE,
        Integer.MAX_VALUE);
    assertEquals(Integer.MAX_VALUE, CodeCoverageStore.decodeClassId(value));
    assertEquals(Integer.MAX_VALUE, CodeCoverageStore.decodeLineId(value));
  }

  @Test
  public void shouldCodeAndEncodeWhenClassIdAndLineNumberAreAtMinimum() {
    final long value = CodeCoverageStore.encode(Integer.MIN_VALUE, 0);
    assertEquals(Integer.MIN_VALUE, CodeCoverageStore.decodeClassId(value));
    assertEquals(0, CodeCoverageStore.decodeLineId(value));
  }

  @Test
  public void shouldCodeAndEncodeWhenClassIdAndLineNumberAreZero() {
    final long value = CodeCoverageStore.encode(0, 0);
    assertEquals(0, CodeCoverageStore.decodeClassId(value));
    assertEquals(0, CodeCoverageStore.decodeLineId(value));
  }

  @Test
  public void shouldClearHitCountersWhenReset() {
    final int classId = CodeCoverageStore.registerClass("foo");
    CodeCoverageStore.registerClassProbes(classId, 1);

    CodeCoverageStore.visitProbes(classId, 0, new boolean[] { true });
    CodeCoverageStore.reset();

    final Collection<Long> actual = CodeCoverageStore.getHits();
    assertEquals(Collections.emptyList(), actual);
  }

  @Test
  public void shouldBeSafeToAccessAcrossMultipleThreads()
      throws InterruptedException, ExecutionException {

    CodeCoverageStore.registerClass("foo");
    CodeCoverageStore.registerClassProbes(0, 1);

    final Callable<ConcurrentModificationException> read = makeReader();

    final ExecutorService pool = Executors.newFixedThreadPool(13);
    for (int i = 1; i != 13; i++) {
      pool.submit(makeWriter(i));
    }
    final Future<ConcurrentModificationException> future = pool.submit(read);
    pool.shutdown();

    assertNull(future.get());

  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation1() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0]);
      }

    };
    assertLineCombinations(1, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation2() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1]);
      }

    };
    assertLineCombinations(2, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation3() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2]);
      }

    };
    assertLineCombinations(3, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation4() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3]);
      }

    };
    assertLineCombinations(4, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation5() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4]);
      }

    };
    assertLineCombinations(5, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation6() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5]);
      }

    };
    assertLineCombinations(6, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation7() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6]);
      }

    };
    assertLineCombinations(7, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation8() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7]);
      }

    };
    assertLineCombinations(8, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation9() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8]);
      }

    };
    assertLineCombinations(9, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation10() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8], probes[9]);
      }

    };
    assertLineCombinations(10, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation11() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8], probes[9], probes[10]);
      }

    };
    assertLineCombinations(11, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation12() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8], probes[9], probes[10], probes[11]);
      }

    };
    assertLineCombinations(12, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation13() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8], probes[9], probes[10], probes[11], probes[12]);
      }

    };
    assertLineCombinations(13, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation14() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8], probes[9], probes[10], probes[11], probes[12],
            probes[13]);
      }

    };
    assertLineCombinations(14, se);
  }

  @Test
  public void shouldReportCorrectCoverageForSpecialisation15() {
    final SideEffect2<Integer, boolean[]> se = new SideEffect2<Integer, boolean[]>() {

      @Override
      public void apply(final Integer classId, final boolean[] probes) {
        CodeCoverageStore.visitProbes(classId, 0, probes[0], probes[1],
            probes[2], probes[3], probes[4], probes[5], probes[6], probes[7],
            probes[8], probes[9], probes[10], probes[11], probes[12],
            probes[13], probes[14]);
      }

    };
    assertLineCombinations(15, se);
  }

  private void assertLineCombinations(final int size,
      final SideEffect2<Integer, boolean[]> function) {
    ascendingPermuation(size, function);
    CodeCoverageStore.resetAllStaticState();
    descendingPermutation(size, function);
  }

  private void ascendingPermuation(final int size,
      final SideEffect2<Integer, boolean[]> function) {
    final int classId = CodeCoverageStore.registerClass("foo");
    CodeCoverageStore.registerClassProbes(classId, 15);
    final boolean[] probes = new boolean[size];

    function.apply(classId, probes);
    assertDoesNotHitLine(classId, 1, 2, 3);

    for (int i = 0; i != probes.length; i++) {
      probes[i] = true;
      function.apply(classId, probes);
      for (int j = 0; j <= i; j++) {
        assertHitsLine(classId, j);
      }
      for (int j = i + 1; j != probes.length; j++) {
        assertDoesNotHitLine(classId, j);
      }
    }
  }

  private void descendingPermutation(final int size,
      final SideEffect2<Integer, boolean[]> function) {
    final int classId = CodeCoverageStore.registerClass("foo");
    CodeCoverageStore.registerClassProbes(classId, 15);
    final boolean[] probes = new boolean[size];

    for (int i = probes.length - 1; i != 0; i--) {
      probes[i] = true;
      function.apply(classId, probes);
      for (int j = 0; j != i; j++) {
        assertDoesNotHitLine(classId, j);
      }
      for (int j = probes.length; j != i; j--) {
        assertHitsLine(classId, j - 1);
      }
    }
  }

  private void assertHitsLine(final int classId, final int... i) {
    final Collection<Long> actual = CodeCoverageStore.getHits();
    for (final int probe : i) {
      assertThat(actual).contains(CodeCoverageStore.encode(classId, probe));
    }
  }

  private void assertDoesNotHitLine(final int classId, final int... i) {
    final Collection<Long> actual = CodeCoverageStore.getHits();
    for (final int probe : i) {
      assertThat(actual).doesNotContain(
          CodeCoverageStore.encode(classId, probe));
    }
  }

  private Callable<ConcurrentModificationException> makeReader() {
    final Callable<ConcurrentModificationException> read = new Callable<ConcurrentModificationException>() {
      @Override
      public ConcurrentModificationException call() throws Exception {
        ConcurrentModificationException error = null;
        try {
          pointlesslyIterateCollection();
          pointlesslyIterateCollection();
          pointlesslyIterateCollection();
        } catch (final ConcurrentModificationException ex) {
          error = ex;
        }
        return error;
      }

      private long pointlesslyIterateCollection() {
        long total = 0;
        for (final Long i : CodeCoverageStore.getHits()) {
          total += i;
          try {
            Thread.sleep(5);
          } catch (final InterruptedException e) {

          }
        }
        return total;
      }
    };
    return read;
  }

  private static Runnable makeWriter(final int sleepPeriod) {
    final Runnable write = new Runnable() {

      @Override
      public void run() {
        for (int i = 0; i != 1000; i++) {
          try {
            Thread.sleep(sleepPeriod);
          } catch (final InterruptedException e) {
          }
          final boolean b[] = new boolean[1000];
          CodeCoverageStore.visitProbes(0, 0, b);
        }
      }
    };
    return write;
  }

}
