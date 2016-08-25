package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ModuleTypeVersion;
import gov.epa.emissions.commons.data.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

public class NewModuleTypeVersionDatasetPresenter {
       
    private NewModuleTypeVersionDatasetView view;
    private ModuleTypeDatasetsObserver datasetsObserver;

    private EmfSession session;

    public NewModuleTypeVersionDatasetPresenter(EmfSession session, NewModuleTypeVersionDatasetView view, ModuleTypeDatasetsObserver datasetsObserver) {
        this.session = session;
        this.view = view;
        this.datasetsObserver = datasetsObserver;
    }

    public DatasetType[] getDatasetTypes() {
        try {
            return service().getDatasetTypes();
        }
        catch(EmfException ex){
            return new DatasetType[]{};
        }
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(ModuleTypeVersion moduleTypeVersion, ModuleTypeVersionDataset moduleTypeVersionDataset) throws EmfException {
        moduleTypeVersion.addModuleTypeVersionDataset(moduleTypeVersionDataset);
        datasetsObserver.refreshDatasets();
        closeView();
    }

}
