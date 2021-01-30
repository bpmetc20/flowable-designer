package org.activiti.designer.handlers;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.util.DiagramHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class MyGatewayAreaDialog extends TitleAreaDialog {

    private Text conditionText;
    private String connectionLabel = ""; 
    private String gatewayLabel = ""; 
    private String conditionValue = ""; 
    private String title = "Please add condition to your %s %s connection";
    
            
    public MyGatewayAreaDialog(String connectionLabel, String getewayLabel, String conditionValue) {
    	super(ActivitiPlugin.getShell());
        
    	this.connectionLabel = connectionLabel;
    	this.gatewayLabel = getewayLabel;
    	this.conditionValue = conditionValue;
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

        conditionText = new Text(container, SWT.BORDER);
        conditionText.setLayoutData(dataDiagramName);
        conditionText.setText(conditionValue);
                       
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

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private boolean saveInput() {
    	conditionValue = conditionText.getText();
    	if (conditionValue.isEmpty()) {
    		showMesaage("Condition value swhould not be empty!");
    		return false;
    	}    	
    	return true;
    }

    @Override
    protected void okPressed() {
        if (saveInput())
        	super.okPressed();        
    }

    public String getConditionValue() {
        return conditionValue;
    }    
    
    private void showMesaage(String message) {
    	MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), SWT.ICON_WARNING | SWT.OK);
    	messageBox.setText("Warning");
    	messageBox.setMessage(message);
    	messageBox.open();	
    }
}