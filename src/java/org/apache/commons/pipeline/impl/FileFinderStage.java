/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package org.apache.commons.pipeline.impl;

import java.io.File;
import java.util.regex.Pattern;
import java.util.Queue;
import org.apache.commons.pipeline.BaseStage;
import org.apache.log4j.Logger;

/**
 * This {@link org.apache.commons.pipeline.Pipeline$Stage Stage} is used
 * to recursively find (non-directory) files that match the specified regex.
 *
 * File elements in the stage's queue will be recursively searched with the
 * resulting File objects placed on the subsequent stage's queue.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.2 $
 */
public class FileFinderStage extends BaseStage {
    private static final Logger log = Logger.getLogger(FileFinderStage.class);
    private String filePattern = ".*";
    Pattern pattern;
    
    /** Creates a new instance of FileFinder */
    public FileFinderStage() { }
    
    /** Creates a new instance of FileFinder that uses the specified queue. */     
    public FileFinderStage(Queue<Object> queue) {
        super(queue);
    }
    
    /**
     * Precompiles the regex pattern for matching against filenames
     */
    public void preprocess() {
        this.pattern = Pattern.compile(this.filePattern);
    }
    
    
    /**
     * This method inspects a File object to determine if
     * it matches this FileFinder's filePattern property. If the File
     * represents a directory, it recursively searches that directory and
     * all subdirectories for matching files. Matched files are placed
     * on the next stage's queue.
     */
    public void process(Object obj) {
        File file = (obj instanceof String) ? new File((String) obj) : (File) obj;        
        log.debug("Examining file " + file.getAbsolutePath());
        
        if (!file.exists()) {
            log.debug("File does not exist.");
        }
        else if (file.isDirectory()) {
            File[] files = file.listFiles();
            log.debug(file.getName() + " is a directory, processing " + files.length + " files within.");
            for (int i = 0; i < files.length; i++) {
                process(files[i]);
            }
        }
        else if (this.pattern.matcher(file.getName()).matches()){
            this.exqueue(file);
            log.debug("Enqueueing file: " + file.getName());
        }
    }    
    
    
    /** Getter for property filePattern.
     * @return Value of property filePattern.
     *
     */
    public String getFilePattern() {
        return this.filePattern;
    }
    
    
    /** Setter for property filePattern.
     * @param pattern Value of property filePattern.
     *
     */
    public void setFilePattern(String pattern) {
        this.filePattern = pattern;
    }    
}
