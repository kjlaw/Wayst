package com.champions.wayst;

public class DirectionsDataModel {

    public DirectionsDataModel() {}

    public static class Directions {
        public Route[] routes;

        public Directions() {}
    }

    public static class Route {
        public Leg[] legs;
    }

    public static class Leg {
        public Step[] steps;
    }

    public static class Step {
        public String html_instructions;
        public LocationLatLng start_location;
        public LocationLatLng end_location;
    }

    public static class LocationLatLng {
        public double lat;
        public double lng;
    }
}
