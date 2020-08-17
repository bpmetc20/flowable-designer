package org.activiti.designer.handlers;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.util.DiagramHandler;
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
    private String diagramName = "";    
            
    public MyTitleAreaDialog(String diagramName) {
    	super(ActivitiPlugin.getShell());
        
    	this.diagramName = diagramName.isEmpty() ? DiagramHandler.newDiagramName : diagramName;
    }

    @Override
    public void create() {
        super.create();
        
        if (diagramName.equalsIgnoreCase(DiagramHandler.newDiagramName)) {
        	setTitle("Your new Diagram will be Created");
        	setMessage("Please type new diagram name and process attributed to be created", IMessageProvider.INFORMATION);
        } else { 
        	setTitle("Your current Diagram will be Saved As...");
        	setMessage("Please type new diagram name and process attributed to be saved", IMessageProvider.INFORMATION);
        }               
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);        
               
        createDiagramName(container);
               
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

    @Override
    protected boolean isResizable() {
        return true;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private boolean saveInput() {
    	diagramName = diagramNameText.getText();
    	if (diagramName.isEmpty()) {
    		showMesaage("Diagram name should not be empty!");
    		return false;
    	}    	
    	    	
    	if (DiagramHandler.isDiagramExist(diagramName) != 0) {
    		showMesaage("Already exist " + diagramName);
        	return false;    		
    	}	
    	return true;
    }

    @Override
    protected void okPressed() {
        if (saveInput())
        	super.okPressed();        
    }

    public String getDiagramName() {
        return diagramName;
    }    
    
    private void showMesaage(String message) {
    	MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), SWT.ICON_WARNING | SWT.OK);
    	messageBox.setText("Warning");
    	messageBox.setMessage(message);
    	messageBox.open();	
    }
}