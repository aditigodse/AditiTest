package com.demo.adititest.Response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Aditi on 13-10-20.
 */

public class DistanceResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("rows")
    public ArrayList<InfoDistance> rows = new ArrayList<>();

    public class InfoDistance {
        @SerializedName("elements")
        public ArrayList<DistanceElement> elements = new ArrayList<>();

        public class DistanceElement {
            @SerializedName("status")
            public String status;

            @SerializedName("duration")
            public ValueItem duration;

            @SerializedName("distance")
            public ValueItem distance;
        }

        public class ValueItem {
            @SerializedName("value")
            public long value;

            @SerializedName("text")
            public String text;

        }
    }
}
