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
package org.apache.camel.component.atmos.integration.producer;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.atmos.integration.AtmosTestSupport;
import org.apache.camel.component.atmos.util.AtmosConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AtmosProducerMoveTest extends AtmosTestSupport {

    public AtmosProducerMoveTest() {
    }

    @Test
    public void testCamelAtmos() throws Exception {
        template.send("direct:start", new Processor() {
            @Override
            public void process(Exchange exchange) {
                exchange.getIn().setHeader("test", "test");
            }
        });

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        assertMockEndpointsSatisfied(100L, TimeUnit.SECONDS);

        List<Exchange> exchanges = mock.getReceivedExchanges();
        Exchange exchange = exchanges.get(0);
        Object header = exchange.getIn().getHeader(AtmosConstants.MOVED_PATH);
        Object body = exchange.getIn().getBody();
        assertNotNull(header);
        assertNotNull(body);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:start")
                        .to("atmos://move?remotePath=/dummy/&newRemotePath=/dummy2/")
                        .to("mock:result");
            }
        };
    }
}
