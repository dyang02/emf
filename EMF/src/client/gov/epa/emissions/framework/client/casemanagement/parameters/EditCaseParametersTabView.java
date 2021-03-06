package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public interface EditCaseParametersTabView {

    void display(EmfSession session, Case caseObj, EditParametersTabPresenter presenter);

    //CaseParameter[] caseParameters();

    void addParameter(CaseParameter param);
    
    void refresh(CaseParameter[] caseParas);
    
    int numberOfRecord();

    void clearMessage();
    
    void setMessage(String msg);
    
    Sector getSelectedSector();
    
    String nameContains();
    
    Boolean isShowAll();

}
