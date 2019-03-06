package gui;

import gui.log.model.Direction;
import gui.log.model.LogItem;
import gui.log.model.log.view.LogViewer;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        EventQueue.invokeLater(Main::createAndShowGUI);
    }

    private static void createAndShowGUI() {

        try {
            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600,400);
        frame.setLocationRelativeTo(null);

        LogViewer logViewer = new LogViewer();
        frame.getContentPane().add(logViewer);

        frame.setVisible(true);



        scheduler.scheduleWithFixedDelay(()->{

            for (int i = 0; i < 1000; i++) {
                LogItem logItem = random();
                //logViewer.getItems().add(logItem);
                EventQueue.invokeLater(()-> logViewer.getLogTableModel().addRow(logItem));
            }

        }, 1000,50, TimeUnit.MILLISECONDS);


    }

    static AtomicLong next = new AtomicLong(0);

    static LogItem random() {

        LogItem logItem = new LogItem();
        ThreadLocalRandom localRandom = ThreadLocalRandom.current();
        byte[] bytes = new byte[10 + localRandom.nextInt(40)];
        localRandom.nextBytes(bytes);
        logItem.setMessageType((2 + localRandom.nextInt(1)) + "." + localRandom.nextInt(5));
        logItem.setContent(bytes);
        logItem.setDirection(localRandom.nextBoolean() ? Direction.RX : Direction.TX);
        logItem.setPacketNo(next.incrementAndGet());
        if(logItem.getPacketNo() % 100000 == 0) {
            System.out.println(Instant.now() + " " + logItem.getPacketNo());
        }
        return logItem;
    }
}
