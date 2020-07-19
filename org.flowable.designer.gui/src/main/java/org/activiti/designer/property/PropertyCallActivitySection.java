/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.designer.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.bpmn.model.CallActivity;
import org.activiti.designer.command.BpmnProcessModelUpdater;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.DiagramHandler;
import org.activiti.designer.util.workspace.ActivitiWorkspaceUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Adds the called element of the call activity to the "Main Config" properties sheet. Provides a
 * possibility to lookup a process with the given element ID in the currently open workspace
 * projects as well as a choice of all available projects.
 *
 * @since 5.12
 */
public class PropertyCallActivitySection extends ActivitiPropertySection implements ITabbedPropertyConstants {

  /** The text field of the process ID to call */
  private Combo calledElementCombo;   

  /**
   * A button that becomes active in case a process is found in the current workspace with this
   * the ID
   */
  private Button openCalledElementButton;

  /**
   * A button that allows to choose a called element among all currently found processes.
   */
  private Button chooseCalledElementButton; 
  
  private List<Map<String, String>> loadModels; 
    
  protected Combo createComboboxMy(String[] values, int defaultSelectionIndex) {
		Combo comboControl = new Combo(formComposite, SWT.READ_ONLY);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 200);
		data.right = new FormAttachment(70, 0);
		data.top = createTopFormAttachment();
		comboControl.setLayoutData(data);

		// Set possible values
		if (values != null && values.length > 0) {
			comboControl.setItems(values);

			if (defaultSelectionIndex >= 0) {
				comboControl.select(defaultSelectionIndex);
				// Store the default-selection as "data", so we can reselect it when
				// the combo needs to be reset
				comboControl.setData(defaultSelectionIndex);
			}
		}

		comboControl.addSelectionListener(selectionListener);
		registerControl(comboControl);
		return comboControl;
	}  

  
  @Override
  public void createFormControls(TabbedPropertySheetPage aTabbedPropertySheetPage) {
	//openCalledElementButton = getWidgetFactory().createButton(formComposite, StringUtils.EMPTY, SWT.PUSH);
    //openCalledElementButton.setImage(Activator.getImage(PluginImage.ACTION_GO));
    //FormData data = new FormData();
    //data.right = new FormAttachment(100, -HSPACE);
    //openCalledElementButton.setLayoutData(data);
    

    //chooseCalledElementButton = getWidgetFactory().createButton(formComposite, "\u2026", SWT.PUSH);
    //chooseCalledElementButton.setToolTipText(
    //        "Click to open a dialog to choose from all found processes.");

    //data = new FormData();
    //data.right = new FormAttachment(openCalledElementButton, -HSPACE);
    //chooseCalledElementButton.setLayoutData(data);
    //chooseCalledElementButton.addSelectionListener(chooseCalledElementSelected);
    
	loadModels = DiagramHandler.loadModels();
	String[] tasksArray = DiagramHandler.buildListFromList(loadModels, "name");
	calledElementCombo = createComboboxMy(tasksArray, 0 );
    FormData formData = (FormData) calledElementCombo.getLayoutData();
    formData.right.offset = -80;
    createLabel("Called element", calledElementCombo);
    
    openCalledElementButton = getWidgetFactory().createButton(formComposite, "Show Diagram", SWT.PUSH);
    openCalledElementButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
    FormData data = new FormData();
	data.left = new FormAttachment(calledElementCombo, 0);
	data.right = new FormAttachment(90, 0);
	data.top = new FormAttachment(calledElementCombo, -2, SWT.TOP);
	openCalledElementButton.setLayoutData(data);
	openCalledElementButton.addSelectionListener(openCalledElementSelected);	
	
  }

  @Override
  protected Object getModelValueForControl(Control control, Object businessObject) {
    CallActivity activity = (CallActivity) businessObject;
    if (control == calledElementCombo) {
      return activity.getCalledElement();
    }
    return null;
  }

  @Override
  protected void storeValueInModel(Control control, Object businessObject) {
    CallActivity activity = (CallActivity) businessObject;
    
    if (control == calledElementCombo) {
      //evaluateOpenCalledElementButtonEnabledStatus();
      activity.setCalledElement(calledElementCombo.getText());
    }
  }

  /**
   * Checks, whether the given process ID refers to a process this call activity might lead to.
   *
   * @param calledElement the process ID to check
   * @return <code>true</code> in case such a process ID exists, <code>false</code> otherwise.
   */
  private boolean isCalledElementExisting(final String calledElement) {
    final Set<IFile> resources = ActivitiWorkspaceUtil.getDiagramDataFilesByProcessId(calledElement);

    return !resources.isEmpty();
  }

  /**
   * Evaluates the current state of the button to open the corresponding process diagram of the
   * given process ID. In case the called element is empty or a not found, the button will be
   * disabled, otherwise enabled.
   */
  private void evaluateOpenCalledElementButtonEnabledStatus() {
    final String calledElement = calledElementCombo.getText();

    if (StringUtils.isBlank(calledElement) || !isCalledElementExisting(calledElement)) {
      openCalledElementButton.setEnabled(false);
      openCalledElementButton.setToolTipText(null);
    } else {
      openCalledElementButton.setEnabled(true);
      openCalledElementButton.setToolTipText("Click to open the called element's process diagram");
    }
  }

  /**
   * This listener is called in case the user presses the button to choose a process from one of
   * the existing processes in any open project.
   */
  private SelectionListener chooseCalledElementSelected = new SelectionListener() {

    @Override
    public void widgetSelected(SelectionEvent event) {
      final Map<IFile, Set<String>> processIdsByDataFiles
        = ActivitiWorkspaceUtil.getAllProcessIdsByDiagramDataFile();

      // we now need to make a list out of this, as the TwoPaneElementSelector wants it this way
      final List<Object[]> selectorInput = new ArrayList<Object[]>();

      for (final Entry<IFile, Set<String>> entry : processIdsByDataFiles.entrySet()) {
        for (final String processId : entry.getValue()) {
          entry.getKey().getFullPath();


          selectorInput.add(new Object[] { entry.getKey(), processId });
        }
      }

      final TwoPaneElementSelector dialog
        = new TwoPaneElementSelector(Display.getCurrent().getActiveShell()
                                   , new DiagramLabelProvider()
                                   , new ProcessIdLabelProvider());

      dialog.setTitle("Choose Called Element for Call Activity");
      dialog.setMessage("Choose a diagram and a process");
      dialog.setBlockOnOpen(true);
      dialog.setElements(selectorInput.toArray());
      dialog.setUpperListLabel("Diagrams");
      dialog.setLowerListLabel("Processes");

      if (dialog.open() == Window.OK) {
        final Object[] data = (Object[]) dialog.getFirstResult();

        calledElementCombo.setText((String) data[1]);
        
        //Fix "called element" value not save when select from pop up.
        BpmnProcessModelUpdater updater = getProcessModelUpdater();
        Object updatableBo = updater.getUpdatableBusinessObject();
        storeValueInModel(calledElementCombo, updatableBo);
        executeModelUpdater();
      }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
      // intentionally left blank
    }
  };

  /**
   * This listener is called in case the user clicks the button to open the selected process
   * model.
   */
  private SelectionListener openCalledElementSelected = new SelectionListener() {

    @Override
    public void widgetSelected(SelectionEvent event) {
    	String modelName = calledElementCombo.getText();
    	
    	for(Map<String, String> model : loadModels) {
			if (model.get("name").equals(modelName)) {
				DiagramHandler.openDiagram(model, Display.getCurrent().getActiveShell());
				return;
			}
		}										
		ErrorDialog.openError(Display.getCurrent().getActiveShell(), DiagramHandler.errorMessage, modelName, 
				new Status(IStatus.ERROR, ActivitiPlugin.getID(), "Error while opening new editor.", new PartInitException("Can't find diagram")));
	
   	
      /*
       * final String calledElement = calledElementCombo.getText();

      final Set<IFile> resources = ActivitiWorkspaceUtil.getDiagramDataFilesByProcessId(calledElement);

      if (resources.size() == 1) {
        // open diagram
        openDiagramForBpmnFile(resources.iterator().next());
        
      } else if (resources.size() > 1) {
        final ActivitiResourceSelectionDialog dialog = new ActivitiResourceSelectionDialog(
                Display.getCurrent().getActiveShell(), resources.toArray(new IResource[] {}));

        dialog.setTitle("Multiple Processes Found");
        dialog.setMessage("Select a Process Model to use (* = any string, ? = any char):");
        dialog.setBlockOnOpen(true);
        dialog.setInitialPattern("*");

        if (dialog.open() == Window.OK) {
          final Object[] result = dialog.getResult();

          openDiagramForBpmnFile((IFile) result[0]);
        }
      }
      */
    }    

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
      widgetSelected(event);
    }
  };
  
  /**
   * A private process ID label provider that simply returns the second element of the pair built
   * from process ID and data file. This is used in the selection list for an available process.
   */
  private static class ProcessIdLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {

      return (String) ((Object[]) element)[1];
    }

  }

  /**
   * This label provider is used in the selection list for a diagram in the lower list. It utilizes
   * a workbench label provider to retrieve the appropriate image for the diagram.
   */
  private static class DiagramLabelProvider extends LabelProvider {

    private WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();

    @Override
    public Image getImage(Object element) {
      final IResource resource = (IResource) ((Object[]) element)[0];

      return labelProvider.getImage(resource);
    }

    @Override
    public String getText(Object element) {
      final IResource resource = (IResource) ((Object[]) element)[0];

      return resource.getFullPath().makeRelative().toString();
    }

  }   
  
}
