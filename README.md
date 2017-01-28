# Retail Location Insights Reference Implementation

This repository showcases a reference implementation for [Retail Location Insights](https://developer.mastercard.com/documentation/retail-location-insights) using APIs from [Mastercard Developers](https://developer.mastercard.com). 
The shapefiles of the areas are from [United States Census Bureau](https://www.census.gov/cgi-bin/geo/shapefiles/index.php).

Try the [demo](https://www.mastercardlabs.com/ref-impl-retail-location-insights/).

## Frameworks / Libraries used
- [Spring Framework](https://projects.spring.io/spring-framework/) 3.2.17
- [Hibernate ORM](http://hibernate.org/orm/) 3
- [GeoTools](http://www.geotools.org/) 14.3
- [jQuery](https://jquery.com/)  3.1.1
- [sanitize](https://jonathantneal.github.io/sanitize.css/) 4.1.0

## Requirements
- Java 7 and above
- Set up the `JAVA_HOME` environment variable to match the location of your Java installation.

## Setup
1. Create an account at [Mastercard Developers](https://developer.mastercard.com).
2. Create a new project and add `Retail Location Insights` API to your project. A `.p12` file is downloaded automatically. **Note**: On Safari, the file name will be `Unknown`. Rename it to a .p12 extension.
3. Copy the downloaded `.p12` file to `src/main/resources`.
4. Open `src/main/resources/mastercard-api.properties` and configure:
  - `mastercard.api.debug` - `true` if you need console logging, otherwise `false`.
  - `mastercard.api.p12.path` - Path to keystore (.p12). Uses Spring's resource strings.
  - `mastercard.api.consumer.key` - Consumer key. Copy this from "My Keys" on your project page
  - `mastercard.api.key.alias` - Key alias. Default key alias for sandbox project is `keyalias`.
  - `mastercard.api.keystore.password` - Keystore password. Default keystore password for sandbox project is `keystorepassword`.
  - `mastercard.api.sandbox` - `true` if you are using sandbox environment, otherwise `false`.
5. Get a Google Maps API key at [Google Maps APIs](https://developers.google.com/maps/documentation/javascript/get-api-key#get-an-api-key).
6. Open `src/main/resources/google-maps.properties` and configure:
  - `google.maps.api.key` - This value is automatically applied to `src/main/webapp/WEB-INF/static/index.html` during the maven build process.
7. Configure database. For ease of demonstration, we are using H2 in-memory database. You do not need to change any configuration if you are fine with that.
Otherwise, you can change to a different database by configuring it in `tomcat/context.xml`. 

## Build and Run

> Windows: `mvnw.cmd clean tomcat7:run-war`

> Linux / Mac: `./mvnw clean tomcat7:run-war`

Note that the application may take some time to start up completely because it needs to retrieve the metrics for the areas from the APIs. 

Open [http://localhost:9090/ref-impl-retail-location-insights/](http://localhost:9090/ref-impl-retail-location-insights/).

## Deploying to your own server

> Windows: `mvnw.cmd clean package`

> Linux / Mac: `./mvnw clean package`

Note that the application may take some time to start up completely because it needs to retrieve the metrics for the areas from the APIs. 

Deploy `ref-impl-retail-location-insights.war` in `target/` directory to your container.
