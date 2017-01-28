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

package com.mastercard.ri.retaillocationinsights.service;

import com.mastercard.ri.retaillocationinsights.config.TestConfig;
import com.mastercard.ri.retaillocationinsights.model.Area;
import com.mastercard.ri.retaillocationinsights.model.CompositeIndustry;
import com.mastercard.ri.retaillocationinsights.model.Period;
import com.mastercard.ri.retaillocationinsights.model.RetailUnitScore;
import com.mastercard.ri.retaillocationinsights.repository.AreaRepository;
import com.mastercard.ri.retaillocationinsights.repository.CompositeIndustryRepository;
import com.mastercard.ri.retaillocationinsights.repository.PeriodRepository;
import com.mastercard.ri.retaillocationinsights.repository.RetailUnitScoreRepository;
import com.mastercard.ri.retaillocationinsights.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class MetricsControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    CompositeIndustryRepository compositeIndustryRepository;

    @Autowired
    PeriodRepository periodRepository;

    @Autowired
    AreaRepository areaRepository;

    @Autowired
    RetailUnitScoreRepository retailUnitScoreRepository;

    MockMvc mvc;

    @BeforeClass
    public static void init() {
        TestUtil.setupTestDb();
    }

    @Before
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @Transactional
    @Rollback
    public void shouldReturnConfiguration() throws Exception {
        periodRepository.save(new Period("2016/08"));
        periodRepository.save(new Period("2016/09"));
        periodRepository.save(new Period("2016/10"));

        compositeIndustryRepository.save(new CompositeIndustry("2", "Test 2"));
        compositeIndustryRepository.save(new CompositeIndustry("3", "Test 3"));
        compositeIndustryRepository.save(new CompositeIndustry("1", "Test 1"));

        this.mvc.perform(
                get("/api/configuration"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.periods[0].id", is("2016/10")))
                .andExpect(jsonPath("$.periods[0].name", is("October 2016")))
                .andExpect(jsonPath("$.periods[1].id", is("2016/09")))
                .andExpect(jsonPath("$.periods[1].name", is("September 2016")))
                .andExpect(jsonPath("$.periods[2].id", is("2016/08")))
                .andExpect(jsonPath("$.periods[2].name", is("August 2016")))
                .andExpect(jsonPath("$.compositeIndustries[0].id", is("1")))
                .andExpect(jsonPath("$.compositeIndustries[0].name", is("Test 1")))
                .andExpect(jsonPath("$.compositeIndustries[1].id", is("2")))
                .andExpect(jsonPath("$.compositeIndustries[1].name", is("Test 2")))
                .andExpect(jsonPath("$.compositeIndustries[2].id", is("3")))
                .andExpect(jsonPath("$.compositeIndustries[2].name", is("Test 3")))
                .andExpect(jsonPath("$.areaZoomLevels[0].areaType", is("State")))
                .andExpect(jsonPath("$.areaZoomLevels[0].zoomLevel", is(4)))
                .andExpect(jsonPath("$.areaZoomLevels[1].areaType", is("CensusTract")))
                .andExpect(jsonPath("$.areaZoomLevels[1].zoomLevel", is(9)))
        ;
    }

    @Test
    @Transactional
    @Rollback
    public void shouldReturnMetrics() throws Exception {
        periodRepository.save(new Period("2016/08"));
        periodRepository.save(new Period("2016/09"));
        periodRepository.save(new Period("2016/10"));

        compositeIndustryRepository.save(new CompositeIndustry("2", "Test 2"));
        compositeIndustryRepository.save(new CompositeIndustry("3", "Test 3"));
        compositeIndustryRepository.save(new CompositeIndustry("1", "Test 1"));

        areaRepository.save(new Area("State", "12345", "[[123.123, 123.123]]"));
        areaRepository.save(new Area("CensusTract", "98765", "[[456.456, 456.456]]"));

        RetailUnitScore retailUnitScore = new RetailUnitScore();
        retailUnitScore.setCompositeIndustry(compositeIndustryRepository.findOne("1"));
        retailUnitScore.setPeriod(periodRepository.findOne("2016/09"));
        retailUnitScore.setArea(areaRepository.findByTypeAndGeoId("State", "12345"));
        retailUnitScore.setComposite(111.1D);
        retailUnitScore.setGrowth(222.2D);
        retailUnitScore.setSales(333.3D);
        retailUnitScore.setStability(444.4D);
        retailUnitScore.setTicketSize(555.5D);
        retailUnitScore.setTransactions(666.6D);
        retailUnitScoreRepository.save(retailUnitScore);

        retailUnitScore = new RetailUnitScore();
        retailUnitScore.setCompositeIndustry(compositeIndustryRepository.findOne("2"));
        retailUnitScore.setPeriod(periodRepository.findOne("2016/09"));
        retailUnitScore.setArea(areaRepository.findByTypeAndGeoId("CensusTract", "98765"));
        retailUnitScore.setComposite(777.7D);
        retailUnitScore.setGrowth(888.8D);
        retailUnitScore.setSales(999.9D);
        retailUnitScore.setStability(111.1D);
        retailUnitScore.setTicketSize(222.2D);
        retailUnitScore.setTransactions(333.3D);
        retailUnitScoreRepository.save(retailUnitScore);

        this.mvc.perform(
                get("/api/metrics")
                        .param("areaType", "State")
                        .param("period", "2016/09")
                        .param("compositeIndustry", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].geoId", is("12345")))
                .andExpect(jsonPath("$[0].ticketSize", is(555.5D)))
                .andExpect(jsonPath("$[0].transactions", is(666.6D)))
                .andExpect(jsonPath("$[0].sales", is(333.3D)))
                .andExpect(jsonPath("$[0].stability", is(444.4D)))
                .andExpect(jsonPath("$[0].growth", is(222.2D)))
                .andExpect(jsonPath("$[0].composite", is(111.1D)))
        ;
    }

    @Test
    @Transactional
    @Rollback
    public void shouldReturnArea() throws Exception {
        periodRepository.save(new Period("2016/09"));

        compositeIndustryRepository.save(new CompositeIndustry("1", "Test 1"));

        areaRepository.save(new Area("State", "12345", "[[123.123, 123.123]]"));
        areaRepository.save(new Area("CensusTract", "98765", "[[456.456, 456.456]]"));

        RetailUnitScore retailUnitScore = new RetailUnitScore();
        retailUnitScore.setCompositeIndustry(compositeIndustryRepository.findOne("1"));
        retailUnitScore.setPeriod(periodRepository.findOne("2016/09"));
        retailUnitScore.setArea(areaRepository.findByTypeAndGeoId("State", "12345"));
        retailUnitScore.setComposite(111.1D);
        retailUnitScore.setGrowth(222.2D);
        retailUnitScore.setSales(333.3D);
        retailUnitScore.setStability(444.4D);
        retailUnitScore.setTicketSize(555.5D);
        retailUnitScore.setTransactions(666.6D);
        retailUnitScoreRepository.save(retailUnitScore);

        this.mvc.perform(
                get("/api/area")
                        .param("areaType", "State"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.type", is("FeatureCollection")))
                .andExpect(jsonPath("$.crs.type", is("name")))
                .andExpect(jsonPath("$.crs.properties.name", is("EPSG:4269")))
                .andExpect(jsonPath("$.features[0].geometry[0][0]", is(123.123D)))
                .andExpect(jsonPath("$.features[0].geometry[0][1]", is(123.123D)))
                .andExpect(jsonPath("$.features[0].id", is("12345")))
        ;
    }
}
