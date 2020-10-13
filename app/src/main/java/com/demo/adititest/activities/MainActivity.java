package com.demo.adititest.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.demo.adititest.network.APIInterface;
import com.demo.adititest.network.ApiClient;
import com.demo.adititest.models.PlacesDetails_Modal;
import com.demo.adititest.R;
import com.demo.adititest.Response.DistanceResponse;
import com.demo.adititest.Response.PlacesResponse;
import com.demo.adititest.Response.Places_details;
import com.demo.adititest.adapters.RestaurantAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String PREFS_FILE_NAME = "SharedPref";
    private static final String TAG = "MainActivity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private long radius = 5 * 1000;

    private static final int MY_PERMISION_CODE = 10;

    private boolean Permission_is_granted = false;
    private Location mLastLocation;
    private APIInterface apiService;
    private String latLngString;
    ArrayList<PlacesResponse.CustomA> results;
    private RecyclerView recyclerview;
    public String mAddressOutput;
    private ArrayList<PlacesDetails_Modal> details_modal;
    //private String dist = "google.maps.UnitSystem.METRIC";
    private String dist = "km";

    private Toolbar mToolbar;
    private String sort = "dist";
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        apiService = ApiClient.getClient().create(APIInterface.class);
        initViews();
        // Manual check internet conn. on activity start
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network nw = connectivityManager.getActiveNetwork();
                if (nw == null) {
                    progress.setVisibility(View.GONE);
                    showSnack(false);
                }
                NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
                if (actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)))
                    showSnack(true);

            } else {
                NetworkInfo nwInfo = connectivityManager.getActiveNetworkInfo();
                if (nwInfo != null && nwInfo.isConnected())
                    showSnack(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miles:
                dist = "miles";
                break;

            case R.id.km:
                dist = "km";
                break;

            case R.id.sort_by_distance:
                dist = "km";
                sort = "dist";
                break;

            case R.id.sort_by_review:
                dist = "km";
                sort = "review";
                break;
        }
        fetchStores("restaurant");
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        progress = findViewById(R.id.progress);

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setNestedScrollingEnabled(false);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(MainActivity.this));
    }

    public void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
            getUserLocation();
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                        ACCESS_COARSE_LOCATION)) {
                    showAlert();
                } else {

                    if (isFirstTimeAskingPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        firstTimeAskingPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        ActivityCompat.requestPermissions(this,
                                new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);
                    } else {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, "You won't be able to access the features of this App", Toast.LENGTH_LONG).show();
                    }
                }
            } else Permission_is_granted = true;
        } else {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    if (location != null) {

                        mLastLocation = location;
                        double source_lat = location.getLatitude();
                        double source_long = location.getLongitude();
                        latLngString = location.getLatitude() + "," + location.getLongitude();
                        fetchCurrentAddress(latLngString);

                        Log.i(TAG, latLngString + "");

                        fetchStores("restaurant");

                    } else {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Error in fetching the location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void fetchStores(String placeType) {

        Call<PlacesResponse.Root> call = apiService.doPlaces(latLngString, radius, placeType, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<PlacesResponse.Root>() {
            @Override
            public void onResponse(Call<PlacesResponse.Root> call, Response<PlacesResponse.Root> response) {
                PlacesResponse.Root root = (PlacesResponse.Root) response.body();


                if (response.isSuccessful()) {

                    switch (root.status) {
                        case "OK":

                            results = root.customA;

                            details_modal = new ArrayList<PlacesDetails_Modal>();
                            String photourl;
                            Log.i(TAG, "fetch stores");


                            for (int i = 0; i < results.size(); i++) {

                                PlacesResponse.CustomA info = (PlacesResponse.CustomA) results.get(i);

                                String place_id = results.get(i).place_id;


                                if (results.get(i).photos != null) {

                                    String photo_reference = results.get(i).photos.get(0).photo_reference;

                                    photourl = ApiClient.base_url + "place/photo?maxwidth=100&photoreference=" + photo_reference +
                                            "&key=" + ApiClient.GOOGLE_PLACE_API_KEY;

                                } else {
                                    photourl = "NA";
                                }

                                fetchDistance(info, place_id, photourl);


                                Log.i("Coordinates  ", info.geometry.locationA.lat + " , " + info.geometry.locationA.lng);
                                Log.i("Names  ", info.name);

                            }

                            break;
                        case "ZERO_RESULTS":
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "No matches found near you", Toast.LENGTH_SHORT).show();
                            break;
                        case "OVER_QUERY_LIMIT":
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "You have reached the Daily Quota of Requests", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            progress.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error in Fetching Details,Please Refresh", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }


    private void fetchDistance(final PlacesResponse.CustomA info, final String place_id, final String photourl) {

        Log.i(TAG, "Distance API call start");

        Call<DistanceResponse> call = apiService.getDistance(dist, latLngString, info.geometry.locationA.lat + "," + info.geometry.locationA.lng, ApiClient.GOOGLE_PLACE_API_KEY);

        call.enqueue(new Callback<DistanceResponse>() {
            @Override
            public void onResponse(Call<DistanceResponse> call, Response<DistanceResponse> response) {

                DistanceResponse resultDistance = (DistanceResponse) response.body();

                if (response.isSuccessful()) {

                    Log.i(TAG, resultDistance.status);

                    if ("OK".equalsIgnoreCase(resultDistance.status)) {
                        DistanceResponse.InfoDistance row1 = resultDistance.rows.get(0);
                        DistanceResponse.InfoDistance.DistanceElement element1 = row1.elements.get(0);

                        if ("OK".equalsIgnoreCase(element1.status)) {

                            DistanceResponse.InfoDistance.ValueItem itemDistance = element1.distance;

                            String total_distance = itemDistance.text;

                            fetchPlace_details(info, place_id, total_distance, info.name, photourl);
                        }


                    }

                } else if (response.code() != 200) {
                    Toast.makeText(getApplicationContext(), "Error " + response.code() + " found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error in Fetching Details,Please Refresh", Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });

    }


    private void fetchPlace_details(final PlacesResponse.CustomA info, final String place_id, final String totaldistance, final String name, final String photourl) {

        Call<Places_details> call = apiService.getPlaceDetails(place_id, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<Places_details>() {
            @Override
            public void onResponse(Call<Places_details> call, Response<Places_details> response) {

                Places_details details = (Places_details) response.body();

                if ("OK".equalsIgnoreCase(details.status)) {

                    String address = details.result.formatted_adress;
                    String phone = details.result.international_phone_number;
                    float rating = details.result.rating;

                    details_modal.add(new PlacesDetails_Modal(address, phone, rating, totaldistance, name, photourl));

                    Log.i("details : ", info.name + "  " + address);

                    if (null != details_modal && null != results) {
                        if (details_modal.size() == results.size()) {
                            if (sort.equalsIgnoreCase("dist")) {
                                Collections.sort(details_modal, new Comparator<PlacesDetails_Modal>() {
                                    @Override
                                    public int compare(PlacesDetails_Modal lhs, PlacesDetails_Modal rhs) {
                                        return lhs.distance.compareTo(rhs.distance);
                                    }
                                });
                            } else {
                                Collections.sort(details_modal, new Comparator<PlacesDetails_Modal>() {
                                    @Override
                                    public int compare(PlacesDetails_Modal lhs, PlacesDetails_Modal rhs) {
                                        return String.valueOf(lhs.rating).compareTo(String.valueOf(rhs.rating));
                                    }
                                });
                            }

                            progress.setVisibility(View.GONE);
                            RestaurantAdapter adapterStores = new RestaurantAdapter(getApplicationContext(), details_modal, mAddressOutput, dist);
                            recyclerview.setAdapter(adapterStores);
                            //adapterStores.notifyDataSetChanged();
                        }
                    }

                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                call.cancel();
            }
        });

    }


    private void fetchCurrentAddress(final String latLngString) {

        Call<Places_details> call = apiService.getCurrentAddress(latLngString, ApiClient.GOOGLE_PLACE_API_KEY);
        call.enqueue(new Callback<Places_details>() {
            @Override
            public void onResponse(Call<Places_details> call, Response<Places_details> response) {

                Places_details details = (Places_details) response.body();

                if ("OK".equalsIgnoreCase(details.status)) {

                    mAddressOutput = details.results.get(0).formatted_adress;
                    Log.i("Addr Current and coord.", mAddressOutput + latLngString);
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                call.cancel();
            }
        });

    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings are OFF \nPlease Enable Location")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }

    public static boolean isFirstTimeAskingPermission(Context context, String permission) {
        return context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("On request permiss", "executed");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case MY_PERMISION_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Permission_is_granted = true;
                    getUserLocation();
                } else {
                    showAlert();
                    Permission_is_granted = false;
                    Toast.makeText(getApplicationContext(), "Please switch on GPS to access the features", Toast.LENGTH_LONG).show();
                    progress.setVisibility(View.GONE);

                }
                break;

        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i("google api client", "coonected");
        if (Permission_is_granted) {
            getUserLocation();
        }
        //  requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        // mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        Log.i("on start", "true");
        super.onStart();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.connect();
//        }
    }

    @Override
    protected void onResume() {

        Log.i("on resume", "true");
        super.onResume();
//        if(Permission_is_granted) {
//            if (mGoogleApiClient.isConnected()) {
//                //  getUserLocation();
//            }
//        }
    }

    @Override
    protected void onPause() {
        Log.i("on pause", "true");

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }
}