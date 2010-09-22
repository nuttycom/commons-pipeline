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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.StageException;

/**
 * Converts a URL into an InputStream.  This stage keeps track of all
 * input streams that are created and closes them at the release step.
 */
public class URLToInputStreamStage extends BaseStage {
    private final Log log = LogFactory.getLog(URLToInputStreamStage.class);
    private List<InputStream> inputStreams = new ArrayList<InputStream>();
    
    /** Creates a new instance of URLToInputStreamStage */
    public URLToInputStreamStage() {    }
    
    /** 
     * Takes a String or a URL object representing a URL and exqueues the input 
     * stream returned by opening that URL.
     *
     * @param obj A String or URL object
     */    
    public void process(Object obj) throws org.apache.commons.pipeline.StageException {        
        URL url = null;
        if (obj instanceof URL){
            url = (URL) obj;
        } else if (obj instanceof String) {
            String urlString = (String) obj;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e){
                throw new StageException(this, "Error converting url String:" + urlString, e);
            }
        }
        
        try {
            InputStream inputStream = url.openStream();
            this.inputStreams.add(inputStream);
            log.info("enqueing input stream");
            this.emit(inputStream);
        } catch (IOException e){
            throw new StageException(this, "Error with stream from url:" + url, e);
        }
    }
    
    /**
     * Ensure that all opened input streams are closed.
     */
    public void release() {
        log.info("running post process number of streams:" + inputStreams.size());
        while(inputStreams.size() > 0){
            InputStream is = (InputStream) inputStreams.remove(0);
            try {
                is.close();
                log.info("closed stream");
            } catch (IOException e){
                log.warn("Error closing stream",e);
            }
        }
    }
}
