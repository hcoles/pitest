package org.pitest.maven.report;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.pitest.util.PitError;

public class ReportSourceLocator {

	//a java.io.File object is considered to be representing a directory where a timestamped pit report is located if 
	//    1.) the java.io.File is a directory
	//    2.) the directory name contains only numbers
	private static final FileFilter TIMESTAMPED_REPORTS_FILE_FILTER = new AndFileFilter(DirectoryFileFilter.DIRECTORY, new RegexFileFilter("^\\d+$"));
	
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
		
		if(subdirectories == null){
			throw new PitError("could not list files in directory [" + reportsDirectory.getAbsolutePath() + "]");
		}
		
		if(subdirectories.length > 0){
			Comparator<File> c = new LastModifiedFileComparator();
			File latest = subdirectories[0];
			
			for(int i=0; i<subdirectories.length; i++){
				if(c.compare(latest, subdirectories[i]) > 0){
					latest = subdirectories[i];
				}
			}
			
			return latest;
		}
		
		
		return reportsDirectory;
	}

}
