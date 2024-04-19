package com.example.semsemgallery.domain;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.semsemgallery.models.Tag;

import java.util.ArrayList;
import java.util.Objects;

public class TagUtils extends SQLiteOpenHelper {
    private final Context mContext;

    public static final String DATABASE_NAME = "gallery-tag.db";
    private static final int DATABASE_VERSION = 1;

    // Define Name and Attributes of Table PICTURETAG
    // (Declaring this way will be easy to reuse when need to Query)
    public static final String TABLE_PICTURETAG = "PICTURETAG";
    public static final String COLUMN_PICTUREID = "PICTUREID";
    public static final String COLUMN_TAGID_PICTURETAG = "TAGID"; // Foreign Key

    // Define Name and Attributes of Table TAG
    public static final String TABLE_TAG = "TAG";
    public static final String COLUMN_TAGID = "TAGID";
    public static final String COLUMN_TAGNAME = "TAGNAME";

    // Script creates Table PICTURETAG
    private static final String CREATE_TABLE_TAG = "CREATE TABLE " + TABLE_TAG + "("
            + COLUMN_TAGID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TAGNAME + " TEXT"
            + ")";
    private static final String CREATE_TABLE_PICTURETAG = "CREATE TABLE " + TABLE_PICTURETAG + "("
            + COLUMN_PICTUREID + " TEXT,"
            + COLUMN_TAGID_PICTURETAG + " INTEGER,"
            + "PRIMARY KEY (" + COLUMN_PICTUREID + ", " + COLUMN_TAGID_PICTURETAG + ")"
            + ")";

    // Constructor
    public TagUtils(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    // Override methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_PICTURETAG);
            db.execSQL(CREATE_TABLE_TAG);
        } catch (SQLException e) {
            Log.e("SearchViewActivity", e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete old Tables if they are exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PICTURETAG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);

        // Create Database
        onCreate(db);
    }


    // Get Database
    public SQLiteDatabase myGetDatabase(Context context) {
        return context.getDatabasePath(DATABASE_NAME).exists() ?
                getReadableDatabase() : getWritableDatabase();
    }

    // Insert a new Tag
    public void insertTag(SQLiteDatabase db, String tagName) {
        try {
            if (!checkAddedTag(db, tagName)) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TAGNAME, tagName);
                db.insert(TABLE_TAG, null, values);
            } else {
                showToast("This" + tagName + " already added");
            }
        } catch (SQLException e) {
            Log.e("TagUtils", Objects.requireNonNull(e.getMessage()));
        }

    }

    public void insertTagPicture(SQLiteDatabase db, String tagName, String pictureId) {
        if (!checkAddedTag(db, tagName)) {
            insertTag(db, tagName);
        }
        try {
            int tagId = getTagId(db, tagName);
            ContentValues values = new ContentValues();
            values.put(COLUMN_TAGID_PICTURETAG, tagId);
            values.put(COLUMN_PICTUREID, pictureId);
            db.insert(TABLE_PICTURETAG, null, values);
        } catch (SQLException e) {
            Log.e("TagUtils", Objects.requireNonNull(e.getMessage()));
        }
    }

    // Get all tags
    public ArrayList<Tag> getAllTags(SQLiteDatabase db) {
        ArrayList<Tag> tags = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TAG;
        Cursor cursor = db.rawQuery(query, null);


        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TAGID));
                @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(COLUMN_TAGNAME));
                Tag tag = new Tag(id, tagName);
                tags.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    // Search tag by keywords
    public Tag searchTag(SQLiteDatabase db, String keyword) {
        String selectionArgs = "%" + keyword + "%";

        String query = "SELECT * FROM " + TABLE_TAG + " WHERE " + COLUMN_TAGNAME + " LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{selectionArgs});

        Tag tag = null;
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TAGID));
            @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(COLUMN_TAGNAME));
            tag = new Tag(id, tagName);
        }

        if (cursor != null) {
            cursor.close();
        }

        return tag;
    }

    // Show toast methods
    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    // Check added tag
    private boolean checkAddedTag(SQLiteDatabase db, String tagName) {
        String selection = COLUMN_TAGNAME + " = ?";
        String[] selectionArgs = {tagName};
        try (Cursor cursor = db.query(TABLE_TAG, null, selection, selectionArgs, null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    /* getTagInfo usage
     * if isTagId == true, value == tagId => get TagName by TagId
     * if isTagId == false, value == tagName => getTagId by TagName
     * */
    @SuppressLint("Range")
    private int getTagId(SQLiteDatabase db, String value) {
        int info = 0;
        String columnName = COLUMN_TAGID;
        String query = "SELECT " + columnName + " FROM " + TABLE_TAG + " WHERE " +  COLUMN_TAGNAME + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{value})) {
            if (cursor != null && cursor.moveToFirst()) {
                info = cursor.getInt(cursor.getColumnIndex(columnName));
            }
        } catch (Exception e) {
            Log.e("TagUtils", Objects.requireNonNull(e.getMessage()));
        }

        return info;
    }

    public ArrayList<Tag> getTagsByPictureId(SQLiteDatabase db, String pictureId) {
        ArrayList<Tag> data = new ArrayList<>();
        String query = "SELECT DISTINCT " + TABLE_TAG + "." + COLUMN_TAGID + ", " + TABLE_TAG + "." + COLUMN_TAGNAME +
                " FROM " + TABLE_TAG + ", " + TABLE_PICTURETAG +
                " WHERE " + COLUMN_PICTUREID + " = " + "\"" + pictureId + "\"" +
                " AND " + TABLE_TAG + "." + COLUMN_TAGID + " = " + TABLE_PICTURETAG + "." + COLUMN_TAGID_PICTURETAG;

        try(Cursor cursor = db.rawQuery(query, null)) {
            if(cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int tagId = cursor.getInt(cursor.getColumnIndex(COLUMN_TAGID));
                    @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(COLUMN_TAGNAME));
                    Tag tag = new Tag(tagId, tagName);
                    tag.setType(3);
                    data.add(tag);
                } while(cursor.moveToNext());
            }
        }
        return data;
    }

    public ArrayList<String> getPictureIdsByTagName(SQLiteDatabase db, String tagName) {
        ArrayList<String> data = new ArrayList<>();
        String query = "SELECT DISTINCT " + TABLE_PICTURETAG + "." + COLUMN_PICTUREID  +
                " FROM " + TABLE_TAG + ", " + TABLE_PICTURETAG +
                " WHERE " + TABLE_TAG + "." + COLUMN_TAGNAME + " = " + "\"" + tagName + "\"" +
                " AND " + TABLE_TAG + "." + COLUMN_TAGID + " = " + TABLE_PICTURETAG + "." + COLUMN_TAGID_PICTURETAG;
        try(Cursor cursor = db.rawQuery(query, null)) {
            if(cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String pictureId = cursor.getString(cursor.getColumnIndex(COLUMN_PICTUREID));
                    Log.d("Tag", pictureId);
                    data.add(pictureId);
                } while(cursor.moveToNext());
            }
        }
        return data;
    }

    public void removePictureTag(SQLiteDatabase db, String tagName, String pictureId) {
        int tagId = getTagId(db, tagName);
        String query = "DELETE FROM " + TABLE_PICTURETAG +
                " WHERE " + COLUMN_TAGID_PICTURETAG + " = " + tagId  +
                " AND " + COLUMN_PICTUREID + " = \"" + pictureId  + "\"";

        // Execute the query
        try {
            db.execSQL(query);
            String queryCheck = "SELECT * FROM " + TABLE_PICTURETAG +
                    " WHERE " + COLUMN_TAGID_PICTURETAG + " = ?";

            Cursor cursor = db.rawQuery(queryCheck, new String[]{String.valueOf(tagId)});
            if (cursor.getCount() == 0) {
                String deleteQuery = "DELETE FROM " + TABLE_TAG +
                        " WHERE " + COLUMN_TAGNAME + " = \"" + tagName  + "\"";
                db.execSQL(deleteQuery);
            }
            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removePictureTagById(SQLiteDatabase db, int tagId, String pictureId) {
        String query = "DELETE FROM " + TABLE_PICTURETAG +
                " WHERE " + COLUMN_TAGID_PICTURETAG + " = " + tagId  +
                " AND " + COLUMN_PICTUREID + " = \"" + pictureId  + "\"";

        // Execute the query
        try {
            db.execSQL(query);
            Log.d("Remove", "Successfully");
            String queryCheck = "SELECT * FROM " + TABLE_PICTURETAG +
                    " WHERE " + COLUMN_TAGID_PICTURETAG + " = ?";

            Cursor cursor = db.rawQuery(queryCheck, new String[]{String.valueOf(tagId)});
            if (cursor.getCount() == 0) {
                String deleteQuery = "DELETE FROM " + TABLE_TAG +
                        " WHERE " + COLUMN_TAGID + " = \"" + tagId  + "\"";
                db.execSQL(deleteQuery);
            }
            cursor.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Tag> getRecentTags(SQLiteDatabase db) {
        ArrayList<Tag> data = new ArrayList<>();
        String query = "SELECT " + COLUMN_TAGID + ", " + COLUMN_TAGNAME +
                " FROM " + TABLE_TAG +
                " ORDER BY " + COLUMN_TAGID +  " DESC";
        try(Cursor cursor = db.rawQuery(query, null)) {
            if(cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int tagId = cursor.getInt(cursor.getColumnIndex(COLUMN_TAGID));
                    @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(COLUMN_TAGNAME));
                    Tag tag = new Tag(tagId, tagName);
                    tag.setType(2);
                    data.add(tag);
                } while(cursor.moveToNext());
            }
        }
        return data;
    }
}
