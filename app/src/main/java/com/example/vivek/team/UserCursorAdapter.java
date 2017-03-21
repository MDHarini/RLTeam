package com.example.vivek.team;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import static com.example.vivek.team.Data.UserContract.UserDB.COLUMN_NAME;
import static com.example.vivek.team.Data.UserContract.UserDB.COLUMN_USERID;

/**
 * Created by Vivek on 3/19/2017.
 */

public class UserCursorAdapter extends CursorAdapter {
    public UserCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView summary = (TextView) view.findViewById(R.id.summary);
        String cName = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        String cSummary = cursor.getString(cursor.getColumnIndex(COLUMN_USERID));
        if (TextUtils.isEmpty(cSummary)) {
            cSummary = context.getString(R.string.unknown_breed);
        }
        name.setText(cName);
        summary.setText(cSummary);
    }
}
