package org.activiti.designer.handlers;

import org.activiti.designer.eclipse.editor.ActivitiDiagramEditorInput;
import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class SaveProcessHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
				
		IFile dataFile = FileService.getActiveDiagramFile();
		if (dataFile == null) {
			MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_WARNING | SWT.OK);
			messageBox.setText("Warning");
			messageBox.setMessage("Please load diagram first!");
			messageBox.open();
		} else {
			String diagramName = FileService.getDiagramName(dataFile);			
			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_QUESTION | SWT.NO | SWT.YES );
		    messageBox.setText("Info");
		    messageBox.setMessage("Would you like to save " + diagramName);
		    int result = messageBox.open();
		    switch(result) {
		    	case SWT.NO:	    	  			
		    	break;
		    	case SWT.YES:
		    		DiagramHandler.saveDiagram(dataFile);
			    break;
		    }		  	  
		}				
		return window;
	}	
}