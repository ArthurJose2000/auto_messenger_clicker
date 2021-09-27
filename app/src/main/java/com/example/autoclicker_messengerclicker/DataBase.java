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
            coordinatesDbHelper.onUpgrade(coordinatesDB, 1, 1);
        }
        else if (table == Messages.TABLE_NAME){
            messagesDbHelper = new MessagesDbHelper(context);
            messagesDB = messagesDbHelper.getWritableDatabase();
        }
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

    public void insertCoordinatesToDataBase(String s, int x, int y){
        //boolean sucess = true;
        ContentValues values = new ContentValues();
        values.put(Coordinates.COLUMN_CHARACTER, s);
        values.put(Coordinates.COLUMN_X_COORDINATE, x);
        values.put(Coordinates.COLUMN_Y_COORDINATE, y);
        long newRowID = coordinatesDB.insert(Coordinates.TABLE_NAME, null, values);
        //if(newRowID == -1) sucess = false;
        //return sucess;
    }

    public int[] getCoordinatesFromDataBase(String StringCharacter){
        coordinatesDB = coordinatesDbHelper.getReadableDatabase();
        String[] projection = {
                Coordinates.COLUMN_X_COORDINATE,
                Coordinates.COLUMN_Y_COORDINATE
        };
        String selection = Coordinates.COLUMN_CHARACTER + " = ?";
        String[] selectionArgs = { StringCharacter };
        String sortOrder = Coordinates.COLUMN_X_COORDINATE + " ASC"; //unnecessary

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
                    Messages.COLUMN_GROUP_MESSAGE + " TEXT)";

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

    public void insertMessagesToDataBase(String message, String groupName){
        //boolean sucess = true;
        ContentValues values = new ContentValues();
        values.put(Messages.COLUMN_MESSAGE, message);
        values.put(Messages.COLUMN_GROUP_MESSAGE, groupName);
        long newRowID = messagesDB.insert(Messages.TABLE_NAME, null, values);
        //if(newRowID == -1) sucess = false;
        //return sucess;
    }

    public void deleteGroupName(String groupName){
        String selection = Messages.COLUMN_GROUP_MESSAGE + " LIKE ?";
        String[] selectionArgs = { groupName };
        int deletedRows = messagesDB.delete(Messages.TABLE_NAME, selection, selectionArgs);
    }

    public String getMessageFromDataBase(String groupName){
        messagesDB = messagesDbHelper.getReadableDatabase();
        String[] projection = {
                Messages.COLUMN_MESSAGE
        };
        String selection = Messages.COLUMN_GROUP_MESSAGE + " = ?";
        String[] selectionArgs = { groupName };
        String sortOrder = Messages.COLUMN_MESSAGE + " ASC"; //unnecessary

        Cursor cursor = messagesDB.query(
                Messages.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();
        String msg = cursor.getString(0);
        cursor.close();
        return msg;
    }

    public ArrayList<String> getGroupNamesFromDataBase(){
        messagesDB = messagesDbHelper.getReadableDatabase();
        String[] projection = {
                Messages.COLUMN_GROUP_MESSAGE
        };
        String sortOrder = Messages._ID + " ASC";

        Cursor cursor = messagesDB.query(
                Messages.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        ArrayList<String> groups = new ArrayList<String>();

        while(cursor.moveToNext())
            groups.add(cursor.getString(0));

        cursor.close();
        return groups;
    }
}
