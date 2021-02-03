package org.activiti.designer.handlers;

import org.activiti.designer.eclipse.util.DiagramHandler;
import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.util.dialog.MyTitleAreaDialog;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.window.Window;

public class DeploymenProcesstHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);		
		IFile dataFile = FileService.getActiveDiagramFile();
		if (dataFile == null) {
			DiagramHandler.showMessageBoxError("Please load diagram first!");
		} else {				
		 	MyTitleAreaDialog dialog = new MyTitleAreaDialog(FileService.getDiagramName(dataFile), "Your current Diagram will be Deployed",
		 			"Please type deployment name", true);
			dialog.create();
			if (dialog.open() == Window.OK) {
				DiagramHandler.deployModel(dataFile, dialog.getDiagramName());		    
			}
		}
		return window;
	}
}
