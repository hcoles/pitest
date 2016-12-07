package org.pitest.coverage;

import static org.pitest.mutationtest.LocationMother.aLocation;

import java.util.Collection;
import java.util.Collections;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;
import org.pitest.quickbuilder.Builder;
import org.pitest.quickbuilder.Generator;
import org.pitest.quickbuilder.SequenceBuilder;
import org.pitest.quickbuilder.builders.QB;
import org.pitest.testapi.Description;

public class CoverageMother {

  public interface BlockLocationBuilder extends SequenceBuilder<BlockLocation> {
    BlockLocationBuilder withLocation(Builder<Location> location);

    BlockLocationBuilder withLocation(Location location);

    BlockLocationBuilder withBlock(int block);

    Location _Location();

    int _Block();
  }

  public interface CoverageResultBuilder extends
  SequenceBuilder<CoverageResult> {

    CoverageResultBuilder withTestUnitDescription(Description d);

    CoverageResultBuilder withExecutionTime(int time);

    CoverageResultBuilder withVisitedBlocks(Collection<BlockLocation> blocks);

    CoverageResultBuilder withGreenSuite(boolean green);

    Description _TestUnitDescription();

    int _ExecutionTime();

    Collection<BlockLocation> _VisitedBlocks();

    boolean _GreenSuite();

  }

  public static BlockLocationBuilder aBlockLocation() {
    return QB.builder(BlockLocationBuilder.class, blockLocationSeed())
        .withBlock(1).withLocation(aLocation());
  }

  public static CoverageResultBuilder aCoverageResult() {
    final ClassName fooTest = ClassName.fromString("FooTest");
    return QB
        .builder(CoverageResultBuilder.class, CoverageResultSeed())
        .withTestUnitDescription(
            new Description("fooTest", fooTest.asJavaName()))
            .withExecutionTime(1).withGreenSuite(true)
            .withVisitedBlocks(Collections.<BlockLocation> emptyList());
  }

  private static Generator<BlockLocationBuilder, BlockLocation> blockLocationSeed() {
    return new Generator<BlockLocationBuilder, BlockLocation>() {
      @Override
      public BlockLocation generate(BlockLocationBuilder b) {
        return BlockLocation.blockLocation(b._Location(), b._Block());
      }

    };
  }

  private static Generator<CoverageResultBuilder, CoverageResult> CoverageResultSeed() {
    return new Generator<CoverageResultBuilder, CoverageResult>() {
      @Override
      public CoverageResult generate(CoverageResultBuilder b) {
        return new CoverageResult(b._TestUnitDescription(), b._ExecutionTime(),
            b._GreenSuite(), b._VisitedBlocks());
      }

    };
  }

}
