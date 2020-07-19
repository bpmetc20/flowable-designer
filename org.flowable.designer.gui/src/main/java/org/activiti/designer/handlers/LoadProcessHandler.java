package org.activiti.designer.handlers;

import java.util.List;
import java.util.Map;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.LabelProvider;

public class LoadProcessHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		List<Map<String, String>> loadedModels = DiagramHandler.loadModels();
		final String[] tasksArray = DiagramHandler.buildListFromList(loadedModels, "name");
				
		if (tasksArray != null && tasksArray.length > 0) {
			String modelName = selectProcess(window, tasksArray);		
		
			if (!modelName.isEmpty()) {				
				for(Map<String, String> model : loadedModels) {
					if (model.get("name").equals(modelName)) {
						DiagramHandler.openDiagram(model, window.getShell());
						return window;
					}
				}										
				ErrorDialog.openError(window.getShell(), DiagramHandler.errorMessage, modelName, 
						new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", new PartInitException("Can't find diagram")));
			}
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
