package org.activiti.designer.util.dialog;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MyGatewayAreaDialog extends TitleAreaDialog {

    private Combo conditionText;
    private String connectionLabel = ""; 
    private String gatewayLabel = ""; 
    private String[] conditionValues;     
    private String title = "Please add condition to your %s %s connection";
    private String selectedValue = "";
            
    public MyGatewayAreaDialog(String connectionLabel, String getewayLabel, String[] conditionValues) {
    	super(ActivitiPlugin.getShell());
        
    	this.connectionLabel = connectionLabel;
    	this.gatewayLabel = getewayLabel;
    	this.conditionValues = conditionValues;
    }

    @Override
    public void create() {
        super.create();
        
        setTitle(String.format(title, gatewayLabel, connectionLabel));
        setMessage("", IMessageProvider.INFORMATION);                       
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);        
               
        Label connectionName = new Label(container, SWT.NONE);
        connectionName.setText("Condition");

        GridData dataDiagramName = new GridData();
        dataDiagramName.grabExcessHorizontalSpace = true;
        dataDiagramName.horizontalAlignment = GridData.FILL;

        
        conditionText = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
        conditionText.setLayoutData(dataDiagramName);
        conditionText.setItems(conditionValues);
        conditionText.select(0);
                       
        return area;
    }  
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {    	
    	createButton(parent, IDialogConstants.OK_ID, "OK", true);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }    

    @Override
    protected void okPressed() {
    	selectedValue = conditionText.getText();
        super.okPressed();        
    }

    public String getConditionValue() {
        return selectedValue;
    }    
    
    private void showMesaage(String message) {
    	MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), SWT.ICON_WARNING | SWT.OK);
    	messageBox.setText("Warning");
    	messageBox.setMessage(message);
    	messageBox.open();	
    }
}