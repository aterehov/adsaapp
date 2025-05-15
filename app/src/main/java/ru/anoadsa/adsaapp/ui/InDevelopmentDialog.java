package ru.anoadsa.adsaapp.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import androidx.fragment.app.DialogFragment;

public class InDevelopmentDialog extends DialogFragment {
    private Runnable onCloseAction;

    public void setOnCloseAction(Runnable r) {
        onCloseAction = r;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("В разработке")
                .setMessage("Данная функция находится в разработке и в данный момент недоступна")
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
