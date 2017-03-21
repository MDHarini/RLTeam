/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.vivek.team;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.vivek.team.Data.UserContract.UserDB;
import com.example.vivek.team.Data.UserHelper;

import static com.example.vivek.team.Data.UserContract.UserDB.GENDER_FEMALE;
import static com.example.vivek.team.Data.UserContract.UserDB.GENDER_MALE;
import static com.example.vivek.team.Data.UserContract.UserDB.GENDER_SELECT;

/**
 * Allows user to create a new user or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private UserHelper mDbHelper;
    private static final int EXISTING_USER_LOADER =0;
    /** EditText field to enter the user's name */
    private EditText mNameEditText;

    /** EditText field to enter the user's id */
    private EditText mUserIdEditText;

    /** EditText field to enter the user birthday */
    private EditText mBirthdayEditText;

    /** EditText field to enter the user's gender */
    private Spinner mGenderSpinner;
    /**
     * Gender of the USER. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = GENDER_SELECT;
    private Uri mCurrentUserUri;

    private boolean mUserHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mUserHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentUserUri = intent.getData();
        if(mCurrentUserUri == null){
            setTitle(getString(R.string.editor_Newuser_title));
            invalidateOptionsMenu();
        }
        else {
            setTitle(getString(R.string.editor_update_title));
            getLoaderManager().initLoader(EXISTING_USER_LOADER, null, this);
        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mUserIdEditText = (EditText) findViewById(R.id.edit_userid);
        mBirthdayEditText = (EditText) findViewById(R.id.edit_birthday);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        
        mNameEditText.setOnTouchListener(mTouchListener);
        mUserIdEditText.setOnTouchListener(mTouchListener);
        mBirthdayEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the user.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = GENDER_FEMALE; // Female
                    }
                    else  {
                        mGender = GENDER_SELECT;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new user, hide the "Delete" menu item.
        if (mCurrentUserUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:
                // insert values from editor page
                saveUser();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the user hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mUserHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the user.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        // If the user hasn't changed, continue with handling back button press
        if (!mUserHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the user.
                deleteUser();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the user.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteUser() {
        if(mCurrentUserUri!=null) {
            int rowsDeleted = getContentResolver().delete(mCurrentUserUri, null, null);
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_user_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_user_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void saveUser() {
        mDbHelper = new UserHelper(this);
        String userName = mNameEditText.getText().toString().trim();
        String userID = mUserIdEditText.getText().toString().trim();
        //int Birthday =Integer.parseInt(
                String Birthday = mBirthdayEditText.getText().toString().trim();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if ((mCurrentUserUri == null) &&
                TextUtils.isEmpty(userName) && TextUtils.isEmpty(userID) && TextUtils.isEmpty(Birthday) )
        {
            return;}
        ContentValues values = new ContentValues();
        values.put(UserDB.COLUMN_NAME, userName);
        values.put(UserDB.COLUMN_USERID, userID);
        values.put(UserDB.COLUMN_GENDER, mGender);
        int BDay = 0;
                if (!TextUtils.isEmpty(Birthday)) {
                    BDay = Integer.parseInt(Birthday);
                   }
        values.put(UserDB.COLUMN_BIRTHDAY, BDay);
        if (mCurrentUserUri == null) {
            // This is a NEW user, so insert a new user into the provider,
            // returning the content URI for the new user.
            Uri newUri = getContentResolver().insert(UserDB.content_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_user_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_user_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
            else {
                int rowsAffected = getContentResolver().update(mCurrentUserUri, values, null, null);
                                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                             // If no rows were affected, then there was an error with the update.
                        Toast.makeText(this, getString(R.string.editor_update_user_failed),
                                Toast.LENGTH_SHORT).show();
                                } else {

                                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_user_successful),
                            Toast.LENGTH_SHORT).show();
                             }
            }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                UserDB._ID,
                UserDB.COLUMN_NAME,
                UserDB.COLUMN_USERID,
                UserDB.COLUMN_GENDER,
                UserDB.COLUMN_BIRTHDAY};

        return new CursorLoader(this,mCurrentUserUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            // Find the columns of user attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(UserDB.COLUMN_NAME);
            int userIdColumnIndex = cursor.getColumnIndex(UserDB.COLUMN_USERID);
            int genderColumnIndex = cursor.getColumnIndex(UserDB.COLUMN_GENDER);
            int birthdayColumnIndex = cursor.getColumnIndex(UserDB.COLUMN_BIRTHDAY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String userId = cursor.getString(userIdColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int birthday = cursor.getInt(birthdayColumnIndex);
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mUserIdEditText.setText(userId);
            mBirthdayEditText.setText(Integer.toString(birthday));
            switch (gender) {
                case GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }
        @Override
        public void onLoaderReset (Loader < Cursor > loader) {
            // If the loader is invalidated, clear out all the data from the input fields.
            mNameEditText.setText("");
            mUserIdEditText.setText("");
            mBirthdayEditText.setText("");
            mGenderSpinner.setSelection(0); // Select "Unknown" gender
        }
    }
