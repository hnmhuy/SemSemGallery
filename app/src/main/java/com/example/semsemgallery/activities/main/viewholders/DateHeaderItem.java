package com.example.semsemgallery.activities.main.viewholders;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHeaderItem {
    private Date date;

    public DateHeaderItem(Date value) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(value);

        c1.set(Calendar.HOUR_OF_DAY, 23);
        c1.set(Calendar.MINUTE, 59);
        c1.set(Calendar.SECOND, 59);
        c1.set(Calendar.MILLISECOND, 999);

        date = c1.getTime();
    }

    public Date getDate() {
        return date;
    }
}
