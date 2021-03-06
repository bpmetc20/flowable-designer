package org.activiti.designer.eclipse.util;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PartInitException;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.extension.UserTaskProperties;
import org.activiti.designer.util.workspace.ActivitiWorkspaceUtil;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;

public class DiagramHandler {	
	public static final String errorMessage = "Error Opening Activiti Diagram";
	public static final String errorSaveMessage = "Error Saving Activiti Diagram";
	
	public static List<Process> getProcesses() {
		BpmnMemoryModel bpmnMemoryModel = ActivitiDiagramEditor.get().getModel();
		return bpmnMemoryModel.getBpmnModel().getProcesses();		
	}	
	
	public static int isDiagramExist(String diagramName) {
		//check cloud first
		final Map<String, String> model = getDiagramByName(diagramName, ActivitiPlugin.getModels(true));
		if (!model.isEmpty())
			return 1;
		//check local
		Set<IFile> diagrams = ActivitiWorkspaceUtil.getAllDiagramDataFiles();
		for (IFile dataFile : diagrams) {
			if (FileService.getDiagramName(dataFile).equalsIgnoreCase(diagramName))
				return -1;
		}
		return 0;
	}
	
	
	public static void openDiagram(Map<String, String> model) {
		String modelId = model.get("id");;
		String modelName = model.get("name");
		long updateTime = 0; 
		
		if (modelId.isEmpty()) {						
			ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, modelName, 
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
			ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, modelName, 
					new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
							new PartInitException(e.getMessage())));
			return;	
		}
		
		IStatus status = openDiagramForBpmnFile(dataFile);	
		if (!status.isOK()) {
			ErrorDialog.openError(ActivitiPlugin.getShell(), "Error Opening Activiti Diagram", modelName, status);
		}		
	 }
	
	 public static void createNewDiagram(String newDiagramName) {
		try {			 	
			 IFile newDiagram = FileService.getDiagramFile(newDiagramName);	
			 ActivitiDiagramEditor.get().createNewDiagram(newDiagram);
		 } catch(Exception e) {
			 ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, "", 
				 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
						 new PartInitException("Can't create diagram")));
		 }		 
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
	 
	 public static boolean deleteDiagram(IFile dataFile) {
		 String diagramName = FileService.getDiagramName(dataFile);
		 if (!showYesNoMessageBox("Would you like to delete " + diagramName))
			 return false;		 
		 
		 final Map<String, String> model = getDiagramByName(diagramName, ActivitiPlugin.getModels(true));
		 if (model.isEmpty()) {
			 ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, diagramName, 
				 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while deleting diagram.", 
					 new PartInitException("Can't find diagram" + diagramName)));
	    	  return false; 
		 }
		 
		 String id = getDiagramId(model);
		 if (id.isEmpty()) {
			 ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, diagramName, 
					 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while deleting diagram.", 
						 new PartInitException("Can't find diagram" + diagramName)));
			 return false;
		 } 
		 
		 //delete from cloud first
		 if (!RestClient.deleteModel(id)) {
			 ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, diagramName, 
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
	 
	 public static boolean saveDiagramAS(IFile currentDiagram, String newDiagramName) {			 
		 String modelId = RestClient.createNewModel(newDiagramName);
		 String xmlString;	
		 String errorMessge = "Error while saving the model " + newDiagramName;	
		 IFile ifile;
		 try {			 
			 if (ActivitiDiagramEditor.get().isDirty()) {
				 ifile = FileService.getDiagramFile(newDiagramName);
				 ActivitiDiagramEditor.get().doSave(ifile, "id-" + modelId);
				 ActivitiDiagramEditor.get().setDirty();				 
				 xmlString = FileService.getFileContent(ifile);				 
			 } else {
				 xmlString = FileService.getFileContent(currentDiagram);
				 ifile = FileService.getDiagramFile(newDiagramName);
				 FileService.writeDiagramToIFile(ifile, xmlString);	
			 }			 
			 xmlString = updateProcessAttributes(xmlString, modelId, newDiagramName); 	     
		 } catch (Exception e) {
			 showMessageBoxError(errorMessge);
			 return false; 
		 }		 
		 
		 try {			 		 
			 if (!xmlString.isEmpty() && RestClient.saveNewModel(newDiagramName, xmlString, modelId) != null) {				 			 
				 return openDiagramForBpmnFile(ifile).isOK();				 
			 }			 	 
		 } catch(Exception e) {			 
		 }		 
		 ErrorDialog.openError(ActivitiPlugin.getShell(), DiagramHandler.errorMessage, "", 
				 new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
						 new PartInitException("Can't save diagram " + newDiagramName)));
		 return false;
	 }	  
	 
	 public static void saveAllDiagrams() {	
		 Set<IFile> diagrams = FileService.getAllDiagramDataFiles();
		 for (IFile dataFile : diagrams) {
			 saveDiagram(dataFile, ActivitiPlugin.getModels(true)); 
		 }
	 }
	 
	 public static boolean saveDiagram(IFile dataFile) {
		 String diagramName = FileService.getDiagramName(dataFile);
		 if (!showYesNoMessageBox("Would you like to save " + diagramName))
			 return false;		 
		 return saveDiagram(dataFile, ActivitiPlugin.getModels(true));
	 }	 
	 
	 public static boolean saveDiagramInCloud(IFile dataFile) {
		 List<Map<String, String>> listModels = ActivitiPlugin.getModels(true);
		 String diagramName = FileService.getDiagramName(dataFile);
		 final Map<String, String> model = getDiagramByName(diagramName, listModels);
		 return saveDiagramInCloud(dataFile, model);
	 }
	 
	 public static boolean saveDiagramInCloud(IFile dataFile, Map<String, String> model) {
		 String diagramName = FileService.getDiagramName(dataFile);	
		 String xmlString;
		 String errorMessge = "Error while saving the model " + diagramName;
		 try {
			 xmlString = FileService.getFileContent(dataFile);
		 } catch (Exception e) {
			 showMessageBoxError(errorMessge);
			 return false; 
		 }		 	  
		 			
		 if (model.isEmpty()) {
			 //new diagram
			 saveDiagramAS(dataFile, diagramName); 
			 return true;
		 }
	 		 
		 String id = getDiagramId(model);
		 if (id.isEmpty()) {
			 showMessageBoxError(errorMessge);
			 return false;
		 } 
			 
		 if (!RestClient.updateModelSource(id, xmlString)) {
			 showMessageBoxError(errorMessge);		 
			 return false;
		 }		 
		 return true;
	 }
	 
	 public static boolean deployModel(IFile dataFile, String name) {
		 List<Map<String, String>> listModels = ActivitiPlugin.getModels(true);
		 String diagramName = FileService.getDiagramName(dataFile);
		 final Map<String, String> model = getDiagramByName(diagramName, listModels);
		 String id = getDiagramId(model);
		 String errorMessge = "Error while deployung the model " + diagramName;
		 if (id.isEmpty()) {
			 showMessageBoxError(errorMessge);
			 return false;
		 } 
		 try {
			 RestClient.deployModel(id, name);
			 return true;
		 } catch (Exception e) {
			 showMessageBoxError(errorMessge);			 
		 }
		 return false;		 
	 }
	 
	 public static String getDiagramIdByName(String diagramName) {
		 if (diagramName.isEmpty()) 
			 return "";
			 
		 for(Map<String, String> model : ActivitiPlugin.getModels(true)) {
			 String modelName = getDiagramName(model);
		 	 if (modelName.equals(diagramName)) 
		 		 return getDiagramId(model);			
		 }
		 return "";
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
	 
	 public static void showMessageBoxError(String nessage) {
		 MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), SWT.ICON_ERROR | SWT.OK);
		 messageBox.setText("Warning");
		 messageBox.setMessage(nessage);
		 messageBox.open();	
	 } 
	 
	 public static UserTaskProperties getCustomTasksUserProperties(String categoryId) {
		  return categoryId != null && !categoryId.isEmpty() ? ActivitiPlugin.getTasksUserProperties(false).stream()
				  .filter(userProperty -> categoryId.equals(Long.toString(userProperty.getCategoryId())))
				  .findAny()
				  .orElse(null) : null;
	 }   
	  
	 public static <K, V> Stream<K> keys(Map<K, V> map, V value) {
		  return map
		  .entrySet()
		  .stream()
		  .filter(entry -> value.equals(entry.getValue()))
		  .map(Map.Entry::getKey);
	 }	
	 
	 public static IStatus openDiagramForBpmnFile(IFile dataFile) {
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
	 
	 //////////////////////////////////////
	 
	 private static boolean saveDiagram(IFile dataFile, List<Map<String, String>> listModels) {
		 //saving file first if diagram is open	
		 String diagramName = FileService.getDiagramName(dataFile);
		 final Map<String, String> model = getDiagramByName(diagramName, listModels);
		 		 	
		 ActivitiDiagramEditor editor = ActivitiDiagramEditor.get();		 
		 if (FileService.isDiagramOpen(dataFile) && editor.isDirty() && !ActivitiDiagramEditor.get().doSave(dataFile, getSavedModelId(model))) {
			 //no message box needed
			 return false;
		 } 
			 
		 //saved, now saving on cloud
		 saveDiagramInCloud(dataFile, model);		 
		 return true;	 
     }	 	
	 
	 private static String getSavedModelId(final Map<String, String> model) {
		 return "id-" + getDiagramId(model);
	 }	 
	 
	 private static boolean showYesNoMessageBox(String message) {
		 MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), 
				 SWT.ICON_QUESTION | SWT.NO | SWT.YES );
   	  	 messageBox.setText("Info");
   	  	 messageBox.setMessage(message);
   	  	 int result = messageBox.open();
   	  	 if (result == SWT.YES)
   	  		 return true;   	  	   
   	  	 return false;
	 }	
	 
	 private static String updateProcessAttributes(String xmlString, String modelId, String diagramName) throws Exception {
		 BpmnModel model = null;
		 Reader reader = new StringReader(xmlString);
		 XMLInputFactory xif = XMLInputFactory.newInstance();
		 XMLStreamReader xtr = xif.createXMLStreamReader(reader);				 
		 BpmnXMLConverter bpmnConverter = new BpmnXMLConverter();			     
		 model = bpmnConverter.convertToBpmnModel(xtr);	
		 List<Process> processes =  model.getProcesses();
		 if (!processes.isEmpty()) {
			 processes.get(0).setName(diagramName);
		     if (!modelId.isEmpty())
		    	 processes.get(0).setId("id-" + modelId);
		 }			     
		 byte[] xmlBytes = bpmnConverter.convertToXML(model);
		 return new String(xmlBytes, "UTF-8");		
	 } 
	 
	 
}
