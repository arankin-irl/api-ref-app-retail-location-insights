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

var Maps = (function() {
  "use strict";

  return function(domElem, mapLoadedCallback) {
    var map;
    var infoWindow = new google.maps.InfoWindow();
    var hasControls = false;

    function init() {
      map = new google.maps.Map(domElem, {
        center: {lat: 36.9726631, lng: -99.6682147},
        zoom: 4,
        disableDefaultUI: true,
        zoomControl: true,
        styles: [
          {
            "elementType": "geometry",
            "stylers": [ {"color": "#f5f5f5"}]
          },
          {
            "elementType": "labels.text.fill",
            "stylers": [ {"color": "#616161"} ]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [ {"color": "#f5f5f5"} ]
          },
          {
            "featureType": "administrative.province",
            "elementType": "geometry.stroke",
            "stylers": [ {"color": "#a9b3c7"},{"visibility": "on"} ]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [ {"color": "#c9c9c9"} ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text",
            "stylers": [{ "visibility": "off" }]
          }
        ]
      });

      google.maps.event.addListener(map, "click", function() {
        closeInfoWindow();
      });

      map.data.setStyle({
        clickable: false,
        visible: false
      });

      map.data.addListener('click', function (event) {
        var metrics = event.feature.getProperty('metrics');

        if (metrics) {
          infoWindow.setContent('<div class="metrics">' +
            '<div>Composite Upfront Score: ' + metrics['composite'] + '</div>' +
            '<div>Growth: ' + metrics['growth'] + '</div>' +
            '<div>Sales: ' + metrics['sales'] + '</div>' +
            '<div>Stability: ' + metrics['stability'] + '</div>' +
            '<div>Ticket Size: ' + metrics['ticketSize'] + '</div>' +
            '<div>Transactions: ' + metrics['transactions'] + '</div>' +
            '</div>');
          infoWindow.setPosition(event.latLng);
          infoWindow.open(map);
        }
      });

      if (mapLoadedCallback) {
        google.maps.event.addListenerOnce(map, 'idle', mapLoadedCallback);
      }
    }

    function closeInfoWindow() {
      if (infoWindow) {
        infoWindow.close();
      }
    }

    function getMetricColor(metrics, currentMetric) {
      var score = metrics[currentMetric];
      if (score >= 860) {
        return '#004B19';
      } else if (score >= 710) {
        return '#016B25';
      } else if (score >= 570) {
        return '#24AF3C';
      } else if (score >= 430) {
        return '#10E11F';
      } else if (score >= 290) {
        return '#6AED1F';
      } else if (score >= 140) {
        return '#9DFF74';
      } else if (score >= 0) {
        return '#C9FFAE';
      }
    }

    function findMetrics(metricsList, geoId) {
      for (var i=0; i<metricsList.length; i++) {
        if (metricsList[i].geoId === geoId) {
          return metricsList[i];
        }
      }
    }

    init();

    return {
      setGeoJsonUrl: function(geojsonUrl, callback) {
        closeInfoWindow();
        map.data.forEach(function (feature) {
          map.data.remove(feature);
        });
        map.data.loadGeoJson(geojsonUrl, {}, function() {
          if (callback) {
            callback();
          }
        });
      },

      setZoomChangedHandler: function(callback) {
        google.maps.event.clearListeners(map, 'zoom_changed');

        if (callback) {
          google.maps.event.addListener(map, 'zoom_changed', function() {
            callback(map.getZoom());
          });
        }
      },

      hasControls: function() {
        return hasControls;
      },

      addLegend: function(elem) {
        map.controls[google.maps.ControlPosition.LEFT_BOTTOM].push(elem);
      },

      addControls: function(elem) {
        map.controls[google.maps.ControlPosition.LEFT_TOP].push(elem);
        hasControls = true;
      },

      removeControls: function() {
        map.controls[google.maps.ControlPosition.LEFT_TOP].clear();
        hasControls = false;
      },

      showMetrics: function(metricsList, currentMetric) {
        closeInfoWindow();

        map.data.setStyle(function(feature) {
          var metrics = findMetrics(metricsList, feature.getId());

          if (metrics) {
            var color = getMetricColor(metrics, currentMetric);

            if (color) {
              feature.setProperty('metrics', metrics);

              return {
                visible: true,
                clickable: true,
                fillColor: color,
                fillOpacity: 0.6,
                strokeWeight: 0.7
              };
            }
          }

          return {
            clickable: false,
            visible: false
          };
        });
      }
    };
  };
})();
