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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;
import java.util.Queue;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pipeline.BaseStage;
import org.apache.commons.pipeline.StageException;
import org.apache.log4j.Logger;

/**
 * This {@link org.apache.commons.pipeline.Pipeline$Stage Stage} provides the 
 * functionality needed to retrieve data from an FTP URL. Multipart responses 
 * are not yet supported.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision$
 */
public class FtpFileDownloadStage extends BaseStage {
    private static Logger log = Logger.getLogger(FtpFileDownloadStage.class);
    
    private String workDir = "/tmp";
    private File fworkDir;
    private FTPClient client = new FTPClient();
    
    /** Holds value of property host. */
    private String host;
    
    /** Holds value of property user. */
    private String user;
    
    /** Holds value of property password. */
    private String password;
    
    
    /**
     * Default constructor - creates work directory in /tmp
     */
    public FtpFileDownloadStage() {
    }
   
    /**
     * Constructor specifying work directory.
     */
    public FtpFileDownloadStage(String workDir) {
        this.workDir = workDir;
    }
   
    /**
     * Default constructor - creates work directory in /tmp
     */
    public FtpFileDownloadStage(Queue<Object>  queue) {
        super(queue);
    }
    
    /**
     * Creates a new instance of HttpFileDownload with the specified work directory
     * into which to download files.
     */
    public FtpFileDownloadStage(Queue<Object> queue, String workDir) {
        super(queue);
        this.workDir = workDir;
    }
    
    /**
     * Creates the download directory {@link #setWorkDir(String) workDir} uf it does
     * not exist.
     */
    public void preprocess() throws StageException {
        if (fworkDir == null) fworkDir = new File(workDir);
        if (!this.fworkDir.exists()) fworkDir.mkdirs();
        
        try {
            //connect to the ftp site
            client.connect(host);
            log.debug(client.getReplyString());
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP server at host " + host + " refused connection.");
            }
            
            client.login(user, password);
            log.debug(client.getReplyString());
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP login failed for user " + user + ": " + client.getReplyString());
            }
        }
        catch (IOException e) {
            throw new StageException(e.getMessage(), e);
        }
    }
    
    /**
     * Removes a java.net.URL (an HTTP URL) from the input queue, follows any redirects
     * specified by that URL, and then retrieves the data to a file over an HTTP
     * connection. The file name for download is the
     * last element of the URL path for download appended with a timestamp
     * value, and it is stored in the directory specified by {@link #setWorkDir(String) setWorkDir()}, or to
     * /tmp if no work directory is set.
     *
     * @param obj The URL from which to download data.
     * @throws ClassCastException if the parameter obj is not an instance of java.net.URL
     */
    public void process(Object obj) throws StageException {
        if (!this.fworkDir.exists()) throw new StageException("The work directory for file download " + workDir.toString() + " does not exist.");
        
        FileSpec spec = (FileSpec) obj;
        
        try {
            client.changeWorkingDirectory(spec.path);
            log.debug(client.getReplyString());
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP client could not change to remote directory " + spec.path + ": " + client.getReplyString());
            }
            
            log.debug("FTP connection successfully established to " + host + spec.path);
            
            //get the list of files
            client.enterLocalPassiveMode();
            String[] dirs = client.listNames();
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP client could not obtain file list : " + client.getReplyString());
            }
            //client.enterLocalActiveMode();
                       
            log.debug("FTP file list successfully obtained.");
            
            Pattern pattern = Pattern.compile(spec.pattern);
            
            log.debug("File pattern is " + spec.pattern);
            
            //create the list of netcdf track files to get
            for (int i = 0; i < dirs.length; i++){
                log.debug("Obtaining files in directory " + dirs[i]);
                String[] files = client.listNames(dirs[i]);
                
                for (int j = 0; j < files.length; j++) {
                    if (pattern.matcher(files[j]).matches()) {
                        log.debug("Matched file name " + files[j] + " against pattern " + spec.pattern);
                        File f = new File(workDir + File.separatorChar + files[j]);
                        if (! f.getParentFile().exists()) f.getParentFile().mkdir();
                        
                        OutputStream out = new FileOutputStream(f);
                        client.retrieveFile(files[j], out);
                        this.exqueue(f);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new StageException(e.getMessage(), e);
        }
    }
    
    
    /**
     * Disconnects from FTP server. Errors are logged.
     */
    public void release() {
        try {
            client.disconnect(); //close ftp connection
        }
        catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    
    /**
     * Sets the working directory for the file download. If the directory does
     * not already exist, it will be created during the preprocess() step.
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
    
    /** Getter for property host.
     * @return Value of property host.
     *
     */
    public String getHost() {
        return this.host;
    }
    
    /** Setter for property host.
     * @param host New value of property host.
     *
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /** Getter for property user.
     * @return Value of property user.
     *
     */
    public String getUser() {
        return this.user;
    }
    
    /** Setter for property user.
     * @param user New value of property user.
     *
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    /** Setter for property password.
     * @param password New value of property password.
     *
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    
    /**
     * This class is used to specify a path and pattern of file for the FtpFileDownload
     * to retrieve.
     */
    public static class FileSpec {
        
        /** Holds value of property path. */
        private String path = "/";
        
        /** Holds value of property pattern. */
        private String pattern = ".*";
        
        /** Getter for property path.
         * @return Value of property path.
         *
         */
        public String getPath() {
            return this.path;
        }
        
        /** Setter for property path.
         * @param path New value of property path.
         *
         */
        public void setPath(String path) {
            this.path = path;
        }
        
        /** Getter for property pattern.
         * @return Value of property pattern.
         *
         */
        public String getPattern() {
            return this.pattern;
        }
        
        /** Setter for property pattern.
         * @param pattern New value of property pattern.
         *
         */
        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
}
