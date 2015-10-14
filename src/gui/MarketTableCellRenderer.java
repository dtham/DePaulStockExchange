
package gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class MarketTableCellRenderer extends DefaultTableCellRenderer {

    private double goodMarket;
    
    public MarketTableCellRenderer(double gmIn) {
        super();
        goodMarket = gmIn;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (column != 3) 
        {
            setForeground(Color.black);
            setBackground(Color.white);
            setText(value.toString());
            return this;
        }
        
        String cleanValue = value.toString().replaceAll("\\$", "");
        double doubleValue = Double.parseDouble(cleanValue);
        doubleValue = Math.abs(doubleValue);
        
        
        if (doubleValue == 0.00) {
            setForeground(Color.black);
            setBackground(Color.lightGray);
        } else if (doubleValue < goodMarket) {
            setForeground(Color.black);
            setBackground(Color.green);
        } else {
            setForeground(Color.black);
            setBackground(Color.white);
        }
        setText(value.toString());
        return this;
    }
}
