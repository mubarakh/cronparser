import cronparser.CronParser;
import cronparser.InvalidInputException;
import org.junit.Assert;
import org.junit.Test;

public class CronParserTest {


    @Test
    public void testEmptyInput(){
        System.out.println("Test Empty Input");
        try {
            CronParser cronParser = new CronParser("");
            cronParser.getCronSummary();
        } catch (InvalidInputException invalidInputException) {
            Assert.assertNotNull(invalidInputException);
        }
    }

    @Test
    public void testIncby15Min(){
        System.out.println("Test Incby15Min");
        try {
            CronParser cronParser = new CronParser("*/15 0 1,15 * 1-5");
            //System.out.println(cronParser.getCronSummary());
            String res = "minute        0,15,30,45\n" +
                    "hour          0\n" +
                    "day of month  1,15\n" +
                    "month         1,2,3,4,5,6,7,8,9,10,11,12\n" +
                    "day of week   1,2,3,4,5\n";
            Assert.assertEquals(cronParser.getCronSummary(),res);
        } catch (InvalidInputException invalidInputException) {

        }
    }

    @Test
    public void testIncby60MinsholdthrowException(){
        System.out.println("Test Incby15Min");
        try {
            CronParser cronParser = new CronParser("*/60 0 1,15 * 1-5");

        } catch (InvalidInputException invalidInputException) {
            System.out.println(invalidInputException);
            Assert.assertNotNull(invalidInputException);
        }
    }

    @Test
    public void testIncby2Hour(){
        System.out.println("Test Incby2Hour");
        try {
            CronParser cronParser = new CronParser("0 */2 1,15 * 1-5");
            System.out.println(cronParser.getCronSummary());
            String res = "minute        0\n" +
                    "hour          0,2,4,6,8,10,12,14,16,18,20,22\n" +
                    "day of month  1,15\n" +
                    "month         1,2,3,4,5,6,7,8,9,10,11,12\n" +
                    "day of week   1,2,3,4,5\n";
            Assert.assertEquals(cronParser.getCronSummary(),res);
        } catch (InvalidInputException invalidInputException) {

        }
    }


}
