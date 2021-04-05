package com.example.godhand.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////////////////////
// DBHelper Class
// Used for managing the database
//////////////////////////////////////////////////////////////////////////////////////////
public class DBHelper extends SQLiteOpenHelper {
    // Database's version
    private static final int DATABASE_VERSION = 1;
    // Database's name
    private static final String DATABASE_NAME = "godhandDB";
    // Table's name
    private static final String TABLE_GESTURE = "gesture";
    // object's id
    private static final String KEY_ID = "id";
    // object's name
    private static final String KEY_NAME = "name";
    // object's content (command)
    private static final String KEY_CONTENT = "content";

    // Constructor
    public DBHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onCreate()
    // Creating the table and adding objects
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate (SQLiteDatabase db) {
        String CREATE_GESTURE_TABLE = "CREATE TABLE " + TABLE_GESTURE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, "
                + KEY_CONTENT + " TEXT" + ")";
        db.execSQL(CREATE_GESTURE_TABLE);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // onUpgrade()
    // Dropping table if exists, then call onCreate()
    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURE);
        onCreate(db);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for adding new Gesture to database
    // Para: gesture - Gesture's object
    //////////////////////////////////////////////////////////////////////////////////////////
    public void addGesture (Gesture gesture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, gesture.getName());
        values.put(KEY_CONTENT, gesture.getContent());
        db.insert(TABLE_GESTURE, null, values);
        db.close();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for getting Gesture from table
    // Para: id - Gesture object's id
    // Return: gesture - Gesture's object
    //////////////////////////////////////////////////////////////////////////////////////////
    public Gesture getGesture(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GESTURE, new String[] {KEY_ID, KEY_NAME, KEY_CONTENT}, KEY_ID
        + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        Gesture gesture = new Gesture(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2));
        return gesture;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for getting all Gestures from table
    // Return: gesturesList - Gestures list
    //////////////////////////////////////////////////////////////////////////////////////////
    public List<Gesture> getAllGesture() {
        List<Gesture> gesturesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GESTURE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Gesture gesture = new Gesture();
                gesture.setId(Integer.parseInt(cursor.getString(0)));
                gesture.setName(cursor.getString(1));
                gesture.setContent(cursor.getString(2));
                gesturesList.add(gesture);
            } while (cursor.moveToNext());
        }
        return gesturesList;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for updating the Gesture in database
    // Para: gesture - Gesture's object
    // Return: int - Gesture's id
    //////////////////////////////////////////////////////////////////////////////////////////
    public int undateGesture(Gesture gesture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, gesture.getName());
        values.put(KEY_CONTENT, gesture.getContent());
        return db.update(TABLE_GESTURE, values, KEY_ID + "=?",
                new String[]{String.valueOf(gesture.getId())});
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for deleting the Gesture in database
    // Para: gesture - Gesture's object
    //////////////////////////////////////////////////////////////////////////////////////////
    public void deleteGesture(Gesture gesture) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GESTURE, KEY_ID + "=?",
                new String[]{String.valueOf(gesture.getId())});
        db.close();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // Used for get the total number of the Gestures in database
    // Return: integer - the total number of Gestures in database
    //////////////////////////////////////////////////////////////////////////////////////////
    public int getGestureCount() {
        String countQuery = "SELECT * FROM " + TABLE_GESTURE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

}
