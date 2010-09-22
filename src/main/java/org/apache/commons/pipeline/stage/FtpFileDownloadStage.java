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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
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
     * @param workDir local directory in which to store downloaded files
     */
    public FtpFileDownloadStage(String workDir) {
        this.workDir = workDir;
    }
    
    /**
     * Creates the download directory {@link #setWorkDir(String) workDir} uf it does
     * not exist and makes a connection to the remote FTP server.
     * @throws org.apache.commons.pipeline.StageException if a connection to the remote FTP server cannot be established, or the login to
     * the remote system fails
     */
    public void preprocess() throws StageException {
        super.preprocess();
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
                throw new StageException(this, "FTP login failed for user " + user + ": " + client.getReplyString());
            }
        } catch (IOException e) {
            throw new StageException(this, e);
        }
    }
    
    /**
     * Retrieves files that match the specified FileSpec from the FTP server
     * and stores them in the work directory.
     * @param obj incoming {@link FileSpec} that indicates the file to download along with some flags to
     * control the download behavior
     * @throws org.apache.commons.pipeline.StageException if there are errors navigating the remote directory structure or file download 
     * fails
     */
    public void process(Object obj) throws StageException {
        if (!this.fworkDir.exists()) throw new StageException(this, "The work directory for file download " + workDir.toString() + " does not exist.");
        
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
            throw new StageException(this, e);
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
                
                boolean getFile = true;
                File localFile = new File(workDir + File.separatorChar + localPath);
                if (localFile.exists()) {
                    if (spec.overwrite) {
                        log.info("Replacing existing local file " + localFile.getPath());
                        getFile = true;
                    } else {
                        if (spec.ignoreExisting) {
                            log.info("Ignoring existing local file " + localFile.getPath());
                            continue search;
                } else {
                            log.info("Using existing local file " + localFile.getPath());
                            getFile = false;
                        }
                    }
                } else {
                    getFile = true;
                }
                
                if (getFile) {
                    if (! localFile.getParentFile().exists()) localFile.getParentFile().mkdir();
                    
                    OutputStream out = new FileOutputStream(localFile);
                    try {
                        client.retrieveFile(file.getName(), out);
                    } finally {
                        out.flush();
                        out.close();
                    }
                }
                
                this.emit(localFile);
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
     * @param workDir local directory to receive file downloads
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
    
    /**
     * Returns the name of the file download directory.
     * @return the string containing the local working directory
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
     * to retrieve. There are some parameters that can be configured in the filespec
     * that will control download behavior for <CODE>recursive</CODE> searching, the 
     * <CODE>overwrite</CODE> of locally existing files, and to 
     * <CODE>ignoreExisting</CODE> files.
     * <p>
     * If a file already exists in the local directory, it is only replaced if 
     * <CODE>overwrite</CODE> is set to <CODE>true</CODE>. If it is replaced, then the
     * filename is passed on to the next stage. Existing files are passed on to the
     * stage unless <CODE>ignoreExisting</CODE> is <CODE>true</CODE>. Note that the
     * <CODE>ignoreExisting</CODE> flag is only used if <CODE>overwrite</CODE> is 
     * <CODE>false</CODE> (it's assumed that if a file will be downloaded, then it 
     * shouldn't be ignored).
     * <p>
     * Pseudocode to summarize the interaction of <CODE>overwrite</CODE> and 
     * <CODE>ignoreExisting</CODE>: <PRE>
     *     if (file exists) {
     *        if (overwrite) {
     *            download file over existing local copy
     *            and pass it on to the next stage
     *        } else {
     *            if (ignoreExisting) {
     *                skip this file
     *            } else {
     *                pass existing file on to the next stage
     *            }
     *        }
     *     } else {
     *        download new file 
     *        and pass it on to the next stage
     *     }
     * </PRE>
     */
    public static class FileSpec {
        /**
         * Enumeration of legal FTP file tranfer types
         */
        public enum FileType {
            /**
             * ASCII text transfer mode, with end of line conversion.
             */
            ASCII(FTPClient.ASCII_FILE_TYPE),
            /**
             * Binary transfer mode, no changes made to data stream.
             */
                    BINARY(FTPClient.BINARY_FILE_TYPE);
            
            private int type;
            
            private FileType(int type) {
                this.type = type;
            }
            
            /**
             * Get the integer value of the FTP transfer mode enumeration.
             * @return the integer equivalent to the FTP transfer mode setting
             */
            public int intValue() {
                return this.type;
            }
        }
        
        /** Holds value of property path. */
        private String path = "/";
        
        /** Holds flag that determines whether or not to perform recursive search of the specified path */
        private boolean recursive;
        
        // Holds flag that determines whether or not to overwrite local files
        private boolean overwrite = false;

        /**
         * Holds flag that determines if existing files are passed to the next stage.
         */
        private boolean ignoreExisting = false;
        
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
         * @param crit {@link Criterion} used to match desired files for download, typically a filename pattern
         */
        public void addCriterion(Criterion crit) {
            this.criteria.add(crit);
        }
        
        /**
         * Sets the flag determining whether or not the stage will recursively
         * traverse the directory tree to find files.
         * @param recursive this value is <CODE>true</CODE> to recursively search the remote directories for matches to
         * the criterion, <CODE>false</CODE> to turn off recursive searching
         */
        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }
        
        /**
         * Returns whether or not the stage will recursively
         * traverse the directory tree to find files.
         * @return the current recursive search setting
         */
        public boolean isRecursive() {
            return this.recursive;
        }
        
        /**
         * Sets the file type for the transfer. Legal values are "ascii" and "binary".
         * Binary transfers are the default.
         * @param fileType the FTP transfer type to use, "<CODE>ascii</CODE>" or "<CODE>binary</CODE>"
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
         * @return the current FTP transfer type setting
         */
        public String getFileType() {
            return this.type.toString();
        }

        /**
         * Getter for property overwrite. The default value for this flag is 
         * <CODE>false</CODE>, so existing local files will not be replaced by downloading
         * remote files. This flag should be set to <CODE>true</CODE> if it is expected
         * that the remote file is periodically updated and the local file is and out of
         * date copy from a previous run of this pipeline.
         * @return Value of property overwrite.
         */
        public boolean isOverwrite() {
            return this.overwrite;
        }

        /**
         * Setter for property overwrite.
         * @param overwrite New value of property overwrite.
         */
        public void setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
        }

        /**
         * Getter for property ignoreExisting. The default value for this flag is 
         * <CODE>false</CODE>, so existing files that aren't downloaded are still passed
         * on to the next stage.
         * @return Value of property ignoreExisting.
         */
        public boolean isIgnoreExisting() {
            return this.ignoreExisting;
        }

        /**
         * Setter for property ignoreExisting.
         * @param ignoreExisting New value of property ignoreExisting.
         */
        public void setIgnoreExisting(boolean ignoreExisting) {
            this.ignoreExisting = ignoreExisting;
        }
    }
    
    /**
     * This class is used to specify a criterion that the downloaded file
     * must satisfy.
     */
    public interface Criterion {
        /**
         * Interface defining matches for FTP file downloading. Those remote files that
         * match the criterion will be downloaded.
         * @param file file to compare criterion to
         * @return <CODE>true</CODE> if the file meets the Criterion, <CODE>false</CODE> otherwise
         */
        public boolean matches(FTPFile file);
    }
    
    /**
     * Matches file names based upon the Java regex supplied in the constructor.
     */
    public static class FileNameMatchCriterion implements Criterion {
        // precompiled pattern used to match filenames
        private Pattern pattern;
        private String _pattern;
        
        /**
         * Construct a new criterion to match on file names.
         * @param pattern Java regex pattern specifying acceptable file names
         */
        public FileNameMatchCriterion(String pattern) {
            this._pattern = pattern;
            this.pattern = Pattern.compile(pattern);
        }
        
        /**
         * Test the given file's name against this criterion.
         * @param file file to compare to
         * @return <CODE>true</CODE> if the filename matches the filename pattern of this criterion,
         * <CODE>false</CODE> otherwise
         */
        public boolean matches(FTPFile file) {
            return pattern.matcher(file.getName()).matches();
        }
        
        /**
         * Printable version of this Criterion indicating the Java regex used for filename
         * matching.
         * @return a string containing the regex used to construct this filename criterion
         */
        public String toString() {
            return "filename matches pattern " + _pattern;
        }
    }
    
    /**
     * Matches files by matching their filesystem timestamp to a date range.
     */
    public static class FileDateMatchCriterion implements Criterion {
        private Date startDate;
        private Date endDate;
        
        /**
         * Construct a new criterion to match file timestamp to a range of dates.
         * @param startDate starting date (inclusive) of the date range
         * @param endDate ending date (inclusive) of the date range
         */
        public FileDateMatchCriterion(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        /**
         * Test the given file's date against this criterion.
         * @param file file to compare to
         * @return <CODE>true</CODE> if the file date falls into the time window of 
         * [startDate, endDate], <CODE>false</CODE> otherwise
         */
        public boolean matches(FTPFile file) {
            Calendar cal = file.getTimestamp();
            if ((startDate != null && cal.getTime().before(startDate)) || (endDate != null && cal.getTime().after(endDate))) {
                return false;
            } else {
                return true;
            }
        }
        
        /**
         * Printable version of this Criterion indicating the inclusive date range used
         * for file date matching.
         * @return a string noting the startDate and endDate
         */
        public String toString() {
            return "file date is between " + startDate + " and " + endDate;
        }
    }
}
