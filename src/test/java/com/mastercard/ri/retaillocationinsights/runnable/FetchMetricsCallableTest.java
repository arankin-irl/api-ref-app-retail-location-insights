/*
 * Copyright 2017 MasterCard International.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * Neither the name of the MasterCard International Incorporated nor the names of its
 * contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */

package com.mastercard.ri.retaillocationinsights.runnable;

import com.mastercard.api.core.ApiConfig;
import com.mastercard.api.core.security.Authentication;
import com.mastercard.api.retaillocationinsights.SDKConfig;
import com.mastercard.ri.retaillocationinsights.model.Area;
import com.mastercard.ri.retaillocationinsights.model.RetailUnitScore;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class FetchMetricsCallableTest {
    private static final String CONTENT_TYPE = "Content-Type";

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    MockServerClient mockServerClient;

    @Before
    public void setup() {
        SDKConfig.setHost("http://127.0.0.1:" + mockServerRule.getPort());
        ApiConfig.setAuthentication(mock(Authentication.class));
    }

    @After
    public void cleanup() {
        mockServerClient.reset();
    }

    @Test
    public void shouldFetchRetailUnitsMetricsApi() throws Exception {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.OK.value())
                                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .withBody(IOUtils.toString(getClass()
                                        .getResourceAsStream("/retailunitmetrics-test.json")))
                );

        Area area = new Area("State", "36", "[[123]]");
        FetchMetricsCallable callable = new FetchMetricsCallable(null, area);
        List<RetailUnitScore> metrics = callable.call();

        assertEquals(5, metrics.size()); // 5 metrics in retailunitmetrics-test.json

        RetailUnitScore metric = metrics.get(0);
        assertEquals("ALL", metric.getCompositeIndustry().getId());
        assertEquals("All", metric.getCompositeIndustry().getName());
        assertEquals("2016/07", metric.getPeriod().getId());
        assertEquals("July 2016", metric.getPeriod().getName());
        assertEquals(area, metric.getArea());
        assertEquals(812, metric.getSales(), 0);
        assertEquals(831, metric.getTransactions(), 0);
        assertEquals(208, metric.getTicketSize(), 0);
        assertEquals(359, metric.getGrowth(), 0);
        assertEquals(906, metric.getStability(), 0);
        assertEquals(793, metric.getComposite(), 0);


        metric = metrics.get(3);
        assertEquals("NEP", metric.getCompositeIndustry().getId());
        assertEquals("Non Eating Places", metric.getCompositeIndustry().getName());
        assertEquals("2016/07", metric.getPeriod().getId());
        assertEquals("July 2016", metric.getPeriod().getName());
        assertEquals(area, metric.getArea());
        assertEquals(812, metric.getSales(), 0);
        assertEquals(793, metric.getTransactions(), 0);
        assertEquals(472, metric.getTicketSize(), 0);
        assertEquals(359, metric.getGrowth(), 0);
        assertEquals(944, metric.getStability(), 0);
        assertEquals(850, metric.getComposite(), 0);
    }
}
