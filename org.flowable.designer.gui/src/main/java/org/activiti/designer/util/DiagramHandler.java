package org.activiti.designer.util;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.activiti.designer.eclipse.util.FileService;

public class DiagramHandler {
	public static final String processesFolder = System.getProperty("user.home") + 
			"/Desktop/FtdSolution/runtime-EclipseApplication/FTDSolutionDesigner/target/";
	public static final String newProcessName = "NewProcess";

	public static final String fullDiagramPath = System.getProperty("user.home") + "/Desktop/FtdSolution/runtime-EclipseApplication/FTDSolutionDesigner/target/";
	public static final String errorMessage = "Error Opening Activiti Diagram";
	public static final String errorSaveMessage = "Error Saving Activiti Diagram";
	
	private IFile diagramFile;
	
	public static String createNewProcessFolder() {
		int i = 0;
		
		File theDir = new File(processesFolder);
		if (!theDir.exists()) {
			try {
		        theDir.mkdir();		         
		    } 
		    catch(Exception e){
		    	System.out.println(e.getMessage()); 
		    	return "";
		    }       
		}
			
		
		while(true) {
			i++;
			
			String processName = newProcessName + Integer.toString(i);
			String dirName = processesFolder + "/" + processName;
			theDir = new File(dirName);
			// if the directory does not exist, create it
			if (theDir.exists())  
				continue;
		    
			try {
		        theDir.mkdir();
		        return processName; 
		    } 
		    catch(Exception e){
		    	System.out.println(e.getMessage()); 
		    }        
			return "";
		}		
	}
	
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
				
		if(!isDiagramExist(modelName)) 
			reloadModelFromCloud = true;
		else {
			String fullFileName = fullDiagramPath +  modelName +  ".bpmn";
			java.nio.file.Path path = Paths.get(fullFileName);
			try {
				FileTime fileTime = Files.getLastModifiedTime(path);
				if (fileTime.toMillis() < updateTime)
					reloadModelFromCloud = true;
			} catch (IOException e) {
				reloadModelFromCloud = true;
			}
		}
			
		if (reloadModelFromCloud) {
			String diagram = RestClient.getModelSource(modelId);				
			if (diagram.isEmpty() || !DiagramHandler.writeDiagramToFile(modelName, diagram)) {						
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
	
	 public static List<Map<String, String>> loadModels() { 
		return RestClient.getModels();
	 }
	 
	 public static Map<String, String> loadForms() { 
		 Map<String, String> loadedForms = new HashMap<String, String>();
		 loadedForms.put("2a3d7d0e3e4aca9a24893aaaf2e449e837de1f5c", "Propose PO");
		 loadedForms.put("d547a52eeae2e8145fe314d2410aaf867146502c", "Approve PO");
		 loadedForms.put("650e3c8dc56fcc9bcc35070ee744789b8073d245", "Escalate PO");
		 loadedForms.put("683a021ac763b3f0cec9ecc2991e5e72e4132581", "Review Proposal");
			
			
		 loadedForms.put("f0b4e925b681ef2dcedb843a62c12293b2ee1f21", "Gather New Project Information");
		 loadedForms.put("8090badbafc70ffb64ec241b74ebf540a1b2e001", "Assign Proposal Team");
		 loadedForms.put("d96f949174e29627f1b1a4cbab90e1e27d2ae931", "Contact External Expert for Proposal Input and Availability");
		 loadedForms.put("3b40393d4dc8fd2f76080165566a5ffcf5bb4beb", "Project Startup Data");
			
		 loadedForms.put("f328f5aa5feaa1dec8401a47b1badd99afcf70b5", "Submit Draft Proposal to Client");
		 loadedForms.put("efcb7c34073379e84986006957d3d7203b4efa29", "Submit Proposal");
		 loadedForms.put("ed2e8dfd324d9a11e5cf28aa7ef8f82950c32397", "Submit Proposal to PST");
		 loadedForms.put("173d32236635b57bd09896e6bd7e51d548b77bf2", "PST Proposal Approval");
		 return loadedForms;
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
	 
	 public static IFile getCurrentDiagramFile() {
		 return ActivitiDiagramEditor.get().getCurrentDiagramFile();
	  }
	 
	 public static void saveDiagram() {
		 //final Set<IFile> result = new HashSet<IFile>();
		 //final Set<IFile> projectResources = ActivitiWorkspaceUtil.getAllDiagramDataFiles();
		 ActivitiDiagramEditor.get().doSave();
     }		 
	 
	 private static IStatus openDiagramForBpmnFile(String diagramName) {
			String fullFileName = fullDiagramPath +  diagramName +  ".bpmn";
			File file  = new File(fullFileName);
			IPath location= Path.fromOSString(file.getAbsolutePath()); 
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
			return openDiagramForBpmnFile(ifile);
		}
		
		private static boolean isDiagramExist(String diagramName) {
			String fullFileName = fullDiagramPath +  diagramName +  ".bpmn";					
			File file  = new File(fullFileName);
			return file.exists() && !file.isDirectory();  
		}
		
		private static boolean writeDiagramToFile(String diagramName, String xmlString) {
			String fullFileName = fullDiagramPath +  diagramName +  ".bpmn";
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
}
