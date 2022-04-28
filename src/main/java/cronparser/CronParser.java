package cronparser;

import java.util.*;
import static cronparser.Constants.*;
import static cronparser.Utils.*;


public final class CronParser {

    private final String cronExpression;
    private HashMap<Integer, TreeSet> map;
    private TreeSet<Integer> minutes;
    private TreeSet<Integer> hours;
    private TreeSet<Integer> daysOfMonth;
    private TreeSet<Integer> months;
    private TreeSet<Integer> daysOfWeek;
    private boolean lastdayOfWeek = false;
    private int nthdayOfWeek = 0;
    private boolean lastdayOfMonth = false;
    private boolean nearestWeekday = false;
    private int lastdayOffset = 0;

    
    public CronParser(String cronExpression) throws InvalidInputException {
        if (Objects.isNull(cronExpression)) {
            throw new InvalidInputException("cronExpression cannot be null", 0);
        }
        map = new HashMap<>();
        if (minutes == null) {
            minutes = new TreeSet<>();
            map.put(MINUTE, minutes);
        }
        if (hours == null) {
            hours = new TreeSet<>();
            map.put(HOUR, hours);
        }
        if (daysOfMonth == null) {
            daysOfMonth = new TreeSet<>();
            map.put(DAY_OF_MONTH, daysOfMonth);
        }
        if (months == null) {
            months = new TreeSet<>();
            map.put(MONTH, months);
        }
        if (daysOfWeek == null) {
            daysOfWeek = new TreeSet<>();
            map.put(DAY_OF_WEEK, daysOfWeek);
        }
        this.cronExpression = cronExpression.toUpperCase(Locale.US);
        parse(this.cronExpression);
    }

    protected void parse(String expression) throws InvalidInputException {
        try {
            int currExpression = MINUTE;
            StringTokenizer expressionTokens = new StringTokenizer(expression, DELIMITER,false);
            while (expressionTokens.hasMoreTokens() && currExpression <= DAY_OF_WEEK) {
                String expr = expressionTokens.nextToken().trim();
                if(currExpression == DAY_OF_MONTH && expr.indexOf('L') != -1 && expr.length() > 1 && expr.contains(",")) {
                    throw new InvalidInputException("'L' and 'LW' with other days of the month is not implemented", -1);
                }
                if(currExpression == DAY_OF_WEEK && expr.indexOf('L') != -1 && expr.length() > 1  && expr.contains(",")) {
                    throw new InvalidInputException("'L' with other days of the week is not implemented", -1);
                }
                if(currExpression == DAY_OF_WEEK && expr.indexOf('#') != -1 && expr.indexOf('#', expr.indexOf('#') +1) != -1) {
                    throw new InvalidInputException("multiple \"nth\" days is not implemented.", -1);
                }

                StringTokenizer vTok = new StringTokenizer(expr, ",");
                while (vTok.hasMoreTokens()) {
                    String v = vTok.nextToken();
                    storeExpressionValues(0, v, currExpression);
                }
                currExpression++;
            }

            if (currExpression <= DAY_OF_WEEK) {
                throw new InvalidInputException("Unexpected end of expression.", expression.length());
            }
            
        } catch (InvalidInputException pe) {
            throw pe;
        } catch (Exception e) {
            throw new InvalidInputException("Illegal cron expression format ("+ e + ")", 0);
        }
    }

    protected int storeExpressionValues(int pos, String s, int type) throws InvalidInputException {
        int incr = 0;
        int i = skipWhiteSpace(pos, s);
        if (i >= s.length()) {
            return i;
        }
        char charAtpos = s.charAt(i);
        if ((charAtpos >= 'A') && (charAtpos <= 'Z') && (!s.equals("L")) && (!s.equals("LW")) && (!s.matches(REGEX_INVALID_INPUT))) {
            String sub = s.substring(i, i + 3);
            int sval = -1;
            int eval = -1;
            if (type == MONTH) {
                sval = getMonthNumber(sub) + 1;
                if (sval <= 0) {
                    throw new InvalidInputException("Invalid Month value: '" + sub + "'", i);
                }
                if (s.length() > i + 3) {
                    charAtpos = s.charAt(i + 3);
                    if (charAtpos == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getMonthNumber(sub) + 1;
                        if (eval <= 0) {
                            throw new InvalidInputException("Invalid Month value: '" + sub + "'", i);
                        }
                    }
                }
            }
            else if (type == DAY_OF_WEEK) {
                sval = getDayOfWeekNumber(sub);
                if (sval < 0) {
                    throw new InvalidInputException("Invalid Day-of-Week value: '"+ sub + "'", i);
                }
                if (s.length() > i + 3) {
                    charAtpos = s.charAt(i + 3);
                    if (charAtpos == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getDayOfWeekNumber(sub);
                        if (eval < 0) {
                            throw new InvalidInputException("Invalid Day-of-Week value: '" + sub+ "'", i);
                        }
                    } else if (charAtpos == '#') {
                        try {
                            i += 4;
                            nthdayOfWeek = Integer.parseInt(s.substring(i));
                            if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            throw new InvalidInputException("A numeric value between 1 and 5 must follow the '#' option", i);
                        }
                    } else if (charAtpos == 'L') {
                        this.lastdayOfWeek = true;
                        i++;
                    }
                }

            }
            else {
                throw new InvalidInputException("Illegal characters for this position: '" + sub + "'", i);
            }
            if (eval != -1) {
                incr = 1;
            }
            populateSet(sval, eval, incr, type);
            return (i + 3);
        }

        if (charAtpos == '?') {
            i++;
            if ((i + 1) < s.length()
                    && (s.charAt(i) != ' ' && s.charAt(i + 1) != '\t')) {
                throw new InvalidInputException("Illegal character after '?': " + s.charAt(i), i);
            }
            if (type != DAY_OF_WEEK && type != DAY_OF_MONTH) {
                throw new InvalidInputException("'?' can only be specified for Day-of-Month or Day-of-Week.", i);
            }
            if (type == DAY_OF_WEEK && !lastdayOfMonth) {
                int val = daysOfMonth.last();
                if (val == SPECIAL_CHAR_ANY_INT) {
                    throw new InvalidInputException("'?' can only be specified for Day-of-Month -OR- Day-of-Week.", i);
                }
            }

            populateSet(SPECIAL_CHAR_ANY_INT, -1, 0, type);
            return i;
        }

        if (charAtpos == '*' || charAtpos == '/') {
            if (charAtpos == '*' && (i + 1) >= s.length()) {
                populateSet(SPECIAL_CHAR_ALL, -1, incr, type);
                return i + 1;
            } else if (charAtpos == '/'
                    && ((i + 1) >= s.length() || s.charAt(i + 1) == ' ' || s
                    .charAt(i + 1) == '\t')) {
                throw new InvalidInputException("'/' must be followed by an integer.", i);
            } else if (charAtpos == '*') {
                i++;
            }
            charAtpos = s.charAt(i);
            if (charAtpos == '/') { // is an increment specified?
                i++;
                if (i >= s.length()) {
                    throw new InvalidInputException("Unexpected end of string.", i);
                }
                incr = getNumericValue(s, i);
                i++;
                if (incr > 10) {
                    i++;
                }
                if(isIncrementRangeValid(incr, type, i)) {
                    throw new InvalidInputException("Increment cannot be > "+getMax(type)+" " + incr, i);
                }
            } else {
                incr = 1;
            }

            populateSet(SPECIAL_CHAR_ALL, -1, incr, type);
            return i;
        } else if (charAtpos == 'L') {
            i++;
            if (type == DAY_OF_MONTH) {
                lastdayOfMonth = true;
            }
            if (type == DAY_OF_WEEK) {
                populateSet(7, 7, 0, type);
            }
            if(type == DAY_OF_MONTH && s.length() > i) {
                charAtpos = s.charAt(i);
                if(charAtpos == '-') {
                    PosSet vs = getValue(0, s, i+1);
                    lastdayOffset = vs.val;
                    if(lastdayOffset > 30)
                        throw new InvalidInputException("Offset from last day must be <= 30", i+1);
                    i = vs.pos;
                }
                if(s.length() > i) {
                    charAtpos = s.charAt(i);
                    if(charAtpos == 'W') {
                        nearestWeekday = true;
                        i++;
                    }
                }
            }
            return i;
        } else if (charAtpos >= '0' && charAtpos <= '9') {
            int val = Integer.parseInt(String.valueOf(charAtpos));
            i++;
            if (i >= s.length()) {
                populateSet(val, -1, -1, type);
            } else {
                charAtpos = s.charAt(i);
                if (charAtpos >= '0' && charAtpos <= '9') {
                    PosSet vs = getValue(val, s, i);
                    val = vs.val;
                    i = vs.pos;
                }
                i = checkNext(i, s, val, type);
                return i;
            }
        } else {
            throw new InvalidInputException("Unexpected character: " + charAtpos, i);
        }

        return i;
    }

    private boolean isIncrementRangeValid(int inc, int type, int pos) throws InvalidInputException {
        switch (type){
            case MINUTE: return inc > 59;
            case HOUR: return inc > 23;
            case DAY_OF_MONTH: return inc > 31;
            case DAY_OF_WEEK: return inc > 7;
            case MONTH: return inc > 12;
            default: return true;
        }
    }

    protected int checkNext(int pos, String s, int val, int type) throws InvalidInputException {

        int end = -1;
        int i = pos;

        if (i >= s.length()) {
            populateSet(val, end, -1, type);
            return i;
        }

        char c = s.charAt(pos);

        if (c == 'L') {
            if (type == DAY_OF_WEEK) {
                if(val < 1 || val > 7)
                    throw new InvalidInputException("Day-of-Week values must be between 1 and 7", -1);
                this.lastdayOfWeek = true;
            } else {
                throw new InvalidInputException("'L' option is not valid here. (pos=" + i + ")", i);
            }
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == 'W') {
            if (type == DAY_OF_MONTH) {
                nearestWeekday = true;
            } else {
                throw new InvalidInputException("'W' option is not valid here. (pos=" + i + ")", i);
            }
            if(val > 31)
                throw new InvalidInputException("The 'W' option does not make sense with values larger than 31 (max number of days in a month)", i);
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == '#') {
            if (type != DAY_OF_WEEK) {
                throw new InvalidInputException("'#' option is not valid here. (pos=" + i + ")", i);
            }
            i++;
            try {
                nthdayOfWeek = Integer.parseInt(s.substring(i));
                if (nthdayOfWeek < 1 || nthdayOfWeek > 5) {
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new InvalidInputException("A numeric value between 1 and 5 must follow the '#' option", i);
            }
            TreeSet<Integer> set = getSet(type);
            set.add(val);
            i++;
            return i;
        }

        if (c == '-') {
            i++;
            c = s.charAt(i);
            int v = Integer.parseInt(String.valueOf(c));
            end = v;
            i++;
            if (i >= s.length()) {
                populateSet(val, end, 1, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                PosSet vs = getValue(v, s, i);
                end = vs.val;
                i = vs.pos;
            }
            if (i < s.length() && ((c = s.charAt(i)) == '/')) {
                i++;
                c = s.charAt(i);
                int v2 = Integer.parseInt(String.valueOf(c));
                i++;
                if (i >= s.length()) {
                    populateSet(val, end, v2, type);
                    return i;
                }
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    PosSet vs = getValue(v2, s, i);
                    int v3 = vs.val;
                    populateSet(val, end, v3, type);
                    i = vs.pos;
                    return i;
                } else {
                    populateSet(val, end, v2, type);
                    return i;
                }
            } else {
                populateSet(val, end, 1, type);
                return i;
            }
        }

        if (c == '/') {
            if ((i + 1) >= s.length() || s.charAt(i + 1) == ' ' || s.charAt(i + 1) == '\t') {
                throw new InvalidInputException("'/' must be followed by an integer.", i);
            }

            i++;
            c = s.charAt(i);
            int v2 = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                isIncrementRangeValid(v2, type, i);
                populateSet(val, end, v2, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                PosSet vs = getValue(v2, s, i);
                int v3 = vs.val;
                isIncrementRangeValid(v3, type, i);
                populateSet(val, end, v3, type);
                i = vs.pos;
                return i;
            } else {
                throw new InvalidInputException("Unexpected character '" + c + "' after '/'", i);
            }
        }

        populateSet(val, end, 0, type);
        i++;
        return i;
    }


    public String getCronSummary() {
        StringBuilder buf = new StringBuilder();
        buf.append("minute        ");
        buf.append(getSetValues(getSet(MINUTE)));
        buf.append("\n");
        buf.append("hour          ");
        buf.append(getSetValues(getSet(HOUR)));
        buf.append("\n");
        buf.append("day of month  ");
        buf.append(getSetValues(getSet(DAY_OF_MONTH)));
        buf.append("\n");
        buf.append("month         ");
        buf.append(getSetValues(getSet(MONTH)));
        buf.append("\n");
        buf.append("day of week   ");
        buf.append(getSetValues(getSet(DAY_OF_WEEK)));
        buf.append("\n");
        return buf.toString();
    }

    protected String getSetValues(java.util.Set<Integer> set) {
        if (set.contains(SPECIAL_CHAR_ANY_INT)) {
            return "?";
        }
        StringBuilder buf = new StringBuilder();

        Iterator<Integer> itr = set.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = itr.next();
            String val = iVal.toString();
            if (!first) {
                buf.append(",");
            }
            buf.append(val);
            first = false;
        }
        return buf.toString();
    }


    protected int skipWhiteSpace(int i, String s) {
        while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) {
            i++;
        }
        return i;
    }

    protected int findNextWhiteSpace(int i, String s) {
        while (i < s.length() && (s.charAt(i) != ' ' || s.charAt(i) != '\t')) {
            i++;
        }
        return i;
    }

    protected void populateSet(int val, int end, int incr, int type) throws InvalidInputException {
        TreeSet<Integer> set = getSet(type);
        if (type == MINUTE) {
            if ((val < 0 || val > 59 || end > 59) && (val != SPECIAL_CHAR_ALL)) {
                throw new InvalidInputException("Minute and Second values must be between 0 and 59",-1);
            }
        } else if (type == HOUR) {
            if ((val < 0 || val > 23 || end > 23) && (val != SPECIAL_CHAR_ALL)) {
                throw new InvalidInputException("Hour values must be between 0 and 23", -1);
            }
        } else if (type == DAY_OF_MONTH) {
            if ((val < 1 || val > 31 || end > 31) && (val != SPECIAL_CHAR_ALL)
                    && (val != SPECIAL_CHAR_ANY_INT)) {
                throw new InvalidInputException("Day of month values must be between 1 and 31", -1);
            }
        } else if (type == MONTH) {
            if ((val < 1 || val > 12 || end > 12) && (val != SPECIAL_CHAR_ALL)) {
                throw new InvalidInputException("Month values must be between 1 and 12", -1);
            }
        } else if (type == DAY_OF_WEEK) {
            if ((val == 0 || val > 7 || end > 7) && (val != SPECIAL_CHAR_ALL) && (val != SPECIAL_CHAR_ANY_INT)) {
                throw new InvalidInputException("Day-of-Week values must be between 1 and 7", -1);
            }
        }

        if ((incr == 0 || incr == -1) && val != SPECIAL_CHAR_ALL) {
            if (val != -1) {
                set.add(val);
            } else {
                set.add(SPECIAL_CHAR_ANY_INT);
            }
            return;
        }

        int start = val;
        int stop = end;

        if (val == SPECIAL_CHAR_ALL && incr <= 0) {
            incr = 1;
        }

        if (type == MINUTE) {
            if (stop == -1) {
                stop = 59;
            }
            if (start == -1 || start == SPECIAL_CHAR_ALL) {
                start = 0;
            }
        } else if (type == HOUR) {
            if (stop == -1) {
                stop = 23;
            }
            if (start == -1 || start == SPECIAL_CHAR_ALL) {
                start = 0;
            }
        } else if (type == DAY_OF_MONTH) {
            if (stop == -1) {
                stop = 31;
            }
            if (start == -1 || start == SPECIAL_CHAR_ALL) {
                start = 1;
            }
        } else if (type == MONTH) {
            if (stop == -1) {
                stop = 12;
            }
            if (start == -1 || start == SPECIAL_CHAR_ALL) {
                start = 1;
            }
        } else if (type == DAY_OF_WEEK) {
            if (stop == -1) {
                stop = 7;
            }
            if (start == -1 || start == SPECIAL_CHAR_ALL) {
                start = 1;
            }
        }

        int max = -1;
        if (stop < start) {
            max = Utils.getMax(type);
            stop += max;
        }

        for (int i = start; i <= stop; i += incr) {
            if (max == -1) {
                set.add(i);
            } else {
                int i2 = i % max;
                if (i2 == 0 && (type == MONTH || type == DAY_OF_WEEK || type == DAY_OF_MONTH) ) {
                    i2 = max;
                }
                set.add(i2);
            }
        }
    }

    TreeSet<Integer> getSet(int type) {
        return map.get(type);
    }

    protected int getNumericValue(String s, int i) {
        int endOfVal = findNextWhiteSpace(i, s);
        String val = s.substring(i, endOfVal);
        return Integer.parseInt(val);
    }

}


