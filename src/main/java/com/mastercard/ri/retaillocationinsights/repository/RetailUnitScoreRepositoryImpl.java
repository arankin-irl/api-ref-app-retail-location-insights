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

package com.mastercard.ri.retaillocationinsights.repository;

import com.mastercard.ri.retaillocationinsights.model.Area;
import com.mastercard.ri.retaillocationinsights.model.CompositeIndustry;
import com.mastercard.ri.retaillocationinsights.model.Period;
import com.mastercard.ri.retaillocationinsights.model.RetailUnitScore;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RetailUnitScoreRepositoryImpl extends AbstractJpaRepository<RetailUnitScore>
        implements RetailUnitScoreRepository {

    @Override
    public Long countByPeriodAndAreaAndCompositeIndustry(Period period, Area area, CompositeIndustry compositeIndustry) {
        return (Long) getEntityManager().createNamedQuery("RetailUnitScore.countByPeriodAndAreaAndCompositeIndustry")
                .setParameter("period", period)
                .setParameter("area", area)
                .setParameter("compositeIndustry", compositeIndustry)
                .getSingleResult();
    }

    @Override
    public List<RetailUnitScore> findAllByPeriodAndAreaTypeAndCompositeIndustry(Period period, String areaType, CompositeIndustry compositeIndustry) {
        return getEntityManager().createNamedQuery("RetailUnitScore.findAllByPeriodAndAreaTypeAndCompositeIndustry")
                .setParameter("period", period)
                .setParameter("areaType", areaType)
                .setParameter("compositeIndustry", compositeIndustry)
                .getResultList();
    }
}
