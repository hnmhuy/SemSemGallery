package com.example.semsemgallery.activities.main2.viewholder;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHeaderItem {
    private Date date;
    private String dateFormatted;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);

    public DateHeaderItem(Date value) {
//        Calendar c1 = Calendar.getInstance();
//        c1.setTime(value);
//
//        c1.set(Calendar.HOUR_OF_DAY, 23);
//        c1.set(Calendar.MINUTE, 59);
//        c1.set(Calendar.SECOND, 59);
//        c1.set(Calendar.MILLISECOND, 999);

        date = value;

        dateFormatted = dateFormat.format(date);
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }
}
