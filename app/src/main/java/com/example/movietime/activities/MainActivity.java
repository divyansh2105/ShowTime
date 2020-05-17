package com.example.movietime.activities;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.movietime.ApplicationClass;
import com.example.movietime.ContactObject;
import com.example.movietime.CreateAccountObject;
import com.example.movietime.R;
import com.example.movietime.fragments.GoogleSignInFragment;
import com.example.movietime.ui.favorites.FavoritesFragment;
import com.example.movietime.ui.home.HomeFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
                                                                                 AppBarLayout.OnOffsetChangedListener,
                                                                                 GoogleApiClient.OnConnectionFailedListener,
                                                                                 GoogleSignInFragment.SignInInterface,
                                                                                 HomeFragment.HomeInterface,
                                                                                 FavoritesFragment.FavoritesInteface {

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout drawer;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    TextView title_tv;
    int contact_flag=0;
    String launch;

    List<ContactObject> contactObjects;

    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;
    String loggedInEmailId;

    ArrayList<ContactObject> StoreContacts ;

    Cursor cursor ;
    String name, phonenumber ;
    public  final static int RequestPermissionCode =1;

    String title_collapse_text;

    NavigationView navigationView;
    List<JSONObject> jsonObjects2;

    CreateAccountObject createAccountObject;
    DatabaseReference createAccountDatabase;
    List<CreateAccountObject> createAccountObjects;
    boolean firstSignIn=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        launch="";
        title_collapse_text="Home";

        EnableRuntimePermission();


//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
         drawer = findViewById(R.id.drawer_layout);
         navigationView = findViewById(R.id.nav_view);

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar_layout);

        title_tv=findViewById(R.id.title_tv);

        appBarLayout=findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -collapsingToolbarLayout.getHeight() + toolbar.getHeight()) {
//                    toolbar.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(200, 100));
                    title_tv.setText(title_collapse_text);
//                    title_tv.setGravity(Gravity.TOP);
//                    Toast.makeText(getApplicationContext(),"collapsed",Toast.LENGTH_SHORT).show();
                }else //if(!toolbar.getTitle().equals(mExpandedTitle))
                     {
                         title_tv.setText("ShowTime");
//                         title_tv.setGravity(Gravity.BOTTOM);
//                         Toast.makeText(getApplicationContext(),"expanded",Toast.LENGTH_SHORT).show();
                }

            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration
//                .Builder(
//                R.id.nav_home, R.id.nav_favorites, R.id.nav_booked_movies,
//                R.id.nav_update, R.id.nav_cancel, R.id.nav_details)
//                .setDrawerLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);

        setDrawerEnabled(false);

        checkConnectivity(this);

        Bundle extras=getIntent().getExtras();
        if(extras!=null)
        {
            launch=extras.getString("launch");

            if(launch!=null && launch.equals("home"))
            {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                HomeFragment homeFragment=new HomeFragment(loggedInEmailId,false);
                transaction.replace(R.id.nav_host_fragment, homeFragment);
                transaction.commit();

            }
            else if(launch!=null && launch.equals("favorites"))
            {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                String myemail=extras.getString("myemail");
                loggedInEmailId=myemail;
                String s=extras.getString("jsonObjectsString");
                List<JSONObject> jsonObjects3 = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObjects3.add(jsonArray.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                FavoritesFragment favoritesFragment=new FavoritesFragment(myemail, jsonObjects3);
                transaction.replace(R.id.nav_host_fragment, favoritesFragment);
                transaction.commit();
                return;
            }

        }

        createAccountDatabase= FirebaseDatabase.getInstance().getReference("CreateAccount");




        if(!isSignedIn())
        {

            gotoGoogleSignInFragment();
        }
        else {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

        }

        contactObjects=((ApplicationClass) this.getApplication()).getContactsListFromCursor();



    }


    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(isSignedIn() && launch!=null && !launch.equals("favorites")) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
            if (opr.isDone()) {
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account=result.getSignInAccount();
            Log.d("google_email_id",account.getEmail());
            loggedInEmailId=account.getEmail();
            ((ApplicationClass) this.getApplication()).setApplicationEmail(loggedInEmailId);
//            userName.setText(account.getDisplayName());
//            userEmail.setText(account.getEmail());
//            userId.setText(account.getId());

            retrieveAccountDatabase();

//            gotoHomeFragment();

        }else{
            gotoGoogleSignInFragment();
        }
    }


    void gotoGoogleSignInFragment()
    {

        GoogleSignInFragment googleSignInFragment=new GoogleSignInFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, googleSignInFragment);
        transaction.commit();
    }

    void gotoHomeFragment()
    {


        HomeFragment homeFragment=new HomeFragment(loggedInEmailId,firstSignIn);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, homeFragment);
        transaction.commitAllowingStateLoss();
    }



    public void retrieveAccountDatabase()
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


                for(int i=0;i<createAccountObjects.size();i++)
                {
                    if(((ApplicationClass)getApplication()).getApplicationEmail().equals(createAccountObjects.get(i).getEmail()))
                    {
                        firstSignIn=false;
                        ((ApplicationClass)getApplication()).setApplicationPhone(createAccountObjects.get(i).getPhone());
                        break;
                    }
                }
                gotoHomeFragment();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public void onPause() {
        super.onPause();
        if(googleApiClient!=null && googleApiClient.isConnected()) {
            googleApiClient.stopAutoManage(this);
            googleApiClient.disconnect();
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(googleApiClient!=null && googleApiClient.isConnected()) {
//            googleApiClient.stopAutoManage(this);
//            googleApiClient.disconnect();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setDrawerEnabled(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
//        toggle.setDrawerIndicatorEnabled(enabled);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Log.d("listener","listening");

        switch (item.getItemId()) {

            case R.id.nav_home:  {
                navigationView.getMenu().getItem(0).setChecked(true);
                HomeFragment homeFragment=new HomeFragment(loggedInEmailId,false);
                transaction.replace(R.id.nav_host_fragment, homeFragment);
                break;
            }

            case R.id.nav_favorites:    {
                navigationView.getMenu().getItem(1).setChecked(true);
                FavoritesFragment favoritesFragment=new FavoritesFragment(loggedInEmailId, jsonObjects2);
                transaction.replace(R.id.nav_host_fragment, favoritesFragment);
                Log.d("favorites clicked","nav_bar_fav");
                break;
            }

            case R.id.nav_signout:{
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if(googleApiClient!=null) {
                                    googleApiClient.stopAutoManage(MainActivity.this);
                                    googleApiClient.disconnect();
                                }
                                if (status.isSuccess()){
                                    gotoGoogleSignInFragment();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Session not close",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                break;
            }

            case R.id.nav_details:  {
//                BookingDetailsFragment bookingDetailsFragment=new BookingDetailsFragment(loggedInEmailId,jsonObjects2);
//                transaction.replace(R.id.nav_host_fragment, bookingDetailsFragment);
//                Log.d("favorites clicked","nav_bar_fav");
                    Intent intent=new Intent(this,BookingDetailsActivity.class);
                    intent.putExtra("jsonObjects", String.valueOf(jsonObjects2));
                    intent.putExtra("email",loggedInEmailId);
                    startActivity(intent);

                break;
            }

            case R.id.nav_cancel:   {
                Intent intent=new Intent(this, BookingDeleteActivity.class);
                intent.putExtra("jsonObjects", String.valueOf(jsonObjects2));
                intent.putExtra("email",loggedInEmailId);
                startActivity(intent);
                break;
            }


        }
        //close navigation drawer

        drawer.closeDrawer(GravityCompat.START);
        transaction.commit();

        return true;
    }




    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                MainActivity.this,
                Manifest.permission.READ_CONTACTS))
        {

            //Toast.makeText(MainActivity.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    if(contactObjects==null) {
                        contactObjects=GetContactsIntoArrayList();
                        ((ApplicationClass) this.getApplication()).setContactsListFromCursor(contactObjects);
                    }
                    if(launch!=null && launch.equals("")) {
                        Toast.makeText(MainActivity.this, "Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_SHORT).show();
                    }
//                    StoreContacts = new ArrayList<ContactObject>();
                    //StoreContacts = (ArrayList<ContactObject>) GetContactsIntoArrayList();
                    Log.d("contact_list", String.valueOf(StoreContacts));

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();
                    Log.d("finished","finished gracefully");

                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + RC);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager fragManager = this.getSupportFragmentManager();
        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if(count>0) {
            Fragment frag = fragManager.getFragments().get(count > 0 ? count - 1 : count);
            Log.d("fragid", String.valueOf(frag.getId()));
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void homeInterfaceSetText(String s, List<JSONObject> jsonObjects) {
        setDrawerEnabled(true);
        title_collapse_text=s;
        jsonObjects2=jsonObjects;
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void favoritesInterfaceSetText(String s) {
        setDrawerEnabled(true);
        title_collapse_text=s;
    }

    public List<ContactObject> GetContactsIntoArrayList(){

        List<ContactObject> contactList=new ArrayList<>();
        cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            String phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            ContactObject contactObject=new ContactObject();
            contactObject.setContactName(name);

            String newPhone=phonenumber;
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


            contactObject.setContactPhoneNo(newphone2);

            contactList.add(contactObject);
        }

        cursor.close();

        return contactList;


    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void NoInternet(final Context context)
    {
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Info")
                    .setMessage(R.string.main_no_network)
                    .setNegativeButton(R.string.main_no_network_try_again, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            checkConnectivity(context);
                        }
                    })
                    .setPositiveButton(R.string.main_no_network_close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((Activity)context).finish();
                        }
                    });
            builder.create().show();
        } catch (Exception e) {
        }
    }

    public void checkConnectivity(Context context)
    {
        if(!isOnline())
        {
            NoInternet(context);
        }
        else {
            //new FetchInfo().execute(BASE_URL);
        }
    }

    @Override
    public void signInInterfaceSetText() {
        setDrawerEnabled(false);
    }
}


