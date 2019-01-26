package mmitest;

import java.time.Instant;

@lombok.Data
public final class Record<T> {


    public enum Type {
        RX,
        DELETED,
        TX,
        DISPOSED
    }

    private Instant time = Instant.now();
    private T data;
    private Type type;

    public Record(Type type, T data) {
        this.data = data;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Record{" +
                "time=" + time +
                ", recordList=" + data +
                ", type=" + type +
                '}';
    }
}
