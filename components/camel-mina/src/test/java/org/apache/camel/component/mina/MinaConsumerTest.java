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
package org.apache.camel.component.mina;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for wiki documentation
 */
public class MinaConsumerTest extends BaseMinaTest {

    int port1;
    int port2;

    @Test
    public void testSendTextlineText() throws Exception {
        // START SNIPPET: e2
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("Hello World");

        template.sendBody("mina:tcp://localhost:" + port1 + "?textline=true&sync=false", "Hello World");

        assertMockEndpointsSatisfied();
        // END SNIPPET: e2
    }

    @Test
    public void testSendTextlineSyncText() {
        // START SNIPPET: e4
        String response = (String) template.requestBody("mina:tcp://localhost:" + port2 + "?textline=true&sync=true", "World");
        assertEquals("Bye World", response);
        // END SNIPPET: e4
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {

            public void configure() {
                port1 = getPort();
                port2 = getNextPort();

                // START SNIPPET: e1
                fromF("mina:tcp://localhost:%d?textline=true&sync=false", port1).to("mock:result");
                // END SNIPPET: e1

                // START SNIPPET: e3
                fromF("mina:tcp://localhost:%d?textline=true&sync=true", port2).process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    exchange.getMessage().setBody("Bye " + body);
                });
                // END SNIPPET: e3
            }
        };
    }
}
