package mmitest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mmi {


    private Map<String, List<SubscriptionRecord<?>>> subscriptions = new HashMap<>();
    private final List<SubscriptionRecord<?>> unscribeList = new ArrayList<>();

    public synchronized <T> SubscriptionRecord<T> subscribe(String topic) {
        return subscribe(new Subscription<>(topic));
    }

    public synchronized <T> SubscriptionRecord<T> subscribe(String topic, Class<T> clazz) {
        return subscribe(new Subscription<>(topic));
    }

    public synchronized <T> SubscriptionRecord<T> subscribe(Subscription<T> subscription) {
        SubscriptionRecord<T> record = new SubscriptionRecord<>(subscription.topic(), subscription.filter(), subscription.until());
        List<SubscriptionRecord<?>> subscriptionRecordList = subscriptions.get(subscription.topic());
        if (subscriptionRecordList == null) {
            subscriptionRecordList = new ArrayList<>();
            subscriptions.put(subscription.topic(), subscriptionRecordList);
        }
        subscriptionRecordList.add(record);

        return record;
    }


    public synchronized void unsubscribe(SubscriptionRecord<?> subscriptionRecord) {
        List<SubscriptionRecord<?>> subscriptionRecords = subscriptions.get(subscriptionRecord.getTopic());
        subscriptionRecords.remove(subscriptionRecord);
        if (subscriptionRecords.size() == 0) {
            subscriptions.remove(subscriptionRecord.getTopic());
        }
        subscriptionRecord.stop();
    }


    public void newData(Object data) {
        handleData(data, Record.Type.RX);
    }

    private synchronized void handleData(Object data, Record.Type rx) {
        Class<?> clazz = data.getClass();
        List<SubscriptionRecord<?>> subscriptionRecordList = subscriptions.get(clazz.getSimpleName());
        if (subscriptionRecordList != null) {
            Record record = new Record(rx, data);
            for (SubscriptionRecord<?> subscriptionRecord : subscriptionRecordList) {
                if (subscriptionRecord.getFilter() == null || subscriptionRecord.getFilter().test(record)) {
                    subscriptionRecord.recordList.add(record);
                }
                if (subscriptionRecord.getUntil() != null && subscriptionRecord.getUntil().test(subscriptionRecord.getRecordList(), record)) {
                    unscribeList.add(subscriptionRecord);
                }
            }
            for (SubscriptionRecord<?> subscriptionRecord : unscribeList) {
                unsubscribe(subscriptionRecord);
            }
            unscribeList.clear();
        }
    }


    public void updatedData(Object data) {
        newData(data);
    }

    public void deletedData(Object data) {
        handleData(data, Record.Type.DELETED);
    }

    public void send(String topic, Object data) {

    }

    public void dispose(Object data) {
        dispose(data.getClass().getSimpleName(), data);
    }


    public void send(Object data) {
        send(data.getClass().getSimpleName(), data);
    }


    public void dispose(String topic, Object data) {

    }


}
