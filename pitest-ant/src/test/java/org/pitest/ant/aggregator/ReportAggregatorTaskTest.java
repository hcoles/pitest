package org.pitest.ant.aggregator;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.aggregate.ReportAggregationException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class ReportAggregatorTaskTest {

    private ReportAggregatorTask reportAggregatorTask;

    @Mock
    private Project project;

    @Mock
    private ReportAggregatorFacade aggregatorFacade;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    public Path lineCoverageFiles;
    @Mock
    private Path mutationFiles;
    @Mock
    private Path sourceCodeDirs;
    @Mock
    private Path compiledCodeDirs;

    @Before
    public void setUp() {
        this.reportAggregatorTask = new ReportAggregatorTask();
        this.reportAggregatorTask.setProject(this.project);
    }

    @Test
    public void testExecuteAggregator() throws ReportAggregationException {
        this.reportAggregatorTask.execute(aggregatorFacade);
        verify(aggregatorFacade).aggregateReport();
    }

    @Test
    public void testAggregatorShouldSupportLineCoverageFiles() throws ReportAggregationException {

        this.reportAggregatorTask.addLineCoverageFiles(lineCoverageFiles);

        this.reportAggregatorTask.execute(aggregatorFacade);

        verify(aggregatorFacade).setLineCoverageFiles(singletonList(lineCoverageFiles));
    }

    @Test
    public void testAggregatorShouldSupportMutationFiles() throws ReportAggregationException {

        this.reportAggregatorTask.addMutationFiles(mutationFiles);

        this.reportAggregatorTask.execute(aggregatorFacade);

        verify(aggregatorFacade).setMutationFiles(singletonList(mutationFiles));
    }

    @Test
    public void testAggregatorShouldSupportSourceCodeDirs() throws ReportAggregationException {

        this.reportAggregatorTask.addSourceCodeDirs(sourceCodeDirs);

        this.reportAggregatorTask.execute(aggregatorFacade);

        verify(aggregatorFacade).setSourceCodeDirs(singletonList(sourceCodeDirs));
    }

    @Test
    public void testAggregatorShouldCompiledCodeDirs() throws ReportAggregationException {

        this.reportAggregatorTask.addCompiledCodeDirs(compiledCodeDirs);

        this.reportAggregatorTask.execute(aggregatorFacade);

        verify(aggregatorFacade).setCompiledCodeDirs(singletonList(compiledCodeDirs));
    }
}
