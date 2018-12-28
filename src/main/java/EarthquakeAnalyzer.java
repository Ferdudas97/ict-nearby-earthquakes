import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;
import org.geojson.Point;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EarthquakeAnalyzer {
    private static final String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";
    private static final long earthRadius = 6371; //earth radius in kilometers

    public static void showTheClosestEarthquakes(final double longitude, final double latitude) throws IOException {
        val featureCollection = getFeatureCollectionFromUrl(url);
        val pointAndTitleMap = getPointAndTitleMap(featureCollection);
        val filteredPoints = getFilteredPoints(pointAndTitleMap.keySet());
        filteredPoints.stream()
                .collect(Collectors.toMap(Function.identity(), point -> computeDistance(point, longitude, latitude)))
                .entrySet()
                .parallelStream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .limit(10)
                .forEachOrdered(entry -> System.out.println(pointAndTitleMap.get(entry.getKey()) + " || " + Math.round(entry.getValue())));
    }

    private static FeatureCollection getFeatureCollectionFromUrl(final String url) throws IOException {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(new URL(url), FeatureCollection.class);

    }

    private static Map<Point, String> getPointAndTitleMap(final FeatureCollection collection) {
        return collection.getFeatures()
                .parallelStream()
                .collect(Collectors.toMap(feature -> (Point) feature.getGeometry(), f -> f.getProperty("title")));
    }

    private static Set<Point> getFilteredPoints(final Set<Point> unfilteredPoints) {
        Set<Point> filteredPoints = new HashSet<>();

        unfilteredPoints.stream()
                .filter(point -> distinctByCoords(point,filteredPoints))
                .forEach(filteredPoints::add);

        return filteredPoints;
    }

    private static boolean distinctByCoords(final Point point, final Set<Point> pointSet) {
        return pointSet.parallelStream()
                .noneMatch(p -> checkIfCoordinatesEqual(p.getCoordinates(), point.getCoordinates()));
    }

    private static boolean checkIfCoordinatesEqual(final LngLatAlt cord1, final LngLatAlt cord2) {
//        return Math.round(cord1.getLongitude()) == Math.round(cord2.getLongitude())
//                && Math.round(cord2.getLatitude()) == Math.round(cord1.getLatitude()); // Coordinates are rounded because
                                                                                      // it`s hard to get two same coordinates

        return (cord1.getLongitude()) == (cord2.getLongitude()) && (cord2.getLatitude()) == (cord1.getLatitude());


    }

    private static Double computeDistance(final Point pointFromApi, final double longitude, final double latitude) {
        // algorithm from https://www.movable-type.co.uk/scripts/latlong.html
        val apiLatInRadians = Math.toRadians(pointFromApi.getCoordinates().getLatitude());
        val apiLongInRadians = Math.toRadians(pointFromApi.getCoordinates().getLongitude());
        val latInRadians = Math.toRadians(latitude);
        val longInRadians = Math.toRadians(longitude);
        val latDelta = apiLatInRadians - latInRadians;
        val longDelta = apiLongInRadians - longInRadians;
        val a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2) +
                Math.cos(apiLatInRadians) * Math.cos(latInRadians) *
                        Math.sin(longDelta / 2) * Math.sin(longDelta / 2);
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;

    }
}
