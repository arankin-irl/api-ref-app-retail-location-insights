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

package com.mastercard.ri.retaillocationinsights.controller;

import com.mastercard.ri.retaillocationinsights.dto.Configuration;
import com.mastercard.ri.retaillocationinsights.dto.GeoJson;
import com.mastercard.ri.retaillocationinsights.model.RetailUnitScore;
import com.mastercard.ri.retaillocationinsights.service.AreaService;
import com.mastercard.ri.retaillocationinsights.service.ConfigurationService;
import com.mastercard.ri.retaillocationinsights.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class MetricsController {
    private final ConfigurationService configurationService;
    private final AreaService areaService;
    private final MetricService metricService;

    @Autowired
    public MetricsController(ConfigurationService configurationService, AreaService areaService, MetricService metricService) {
        this.configurationService = configurationService;
        this.areaService = areaService;
        this.metricService = metricService;
    }

    @RequestMapping(value = "/configuration", method = RequestMethod.GET)
    @ResponseBody
    public Configuration getConfiguration() {
        return configurationService.getConfiguration();
    }

    @RequestMapping(value = "/area", method = RequestMethod.GET)
    @ResponseBody
    public GeoJson getArea(@RequestParam("areaType") String areaType) {
        return areaService.getArea(areaType);
    }

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    @ResponseBody
    public List<RetailUnitScore> getMetrics(@RequestParam("areaType") String areaType,
                                            @RequestParam("period") String period,
                                            @RequestParam("compositeIndustry") String compositeIndustry) {

        return metricService.getMetrics(period, areaType, compositeIndustry);
    }
}
