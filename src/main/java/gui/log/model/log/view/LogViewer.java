package gui.log.model.log.view;

import gui.log.model.LogItem;
import gui.log.model.log.view.table.LogTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogViewer extends JPanel {

    private final JEditorPane contentEditor;
    private final JScrollPane contentScroll;
    private final LogTableModel logTableModel;
    private JTable logTable;
    private final JScrollPane tableScroll;
    private ConcurrentLinkedQueue<LogItem> logItems = new ConcurrentLinkedQueue<>();
    private BlockingQueue<LogItem> blockingLogItems = new LinkedBlockingQueue<>();

    public LogViewer() {

        logTable = new JTable();
        logTable.setRowHeight(30);
        logTableModel = new LogTableModel();
        logTable.setModel(logTableModel);


        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(350);
        splitPane.setOneTouchExpandable(true);
        tableScroll = new JScrollPane(logTable);
        logTable.setFillsViewportHeight(true);

        splitPane.setLeftComponent(tableScroll);


        contentEditor = new JEditorPane();
        contentScroll = new JScrollPane(contentEditor);
        splitPane.setRightComponent(contentScroll);


        setLayout(new BorderLayout());
        add(splitPane);

        TableSwingWorker worker = new TableSwingWorker(getLogTableModel());
        worker.execute();

        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logTable.getSelectionModel().addListSelectionListener(e -> {
            int firstIndex = e.getFirstIndex();
            System.out.println(firstIndex + " " + e.getLastIndex());
            LogItem logItem = logTableModel.getData().get(logTable.getSelectedRow());
            byte[] content = logItem.getContent();
            contentEditor.setText(logItem.getPacketNo() + " \n" + new String(content));
        });

    }

    public Collection<LogItem> getItems() {
        return blockingLogItems;
    }


    public LogTableModel getLogTableModel() {
        return logTableModel;
    }

    public class TableSwingWorker extends SwingWorker<LogTableModel, LogItem> {

        private final LogTableModel tableModel;

        public TableSwingWorker(LogTableModel tableModel) {
            this.tableModel = tableModel;
        }

        @Override
        protected LogTableModel doInBackground() throws Exception {

            // This is a deliberate pause to allow the UI time to render
            Thread.sleep(1000);

            System.out.println("Start polulating");

            for (LogItem item = loadData(); !isCancelled() && item != null; item = loadData()) {
                publish(item);
                Thread.yield();
            }
            return tableModel;
        }

        @Override
        protected void process(List<LogItem> chunks) {
            //System.out.println("Adding " + chunks.size() + " rows");
            tableModel.addRows(chunks);
        }
    }

    private LogItem loadData() {

        try {
            LogItem logItem = blockingLogItems.take();
            return logItem;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
