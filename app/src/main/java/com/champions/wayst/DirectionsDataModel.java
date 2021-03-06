package com.champions.wayst;

public class DirectionsDataModel {

    public enum Direction {
        UNKNOWN("unknown"),
        LEFT("left"),
        RIGHT("right"),
        CONTINUE("continue");

        public final String desc;

        Direction(String desc) {
            this.desc = desc;
        }
    }

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
        public LocationLatLng end_location;
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

    public static DirectionsDataModel.Direction parseDescription(String description) {
        description = description.toLowerCase();
        if (description.contains("left")) {
            return DirectionsDataModel.Direction.LEFT;
        } else if (description.contains("right")) {
            return DirectionsDataModel.Direction.RIGHT;
        } else if (description.contains("continue")) {
            return DirectionsDataModel.Direction.CONTINUE;
        }
        return DirectionsDataModel.Direction.UNKNOWN;
    }
}
