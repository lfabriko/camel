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
package org.apache.camel.component.jdbc;

import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.apache.camel.test.junit5.TestSupport.assertIsInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcProducerOutputTypeSelectOneTest extends AbstractJdbcTestSupport {

    @EndpointInject("mock:result")
    private MockEndpoint mock;

    @Test
    public void testOutputTypeSelectOne() throws Exception {
        mock.expectedMessageCount(1);

        template.sendBody("direct:start", "select * from customer where ID = 'cust1'");

        assertMockEndpointsSatisfied();

        Map row = assertIsInstanceOf(Map.class, mock.getReceivedExchanges().get(0).getIn().getBody(Map.class));
        assertEquals("cust1", row.get("ID"));
        assertEquals("jstrachan", row.get("NAME"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").to("jdbc:testdb?outputType=SelectOne").to("mock:result");
            }
        };
    }
}
