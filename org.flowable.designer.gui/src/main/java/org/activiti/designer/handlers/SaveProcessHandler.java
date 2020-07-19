package org.activiti.designer.handlers;

import org.eclipse.swt.widgets.*;

import java.util.Map;
import java.util.Objects;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.DiagramHandler;
import org.activiti.designer.util.RestClient;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.LabelProvider;

public class SaveProcessHandler extends AbstractHandler {
	
	private String modelId = "";
	
	public final class BreakLoopException  extends RuntimeException {}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		try {
			String dataFile = DiagramHandler.getCurrentDiagramFile().getName();
			DiagramHandler.saveDiagram();
		} catch (Exception e) {
			ErrorDialog.openError(window.getShell(), DiagramHandler.errorSaveMessage, "", 
					new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while saving diagram.", 
							new PartInitException("Can't find diagram")));
			
		}
		return window;		
	}
	
	public String selectProcess(IWorkbenchWindow window, String[] elements) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(window.getShell(), new LabelProvider());

        dialog.setTitle("Select Process");

        dialog.setMessage("&Select a process from the list. Type word using filter");

        dialog.setElements(elements);

        String selected = "";
        
        dialog.setMultipleSelection(false);
        int returnCode = dialog.open();
        if (returnCode == 0) {
            selected = (String) dialog.getFirstResult();
        }
        return selected;
	}		
}
