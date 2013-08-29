package com.jbirdvegas.mgerrit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jbirdvegas.mgerrit.Prefs;

/** This class aims to manage and abstract which database is being accessed.
 *  This is a singleton class, we can have only one DBHelper (it is also a
 *   singleton) and we only need one instance of a wdb as we should only
 *   have one database file open at a time. */
public class DatabaseFactory
{
    private static DBHelper dbHelper;
    private static SQLiteDatabase wdb;

    private static DatabaseFactory mInstance = null;
    private Context mContext;
    private static String mCurrentGerrit = null;

    private DatabaseFactory() {
        super();
    }

    public static DatabaseFactory getDatabase(Context context) {
        return getDatabase(context, Prefs.getCurrentGerrit(context));
    }

    public static DatabaseFactory getDatabase(Context context, String gerrit)
    {
        String dbName = DBHelper.getDatabaseName(gerrit);
        DatabaseFactory.dbHelper = DBHelper.getInstance(context, dbName);

        if (mInstance != null && dbName.equals(mCurrentGerrit)) return mInstance;

        // Ensure the database is open and we have a reference to it before
        //  trying to perform any queries using it.
        DatabaseFactory.wdb = dbHelper.getWritableDatabase();

        mInstance = new DatabaseFactory();
        mInstance.mContext = context;
        return mInstance;
    }

    // Not static as this ensures getDatabase was called
    public SQLiteOpenHelper getDatabaseHelper() {
        return dbHelper;
    }

    public SQLiteDatabase getWritableDatabase() {
        return wdb;
    }

    public void closeDatabase() {
        dbHelper.shutdown();
        wdb = null;
        dbHelper = null;
    }

    /* -- Get table methods -- */
    public ProjectsTable getProjectsTable() {
        return new ProjectsTable(wdb, this, mContext);
    }

    // This should be called when the Gerrit source changes to modify all database references to
    //  use the new database source.
    public void changeGerrit(Context context, String newGerrit) {

        // Close the database file
        closeDatabase();

        // Reopen the new database
        getDatabase(context, newGerrit);
    }
}
