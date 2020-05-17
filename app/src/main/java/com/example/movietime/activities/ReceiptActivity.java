package com.example.movietime.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.movietime.AppUtility;
import com.example.movietime.ApplicationClass;
import com.example.movietime.BookingObject;
import com.example.movietime.CinemaObject;
import com.example.movietime.MovieObject;
import com.example.movietime.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ReceiptActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    DatabaseReference databaseBookings;

    BookingObject bookingObject;
    List<BookingObject> bookingObjects;


    String selected_date;
    String selected_time;
    MovieObject movieObject;
    Integer cinemaIndex;
    int[] seat_flag;
    ImageView poster_imageview;
    String phone_no, checkbox_global;

    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    static File pdfDir;

    TextView cinema_tv, show_date, show_time, movie_name, seats_tv, price_tv,phone_no_tv, bookingId_tv;
    CinemaObject cinemaObject;
    String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_receipt);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(" ");

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();


//                clcikImage();


//                if (!checkPermission()) {
//                    takeScreenshot();
//                } else {
//                    if (checkPermission()) {
//                        requestPermissionAndContinue();
//                    } else {
//                        takeScreenshot();
//                    }
//                }


//            }
//        });

        bookingObject=new BookingObject();

        Bundle extras=getIntent().getExtras();
        movieObject= (MovieObject) extras.getSerializable("movieObject_intent");
        selected_date=extras.getString("selected_date");
        selected_time=extras.getString("selected_interval");
        cinemaIndex=extras.getInt("cinemaIndex_intent");
        seat_flag=new int[18];
        seat_flag=extras.getIntArray("seat_flag");
        phone_no=extras.getString("phone_no");
        checkbox_global=extras.getString("checkbox_global");
        bookingId=extras.getString("booking_id");

//        Toast.makeText(this,checkbox_global,Toast.LENGTH_SHORT).show();

        poster_imageview=findViewById(R.id.poster_image);
        String url= AppUtility.getBaseImageUrl() +movieObject.getPoster_url();
        Log.d("picasso",url);
        Picasso.get().load(url).into(poster_imageview);

        price_tv=findViewById(R.id.price_tv);

        bookingId_tv=findViewById(R.id.bookingId_tv);

        phone_no_tv=findViewById(R.id.phone_no_tv);


        movie_name=findViewById(R.id.movie_name_tv);
        seats_tv=findViewById(R.id.seats_tv);


        int count=0;
        for(int i=0;i<18;i++)
        {
            if(seat_flag[i]==1)
            {
                if(count==0)
                {
                    seats_tv.append(String.valueOf(i+1));

                }
                else
                {
                    seats_tv.append(", "+String.valueOf(i+1));
                }
                count++;
            }
        }
        price_tv.setText(String.valueOf(count*150));

        movie_name.setText(movieObject.getTitle());
        cinemaObject=new CinemaObject();
        String[] ar=cinemaObject.getCinema_addresses();


        cinema_tv=findViewById(R.id.cinema_address_tv);
        cinema_tv.setText(ar[cinemaIndex]);

        show_date=findViewById(R.id.show_date_tv);
        show_time=findViewById(R.id.show_time_tv);

        show_date.setText(selected_date);
        show_time.setText(selected_time);

        phone_no_tv.append(phone_no);

        if(isSignedIn()) {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            googleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

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


        bookingObject.setPhone(phone_no);
        bookingObject.setMovieId(movieObject.getId());
        bookingObject.setCinemaId(String.valueOf(cinemaIndex));
        bookingObject.setDateOfShow(selected_date);
        bookingObject.setTimeOfShow(selected_time);
        bookingObject.setSeat(String.valueOf(seats_tv.getText()).substring(10));
        bookingObject.setPrice(String.valueOf(price_tv.getText()));
        bookingObject.setGlobal(checkbox_global);
        bookingObject.setMovieTitle(movieObject.getTitle());

        databaseBookings= FirebaseDatabase.getInstance().getReference("Bookings");

        bookingObjects=new ArrayList<>();
        retrieveDatabaseAndAdd();


    }

    public void retrieveDatabaseAndAdd()
    {
        databaseBookings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bookingObjects.clear();

                for(DataSnapshot iterator: dataSnapshot.getChildren())
                {
                    BookingObject bookingObject=iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }


                Log.d("retrieved_database", String.valueOf(bookingObjects));

                int flag=0;
                for(int i=0;i<bookingObjects.size();i++)
                {
                    if(bookingObject.getMovieId().equals(bookingObjects.get(i).getMovieId())
                            && bookingObject.getCinemaId().equals(bookingObjects.get(i).getCinemaId())
                            && bookingObject.getDateOfShow().equals(bookingObjects.get(i).getDateOfShow())
                            && bookingObject.getTimeOfShow().equals(bookingObjects.get(i).getTimeOfShow())
                            && bookingObject.getSeat().equals(bookingObjects.get(i).getSeat()))
                    {
                        flag=1;
                        break;
                    }
                }
                if(flag==0)
                {
                    String id=databaseBookings.push().getKey();

                    bookingObject.setDatabaseEntryId(id);
                    bookingObject.setEmail(((ApplicationClass)getApplication()).getApplicationEmail());

                    databaseBookings.child(id).setValue(bookingObject);
                    bookingId_tv.setText(id);
                    Toast.makeText(getApplicationContext(),"Booking Done",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(this) != null;
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account=result.getSignInAccount();
            Log.d("google_email_id_receipt",account.getEmail());
//            userName.setText(account.getDisplayName());
//            userEmail.setText(account.getEmail());
//            userId.setText(account.getId());
            bookingObject.setEmail(account.getEmail());
            bookingObject.setUsername(account.getDisplayName());

        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

//    private void takeScreenshot() {
//        Date now = new Date();
//        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//
//        try {
//            // image naming and path  to include sd card  appending name you choose for file
//            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
//
//            // create bitmap screen capture
//            View v1 = getWindow().getDecorView().getRootView();
//            v1.setDrawingCacheEnabled(true);
//            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
//            v1.setDrawingCacheEnabled(false);
//
//            File imageFile = new File(mPath);
//
//            FileOutputStream outputStream = new FileOutputStream(imageFile);
//            int quality = 100;
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
//            outputStream.flush();
//            outputStream.close();
//
//            openScreenshot(imageFile);
//        } catch (Throwable e) {
//            // Several error may come out with file handling or DOM
//            e.printStackTrace();
//        }
//    }

//    private void openScreenshot(File imageFile) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        Uri uri = Uri.fromFile(imageFile);
//        intent.setDataAndType(uri, "image/*");
//        startActivity(intent);
//    }








//    private boolean checkPermission() {
//
//        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                ;
//    }

//    private void requestPermissionAndContinue() {
//        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
//                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
//                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
//                alertBuilder.setCancelable(true);
//                alertBuilder.setTitle("permissions necessary");
//                alertBuilder.setMessage("Allow permissions");
//                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//                    public void onClick(DialogInterface dialog, int which) {
//                        ActivityCompat.requestPermissions(ReceiptActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
//                                , READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
//                    }
//                });
//                AlertDialog alert = alertBuilder.create();
//                alert.show();
//                Log.e("", "permission denied, show dialog");
//            } else {
//                ActivityCompat.requestPermissions(ReceiptActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
//                        READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
//            }
//        } else {
//            takeScreenshot();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//
//        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
//            if (permissions.length > 0 && grantResults.length > 0) {
//
//                boolean flag = true;
//                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                        flag = false;
//                    }
//                }
//                if (flag) {
//                    takeScreenshot();
//                } else {
//                    finish();
//                }
//
//            } else {
//                finish();
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }


}

