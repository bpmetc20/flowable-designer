package org.activiti.designer.handlers;

import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class SaveProcessHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return Boolean.toString(DiagramHandler.saveDiagram());
	}	
}