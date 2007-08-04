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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.validation.ConsumedTypes;
import org.apache.commons.pipeline.validation.ProducedTypes;


/**
 * This {@link org.apache.commons.pipeline.Pipeline$Stage Stage} provides the 
 * functionality needed to retrieve data from an HTTP URL. Multipart responses 
 * are not yet supported.
 */
@ConsumedTypes({URL.class, String.class})
@ProducedTypes({File.class})
public class HttpFileDownloadStage extends BaseStage {
    private static final int BUFFER_SIZE = 10000;
    private String workDir = null;
    private Log log = LogFactory.getLog(HttpFileDownloadStage.class);
    
    public HttpFileDownloadStage() { }
    
    /**
     * Creates a new HttpFileDownloadStage which will download files to the
     * specified work directory.
     * @param workDir the path to which files will be downloaded.
     */
    public HttpFileDownloadStage(String workDir) {
        this.workDir = workDir;
    }
    
    /**
     * Removes a java.net.URL (an HTTP URL) or string representing a URL from 
     * the input queue, and then retrieves the data at that URL and stores it
     * in a temporary file. The file is stored in the directory specified by 
     * {@link #setWorkDir(String) setWorkDir()}, or to the system default 
     * temporary directory if no work directory is set.
     *
     * @param obj The URL from which to download data.
     * @throws IllegalArgumentException if the parameter obj is not a string or 
     * an instance of {@link java.net.URL}.
     * @throws StageException if there is an error retrieving data from the 
     * URL specified.
     */
    public void process(Object obj) throws StageException {
        //Map params = new HashMap();
        
        URL url;
        try {
            if (obj instanceof String) {
//                String loc = (String) obj;
//                int paramIndex = loc.indexOf('?');
//                if (paramIndex > 0) {
//                    url = new URL(loc.substring(0, paramIndex));
//                    for (StringTokenizer st = new StringTokenizer(loc.substring(paramIndex + 1), "&"); st.hasMoreTokens();) {
//                        String tok = st.nextToken();
//                        int eqIndex = tok.indexOf('=');
//                        if (eqIndex > 0) {
//                            params.put(tok.substring(0, eqIndex), tok.substring(eqIndex + 1));
//                        }
//                        else {
//                            params.put(tok, null);
//                        }
//                    }
//                }
//                else {
                url = new URL((String) obj);
//                }
            } else if (obj instanceof URL) {
                url = (URL) obj;
            } else {
                throw new IllegalArgumentException("Unrecognized parameter class to process() for HttpFileDownload: " + obj.getClass().getName() + "; must be URL or String");
            }
        } catch (MalformedURLException e) {
            throw new StageException(this, "Malformed URL: " + obj, e);
        }
        
        log.debug("Retrieving data from " + url.toString());
        
//        try {
//            url = handleRedirects(url);
//        }
//        catch (Exception e) { //catches MalformedURLException, IOException
//            throw new StageException("An error was encountered attempting to follow URL redirects from " + url.toString(), e);
//        }
        
        HttpURLConnection con = null;
        try {
            con = (java.net.HttpURLConnection) url.openConnection();
//            if (!params.isEmpty()) {
//                con.setRequestMethod("GET");
//                for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
//                    Map.Entry entry = (Map.Entry) iter.next();
//                    con.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
//                }
//            }

            File workDir = (this.workDir == null) ? null : new File(this.workDir);
            File workFile = File.createTempFile("http-file-download","tmp", workDir);
            
            InputStream in = new BufferedInputStream(con.getInputStream());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(workFile, false));
            byte[] buffer = new byte[BUFFER_SIZE]; //attempt to read 10k at a time
            for (int results = 0; (results = in.read(buffer)) != -1;) {
                out.write(buffer, 0, results);
            }
            out.close();
            in.close();
            
            this.emit(workFile);
        } catch (IOException e) {
            throw new StageException(this, "An error occurred downloading a data file from " + url.toString(), e);
        } finally {
            con.disconnect();
        }        
    }
    
    
    /**
     * Sets the working directory for the file download. If the directory does
     * not already exist, it will be created during the preprocess() step.
     * If you do not set this directory, the work directory will be the
     * default temporary directory for your machine type.
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
    
    /**
     * Returns the name of the file download directory.
     */
    public String getWorkDir() {
        return this.workDir;
    }
    
    /**
     * Follows redirects from the specified URL and recursively returns the destination
     * URL. This method does not check for circular redirects, so it is possible that a malicious
     * site could force this method into infinite recursion.
     *
     * TODO: Add a max_hops parameterized version
     */
    public URL handleRedirects(URL url) throws IOException, MalformedURLException {
        java.net.HttpURLConnection.setFollowRedirects(false);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int response = con.getResponseCode();
        log.debug("Response code for " + url + " = " + response);
        
        if (response == java.net.HttpURLConnection.HTTP_MOVED_PERM || response == java.net.HttpURLConnection.HTTP_MOVED_TEMP) {
            String location = con.getHeaderField("Location");
            log.debug("Handling redirect to location: " + location);
            
            if (location.startsWith("http:")) {
                url = new URL(location);
            } else if (location.startsWith("/")) {
                url = new URL("http://" + url.getHost() + location);
            } else {
                url = new URL(con.getURL(), location);
            }
            
            url = handleRedirects(url); // to handle nested redirections
        }
        
        return url;
    }
}
