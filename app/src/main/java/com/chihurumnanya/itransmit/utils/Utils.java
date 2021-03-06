package com.chihurumnanya.itransmit.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.chihurumnanya.itransmit.models.BudgetModel;
import com.chihurumnanya.itransmit.models.DestinationModel;
import com.chihurumnanya.itransmit.models.ExpenseModel;
import com.chihurumnanya.itransmit.models.TravelerModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


/**
 * Helper class with static common helper methods
 */
public class Utils {

    public static String getFormattedStartEndTripDateText(long startDateMillis,
                                                          long endDateMillis) {
        Calendar startDateCalendar = Calendar.getInstance(Locale.getDefault());
        startDateCalendar.setTimeInMillis(startDateMillis);
        Calendar endDateCalendar = Calendar.getInstance(Locale.getDefault());
        endDateCalendar.setTimeInMillis(endDateMillis);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        return dateFormat.format(startDateCalendar.getTime()) +
                " ~ " +
                dateFormat.format(endDateCalendar.getTime());
    }

    public static String getFormattedStartEndShortTripDateText(long startDateMillis,
                                                               long endDateMillis) {
        Calendar startDateCalendar = Calendar.getInstance(Locale.getDefault());
        startDateCalendar.setTimeInMillis(startDateMillis);
        Calendar endDateCalendar = Calendar.getInstance(Locale.getDefault());
        endDateCalendar.setTimeInMillis(endDateMillis);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return dateFormat.format(startDateCalendar.getTime()) +
                " ~ " +
                dateFormat.format(endDateCalendar.getTime());
    }

    public static String getFormattedTripDateText(long dateMillis) {
        Calendar dateCalendar = Calendar.getInstance(Locale.getDefault());
        dateCalendar.setTimeInMillis(dateMillis);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return dateFormat.format(dateCalendar.getTime());
    }

    public static String getFormattedDateText(long dateMillis) {
        Calendar startDateCalendar = Calendar.getInstance(Locale.getDefault());
        startDateCalendar.setTimeInMillis(dateMillis);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        return dateFormat.format(startDateCalendar.getTime());
    }


    public static String getFormattedTravelersText(Map<String, TravelerModel> travelers) {
        StringBuilder travelersString = new StringBuilder();
        int count_for_comma = 0;
        for (Map.Entry entry : travelers.entrySet()) {
            TravelerModel traveler = (TravelerModel) entry.getValue();
            travelersString.append(traveler.getName());
            if (++count_for_comma < travelers.size()) {
                travelersString.append(", ");
            }
        }

        return travelersString.toString();
    }

    public static void saveBitmapToInternalStorage(Context context, Bitmap bitmapImage,
                                                   String directory, String fileName) {
        FileOutputStream fos = null;
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File photoFile = new File(directoryFile, fileName);
        try {
            fos = new FileOutputStream(photoFile);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            Timber.e("IOException while saving bitmap to internal storage");
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Timber.e("IOException while saving bitmap to internal storage");
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getBitmapFromInternalStorage(Context context, String directory, String
            fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File photoFile = new File(directoryFile, fileName);

        if (photoFile.exists()) {
            return BitmapFactory.decodeFile(photoFile.getAbsolutePath());

        }

        return null;
    }

    public static boolean deleteFileFromInternalStorage(Context context, String directory,
                                                        String fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File file = new File(directoryFile, fileName);
        boolean isDeleted = false;

        try {
            isDeleted = file.delete();
        } catch (SecurityException e) {
            Timber.e("SecurityException while deleting bitmap from internal storage");
            e.printStackTrace();
        }

        return isDeleted;
    }

    public static boolean deleteFolderFromInternalStorage(Context context, String directory) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        if (directoryFile.isDirectory()) {
            boolean isDeleted = false;
            for (File childFiled : directoryFile.listFiles()) {
                isDeleted = childFiled.delete();
            }
            return isDeleted;
        } else {
            return false;
        }
    }

    public static boolean isFileExists(Context context, String directory, String fileName) {
        ContextWrapper cw = new ContextWrapper(context);
        File directoryFile = cw.getDir(directory, Context.MODE_PRIVATE);
        File file = new File(directoryFile, fileName);
        return file.exists();
    }

    public static void saveBooleanToSharedPrefs(Context context, String key, boolean value,
                                                boolean isImmediate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        if (isImmediate) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static void deleteSharedPrefs(Context context, String key, boolean isImmediate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(key);
        if (isImmediate) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static boolean getBooleanFromSharedPrefs(Context context, String key,
                                                    boolean defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(key, defaultValue);
    }

    public static void saveStringToSharedPrefs(Context context, String key, String value, boolean
            isImmediate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        if (isImmediate) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static String getStringFromSharedPrefs(Context context, String key, String
            defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(key, defaultValue);
    }

    public static void saveIntToSharedPrefs(Context context, String key, int value, boolean
            isImmediate) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        if (isImmediate) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static int getIntFromSharedPrefs(Context context, String key, int defaultValue) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getInt(key, defaultValue);
    }

    // Based on https://stackoverflow.com/a/24397810/2983102
    public static String getRestrictedDecimal(String str, int max_digits_before_point,
                                              int max_decimal_digits) {
        if (str.charAt(0) == '.') str = "0" + str;
        int max = str.length();

        String rFinal = "";
        boolean after = false;
        int i = 0, up = 0, decimal = 0;
        char t;
        while (i < max) {
            t = str.charAt(i);
            if (t != '.' && !after) {
                up++;
                if (up > max_digits_before_point) return rFinal;
            } else if (t == '.') {
                after = true;
            } else {
                decimal++;
                if (decimal > max_decimal_digits)
                    return rFinal;
            }
            rFinal = rFinal + t;
            i++;
        }
        return rFinal;
    }

    public static String getCurrencySymbol(String countryCode) {
        String currencySymbol = "";
        Locale locale;
        Currency currency = null;
        try {
            locale = new Locale("", countryCode);
            currency = Currency.getInstance(locale);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (currency != null) {
            currencySymbol = currency.getCurrencyCode();
        }

        return currencySymbol;
    }

    // Helper method that returns if an specific expense should be part of a specific budget
    // This method can grow to include other validations, such as: payment method, label,
    // category, etc
    public static boolean isBudgetExpense(@NonNull BudgetModel budget, @NonNull ExpenseModel
            expense) {
        return TextUtils.equals(budget.getCurrency(), expense.getCurrency())
                && TextUtils.equals(budget.getCountry(), expense.getCountry());
    }

    public static Uri getDestinationMapUri(DestinationModel destination) {
        String encodedQuery = "";

        try {
            encodedQuery = URLEncoder.encode(destination.getName(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return Uri.parse("https://www.google.com/maps/search/?api=1")
                .buildUpon()
                .appendQueryParameter("query", encodedQuery)
                .appendQueryParameter("query_place_id", destination.getId())
                .build();
    }

    public static String getUrlFromHtml(String html) {
        String url = "";
        try {
            Document doc = Jsoup.parse(html);
            Element link = null;
            if (doc != null) {
                link = doc.select("a").first();
            }
            if (link != null) {
                url = link.attr("abs:href");
            }
        } catch (Selector.SelectorParseException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static void updateAppWidgets(Context context, String tripId, boolean isDeleted) {
        Intent updateWidgetsIntent = new Intent(isDeleted
                ? Constants.Action.ACTION_APPWIDGET_TRIP_DELETED
                : Constants.Action.ACTION_APPWIDGET_TRIP_UPDATED)
                .setPackage(context.getPackageName());
        updateWidgetsIntent.putExtra(Constants.Extra.EXTRA_TRIP_ID, tripId);
        context.sendBroadcast(updateWidgetsIntent);
    }

    public static void updateAppWidget(Context context, String tripId, int appWidgetId,
                                       boolean isDeleted) {
        Intent updateWidgetIntent = new Intent(isDeleted
                ? Constants.Action.ACTION_APPWIDGET_TRIP_DELETED
                : Constants.Action.ACTION_APPWIDGET_TRIP_UPDATED)
                .setPackage(context.getPackageName());

        updateWidgetIntent.putExtra(Constants.Extra.EXTRA_TRIP_ID, tripId);
        updateWidgetIntent.putExtra(Constants.Extra.EXTRA_APPWIDGET_ID, appWidgetId);
        context.sendBroadcast(updateWidgetIntent);
    }

    public static void signOutAppWidgets(Context context) {
        Intent updateWidgetIntent = new Intent(Constants.Action.ACTION_APPWIDGET_SIGN_OUT)
                .setPackage(context.getPackageName());
        context.sendBroadcast(updateWidgetIntent);
    }
}
