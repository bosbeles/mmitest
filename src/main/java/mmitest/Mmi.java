package mmitest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mmi {


    private Map<String, List<SubscriptionResult<?>>> subscriptions = new HashMap<>();
    private final List<SubscriptionResult<?>> unscribeList = new ArrayList<>();

    public synchronized <T> SubscriptionResult<T> subscribe(String topic) {
        return subscribe(new Subscription<>(topic));
    }

    public synchronized <T> SubscriptionResult<T> subscribe(String topic, Class<T> clazz) {
        return subscribe(new Subscription<>(topic));
    }

    public synchronized <T> SubscriptionResult<T> subscribe(Subscription<T> subscription) {
        SubscriptionResult<T> record = new SubscriptionResult<>(subscription.topic(), subscription.filter(), subscription.until());
        List<SubscriptionResult<?>> subscriptionResultList = subscriptions.get(subscription.topic());
        if (subscriptionResultList == null) {
            subscriptionResultList = new ArrayList<>();
            subscriptions.put(subscription.topic(), subscriptionResultList);
        }
        subscriptionResultList.add(record);

        return record;
    }


    public synchronized void unsubscribe(SubscriptionResult<?> subscriptionResult) {
        List<SubscriptionResult<?>> subscriptionResults = subscriptions.get(subscriptionResult.getTopic());
        subscriptionResults.remove(subscriptionResult);
        if (subscriptionResults.size() == 0) {
            subscriptions.remove(subscriptionResult.getTopic());
        }
        subscriptionResult.stop();
    }


    public void newData(Object data) {
        handleData(data, Record.Type.RX);
    }

    private synchronized void handleData(Object data, Record.Type rx) {
        Class<?> clazz = data.getClass();
        List<SubscriptionResult<?>> subscriptionResultList = subscriptions.get(clazz.getSimpleName());
        if (subscriptionResultList != null) {
            Record record = new Record(rx, data);
            for (SubscriptionResult<?> subscriptionResult : subscriptionResultList) {
                if (subscriptionResult.getFilter() == null || subscriptionResult.getFilter().test(record)) {
                    subscriptionResult.recordList.add(record);
                }
                if (subscriptionResult.getUntil() != null && subscriptionResult.getUntil().test(subscriptionResult.getRecordList(), record)) {
                    unscribeList.add(subscriptionResult);
                }
            }
            for (SubscriptionResult<?> subscriptionResult : unscribeList) {
                unsubscribe(subscriptionResult);
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
