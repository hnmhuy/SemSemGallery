package com.example.semsemgallery.activities.main2.viewholder;

import com.example.semsemgallery.models.Picture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GalleryItem implements Comparable<GalleryItem> {
    public static final int THUMBNAIL = 1;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    public static final int GROUPDATE = 2;
    private Date time;
    private Date timeAdded;
    private int priority;
    private Object data;
    private int type;

    public GalleryItem(Object data) {
        if (data instanceof Picture) {
            priority = 0;
            time = ((Picture) data).getDateTaken();
            timeAdded = ((Picture) data).getDateAdded();
            type = THUMBNAIL;
        } else if (data instanceof DateHeaderItem) {
            time = ((DateHeaderItem) data).getDate();
            timeAdded = time;
            priority = 1;
            type = GROUPDATE;
        }

        this.data = data;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public static void setDateFormat(SimpleDateFormat dateFormat) {
        GalleryItem.dateFormat = dateFormat;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getTimeAdded() {
        return timeAdded;
    }

    public String getDateFormatted() {
        return dateFormat.format(time);
    }

    @Override
    public int compareTo(GalleryItem other) {

        long time1 = time.getTime() == 0 ? timeAdded.getTime() : time.getTime();
        long time2 = other.getTime().getTime() == 0 ? other.getTimeAdded().getTime() : other.getTime().getTime();

        return Long.compare(time1, time2);
    }

}
