package mmitest;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Data
@Accessors(fluent = true)
public class Subscription<T> {
    private BiPredicate<List<T>, Record<T>> until;
    private Predicate<Record<T>> filter;
    private String topic;

    public Subscription(String topic) {
        this.topic = topic;
    }

}