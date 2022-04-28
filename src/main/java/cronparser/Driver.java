package cronparser;

public class Driver {



    private CronParser cronParser;
    private String command;
    public static void main(String args[]){
        Driver d= new Driver(args);
        d.displayCronExpression();
    }
    Driver(String[] args){
        if(args.length < 1){
            System.out.println("::usage::" );
            System.out.println("your-program \"cron-expression command" );
            System.out.println("your-program \"*/15 0 1,15 * 1-5 /usr/bin/find\"" );
            System.exit(1);
        }
        String input = args[0];
        String[] tokens = input.split(Constants.DELIMITER);
        String command = tokens[tokens.length-1];
        this.command = command;
        try {
            cronParser = new CronParser(input.substring(0, input.length() - command.length()-1));
        } catch (InvalidInputException e) {
            e.printStackTrace();
        }
    }

    public void displayCronExpression(){
        StringBuilder stringBuilder = new StringBuilder();
        String cronExpression = cronParser.getCronSummary();
        stringBuilder.append(cronExpression);
        stringBuilder.append("command       ");
        stringBuilder.append(command);
        System.out.println(stringBuilder);
    }

}
