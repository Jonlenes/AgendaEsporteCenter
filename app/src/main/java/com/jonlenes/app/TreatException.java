package com.jonlenes.app;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * Created by asus on 24/07/2016.
 */
public class TreatException {
    private TreatException(Context context, Exception exception) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

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

    static public void treat(Context context, Exception exception) {
        new TreatException(context, exception);
    }
}
