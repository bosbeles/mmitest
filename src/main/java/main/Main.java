package main;

import mmitest.Mmi;
import mmitest.Record;
import mmitest.Subscription;
import mmitest.SubscriptionResult;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {


    public static void main(String[] args) {


        Mmi mmi = new Mmi();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
        executor.schedule(() -> {
            try {
                mmi.newData("D");
                sleep();
                mmi.newData("Dene");
                sleep();
                mmi.newData("Dd");
                sleep();
                mmi.newData("Deneme");
                sleep();
                mmi.deletedData("Dene");
                sleep();
                mmi.newData("Dene");
                sleep();
                mmi.newData("Done");
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }, 2, TimeUnit.SECONDS);

        Subscription<String> subscription =
                new Subscription<String>("String")
                        .filter(record -> record.getData().length() > 3 && record.getType() == Record.Type.RX)
                        .until((list, record) -> list.size() == 2 && record.getType() == Record.Type.DELETED);
        SubscriptionResult<String> subscriptionResult = mmi.subscribe(subscription);

        SubscriptionResult<String> subscriptionResult2 = mmi.subscribe("String", String.class);
        SubscriptionResult<Object> subscriptionResult3 = mmi.subscribe("String");
        SubscriptionResult<String> subscriptionResult4 = mmi.subscribe("String");

        executor.scheduleWithFixedDelay(() -> {
            System.out.println("Record: " + Arrays.toString(subscriptionResult.getRecordList().toArray()));
            System.out.println("Record2: " + Arrays.toString(subscriptionResult2.getRecordList().toArray()));
            System.out.println("Record3: " + Arrays.toString(subscriptionResult3.getRecordList().toArray()));
            System.out.println("Record4: " + Arrays.toString(subscriptionResult4.getRecordList().toArray()));
        }, 1, 1, TimeUnit.SECONDS);


        List<Record<String>> stringList = subscriptionResult.waitRecordList(10, TimeUnit.SECONDS);

        System.out.println("Subscription: " + Arrays.toString(stringList.toArray()));
    }

    private static void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
