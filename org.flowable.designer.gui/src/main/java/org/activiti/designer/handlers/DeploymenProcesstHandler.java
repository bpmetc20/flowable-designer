package org.activiti.designer.handlers;

import java.io.File;import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.activiti.designer.eclipse.util.DiagramHandler;
import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.util.ActivitiConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;

public class DeploymenProcesstHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);		
		IFile dataFile = FileService.getActiveDiagramFile();
		if (dataFile == null) {
			DiagramHandler.showMessageBoxError("Please load diagram first!");
		} else {				
		 	MyTitleAreaDialog dialog = new MyTitleAreaDialog(FileService.getDiagramName(dataFile), "Your current Diagram will be Deployed",
		 			"Please type deployment name", true);
			dialog.create();
			if (dialog.open() == Window.OK) {
				DiagramHandler.deployModel(dataFile, dialog.getDiagramName());		    
			}
		}
		return window;
	}
}
