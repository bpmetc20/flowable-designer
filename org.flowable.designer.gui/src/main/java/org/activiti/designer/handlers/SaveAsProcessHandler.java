package org.activiti.designer.handlers;

import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.window.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;


public class SaveAsProcessHandler extends AbstractHandler {
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
		 	MyTitleAreaDialog dialog = new MyTitleAreaDialog(FileService.getDiagramName(dataFile), true);
			dialog.create();
			if (dialog.open() == Window.OK) {
				DiagramHandler.saveDiagramAS(dataFile, dialog.getDiagramName());		    
			}
		}
		return window;
	}	
}