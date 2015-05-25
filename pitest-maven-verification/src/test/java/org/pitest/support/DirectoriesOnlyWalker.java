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
package org.pitest.support;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * Builds a list of all directories contained within a provided directory.  The starting directory is also contained 
 * within the list of directories. <br /><br />
 * 
 * <code>
 * DirectoriesOnlyWalker walker = new DirectoriesOnlyWalker(); <br />
 * List<File> directories = walker.locateDirectories(someDirectory);
 * </code>
 */
public class DirectoriesOnlyWalker extends DirectoryWalker {

    public DirectoriesOnlyWalker() {
        super(FileFilterUtils.directoryFileFilter(), -1);
    }
    
    public List<File> locateDirectories(File startDir) {
        List<File> foundDirs = new LinkedList<File>();
        
        try {
          this.walk(startDir, foundDirs);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        
      return foundDirs;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected boolean handleDirectory(File directory, int depth, Collection results) throws IOException {
        results.add(directory);
        
        return super.handleDirectory(directory, depth, results);
    }
  
}
