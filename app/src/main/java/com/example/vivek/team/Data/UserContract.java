package com.example.vivek.team.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Vivek on 3/5/2017.
 */

public class UserContract {
    public UserContract(){}
    public static final String contentP = "com.example.vivek.team";
    public static final Uri baseContenturi = Uri.parse("content://"+contentP);
    public static final String path_user = "Users";

    public static final class UserDB implements BaseColumns {
        public static final Uri content_URI = Uri.withAppendedPath(baseContenturi, path_user);
        public static final String TABLE_NAME = "Users";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_USERID = "UserId";
        public static final String COLUMN_GENDER = "Gender";
        public static final String COLUMN_BIRTHDAY = "Birthday";

        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;
        public static final int GENDER_SELECT = 0;

        public static boolean isValidGender(int gender) {
            if (gender == GENDER_MALE || gender == GENDER_FEMALE || gender == GENDER_SELECT) {
                return true;
            }
            return false;
        }
        /**
                  * The MIME type of the {@link } for a list of users.
         +        */
         public static final String CONTENT_LIST_TYPE =
                               ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + contentP + "/" + path_user;

                /*** The MIME type of the {@link } for a single user.
          */
                public static final String CONTENT_ITEM_TYPE =
                               ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + contentP + "/" + path_user;
    }
}
