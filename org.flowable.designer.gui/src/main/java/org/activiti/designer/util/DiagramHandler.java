package org.activiti.designer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditorInput;
import org.activiti.designer.util.workspace.ActivitiWorkspaceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.activiti.designer.eclipse.util.FileService;

public class DiagramHandler {
	public static final String newModelName = "NewDiagram";
	public static final String fullDiagramPath = System.getProperty("user.home") + 
				"/Desktop/FTDDevelopment/runtime-EclipseApplication/FlowableProject/";
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
		String fullFileName = fullDiagramPath +  modelName +  ".bpmn";
		
		if(!isDiagramExist(fullFileName)) 
			reloadModelFromCloud = true;
		else {
			java.nio.file.Path path = Paths.get(fullFileName);
			try {
				FileTime fileTime = Files.getLastModifiedTime(path);
				if (fileTime.toMillis() < updateTime)
					reloadModelFromCloud = true;
			} catch (IOException e) {
				reloadModelFromCloud = true;
			}
		}
			
		if (true /*reloadModelFromCloud*/) {
			String diagram = RestClient.getModelSource(modelId);				
			if (diagram.isEmpty() || !DiagramHandler.writeDiagramToFile(fullFileName, diagram)) {						
				ErrorDialog.openError(shell, DiagramHandler.errorMessage, modelName, 
						new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", 
								new PartInitException("Can't write diagram")));
				return;	
			}
		}
		IStatus status = openDiagramForBpmnFile(modelName);	
		if (!status.isOK()) {
			ErrorDialog.openError(shell, "Error Opening Activiti Diagram", modelName, status);
		}		
	 }
	
	 public static void createNewDiagram(Shell shell) {
		 String fullFileName = fullDiagramPath +  newModelName +  ".bpmn";
		 
		 try {
			 File file = new File(fullFileName);
			 file.createNewFile();
			 IPath location= Path.fromOSString(file.getAbsolutePath()); 
			 IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);			 
			 ActivitiDiagramEditor.get().createNewDiagram(ifile);
		 } catch(Exception e) {
			 ErrorDialog.openError(shell, DiagramHandler.errorMessage, newModelName, 
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
	 
	 public static String getCurrentDiagramName() {
		 return ActivitiDiagramEditor.get().getCurrentDiagramName();
	 }
	 
	 public static boolean saveDiagram(String diagramName) {
		 //final Set<IFile> result = new HashSet<IFile>();
		 //final Set<IFile> projectResources = ActivitiWorkspaceUtil.getAllDiagramDataFiles();
		 //find diagram id first
		 
		 final Map<String, String> model = getDiagramByName(diagramName);			
		 boolean existInCloud = model.isEmpty() ? false : true;    
			 	
		 //saving file first
		 if (!ActivitiDiagramEditor.get().doSave()) {
			 //no message box needed
			 return false;
		 } 
			 
		 //saved, now saving on cloud
		 StringBuilder contentBuilder = new StringBuilder();					 
		 try (Stream<String> stream = Files.lines( Paths.get(
				 ActivitiDiagramEditor.get().getCurrentDiagramFullPath()), 
			     StandardCharsets.UTF_8)) {
		         stream.forEach(s -> contentBuilder.append(s).append("\n")); 					 
		 } catch (IOException e) {
			 e.printStackTrace();
		 	 showSaveMessageBoxError(diagramName);			 
			 return false;
		 }	
		 
		 if (existInCloud) {
			 String id = getDiagramId(model);
			 if (id.isEmpty()) {
				 showSaveMessageBoxError(diagramName);
				 return false;
			 } 
			 if (!RestClient.updateModelSource(id, contentBuilder.toString())) {
				 showSaveMessageBoxError(diagramName);			 
				 return false;
			 }
		 } else {
			 if (RestClient.saveNewModel(diagramName, contentBuilder.toString()) == null) {
				 showSaveMessageBoxError(diagramName);			 
				 return false;
			 }
		 }
		 return true;	 
     }
	 
	 public static Map<String, String> getDiagramByName(String diagramName) {
		 if (!diagramName.isEmpty()) {
			 List<Map<String, String>> loadedModels = loadModels();
		 
		 	for(Map<String, String> model : loadedModels) {
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
	 
	 private static IStatus openDiagramForBpmnFile(String diagramName) {
		 String fullFileName = fullDiagramPath +  diagramName +  ".bpmn";
		 File file  = new File(fullFileName);
		 IPath location= Path.fromOSString(file.getAbsolutePath()); 
		 IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
		 return openDiagramForBpmnFile(ifile);
	 }
		
		private static boolean isDiagramExist(String fullFileName) {
			File file  = new File(fullFileName);
			return file.exists() && !file.isDirectory();  
		}
		
		private static boolean writeDiagramToFile(String fullFileName, String xmlString) {
			try {
				Files.write(Paths.get(fullFileName), xmlString.getBytes(), StandardOpenOption.CREATE);
				return true;
			} catch (IOException e) {
				System.out.print(e.getMessage());
			}
			return false;
		}
			
		/**
		 * Opens the given diagram specified by the given data file in a new editor. In case an error
		 * occurs while doing so, opens an error dialog.
		 *
		 * @param dataFile the data file to use for the new editor to open
		 *
		 * TODO: this is a copy from PropertyCallActivitySection. Figure out how to make sure we do not double this
		*/  
		private static IStatus openDiagramForBpmnFile(IFile dataFile) {
			IStatus status = new Status(IStatus.OK, ActivitiPlugin.getID(), errorMessage, null); 
			if (dataFile.exists()) {
		        final IWorkbenchPage activePage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		        try {
		          IDE.openEditor(activePage, dataFile, ActivitiConstants.DIAGRAM_EDITOR_ID, true);
		          return status;
		        } catch (PartInitException exception) {
		        	status =  new Status(IStatus.ERROR, ActivitiPlugin.getID(), errorMessage, exception);      
		        	return status;
		        }
			}
			return new Status(IStatus.INFO, ActivitiPlugin.getID(), errorMessage, new PartInitException("Can't find diagram")); 
		}
		
		private static void showSaveMessageBoxError(String diagramName) {
			MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
		    messageBox.setText("Warning");
		    messageBox.setMessage("Error while saving the model " + diagramName);
		    messageBox.open();	
		}
}
