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

import com.google.common.base.Optional;
import com.mastercard.api.core.model.RequestMap;
import com.mastercard.api.retaillocationinsights.RetailUnitsMetrics;
import com.mastercard.ri.retaillocationinsights.model.Area;
import com.mastercard.ri.retaillocationinsights.model.CompositeIndustry;
import com.mastercard.ri.retaillocationinsights.model.Period;
import com.mastercard.ri.retaillocationinsights.model.RetailUnitScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class FetchMetricsCallable implements Callable<List<RetailUnitScore>> {
    private static final Logger log = LoggerFactory.getLogger(FetchMetricsCallable.class);

    private final String period;
    private final Area area;

    public FetchMetricsCallable(String period, Area area) {
        this.period = period;
        this.area = area;
    }

    @Override
    public List<RetailUnitScore> call() throws Exception {
        List<RetailUnitScore> scores = new ArrayList<>();
        int count = 0, offset = 0;

        String areaType = area.getType();
        String value = area.getGeoId();

        log.debug("Fetching scores for period {}, area type {}, value {}", period, areaType, value);

        while (true) {
            RequestMap map = new RequestMap();
            map.set("PageLength", 99);
            map.set("PageOffset", offset);
            map.set("RetailUnitType", areaType);
            map.set("RetailUnitId", value);
            if (period != null) {
                map.set("Period", period);
            }

            RetailUnitsMetrics response = RetailUnitsMetrics.query(map);
            int totalCount = Integer.valueOf(response.get("RetailUnitMetricResponse.TotalCount").toString());

            List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("RetailUnitMetricResponse.RetailUnitMetrics.RetailUnitMetric");
            if (list != null && !list.isEmpty()) {
                count += list.size();
                offset = count + 1;

                for (Map<String, Object> i : list) {
                    Map<String, Object> rliScore = (Map<String, Object>) i.get("RLIScores");

                    Period period = new Period(i.get("Period").toString());

                    String compositeIndustryId = rliScore.get("CompositeIndustry").toString();
                    String compositeIndustryName = CompositeIndustry.parseName(rliScore.get("CompositeIndustryName").toString());
                    CompositeIndustry compositeIndustry = new CompositeIndustry(compositeIndustryId, compositeIndustryName);

                    RetailUnitScore retailUnitScore = new RetailUnitScore();
                    retailUnitScore.setCompositeIndustry(compositeIndustry);
                    retailUnitScore.setPeriod(period);
                    retailUnitScore.setArea(area);
                    retailUnitScore.setComposite(Double.valueOf(Optional.fromNullable(rliScore.get("Composite")).or("").toString()));
                    retailUnitScore.setGrowth(Double.valueOf(Optional.fromNullable(rliScore.get("Growth")).or("").toString()));
                    retailUnitScore.setSales(Double.valueOf(Optional.fromNullable(rliScore.get("Sales")).or("").toString()));
                    retailUnitScore.setStability(Double.valueOf(Optional.fromNullable(rliScore.get("Stability")).or("").toString()));
                    retailUnitScore.setTicketSize(Double.valueOf(Optional.fromNullable(rliScore.get("TicketSize")).or("").toString()));
                    retailUnitScore.setTransactions(Double.valueOf(Optional.fromNullable(rliScore.get("Transactions")).or("").toString()));

                    scores.add(retailUnitScore);
                }
            }

            if (count >= totalCount) {
                break;
            }
        }
        log.debug("Fetched {} scores for {} {}", count, areaType, value);

        return scores;
    }
}
