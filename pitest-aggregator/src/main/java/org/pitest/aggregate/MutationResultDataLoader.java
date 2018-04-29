package org.pitest.aggregate;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

class MutationResultDataLoader extends DataLoader<MutationResult> {

  private static final String MUTATED_CLASS       = "mutatedClass";
  private static final String MUTATED_METHOD      = "mutatedMethod";
  private static final String METHOD_DESCRIPTION  = "methodDescription";
  private static final String INDEX               = "index";
  private static final String MUTATOR             = "mutator";
  private static final String SOURCE_FILE         = "sourceFile";
  private static final String DESCRIPTION         = "description";
  private static final String LINE_NUMBER         = "lineNumber";
  private static final String BLOCK               = "block";
  private static final String NUMBER_OF_TESTS_RUN = "numberOfTestsRun";
  private static final String STATUS              = "status";
  private static final String KILLING_TEST        = "killingTest";

  MutationResultDataLoader(final Collection<File> filesToLoad) {
    super(filesToLoad);
  }

  @Override
  protected MutationResult mapToData(final Map<String, Object> map) {
    final Location location = new Location(ClassName.fromString((String) map.get(MUTATED_CLASS)), MethodName.fromString((String) map.get(MUTATED_METHOD)),
        (String) map.get(METHOD_DESCRIPTION));

    final MutationIdentifier id = new MutationIdentifier(location, Arrays.asList(new Integer((String) map.get(INDEX))), (String) map.get(MUTATOR));

    final MutationDetails md = new MutationDetails(id, (String) map.get(SOURCE_FILE), (String) map.get(DESCRIPTION),
        Integer.parseInt((String) map.get(LINE_NUMBER)), Integer.parseInt((String) map.get(BLOCK)));

    final MutationStatusTestPair status = new MutationStatusTestPair(Integer.parseInt((String) map.get(NUMBER_OF_TESTS_RUN)),
        DetectionStatus.valueOf((String) map.get(STATUS)), (String) map.get(KILLING_TEST));

    return new MutationResult(md, status);
  }

}
