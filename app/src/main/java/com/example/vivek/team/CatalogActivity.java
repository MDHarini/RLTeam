package com.example.vivek.team;


import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.vivek.team.Data.UserContract.UserDB;
import com.example.vivek.team.Data.UserHelper;

/**
 * Displays list of users that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int USER_LODER =0;
    UserCursorAdapter mCursorAdapter;
    private UserHelper mDbHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        // and pass the context, which is the current activity.
        mDbHelper = new UserHelper(this);

        ListView userListView = (ListView) findViewById(R.id.list_item);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
              userListView.setEmptyView(emptyView);

        mCursorAdapter = new UserCursorAdapter(this,null);
        userListView.setAdapter(mCursorAdapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
              Uri currentUserUri = ContentUris.withAppendedId(UserDB.content_URI,id);
              intent.setData(currentUserUri);
              startActivity(intent);
            }
        });

        //kick off loader
        getLoaderManager().initLoader(USER_LODER,null,this);
    }
     private void insert(){
//        // Create and/or open a database to write it
//        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDB.COLUMN_NAME, "Harini");
        values.put(UserDB.COLUMN_USERID, "harini_md");
        values.put(UserDB.COLUMN_GENDER, 2);
        values.put(UserDB.COLUMN_BIRTHDAY, 16);
//        // Insert the new row, returning the primary key value of the new row
//        long newRowId = db.insert(UserDB.TABLE_NAME, null, values);
//        Log.v("CatalogActivity","New row id"+newRowId);
        Uri rowId = getContentResolver().insert(UserDB.content_URI,values);
      }

    private void deleteAllUser() {
               int rowsDeleted = getContentResolver().delete(UserDB.content_URI, null, null);
                Log.v("CatalogActivity", rowsDeleted + " rows deleted from user database");
            }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // INSERT VAULES TO DB
               insert();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // delete users
                deleteAllUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                UserDB._ID,
                UserDB.COLUMN_NAME,
                UserDB.COLUMN_USERID};
        return new CursorLoader(this,UserDB.content_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}

