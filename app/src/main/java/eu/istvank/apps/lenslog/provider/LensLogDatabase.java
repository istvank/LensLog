/*
 * Copyright 2014 istvank.eu
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

package eu.istvank.apps.lenslog.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import eu.istvank.apps.lenslog.provider.LensLogContract.*;

/**
 * The main database class of the LensLog app. Creates, reads, updates and deletes data in the
 * SQLite database.
 *
 * The code is ased on the ideas of
 * https://github.com/google/iosched/blob/master/android/src/main/java/com/google/samples/apps/iosched/provider/ScheduleDatabase.java
 */
public class LensLogDatabase extends SQLiteOpenHelper {

    private static final String TAG = "LensLogDatabase";

    private static final String DATABASE_NAME = "lenslog.db";

    private static final int VERSION_1 = 1; // app version 0.1
    private static final int CUR_DATABASE_VERSION = VERSION_1;

    private final Context mContext;

    /**
     * An interface containing all the tables.
     */
    interface Tables {
        String PACKS = "packs";
        String LENSES = "lenses";
        String DAYSWORN = "daysworn";
    }

    public LensLogDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.PACKS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PacksColumns.EYE + " TEXT NOT NULL,"
                + PacksColumns.LENS_TYPE + " TEXT NOT NULL,"
                + PacksColumns.SPHERE + " TEXT,"
                + PacksColumns.BASE_CURVE + " TEXT,"
                + PacksColumns.DIAMETER + " TEXT,"
                + PacksColumns.CYLINDER + " TEXT,"
                + PacksColumns.AXIS + " TEXT,"
                + PacksColumns.ADD_POWER + " TEXT,"
                + PacksColumns.EXPIRATION_DATE + " INTEGER,"
                + PacksColumns.PURCHASED_DATE + " INTEGER,"
                + PacksColumns.NAME + " TEXT,"
                + PacksColumns.BRAND + " TEXT,"
                + PacksColumns.SHOP + " TEXT)");

        db.execSQL("CREATE TABLE " + Tables.LENSES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + LensesColumns.PACK_ID + " INTEGER NOT NULL,"
                + LensesColumns.TRASH + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + Tables.DAYSWORN + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DaysWornColumns.DATETIME + " INTEGER NOT NULL,"
                + DaysWornColumns.WASWORN + " INTEGER NOT NULL)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // Current DB version. We update this variable as we perform upgrades to reflect
        // the current version we are in.
        int version = oldVersion;

        if (version != CUR_DATABASE_VERSION) {

            // list here all tables to drop
            db.execSQL("DROP TABLE IF EXISTS " + Tables.PACKS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.LENSES);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.DAYSWORN);

            onCreate(db);
            version = CUR_DATABASE_VERSION;
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}