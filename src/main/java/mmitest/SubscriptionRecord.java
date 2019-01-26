package mmitest;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Data
    public class SubscriptionRecord<T> {
        private final BiPredicate until;
        private final Predicate filter;
        private final String topic;
        private final CountDownLatch latch = new CountDownLatch(1);
        List recordList = new ArrayList<>();

        public SubscriptionRecord(String topic, Predicate<Record<T>> filter, BiPredicate<List<T>, Record<T>> until) {
            this.topic = topic;
            this.filter = filter;
            this.until = until;
        }

        public List<Record<T>> getRecordList() {
            return recordList;
        }

        public List<Record<T>> waitRecordList() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getRecordList();
        }

        public List<Record<T>> waitRecordList(long t, TimeUnit unit) {
            try {
                latch.await(t, unit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getRecordList();
        }

        public void stop() {
            latch.countDown();
        }
    }