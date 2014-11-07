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

import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

/**
 * The Contract class for the database columns of the LensLog app. Contains all column names of all
 * tables.
 */
public class LensLogContract {

    /**
     * Query parameter to create a distinct query.
     */
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";

    public interface PackagesColumns {
        String EYE = "eye";
        String LENS_TYPE = "lens_type";
        String CONTENT = "content";
        String REMAINING = "remaining";
        String REPLACEMENT_VALUE = "replacement_value";
        String REPLACEMENT_PERIOD = "replacement_period";
        String SPHERE = "sphere";
        String BASE_CURVE = "base_curve";
        String DIAMETER = "diameter";
        String CYLINDER = "cylinder";
        String AXIS = "axis";
        String ADD_POWER = "add_power";
        String EXPIRATION_DATE = "expiration_date";
        String PURCHASED_DATE = "purchased_date";
        String NAME = "name";
        String BRAND = "brand";
        String SHOP = "shop";
    }

    public interface LensesColumns {
        String PACKAGE_ID = "package_id";
        String TRASH = "trash";
    }

    public interface DaysWornColumns {
        String DATETIME = "datetime";
        String WASWORN = "was_worn";
    }

    public static final String CONTENT_AUTHORITY = "eu.istvank.apps.lenslog";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PACKAGES = "packages";
    public static final String PATH_LENSES = "lenses";
    private static final String PATH_DAYSWORN = "daysworn";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_PACKAGES,
            PATH_LENSES,
            PATH_DAYSWORN
    };

    public static class Packages implements BaseColumns, PackagesColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PACKAGES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.lenslog.packages";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.lenslog.packages";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " DESC, ";

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static Uri buildPackageUri(String packageId) {
            return CONTENT_URI.buildUpon().appendPath(packageId).build();
        }

        /** Read {@link #_ID} from {@link Packages} {@link Uri}. */
        public static String getPackageId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Lenses implements BaseColumns, LensesColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LENSES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.lenslog.lenses";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.lenslog.lenses";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " DESC, ";

        /** Build {@link Uri} for requested {@link #_ID}. */
        public static Uri buildLensUri(String lensId) {
            return CONTENT_URI.buildUpon().appendPath(lensId).build();
        }

        /** Read {@link #_ID} from {@link Lenses} {@link Uri}. */
        public static String getLensId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class DaysWorn implements BaseColumns, DaysWornColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DAYSWORN).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.lenslog.daysworn";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.lenslog.daysworn";

        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = DaysWornColumns.DATETIME + " ASC, ";

        /**
         * Build {@link Uri} for requested {@link #_ID}.
         */
        public static Uri buildDaysWornUri(String daysWornId) {
            return CONTENT_URI.buildUpon().appendPath(daysWornId).build();
        }

        /** Read {@link #_ID} from {@link DaysWorn} {@link Uri}. */
        public static String getDaysWornId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }

    private LensLogContract() {
    }

}
