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
package org.apache.camel.component.jms;

import javax.jms.ConnectionFactory;

import org.apache.camel.BindToRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test inspired by user forum
 */
public class JmsRouteWithInOnlyTest extends CamelTestSupport {

    protected String componentName = "activemq";

    @BindToRegistry("orderService")
    private MyOrderServiceBean serviceBean = new MyOrderServiceBean();

    @Test
    public void testSendOrder() throws Exception {
        MockEndpoint inbox = getMockEndpoint("mock:inbox");
        inbox.expectedBodiesReceived("Camel in Action");

        MockEndpoint order = getMockEndpoint("mock:topic");
        order.expectedBodiesReceived("Camel in Action");

        Object out = template.requestBody("activemq:queue:inbox", "Camel in Action");
        assertEquals("OK: Camel in Action", out);

        assertMockEndpointsSatisfied();

        // assert MEP
        assertEquals(ExchangePattern.InOut, inbox.getReceivedExchanges().get(0).getPattern());
        assertEquals(ExchangePattern.InOnly, order.getReceivedExchanges().get(0).getPattern());
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();

        ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
        camelContext.addComponent(componentName, jmsComponentAutoAcknowledge(connectionFactory));

        return camelContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("activemq:queue:inbox").to("mock:inbox").to(ExchangePattern.InOnly, "activemq:topic:order").bean(
                        "orderService",
                        "handleOrder");

                from("activemq:topic:order").to("mock:topic");
            }
        };
    }

    public static class MyOrderServiceBean {

        public String handleOrder(String body) {
            return "OK: " + body;
        }

    }
}
