import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * INHERITANCE + METHOD OVERRIDING - colours a table row when the text in one column
 * (for example "Overdue") matches one of the given words.
 */
public class StatusRenderer extends DefaultTableCellRenderer {
    private int column;
    private String[] words;
    private Color color;

    public StatusRenderer(int column, String[] words, Color color) {
        this.column = column;
        this.words = words;
        this.color = color;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                   boolean focus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, col);
        if (!selected) {
            c.setBackground(Color.WHITE);
            Object text = table.getValueAt(row, column);
            for (int i = 0; i < words.length; i++) {
                if (words[i].equals(text)) {
                    c.setBackground(color);
                }
            }
        }
        return c;
    }
}
