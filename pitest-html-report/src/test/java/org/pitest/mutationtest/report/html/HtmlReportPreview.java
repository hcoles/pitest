package org.pitest.mutationtest.report.html;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.ClassLine;
import org.pitest.coverage.ClassLines;
import org.pitest.coverage.ReportCoverage;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.SourceLocator;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.verify.BuildMessage;
import org.pitest.util.ResultOutputStrategy;

/** Generates an illustrative report through the production listener and templates. */
public class HtmlReportPreview {

  public static void main(String[] args) throws IOException {
    Path output = Path.of(args.length == 0 ? "target/report-preview" : args[0]);
    generate(output, true);
    System.out.println("Report preview: " + output.toAbsolutePath().resolve("index.html"));
  }

  static void generate(Path output, boolean showCoverage) throws IOException {
    Files.createDirectories(output);
    PreviewCoverage coverage = new PreviewCoverage();
    Map<String, String> sources = new HashMap<>();
    ResultOutputStrategy strategy = file -> {
      try {
        Path target = output.resolve(file);
        Files.createDirectories(target.getParent());
        return Files.newBufferedWriter(target, StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    };
    SourceLocator locator = (classes, filename) -> Optional.ofNullable(sources.get(filename))
        .map(StringReader::new);
    List<String> mutators = List.of("CONDITIONALS_BOUNDARY", "MATH", "PRIMITIVE_RETURNS", "NEGATE_CONDITIONALS");
    MutationHtmlReportListener listener = new MutationHtmlReportListener(StandardCharsets.UTF_8,
        coverage, strategy, mutators, showCoverage,
        List.of(BuildMessage.buildMessage("Illustrative sample — these results demonstrate the report UI; they are not a mutation run against this repository.")),
        false, 1, locator);
    String[][] packages = {
        {"com.example.checkout", "DiscountPolicy", "CouponPolicy", "CheckoutPolicy"},
        {"com.example.delivery", "ShippingPolicy", "DeliveryPolicy"},
        {"com.example.orders", "OrderPolicy", "RewardPolicy", "ReturnPolicy"},
        {"com.example.pricing", "TaxPolicy", "PricingPolicy"},
        {"com.example.validation", "MinimumPolicy", "LimitPolicy"}
    };
    int variant = 0;
    for (String[] pkg : packages) {
      for (int i = 1; i < pkg.length; i++) {
        String filename = pkg[i] + ".java";
        ClassName name = ClassName.fromString(pkg[0] + "." + pkg[i]);
        sources.put(filename, source(pkg[0], pkg[i]));
        coverage.add(name, variant % 3 != 1);
        listener.handleMutationResult(new ClassMutationResults(mutations(name, filename, variant++)));
      }
    }
    listener.runEnd();
  }

  private static String source(String pkg, String name) {
    return String.join("\n",
        "package " + pkg + ";",
        "",
        "/** A minimum-amount policy used by the report sample. */",
        "public final class " + name + " {",
        "",
        "    private static final int MINIMUM = 100;",
        "",
        "    public int apply(int amount) {",
        "        if (amount >= MINIMUM) {",
        "            return amount - 10;",
        "        }",
        "        return amount;",
        "    }",
        "",
        "    public boolean isEligible(int amount) {",
        "        return amount >= MINIMUM;",
        "    }",
        "",
        "    public int minimumAmount() {",
        "        return MINIMUM;",
        "    }",
        "}", "");
  }

  private static List<MutationResult> mutations(ClassName name, String file, int variant) {
    int[] lines = {9, 9, 10, 12, 16, 20};
    String[] methods = {"apply", "apply", "apply", "apply", "isEligible", "minimumAmount"};
    String[] descriptions = {"changed conditional boundary", "negated conditional",
        "Replaced integer subtraction with addition", "replaced int return with 0",
        "replaced boolean return with false", "replaced int return with 0"};
    List<MutationResult> results = new ArrayList<>();
    String testName = name.asJavaName() + "Test.appliesAtMinimum";
    for (int i = 0; i < lines.length; i++) {
      DetectionStatus status = DetectionStatus.KILLED;
      if (i == 0 && variant % 3 == 0) {
        status = DetectionStatus.SURVIVED;
      } else if (i == 5 && variant % 3 == 1) {
        status = DetectionStatus.NO_COVERAGE;
      } else if (i == 4 && variant % 4 == 0) {
        status = DetectionStatus.TIMED_OUT;
      } else if (i == 3 && variant % 5 == 0) {
        status = DetectionStatus.SURVIVED;
      }
      MutationIdentifier id = new MutationIdentifier(new Location(name, methods[i], "(I)I"), i,
          "org.pitest.mutationtest.engine.gregor.mutators." + (i == 2 ? "MathMutator" : "ReturnValsMutator"));
      MutationDetails details = new MutationDetails(id, file, descriptions[i], lines[i], 0);
      List<String> covering = status == DetectionStatus.NO_COVERAGE ? List.of() : List.of(testName);
      details.addTestsInOrder(List.of(new TestInfo(name.asJavaName() + "Test", testName,
          3 + variant, Optional.of(name), 1)));
      MutationStatusTestPair pair = new MutationStatusTestPair(covering.size(), status,
          status == DetectionStatus.KILLED ? covering : List.of(), List.of(), covering);
      results.add(new MutationResult(details, pair));
    }
    return results;
  }

  private static class PreviewCoverage implements ReportCoverage {
    private final Map<ClassName, ClassLines> classes = new HashMap<>();
    private final Map<ClassName, Set<ClassLine>> covered = new HashMap<>();

    void add(ClassName name, boolean coversLastMethod) {
      Set<Integer> code = Set.of(9, 10, 12, 16, 20);
      classes.put(name, new ClassLines(name, code));
      covered.put(name, code.stream().filter(line -> coversLastMethod || line != 20)
          .map(line -> new ClassLine(name, line)).collect(Collectors.toSet()));
    }

    @Override
    public ClassLines getCodeLinesForClass(ClassName name) {
      return classes.get(name);
    }

    @Override
    public Set<ClassLine> getCoveredLines(ClassName name) {
      return covered.getOrDefault(name, new HashSet<>());
    }

    @Override
    public Collection<ClassLines> getClassesForFile(String sourceFile, String packageName) {
      String className = packageName + "." + sourceFile.replace(".java", "");
      return List.of(classes.get(ClassName.fromString(className)));
    }
  }
}
