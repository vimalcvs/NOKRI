package com.scriptsbundle.nokri.employeer.edit.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.JsonObject;
import com.scriptsbundle.nokri.manager.Nokri_DialogManager;
import com.scriptsbundle.nokri.manager.Nokri_RequestHeaderManager;
import com.scriptsbundle.nokri.manager.Nokri_SharedPrefManager;
import com.scriptsbundle.nokri.manager.Nokri_ToastManager;
import com.scriptsbundle.nokri.network.Nokri_ServiceGenerator;
import com.scriptsbundle.nokri.rest.RestService;
import com.scriptsbundle.nokri.R;
import com.scriptsbundle.nokri.manager.Nokri_FontManager;
import com.scriptsbundle.nokri.utils.Nokri_Config;
import com.scriptsbundle.nokri.utils.Nokri_Globals;
import com.scriptsbundle.nokri.utils.Nokri_Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class Nokri_LocationAndMapFragment extends Fragment implements OnMapReadyCallback,View.OnFocusChangeListener,View.OnClickListener,GoogleMap.OnMyLocationChangeListener,AdapterView.OnItemClickListener, TextWatcher {


    private TextView setLocationTextView,latitudeTextView,longitudeTextView;
    private MapView map;
    private EditText latitudeEditText,longitudeEditText;
    private Button saveLocatiosButton;
    private Nokri_FontManager fontManager;
    private static double LATITUDE,LONGITUDE;
    private GoogleMap googleMap;
    private boolean shouldWait = true;
    private boolean isPaused = false;

    private Nokri_DialogManager dialogManager;
    private PlacesClient placesClient;
    ArrayList<String> places = new ArrayList<>();
    ArrayList<String> ids = new ArrayList<>();

    private AutoCompleteTextView autoCompleteTextView;
    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
        isPaused = true;
        dialogManager.hideAlertDialog();
    }


    public Nokri_LocationAndMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nokri_initialize();
        nokri_setFonts();
        nokri_getLocationAndAddress();

    }
    private void nokri_setFonts() {


        fontManager.nokri_setMonesrratSemiBioldFont(setLocationTextView,getActivity().getAssets());

        fontManager.nokri_setMonesrratSemiBioldFont(latitudeTextView,getActivity().getAssets());
        fontManager.nokri_setMonesrratSemiBioldFont(longitudeTextView,getActivity().getAssets());

        fontManager.nokri_setOpenSenseFontEditText(latitudeEditText,getActivity().getAssets());
        fontManager.nokri_setOpenSenseFontEditText(longitudeEditText,getActivity().getAssets());

        fontManager.nokri_setOpenSenseFontEditText(autoCompleteTextView,getActivity().getAssets());

        fontManager.nokri_setOpenSenseFontButton(saveLocatiosButton,getActivity().getAssets());
    }
    private void nokri_initialize() {
        fontManager = new Nokri_FontManager();

        setLocationTextView = getView().findViewById(R.id.txt_set_location);

        latitudeTextView = getView().findViewById(R.id.txt_latitude);
        longitudeTextView = getView().findViewById(R.id.txt_longitude);

        latitudeEditText = getView().findViewById(R.id.edittxt_latitude);
        longitudeEditText = getView().findViewById(R.id.edittxt_longitude);
        placesClient = Places.createClient(getContext());
        autoCompleteTextView  = getView().findViewById(R.id.autoCompleteTextView);

        latitudeEditText.setOnFocusChangeListener(this);
        longitudeEditText.setOnFocusChangeListener(this);
        autoCompleteTextView.setOnFocusChangeListener(this);
        autoCompleteTextView.addTextChangedListener(this);
        autoCompleteTextView.setOnItemClickListener(this);
      //  addressEditText.addTextChangedListener(this);
        saveLocatiosButton = getView().findViewById(R.id.btn_savelocations);
        saveLocatiosButton.setOnClickListener(this);
        Nokri_Utils.setEditBorderButton(getContext(),saveLocatiosButton);







        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nokri_location_and_map, container, false);
        map = view.findViewById(R.id.map_fragment);
        map.onCreate(savedInstanceState);

        map.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.getMapAsync(this);
        return view;
    }




    @Override
    public void onClick(View view) {
        Nokri_Utils.checkEditTextForError(autoCompleteTextView);
        Nokri_Utils.checkEditTextForError(latitudeEditText);
        Nokri_Utils.checkEditTextForError(longitudeEditText);
        if (!autoCompleteTextView.getText().toString().trim().isEmpty()&&!latitudeEditText.getText().toString().trim().isEmpty() && !longitudeEditText.getText().toString().trim().isEmpty())
            nokri_postLocationAndAddress();

        else Nokri_ToastManager.showLongToast(getContext(), Nokri_Globals.EMPTY_FIELDS_PLACEHOLDER);
    }

    private void nokri_postLocationAndAddress(){

        dialogManager = new Nokri_DialogManager();
        dialogManager.showAlertDialog(getActivity());




        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("emp_lat",latitudeEditText.getText().toString());
        jsonObject.addProperty("emp_long",longitudeEditText.getText().toString());
        Nokri_ToastManager.showShortToast(getContext(),autoCompleteTextView.getText().toString());
        jsonObject.addProperty("emp_loc",autoCompleteTextView.getText().toString());






        RestService restService =  Nokri_ServiceGenerator.createService(RestService.class, Nokri_SharedPrefManager.getEmail(getContext()), Nokri_SharedPrefManager.getPassword(getContext()),getContext());

        Call<ResponseBody> myCall;
        if(Nokri_SharedPrefManager.isSocialLogin(getContext())) {
            myCall = restService.postEmployeerLocation(jsonObject, Nokri_RequestHeaderManager.addSocialHeaders());
        } else

        {
            myCall = restService.postEmployeerLocation(jsonObject, Nokri_RequestHeaderManager.addHeaders());
        }
        // Call<ResponseBody> myCall = restService.postCandidateLocation(jsonObject, Nokri_RequestHeaderManager.addHeaders());
        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObject) {
                if(responseObject.isSuccessful()){
                    try {
                        JSONObject response = new JSONObject(responseObject.body().string());
                        Log.v("response",responseObject.message());
                        if (response.getBoolean("success")) {
                            dialogManager.hideAlertDialog();
                            nokri_getLocationAndAddress();
                            Nokri_ToastManager.showLongToast(getContext(),response.getString("message"));
                        } else {
                            Nokri_ToastManager.showLongToast(getContext(),response.getString("message"));

                            dialogManager.hideAfterDelay();
                        }

                    } catch (JSONException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();

                        e.printStackTrace();
                    } catch (IOException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();

                    }
                }
                else {
                    dialogManager.showCustom(responseObject.code()+"");
                    dialogManager.hideAfterDelay();

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Nokri_ToastManager.showLongToast(getContext(),t.getMessage());
                dialogManager.hideAfterDelay();

            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean selected) {
        switch (view.getId()){
            case R.id.autoCompleteTextView:
                if(selected)
                {
                    autoCompleteTextView.setHintTextColor(getResources().getColor(R.color.quantum_grey));
                    latitudeEditText.setHintTextColor(getResources().getColor(R.color.grey));
                    longitudeTextView.setHintTextColor(getResources().getColor(R.color.grey));

                }
                break;
            case R.id.edittxt_latitude:
                if(selected)
                {
                    autoCompleteTextView.setHintTextColor(getResources().getColor(R.color.grey));
                    latitudeEditText.setHintTextColor(getResources().getColor(R.color.quantum_grey));
                    longitudeTextView.setHintTextColor(getResources().getColor(R.color.grey));

                }
                break;
            case R.id.edittxt_longitude:
                if(selected){

                    autoCompleteTextView.setHintTextColor(getResources().getColor(R.color.grey));
                    latitudeEditText.setHintTextColor(getResources().getColor(R.color.grey));
                    longitudeTextView.setHintTextColor(getResources().getColor(R.color.quantum_grey));

                }
                break;


        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        googleMap.setOnMyLocationChangeListener(this);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Nokri_ToastManager.showLongToast(getContext(),"This Feature Requires Permission");
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                latitudeEditText.setText(LATITUDE+"");
                longitudeEditText.setText(LONGITUDE+"");
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getContext(), Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    autoCompleteTextView.setText(address);
                    setMapLocation();

                } catch (IOException e) {
                    Nokri_ToastManager.showLongToast(getContext(),"Something Went Wrong");
                }
                catch (Exception ex){
                    Nokri_ToastManager.showLongToast(getContext(),"Something Went Wrong");
                }



                return false;
            }
        });

    }
    private void setMapLocation(){
        if(googleMap!=null)
        {   googleMap.clear();
            LatLng location = new LatLng(LATITUDE, LONGITUDE);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.addMarker(new MarkerOptions().position(location));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location).zoom(Nokri_Config.MAP_CAM_MIN_ZOOM).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                    2000, null);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
        isPaused = false;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        map.onLowMemory();
    }

    private void nokri_getLocationAndAddress(){
        dialogManager = new Nokri_DialogManager();
        dialogManager.showAlertDialog(getActivity());
        RestService restService =  Nokri_ServiceGenerator.createService(RestService.class, Nokri_SharedPrefManager.getEmail(getContext()), Nokri_SharedPrefManager.getPassword(getContext()),getContext());

        Call<ResponseBody> myCall;
        if(Nokri_SharedPrefManager.isSocialLogin(getContext())) {
            myCall = restService.getEmployeerLocation(Nokri_RequestHeaderManager.addSocialHeaders());
        } else

        {
            myCall = restService.getEmployeerLocation( Nokri_RequestHeaderManager.addHeaders());
        }
        //Call<ResponseBody> myCall = restService.getCandidateLocation(Nokri_RequestHeaderManager.addHeaders());
        myCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObject) {
                if(responseObject.isSuccessful())
                {
                    try {
                        JSONObject response = new JSONObject(responseObject.body().string());
                        if(response.getBoolean("success")){
                            JSONArray dataArray = response.getJSONArray("data");
                            JSONArray extras = response.getJSONArray("extras");

                            for(int i =0;i<extras.length();i++){
                                JSONObject extra = extras.getJSONObject(i);
                                if(extra.getString("field_type_name").equals("save"))
                                    saveLocatiosButton.setText(extra.getString("value"));
                            }


                            for(int i=0;i<dataArray.length();i++) {

                                JSONObject data = dataArray.getJSONObject(i);

                                Log.d("Location",data.toString());
                                if (data.getString("field_type_name").equals("emp_lat")) {
                                    latitudeTextView.setText(data.getString("key"));
                                    latitudeEditText.setText(data.getString("value"));
                                    latitudeEditText.setHint(data.getString("key"));
                                    try {
                                        LATITUDE = Double.parseDouble(data.getString("value"));

                                    }
                                    catch (NumberFormatException e){
                                        LATITUDE = 0;
                                    }
                                } else if (data.getString("field_type_name").equals("emp_long")) {
                                    longitudeTextView.setText(data.getString("key"));
                                    longitudeEditText.setText(data.getString("value"));
                                    longitudeEditText.setHint(data.getString("key"));

                                    try {
                                        LONGITUDE = Double.parseDouble(data.getString("value"));
                                    }
                                    catch (NumberFormatException e){
                                        LONGITUDE = 0;
                                    }
                                } else if (data.getString("field_type_name").equals("emp_loc")) {
                                    setLocationTextView.setText(data.getString("key"));
                                    autoCompleteTextView.setText(data.getString("value"));
                                    autoCompleteTextView.setHint(data.getString("key"));

                                }

                            }

                            dialogManager.hideAlertDialog();
                            setMapLocation();}

                        else
                        {
                            dialogManager.showCustom(response.getString("message"));
                            dialogManager.hideAfterDelay();
                        }
                    } catch (JSONException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    } catch (IOException e) {
                        dialogManager.showCustom(e.getMessage());
                        dialogManager.hideAfterDelay();
                        e.printStackTrace();
                    }
                }
                else{
                    dialogManager.showCustom(responseObject.message());
                    dialogManager.hideAfterDelay();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Nokri_ToastManager.showLongToast(getContext(),t.getMessage());
                dialogManager.hideAfterDelay();
            }
        });
    }


    @Override
    public void onMyLocationChange(Location location) {

        if(location!=null){
            LATITUDE = location.getLatitude();
            LONGITUDE = location.getLongitude();

        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        nokri_manageAutoComplete(s.toString());

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String placeId = ids.get(position);
        List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);


        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i("Places", "Place found: " + place.getLatLng().latitude + " "+place.getLatLng().longitude);
            LATITUDE = place.getLatLng().latitude;
            LONGITUDE = place.getLatLng().longitude;
            latitudeEditText.setText(place.getLatLng().latitude+"");
            longitudeEditText.setText(place.getLatLng().longitude+"");
            setMapLocation();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();

                Log.e("Places", "Place not found: " + exception.getMessage());
            }
        });
    }
    private void nokri_manageAutoComplete(String query){
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build();


        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {

            ids.clear();
            places.clear();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                places.add(prediction.getFullText(null).toString());
                ids.add(prediction.getPlaceId());
                Log.i("Places", prediction.getPlaceId());
                Log.i("Places", prediction.getFullText(null).toString());
            }
            String[] data = places.toArray(new String[places.size()]);  // terms is a List<String>

            ArrayAdapter<?> adapter = new ArrayAdapter<Object>(getContext(), android.R.layout.simple_dropdown_item_1line, data);
            autoCompleteTextView.setAdapter(adapter);

            adapter.notifyDataSetChanged();
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("Places", "Place not found: " + apiException.getStatusCode());
            }
        });


    }

}
