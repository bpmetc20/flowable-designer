package org.activiti.designer.handlers;

import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.window.*;


public class SaveAsProcessHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);		
		String currentDiagramName = DiagramHandler.getDiagramName(true);
		
		if (currentDiagramName.isEmpty()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK );
	    	  messageBox.setText("Info");
	    	  messageBox.setMessage("Can't find digram. Please open diagram and try again ...");
	    	  messageBox.open();
	    	  return window;
		}	
		
		MyTitleAreaDialog dialog = new MyTitleAreaDialog(window.getShell(), currentDiagramName);
		dialog.create();
		if (dialog.open() == Window.OK) {
			DiagramHandler.saveDiagramAS(currentDiagramName, dialog.getDiagramName(), window.getShell());		    
		}	
		return window;
	}	
	
	
	public class MyTitleAreaDialog extends TitleAreaDialog {

	    private Text diagramNameText;
	    private String diagramName;
	    private String currentDiagramName;
	    
	    public MyTitleAreaDialog(Shell parentShell, String currentDiagramName) {
	        super(parentShell);
	        this.currentDiagramName = currentDiagramName;
	    }

	    @Override
	    public void create() {
	        super.create();
	        setTitle("Your current Diagram " + currentDiagramName + " will be Saved As...");
	        setMessage("Please type new diagram name to be saved", IMessageProvider.INFORMATION);
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
	    private void saveInput() {
	    	diagramName = diagramNameText.getText();        

	    }

	    @Override
	    protected void okPressed() {
	        saveInput();
	        super.okPressed();
	    }

	    public String getDiagramName() {
	        return diagramName;
	    }	    
	}
}