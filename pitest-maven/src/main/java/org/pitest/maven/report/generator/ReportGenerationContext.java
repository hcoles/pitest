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
package org.pitest.maven.report.generator;

import java.io.File;
import java.util.Locale;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugin.logging.Log;

public class ReportGenerationContext {

	private Locale locale;
	private Sink sink;
	private File reportsDirectory;
	private File siteDirectory;
	private Log logger;
	
	public ReportGenerationContext() {
		
	}
	
	public ReportGenerationContext(Locale locale, Sink sink, File reportsDirectory, File siteDirectory, Log logger) {
		this.locale = locale;
		this.sink = sink;
		this.reportsDirectory = reportsDirectory;
		this.siteDirectory = siteDirectory;
		this.logger = logger;
	}

	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public Sink getSink() {
		return sink;
	}
	public void setSink(Sink sink) {
		this.sink = sink;
	}
	
	public File getReportsDirectory() {
		return reportsDirectory;
	}
	public void setReportsDirectory(File reportsDirectory) {
		this.reportsDirectory = reportsDirectory;
	}
	
	public File getSiteDirectory() {
		return siteDirectory;
	}
	public void setSiteDirectory(File siteDirectory) {
		this.siteDirectory = siteDirectory;
	}
	
	public Log getLogger() {
		return this.logger;
	}
	public void setLogger(Log logger) {
		this.logger = logger;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
