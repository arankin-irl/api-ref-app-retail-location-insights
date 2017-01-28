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

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mastercard.api.core.model.RequestMap;
import com.mastercard.api.retaillocationinsights.RetailUnits;
import com.mastercard.ri.retaillocationinsights.model.Area;
import com.mastercard.ri.retaillocationinsights.model.CompositeIndustry;
import com.mastercard.ri.retaillocationinsights.model.Period;
import com.mastercard.ri.retaillocationinsights.model.RetailUnitScore;
import com.mastercard.ri.retaillocationinsights.repository.AreaRepository;
import com.mastercard.ri.retaillocationinsights.repository.CompositeIndustryRepository;
import com.mastercard.ri.retaillocationinsights.repository.PeriodRepository;
import com.mastercard.ri.retaillocationinsights.repository.RetailUnitScoreRepository;
import com.mastercard.ri.retaillocationinsights.runnable.FetchMetricsCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class MetricServiceImpl implements MetricService {
    private static final Logger log = LoggerFactory.getLogger(MetricServiceImpl.class);

    private final AreaRepository areaRepository;
    private final RetailUnitScoreRepository retailUnitScoreRepository;
    private final PeriodRepository periodRepository;
    private final CompositeIndustryRepository compositeIndustryRepository;
    private final ExecutorService executorService;

    private final String country;
    private final String[] areaTypes;

    @Autowired
    public MetricServiceImpl(AreaRepository areaRepository, RetailUnitScoreRepository retailUnitScoreRepository,
                             PeriodRepository periodRepository, CompositeIndustryRepository compositeIndustryRepository,
                             Environment env, ExecutorService executorService) {
        this.areaRepository = areaRepository;
        this.retailUnitScoreRepository = retailUnitScoreRepository;
        this.periodRepository = periodRepository;
        this.compositeIndustryRepository = compositeIndustryRepository;

        this.country = env.getProperty("ril.country");
        this.areaTypes = env.getProperty("ril.area.type.available").split(",");
        this.executorService = executorService;
    }

    @Override
    @Transactional
    public void fetchMetricsFromApi() {
        int count = 0, offset = 0;

        SetMultimap<String, String> retailUnits = HashMultimap.create();

        log.info("Fetching retail units from API");
        while (true) {
            RequestMap map = new RequestMap();
            map.set("PageLength", 99);
            map.set("PageOffset", offset);
            map.set("CountryCode", country);

            RetailUnits response;
            try {
                response = RetailUnits.query(map);
            } catch (Exception e) {
                log.error("Error fetching RetailUnits", e);
                throw new RuntimeException(e);
            }

            int totalCount = Integer.valueOf(response.get("RetailUnitResponse.TotalCount").toString());

            List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("RetailUnitResponse.RetailUnits.RetailUnit");
            if (list != null && !list.isEmpty()) {
                count += list.size();
                offset = count + 1;

                for (Map<String, Object> i : list) {
                    for (String areaType : areaTypes) {
                        retailUnits.put(areaType, Optional.fromNullable(i.get(areaType)).or("").toString());
                    }
                }
            }

            if (count >= totalCount) {
                break;
            }
        }
        log.debug("Fetched {} retail units from API", count);

        Period latestPeriod = periodRepository.getLatestPeriod();
        String nextPeriod = null;
        if (latestPeriod != null) {
            nextPeriod = latestPeriod.getYear() + "/" + String.format("%02d", latestPeriod.getMonth() + 1);
        }

        log.info("Fetching retail unit metrics from API");
        List<Future<List<RetailUnitScore>>> futures = new ArrayList<>();
        for (Map.Entry<String, String> entry : retailUnits.entries()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Area area = areaRepository.findByTypeAndGeoId(key, value);
            if (area == null) {
                log.info("Area {} {} not found. Skipped fetching scores.", key, value);
                continue;
            }

            futures.add(executorService.submit(new FetchMetricsCallable(nextPeriod, area)));
        }

        for (Future<List<RetailUnitScore>> f : futures) {
            List<RetailUnitScore> scores;
            try {
                scores = f.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error fetching scores", e);
                continue;
            }

            for (RetailUnitScore score : scores) {
                Period period = periodRepository.findOne(score.getPeriod().getId());
                if (period == null) {
                    log.debug("Creating period {}", score.getPeriod().getId());
                    period = periodRepository.save(score.getPeriod());
                }
                score.setPeriod(period);

                CompositeIndustry compositeIndustry = compositeIndustryRepository.findByName(score.getCompositeIndustry().getName());
                if (compositeIndustry == null) {
                    log.debug("Creating composite industry {} - {}", score.getCompositeIndustry().getId(), score.getCompositeIndustry().getName());
                    compositeIndustry = compositeIndustryRepository.save(score.getCompositeIndustry());
                }
                score.setCompositeIndustry(compositeIndustry);

                Area area = score.getArea();
                if (retailUnitScoreRepository.countByPeriodAndAreaAndCompositeIndustry(period, area, compositeIndustry) > 0) {
                    log.info("Metrics for area {} {}, period {}, composite industry {} already exist.", area.getType(),
                            area.getGeoId(), period.getId(), compositeIndustry.getName());
                    continue;
                }

                retailUnitScoreRepository.save(score);
            }
        }

        log.info("Fetched metrics from API");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetailUnitScore> getMetrics(String periodStr, String areaType, String compositeIndustryStr) {
        log.info("Getting metrics for period {}, area type {}, composite industry {}", periodStr, areaType, compositeIndustryStr);

        Period period = periodRepository.findOne(periodStr);
        CompositeIndustry compositeIndustry = compositeIndustryRepository.findOne(compositeIndustryStr);

        if (period == null || compositeIndustry == null) {
            return Collections.emptyList();
        }

        return retailUnitScoreRepository.findAllByPeriodAndAreaTypeAndCompositeIndustry(period, areaType, compositeIndustry);
    }
}
