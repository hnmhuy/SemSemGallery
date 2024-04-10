package com.example.semsemgallery.activities.main.viewholders;

import com.example.semsemgallery.models.Picture;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class GalleryItem implements Comparable<GalleryItem> {
    public static final int THUMBNAIL = 1;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    public static final int GROUPDATE = 2;
    private Date time;
    private int priority;
    private Object data;
    private int type;

    public GalleryItem(Object data) {
        if (data instanceof Picture) {
            priority = 0;
            time = ((Picture) data).getDateTaken();
            type = THUMBNAIL;
        } else if (data instanceof DateHeaderItem) {
            time = ((DateHeaderItem) data).getDate();
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

    public String getDateFormatted() {
        return dateFormat.format(time);
    }

    @Override
    public int compareTo(GalleryItem other) {
        int dateComparison = time.compareTo(other.getTime());
        if (dateComparison == 0)
            return Integer.compare(priority, other.getPriority());
        else return dateComparison;
    }

}
