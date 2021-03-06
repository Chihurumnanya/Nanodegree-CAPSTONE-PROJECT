package com.chihurumnanya.itransmit.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.chihurumnanya.itransmit.R;
import com.chihurumnanya.itransmit.activities.MainActivity;
import com.chihurumnanya.itransmit.activities.TripDetailsActivity;
import com.chihurumnanya.itransmit.models.BudgetModel;
import com.chihurumnanya.itransmit.models.TripModel;


/**
 * Helper class with static helper methods for notifications
 */
public class NotificationUtils {

    private static final int BUDGET_LIMIT_EXCEEDED_NOTIFICATION_START_ID = 1;
    private static final String DEFAUKT_NOTIFICATION_CHANNEL = "DEFAUKT_NOTIFICATION_CHANNEL";

    public static void notifyBudgetLimitExceeded(Context context, BudgetModel budget,
                                                 TripModel trip) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, DEFAUKT_NOTIFICATION_CHANNEL);

        Intent tripDetails = new Intent(context, TripDetailsActivity.class);
        tripDetails.putExtra(Constants.Extra.EXTRA_TRIP, (Parcelable) trip);
        tripDetails.putExtra(Constants.Extra.EXTRA_TRIP_DETAILS_TAB_INDEX,
                TripDetailsActivity.TAB_BUDGETS_POSITION);
        Intent mainActivityIntent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent =
                TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(mainActivityIntent)
                        .addNextIntent(tripDetails)
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setSmallIcon(getNotificationIcon())
                .setContentTitle(budget.getTitle())
                .setContentText(context.getString(R.string.budget_limit_exceeded))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Do not replace previous budget notification
        int nextNotificationId = Utils.getIntFromSharedPrefs(context,
                Constants.Preference.PREFERENCE_LAST_BUDGET_NOTIFICATION_ID,
                BUDGET_LIMIT_EXCEEDED_NOTIFICATION_START_ID) + 1;

        Utils.saveIntToSharedPrefs(context,
                Constants.Preference.PREFERENCE_LAST_BUDGET_NOTIFICATION_ID,
                nextNotificationId, true);

        mNotificationManager.notify(nextNotificationId, mBuilder.build());
    }

    // Based on https://stackoverflow.com/a/29207365
    private static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build
                .VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notification : R.mipmap.ic_launcher;
    }
}
