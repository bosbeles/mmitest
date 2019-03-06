package gui.log.model;

import lombok.Data;

@Data
public class LogItem {

    private long packetNo;
    private String logger;
    private Direction direction;
    private short source;
    private short destination;
    private String messageType;
    private byte[] content;

}
