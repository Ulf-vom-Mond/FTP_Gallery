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
        if (tag.contains("ipAddress")) {
            message = message + "an IP address or an URL";
        }
        if (tag.contains("username")) {
            if (message.length() > 20) {
                message = message + " and a username";
            }else {
                message = message + "a username";
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
