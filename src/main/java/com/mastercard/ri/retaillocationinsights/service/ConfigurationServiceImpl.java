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

import com.google.common.collect.ImmutableMap;
import com.mastercard.ri.retaillocationinsights.dto.Configuration;
import com.mastercard.ri.retaillocationinsights.repository.CompositeIndustryRepository;
import com.mastercard.ri.retaillocationinsights.repository.PeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {
    private final PeriodRepository periodRepository;
    private final CompositeIndustryRepository compositeIndustryRepository;

    private final List<Map<String, Object>> areaZoomLevels;

    private Configuration configuration;

    @Autowired
    public ConfigurationServiceImpl(Environment env, PeriodRepository periodRepository, AreaService areaService,
                                    CompositeIndustryRepository compositeIndustryRepository) {
        this.periodRepository = periodRepository;
        this.compositeIndustryRepository = compositeIndustryRepository;

        this.areaZoomLevels = new ArrayList<>();
        for (String areaType : areaService.getAreaTypes()) {
            Integer zoomLevel = env.getProperty("ril.area.type.zoomlevel." + areaType.toLowerCase(), Integer.class);
            if (zoomLevel == null) {
                continue;
            }
            this.areaZoomLevels.add(ImmutableMap.<String, Object>of(
                    "areaType", areaType,
                    "zoomLevel", zoomLevel));
        }
        Collections.sort(this.areaZoomLevels, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return ((Integer) o1.get("zoomLevel")).compareTo((Integer) o2.get("zoomLevel"));
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration(periodRepository.findAllByOrderByYearDescMonthDesc(),
                    compositeIndustryRepository.findAllByOrderByNameAsc(),
                    areaZoomLevels);
        }
        return configuration;
    }
}
