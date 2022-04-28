package cronparser;

import java.util.HashMap;
import java.util.Map;

import static cronparser.Constants.*;

public class Utils {

    protected static final Map<String, Integer> monthMap = new HashMap<>(12);
    protected static final Map<String, Integer> dayMap = new HashMap<>(7);
    static {
        monthMap.put("JAN", 0);
        monthMap.put("FEB", 1);
        monthMap.put("MAR", 2);
        monthMap.put("APR", 3);
        monthMap.put("MAY", 4);
        monthMap.put("JUN", 5);
        monthMap.put("JUL", 6);
        monthMap.put("AUG", 7);
        monthMap.put("SEP", 8);
        monthMap.put("OCT", 9);
        monthMap.put("NOV", 10);
        monthMap.put("DEC", 11);

        dayMap.put("SUN", 1);
        dayMap.put("MON", 2);
        dayMap.put("TUE", 3);
        dayMap.put("WED", 4);
        dayMap.put("THU", 5);
        dayMap.put("FRI", 6);
        dayMap.put("SAT", 7);
    }

    public static int getMonthNumber(String s) {
        Integer integer = monthMap.get(s);

        if (integer == null) {
            return -1;
        }
        return integer;
    }

    public static int getDayOfWeekNumber(String s) {
        Integer integer = dayMap.get(s);
        if (integer == null) {
            return -1;
        }
        return integer;
    }

    public static PosSet getValue(int v, String s, int i) {
        char c = s.charAt(i);
        StringBuilder s1 = new StringBuilder(String.valueOf(v));
        while (c >= '0' && c <= '9') {
            s1.append(c);
            i++;
            if (i >= s.length()) {
                break;
            }
            c = s.charAt(i);
        }
        PosSet val = new PosSet();
        val.pos = (i < s.length()) ? i : i + 1;
        val.val = Integer.parseInt(s1.toString());
        return val;
    }

    public static int getMax(int type) {
        int max = -1;
        switch (type) {
            case MINUTE:
                max = 60;
                break;
            case HOUR:
                max = 24;
                break;
            case MONTH:
                max = 12;
                break;
            case DAY_OF_WEEK:
                max = 7;
                break;
            case DAY_OF_MONTH:
                max = 31;
                break;
            default:
                throw new IllegalArgumentException("Unexpected type encountered");
        }

        return max;
    }
}
