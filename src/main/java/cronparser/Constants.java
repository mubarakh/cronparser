package cronparser;

public class Constants {

    public static final int MINUTE = 0;
    public static final int HOUR = 1;
    public static final int DAY_OF_MONTH = 2;
    public static final int MONTH = 3;
    public static final int DAY_OF_WEEK = 4;
    public static final int SPECIAL_CHAR_ALL = 99; // '*'
    public static final int SPECIAL_CHAR_ANY_INT = 98; // '?'
    public static final String DELIMITER = " ";
    public static final String REGEX_INVALID_INPUT = "^L-[0-9]*[W]?";
}
