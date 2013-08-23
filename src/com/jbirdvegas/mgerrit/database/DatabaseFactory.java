package com.jbirdvegas.mgerrit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/** This class aims to manage and abstract which database is being accessed.
 *  This is a singleton class, we can have only one DBHelper (it is also a
 *   singleton) and we only need one instance of a wdb as we should only
 *   have one database file open at a time. */
public class DatabaseFactory
{
    private static DBHelper dbHelper;
    private static SQLiteDatabase wdb;

    // TODO: Save all the cursors here so they can be re-queried when the
    // gerrit source (database file) changes

    private DatabaseFactory() {
        super();
    }

    public static void getDatabase(Context context, String gerrit)
    {
        String dbName = DBHelper.getDatabaseName(gerrit);
        DatabaseFactory.dbHelper = DBHelper.getInstance(context, gerrit);

        // Ensure the database is open and we have a reference to it before
        //  trying to perform any queries using it.
        DatabaseFactory.wdb = dbHelper.getWritableDatabase();
    }

    public void closeDatabase() {
        dbHelper.shutdown();
        wdb = null;
        dbHelper = null;
    }

    /* -- Get table methods -- */
    public ProjectsTable getProjectsTable() {
        return new ProjectsTable(wdb);
    }

    // This should be called when the Gerrit source changes to modify all database references to
    //  use the new database source.
    public void changeGerrit(Context context, String newGerrit) {

        // Close the database file
        closeDatabase();

        // Reopen the new database
        getDatabase(context, newGerrit);

        // TODO: Re-query data for open cursors?
    }
}
