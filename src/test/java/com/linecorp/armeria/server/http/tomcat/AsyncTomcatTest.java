/*
 * Copyright 2015 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.linecorp.armeria.server.http.tomcat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.catalina.Service;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.linecorp.armeria.server.AbstractServerTest;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.server.logging.LoggingService;

public class AsyncTomcatTest extends AbstractServerTest {

    private static final Pattern CR_OR_LF = Pattern.compile("[\\r\\n]");

    private static final String SERVICE_NAME = "TomcatServiceTest";

    private final List<Service> tomcatServices = new ArrayList<>();

    @Override
    protected void configureServer(ServerBuilder sb) {
        sb.serviceUnder(
                "/",
                TomcatServiceBuilder.forCurrentClassPath("tomcat_async")
                                    .serviceName(SERVICE_NAME)
                                    .configurator(s -> Collections.addAll(tomcatServices, s.findServices()))
                                    .build()
                                    .decorate(LoggingService::new));
    }

    @Test
    public void testAsync() throws Exception {
        try (CloseableHttpClient hc = HttpClients.createMinimal()) {
            try (CloseableHttpResponse res = hc.execute(new HttpGet(uri("/async")))) {
                assertThat(res.getStatusLine().toString(), is("HTTP/1.1 200 OK"));
//                assertThat(res.getFirstHeader(HttpHeaderNames.CONTENT_TYPE.toString()).getValue(),
//                           startsWith("text/html"));
                final String actualContent = CR_OR_LF.matcher(EntityUtils.toString(res.getEntity()))
                                                     .replaceAll("");
                assertThat(actualContent, is(
                        "<html><body>" +
                        "<p>Hello, Armerian World!</p>" +
                        "<p>Have you heard about the class 'io.netty.buffer.ByteBuf'?</p>" +
                        "<p>Context path: </p>" + // ROOT context path
                        "<p>Request URI: /</p>" +
                        "</body></html>"));
            }
        }
    }
}
