package mmitest;

import lombok.Data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Data
public class SubscriptionResult<T> {
    private final BiPredicate until;
    private final Predicate filter;
    private final String topic;
    private final CountDownLatch latch = new CountDownLatch(1);
    List recordList = new CopyOnWriteArrayList();

    public SubscriptionResult(String topic, Predicate<Record<T>> filter, BiPredicate<List<T>, Record<T>> until) {
        this.topic = topic;
        this.filter = filter;
        this.until = until;
    }

    public List<T> getDataList() {
        return ((List<Record<T>>) this.recordList).stream().map(r->r.getData()).collect(Collectors.toList());
    }

    public List<Record<T>> getRecordList() {
        return recordList;
    }

    public List<T> waitDataList() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getDataList();
    }

    public List<Record<T>> waitRecordList() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getRecordList();
    }

    public List<T> waitDataList(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getDataList();
    }

    public List<Record<T>> waitRecordList(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getRecordList();
    }

    public void stop() {
        latch.countDown();
    }
}