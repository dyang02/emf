package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class ModuleTypesTableData extends AbstractTableData {
    private List rows;

    public ModuleTypesTableData(ModuleType[] types) {
        this.rows = createRows(types);
    }

    public String[] columns() {
        return new String[] { "Name", "Composite?", "Creator", "Creation Date", "Last Mod Date", "Default Version", "Lock Owner", "Lock Date", "Description"};
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(ModuleType[] types) {
        List rows = new ArrayList();

        for (int i = 0; i < types.length; i++) {
            ModuleType element = types[i];
            Object[] values = { element.getName(),
                                element.isComposite() ? "Yes" : "No",
                                element.getCreator().getName(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getCreationDate()),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getLastModifiedDate()),
                                element.getDefaultVersion(),
                                element.getLockOwner(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getLockDate()),
                                element.getDescription() };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }
}