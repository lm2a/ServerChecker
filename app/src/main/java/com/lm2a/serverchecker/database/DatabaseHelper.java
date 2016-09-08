package com.lm2a.serverchecker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lm2a.serverchecker.model.Email;
import com.lm2a.serverchecker.model.Host;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "sites";

    // Table Names
    private static final String TABLE_HOSTS = "hosts";
    private static final String TABLE_EMAILS = "emails";
    private static final String TABLE_HOSTS_EMAILS = "hosts_emails";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // NOTES Table - column nmaes
    private static final String KEY_URL = "url";
    private static final String KEY_PORT = "port";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_EMAIL = "email";


    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_HOSTS = "CREATE TABLE "
            + TABLE_HOSTS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_URL + " TEXT,"
            + KEY_PORT + " TEXT,"
            + KEY_NOTIFICATION + " INTEGER,"
            + KEY_EMAIL + " INTEGER,"
            + KEY_CREATED_AT
            + " DATETIME" + ")";

    // EMAIL Table - column names
    private static final String KEY_EMAIL_ADDRESS = "email_address";

    // EMAIL table create statement
    private static final String CREATE_TABLE_EMAIL = "CREATE TABLE "
            + TABLE_EMAILS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_EMAIL_ADDRESS + " TEXT,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    // HOSTS_EMAILS Table - column names
    private static final String KEY_HOST_ID = "host_id";
    private static final String KEY_EMAIL_ID = "email_id";

    // HOSTS_EMAILS table create statement
    private static final String CREATE_TABLE_HOSTS_EMAILS = "CREATE TABLE "
            + TABLE_HOSTS_EMAILS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_HOST_ID + " INTEGER,"
            + KEY_EMAIL_ID + " INTEGER,"
            + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_HOSTS);
        db.execSQL(CREATE_TABLE_EMAIL);
        db.execSQL(CREATE_TABLE_HOSTS_EMAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_HOSTS);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_EMAIL);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_HOSTS_EMAILS);

        // create new tables
        onCreate(db);
    }

    // ------------------------ "hosts" table methods ----------------//

    /**
     * Creating a todo
     */
    public long createHost(Host host) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_URL, host.getHost());
        values.put(KEY_PORT, host.getPort());
        values.put(KEY_NOTIFICATION, host.isNotification()?1:0);
        values.put(KEY_EMAIL, host.isEmails()?1:0);
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long host_id = db.insert(TABLE_HOSTS, null, values);

        return host_id;
    }

    /**
     * get single todo
     */
    public Host getHost(long host_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_HOSTS + " WHERE "
                + KEY_ID + " = " + host_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Host td = new Host();
        td.setId(c.getInt(c.getColumnIndex(KEY_ID)));
        td.setHost(c.getString(c.getColumnIndex(KEY_URL)));
        td.setPort(c.getString(c.getColumnIndex(KEY_PORT)));
        boolean b = (c.getInt(c.getColumnIndex(KEY_NOTIFICATION))==1)?true:false;
        td.setNotification(b);
        //td.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

        return td;
    }

    /**
     * getting all todos
     * */
    public List<Host> getAllHosts() {
        List<Host> hosts = new ArrayList<Host>();
        String selectQuery = "SELECT  * FROM " + TABLE_HOSTS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Host ht = new Host();
                ht.setId(c.getInt(c.getColumnIndex(KEY_ID)));
                ht.setHost(c.getString(c.getColumnIndex(KEY_URL)));
                ht.setPort(c.getString(c.getColumnIndex(KEY_PORT)));
                boolean b = (c.getInt(c.getColumnIndex(KEY_NOTIFICATION))==1)?true:false;
                ht.setNotification(b);
                // adding to todo list
                hosts.add(ht);
            } while (c.moveToNext());
        }

        return hosts;
    }

/*
    */
/**
     * getting all todos under single tag
     * *//*

    public List<Todo> getAllToDosByTag(String tag_name) {
        List<Todo> todos = new ArrayList<Todo>();

        String selectQuery = "SELECT  * FROM " + TABLE_TODO + " td, "
                + TABLE_TAG + " tg, " + TABLE_TODO_TAG + " tt WHERE tg."
                + KEY_TAG_NAME + " = '" + tag_name + "'" + " AND tg." + KEY_ID
                + " = " + "tt." + KEY_TAG_ID + " AND td." + KEY_ID + " = "
                + "tt." + KEY_TODO_ID;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Todo td = new Todo();
                td.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                td.setNote((c.getString(c.getColumnIndex(KEY_TODO))));
                td.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

                // adding to todo list
                todos.add(td);
            } while (c.moveToNext());
        }

        return todos;
    }
*/

    /**
     * getting todo count
     */
    public int getHostsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_HOSTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a todo
     */
    public int updateHost(Host host) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_URL, host.getHost());
        values.put(KEY_PORT, host.getPort());
        values.put(KEY_NOTIFICATION, host.isNotification()?1:0);
        values.put(KEY_EMAIL, host.isEmails()?1:0);
        values.put(KEY_CREATED_AT, getDateTime());


        // updating row
        return db.update(TABLE_HOSTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(host.getId()) });
    }

    /**
     * Deleting a todo
     */
    public void deleteHost(long host_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HOSTS, KEY_ID + " = ?",
                new String[] { String.valueOf(host_id) });
    }


    // ------------------------ "EMAILS" table methods ----------------//

    /**
     * Creating tag
     */
    public long createEmail(Email email) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL_ADDRESS, email.getEmail());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long tag_id = db.insert(TABLE_EMAILS, null, values);

        return tag_id;
    }

    /**
     * getting all tags
     * */
    public List<Email> getAllEmails() {
        List<Email> emails = new ArrayList<Email>();
        String selectQuery = "SELECT  * FROM " + TABLE_EMAILS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Email t = new Email();
                t.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                t.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL_ADDRESS)));
                // adding to tags list
                emails.add(t);
            } while (c.moveToNext());
        }
        return emails;
    }

    /**
     * Updating a tag
     */
    public int updateEmail(Email email) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL_ADDRESS, email.getEmail());

        // updating row
        return db.update(TABLE_EMAILS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(email.getId()) });
    }

    /**
     * Deleting a tag
     */
    public void deleteEmail(Email tag) {
        SQLiteDatabase db = this.getWritableDatabase();
         // now delete the tag
        db.delete(TABLE_EMAILS, KEY_ID + " = ?",
                new String[] { String.valueOf(tag.getId()) });
    }


    // ------------------------ "hosts_emails" table methods ----------------//

    /**
     * Creating todo_tag
     */
    public long createHostEmail(long host_id, long email_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HOST_ID, host_id);
        values.put(KEY_EMAIL_ID, email_id);
        values.put(KEY_CREATED_AT, getDateTime());

        long id = db.insert(TABLE_HOSTS_EMAILS, null, values);

        return id;
    }



    /**
     * Updating a todo tag
     */
    public int updateHostEmail(long id, long email_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL_ID, email_id);

        // updating row
        return db.update(TABLE_HOSTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    /**
     * Deleting a todo tag
     */
    public void deleteHostEmail(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HOSTS_EMAILS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }


    /**
     * get single todo
     */
    public List<Email> getEmailsByHost(String url) {
        List<Email> emails = new ArrayList<Email>();

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_HOSTS + " ht, "+ TABLE_EMAILS + " el, "+ TABLE_HOSTS_EMAILS + " he "
                + "WHERE "
                + "ht."+KEY_URL+" = '"+url+ "'"
                + " AND " + "he." + KEY_HOST_ID + " = " + "ht." +  KEY_ID
                + " AND " + "he." + KEY_EMAIL_ID + " = " + "el." + KEY_ID;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Email email = new Email();
                email.setEmail(c.getString((c.getColumnIndex(KEY_EMAIL_ADDRESS))));
                // adding to todo list
                emails.add(email);
            } while (c.moveToNext());
        }
        return emails;
    }

    /**
     * get datetime
     * */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}