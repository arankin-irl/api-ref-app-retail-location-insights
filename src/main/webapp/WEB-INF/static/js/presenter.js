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

var Presenter = (function($, API) {
  'use strict';

  return function(view) {
    var configuration;
    var currentPeriod;
    var currentCategory;
    var currentAreaType;
    var currentMetric = 'composite';
    var currentMetricsList = [];
    var areaZoomLevels;

    function init() {
      view.setOnMapInitializedHandler(function() {
        fetchConfiguration();

        view.setZoomChangedHandler(zoomLevelChangedHandler);
        view.setPeriodChangedHandler(periodChangedHandler);
        view.setCategoryChangedHandler(categoryChangedHandler);
        view.setMetricChangedHandler(metricChangedHandler);

        view.showMetrics(currentMetricsList, currentMetric);
      });
    }

    function periodChangedHandler(newPeriod) {
      currentPeriod = newPeriod;
      showMetrics();
    }

    function categoryChangedHandler(newCategory) {
      currentMetricsList = [];
      currentCategory = newCategory;
      showMetrics();
    }

    function zoomLevelChangedHandler(zoomLevel) {
      if (areaZoomLevels) {
        for (var i=0; i<areaZoomLevels.length; i++) {
          var areaZoomLevel = areaZoomLevels[i];
          var nextAreaZoomLevel = areaZoomLevels[i+1];

          if (zoomLevel >= areaZoomLevel.zoomLevel &&
            (!nextAreaZoomLevel || zoomLevel < nextAreaZoomLevel.zoomLevel)) {

            if (areaZoomLevel.areaType !== currentAreaType) {
              currentAreaType = areaZoomLevel.areaType;
              showAreaAndMetrics();
            }

            return;
          }
        }
      }
    }

    function metricChangedHandler(newMetric) {
      currentMetric = newMetric;
      view.showMetrics(currentMetricsList, currentMetric);
    }

    function fetchConfiguration() {
      if (configuration) {
          return;
      }

      API.getConfiguration()
        .done(function (data) {
          configuration = data;

          currentPeriod = configuration.periods[0].id;
          currentAreaType = configuration.areaZoomLevels[0].areaType;
          currentCategory = configuration.compositeIndustries[0].id;

          areaZoomLevels = configuration.areaZoomLevels;

          view.showPeriods(configuration.periods);
          view.showCategories(configuration.compositeIndustries);

          showAreaAndMetrics();
        });
    }

    function showAreaAndMetrics() {
      view.showLoading();
      var deferredArea = view.showArea('api/area?areaType=' + currentAreaType);
      var deferredMetrics = API.getMetrics(currentPeriod, currentAreaType, currentCategory);

      $.when(deferredArea, deferredMetrics)
        .done(function(ignored, metrics) {
          currentMetricsList = metrics[0];
          view.showMetrics(currentMetricsList, currentMetric);
        });
    }

    function showMetrics() {
      API.getMetrics(currentPeriod, currentAreaType, currentCategory)
        .done(function(metrics) {
          currentMetricsList = metrics;
          view.showMetrics(currentMetricsList, currentMetric);
        });
    }

    init();

    return {
    };
  };
})(window.jQuery, API);
