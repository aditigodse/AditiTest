package com.demo.adititest.Response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Aditi on 13-10-20.
 */

public class PlacesResponse {

    public class Root implements Serializable {

        @SerializedName("results")
        public ArrayList<CustomA> customA = new ArrayList<>();
        @SerializedName("status")
        public String status;
    }

    public class CustomA implements Serializable {

        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("vicinity")
        public String vicinity;

        @SerializedName("name")
        public String name;

        @SerializedName("photos")
        public ArrayList<Photos> photos = new ArrayList<>();

        @SerializedName("place_id")
        public String place_id;

    }

    public class Photos implements Serializable {

        @SerializedName("photo_reference")
        public String photo_reference;

    }

    public class Geometry implements Serializable {

        @SerializedName("location")
        public LocationA locationA;

    }

    public class LocationA implements Serializable {

        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;

    }
}
