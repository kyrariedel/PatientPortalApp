package com.bridgecare.app.utility;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_DAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy (EEEE) HH:mm");

    public static String formatDate(LocalDate dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "No date available";
    }

    public static String formatDateDayTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_DAY_TIME_FORMATTER) : "No date, day and time available";
    }

    public static String formatDateDayTime(Long epochMilli) {
        if (epochMilli == null) {
            return "No date, day and time available";
        }
        LocalDateTime dateTime = Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return formatDateDayTime(dateTime);
    }

    public static String parseDate(String month, int day, int year) {
       int monthNumber = getMonthNumber(month);
       return monthNumber + "/" + day + "/" + year;
    }

    private static int getMonthNumber(String month) {
        switch (month.toLowerCase()) {
            case "january":
                return 1;
            case "february":
                return 2;
            case "march":
                return 3;
            case "april":
                return 4;
            case "may":
                return 5;
            case "june":
                return 6;
            case "july":
                return 7;
            case "august":
                return 8;
            case "september":
                return 9;
            case "october":
                return 10;
            case "november":
                return 11;
            case "december":
                return 12;
            default:
                throw new IllegalArgumentException("Invalid month: " + month);
        }
    }
}
