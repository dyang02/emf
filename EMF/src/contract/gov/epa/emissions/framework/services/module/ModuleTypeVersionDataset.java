package gov.epa.emissions.framework.services.module;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.epa.emissions.commons.data.DatasetType;

public class ModuleTypeVersionDataset implements Serializable {

    public static final String IN    = "IN";
    public static final String INOUT = "INOUT";
    public static final String OUT   = "OUT";
    
    // By default, in PostgreSQL, NAMEDATALEN is 64 so the maximum identifier length is 63 bytes.
    public static final int MAX_NAME_LEN = 63; // NAMEDATALEN-1

    private int id;

    private ModuleTypeVersion moduleTypeVersion;

    private String placeholderName;

    private String mode; // 'IN', 'INOUT', 'OUT'

    private DatasetType datasetType;

    private String description;

    public ModuleTypeVersionDataset deepCopy() {
        ModuleTypeVersionDataset newModuleTypeVersionDataset = new ModuleTypeVersionDataset();
        newModuleTypeVersionDataset.setModuleTypeVersion(moduleTypeVersion);
        newModuleTypeVersionDataset.setPlaceholderName(placeholderName);
        newModuleTypeVersionDataset.setMode(mode);
        newModuleTypeVersionDataset.setDatasetType(datasetType);
        newModuleTypeVersionDataset.setDescription(description);
        return newModuleTypeVersionDataset;
    }
    
    // Technically, the placeholder names do not need to follow the PostgreSQL naming
    // conventions but we do that for uniformity.
    public static boolean isValidPlaceholderName(String name, final StringBuilder error) {
        error.setLength(0);
        name = name.trim();
        if (name.length() == 0) {
            error.append("Placeholder name cannot be empty.");
            return false;
        }
        if (name.length() > MAX_NAME_LEN) {
            error.append(String.format("Placeholder name '%s' is longer than %d characters.", name, MAX_NAME_LEN));
            return false;
        }
        Matcher matcher = Pattern.compile("[^a-zA-Z0-1_]", Pattern.CASE_INSENSITIVE).matcher(name);
        if (matcher.find()) {
            error.append(String.format("Placeholder name '%s' contains illegal characters.", name));
            return false;
        }
        if (name.charAt(0) != '_' && !Character.isLetter(name.charAt(0))) {
            error.append(String.format("Placeholder name '%s' must begin with a letter or _ (underscore).", name));
            return false;
        }
        return true;
    }

    public boolean isValid(final StringBuilder error) {
        error.setLength(0);
        if (!isValidPlaceholderName(placeholderName, error)) return false;
        if (datasetType == null) {
            error.append(String.format("Dataset type for placeholder '%s' has not been set.", placeholderName));
            return false;
        }
        return true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ModuleTypeVersion getModuleTypeVersion() {
        return moduleTypeVersion;
    }

    public void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        this.moduleTypeVersion = moduleTypeVersion;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public void setPlaceholderName(String placeholderName) {
        this.placeholderName = placeholderName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isModeIN() {
        return this.mode.equals(IN);
    }

    public boolean isModeINOUT() {
        return this.mode.equals(INOUT);
    }

    public boolean isModeOUT() {
        return this.mode.equals(OUT);
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return getPlaceholderName();
    }

    public int hashCode() {
        return placeholderName.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof ModuleTypeVersionDataset && ((ModuleTypeVersionDataset) other).getPlaceholderName() == placeholderName);
    }

    public int compareTo(ModuleTypeVersionDataset o) {
        return placeholderName.compareTo(o.getPlaceholderName());
    }
}