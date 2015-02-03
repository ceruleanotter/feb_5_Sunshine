/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.

    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The inputted date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Date todayDate = new Date();
        Date inputDate = new Date(dateInMillis);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (checkSameDay(inputDate, todayDate)) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, inputDate)));
        } else {
            Calendar calFuture = Calendar.getInstance();
            Calendar calInput = Calendar.getInstance();
            calFuture.setTime(todayDate);
            calFuture.add(Calendar.DATE, 7);
            calInput.setTime(inputDate);

            if (calInput.compareTo(calFuture) < 0) {
                // If the input date is less than a week in the future, just return the day name.
                return getDayName(context, inputDate);
            } else {
                // Otherwise, use the form "Mon Jun 3"
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(inputDate);
            }
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param inputDate The Date in question
     * @return
     */
    public static String getDayName(Context context, Date inputDate) {
        Date todayDate = new Date();
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.
        if (checkSameDay(todayDate, inputDate)) {
            return context.getString(R.string.today);
        } else {
            // If the date is set for tomorrow, the format is "Tomorrow".
            Calendar cal = Calendar.getInstance();
            cal.setTime(todayDate);
            cal.add(Calendar.DATE, 1);
            Date tomorrowDate = cal.getTime();
            if (checkSameDay(tomorrowDate, inputDate)) {
                return context.getString(R.string.tomorrow);
            } else {
                // Otherwise, the format is just the day of the week (e.g "Wednesday".
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                return dayFormat.format(inputDate);
            }
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param inputDate The Date in question
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, Date inputDate) {
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(inputDate);
        return monthDayString;
    }

    /**
     * Compares date equality based on day by converting to a string format that only has
     * granularity down to the day and then compares the two.
     *
     * @param dateOne The first date to compare
     * @param dateTwo The second date to compare
     * @return A boolean as to whether the two dates were on the same day.
     */
    public static boolean checkSameDay(Date dateOne, Date dateTwo) {
        SimpleDateFormat compareFormat = new SimpleDateFormat(DATE_FORMAT);
        return compareFormat.format(dateOne).
                equals(compareFormat.format(dateTwo));
    }
}