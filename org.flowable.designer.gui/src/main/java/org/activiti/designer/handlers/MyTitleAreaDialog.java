package org.activiti.designer.handlers;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.activiti.designer.util.DiagramHandler;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MyTitleAreaDialog extends TitleAreaDialog {

    private Text diagramNameText;
    private String diagramName;
    private String currentDiagramName;
    private Shell shell;
    
    public MyTitleAreaDialog(Shell parentShell, String currentDiagramName) {
    	super(parentShell);
        this.currentDiagramName = currentDiagramName;
        shell = parentShell;
    }

    @Override
    public void create() {
        super.create();
        
        if (currentDiagramName.isEmpty()) {
        	setTitle("Your new Diagram  will be Created");
        	setMessage("Please type new diagram name to be created", IMessageProvider.INFORMATION);
        } else { 
        	setTitle("Your current Diagram " + currentDiagramName + " will be Saved As...");
        	setMessage("Please type new diagram name to be saved", IMessageProvider.INFORMATION);
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
        Label lbtFirstName = new Label(container, SWT.NONE);
        lbtFirstName.setText("Diagram Name");

        GridData dataFirstName = new GridData();
        dataFirstName.grabExcessHorizontalSpace = true;
        dataFirstName.horizontalAlignment = GridData.FILL;

        diagramNameText = new Text(container, SWT.BORDER);
        diagramNameText.setLayoutData(dataFirstName);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    // save content of the Text fields because they get disposed
    // as soon as the Dialog closes
    private boolean saveInput() {
    	diagramName = diagramNameText.getText();        
    	int result = DiagramHandler.isDiagramExist(diagramName);
    	if (result == 0)
    		return true;
    	MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
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