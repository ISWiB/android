package org.iswib.iswibexplorer.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import org.iswib.iswibexplorer.R;

/**
 * The DialogLocations shows the view from which any location relative
 *  to the festival can be shown in maps
 *
 * @author Jovan
 * @version 1.1
 */
public class DialogLocations extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_locations, null));
        return builder.create();
    }
}
