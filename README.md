# cronparser
java app to parse a cron expression

### Features

breaks down the cron expression into diffrent sets of values
which shows minute, hour, day of month, month, day of week, command

Eg: for given input : 

`"*/15 0 1,15 * 1-5 /usr/bin/find"`

Output will be:

```minute        0,15,30,45
hour          0
day of month  1,15
month         1,2,3,4,5,6,7,8,9,10,11,12
day of week   1,2,3,4,5
command       /usr/bin/find 
```


How to build the project:

build using mvn

`mvn clean install`

how to run

`java -cp target/cronparser-1.0-SNAPSHOT.jar cronparser.Driver "*/15 0 1,15 * 1-5 /usr/bin/find"`

Note: Required jdk-11.*

How to run tests

`mvn '-Dtest=CronParserTest' test`

you can also import in intelliJ and run the application by passing 
<img width="590" alt="Screenshot 2022-04-28 at 16 49 47" src="https://user-images.githubusercontent.com/5393014/165741080-e16e911f-9dcd-4419-a1da-2e9d2c92cb5b.png">

You can run test cases by 

<img width="564" alt="Screenshot 2022-04-28 at 16 50 23" src="https://user-images.githubusercontent.com/5393014/165741172-064ed0d7-46d0-4846-b621-15d0a32568bf.png">

