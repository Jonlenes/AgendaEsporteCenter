package com.jonlenes.app;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by asus on 24/07/2016.
 */
public class TreatException {
    static public void treat(AppCompatActivity activity, Exception exception) {
        new TreatException(activity, exception, true);
    }

    private TreatException(AppCompatActivity activity, Exception exception, boolean checkConnection) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if (exception instanceof RuntimeException) {
            builder.setMessage(exception.getMessage());
        } else {
            builder.setMessage("Erro inesperado.");
        }
        exception.printStackTrace();

        builder.setPositiveButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
