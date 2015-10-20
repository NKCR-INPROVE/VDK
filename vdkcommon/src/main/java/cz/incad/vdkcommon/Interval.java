package cz.incad.vdkcommon;

import java.util.Calendar;

/**
 *
 * @author alberto
 */
public enum Interval {
    YEAR, MONTH, DAY, HOUR, MINUTE, SECOND;
    public static int parseString(String s) {
        if (s.equalsIgnoreCase("year")) {
            return Calendar.YEAR;
        } else if (s.equalsIgnoreCase("month")) {
            return Calendar.MONTH;
        } else if (s.equalsIgnoreCase("day")) {
            return Calendar.DATE;
        } else if (s.equalsIgnoreCase("hour")) {
            return Calendar.HOUR;
        } else if (s.equalsIgnoreCase("minute")) {
            return Calendar.MINUTE;
        } else if (s.equalsIgnoreCase("second")) {
            return Calendar.SECOND;
        } else {
            throw new RuntimeException("Unsupported interval");
        }
    }
}
