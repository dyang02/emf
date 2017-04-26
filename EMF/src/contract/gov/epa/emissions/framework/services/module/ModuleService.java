package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ModuleService {

    // Parameter Types
    
    ParameterType[] getParameterTypes() throws EmfException;

    // Module Types

    ModuleType[] getModuleTypes() throws EmfException; // TODO add LiteModuleType class (and maybe LiteModuleTypeVersion too) and use the session object cache

    ModuleType getModuleType(int id) throws EmfException;
    
    ModuleType addModuleType(ModuleType moduleType) throws EmfException;

    void deleteModuleTypes(User owner, ModuleType[] moduleTypes) throws EmfException;

    ModuleType obtainLockedModuleType(User owner, int moduleTypeId) throws EmfException;

    ModuleType releaseLockedModuleType(User owner, int moduleTypeId) throws EmfException;

    // Module Type Versions
    
    ModuleType updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, User user) throws EmfException;

    ModuleType finalizeModuleTypeVersion(int moduleTypeVersionId, User user) throws EmfException;

    ModuleType removeModuleTypeVersion(int moduleTypeVersionId) throws EmfException;

    // Lite Modules

    LiteModule[] getLiteModules() throws EmfException;

    LiteModule[] getRelatedLiteModules(int datasetId) throws EmfException;
    
    // Modules

    Module getModule(int moduleId) throws EmfException;
    
    Module addModule(Module module) throws EmfException;

    Module updateModule(Module module) throws EmfException;

    int[] deleteModules(User owner, int[] moduleIds) throws EmfException;

    Module obtainLockedModule(User owner, int moduleId) throws EmfException;

    Module releaseLockedModule(User owner, int moduleId) throws EmfException;

    int[] lockModules(User owner, int[] moduleIds) throws EmfException;

    int[] unlockModules(User owner, int[] moduleIds) throws EmfException;
    
    void runModules(int[] moduleIds, User user) throws EmfException;

    EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId) throws EmfException;
    EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId, Integer newDatasetId, String newDatasetNamePattern) throws EmfException;
    
    Module[] getModulesForModuleTypeVersion(int moduleTypeVersionId) throws EmfException;
}
