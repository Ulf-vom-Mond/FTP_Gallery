package com.noah.ftpgallery.ui.gallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.noah.ftpgallery.R;

public class Introduction extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        builder.setMessage("There are three options for downloading a file:\n" +
                "1. Tap a non-media item (no picture or video) in the file explorer\n" +
                "2. Long-click a file in the file explorer\n" +
                "3. Long-click a photo or video in the media viewer\n\n" +
                "To navigate in the media viewer, just tap at the right or left border of the screen\n\n" +
                "Please don't wonder about crashes or when something doesn't work. My duck couldn't always help me.\n\n" +
                "I hope the rest is self-explanatory. If not, see the \"About\" dialog and feel free to contact the developers.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                });
        return builder.create();
    }

}