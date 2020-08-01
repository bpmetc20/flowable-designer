package org.activiti.designer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.util.workspace.ActivitiWorkspaceUtil;

public class DiagramHandler {
	public static final String newModelName = "NewDiagram";
	public static final String errorMessage = "Error Opening Activiti Diagram";
	public static final String errorSaveMessage = "Error Saving Activiti Diagram";
		
	public static void openDiagram(Map<String, String> model, Shell shell) {
		String modelId = model.get("id");;
		String modelName = model.get("name");
		long updateTime = 0; 
		
		if (modelId.isEmpty()) {						
			ErrorDialog.openError(shell, DiagramHandler.errorMessage, modelName, 
					new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", new PartInitException("Can't find diagram")));
			return;
		}	
		 
		//"lastUpdateTime": "2020-07-10T20:08:23.625-07:00"
		String lastUpdateTime = model.get("lastUpdateTime");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
		try {
			Date date = sdf.parse(lastUpdateTime);
			updateTime = date.getTime();
		} catch (ParseException ex)	{
			
		}	
		
		boolean reloadModelFromCloud = false;
		
		IFile dataFile = null;		
		try {
			dataFile = FileService.getDiagramFile(modelName);
			if (FileService.getFileContent(dataFile).isEmpty())
				reloadModelFromCloud = true;
			else {			
				IFileState[] history = dataFile.getHistory(null);
				if (history.length > 0 && history[0].getModificationTime() < updateTime)
					reloadModelFromCloud = true;
			
			}			
			if (true /*reloadModelFromCloud*/) {
				String diagram = RestClient.getModelSource(modelId);		
				FileService.writeDiagramToIFile(dataFile, diagram);
			
			}
		} catch (Exception e) {	
			ErrorDialog.openError(shell, DiagramHandler.errorMessage, modelName, 
					new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
							new PartInitException(e.getMessage())));
			return;	
		}
		
		IStatus status = openDiagramForBpmnFile(dataFile);	
		if (!status.isOK()) {
			ErrorDialog.openError(shell, "Error Opening Activiti Diagram", modelName, status);
		}		
	 }
	
	 public static void createNewDiagram(Shell shell) {
		try {			 	
			 IFile newDiagram = FileService.getDiagramFile(newModelName);	
			 ActivitiDiagramEditor.get().createNewDiagram(newDiagram);
		 } catch(Exception e) {
			 ErrorDialog.openError(shell, DiagramHandler.errorMessage, "", 
				 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
						 new PartInitException("Can't create diagram")));
		 }		 
	 }
	
	 public static List<Map<String, String>> loadModels() { 
		return RestClient.getModels();
	 }
	 
	 public static Map<String, String> loadForms() { 
		return RestClient.getForms();
	 }	 
	 
	 public static Map<String, String> loadUsers() {
		 return RestClient.getUsers();
	 }
	 
	 public static Map<String, String> loadGroups() {
		 return RestClient.getGroups();
	 }
	
	 public static String[] buildListFromMap(Map<String, String> mapString) {		
		  List<String> values = new ArrayList<String>(mapString.values());
		  Collections.sort(values);
		  return values.toArray(new String[0]);
	 }
	 
	 public static String[] buildListFromList(List<Map<String, String>> mapList, String propName) {	
		 List<String> values = new ArrayList<String>();
		 for(Map<String, String> model : mapList) {
			 values.add(model.get(propName));
		 }
		 Collections.sort(values);
		 return values.toArray(new String[0]);
	 }	 
	 
	 public static boolean deleteDiagram(Shell shell) {
		 IFile dataFile = FileService.getCurrentDiagramFile();
		 String diagramName = FileService.getDiagramName(dataFile);
		 MessageBox messageBox = new MessageBox(shell, 
				 SWT.ICON_QUESTION | SWT.NO | SWT.YES );
   	  	 messageBox.setText("Info");
   	  	 messageBox.setMessage("Would you like to delete " + diagramName);
   	  	 int result = messageBox.open();
   	  	 switch(result) {
   	  	 	case SWT.NO:	    	  			
   	  	 	return false;
   	  	 	case SWT.YES:
	    	break;
   	  	 } 	  
		 
		 final Map<String, String> model = getDiagramByName(diagramName, loadModels());
		 if (model.isEmpty()) {
			 ErrorDialog.openError(shell, DiagramHandler.errorMessage, diagramName, 
				 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while deleting diagram.", 
					 new PartInitException("Can't find diagram" + diagramName)));
	    	  return false; 
		 }
		 
		 String id = getDiagramId(model);
		 if (id.isEmpty()) {
			 ErrorDialog.openError(shell, DiagramHandler.errorMessage, diagramName, 
					 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while deleting diagram.", 
						 new PartInitException("Can't find diagram" + diagramName)));
			 return false;
		 } 
		 
		 //delete from cloud first
		 if (!RestClient.deleteModel(id)) {
			 ErrorDialog.openError(shell, DiagramHandler.errorMessage, diagramName, 
					 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while deleting diagram.", 
						 new PartInitException("Can't delete diagram" + diagramName)));		 
			 return false;
		 }
		 
		 //delete file
		 
		 try {
			 FileService.deleteFile(dataFile);			 
		 } catch(Exception e) {			 
		 }	
		 //switch to previous one		 
		 return openDiagramForBpmnFile(dataFile).isOK();		 
	 }
	 
	 public static boolean saveDiagramAS(IFile currentDiagram, String newDiagramName, Shell shell) {		 		 	 
		 try {
			 String xmlString;
			 try {
				 xmlString = FileService.getFileContent(currentDiagram);
			 } catch (Exception e) {
				 showSaveMessageBoxError(newDiagramName, shell);
				 return false; 
			 }		 
			 
			 if (!xmlString.isEmpty() && RestClient.saveNewModel(newDiagramName, xmlString) != null) {
				 IFile ifile = FileService.getDiagramFile(newDiagramName);
				 FileService.writeDiagramToIFile(ifile, xmlString);				 
				 return openDiagramForBpmnFile(ifile).isOK();				 
			 }			 	 
		 } catch(Exception e) {			 
		 }		 
		 ErrorDialog.openError(shell, DiagramHandler.errorMessage, "", 
				 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
						 new PartInitException("Can't save diagram " + newDiagramName)));
		 return false;
	 }
	 
	 public static void saveAllDiagrams(Shell shell) {	
		 List<Map<String, String>> listModels = loadModels();
		 Set<IFile> diagrams = ActivitiWorkspaceUtil.getAllDiagramDataFiles();
		 for (IFile dataFile : diagrams) {
			 saveDiagram(dataFile, shell, listModels); 
		 }
	 }
	 
	 public static void saveDiagram(Shell shell) {
		 IFile dataFile = FileService.getCurrentDiagramFile();
		 saveDiagram(dataFile, shell, loadModels());
	 }
	 
	 public static boolean saveDiagram(IFile dataFile, Shell shell, List<Map<String, String>> listModels) {
		 //final Set<IFile> result = new HashSet<IFile>();
		 //final Set<IFile> projectResources = ActivitiWorkspaceUtil.getAllDiagramDataFiles();
		 //find diagram id first
		 
		 String diagramName = FileService.getDiagramName(dataFile);
		 
		 final Map<String, String> model = getDiagramByName(diagramName, listModels);			
		 boolean existInCloud = model.isEmpty() ? false : true;    
			 	
		 //saving file first
		 if (!ActivitiDiagramEditor.get().doSave()) {
			 //no message box needed
			 return false;
		 } 
			 
		 //saved, now saving on cloud
		 
		 String xmlString;
		 try {
			 xmlString = FileService.getFileContent(dataFile);
		 } catch (Exception e) {
			 showSaveMessageBoxError(diagramName, shell);
			 return false; 
		 }
		 if (xmlString.isEmpty()) {
			 showSaveMessageBoxError(diagramName, shell);
			 return false; 
		 }
		 		 
		 if (existInCloud) {
			 String id = getDiagramId(model);
			 if (id.isEmpty()) {
				 showSaveMessageBoxError(diagramName, shell);
				 return false;
			 } 
			 if (!RestClient.updateModelSource(id, xmlString)) {
				 showSaveMessageBoxError(diagramName, shell);			 
				 return false;
			 }
		 } else {
			 if (RestClient.saveNewModel(diagramName, xmlString) == null) {
				 showSaveMessageBoxError(diagramName, shell);			 
				 return false;
			 }
		 }
		 return true;	 
     }
	 
	 public static Map<String, String> getDiagramByName(String diagramName, List<Map<String, String>> listModels) {
		 if (!diagramName.isEmpty()) {		 
		 	for(Map<String, String> model : listModels) {
		 		String modelName = getDiagramName(model);
		 		if (modelName.equals(diagramName)) 
		 			return model;			
		 	}
	 	 }
		 return new HashMap<String, String>();
	 }
	 
	 public static String getDiagramName(Map<String, String> model) {
		 return model.get("name");
	 }
	 
	 public static String getDiagramId(Map<String, String> model) {
		 return model.get("id");
	 }	 
	 
	 private static IStatus openDiagramForBpmnFile(IFile dataFile) {
		 if (!dataFile.exists()) {
			return new Status(IStatus.INFO, ActivitiPlugin.getID(), errorMessage, 
					new PartInitException("Can't find diagram")); 				
		 }
		 
		 IStatus status = new Status(IStatus.OK, ActivitiPlugin.getID(), errorMessage, null); 			
		 try {
			 FileService.openDiagramForBpmnFile(dataFile);
		 } catch (PartInitException exception) {
			 status =  new Status(IStatus.ERROR, ActivitiPlugin.getID(), errorMessage, exception); 
	     }
		 return status;
	 }	
			
	 private static void showSaveMessageBoxError(String diagramName, Shell shell) {
		 MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
		 messageBox.setText("Warning");
		 messageBox.setMessage("Error while saving the model " + diagramName);
		 messageBox.open();	
	 }
}
