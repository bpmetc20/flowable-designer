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


public class SaveAsProcessHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);		
		
		IFile dataFile = FileService.getCurrentDiagramFile();
		String diagramName = FileService.getDiagramName(dataFile);
		MyTitleAreaDialog dialog = new MyTitleAreaDialog(window.getShell(), diagramName);
		dialog.create();
		if (dialog.open() == Window.OK) {
			DiagramHandler.saveDiagramAS(dataFile, dialog.getDiagramName(), window.getShell());		    
		}		
		return window;
	}	
}