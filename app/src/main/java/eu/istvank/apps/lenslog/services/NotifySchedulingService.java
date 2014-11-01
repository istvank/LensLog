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

package eu.istvank.apps.lenslog.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import eu.istvank.apps.lenslog.R;
import eu.istvank.apps.lenslog.activities.MainActivity;
import eu.istvank.apps.lenslog.receivers.NotifyAlarmReceiver;

/**
 * Created by koren on 12/10/14.
 */
public class NotifySchedulingService extends IntentService {
    public NotifySchedulingService() {
        super("NotifySchedulingService");
    }

    public static final String TAG = "NotifySchedulingService";
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    public static final String NOTIFICATION_WORN = "NOTIFICATION_WORN";

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onHandleIntent(Intent intent) {
        sendNotification("Great!");

        // Release the wake lock provided by the BroadcastReceiver
        NotifyAlarmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        //TODO: if user selects the main notification we should ask the question in the UI
        PendingIntent mainPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(this, MainActivity.class), 0);

        // YES
        Intent yesIntent = new Intent(this, MainActivity.class);
        yesIntent.putExtra(NOTIFICATION_WORN, true);
        yesIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent yesPendingIntent = PendingIntent.getActivity(this, MainActivity.REQUEST_YES,
                yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // NO
        Intent noIntent = new Intent(this, MainActivity.class);
        noIntent.putExtra(NOTIFICATION_WORN, false);
        noIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent noPendingIntent = PendingIntent.getActivity(this, MainActivity.REQUEST_NO,
                noIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(getString(R.string.notification_question))
                        .addAction(R.drawable.ic_done_white, getString(R.string.yes), yesPendingIntent)
                        .addAction(R.drawable.ic_close_white, getString(R.string.no), noPendingIntent);

        mBuilder.setContentIntent(mainPendingIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
