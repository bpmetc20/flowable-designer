package org.activiti.designer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;



public class FormHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageBox messageBox = new MessageBox(window.getShell(), SWT.ICON_WARNING | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
		messageBox.setText("Warning");
		messageBox.setMessage("Save the changes before exiting?");
		 int buttonID = messageBox.open();
		 switch(buttonID) {
		   case SWT.YES:
		  // saves changes ...
		case SWT.NO:
		 // exits here ...
		  break;
		 case SWT.CANCEL:
		// does nothing ...
		 }
		return null;
	}
}
