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
import com.mastercard.ri.retaillocationinsights.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class AreaServiceTest {
    @Autowired
    AreaService areaService;

    @PersistenceContext
    EntityManager entityManager;

    @BeforeClass
    public static void init() {
        TestUtil.setupTestDb();
    }

    @Test
    @Transactional
    @Rollback
    public void shouldParseAndStoreShapeFiles() {
        areaService.parseAndStoreShapeFiles();

        List<Area> areas = getAreas("State");
        assertEquals(10, areas.size());
        assertEquals("04", areas.get(0).getGeoId());
        assertThat(areas.get(0).getGeometry(), not(isEmptyOrNullString()));

        areas = getAreas("CensusTract");
        assertEquals(38, areas.size());
        assertEquals("36019100100", areas.get(0).getGeoId());
        assertThat(areas.get(0).getGeometry(), not(isEmptyOrNullString()));
    }

    @Test
    @Transactional
    @Rollback
    public void shouldNotParseAndStoreShapeFilesIfTheyAreAlreadyProcessed() {
        areaService.parseAndStoreShapeFiles();
        areaService.parseAndStoreShapeFiles();

        List<Area> areas = getAreas("State");
        assertEquals(10, areas.size());
        assertEquals("04", areas.get(0).getGeoId());
        assertThat(areas.get(0).getGeometry(), not(isEmptyOrNullString()));

        areas = getAreas("CensusTract");
        assertEquals(38, areas.size());
        assertEquals("36019100100", areas.get(0).getGeoId());
        assertThat(areas.get(0).getGeometry(), not(isEmptyOrNullString()));
    }

    private List<Area> getAreas(String areaType) {
        return entityManager.createQuery("select a from Area a where a.type = :areaType order by a.geoId asc")
                .setParameter("areaType", areaType)
                .getResultList();
    }
}
