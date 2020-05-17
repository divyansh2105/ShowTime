package com.example.movietime.ui.booking_details;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.movietime.BookingObject;
import com.example.movietime.R;
import com.example.movietime.adapters.BookingDetailsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookingDetailsFragment extends Fragment {

//    private SendViewModel sendViewModel;

    public interface BookingDetailInterface
    {
        public void bookingInterfaceSetText(String s);
    }

    BookingDetailInterface bookingDetailInterface;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            bookingDetailInterface = (BookingDetailInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    DatabaseReference databaseReference;
    List<BookingObject> bookingObjects;


    String loggedInEmailId;

    RadioButton radioButton;
    RadioGroup radioGroup;

    int radiobutton_checked=R.id.radio_phone;
    EditText phone_edittext;
    TextView email_tv;

    RecyclerView recyclerView_bookingDetails;
    BookingDetailsAdapter bookingDetailsAdapter;

    Button go_button;
    List<JSONObject> jsonObjects;

    public BookingDetailsFragment(String loggedInEmailId,List<JSONObject> jsonObjects)
    {
        this.loggedInEmailId=loggedInEmailId;
        this.jsonObjects=jsonObjects;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//        sendViewModel =
//                ViewModelProviders.of(this).get(SendViewModel.class);
        View root = inflater.inflate(R.layout.fragment_booking_details, container, false);
//        sendViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        bookingDetailInterface.bookingInterfaceSetText("Booking Details");

        phone_edittext=root.findViewById(R.id.editText_phone);
        email_tv=root.findViewById(R.id.email_display_tv);
        radioGroup=(RadioGroup)root.findViewById(R.id.radio_group);
        go_button=root.findViewById(R.id.go_button);

        email_tv.setText(loggedInEmailId);

        recyclerView_bookingDetails =root.findViewById(R.id.recyclerview_booking_details);
        recyclerView_bookingDetails.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radiobutton_checked=checkedId;
                if(checkedId==R.id.radio_phone)
                {
                    email_tv.setVisibility(View.GONE);
                    phone_edittext.setVisibility(View.VISIBLE);
                }
                else if(checkedId==R.id.radio_email)
                {
                    phone_edittext.setVisibility(View.INVISIBLE);
                    email_tv.setVisibility(View.VISIBLE);
                }
            }
        });

        bookingObjects = new ArrayList<>();
        go_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");
                retrieveDatabase();

            }
        });


        return root;
    }



    public void retrieveDatabase()
    {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                bookingObjects.clear();

                for (DataSnapshot iterator : dataSnapshot.getChildren()) {
                    BookingObject bookingObject = iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }

                if(radiobutton_checked==R.id.radio_phone)
                {
                    populateThroughPhone();
                }
                else if(radiobutton_checked==R.id.radio_email)
                {
                    populateThroughEmail();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public void populateThroughPhone()
    {
        List<BookingObject> bookingObjects_byPhone=new ArrayList<>();
        for(int i=0;i<bookingObjects.size();i++)
        {
            String newPhone=phone_edittext.getText().toString();
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



            if(bookingObjects.get(i).getPhone().equals(newphone2))
            {
                bookingObjects_byPhone.add(bookingObjects.get(i));
            }
        }
        bookingDetailsAdapter=new BookingDetailsAdapter(bookingObjects_byPhone,jsonObjects);
        recyclerView_bookingDetails.setAdapter(bookingDetailsAdapter);



    }

    public void populateThroughEmail()
    {
        List<BookingObject> bookingObjects_byEmail=new ArrayList<>();
        for(int i=0;i<bookingObjects.size();i++)
        {

            if(bookingObjects.get(i).getEmail().equals(loggedInEmailId))
            {
                bookingObjects_byEmail.add(bookingObjects.get(i));
            }
        }
        bookingDetailsAdapter=new BookingDetailsAdapter(bookingObjects_byEmail,jsonObjects);
        recyclerView_bookingDetails.setAdapter(bookingDetailsAdapter);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}