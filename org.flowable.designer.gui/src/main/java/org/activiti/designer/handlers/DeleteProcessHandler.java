package org.activiti.designer.handlers;

import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteProcessHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String diagramName = DiagramHandler.getCurrentDiagramName();
		
		if (diagramName.isEmpty()) {
			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_INFORMATION | SWT.OK );
	    	  messageBox.setText("Info");
	    	  messageBox.setMessage("Can't find digram. Please open diagram and try again ...");
	    	  messageBox.open();
	    	  return diagramName;
		}		
		return Boolean.toString(DiagramHandler.deleteDiagram(diagramName, window.getShell()));
	}	
}