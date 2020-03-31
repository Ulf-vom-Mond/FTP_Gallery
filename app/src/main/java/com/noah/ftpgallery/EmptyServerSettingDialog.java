package com.noah.ftpgallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class EmptyServerSettingDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String tag = getTag();
        String message = "Please enter ";
        if (tag.contains("ipaddress")) {
            message = message + "an IP address or an URL";
        }
        if (tag.contains("username")) {
            if (tag.contains("ipAddress")) {
                message = message + " and a username";
            }else {
                message = message + "a username";
            }
        }
        if (tag.contains("samename")) {
            if (tag.contains("ipaddress") || tag.contains("username")) {
                message = message + "\nAlso, please use a name that doesn't already exist";
            }else {
                message = "Please use a name that doesn't already exist";
            }
        }
        builder.setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
