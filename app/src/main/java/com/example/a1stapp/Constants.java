package com.example.a1stapp;

import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContracts;

public class Constants {

    public static final long MAX_BYTES_PDF = 60000000; //60mb-

    public static final int PICK_IMAGE = 1;

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            //TODO: action

        }
    }

}
