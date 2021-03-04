package org.activiti.designer.handlers;

import org.activiti.designer.eclipse.util.DiagramHandler;
import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.eclipse.util.RefreshDiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshProcessHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		IFile dataFile = FileService.getActiveDiagramFile();
		if (dataFile == null) {
			DiagramHandler.showMessageBoxError("Please load diagram first!");			
		} else {
			RefreshDiagramHandler.refreshDiagram(null);
		}		
		return window;
		
	}	
}