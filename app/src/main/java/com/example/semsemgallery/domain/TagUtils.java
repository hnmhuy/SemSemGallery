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

import com.example.semsemgallery.activities.search.SearchViewActivity;
import com.example.semsemgallery.models.Tag;

import java.io.File;
import java.util.ArrayList;

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

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_PICTURETAG);
            db.execSQL(CREATE_TABLE_TAG);
            Toast.makeText(mContext, "Create database successfully", Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Log.e("SearchViewActivity",e.toString());
            Toast.makeText(mContext, "Create database failed", Toast.LENGTH_SHORT).show();
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
        SQLiteDatabase db;

        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (dbFile.exists()) {
            db = getReadableDatabase();
        } else {
            db = getWritableDatabase();
        }
        return db;
    }

    // Insert a new Tag
    public void insertTag(SQLiteDatabase db, String tagName) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAGNAME, tagName);
        db.insert(TABLE_TAG, null, values);
    }

    public ArrayList<Tag> getAllTags(SQLiteDatabase db) {
        ArrayList<Tag> tags = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TAG;
        Cursor cursor = db.rawQuery(query, null);


        if(cursor != null && cursor.moveToFirst()){
            do{
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TAGID));
                @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(COLUMN_TAGNAME));
                Tag tag = new Tag(id, tagName);
                tags.add(tag);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return tags;
    }

    public Tag searchTag(SQLiteDatabase db, String keyword){
        String selectionArgs = "%" + keyword + "%";

        String query = "SELECT * FROM " + TABLE_TAG + " WHERE " + COLUMN_TAGNAME + " LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{selectionArgs});

        Tag tag = null;
        if(cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_TAGID));
            @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex(COLUMN_TAGNAME));
            tag = new Tag(id, tagName);
        }

        if(cursor != null) {
            cursor.close();
        }

        return tag;
    }

}
