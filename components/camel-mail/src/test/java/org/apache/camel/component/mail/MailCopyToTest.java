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
package org.apache.camel.component.mail;

import java.util.concurrent.TimeUnit;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.mock_javamail.Mailbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for copyTo.
 */
public class MailCopyToTest extends CamelTestSupport {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        prepareMailbox();
        super.setUp();
    }

    @Test
    public void testCopyTo() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(5);

        assertMockEndpointsSatisfied();

        // windows need a little slack
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertEquals(0, Mailbox.get("jones@localhost").getNewMessageCount()));
        assertEquals(5, Mailbox.get("backup-jones@localhost").getNewMessageCount());
    }

    private void prepareMailbox() throws Exception {
        // connect to mailbox
        Mailbox.clearAll();
        JavaMailSender sender = new DefaultJavaMailSender();
        Store store = sender.getSession().getStore("pop3");
        store.connect("localhost", 25, "jones", "secret");
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);
        folder.expunge();

        // inserts 5 new messages
        Message[] messages = new Message[5];
        for (int i = 0; i < 5; i++) {
            messages[i] = new MimeMessage(sender.getSession());
            messages[i].setHeader("Message-ID", "" + i);
            messages[i].setText("Message " + i);
        }
        folder.appendMessages(messages);
        folder.close(true);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("imap://jones@localhost?password=secret&copyTo=backup&initialDelay=100&delay=100").to("mock:result");
            }
        };
    }
}
