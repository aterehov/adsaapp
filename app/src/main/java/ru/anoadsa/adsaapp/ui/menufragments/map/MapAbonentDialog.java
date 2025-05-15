package ru.anoadsa.adsaapp.ui.menufragments.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MapAbonentDialog extends DialogFragment {
    private Runnable onCloseAction;

    public void setOnCloseAction(Runnable r) {
        onCloseAction = r;
    }

    private String name;
    private String address;
    private Double distance;

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String m = "Адрес: " + address;
        if (distance != null) {
            m += "\nРасстояние: " + distance.intValue() + " м";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(name)
                .setMessage(m)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
//                        new Handler().post(onCloseAction);
//                        onCloseAction.run();
                        dialog.cancel();
                    }
                });
        return builder.create();
    }
}
