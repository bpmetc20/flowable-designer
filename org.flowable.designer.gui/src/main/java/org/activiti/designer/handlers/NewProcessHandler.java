package org.activiti.designer.handlers;

import org.activiti.designer.eclipse.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.window.Window;

public class NewProcessHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		MyTitleAreaDialog dialog = new MyTitleAreaDialog("", "Your new Diagram will be Created", 
				"Please type new diagram name to be created", false);
		dialog.create();
		if (dialog.open() == Window.OK) {
			DiagramHandler.createNewDiagram(dialog.getDiagramName());	    
		}		
		return window;
	}	
}
