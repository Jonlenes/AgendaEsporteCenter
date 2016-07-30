package com.jonlenes.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by asus on 21/07/2016.
 */
public class Util {
    static public Date parseDate(String sDate) throws ParseException {
        return new Date(new SimpleDateFormat("dd/MM/yyyy").parse(sDate).getTime());
    }

    static public Time parseTime(String sTime) throws ParseException {
        return new Time(new SimpleDateFormat("HH:mm").parse(sTime).getTime());
    }

    static public Time sumTime(Time time, Long minutsAdd) throws ParseException {
        return new Time(time.getTime() + minutsAdd * 60 * 1000);
    }

    static public String formatTime(Time time) {
        return new SimpleDateFormat("HH:mm").format(time);
    }

    static public String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }





    static public Bitmap getBitmap(Activity activity, byte [] bytes) {
        if (bytes != null)
            return  BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
        else
            return BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_perfil);
    }
}
