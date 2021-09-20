package com.example.autoclicker_messengerclicker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public final class DataBase {

    private SQLiteDatabase coordinatesDB;
    private SQLiteDatabase messagesDB;
    private CoordinatesDbHelper coordinatesDbHelper;
    private MessagesDbHelper messagesDbHelper;

    public DataBase(Context context, String table) {
        if(table == Coordinates.TABLE_NAME){
            coordinatesDbHelper = new CoordinatesDbHelper(context);
            coordinatesDB = coordinatesDbHelper.getWritableDatabase();
        }
        else if (table == Messages.TABLE_NAME){
            messagesDbHelper = new MessagesDbHelper(context);
            messagesDB = messagesDbHelper.getWritableDatabase();
        }
    }

    public boolean insertCoordinatesInDataBase(String s, int x, int y){
        boolean sucess = true;
        ContentValues values = new ContentValues();
        values.put(Coordinates.COLUMN_CHARACTER, s);
        values.put(Coordinates.COLUMN_X_COORDINATE, x);
        values.put(Coordinates.COLUMN_Y_COORDINATE, y);
        long newRowID = coordinatesDB.insert(Coordinates.TABLE_NAME, null, values);
        if(newRowID == -1) sucess = false;
        return sucess;
    }

    public int[] getCoordinatesInDataBase(String character){
        coordinatesDB = coordinatesDbHelper.getReadableDatabase();
        String[] projection = {
            Coordinates.COLUMN_X_COORDINATE,
            Coordinates.COLUMN_Y_COORDINATE
        };
        String selection = Coordinates.COLUMN_CHARACTER + " = ?";
        String[] selectionArgs = { character };
        String sortOrder = Coordinates.COLUMN_X_COORDINATE + " ASC";

        Cursor cursor = coordinatesDB.query(
                Coordinates.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();
        int coordinates[];
        coordinates = new int[2];
        coordinates[0] = cursor.getInt(0);
        coordinates[1] = cursor.getInt(1);
        cursor.close();
        return coordinates;
    }

    /**** Coordinates data base configuration ****/
    public static class Coordinates implements BaseColumns {
        public static final String TABLE_NAME = "coordinates";
        public static final String COLUMN_CHARACTER = "character";
        public static final String COLUMN_X_COORDINATE = "x";
        public static final String COLUMN_Y_COORDINATE = "y";
    }

    private static final String SQL_CREATE_ENTRIES_COORDINATES =
            "CREATE TABLE " + Coordinates.TABLE_NAME + " (" +
                    Coordinates._ID + " INTEGER PRIMARY KEY," +
                    Coordinates.COLUMN_CHARACTER + " TEXT," +
                    Coordinates.COLUMN_X_COORDINATE + " INTEGER," +
                    Coordinates.COLUMN_Y_COORDINATE + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES_COORDINATES =
            "DROP TABLE IF EXISTS " + Coordinates.TABLE_NAME;

    public class CoordinatesDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Coordinates.db";

        public CoordinatesDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_COORDINATES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES_COORDINATES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    /**** Messages data base configuration ****/
    public static class Messages implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_GROUP_MESSAGE = "group_message";
    }

    private static final String SQL_CREATE_ENTRIES_MESSAGES =
            "CREATE TABLE " + Messages.TABLE_NAME + " (" +
                    Messages._ID + " INTEGER PRIMARY KEY," +
                    Messages.COLUMN_MESSAGE + " TEXT," +
                    Messages.COLUMN_GROUP_MESSAGE + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES_MESSAGES =
            "DROP TABLE IF EXISTS " + Messages.TABLE_NAME;

    public class MessagesDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Messages.db";

        public MessagesDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_MESSAGES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES_MESSAGES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
