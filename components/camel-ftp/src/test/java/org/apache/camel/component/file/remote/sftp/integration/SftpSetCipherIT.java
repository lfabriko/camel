/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file.remote.sftp.integration;

import java.io.File;

import org.apache.camel.Exchange;
import org.apache.camel.component.file.remote.SftpEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf(value = "org.apache.camel.component.file.remote.services.SftpEmbeddedService#hasRequiredAlgorithms")
public class SftpSetCipherIT extends SftpServerTestSupport {

    @Test
    public void testSftpSetCipherName() {
        String cipher = "aes256-ctr";
        String uri
                = "sftp://localhost:{{ftp.server.port}}/{{ftp.root.dir}}?username=admin&password=admin&ciphers="
                  + cipher;
        template.sendBodyAndHeader(uri, "Hello World", Exchange.FILE_NAME, "hello.txt");

        // test setting the cipher doesn't interfere with message payload
        File file = ftpFile("hello.txt").toFile();
        assertTrue(file.exists(), "File should exist: " + file);
        assertEquals("Hello World", context.getTypeConverter().convertTo(String.class, file));

        // did we actually set the correct cipher?
        SftpEndpoint endpoint = context.getEndpoint(uri, SftpEndpoint.class);
        assertEquals(cipher, endpoint.getConfiguration().getCiphers());
    }

}
