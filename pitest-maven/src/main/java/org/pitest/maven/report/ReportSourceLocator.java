/*
 * Copyright 2015 Jason Fehr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.maven.report;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.CanWriteFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.pitest.util.PitError;

/**
 * Determines the directory where the most recent PIT reports are located.  If timestampedReports is set to true, then the latest 
 * reports directory is located.  If timestampedReports is set to false, then 
 * 
 * @author jasonmfehr
 */
public class ReportSourceLocator {

	//a java.io.File object is considered to be representing a directory where a timestamped pit report is located if 
	//    1.) the java.io.File is a directory
	//    2.) the directory name contains only numbers
	//    3.) the directory is readable
	protected static final FileFilter TIMESTAMPED_REPORTS_FILE_FILTER = new AndFileFilter(Arrays.asList(DirectoryFileFilter.DIRECTORY, new RegexFileFilter("^\\d+$"), CanWriteFileFilter.CAN_WRITE));
	
	/**
	 * Determines the directory where the most recent PIT reports are located.  If timestampedReports is set to true, then the latest 
	 * reports directory is located and returned.  If timestampedReports is set to false, then the provided reportsDirectory parameter is returned.  If 
	 * multiple runs of the plugin have resulted in a combination of a non-timestamped report directory and timestamped reports directories, 
	 * then the latest of all those directories is returned.
	 * 
	 * See {@link ReportSourceLocator#TIMESTAMPED_REPORTS_FILE_FILTER} for an explanation of what constitutes a timestamped reports directory;
	 * 
	 * @param reportsDirectory {@link File} representing the directory where non-timestamped reports were written if the plugin's configuration has 
	 *                         timestampedReports set to true;
	 *                         
	 * @return {@link File} representing the directory where the latest PIT reports are located
	 */
	public File locate(File reportsDirectory) {
		if(!reportsDirectory.exists()){
			throw new PitError("could not find reports directory [" + reportsDirectory + "]");
		}
		
		if(!reportsDirectory.canRead()){
			throw new PitError("reports directory [" + reportsDirectory + "] not readable");
		}
		
		if(!reportsDirectory.isDirectory()){
			throw new PitError("reports directory [" + reportsDirectory + "] is actually a file, it must be a directory");
		}
		
		return executeLocator(reportsDirectory);
	}
	
	private File executeLocator(File reportsDirectory) {
		File[] subdirectories = reportsDirectory.listFiles(TIMESTAMPED_REPORTS_FILE_FILTER);
		File latest = reportsDirectory;

		
		if(subdirectories != null){
			LastModifiedFileComparator c = new LastModifiedFileComparator();
			
			for(File f : subdirectories){
				if(c.compare(latest, f) < 0){
					latest = f;
				}
			}
		}else{
			throw new PitError("could not list files in directory [" + reportsDirectory.getAbsolutePath() + "] because of an unknown I/O error");
		}
		
		return latest;
	}

}
