package org.activiti.designer.handlers;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import java.util.List;

import org.activiti.bpmn.model.Process;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.DiagramHandler;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class MyTitleAreaDialog extends TitleAreaDialog {

    private Text diagramNameText;
    private Text processNameText;
    private Text processIdText;
    
    private String diagramName = "";
    private String processName = "";
    private String processId = "";
    private boolean changeName = false;
        
    public MyTitleAreaDialog(String diagramName, boolean changeName) {
    	super(ActivitiPlugin.getShell());
        
    	this.diagramName = diagramName;
        this.changeName = changeName;        
        
		if (!diagramName.isEmpty()) {
			try {
				List<Process> processes = DiagramHandler.getProcesses();
				if (!processes.isEmpty()) {
					processName = processes.get(0).getName();
					processId = processes.get(0).getId();
				}
			} catch (Exception ex) {
				
			}
		} else {
			this.diagramName = DiagramHandler.newDiagramName;			
		}
    }

    @Override
    public void create() {
        super.create();
        
        if (changeName) {
        	if (diagramName.equalsIgnoreCase(DiagramHandler.newDiagramName)) {
        		setTitle("Your new Diagram will be Created");
        		setMessage("Please type new diagram name and process attributed to be created", IMessageProvider.INFORMATION);
        	} else { 
        		setTitle("Your current Diagram " + diagramName + " will be Saved As...");
        		setMessage("Please type new diagram name and process attributed to be saved", IMessageProvider.INFORMATION);
        	}
        } else {
        	setTitle("Your current Diagram " + diagramName + " will be Saved");
    		setMessage("Please type process attributed to be saved", IMessageProvider.INFORMATION);
        }        
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);        
        
        if (changeName)
        	createDiagramName(container);
        
        createProcessName(container);
        
        createProcessId(container);
        
        return area;
    }

    private void createDiagramName(Composite container) {
        Label lbtDiagramName = new Label(container, SWT.NONE);
        lbtDiagramName.setText("Diagram Name");

        GridData dataDiagramName = new GridData();
        dataDiagramName.grabExcessHorizontalSpace = true;
        dataDiagramName.horizontalAlignment = GridData.FILL;

        diagramNameText = new Text(container, SWT.BORDER);
        diagramNameText.setLayoutData(dataDiagramName);
        if (diagramName.isEmpty()) 
        	diagramName = "NewDiagram";                 	
        diagramNameText.setText(diagramName);
    }
    
    private void createProcessName(Composite container) {    	
        Label lbtProcessName = new Label(container, SWT.NONE);
        lbtProcessName.setText("Process Name");

        GridData dataProcessName = new GridData();
        dataProcessName.grabExcessHorizontalSpace = true;
        dataProcessName.horizontalAlignment = GridData.FILL;
        
        processNameText = new Text(container, SWT.BORDER);
        processNameText.setLayoutData(dataProcessName);
        processNameText.setText(processName);
    }
    
    private void createProcessId(Composite container) {
        Label lbtProcessID = new Label(container, SWT.NONE);
        lbtProcessID.setText("Process ID");

        GridData dataProcessID  = new GridData();
        dataProcessID.grabExcessHorizontalSpace = true;
        dataProcessID.horizontalAlignment = GridData.FILL;
        
        processIdText = new Text(container, SWT.BORDER);
        processIdText.setLayoutData(dataProcessID);
        processIdText.setText(processId);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private boolean saveInput() {
    	diagramName = diagramNameText.getText();
    	processName = processNameText.getText();
    	processId = processIdText.getText();
    	
    	int result = DiagramHandler.isDiagramExist(diagramName);
    	if (result == 0)
    		return true;
    	MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), SWT.ICON_WARNING | SWT.OK);
		messageBox.setText("Warning");
		messageBox.setMessage("Already exist " + diagramName);
		messageBox.open();	
    	return false;
    }

    @Override
    protected void okPressed() {
        if (saveInput())
        	super.okPressed();       
    }

    public String getDiagramName() {
        return diagramName;
    }	    
}