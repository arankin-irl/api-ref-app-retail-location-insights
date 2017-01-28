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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.mastercard.ri.retaillocationinsights.dto.GeoJson;
import com.mastercard.ri.retaillocationinsights.dto.GeoJsonFeature;
import com.mastercard.ri.retaillocationinsights.model.Area;
import com.mastercard.ri.retaillocationinsights.repository.AreaRepository;
import com.mastercard.ri.retaillocationinsights.util.FileUtils;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geojson.geom.GeometryJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Service
public class AreaServiceImpl implements AreaService {
    private static final Logger log = LoggerFactory.getLogger(AreaServiceImpl.class);

    private final AreaRepository areaRepository;
    private final Environment env;

    private final String[] areaTypes;

    @Autowired
    public AreaServiceImpl(Environment env, AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
        this.env = env;

        this.areaTypes = env.getProperty("ril.area.type.available").split(",");
    }

    @Override
    public String[] getAreaTypes() {
        return areaTypes;
    }

    @Override
    @Transactional
    public void parseAndStoreShapeFiles() {
        Map<String, String> unzippedFileLocations = unzipShapeFiles();

        if (unzippedFileLocations.isEmpty()) {
            log.info("No files unzipped!");
            return;
        }

        GeometryJSON geometryJSON = new GeometryJSON();

        DirectoryStream.Filter<Path> shpFileFilter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return entry.getFileName().toString().endsWith(".shp");
            }
        };

        for (Map.Entry<String, String> entry : unzippedFileLocations.entrySet()) {
            String areaType = entry.getKey();
            String unzippedDirectory = entry.getValue();

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(unzippedDirectory), shpFileFilter)) {
                for (Path shpFile : directoryStream) {
                    log.debug("Parsing {} at {}", areaType, shpFile.toString());

                    FileDataStore dataStore = null;
                    SimpleFeatureIterator featureIterator = null;
                    try {
                        dataStore = FileDataStoreFinder.getDataStore(shpFile.toFile());
                        SimpleFeatureCollection featureCollection = dataStore.getFeatureSource().getFeatures();
                        featureIterator = featureCollection.features();

                        while (featureIterator.hasNext()) {
                            SimpleFeature feature = featureIterator.next();

                            String geoId = feature.getProperty("GEOID").getValue().toString();
                            StringWriter writer = new StringWriter();

                            Geometry geometry = (Geometry) feature.getDefaultGeometry();
                            geometryJSON.write(geometry, writer);
                            writer.flush();
                            writer.close();

                            Area area = new Area();
                            area.setType(areaType);
                            area.setGeoId(geoId);
                            area.setGeometry(writer.toString());

                            areaRepository.save(area);
                        }
                    } catch (Exception ex) {
                        log.error("Error parsing shapefile at " + shpFile.toString(), ex);
                    } finally {
                        if (featureIterator != null) {
                            featureIterator.close();
                        }
                        if (dataStore != null) {
                            dataStore.dispose();
                        }
                    }
                }
            } catch (IOException ex) {
                log.error("Cannot open or read directory {}", unzippedDirectory);
            } finally {
                try {
                    log.debug("Deleting {}", unzippedDirectory);
                    Files.walkFileTree(Paths.get(unzippedDirectory), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }

                    });
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GeoJson getArea(String areaType) {
        log.info("Getting area geojson for area type {}", areaType);

        List<Area> areas = areaRepository.findAllByType(areaType);
        log.debug("areas.size = {}", areas.size());

        List<GeoJsonFeature> features = new ArrayList<>(areas.size());
        for (Area area : areas) {
            GeoJsonFeature feature = new GeoJsonFeature(area.getGeoId(), area.getGeometry(), Collections.<String, Object>emptyMap());
            features.add(feature);
        }

        return new GeoJson(features);
    }


    private ListMultimap<String, String> getShapeFileLocations() {
        ListMultimap<String, String> shapeFileLocations = ArrayListMultimap.create();

        for (String areaType : areaTypes) {
            String paths = env.getProperty("ril.area.type.shapefile." + areaType.toLowerCase());
            if (paths == null || paths.isEmpty()) {
                continue;
            }
            shapeFileLocations.putAll(areaType, Arrays.asList(paths.split(",")));
        }

        return shapeFileLocations;
    }

    private Map<String, String> unzipShapeFiles() {
        Map<String, String> unzippedFileLocations = new HashMap<>();

        ListMultimap<String, String> shapeFileLocations = getShapeFileLocations();
        for (Map.Entry<String, List<String>> entry : Multimaps.asMap(shapeFileLocations).entrySet()) {
            String areaType = entry.getKey();

            // check if there is data for the area type stored in the database
            Long count = areaRepository.countByType(areaType);

            if (count > 0) {
                log.info("{} is already populated. Skipping it.", areaType);
                continue;
            }

            // temporary holding area for unzipped file contents
            Path tempDirectory;
            try {
                tempDirectory = Files.createTempDirectory("shapefiles-" + areaType.toLowerCase()).toAbsolutePath();
            } catch (IOException ex) {
                log.error("Unable to create temp directory", ex);
                throw new RuntimeException(ex);
            }

            for (String shapeFileLocation : entry.getValue()) {
                log.info("Reading zip file at {}", shapeFileLocation);

                try (InputStream inputStream = FileUtils.getResourceAsStream(shapeFileLocation)) {
                    FileUtils.unzip(inputStream, tempDirectory.toString());

                    if (!unzippedFileLocations.containsKey(areaType)) {
                        unzippedFileLocations.put(areaType, tempDirectory.toString());
                    }
                } catch (FileNotFoundException e) {
                    log.warn(shapeFileLocation + " does not exist!", e);
                } catch (IOException e) {
                    log.error("Unable to unzip {} to temp directory", shapeFileLocation);
                    throw new RuntimeException(e);
                }
            }
        }

        return unzippedFileLocations;
    }
}
