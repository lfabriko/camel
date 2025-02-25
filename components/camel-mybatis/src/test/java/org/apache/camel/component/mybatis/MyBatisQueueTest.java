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
package org.apache.camel.component.mybatis;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.apache.camel.test.junit5.TestSupport.assertIsInstanceOf;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBatisQueueTest extends MyBatisTestSupport {

    @Override
    protected boolean createTestData() {
        return false;
    }

    @Override
    protected String getCreateStatement() {
        return "create table ACCOUNT (ACC_ID INTEGER, ACC_FIRST_NAME VARCHAR(255), ACC_LAST_NAME VARCHAR(255), ACC_EMAIL VARCHAR(255), PROCESSED BOOLEAN DEFAULT false)";
    }

    @Test
    public void testConsume() throws Exception {
        MockEndpoint endpoint = getMockEndpoint("mock:results");
        endpoint.expectedMinimumMessageCount(2);

        Account account1 = new Account();
        account1.setId(1);
        account1.setFirstName("Bob");
        account1.setLastName("Denver");
        account1.setEmailAddress("TryGuessingGilligan@gmail.com");

        Account account2 = new Account();
        account2.setId(2);
        account2.setFirstName("Alan");
        account2.setLastName("Hale");
        account2.setEmailAddress("TryGuessingSkipper@gmail.com");

        template.sendBody("direct:start", new Account[] { account1, account2 });

        assertMockEndpointsSatisfied();

        // need a delay here on slower machines
        List<?> body = await()
                .atMost(1, TimeUnit.SECONDS)
                .until(() -> template.requestBody("mybatis:selectProcessedAccounts?statementType=SelectList", null, List.class),
                        Matchers.notNullValue());

        // now lets poll that the account has been inserted
        assertEquals(2, body.size(), "Wrong size: " + body);
        Account actual = assertIsInstanceOf(Account.class, body.get(0));

        assertEquals("Bob", actual.getFirstName(), "Account.getFirstName()");
        assertEquals("Denver", actual.getLastName(), "Account.getLastName()");

        body = template.requestBody("mybatis:selectUnprocessedAccounts?statementType=SelectList", null, List.class);
        assertEquals(0, body.size(), "Wrong size: " + body);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                // START SNIPPET: e1
                from("mybatis:selectUnprocessedAccounts?onConsume=consumeAccount").to("mock:results");
                // END SNIPPET: e1

                from("direct:start").to("mybatis:insertAccount?statementType=Insert");
            }
        };
    }
}
