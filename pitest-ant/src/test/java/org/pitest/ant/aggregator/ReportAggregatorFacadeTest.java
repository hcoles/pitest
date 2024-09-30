package org.pitest.ant.aggregator;

import org.apache.tools.ant.types.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pitest.aggregate.ReportAggregator;

import java.io.File;
import java.util.Collections;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ReportAggregatorFacadeTest
{
    private static final String MOCK_LINE_COVERAGE_FILE_NAME = "fakeFileCoverageFile";
    private static final String MOCK_LINE_MUTATION_FILE_NAME = "fakeMutationFile";
    private static final String MOCK_LINE_SOURCE_CODE_DIR_NAME = "fakeSourceDir";
    private static final String MOCK_LINE_COMPILED_CODE_DIR_NAME = "fakeSourceDir";

    private final File mockLineCoverageFile = new File(MOCK_LINE_COVERAGE_FILE_NAME);
    private final File mockMutationFile = new File(MOCK_LINE_MUTATION_FILE_NAME);
    private final File mockSourceCodeDir = new File(MOCK_LINE_SOURCE_CODE_DIR_NAME);
    private final File mockCompiledCodeDir = new File(MOCK_LINE_COMPILED_CODE_DIR_NAME);

    @Mock
    private Path mockLineCoverageFilePath;
    @Mock
    private Path mockMutationFilePath;
    @Mock
    private Path mockSourceCodeDirPath;
    @Mock
    private Path mockCompiledCodeDirPath;

    @Mock
    ReportAggregator.Builder builder;

    private ReportAggregatorFacadeImpl facade = new ReportAggregatorFacadeImpl();

    @Before
    public void setup() {
        when(mockLineCoverageFilePath.list()).thenReturn(new String[] { MOCK_LINE_COVERAGE_FILE_NAME });
        when(mockMutationFilePath.list()).thenReturn(new String[] { MOCK_LINE_MUTATION_FILE_NAME });
        when(mockSourceCodeDirPath.list()).thenReturn(new String[] { MOCK_LINE_SOURCE_CODE_DIR_NAME });
        when(mockCompiledCodeDirPath.list()).thenReturn(new String[] { MOCK_LINE_COMPILED_CODE_DIR_NAME});
    }

    @Test
    public void aggregateReportBuilder() {
        facade.aggregateReport(builder);
        verify(builder).build();
    }

    @Test
    public void setLineCoverageFiles() {
        facade.setLineCoverageFiles(Collections.singletonList(mockLineCoverageFilePath));

        facade.aggregateReport(builder);
        verify(builder).addLineCoverageFile(mockLineCoverageFile);
    }

    @Test
    public void setMutationFiles() {
        facade.setMutationFiles(Collections.singletonList(mockMutationFilePath));

        facade.aggregateReport(builder);
        verify(builder).addMutationResultsFile(mockMutationFile);
    }

    @Test
    public void setSourceCodeDirs() {
        facade.setSourceCodeDirs(Collections.singletonList(mockSourceCodeDirPath));

        facade.aggregateReport(builder);
        verify(builder).addSourceCodeDirectory(mockSourceCodeDir);
    }

    @Test
    public void setCompiledCodeDirs() {
        facade.setCompiledCodeDirs(Collections.singletonList(mockCompiledCodeDirPath));

        facade.aggregateReport(builder);
        verify(builder).addCompiledCodeDirectory(mockCompiledCodeDir);
    }
}
