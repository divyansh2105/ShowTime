package com.example.movietime.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.AppUtility;
import com.example.movietime.ApplicationClass;
import com.example.movietime.CreateAccountObject;
import com.example.movietime.R;
import com.example.movietime.adapters.UpcomingMoviesAdapter;
import com.example.movietime.fragments.BookTicketFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    RecyclerView recyclerView;

    List<JSONObject> jsonObjects;
    String loggedInEmailId;
    boolean firstSignIn;

    CreateAccountObject createAccountObject;
    DatabaseReference createAccountDatabase;
    List<CreateAccountObject> createAccountObjects;
    String phone_edittext_string;
    EditText phone_edittext;
    TextView dialog_email_tv;


    public HomeFragment()
    {}


    public HomeFragment(String loggedInEmailId, boolean firstSignIn) {
        this.loggedInEmailId = loggedInEmailId;
        this.firstSignIn=firstSignIn;
    }

    Uri builtUri;

    public interface HomeInterface
    {
        public void homeInterfaceSetText(String s, List<JSONObject> jsonObjects);
    }

    HomeInterface homeInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            homeInterface = (HomeInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        GridLayoutManager gridLayoutManager = null;
        recyclerView=root.findViewById(R.id.recycler_view_homefragment);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager= new GridLayoutManager(root.getContext(), 2, GridLayoutManager.VERTICAL, false);
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(root.getContext(), 3, GridLayoutManager.VERTICAL, false);
        }
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setHasFixedSize(true);

        createAccountObject=new CreateAccountObject();
        createAccountDatabase= FirebaseDatabase.getInstance().getReference("CreateAccount");

        if(firstSignIn)
        {
            showDialog();
        }
        else
        {
            new FetchInfo().execute();
        }



        return root;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HomeFragment homeFragment=new HomeFragment(loggedInEmailId,false);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, homeFragment);
        transaction.commit();
//
    }

    public void showDialog()
    {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.create_account_dialog, null);

        phone_edittext = alertLayout.findViewById(R.id.phone_tv_dialog);

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        dialog_email_tv=alertLayout.findViewById(R.id.dialog_email_tv);
        dialog_email_tv.setText(((ApplicationClass)getActivity().getApplication()).getApplicationEmail());

        Button ok_button=alertLayout.findViewById(R.id.ok_button);
        Button cancel_button=alertLayout.findViewById(R.id.cancel_button);

        final AlertDialog dialog = alert.create();
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phone_edittext_string = phone_edittext.getText().toString();
                if((!android.util.Patterns.PHONE.matcher(phone_edittext_string).matches()) || phone_edittext_string.equals("") )
                {
                    phone_edittext.setHint("Enter Valid Number");
                    phone_edittext.setText("");
                    return;
                }
                if(phone_edittext_string.length()<10)
                {
                    phone_edittext.setHint("Minimum 10 digits");
                    phone_edittext.setText("");
                    return;
                }
                String newPhone=phone_edittext_string;
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
                    phone_edittext_string=newphone2;


                createAccount(dialog);

            }
        });

//        cancel_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.cancel();
//
//            }
//        });


        dialog.show();
    }

    public void createAccount(final AlertDialog dialog)
    {

        createAccountDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                createAccountObjects=new ArrayList<>();
                createAccountObjects.clear();

                for(DataSnapshot iterator: dataSnapshot.getChildren())
                {
                    CreateAccountObject createAccountObject2=iterator.getValue(CreateAccountObject.class);
                    createAccountObjects.add(createAccountObject2);
                }

                int flag=0;
                for(int i=0;i<createAccountObjects.size();i++)
                {
                    if(phone_edittext_string.equals(createAccountObjects.get(i).getPhone()))
                    {
                        flag=1;
                        break;
                    }
                }
                if(flag==1) {
                    Toast.makeText(getContext(), "Phone Number linked with another Email id", Toast.LENGTH_SHORT).show();
                }
                else {

                    dialog.cancel();
                    String id=phone_edittext_string;

                    createAccountObject.setEmail(((ApplicationClass) getActivity().getApplication()).getApplicationEmail());
                    createAccountObject.setPhone(phone_edittext_string);

                    ((ApplicationClass)getActivity().getApplication()).setApplicationPhone(phone_edittext_string);

                    createAccountDatabase.child(id).setValue(createAccountObject);

                    Toast.makeText(getContext(),"Account Created",Toast.LENGTH_SHORT).show();

                    new FetchInfo().execute();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    public class FetchInfo extends AsyncTask<String,Void, List<JSONObject>>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected List<JSONObject> doInBackground(String... strings) {

            String currentDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Log.d("currentDate",currentDateString);

            int elements=14;

            String response_string="";
            JSONArray jsonArray1;
            JSONObject jsonObject1;
            jsonArray1 = new JSONArray();
            List<JSONObject> jsonObjects = new ArrayList<>();
            int it=1;
            int pageNo=20;
            int count=0;

            do {

                if(count>elements && (count%2==0))
                {
                    break;
                }
                response_string = (String) getHttpResponse(it);
                Log.d("home_upcoming_url", response_string);


                try {
                    jsonObject1 = new JSONObject(response_string);
                    jsonArray1 = jsonObject1.getJSONArray("results");
                    pageNo= Integer.parseInt(jsonObject1.getString("total_pages"));
//                Log.d("homefragment_jsonOb", String.valueOf(jsonObject1));
//                Log.d("homefragment_jsonAr", String.valueOf(jsonArray1.getJSONObject(0)));

                    AppUtility.setSt("hello world");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                for(int i=0;i<jsonArray1.length();i++)
                {
                    if(count>elements && count%2==0)
                    {
                        break;
                    }
                    String responseDate;
                    try {
                        responseDate=jsonArray1.getJSONObject(i).getString("release_date");
                        DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                        Date d1 = f.parse(currentDateString, new ParsePosition(0));
                        Date d2 = f.parse(responseDate, new ParsePosition(0));

                        if(d2.compareTo(d1)>0 && !jsonArray1.getJSONObject(i).getString("poster_path").equals("null")
                                && !jsonArray1.getJSONObject(i).getString("backdrop_path").equals("null")){
                            jsonObjects.add(jsonArray1.getJSONObject(i));
                            count++;
                            Log.d("responseDate",responseDate);
                            Log.d("reponseDate_title",jsonArray1.getJSONObject(i).getString("title"));
                            Log.d("responseDate_ob",jsonArray1.getJSONObject(i).getString("poster_path"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                it++;

            }while (it<=pageNo || count%2==1);



            return jsonObjects;


        }

        @Override
        protected void onPostExecute(List<JSONObject> jsonObjects) {
            super.onPostExecute(jsonObjects);

            Log.d("jsonOnject", String.valueOf(jsonObjects));

            homeInterface.homeInterfaceSetText("Home",jsonObjects);

            UpcomingMoviesAdapter upcomingMoviesAdapter=new UpcomingMoviesAdapter(jsonObjects,loggedInEmailId);
            recyclerView.setAdapter(upcomingMoviesAdapter);


        }
    }

    public Object getHttpResponse(int it) {
        OkHttpClient httpClient = new OkHttpClient();

        builtUri = Uri.parse(AppUtility.getBaseUrlPopular()).buildUpon()
                .appendQueryParameter("api_key", AppUtility.getApiKey())
                .appendQueryParameter("page",String.valueOf(it))
                .build();


        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d("url"+String.valueOf(it), String.valueOf(url));

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
    public void onDestroyView() {
        super.onDestroyView();
    }

}