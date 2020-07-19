package org.activiti.designer.handlers;

import java.io.File;
import java.io.InputStream;

import javax.print.DocFlavor.URL;

import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.ActivitiConstants;
import org.activiti.designer.util.DiagramHandler;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;


public class NewProcessHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*
		String newProcessName = DiagramHandler.createNewProcessFolder();
		if (newProcessName.isEmpty())
			System.out.println("Can't create directory");
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is  = cl.getResourceAsStream("FTDNew.bpmn");
		return newProcessName;
		*/
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		String result = MyFileDialog.openDialog(window.getShell());
		if (!result.isEmpty()) {
			String diagramFullPath = DiagramHandler.processesFolder + result + ".bpmn";
			File file  = new File(diagramFullPath);
			IPath location= Path.fromOSString(file.getAbsolutePath()); 
			IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
			//DiagramHandler.openDiagramForBpmnFile(ifile);
		}	
		return window;
	}	
	
	public  void openWizard(String id, Shell shell) {
		 // First see if this is a "new wizard".
		 IWizardDescriptor descriptor = PlatformUI.getWorkbench()
		   .getNewWizardRegistry().findWizard(id);
		 // If not check if it is an "import wizard".
		 if  (descriptor == null) {
		   descriptor = PlatformUI.getWorkbench().getImportWizardRegistry()
		   .findWizard(id);
		 }
		 // Or maybe an export wizard
		 if  (descriptor == null) {
		   descriptor = PlatformUI.getWorkbench().getExportWizardRegistry()
		   .findWizard(id);
		 }
		 try  {
		   // Then if we have a wizard, open it.
		   if  (descriptor != null) {
		     IWizard wizard = descriptor.createWizard();
		     WizardDialog wd = new  WizardDialog(shell, wizard);
		     wd.setTitle(wizard.getWindowTitle());
		     wd.open();
		   }
		 } catch  (CoreException e) {
		   e.printStackTrace();
		 }
		}
	
}
