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
package org.apache.camel.component.netty.http;

import org.apache.camel.BindToRegistry;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singleton;
import static org.apache.camel.Exchange.HTTP_QUERY;

public class NettyHttpHeaderFilterStrategyRemovalTest extends BaseNettyTest {

    @BindToRegistry("headerFilterStrategy")
    NettyHttpHeaderFilterStrategy headerFilterStrategy = new NettyHttpHeaderFilterStrategy();

    @EndpointInject("mock:test")
    MockEndpoint mockEndpoint;

    @Test
    public void shouldRemoveStrategyOption() throws Exception {
        String options = "headerFilterStrategy=#headerFilterStrategy";
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.message(0).header(HTTP_QUERY).isNull();

        template.sendBody("netty-http:http://localhost:" + getPort() + "/?" + options, "message");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void shouldResolveStrategyFromParameter() throws Exception {
        String headerToFilter = "foo";
        headerFilterStrategy.setOutFilter(singleton(headerToFilter));
        String options = "headerFilterStrategy=#headerFilterStrategy";
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.message(0).header(headerToFilter).isNull();

        template.sendBodyAndHeader("netty-http:http://localhost:" + getPort() + "/?" + options, "message", headerToFilter,
                "headerValue");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("netty-http:http://0.0.0.0:{{port}}/").to(mockEndpoint);
            }
        };
    }

}
