package org.activiti.designer.eclipse.util;

import java.util.Map;

import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class RefreshDiagramHandler {		
	public static void refreshDiagram() {
		new RefreshDiagramThread().start();
	}
	
	static class RefreshDiagramThread extends Thread {
		@Override
	     public void run() {
	    	 Display display = Display.getDefault();
	         display.syncExec(new Runnable() {
	        	 @Override
	             public void run() {
	        		 refresh();  
	             }
	         });

	      }    
	}	
	
	private static void refresh() {
		 IFile dataFile = FileService.getActiveDiagramFile();
		 String name = dataFile.getName();
		 String diagramName = FileService.getDiagramName(dataFile);
		 final Map<String, String> model = DiagramHandler.getDiagramByName(diagramName, ActivitiPlugin.getModels(false));
		 String id = DiagramHandler.getDiagramId(model);
		 //Save the bpmn diagram file
		 ActivitiDiagramEditor.get().doSave(dataFile, id); 
		 	
		 //close current editor
		 IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		 IWorkbenchPage page = workbenchWindow.getActivePage();
		 IEditorReference[] editorRefs = page.getEditorReferences();
		 
		 //Get the actual editor part from the references with:
		 for (IEditorReference editorRef : editorRefs) {
			 IEditorPart editor = editorRef.getEditor(true);		 
			 //Get the editor input:
			 IEditorInput input = editor.getEditorInput();
			 //Get the file the editor is editing:
			 IFile file = (IFile)input.getAdapter(IFile.class);
			 if (file.getName().startsWith(name)) {
				 //Close an editor:
				 page.closeEditor(editor, true);
				 break;
			 }
		 }
		 DiagramHandler.openDiagramForBpmnFile(dataFile).isOK();
	}	
}