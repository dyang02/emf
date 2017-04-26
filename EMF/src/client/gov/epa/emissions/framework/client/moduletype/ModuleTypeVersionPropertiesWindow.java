package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDatasetConnection;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameterConnection;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionRevision;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class ModuleTypeVersionPropertiesWindow extends DisposableInteralFrame
        implements ModuleTypeVersionPropertiesView {
    private ModuleTypeVersionPropertiesPresenter presenter;

    private EmfConsole parentConsole;

    private EmfSession session;

    ModuleTypeVersionObserver moduleTypeVersionObserver;

    private ViewMode initialViewMode;

    private ViewMode viewMode;

    private boolean mustUnlock;

    private boolean isDirty;

    private ModuleType moduleType;

    private ModuleTypeVersion moduleTypeVersion;

    // layout
    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private JTabbedPane tabbedPane;

    // module type
    private JPanel moduleTypePanel;

    private TextField moduleTypeName;

    private TextArea moduleTypeDescription;

    private Label moduleTypeLockOwner;

    private Label moduleTypeLockDate;

    private Label moduleTypeCreationDate;

    private Label moduleTypeLastModifiedDate;

    private Label moduleTypeCreator;

    private Label moduleTypeDefaultVersionNumber;

    // version
    private JPanel versionPanel;

    private Label moduleTypeVersionNumber;

    private TextField moduleTypeVersionName;

    private TextArea moduleTypeVersionDescription;

    private Label moduleTypeVersionCreationDate;

    private Label moduleTypeVersionLastModifiedDate;

    private Label moduleTypeVersionCreator;

    private Label moduleTypeVersionBaseVersionNumber;

    private Label moduleTypeVersionIsFinal;

    // datasets
    private JPanel datasetsPanel;

    private JPanel datasetsTablePanel;

    private GetDatasetTypesTask getDatasetTypesTask;

    private DatasetType[] datasetTypesCache;

    private SelectableSortFilterWrapper datasetsTable;

    private ModuleTypeVersionDatasetsTableData datasetsTableData;

    // parameters
    private JPanel parametersPanel;

    private JPanel parametersTablePanel;

    private SelectableSortFilterWrapper parametersTable;

    private ModuleTypeVersionParametersTableData parametersTableData;

    // algorithm
    private JPanel algorithmPanel;

    private TextArea algorithm;

    private UndoManager algorithmUndoManager;

    // submodules
    private JPanel submodulesPanel;

    private JPanel submodulesTablePanel;

    private SelectableSortFilterWrapper submodulesTable;

    private ModuleTypeVersionSubmodulesTableData submodulesTableData;

    // connections
    private JPanel connectionsPanel;

    private JPanel connectionsTablePanel;

    private SelectableSortFilterWrapper connectionsTable;

    private ModuleTypeVersionConnectionsTableData connectionsTableData;

    // revisions
    private JPanel revisionsPanel;

    private TextArea revisions;

    class GetDatasetTypesTask extends SwingWorker<DatasetType[], Void> {

        private Container parentContainer;

        public GetDatasetTypesTask(Container parentContainer) {
            this.parentContainer = parentContainer;
        }

        // Main task. Executed in background thread. Don't update GUI here.
        @Override
        public DatasetType[] doInBackground() throws EmfException {
            return presenter.getDatasetTypes();
        }

        // Executed in event dispatching thread.
        @Override
        public void done() {
            try {
                datasetTypesCache = get();
            } catch (InterruptedException e1) {
                // messagePanel.setError(e1.getMessage());
                // setErrorMsg(e1.getMessage());
            } catch (ExecutionException e1) {
                // messagePanel.setError(e1.getCause().getMessage());
                // setErrorMsg(e1.getCause().getMessage());
            } finally {
                // ComponentUtility.enableComponents(parentContainer, true);
                // this.parentContainer.setCursor(null); //turn off the wait cursor
            }
        }
    };

    // New Module Type
    public ModuleTypeVersionPropertiesWindow(EmfConsole parentConsole, DesktopManager desktopManager,
            EmfSession session, ModuleTypeVersionObserver moduleTypeVersionObserver, boolean composite) {
        super(getWindowTitle(ViewMode.NEW, null), new Dimension(800, 600), desktopManager);

        this.parentConsole = parentConsole;
        this.session = session;
        this.moduleTypeVersionObserver = moduleTypeVersionObserver;

        this.initialViewMode = ViewMode.NEW;
        this.viewMode = ViewMode.NEW;
        this.mustUnlock = false;
        this.isDirty = true;

        Date date = new Date();

        moduleType = new ModuleType();
        moduleType.setName("");
        moduleType.setDescription("");
        moduleType.setCreationDate(date);
        moduleType.setLastModifiedDate(date);
        moduleType.setCreator(session.user());
        moduleType.setDefaultVersion(0);
        moduleType.setIsComposite(composite);

        moduleTypeVersion = new ModuleTypeVersion();
        moduleTypeVersion.setVersion(0);
        moduleTypeVersion.setName("Initial Version");
        String versionDescription = "Initial version created on " + CustomDateFormat.format_MM_DD_YYYY_HH_mm(date)
                + " by " + session.user().getName();
        moduleTypeVersion.setDescription(versionDescription);
        moduleTypeVersion.setCreationDate(date);
        moduleTypeVersion.setLastModifiedDate(date);
        moduleTypeVersion.setCreator(session.user());
        moduleTypeVersion.setBaseVersion(0);
        moduleTypeVersion.setAlgorithm("-- Initial version created by " + session.user().getName() + "\n" + "-- \n"
                + "-- TODO: implement the algorithm\n\n");
        moduleTypeVersion.setIsFinal(false);
        moduleTypeVersion.setModuleType(moduleType);
        moduleType.addModuleTypeVersion(moduleTypeVersion);

        ModuleTypeVersionRevision moduleTypeVersionRevision = new ModuleTypeVersionRevision();
        moduleTypeVersionRevision.setDescription(versionDescription);
        moduleTypeVersionRevision.setCreationDate(date);
        moduleTypeVersionRevision.setCreator(session.user());
        moduleTypeVersion.addModuleTypeVersionRevision(moduleTypeVersionRevision);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        super.getContentPane().add(layout);
    }

    // View/Edit existing Module Type Version
    // Edit new Module Type Version
    public ModuleTypeVersionPropertiesWindow(EmfConsole parentConsole, DesktopManager desktopManager,
            EmfSession session, ModuleTypeVersionObserver moduleTypeVersionObserver, ViewMode viewMode,
            ModuleTypeVersion moduleTypeVersion) {
        super(getWindowTitle(viewMode, moduleTypeVersion), new Dimension(800, 600), desktopManager);

        this.parentConsole = parentConsole;
        this.session = session;
        this.moduleTypeVersionObserver = moduleTypeVersionObserver;

        this.mustUnlock = false;

        this.isDirty = false;
        this.initialViewMode = viewMode;
        this.viewMode = viewMode;

        if (this.viewMode == ViewMode.NEW) {
            this.isDirty = true;
            this.viewMode = ViewMode.EDIT;
        }

        this.moduleTypeVersion = moduleTypeVersion;
        this.moduleType = moduleTypeVersion.getModuleType();

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode, ModuleTypeVersion moduleTypeVersion) {
        if (moduleTypeVersion == null)
            return "New Module Type";

        String viewModeText = "";
        switch (viewMode) {
        case NEW:
            viewModeText = "New";
            break;
        case EDIT:
            viewModeText = "Edit";
            break;
        case VIEW:
            viewModeText = "View";
            break;
        default:
            break; // should never happen
        }

        return viewModeText + " Module Type Version - " + moduleTypeVersion.getModuleType().getName() + " - Version "
                + moduleTypeVersion.getVersion();
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(ModuleTypeVersionPropertiesPresenter presenter) {
        this.presenter = presenter;
        populateDatasetTypesCache();
    }

    public void display() {
        layout.removeAll();
        doLayout(layout);
        super.display();
    }

    private JTabbedPane tabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Module Type", moduleTypePanel());
        tabbedPane.addTab("Version", versionPanel());
        tabbedPane.addTab("Datasets", datasetsPanel());
        tabbedPane.addTab("Parameters", parametersPanel());
        if (moduleTypeVersion.isComposite()) {
            tabbedPane.addTab("Submodules", submodulesPanel());
            tabbedPane.addTab("Connections", connectionsPanel());
            // tabbedPane.addTab("Flowchart", flowchartPanel());
        } else {
            tabbedPane.addTab("Algorithm", algorithmPanel());
        }
        tabbedPane.addTab("Revisions", revisionsPanel());
        return tabbedPane;
    }

    private JPanel moduleTypePanel() {
        moduleTypePanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JLabel warning = new JLabel((viewMode == ViewMode.VIEW) ? "" : "Edits on this tab affect all versions of the module type.", JLabel.CENTER);
        warning.setForeground(Color.BLUE);
        layoutGenerator.addLabelWidgetPair("", warning, formPanel);
        
        moduleTypeName = new TextField("Module Type Name", 60);
        moduleTypeName.setMaximumSize(new Dimension(575, 20));
        moduleTypeName.setText(moduleType.getName());
        moduleTypeName.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(moduleTypeName);
        }
        layoutGenerator.addLabelWidgetPair("Name:", moduleTypeName, formPanel);

        moduleTypeDescription = new TextArea("Module Type Description", moduleType.getDescription(), 60, 8);
        moduleTypeDescription.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(moduleTypeDescription);
        }
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(moduleTypeDescription);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, formPanel);

        moduleTypeCreator = new Label(moduleType.getCreator().getName());
        layoutGenerator.addLabelWidgetPair("Creator:", moduleTypeCreator, formPanel);

        moduleTypeCreationDate = new Label(CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleType.getCreationDate()));
        layoutGenerator.addLabelWidgetPair("Creation Date:", moduleTypeCreationDate, formPanel);

        moduleTypeLastModifiedDate = new Label(
                CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleType.getLastModifiedDate()));
        layoutGenerator.addLabelWidgetPair("Last Modified:", moduleTypeLastModifiedDate, formPanel);

        String lockOwner = moduleType.getLockOwner();
        String safeLockOwner = (lockOwner == null) ? "" : lockOwner;
        moduleTypeLockOwner = new Label(safeLockOwner);
        layoutGenerator.addLabelWidgetPair("Lock Owner:", moduleTypeLockOwner, formPanel);

        Date lockDate = moduleType.getLockDate();
        String safeLockDate = (moduleType.getLockDate() == null) ? ""
                : CustomDateFormat.format_MM_DD_YYYY_HH_mm(lockDate);
        moduleTypeLockDate = new Label(safeLockDate);
        layoutGenerator.addLabelWidgetPair("Lock Date:", moduleTypeLockDate, formPanel);

        moduleTypeDefaultVersionNumber = new Label(moduleType.getDefaultVersion() + "");
        layoutGenerator.addLabelWidgetPair("Default Version:", moduleTypeDefaultVersionNumber, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 9, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        moduleTypePanel.add(formPanel, BorderLayout.PAGE_START);
        return moduleTypePanel;
    }

    private JPanel versionPanel() {
        versionPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        moduleTypeVersionNumber = new Label(moduleTypeVersion.getVersion() + "");
        layoutGenerator.addLabelWidgetPair("Version:", moduleTypeVersionNumber, formPanel);

        moduleTypeVersionName = new TextField("Module Type Version Name", 60);
        moduleTypeVersionName.setMaximumSize(new Dimension(575, 20));
        moduleTypeVersionName.setText(moduleTypeVersion.getName());
        moduleTypeVersionName.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(moduleTypeVersionName);
        }
        layoutGenerator.addLabelWidgetPair("Name:", moduleTypeVersionName, formPanel);

        moduleTypeVersionDescription = new TextArea("Module Type Version Description",
                moduleTypeVersion.getDescription(), 60, 8);
        moduleTypeVersionDescription.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(moduleTypeVersionDescription);
        }
        ScrollableComponent mtvDescScrollableTextArea = new ScrollableComponent(moduleTypeVersionDescription);
        mtvDescScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", mtvDescScrollableTextArea, formPanel);

        moduleTypeVersionCreator = new Label(moduleTypeVersion.getCreator().getName());
        layoutGenerator.addLabelWidgetPair("Creator:", moduleTypeVersionCreator, formPanel);

        moduleTypeVersionCreationDate = new Label(
                CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleTypeVersion.getCreationDate()));
        layoutGenerator.addLabelWidgetPair("Creation Date:", moduleTypeVersionCreationDate, formPanel);

        moduleTypeVersionLastModifiedDate = new Label(
                CustomDateFormat.format_MM_DD_YYYY_HH_mm(moduleTypeVersion.getLastModifiedDate()));
        layoutGenerator.addLabelWidgetPair("Last Modified:", moduleTypeVersionLastModifiedDate, formPanel);

        moduleTypeVersionBaseVersionNumber = new Label(moduleTypeVersion.getBaseVersion() + "");
        layoutGenerator.addLabelWidgetPair("Base Version:", moduleTypeVersionBaseVersionNumber, formPanel);

        moduleTypeVersionIsFinal = new Label(moduleTypeVersion.getIsFinal() ? "Yes" : "No");
        layoutGenerator.addLabelWidgetPair("Is Final:", moduleTypeVersionIsFinal, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 8, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        versionPanel.add(formPanel, BorderLayout.PAGE_START);
        return versionPanel;
    }

    private JPanel datasetsPanel() {
        datasetsTablePanel = new JPanel(new BorderLayout());
        datasetsTableData = new ModuleTypeVersionDatasetsTableData(moduleTypeVersion.getModuleTypeVersionDatasets());
        datasetsTable = new SelectableSortFilterWrapper(parentConsole, datasetsTableData, null);
        datasetsTablePanel.add(datasetsTable);

        datasetsPanel = new JPanel(new BorderLayout());
        datasetsPanel.add(datasetsTablePanel, BorderLayout.CENTER);
        datasetsPanel.add(datasetsCrudPanel(), BorderLayout.SOUTH);

        return datasetsPanel;
    }

    private JPanel parametersPanel() {
        parametersTablePanel = new JPanel(new BorderLayout());
        parametersTableData = new ModuleTypeVersionParametersTableData(
                moduleTypeVersion.getModuleTypeVersionParameters());
        parametersTable = new SelectableSortFilterWrapper(parentConsole, parametersTableData, null);
        parametersTablePanel.add(parametersTable);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(parametersTablePanel, BorderLayout.CENTER);
        parametersPanel.add(parametersCrudPanel(), BorderLayout.SOUTH);

        return parametersPanel;
    }

    private JPanel algorithmPanel() {
        algorithmPanel = new JPanel(new BorderLayout());
        algorithm = new TextArea("algorithm", moduleTypeVersion.getAlgorithm(), 60);
        algorithm.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        algorithm.setEditable(viewMode != ViewMode.VIEW);
        if (viewMode != ViewMode.VIEW) {
            addChangeable(algorithm);
        }
        ScrollableComponent scrollableAlgorithm = new ScrollableComponent(algorithm);
        scrollableAlgorithm.setMaximumSize(new Dimension(575, 200));
        algorithmPanel.add(scrollableAlgorithm);

        algorithmUndoManager = new UndoManager();

        algorithm.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                algorithmUndoManager.addEdit(e.getEdit());
            }
        });

        InputMap im = algorithm.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK), "Undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK), "Redo");

        ActionMap am = algorithm.getActionMap();
        am.put("Undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (algorithmUndoManager.canUndo()) {
                        algorithmUndoManager.undo();
                    }
                } catch (CannotUndoException exp) {
                    exp.printStackTrace();
                }
            }
        });
        am.put("Redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (algorithmUndoManager.canRedo()) {
                        algorithmUndoManager.redo();
                    }
                } catch (CannotUndoException exp) {
                    exp.printStackTrace();
                }
            }
        });

        return algorithmPanel;
    }

    private JPanel submodulesPanel() {
        submodulesTablePanel = new JPanel(new BorderLayout());
        submodulesTableData = new ModuleTypeVersionSubmodulesTableData(
                moduleTypeVersion.getModuleTypeVersionSubmodules());
        submodulesTable = new SelectableSortFilterWrapper(parentConsole, submodulesTableData, null);
        submodulesTablePanel.add(submodulesTable);

        submodulesPanel = new JPanel(new BorderLayout());
        submodulesPanel.add(submodulesTablePanel, BorderLayout.CENTER);
        submodulesPanel.add(submodulesCrudPanel(), BorderLayout.SOUTH);

        return submodulesPanel;
    }

    private JPanel connectionsPanel() {
        connectionsTablePanel = new JPanel(new BorderLayout());
        connectionsTableData = new ModuleTypeVersionConnectionsTableData(moduleTypeVersion);
        connectionsTable = new SelectableSortFilterWrapper(parentConsole, connectionsTableData, null);
        connectionsTablePanel.add(connectionsTable);

        connectionsPanel = new JPanel(new BorderLayout());
        connectionsPanel.add(connectionsTablePanel, BorderLayout.CENTER);
        connectionsPanel.add(connectionsCrudPanel(), BorderLayout.SOUTH);

        return connectionsPanel;
    }

    private JPanel revisionsPanel() {
        revisionsPanel = new JPanel(new BorderLayout());
        revisions = new TextArea("revisions", moduleTypeVersion.revisionsReport(), 60);
        revisions.setEditable(false);
        revisions.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableRevisionsReport = new ScrollableComponent(revisions);
        scrollableRevisionsReport.setMaximumSize(new Dimension(575, 200));
        revisionsPanel.add(scrollableRevisionsReport);

        revisions.setCaretPosition(revisions.getDocument().getLength());

        return revisionsPanel;
    }

    @Override
    public void refreshRevisions() {
        revisions.setText(moduleTypeVersion.revisionsReport());
        revisions.setCaretPosition(revisions.getDocument().getLength());
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button validateButton = new Button("Validate", validateAction());
        validateButton.setMnemonic('V');

        Button newRevisionButton = new Button("New Revision", newRevisionAction());
        newRevisionButton.setMnemonic('R');
        newRevisionButton.setEnabled(viewMode != ViewMode.VIEW);

        Button saveButton = new SaveButton(saveAction());
        saveButton.setEnabled((viewMode != ViewMode.VIEW) && !moduleTypeVersion.getIsFinal());

        Button finalizeButton = new Button("Finalize", finalizeAction());
        finalizeButton.setMnemonic('F');
        finalizeButton.setEnabled((viewMode != ViewMode.VIEW) && !moduleTypeVersion.getIsFinal());

        Button closeButton = new CloseButton("Close", closeAction());

        container.add(validateButton);
        container.add(newRevisionButton);
        container.add(saveButton);
        container.add(finalizeButton);
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel datasetsCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editDatasets();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, datasetsTable, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createDataset();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeDatasets();
            }
        };
        Button removeButton = new RemoveButton(removeAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(newButton);
        crudPanel.add(editButton);
        crudPanel.add(removeButton);
        if (viewMode == ViewMode.VIEW) {
            newButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editDatasets() {
        List selected = selectedDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more datasets");
            return;
        }

        getDatasetTypesTask.done(); // wait if necessary

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersionDataset moduleTypeVersionDataset = (ModuleTypeVersionDataset) iter.next();
            ModuleTypeVersionDatasetWindow view = new ModuleTypeVersionDatasetWindow(parentConsole, desktopManager,
                    session, moduleTypeVersion, datasetTypesCache, ViewMode.EDIT, moduleTypeVersionDataset);
            presenter.displayModuleTypeVersionDatasetView(view);
        }
    }

    private void createDataset() {
        getDatasetTypesTask.done(); // wait if necessary
        ModuleTypeVersionDatasetWindow view = new ModuleTypeVersionDatasetWindow(parentConsole, desktopManager, session,
                moduleTypeVersion, datasetTypesCache, ViewMode.NEW, null);
        presenter.displayModuleTypeVersionDatasetView(view);
    }

    public void refreshDatasets() {
        datasetsTableData = new ModuleTypeVersionDatasetsTableData(moduleTypeVersion.getModuleTypeVersionDatasets());
        datasetsTable.refresh(datasetsTableData);
        refreshConnections();
        isDirty = true;
    }

    private void removeDatasets() {
        messagePanel.clear();
        List<?> selected = selectedDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more datasets");
            return;
        }

        String message = "Are you sure you want to remove the selected " + selected.size() + " dataset(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                for (ModuleTypeVersionDataset moduleTypeVersionDataset : selected
                        .toArray(new ModuleTypeVersionDataset[0])) {
                    moduleTypeVersion.removeModuleTypeVersionDataset(moduleTypeVersionDataset);
                }
                messagePanel.setMessage("Removed " + selected.size() + " dataset(s)");
                refreshDatasets();
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private List selectedDatasets() {
        return datasetsTable.selected();
    }

    private JPanel parametersCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editParameters();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, parametersTable, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createParameter();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeParameters();
            }
        };
        Button removeButton = new RemoveButton(removeAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(newButton);
        crudPanel.add(editButton);
        crudPanel.add(removeButton);
        if (viewMode == ViewMode.VIEW) {
            newButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editParameters() {
        List selected = selectedParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more parameters");
            return;
        }

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersionParameter moduleTypeVersionParameter = (ModuleTypeVersionParameter) iter.next();
            ModuleTypeVersionParameterWindow view = new ModuleTypeVersionParameterWindow(parentConsole, desktopManager,
                    session, moduleTypeVersion, ViewMode.EDIT, moduleTypeVersionParameter);
            presenter.displayModuleTypeVersionParameterView(view);
        }
    }

    private void createParameter() {
        ModuleTypeVersionParameterWindow view = new ModuleTypeVersionParameterWindow(parentConsole, desktopManager,
                session, moduleTypeVersion, ViewMode.NEW, null);
        presenter.displayModuleTypeVersionParameterView(view);
    }

    public void refreshParameters() {
        parametersTableData = new ModuleTypeVersionParametersTableData(
                moduleTypeVersion.getModuleTypeVersionParameters());
        parametersTable.refresh(parametersTableData);
        refreshConnections();
        isDirty = true;
    }

    private void removeParameters() {
        messagePanel.clear();
        List<?> selected = selectedParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more parameters");
            return;
        }

        String message = "Are you sure you want to remove the selected " + selected.size() + " parameter(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                for (ModuleTypeVersionParameter moduleTypeVersionParameter : selected
                        .toArray(new ModuleTypeVersionParameter[0])) {
                    moduleTypeVersion.removeModuleTypeVersionParameter(moduleTypeVersionParameter);
                }
                messagePanel.setMessage("Removed " + selected.size() + " parameter(s)");
                refreshParameters();
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private List selectedParameters() {
        return parametersTable.selected();
    }

    // -------------------------------------------------------------------------

    private JPanel submodulesCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editSubmodules();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, submodulesTable, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createSubmodule();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeSubmodules();
            }
        };
        Button removeButton = new RemoveButton(removeAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(newButton);
        crudPanel.add(editButton);
        crudPanel.add(removeButton);
        if (viewMode == ViewMode.VIEW) {
            newButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editSubmodules() {
        List selected = selectedSubmodules();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more submodules");
            return;
        }

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersionSubmodule moduleTypeVersionSubmodule = (ModuleTypeVersionSubmodule) iter.next();
            ModuleTypeVersionSubmoduleWindow view = new ModuleTypeVersionSubmoduleWindow(parentConsole, desktopManager,
                    session, moduleTypeVersion, ViewMode.EDIT, moduleTypeVersionSubmodule);
            presenter.displayModuleTypeVersionSubmoduleView(view);
        }
    }

    private void createSubmodule() {
        ModuleTypeVersionSubmoduleWindow view = new ModuleTypeVersionSubmoduleWindow(parentConsole, desktopManager,
                session, moduleTypeVersion, ViewMode.NEW, null);
        presenter.displayModuleTypeVersionSubmoduleView(view);
    }

    public void refreshSubmodules() {
        submodulesTableData = new ModuleTypeVersionSubmodulesTableData(
                moduleTypeVersion.getModuleTypeVersionSubmodules());
        submodulesTable.refresh(submodulesTableData);
        refreshConnections();
        isDirty = true;
    }

    private void removeSubmodules() {
        messagePanel.clear();
        List<?> selected = selectedSubmodules();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more submodules");
            return;
        }

        String message = "Are you sure you want to remove the selected " + selected.size() + " submodule(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (selection == JOptionPane.YES_OPTION) {
            try {
                for (ModuleTypeVersionSubmodule moduleTypeVersionSubmodule : selected
                        .toArray(new ModuleTypeVersionSubmodule[0])) {
                    moduleTypeVersion.removeModuleTypeVersionSubmodule(moduleTypeVersionSubmodule);
                }
                messagePanel.setMessage("Removed " + selected.size() + " submodule(s)");
                refreshSubmodules();
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private List selectedSubmodules() {
        return submodulesTable.selected();
    }

    // -------------------------------------------------------------------------

    private JPanel connectionsCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editConnections();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, connectionsTable, confirmDialog);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        if (viewMode == ViewMode.VIEW) {
            editButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editConnections() {
        List selected = selectedConnections();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more connections");
            return;
        }

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Object item = iter.next();
            if (item instanceof ModuleTypeVersionDatasetConnection) {
                ModuleTypeVersionDatasetConnection datasetConnection = (ModuleTypeVersionDatasetConnection) item;
                ModuleTypeVersionDatasetConnectionWindow view = new ModuleTypeVersionDatasetConnectionWindow(
                        parentConsole, desktopManager, session, ViewMode.EDIT, datasetConnection);
                presenter.displayModuleTypeVersionDatasetConnectionView(view);
            } else if (item instanceof ModuleTypeVersionParameterConnection) {
                ModuleTypeVersionParameterConnection parameterConnection = (ModuleTypeVersionParameterConnection) item;
                ModuleTypeVersionParameterConnectionWindow view = new ModuleTypeVersionParameterConnectionWindow(
                        parentConsole, desktopManager, session, ViewMode.EDIT, parameterConnection);
                presenter.displayModuleTypeVersionParameterConnectionView(view);
            }
        }
    }

    public void refreshConnections() {
        if (moduleTypeVersion.isComposite()) {
            connectionsTableData = new ModuleTypeVersionConnectionsTableData(moduleTypeVersion);
            connectionsTable.refresh(connectionsTableData);
            isDirty = true;
        }
    }

    private List selectedConnections() {
        return connectionsTable.selected();
    }

    // -------------------------------------------------------------------------

    private void clear() {
        messagePanel.clear();
    }

    private boolean checkTextFields() {
        if (moduleTypeName.getText().equals(""))
            messagePanel.setError("Name field should be a non-empty string.");
        else {
            messagePanel.clear();
            return true;
        }

        return false;
    }

    public static void showLargeErrorMessage(SingleLineMessagePanel messagePanel, String title, String error) {
        // TODO add this code to SingleLineMessagePanel.setMessage() instead
        if (error.length() > 80 || error.contains("\n")) {
            messagePanel.setError(title);
            JTextArea errorTextArea = new JTextArea(error.toString());
            errorTextArea.setEditable(false);
            errorTextArea.setLineWrap(true);
            errorTextArea.setWrapStyleWord(true);
            JScrollPane errorScrollPane = new JScrollPane(errorTextArea) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(600, 400);
                }
            };
            JOptionPane.showMessageDialog(null, errorScrollPane, title, JOptionPane.ERROR_MESSAGE);
        } else {
            messagePanel.setError(error.toString());
        }
    }

    private Action validateAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                StringBuilder error = new StringBuilder();
                if (moduleTypeVersion.isValid(error)) {
                    messagePanel.setMessage("This module type version is valid.");
                } else {
                    showLargeErrorMessage(messagePanel, "Validation failed!", error.toString());
                }
            }
        };

        return action;
    }

    private Action newRevisionAction() {
        final ModuleTypeVersionPropertiesWindow moduleTypeVersionPropertiesWindow = this;
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                ModuleTypeVersionNewRevisionDialog newRevisionView = new ModuleTypeVersionNewRevisionDialog(
                        parentConsole, moduleTypeVersion, moduleTypeVersionPropertiesWindow);
                ModuleTypeVersionNewRevisionPresenter newRevisionPresenter = new ModuleTypeVersionNewRevisionPresenter(
                        newRevisionView, session);
                try {
                    newRevisionPresenter.display();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        return action;
    }

    private boolean doSave() {
        try {
            if (viewMode == ViewMode.NEW) {
                moduleType.setName(moduleTypeName.getText());
                moduleType.setDescription(moduleTypeDescription.getText());
                moduleTypeVersion.setName(moduleTypeVersionName.getText());
                moduleTypeVersion.setDescription(moduleTypeVersionDescription.getText());
                if (!moduleTypeVersion.isComposite()) {
                    moduleTypeVersion.setAlgorithm(algorithm.getText());
                }

                moduleType = presenter.addModuleType(moduleType);
                viewMode = ViewMode.EDIT;
                ModuleType lockedModuleType = presenter.obtainLockedModuleType(moduleType.getId());
                if (lockedModuleType == null || !lockedModuleType.isLocked(session.user())) {
                    throw new EmfException("Failed to lock module type.");
                }
                moduleType = lockedModuleType;
                moduleTypeVersion = moduleType.getModuleTypeVersions().get(moduleTypeVersion.getVersion());
                mustUnlock = true;
            } else {
                ModuleTypeVersionNewRevisionDialog newRevisionView = new ModuleTypeVersionNewRevisionDialog(
                        parentConsole, moduleTypeVersion, this);
                ModuleTypeVersionNewRevisionPresenter newRevisionPresenter = new ModuleTypeVersionNewRevisionPresenter(
                        newRevisionView, session);
                try {
                    newRevisionPresenter.display();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                Date date = new Date();
                moduleType.setName(moduleTypeName.getText());
                moduleType.setDescription(moduleTypeDescription.getText());
                moduleType.setLastModifiedDate(date);

                moduleTypeVersion.setName(moduleTypeVersionName.getText());
                moduleTypeVersion.setDescription(moduleTypeVersionDescription.getText());
                moduleTypeVersion.setLastModifiedDate(date);
                if (!moduleTypeVersion.isComposite()) {
                    moduleTypeVersion.setAlgorithm(algorithm.getText());
                }

                moduleType = presenter.updateModuleTypeVersion(moduleTypeVersion, session.user());
                moduleTypeVersion = moduleType.getModuleTypeVersions().get(moduleTypeVersion.getVersion());
            }
            messagePanel.setMessage("Saved module type version.");

            resetChanges();
            isDirty = false;

        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return false;
        }

        return true;
    }

    private void doCascadeSave() {
        if (!checkTextFields())
            return;

        doSave();
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doCascadeSave();
            }
        };

        return action;
    }

    private void doFinalize() {
        StringBuilder error = new StringBuilder();
        if (!moduleTypeVersion.isValid(error)) {
            messagePanel.setError("Can't finalize. This module type version is invalid. " + error.toString());
            return;
        }
        if (hasChanges() || isDirty) {
            messagePanel.setError("Can't finalize. You must save changes first. " + error.toString());
            return;
        }

        String title = moduleTypeVersion.fullNameSDS("%s - Version %d - %s");
        String message = "Are you sure you want to finalize this module type version?";
        
        TreeMap<Integer, ModuleTypeVersion> unfinalizedSubmodules = moduleTypeVersion.getUnfinalizedSubmodules();
        if (unfinalizedSubmodules.size() > 0) {
            message += "\n\nThe following module type versions will be finalized also:\n";
            for (ModuleTypeVersion unfinalizedModuleTypeVersion : unfinalizedSubmodules.values()) {
                message += unfinalizedModuleTypeVersion.fullNameSDS("%s - Version %d - %s\n");
            }
        }
        
        int selection = JOptionPane.showConfirmDialog(parentConsole,
                                        message, title, JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
        
        error.setLength(0);
        for (ModuleTypeVersion unfinalizedModuleTypeVersion : unfinalizedSubmodules.values()) {
            ModuleType moduleType = unfinalizedModuleTypeVersion.getModuleType();
            if (moduleType.isLocked(session.user()))
                continue;
            if (moduleType.isLocked()) {
                String errorMessage = String.format("Module type \"%s\" has been locked by %s since %s.\n",
                                                    moduleType.getName(), moduleType.getLockOwner(),
                                                    CustomDateFormat.format_YYYY_MM_DD_HH_MM(moduleType.getLockDate()));
                error.append(errorMessage);
            }
        }
        if (error.length() > 0) {
            showLargeErrorMessage(messagePanel, "Can't finalize this module type version!", error.toString());
            return;
        }

        try {
            moduleType = presenter.finalizeModuleTypeVersion(moduleTypeVersion.getId(), session.user());
            if (moduleType == null)
                throw new EmfException("Failed to get the updated module type.");
            moduleTypeVersion = moduleType.getModuleTypeVersions().get(moduleTypeVersion.getVersion());
            moduleTypeVersionIsFinal.setText(moduleTypeVersion.getIsFinal() ? "Yes" : "No");
            JOptionPane.showConfirmDialog(parentConsole, "This module type version has been finalized!", title,
                    JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
            doClose();
        } catch (Exception e) {
            e.printStackTrace();
            showLargeErrorMessage(messagePanel, "Failed to finalize this module type version!", e.getMessage());
        }
    }

    private Action finalizeAction() {
        final ModuleTypeVersionPropertiesWindow moduleTypeVersionPropertiesWindow = this;
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                moduleTypeVersionPropertiesWindow.doFinalize();
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {

        if (isDirty) {
            int selection = JOptionPane.showConfirmDialog(parentConsole,
                    "Are you sure you want to close without saving?", "Module Type Version Properties",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selection == JOptionPane.NO_OPTION)
                return;
        } else if (!shouldDiscardChanges()) {
            return;
        }

        // TODO if new module type version has never been saved, remove it from the module type object
        if (mustUnlock) {
            moduleType = presenter.releaseLockedModuleType(moduleType.getId());
        }
        presenter.doClose();
        moduleTypeVersionObserver.closedChildWindow(moduleTypeVersion, initialViewMode);
    }

    public void populateDatasetTypesCache() {
        // long running methods.....
        // this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // ComponentUtility.enableComponents(this, false);
        getDatasetTypesTask = new GetDatasetTypesTask(this);
        getDatasetTypesTask.execute();
    }
}
