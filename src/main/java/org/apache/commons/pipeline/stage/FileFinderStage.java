/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.pipeline.stage;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.validation.ConsumedTypes;
import org.apache.commons.pipeline.validation.ProducedTypes;

/**
 * <p>This {@link org.apache.commons.pipeline.Pipeline$Stage Stage} is used
 * to recursively find (non-directory) files that match the specified regex.</p>
 *
 * <p>File elements in the stage's queue will be recursively searched with the
 * resulting File objects placed on the subsequent stage's queue.</p>
 */
@ConsumedTypes({String.class, File.class})
@ProducedTypes(File.class)
public class FileFinderStage extends BaseStage {
    private final Log log = LogFactory.getLog(FileFinderStage.class);
    private String filePattern = ".*";
    Pattern pattern;
    
    /** Creates a new instance of FileFinder */
    public FileFinderStage() { }
    
    /**
     * Precompiles the regex pattern for matching against filenames
     */
    public void preprocess() throws StageException {
        super.preprocess();
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
            log.info("File " + file + " does not exist.");
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            log.debug(file.getName() + " is a directory, processing " + files.length + " files within.");
            for (int i = 0; i < files.length; i++) {
                process(files[i]);
            }
        } else if (this.pattern.matcher(file.getName()).matches()){
            this.emit(file);
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
