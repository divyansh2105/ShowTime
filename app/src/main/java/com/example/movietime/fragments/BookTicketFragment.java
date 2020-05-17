package com.example.movietime.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.ApplicationClass;
import com.example.movietime.BookingObject;
import com.example.movietime.CinemaObject;
import com.example.movietime.ContactObject;
import com.example.movietime.FavoritesObject;
import com.example.movietime.MovieObject;
import com.example.movietime.R;
import com.example.movietime.adapters.CinemasMovieDetailsAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BookTicketFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BookTicketFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookTicketFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MapView mapView;
    MovieObject movieObject;
    List<LatLng> cinema_latlong_list_final;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private static final String ICON_ID = "ICON_ID";

    RecyclerView cinema_address_recyclerview;;

    String movieId;
    int flag=0;

    View root;

    DisplayMetrics displayMetrics;

    Double current_lat;
    Double current_long;
    MovieObject mO;
    Context mycontext;

    DatabaseReference databaseReference;
    List<BookingObject> bookingObjects;
    List<ContactObject> contactObjects;
    TextView no_of_friends_watching_tv;
    List<HashSet<String>> no_of_friends_watching;

    Cursor cursor;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;


    private OnFragmentInteractionListener mListener;




    public BookTicketFragment() {
        // Required empty public constructor
    }

    public BookTicketFragment(MovieObject mO)
    {
        this.mO=mO;
        movieId=mO.getId();
    }

    Bundle bundle;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BookTicketFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BookTicketFragment newInstance(String param1, String param2) {
        BookTicketFragment fragment = new BookTicketFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        bundle=savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        String mapbox_token=getString(R.string.mapbox_access_token);
        Context context=getContext();
        assert context != null;
        Mapbox.getInstance(context,mapbox_token );

        root= inflater.inflate(R.layout.fragment_book_ticket, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if(savedInstanceState!=null) {
            mO = (MovieObject) savedInstanceState.getSerializable("movieObjectSaved");
            movieId=savedInstanceState.getString("movieId");
        }

        contactObjects=new ArrayList<>();
        contactObjects=((ApplicationClass) requireActivity().getApplication()).getContactsListFromCursor();

        cinema_address_recyclerview =root.findViewById(R.id.cinema_adress_recyclerview);
        cinema_address_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");
        retrieveDatabase();

        getLastLocation();

        Log.d("current_lat_long4", String.valueOf(current_lat)+"  "+String.valueOf(current_long));

        return root;
    }

    public void retrieveDatabase()
    {
        bookingObjects=new ArrayList<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bookingObjects.clear();

                for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                    BookingObject bookingObject = iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }

                Log.d("retrieved_database", String.valueOf(bookingObjects));

//                ContactObject mycontactObject=new ContactObject();

                        //GetContactsIntoArrayList();

                String newPhone;
                for(int i=0;i<contactObjects.size();i++)
                {
                    newPhone=contactObjects.get(i).getContactPhoneNo();
                    String newphone2 = "";
                    for(int j=0;j<newPhone.length();j++)
                    {
                        if(newPhone.charAt(j)>=48 && newPhone.charAt(j)<=57)
                        {
                            newphone2+=newPhone.charAt(j);
                        }
                        else
                        {
                            newphone2+="";
                        }
                    }
                    if(newphone2.length()>10)
                    {
                        newphone2=newphone2.substring(newphone2.length()-10);
                    }
                    contactObjects.get(i).setContactPhoneNo(newphone2);

                }

                no_of_friends_watching=new ArrayList<HashSet<String>>();
                for(int i=0;i<10;i++)
                {
                    HashSet<String> hashSet= new HashSet<String>();
                    no_of_friends_watching.add(hashSet);
                }
                for(int i=0;i<bookingObjects.size();i++)
                {
                    for(int j=0;j<contactObjects.size();j++)
                    {
                        if(movieId.equals(bookingObjects.get(i).getMovieId())
                                && bookingObjects.get(i).getPhone().equals(contactObjects.get(j).getContactPhoneNo()))
                        {
                            int cinema_id= Integer.parseInt(bookingObjects.get(i).getCinemaId());
                            no_of_friends_watching.get(cinema_id).add(contactObjects.get(j).getContactName());
//                            names.add(contactObjects.get(j).getContactName());
                            break;
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }



    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
//                                    latTextView.setText(location.getLatitude()+"");
//                                    lonTextView.setText(location.getLongitude()+"");
                                        current_lat=location.getLatitude();
                                        current_long=location.getLongitude();
                                        getCurrentLatLong(current_lat,current_long);
                                    Log.d("current_lat_long1", String.valueOf(current_lat)+"  "+String.valueOf(current_long));
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(getContext(), "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    void getCurrentLatLong(Double lat, Double lng)
    {
        if(flag==0)
        {
            flag=1;
        }
        else
        {
            return;
        }

        movieObject=new MovieObject();
        movieObject.setId(movieId);
        List<Integer> movie_index_list=movieObject.getCinemasfromId(movieId);
        Collections.sort(movie_index_list);

        CinemaObject cinemaObject=new CinemaObject();
        List<LatLng> cinema_latlong_list=cinemaObject.getLatLngList();
        cinema_latlong_list_final=new ArrayList<>();
        int k=0;
        Log.d("movie_index_list", String.valueOf(movie_index_list));
        while(k<movie_index_list.size())
        {
                LatLng x=cinema_latlong_list.get(movie_index_list.get(k));
                cinema_latlong_list_final.add(x);
                k++;

        }

        mapView = (MapView) root.findViewById(R.id.mapView);
        if(mycontext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            dpHeight /= 2;

            final float scale = mycontext.getResources().getDisplayMetrics().density;
            int pixels_height = (int) (dpHeight * scale + 0.5f);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pixels_height);
            mapView.setLayoutParams(layoutParams);
        }

        if(mycontext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
            dpWidth /= 2;

            final float scale = mycontext.getResources().getDisplayMetrics().density;
            int pixels_width = (int) (dpWidth * scale + 0.5f);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pixels_width, LinearLayout.LayoutParams.MATCH_PARENT);
            mapView.setLayoutParams(layoutParams);
        }
        mapView.onCreate(bundle);

        mapView.getMapAsync(this);

        mapView.onStart();

        mapView.onResume();

        List<Double> lats=new ArrayList<>();
        List<Double> longs=new ArrayList<>();

        for(int i=0;i<cinema_latlong_list_final.size();i++)
        {
            lats.add(cinema_latlong_list_final.get(i).latitude);
            longs.add(cinema_latlong_list_final.get(i).longitude);
        }

        flag=0;
        CinemasMovieDetailsAdapter cinemasMovieDetailsAdapter= new CinemasMovieDetailsAdapter(getActivity(),mO,current_lat,current_long,lats,longs,no_of_friends_watching);
        cinema_address_recyclerview.setAdapter(cinemasMovieDetailsAdapter);


        for(int i=0;i<cinema_latlong_list_final.size();i++)
        {
            Log.d("cinemaOnjectlatlonglist", String.valueOf(cinema_latlong_list_final.get(i)));

        }


    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
//            latTextView.setText(mLastLocation.getLatitude()+"");
//            lonTextView.setText(mLastLocation.getLongitude()+"");
            current_lat=mLastLocation.getLatitude();
            current_long=mLastLocation.getLongitude();
            Log.d("current_lat_long2", String.valueOf(current_lat)+"  "+String.valueOf(current_long));
        }
    };

    private boolean checkPermissions() {
        if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
//        ActivityCompat.requestPermissions(
//                getActivity(),
//                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
//                PERMISSION_ID
//        );
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {


        CinemaObject cinemaObject=new CinemaObject();
        final List<LatLng> mylist=cinemaObject.getLatLngList();
        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();

        Double washingtonLat=38.9072;
        Double washingtonLong=77.0369;

        List<Feature> symbolLayerIconFeatureList2 = new ArrayList<>();
        symbolLayerIconFeatureList2.add(Feature.fromGeometry(
                Point.fromLngLat(current_long,current_lat)));

        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(washingtonLong,washingtonLat)));

        for(int i=0;i<cinema_latlong_list_final.size();i++)
        {
            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(cinema_latlong_list_final.get(i).longitude,cinema_latlong_list_final.get(i).latitude)));
        }

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")

                .withImage(ICON_ID, BitmapFactory.decodeResource(
                        mycontext.getResources(), R.drawable.location_yellow))
                .withImage("icon2", BitmapFactory.decodeResource(
                        mycontext.getResources(), R.drawable.location_red))
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                .withSource(new GeoJsonSource("source2",
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList2)))

                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)

                        .withProperties(PropertyFactory.iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconOffset(new Float[] {0f, -9f}))
                )
                .withLayer(new SymbolLayer("layer2", "source2")
                        .withProperties(PropertyFactory.iconImage("icon2"),
                                iconAllowOverlap(true),
                                iconOffset(new Float[] {0f, -9f}))
                )
                        , new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {


            }
        });



    }





    @Override
    public void onStart() {
        super.onStart();
        if(mapView!=null)
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mapView!=null) {
            mapView.onResume();
        }
        if (checkPermissions()) {
            getLastLocation();
            Log.d("current_lat_long3", String.valueOf(current_lat)+"  "+String.valueOf(current_long));
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mapView!=null) {
            mapView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mapView!=null)
        {
            mapView.onStop();
        }
    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mapView!=null) {
            mapView.onDestroy();
        }
    }


    //    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        BookTicketFragment bookTicketFragment=new BookTicketFragment(mO);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.movie_detail_host, bookTicketFragment);
        transaction.commit();
//
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
         displayMetrics = getContext().getResources().getDisplayMetrics();
         mycontext=context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mapView!=null) {
            mapView.onSaveInstanceState(outState);
        }
        outState.putSerializable("movieObjectSaved",mO);
        outState.putString("movieId",movieId);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            mO = (MovieObject) savedInstanceState.getSerializable("movieObjectSaved");
            movieId=savedInstanceState.getString("movieId");
        }
    }



}
