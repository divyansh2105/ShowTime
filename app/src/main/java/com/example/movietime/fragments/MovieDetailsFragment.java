package com.example.movietime.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.AppUtility;
import com.example.movietime.ApplicationClass;
import com.example.movietime.BookingObject;
import com.example.movietime.CinemaObject;
import com.example.movietime.ContactObject;
import com.example.movietime.FavoritesObject;
import com.example.movietime.MovieObject;
import com.example.movietime.R;
import com.example.movietime.adapters.FriendsWatchingAdapter;
import com.example.movietime.adapters.RecommendedMoviesAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link MovieDetailsFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link MovieDetailsFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class MovieDetailsFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;


    ImageView back_image, poster_image;
    TextView title_tv, overview_tv, release_dat_tv;
    String loggedInEmailId;

    RecyclerView recommended_recyclerview, friends_watching_recyclerview; //cinema_address_recyclerview;
    Uri builtUri;

    DatabaseReference databaseBookings;
    Cursor cursor ;
    List<ContactObject> contactObjects;

    List<FavoritesObject> favoritesObjects;
    ImageButton imageButton;
    boolean isFavorite;
    FavoritesObject favoritesObject;
    DatabaseReference databaseFavorite;
    FirebaseDatabase database;
    DatabaseReference favoriteRemoved;
    boolean favoriteDeleted=false;
    boolean favoriteAdded=false;

//    private OnFragmentInteractionListener mListener;

    MovieObject movieObject;
    CinemaObject cinemaObject;
    Button book_now_button;
    List<String> names;
    View root;

    List<BookingObject> bookingObjects;
    String phone,email;
    int white_color, gray_color;


    public MovieDetailsFragment(){
    }

    public MovieDetailsFragment(MovieObject movieObject, String loggedInEmailId) {
        this.movieObject=movieObject;
        this.loggedInEmailId=loggedInEmailId;
        favoritesObject=new FavoritesObject();
        favoritesObject.setMovieObject(movieObject);
        favoritesObject.setEmail(loggedInEmailId);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     //* @param param1 Parameter 1.
     //* @param param2 Parameter 2.
     * @return A new instance of fragment MovieDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static MovieDetailsFragment newInstance(String param1, String param2) {
//        MovieDetailsFragment fragment = new MovieDetailsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_movie_details, container, false);

        poster_image=root.findViewById(R.id.poster_image);
        back_image=root.findViewById(R.id.back_image);
        title_tv=root.findViewById(R.id.movie_detail_title);

        release_dat_tv=root.findViewById(R.id.release_date);

        recommended_recyclerview=root.findViewById(R.id.recommended_recyclerview);
        friends_watching_recyclerview=root.findViewById(R.id.friends_watching_recyclerview);
//        cinema_address_recyclerview =root.findViewById(R.id.cinema_adress_recyclerview);

        book_now_button=root.findViewById(R.id.book_now_button);

        if(savedInstanceState!=null) {
            movieObject = (MovieObject) savedInstanceState.getSerializable("movieObjectSaved");
            favoritesObject = (FavoritesObject) savedInstanceState.getSerializable("favoritesObjectSaved");
        }

        Picasso.get().load(AppUtility.getBaseImageUrl()+movieObject.getPoster_url()).into(poster_image);

        Picasso.get().load(AppUtility.getBaseImageUrl()+movieObject.getBack_image_url()).into(back_image);

        title_tv.setText(movieObject.getTitle());

        imageButton=root.findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFavorite==false)
                {
                    isFavorite=true;
                    imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.myWhite));

                     addDatabaseFavorites();

                }
                else
                {
                    isFavorite=false;
                    imageButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
                    removeDatabaseFavorites();
                }
            }
        });

        database=FirebaseDatabase.getInstance();


        databaseBookings= database.getReference("Bookings");
        retrieveDatabase();

        favoritesObjects=new ArrayList<>();
        databaseFavorite= database.getReference("Favorites");
        checkDatabaseFavorites();

        release_dat_tv.setText(movieObject.getRelease_date());

        recommended_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
//        cinema_address_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        friends_watching_recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        //retrive similar movies
        if(movieObject.getId()!=null) {
            new FetchInfo().execute(movieObject.getId());
        }
        else
        {
            getActivity().finish();
        }

        cinemaObject=new CinemaObject();
//        String[] ar=cinemaObject.getCinema_addresses();

        List<LatLng> lat_long_list_cinemas;
        lat_long_list_cinemas=cinemaObject.getLatLngList();


//        for(int i=0;i< ar.length;i++)
//        {
//            lat_long_list_cinemas.add(getLocationFromAddress(root.getContext(),ar[i]));
//            Log.d("lat_long"+i, String.valueOf(lat_long_list_cinemas.get(i)));
//        }



//        List<Integer> cinemas_index_list=movieObject.getCinemasfromId(movieObject.getId());

//        for(int i=0;i<cinemas_index_list.size();i++)
//        {
//            Log.d("cinema index "+movieObject.getId(), String.valueOf(cinemas_index_list.get(i)));
//        }

//        CinemasMovieDetailsAdapter cinemasMovieDetailsAdapter= new CinemasMovieDetailsAdapter(movieObject.getId());
//        cinema_address_recyclerview.setAdapter(cinemasMovieDetailsAdapter);
//        cinema_address_recyclerview.setNestedScrollingEnabled(false);


        String currentDateString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            currentDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }
        String responseDate=movieObject.getRelease_date();
        DateFormat f = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            f = new SimpleDateFormat("yyyy-MM-dd");
        }
        Date d1 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            d1 = f.parse(currentDateString, new ParsePosition(0));
        }
        Date d2 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            d2 = f.parse(responseDate, new ParsePosition(0));
        }
        if(d1.compareTo(d2)>0)
        {
            book_now_button.setVisibility(View.GONE);
        }
        else
        {
            book_now_button.setVisibility(View.VISIBLE);
            book_now_button.setOnClickListener(this);
        }




        return root;
    }


    public Object getHttpResponse(String idstr) {
        OkHttpClient httpClient = new OkHttpClient();

        Random r = new Random();
        int low = 0;
        int high = 100;
        int result = r.nextInt(high-low) + low;
        int id=Integer.parseInt(idstr);
        builtUri = Uri.parse(AppUtility.getBaseSimilarUrl()).buildUpon()
                .appendQueryParameter("api_key", AppUtility.getApiKey())
                .appendQueryParameter("id",String.valueOf(id))
                .appendQueryParameter("page", String.valueOf(result))
                .build();


        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d("url_id= "+String.valueOf(id), String.valueOf(url));

        Request request = new Request.Builder()
                .url(String.valueOf(url))
                .build();

        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e(TAG, "error in getting response get request okhttp");
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.book_now_button:
            {
//                Toast.makeText(v.getContext(),"clicked",Toast.LENGTH_SHORT).show();
                BookTicketFragment bookTicketFragment=new BookTicketFragment(movieObject);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction =  fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.movie_detail_host, bookTicketFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    public List<ContactObject> GetContactsIntoArrayList(){

        List<ContactObject> contactList=new ArrayList<>();
        cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            ContactObject contactObject=new ContactObject();
            contactObject.setContactName(name);
            contactObject.setContactPhoneNo(phonenumber);

            contactList.add(contactObject);
        }

        cursor.close();

        return contactList;

    }


    public class FetchInfo extends AsyncTask<String,Void, List<JSONObject>>
    {

        @Override
        protected List<JSONObject> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String id=params[0];

            String response_string;
            response_string= (String) getHttpResponse(id);
            Log.d("movie_detail_response",response_string);

            JSONObject jsonObject1;
            JSONArray jsonArray1 = null;
            List<JSONObject> jsonObjects=new ArrayList<>();

            try {
                jsonObject1=new JSONObject(response_string);
                jsonArray1 = jsonObject1.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            for(int i=0;i<jsonArray1.length();i++)
            {
                try {
                    if (!jsonArray1.getJSONObject(i).getString("backdrop_path").equals("null")) {
                        jsonObjects.add(jsonArray1.getJSONObject(i));
                    }
                }catch(JSONException e){
                        e.printStackTrace();
                    }

            }


            return jsonObjects;
        }

        @Override
        protected void onPostExecute(List<JSONObject> jsonObjects) {
            super.onPostExecute(jsonObjects);

            RecommendedMoviesAdapter recommendedMoviesAdapter=new RecommendedMoviesAdapter(jsonObjects);
            recommended_recyclerview.setAdapter(recommendedMoviesAdapter);

        }
    }

    public void retrieveDatabase()
    {
        bookingObjects=new ArrayList<>();
        databaseBookings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bookingObjects.clear();

                for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                    BookingObject bookingObject = iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }

                Log.d("retrieved_database", String.valueOf(bookingObjects));


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

                names=new ArrayList<>();
                for(int i=0;i<bookingObjects.size();i++)
                {
                    for(int j=0;j<contactObjects.size();j++)
                    {
                        if(movieObject.getId().equals(bookingObjects.get(i).getMovieId())
                                && bookingObjects.get(i).getPhone().equals(contactObjects.get(j).getContactPhoneNo()))
                        {
                            if(contactObjects.get(j).getContactPhoneNo().equals(phone))
                            {
                                names.add("YOU");
                            }
                            else {
                                names.add(contactObjects.get(j).getContactName());
                            }
                            break;
                        }
                    }
                }
                if(names.size()==0)
                {
                    TextView friend_watching_label=root.findViewById(R.id.friends_watching_label);
                    friend_watching_label.setVisibility(View.INVISIBLE);
                }
                Set<String> names_set =  new HashSet<String>(names);
                List<String> names_new = new ArrayList<String>(names_set);

                FriendsWatchingAdapter friendsWatchingAdapter=new FriendsWatchingAdapter(names_new);
                friends_watching_recyclerview.setAdapter(friendsWatchingAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void checkDatabaseFavorites()
    {
        if(favoriteAdded || favoriteDeleted)
        {
            return;
        }
        imageButton=root.findViewById(R.id.imageButton);

        databaseFavorite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(favoriteAdded || favoriteDeleted)
                {
                    return;
                }

                favoritesObjects.clear();

                for(DataSnapshot iterator: dataSnapshot.getChildren())
                {
                    FavoritesObject favoritesObject2=iterator.getValue(FavoritesObject.class);
                    //if(favoritesObject2.getEmailId().equals(loggedInEmailId))
                        favoritesObjects.add(favoritesObject2);

                }

                for(int i=0;i<favoritesObjects.size()-1;i++)
                {
                    String s=favoritesObjects.get(i).getEmail();
                    if(!s.equals(loggedInEmailId))
                    {
                        favoritesObjects.remove(i);
                    }
                }
                Log.d("retrieved_database", String.valueOf(favoritesObjects));


                for(int i=0;i<favoritesObjects.size();i++)
                {
                    MovieObject movieObject1,movieObject2;
                    movieObject1=favoritesObject.getMovieObject();
                    movieObject2=favoritesObjects.get(i).getMovieObject();
                    if(movieObject2!=null && (movieObject1.getId().equals(movieObject2.getId())))

                    {
                        isFavorite=true;
                        imageButton.setColorFilter(white_color);
                        return;
                    }
                }
                isFavorite=false;
                imageButton.setColorFilter(gray_color);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void addDatabaseFavorites()
    {


        favoritesObject.setDatabaseId(favoritesObject.getMovieObject().getId());

        favoritesObject.setEmail(email);

        databaseFavorite.child(favoritesObject.getMovieObject().getId()).setValue(favoritesObject);

        favoriteAdded=true;

        Toast.makeText(getContext(),"Added To Favorites",Toast.LENGTH_SHORT).show();
//
    }

    public void removeDatabaseFavorites()
    {
        favoriteRemoved=databaseFavorite.child(favoritesObject.getMovieObject().getId());
        favoriteRemoved.removeValue();
        favoriteDeleted=true;
    }

    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
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
        phone=((ApplicationClass)getActivity().getApplication()).getApplicationPhone();
        contactObjects=((ApplicationClass) getActivity().getApplication()).getContactsListFromCursor();
        email=((ApplicationClass) getActivity().getApplication()).getApplicationEmail();
        gray_color=ContextCompat.getColor(getContext(), R.color.colorPrimaryDark);
        white_color=ContextCompat.getColor(getContext(), R.color.myWhite);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("movieObjectSaved",movieObject);
        outState.putSerializable("favoritesObjectSaved",favoritesObject);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState!=null) {
            movieObject = (MovieObject) savedInstanceState.getSerializable("movieObjectSaved");
            favoritesObject = (FavoritesObject) savedInstanceState.getSerializable("favoritesObjectSaved");
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MovieDetailsFragment movieDetailsFragment=new MovieDetailsFragment(movieObject, loggedInEmailId);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.movie_detail_host, movieDetailsFragment);
        transaction.commit();
    }

}
