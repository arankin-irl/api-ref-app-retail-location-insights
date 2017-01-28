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

package com.mastercard.ri.retaillocationinsights.dto;

import com.mastercard.ri.retaillocationinsights.model.CompositeIndustry;
import com.mastercard.ri.retaillocationinsights.model.Period;

import java.util.List;
import java.util.Map;

public class Configuration {
    private List<Period> periods;
    private List<CompositeIndustry> compositeIndustries;
    private List<Map<String, Object>> areaZoomLevels;

    public Configuration(List<Period> periods, List<CompositeIndustry> compositeIndustries,
                         List<Map<String, Object>> areaZoomLevels) {
        this.periods = periods;
        this.compositeIndustries = compositeIndustries;
        this.areaZoomLevels = areaZoomLevels;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<CompositeIndustry> getCompositeIndustries() {
        return compositeIndustries;
    }

    public List<Map<String, Object>> getAreaZoomLevels() {
        return areaZoomLevels;
    }
}