package com.example.movietime.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.movietime.ApplicationClass;
import com.example.movietime.BookingObject;
import com.example.movietime.CinemaObject;
import com.example.movietime.ContactObject;
import com.example.movietime.MovieObject;
import com.example.movietime.R;
import com.example.movietime.activities.ReceiptActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class SeatMatrixFragment extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    DatabaseReference databaseReference;

    Button btnDatePicker, confirm_book;
    View root;
    String selected_year, selected_month, selected_date;
    int mYear, mMonth, mDay, mHour, mMinute;
    MovieObject movieObject;
    Spinner spinner;
    CinemaObject cinemaObject;
    String selectedInterval;
    String setDate;
    Integer cinemaIndex;
    int[] seat_flag;                //0 for available, 1 for currently choosing, 2 for contacts, 3 for strangers
    Button[] buttons;
    String[] contact_booked_name;
    Cursor cursor ;

    String phone_no="";

    List<ContactObject> contactObjects;
    List<BookingObject> bookingObjects;

    public SeatMatrixFragment(MovieObject movieObject, Integer cinemaIndex) {
        this.movieObject=movieObject;
        this.cinemaIndex=cinemaIndex;
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment SeatMatrixFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static SeatMatrixFragment newInstance(String param1, String param2) {
//        SeatMatrixFragment fragment = new SeatMatrixFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root=inflater.inflate(R.layout.fragment_seat_matrix, container, false);

        btnDatePicker=root.findViewById(R.id.select_date);

        btnDatePicker.setOnClickListener(this);

        spinner=root.findViewById(R.id.spinner);

        confirm_book=root.findViewById(R.id.confirm_book_button);
        confirm_book.setVisibility(View.VISIBLE);
        confirm_book.setOnClickListener(this);

        seat_flag=new int[18];
        contact_booked_name=new String[18];
        buttons=new Button[18];
        for(int i=0;i<18;i++)
        {
            seat_flag[i]=0;
            contact_booked_name[i]="";
            String s="button"+(i+1);
            int resID = getResources().getIdentifier(s, "id", getActivity().getPackageName());
            buttons[i]=(Button) root.findViewById(resID);
            buttons[i].setOnClickListener(this);
            buttons[i].getBackground().setColorFilter(buttons[i].getContext().getResources().getColor(R.color.colorGrey), PorterDuff.Mode.MULTIPLY);
        }

        databaseReference=FirebaseDatabase.getInstance().getReference("Bookings");

//        contactObjects= GetContactsIntoArrayList();
        contactObjects=((ApplicationClass) getActivity().getApplication()).getContactsListFromCursor();
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
        bookingObjects=new ArrayList<>();

        retrieveDatabase();


         cinemaObject= new CinemaObject();
        String[] intervals=cinemaObject.getIntervals();

        ArrayList<String> arrayList=new ArrayList<>();
        for(int i=0;i<intervals.length;i++)
        {
            arrayList.add(intervals[i]);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),  android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedInterval = parent.getItemAtPosition(position).toString();
                clearUI();
                populateUI();
//                Toast.makeText(parent.getContext(), "Selected: " + selectedInterval,          Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
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

                for(DataSnapshot iterator: dataSnapshot.getChildren())
                {
                    BookingObject bookingObject=iterator.getValue(BookingObject.class);
                    bookingObjects.add(bookingObject);
                }


                Log.d("retrieved_database", String.valueOf(bookingObjects));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {

                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            selected_date= String.valueOf(dayOfMonth);
                            selected_month= String.valueOf(monthOfYear);
                            selected_year= String.valueOf(year);

                             setDate=year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                            Log.d("SeatMatrixRelease",movieObject.getRelease_date());

                            DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                            Date d1 = f.parse(movieObject.getRelease_date(), new ParsePosition(0));
                            Date d2 = f.parse(setDate, new ParsePosition(0));
                            if(d1.compareTo(d2)>0)
                            {
                                Toast.makeText(getContext(),"Movie not Released Yet!!",Toast.LENGTH_SHORT).show();
                                btnDatePicker.setText("Select Date");
                                confirm_book.setEnabled(false);
                            }
                            else {
                                btnDatePicker.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                confirm_book.setEnabled(true);

                                clearUI();
                                populateUI();

                            }

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        else if(v==confirm_book)
        {

            if(btnDatePicker.getText().equals("Select Date"))
            {
                Toast.makeText(getContext(),"Select Valid Date",Toast.LENGTH_SHORT).show();
                return;
            }
            if(numberOfSeatsBooked(seat_flag)<=0 || numberOfSeatsBooked(seat_flag)>8)
            {
                Toast.makeText(getContext(),"Select 1-8 Seats",Toast.LENGTH_SHORT).show();
                return;
            }

            confirm_book.setVisibility(View.INVISIBLE);

            LayoutInflater inflater = getLayoutInflater();
            View alertLayout = inflater.inflate(R.layout.booking_dialog, null);

            final TextView global_label=alertLayout.findViewById(R.id.global_label);
            final TextView proceed_tv = alertLayout.findViewById(R.id.proceed_tv);
            proceed_tv.setText("Proceed with phone: "+((ApplicationClass)getActivity().getApplication()).getApplicationPhone()
            +" and email: "+((ApplicationClass)getActivity().getApplication()).getApplicationEmail());
            final CheckBox global_checkbox = alertLayout.findViewById(R.id.checkbox_global);
            final String[] checkbox_global = new String[1];
            checkbox_global[0]="true";

            global_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        checkbox_global[0] ="true";
                        global_label.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        checkbox_global[0]="false";
                        global_label.setVisibility(View.INVISIBLE);
                    }
                }
            });


            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

            // this is set the view from XML inside AlertDialog
            alert.setView(alertLayout);

            Button ok_button=alertLayout.findViewById(R.id.ok_button);
            Button cancel_button=alertLayout.findViewById(R.id.cancel_button);

            final AlertDialog dialog = alert.create();
            ok_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    phone_no = phone_tv.getText().toString();
//                    if((!android.util.Patterns.PHONE.matcher(phone_no).matches()) || phone_no.equals(""))
//                    {
//                        phone_tv.setHint("Enter Valid Number");
//                        phone_tv.setText("");
//                        return;
//                    }

                    confirm_book.setVisibility(View.VISIBLE);

                    Intent intent=new Intent(getActivity(), ReceiptActivity.class);
                    intent.putExtra("selected_date",setDate);
                    intent.putExtra("selected_interval",selectedInterval);
                    intent.putExtra("movieObject_intent",  movieObject);
                    intent.putExtra("cinemaIndex_intent",cinemaIndex);
                    intent.putExtra("seat_flag",seat_flag);
                    intent.putExtra("phone_no",((ApplicationClass)getActivity().getApplication()).getApplicationPhone());
                    intent.putExtra("checkbox_global", checkbox_global[0]);
                    dialog.cancel();
                    getContext().startActivity(intent);
                }
            });

            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    confirm_book.setVisibility(View.VISIBLE);

                    retrieveDatabase();
                    clearUI();
                    populateUI();
                }
            });


            dialog.show();

        }
        else
        {
            for(int i=0;i<18;i++)
            {
                if(v==buttons[i])
                {
                    if(seat_flag[i]==0) {
//                        buttons[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        buttons[i].getBackground().setColorFilter(buttons[i].getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                        seat_flag[i] = 1;
                    }
                    else if(seat_flag[i]==1)
                    {
//                        buttons[i].setBackgroundColor(getResources().getColor(R.color.colorGrey));
                        buttons[i].getBackground().setColorFilter(buttons[i].getContext().getResources().getColor(R.color.colorGrey), PorterDuff.Mode.MULTIPLY);
                        seat_flag[i] = 0;
                    }
                    else if(seat_flag[i]==2)
                    {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(
                                getContext());
                        if(contact_booked_name[i].equals("YOU"))
                        {
                            builder.setTitle("Booked by YOU");
//                            builder.setMessage("Booked By: "+contact_booked_name[i]);
                        }
                        else {
                            builder.setTitle("Booked by Contact");
                            builder.setMessage("Booked By: " + contact_booked_name[i]);
                        }
//                        builder.setCancelable(true);
                        builder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.cancel();
                                    }
                                });
                        builder.show();
                    }
                    else if(seat_flag[i]==3)
                    {
                        return;
                    }
                }
            }
        }
    }

    int numberOfSeatsBooked(int[] seat_flag_arg)
    {
        int count=0;
        for(int i=0;i<seat_flag_arg.length;i++)
        {
            if(seat_flag_arg[i]==1)
            {
                count++;
            }
        }
        return count;
    }

    List<Integer> extractSeatIdFromString(String seats_string)
    {
        List<Integer> seat_list=new ArrayList<>();
        int i=0;
        String s="";
        while(i<seats_string.length())
        {

            if(seats_string.charAt(i)>=48 && seats_string.charAt(i)<=57)
            {
                s+=seats_string.charAt(i);
            }
            else if(seats_string.charAt(i)==',')
            {
                seat_list.add(Integer.parseInt(s));
                s="";
            }
            i++;
        }
        seat_list.add(Integer.parseInt(s));
        return  seat_list;
    }

    public void populateUI()
    {
        for(int i=0;i<bookingObjects.size();i++)
        {
            int flag=0;
            if(bookingObjects.get(i).getCinemaId().equals(String.valueOf(cinemaIndex))
                    && bookingObjects.get(i).getDateOfShow().equals(setDate)
                    && bookingObjects.get(i).getTimeOfShow().equals(selectedInterval)
                    && bookingObjects.get(i).getMovieId().equals(movieObject.getId()))
            {

                for(int j=0;j<contactObjects.size();j++)
                {

                    if(bookingObjects.get(i).getPhone().equals(contactObjects.get(j).getContactPhoneNo()) && bookingObjects.get(i).getGlobal().equals("true")) {
                        List<Integer> seat_list;
                        String seats_string = bookingObjects.get(i).getSeat();
                        seat_list = extractSeatIdFromString(seats_string);

                        for (int k = 0; k < seat_list.size(); k++) {
//                            buttons[seat_list.get(k)].setBackgroundColor(getResources().getColor(R.color.colorGreen));
                            buttons[seat_list.get(k)-1].getBackground().setColorFilter(buttons[seat_list.get(k)-1].getContext().getResources().getColor(R.color.colorGreen), PorterDuff.Mode.MULTIPLY);
                            seat_flag[seat_list.get(k)-1] = 2;
                            if(contactObjects.get(j).getContactPhoneNo().equals(((ApplicationClass)getActivity().getApplication()).getApplicationPhone()))
                            {
                                contact_booked_name[seat_list.get(k)-1]="YOU";
                            }
                            else {
                                contact_booked_name[seat_list.get(k) - 1] = contactObjects.get(j).getContactName();
                            }
                        }

                        flag=1;
                    }

                }

                if(flag==0)                                 //if not found in contact list
                {
                    List<Integer> seat_list;
                    String seats_string = bookingObjects.get(i).getSeat();
                    seat_list = extractSeatIdFromString(seats_string);

                    for (int k = 0; k < seat_list.size(); k++)
                    {
//                        buttons[seat_list.get(k)].setBack(getResources().getColor(R.color.colorRed));
                        buttons[seat_list.get(k)-1].getBackground().setColorFilter(buttons[seat_list.get(k)-1].getContext().getResources().getColor(R.color.colorRed), PorterDuff.Mode.MULTIPLY);

                        seat_flag[seat_list.get(k)-1] = 3;
                    }
                }


            }


        }
    }

    public void clearUI()
    {
        for(int j=0;j<18;j++) {
//            if(seat_flag[j]!=2 && seat_flag[j]!=3){
                buttons[j].getBackground().setColorFilter(buttons[j].getContext().getResources().getColor(R.color.colorGrey), PorterDuff.Mode.MULTIPLY);
                seat_flag[j]=0;
//            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        clearUI();
        populateUI();
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
}
