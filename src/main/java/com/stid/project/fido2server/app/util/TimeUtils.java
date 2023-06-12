package com.stid.project.fido2server.app.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public static long nanoTime(Instant instant) {
        if (instant == null)
            instant = Instant.now();
        String timeString = instant.getEpochSecond() + "" + instant.getNano();
        return Long.parseLong(timeString);
    }

    public static long nanoTime() {
        return nanoTime(null);
    }

    public static Instant startOfHour(Instant instant, int nextHours) {
        return startOfHour(instant.toEpochMilli(), nextHours);
    }

    public static Instant startOfHour(long now, int nextHours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.add(Calendar.HOUR, nextHours);
        return cal.getTime().toInstant();
    }

    public static Instant startOfDay(Instant instant) {
        return startOfDay(instant.toEpochMilli());
    }
    public static Instant startOfDay(long now) {
        return startOfDay(now, 0);
    }

    public static Instant startOfDay(long now, int nextDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.add(Calendar.DAY_OF_MONTH, nextDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().toInstant();
    }

    public static Instant endOfDay(Instant instant) {
        return endOfDay(instant.toEpochMilli());
    }
    public static Instant endOfDay(long now) {
        return endOfDay(now, 0);
    }

    public static Instant endOfDay(long now, int nextDays) {
        Instant startOfNextDay = startOfDay(now, nextDays + 1);
        return startOfNextDay.plusNanos(-1);
    }

    public static Instant startOfWeek(long now, int nextWeeks) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.WEEK_OF_YEAR, nextWeeks);
        return cal.getTime().toInstant();
    }

    public static Instant startOfMonth(long now, int nextMonths) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, nextMonths);
        return cal.getTime().toInstant();
    }

    public static Instant startOfQuarter(long now, int nextQuarter) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int month = cal.get(Calendar.MONTH);
        int quarter = month / 3 + 1;

        int monthStartOfQuarter = quarter == 1 ? Calendar.JANUARY : quarter == 2
                ? Calendar.APRIL : quarter == 3 ? Calendar.JULY : Calendar.OCTOBER;

        cal.set(Calendar.MONTH, monthStartOfQuarter);
        cal.add(Calendar.MONTH, nextQuarter * 3);

        return cal.getTime().toInstant();
    }

    public static Instant endOfQuarter(long now) {
        Instant startOfNextQuarter = startOfQuarter(now, 1);
        return startOfNextQuarter.plusNanos(-1);
    }

    public static Instant startOfYear(long now, int nextYears) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));

        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.YEAR, nextYears);
        return cal.getTime().toInstant();
    }

    public static Instant endOfYear(long now) {
        Instant startOfNextYear = startOfYear(now, 1);
        return startOfNextYear.plusNanos(-1);
    }

    public static String formatTimeNow(String format) {
        return formatTime(System.currentTimeMillis(), format);
    }

    public static String formatTime(long milli, String format) {
        try {
            return new SimpleDateFormat(format).format(new Date(milli));
        } catch (Exception e) {
            return Long.toString(milli);
        }
    }
}
