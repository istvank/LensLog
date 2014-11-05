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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import eu.istvank.apps.lenslog.provider.LensLogContract.*;
import eu.istvank.apps.lenslog.provider.LensLogDatabase.Tables;
import eu.istvank.apps.lenslog.util.SelectionBuilder;

/**
 * The main ContentProvider for the LensLog app that provides access to the data saved in the
 * database.
 */
public class LensLogProvider extends ContentProvider {

    private static final String TAG = "ScheduleProvider";

    private LensLogDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static final int PACKAGES = 100;
    public static final int PACKAGES_ID = 101;

    public static final int LENSES = 200;
    public static final int LENSES_ID = 201;

    public static final int DAYSWORN = 300;
    public static final int DAYSWORN_ID = 301;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LensLogContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "packages", PACKAGES);
        matcher.addURI(authority, "packages/*", PACKAGES_ID);

        matcher.addURI(authority, "lenses", LENSES);
        matcher.addURI(authority, "lenses/*", LENSES_ID);

        matcher.addURI(authority, "daysworn", DAYSWORN);
        matcher.addURI(authority, "daysworn/*", DAYSWORN_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new LensLogDatabase(getContext());
        return true;
    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mOpenHelper.close();
        Context context = getContext();
        LensLogDatabase.deleteDatabase(context);
        mOpenHelper = new LensLogDatabase(getContext());
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PACKAGES:
                return Packages.CONTENT_TYPE;
            case PACKAGES_ID:
                return Packages.CONTENT_ITEM_TYPE;
            case LENSES:
                return Lenses.CONTENT_TYPE;
            case LENSES_ID:
                return Lenses.CONTENT_ITEM_TYPE;
            case DAYSWORN:
                return DaysWorn.CONTENT_TYPE;
            case DAYSWORN_ID:
                return DaysWorn.CONTENT_ITEM_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final SelectionBuilder builder = buildSimpleSelection(uri);

        boolean distinct = !TextUtils.isEmpty(
                uri.getQueryParameter(LensLogContract.QUERY_PARAMETER_DISTINCT));

        Cursor cursor = builder
                .where(selection, selectionArgs)
                .query(db, distinct, projection, sortOrder, null);
        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString()
                + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PACKAGES: {
                db.insertOrThrow(Tables.PACKAGES, null, values);
                notifyChange(uri);
                return Packages.buildPackageUri(values.getAsString(Packages._ID));
            }
            case LENSES: {
                db.insertOrThrow(Tables.LENSES, null, values);
                notifyChange(uri);
                return Lenses.buildLensUri(values.getAsString(Lenses._ID));
            }
            case DAYSWORN: {
                db.insertOrThrow(Tables.DAYSWORN, null, values);
                notifyChange(uri);
                return DaysWorn.buildDaysWornUri(values.getAsString(DaysWorn._ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString()
                + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final SelectionBuilder builder = buildSimpleSelection(uri);

        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete(uri=" + uri + ")");
        if (uri == LensLogContract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    private void notifyChange(Uri uri) {
        // We only notify changes if the caller is not the sync adapter.
        // The sync adapter has the responsibility of notifying changes (it can do so
        // more intelligently than we can -- for example, doing it only once at the end
        // of the sync instead of issuing thousands of notifications for each record).
        if (!LensLogContract.hasCallerIsSyncAdapterParameter(uri)) {
            Context context = getContext();
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PACKAGES: {
                return builder.table(Tables.PACKAGES);
            }
            case PACKAGES_ID: {
                final String packageId = Packages.getPackageId(uri);
                return builder.table(Tables.PACKAGES).where(Packages._ID + "=?", packageId);
            }
            case LENSES: {
                return builder.table(Tables.LENSES);
            }
            case LENSES_ID: {
                final String lensId = Lenses.getLensId(uri);
                return builder.table(Tables.LENSES).where(Lenses._ID + "=?", lensId);
            }
            case DAYSWORN: {
                return builder.table(Tables.DAYSWORN);
            }
            case DAYSWORN_ID: {
                final String daysWornId = DaysWorn.getDaysWornId(uri);
                return builder.table(Tables.DAYSWORN).where(DaysWorn._ID + "=?", daysWornId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }
}
