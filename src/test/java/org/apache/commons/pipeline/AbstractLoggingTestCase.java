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

package org.apache.commons.pipeline;

import java.io.File;
import java.io.InputStream;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;

/**
 *
 *
 */
public abstract class AbstractLoggingTestCase extends TestCase {
    private static volatile boolean initialized = false;
    
    public AbstractLoggingTestCase(String testName) {
        super(testName);
        
        if (!initialized) {
            try {
                ResourceBundle props = ResourceBundle.getBundle("TestResources");
                File logDir = new File(props.getString("test.log.directory"));
                if (!logDir.exists()) logDir.mkdirs();
                InputStream istream = Thread.currentThread().getContextClassLoader().getResourceAsStream("log4j_conf.xml");
                try {
                    Document document  = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(istream);
                    DOMConfigurator.configure(document.getDocumentElement());
                } finally {
                    if (istream != null) istream.close();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
