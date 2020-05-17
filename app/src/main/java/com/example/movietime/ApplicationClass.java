package com.example.movietime;

import android.app.Application;

import java.util.List;

public class ApplicationClass extends Application {

    private List<ContactObject> contactsListFromCursor;

    public String getApplicationEmail() {
        return applicationEmail;
    }

    public void setApplicationEmail(String applicationEmail) {
        this.applicationEmail = applicationEmail;
    }

    private String applicationEmail;

    private String applicationPhone;

    public String getApplicationPhone() {
        return applicationPhone;
    }

    public void setApplicationPhone(String applicationPhone) {
        this.applicationPhone = applicationPhone;
    }

    public List<ContactObject> getContactsListFromCursor() {
        return contactsListFromCursor;
    }

    public void setContactsListFromCursor(List<ContactObject> contactsListFromCursor) {
        this.contactsListFromCursor = contactsListFromCursor;
    }




}
