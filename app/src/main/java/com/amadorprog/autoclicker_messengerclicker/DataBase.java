package com.amadorprog.autoclicker_messengerclicker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

public final class DataBase {
    int CREATE =1;
    int UPDATE = 2;

    private static DataBase dbInstance;

    private SQLiteDatabase coordinatesDB;
    private CoordinatesDbHelper coordinatesDbHelper;

    private SQLiteDatabase messagesDB;
    private MessagesDbHelper messagesDbHelper;

    private SQLiteDatabase settingsDB;
    private SettingsDbHelper settingsDbHelper;

    private DataBase(Context context) {
        coordinatesDbHelper = new CoordinatesDbHelper(context);
        coordinatesDB = coordinatesDbHelper.getWritableDatabase();

        messagesDbHelper = new MessagesDbHelper(context);
        messagesDB = messagesDbHelper.getWritableDatabase();

        settingsDbHelper = new SettingsDbHelper(context);
        settingsDB = settingsDbHelper.getWritableDatabase();
    }

    public static DataBase getDbInstance(Context context){
        if(dbInstance == null)
            dbInstance = new DataBase(context);
        return dbInstance;
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

    public void deleteAllCoordinates(){
        coordinatesDbHelper.onUpgrade(coordinatesDB, 1, 1);
    }

    public void updateKeyCoordinate(String s, int x, int y){
        coordinatesDB = coordinatesDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Coordinates.COLUMN_CHARACTER, s);
        values.put(Coordinates.COLUMN_X_COORDINATE, x);
        values.put(Coordinates.COLUMN_Y_COORDINATE, y);
        String selection = Coordinates.COLUMN_CHARACTER + " LIKE ?";
        String[] selectionArgs = { s };
        int count = coordinatesDB.update(Coordinates.TABLE_NAME, values, selection, selectionArgs);
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
        int size = cursor.getCount();
        int coordinates[];

        if(size != 0) {
            coordinates = new int[2];
            coordinates[0] = cursor.getInt(0);
            coordinates[1] = cursor.getInt(1);
        }
        else{
            coordinates = null;
        }
        cursor.close();
        return coordinates;
    }

    public int getAmountOfRowsFromCoordinatesDataBase(){
        coordinatesDB = coordinatesDbHelper.getReadableDatabase();
        String[] projection = {
                Coordinates.COLUMN_X_COORDINATE,
        };

        String sortOrder = Coordinates.COLUMN_X_COORDINATE + " ASC"; //unnecessary

        Cursor cursor = coordinatesDB.query(
                Coordinates.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();
        return cursor.getCount();
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
        //boolean success = true;
        ContentValues values = new ContentValues();
        values.put(Messages.COLUMN_MESSAGE, message);
        values.put(Messages.COLUMN_GROUP_MESSAGE, groupName);
        long newRowID = messagesDB.insert(Messages.TABLE_NAME, null, values);
        //if(newRowID == -1) success = false;
        //return success;
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

    /**** Settings data base configuration ****/
    public static class Settings implements BaseColumns {
        public static final String TABLE_NAME = "settings";
        public static final String COLUMN_SETTINGS = "settings";
        public static final String COLUMN_RELATED_SETTINGS = "related_settings";
    }

    private static final String SQL_CREATE_ENTRIES_SETTINGS =
            "CREATE TABLE " + Settings.TABLE_NAME + " (" +
                    Settings._ID + " INTEGER PRIMARY KEY," +
                    Settings.COLUMN_SETTINGS + " TEXT," +
                    Settings.COLUMN_RELATED_SETTINGS + " TEXT)";

    private static final String SQL_DELETE_ENTRIES_SETTINGS =
            "DROP TABLE IF EXISTS " + Settings.TABLE_NAME;

    public class SettingsDbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "Settings.db";

        public SettingsDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES_SETTINGS);
            manageInitialSettings(db, CREATE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            manageInitialSettings(db, UPDATE);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public void manageInitialSettings(SQLiteDatabase settingsDB, int operation){
        //boolean success = true;

        if(operation == CREATE) {
            ContentValues values = new ContentValues();
            long newRowID;

            values.put(Settings.COLUMN_SETTINGS, "disclosure_acceptation");
            values.put(Settings.COLUMN_RELATED_SETTINGS, "false");
            newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);

            values.put(Settings.COLUMN_SETTINGS, "used_quantity");
            values.put(Settings.COLUMN_RELATED_SETTINGS, "0");
            newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);

            values.put(Settings.COLUMN_SETTINGS, "enabled_5");
            values.put(Settings.COLUMN_RELATED_SETTINGS, "false");
            newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);

            values.put(Settings.COLUMN_SETTINGS, "enabled_20");
            values.put(Settings.COLUMN_RELATED_SETTINGS, "false");
            newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);
        }
        else if(operation == UPDATE){
            ContentValues values = new ContentValues();
            long newRowID;

            if(!doesSettingsExist("disclosure_acceptation", settingsDB)) {
                values.put(Settings.COLUMN_SETTINGS, "disclosure_acceptation");
                values.put(Settings.COLUMN_RELATED_SETTINGS, "false");
                newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);
            }

            if(!doesSettingsExist("used_quantity", settingsDB)) {
                values.put(Settings.COLUMN_SETTINGS, "used_quantity");
                values.put(Settings.COLUMN_RELATED_SETTINGS, "0");
                newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);
            }

            if(!doesSettingsExist("enabled_5", settingsDB)) {
                values.put(Settings.COLUMN_SETTINGS, "enabled_5");
                values.put(Settings.COLUMN_RELATED_SETTINGS, "false");
                newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);
            }

            if(!doesSettingsExist("enabled_20", settingsDB)) {
                values.put(Settings.COLUMN_SETTINGS, "enabled_20");
                values.put(Settings.COLUMN_RELATED_SETTINGS, "false");
                newRowID = settingsDB.insert(Settings.TABLE_NAME, null, values);
            }
        }

        //if(newRowID == -1) success = false;
        //return success;
    }

    private boolean doesSettingsExist(String settings, SQLiteDatabase settingsDB){
        String[] projection = {
                Settings.COLUMN_RELATED_SETTINGS
        };
        String selection = Settings.COLUMN_SETTINGS + " = ?";
        String[] selectionArgs = { settings };

        Cursor cursor = settingsDB.query(
                Settings.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        if(cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        else {
            cursor.close();
            return false;
        }
    }

    public String getSettings(String settings){
        settingsDB = settingsDbHelper.getReadableDatabase();
        String[] projection = {
                Settings.COLUMN_RELATED_SETTINGS
        };
        String selection = Settings.COLUMN_SETTINGS + " = ?";
        String[] selectionArgs = { settings };

        Cursor cursor = settingsDB.query(
                Settings.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        String related_settings;
        cursor.moveToFirst();

        if(cursor.getCount() > 0)
            related_settings = cursor.getString(0);
        else
            related_settings = null;

        cursor.close();
        return related_settings;
    }

    public void updateSettings(String settings, String relatedSettings){
        settingsDB = settingsDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Settings.COLUMN_SETTINGS, settings);
        values.put(Settings.COLUMN_RELATED_SETTINGS, relatedSettings);
        String selection = Settings.COLUMN_SETTINGS + " LIKE ?";
        String[] selectionArgs = { settings };
        int count = settingsDB.update(Settings.TABLE_NAME, values, selection, selectionArgs);
    }
}
