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

var View = (function($, document) {
  'use strict';
  var select2Options = {
    minimumResultsForSearch: Infinity,
    width: '18rem',
  };

  return function() {
    var atmTemplate;
    var serviceItemTemplate;

    var maps;
    var onMapInitializedCallback;

    function showLoading() {
      $('#loading-overlay').removeClass('hide');
    }

    function hideLoading() {
      $('#loading-overlay').addClass('hide');
    }

    return {
      init: function() {
        $('#metricSelect').select2(select2Options);
      },

      getMaps: function() {
        return maps;
      },

      initMap: function() {
        maps = new Maps(document.getElementById('map'), function() {
          $('#controls-container').removeClass('hide');
          $('#legend').removeClass('hide');

          if (onMapInitializedCallback) {
            onMapInitializedCallback();
          }
        });

        maps.addLegend(document.getElementById('legend'));
      },

      setOnMapInitializedHandler: function(callback) {
        onMapInitializedCallback = callback;
      },

      setZoomChangedHandler: function(callback) {
        maps.setZoomChangedHandler(callback);
      },

      showPeriods: function(periods) {
        if (periods.length > 1) {
          periods.forEach(function(period) {
            $('#periodSelect').append($('<option>', {
              value: period.id,
              text: period.name
            }));
          });
          $('#periodSelect').select2(select2Options);
        } else if (periods.length === 1) {
          $('#periodSpan').html(periods[0].name).removeClass('hide');
        }
      },

      showCategories: function(categories) {
        categories.forEach(function(category) {
          $('#categorySelect').append($('<option>', {
            value: category.id,
            text: category.name
          }));
        });
        $('#categorySelect').select2(select2Options);
      },

      setPeriodChangedHandler: function(callback) {
        $('#periodSelect').change(function(e) {
          callback(e.target.value);
        });
      },

      setCategoryChangedHandler: function(callback) {
        $('#categorySelect').change(function(e) {
          callback(e.target.value);
        });
      },

      setMetricChangedHandler: function(callback) {
        $('#metricSelect').change(function(e) {
          callback(e.target.value);
        });
      },

      showArea: function(geojsonUrl, callback) {
        if (maps) {
          var deferred = $.Deferred();
          showLoading();

          maps.setGeoJsonUrl(geojsonUrl, function() {
            deferred.resolve();
            hideLoading();
          });

          return deferred.promise();
        }
      },

      showMetrics: function(currentMetricsList, currentMetric) {
        if (maps) {
          maps.showMetrics(currentMetricsList, currentMetric);
        }
      },

      showLoading: showLoading,
      hideLoading: hideLoading,
    };
  };
})(window.jQuery, window.document);
