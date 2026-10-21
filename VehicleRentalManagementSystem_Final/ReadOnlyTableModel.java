import javax.swing.table.DefaultTableModel;

/** INHERITANCE + METHOD OVERRIDING - a table whose cells cannot be edited by the user. */
public class ReadOnlyTableModel extends DefaultTableModel {
    public ReadOnlyTableModel(String[] columns) {
        super(columns, 0);
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
