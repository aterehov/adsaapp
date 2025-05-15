package ru.anoadsa.adsaapp.ui.activities.video;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class InstructionFragment extends DialogFragment {
    private Runnable onCloseAction;

    public void setOnCloseAction(Runnable r) {
        onCloseAction = r;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Инструкция")
                .setMessage("1. Нажмите 'Join in browser'\n2. Нажмите 'Join meeting'")
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
