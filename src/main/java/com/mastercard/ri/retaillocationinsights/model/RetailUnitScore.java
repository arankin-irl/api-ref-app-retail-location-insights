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

package com.mastercard.ri.retaillocationinsights.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "RetailUnitScore.countByPeriodAndAreaAndCompositeIndustry",
                query = "select count(s) from RetailUnitScore s where s.period = :period and s.area = :area and s.compositeIndustry = :compositeIndustry"),
        @NamedQuery(name = "RetailUnitScore.findAllByPeriodAndAreaTypeAndCompositeIndustry",
                query = "select s from RetailUnitScore s join fetch s.area a where s.period = :period and a.type = :areaType and s.compositeIndustry = :compositeIndustry"),
})
public class RetailUnitScore {
    @JsonIgnore
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    private Period period;
    @JsonIgnore
    @ManyToOne(optional = false)
    private Area area;
    @JsonIgnore
    @ManyToOne(optional = false)
    private CompositeIndustry compositeIndustry;

    private Double ticketSize;
    private Double transactions;
    private Double sales;
    private Double stability;
    private Double growth;
    private Double composite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    @JsonProperty("geoId")
    public String getGeoId() {
        return getArea().getGeoId();
    }

    public CompositeIndustry getCompositeIndustry() {
        return compositeIndustry;
    }

    public void setCompositeIndustry(CompositeIndustry compositeIndustry) {
        this.compositeIndustry = compositeIndustry;
    }

    public Double getTicketSize() {
        return ticketSize;
    }

    public void setTicketSize(Double ticketSize) {
        this.ticketSize = ticketSize;
    }

    public Double getTransactions() {
        return transactions;
    }

    public void setTransactions(Double transactions) {
        this.transactions = transactions;
    }

    public Double getSales() {
        return sales;
    }

    public void setSales(Double sales) {
        this.sales = sales;
    }

    public Double getStability() {
        return stability;
    }

    public void setStability(Double stability) {
        this.stability = stability;
    }

    public Double getGrowth() {
        return growth;
    }

    public void setGrowth(Double growth) {
        this.growth = growth;
    }

    public Double getComposite() {
        return composite;
    }

    public void setComposite(Double composite) {
        this.composite = composite;
    }
}
