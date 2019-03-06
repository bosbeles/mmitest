package gui.log.model.log.view.table;

import gui.log.model.LogItem;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogTableModel extends AbstractTableModel {


    private List<String> columns = Arrays.asList("Packet No", "Logger", "Direction", "Source", "Destination", "Message");

    private List<LogItem> data = new ArrayList<>();


    public void addRow(LogItem logItem){
        int rowCount = getRowCount();
        data.add(logItem);
        fireTableRowsInserted(rowCount, rowCount);
    }

    public void addRows(LogItem... logItems) {
        addRows(Arrays.asList(logItems));
    }

    public void addRows(List<LogItem> list) {
        int rowCount = getRowCount();
        data.addAll(list);
        fireTableRowsInserted(rowCount, getRowCount() - 1);
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogItem logItem = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return logItem.getPacketNo();
            case 1:
                return logItem.getLogger();
            case 2:
                return logItem.getDirection();
            case 3:
                return logItem.getSource();
            case 4: return logItem.getDestination();
            case 5:
                return  logItem.getMessageType();
            default:
        }
        return null;
    }

    public List<LogItem> getData() {
        return data;
    }
}
