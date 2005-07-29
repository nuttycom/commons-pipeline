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

package org.apache.commons.pipeline.stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pipeline.BaseStage;
import org.apache.commons.pipeline.StageException;

/**
 * <p>This {@link org.apache.commons.pipeline.Pipeline$Stage Stage} provides the
 * functionality needed to retrieve data from an FTP URL. Multipart responses
 * are not yet supported.</p>
 */
public class FtpFileDownloadStage extends BaseStage {
    private final Log log = LogFactory.getLog(FtpFileDownloadStage.class);
    
    private String workDir = "/tmp";
    private File fworkDir;
    private FTPClient client = new FTPClient();
    
    /** Holds value of property host. */
    private String host;
    
    /** Holds value of property user. */
    private String user;
    
    /** Holds value of property password. */
    private String password;
    
    /** Holds value of property port.     */
    private int port;

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
            client.connect(host, port);
            log.debug(client.getReplyString());
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP server at host " + host + " refused connection.");
            }
            
            client.login(user, password);
            log.debug(client.getReplyString());
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP login failed for user " + user + ": " + client.getReplyString());
            }
        } catch (IOException e) {
            throw new StageException(e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves files that match the specified FileSpec from the FTP server
     * and stores them in the work directory.
     *
     * @throws ClassCastException if the parameter obj is not an instance of java.net.URL
     */
    public void process(Object obj) throws StageException {
        if (!this.fworkDir.exists()) throw new StageException("The work directory for file download " + workDir.toString() + " does not exist.");
        
        FileSpec spec = (FileSpec) obj;
        
        try {
            client.setFileType(spec.type.intValue());
            client.changeWorkingDirectory(spec.path);
            if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP client could not change to remote directory " + spec.path + ": " + client.getReplyString());
            }
            
            log.debug("FTP connection successfully established to " + host + ":" + spec.path);
            
            //get the list of files
            client.enterLocalPassiveMode();
            searchCurrentDirectory("", spec);
        } catch (IOException e) {
            throw new StageException(e.getMessage(), e);
        }
    }
    
    
    /**
     * Search the current working directory of the FTP client, saving files
     * to the path specified by workDir + the path to the file on the FTP server.
     * This method will optionally recursively search directories on the remote server.
     */
    private void searchCurrentDirectory(String path, FileSpec spec) throws IOException {
        FTPFile[] files = client.listFiles();
        if(!FTPReply.isPositiveCompletion(client.getReplyCode())) {
            throw new IOException("FTP client could not obtain file list : " + client.getReplyString());
        }
        
        search: for (FTPFile file : files) {
            String localPath = path + File.separatorChar + file.getName();
            
            if (file.isDirectory() && spec.recursive) {
                log.debug("Recursing into directory " + file.getName());
                client.changeWorkingDirectory(file.getName());
                searchCurrentDirectory(localPath, spec);
                client.changeToParentDirectory();
            } else {
                log.debug("Examining file " + localPath);
                for (Criterion crit : spec.criteria) {
                    if (!crit.matches(file)) {
                        log.info("File " + localPath + " failed criterion check " + crit);
                        continue search;
                    }
                }
                
                File localFile = new File(workDir + File.separatorChar + localPath);
                if (localFile.exists() && !spec.overwrite) {
                    //if (spec.)
                } else {
                    if (! localFile.getParentFile().exists()) localFile.getParentFile().mkdir();
                    
                    OutputStream out = new FileOutputStream(localFile);
                    try {
                        client.retrieveFile(file.getName(), out);
                    } finally {
                        out.flush();
                        out.close();
                    }
                }
                
                this.exqueue(localFile);
            }
        }
    }
    
    /**
     * Disconnects from FTP server. Errors are logged.
     */
    public void release() {
        try {
            client.disconnect(); //close ftp connection
        } catch (IOException e) {
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
     * Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Setter for property port.
     * @param port New value of property port.
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * This class is used to specify a path and pattern of file for the FtpFileDownload
     * to retrieve.
     */
    public static class FileSpec {
        //enumeration of legal file types
        public enum FileType {
            ASCII(FTPClient.ASCII_FILE_TYPE),
                    BINARY(FTPClient.BINARY_FILE_TYPE);
            
            private int type;
            
            private FileType(int type) {
                this.type = type;
            }
            
            public int intValue() {
                return this.type;
            }
        }
        
        /** Holds value of property path. */
        private String path = "/";
        
        /** Holds flag that determines whether or not to perform recursive search of the specified path */
        private boolean recursive;
        
        // Holds flag that determines whether or not to overwrite local files
        private boolean overwrite;
        
        // Type of file (ascii or binary)
        private FileType type = FileType.BINARY;
        
        // List of criteria that the retrieved file must satisfy.
        private Set<Criterion> criteria = new HashSet<Criterion>();
        
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
         * @deprecated - not retrievable from criterion
         */
        public String getPattern() {
            return null;
        }
        
        /** Setter for property pattern.
         * @param pattern New value of property pattern.
         *
         */
        public void setPattern(String pattern) {
            this.criteria.add(new FileNameMatchCriterion(pattern));
        }
        
        /**
         * Add a criterion to the set of criteria that must be matched for files
         * to be downloaded
         */
        public void addCriterion(Criterion crit) {
            this.criteria.add(crit);
        }
        
        /**
         * Sets the flag determining whether or not the stage will recursively
         * traverse the directory tree to find files.
         */
        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }
        
        /**
         * Returns whether or not the stage will recursively
         * traverse the directory tree to find files.
         */
        public boolean isRecursive() {
            return this.recursive;
        }
        
        /**
         * Sets the file type for the transfer. Legal values are "ascii" and "binary".
         * Binary transfers are the default.
         */
        public void setFileType(String fileType) {
            if ("ascii".equalsIgnoreCase(fileType)) {
                this.type = FileType.ASCII;
            } else {
                this.type = FileType.BINARY;
            }
        }
        
        /**
         * Returns the file type for the transfer.
         */
        public String getFileType() {
            return this.type.toString();
        }
    }
    
    /**
     * This class is used to specify a criterion that the downloaded file
     * must satisfy.
     */
    public interface Criterion {
        public boolean matches(FTPFile file);
    }
    
    /**
     * Matches file names based upon the Java regex supplied in the constructor.
     */
    public static class FileNameMatchCriterion implements Criterion {
        // precompiled pattern used to match filenames
        private Pattern pattern;
        private String _pattern;
        
        public FileNameMatchCriterion(String pattern) {
            this._pattern = pattern;
            this.pattern = Pattern.compile(pattern);
        }
        
        public boolean matches(FTPFile file) {
            return pattern.matcher(file.getName()).matches();
        }
        
        public String toString() {
            return "filename matches pattern " + _pattern;
        }
    }
    
    /**
     * Matches files based upon a set of date constraints
     */
    public static class FileDateMatchCriterion implements Criterion {
        private Date startDate;
        private Date endDate;
        
        public FileDateMatchCriterion(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public boolean matches(FTPFile file) {
            Calendar cal = file.getTimestamp();
            if ((startDate != null && cal.getTime().before(startDate)) || (endDate != null && cal.getTime().after(endDate))) {
                return false;
            } else {
                return true;
            }
        }
        
        public String toString() {
            return "file date is between " + startDate + " and " + endDate;
        }
    }
}
