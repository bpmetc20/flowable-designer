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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.UserTask;
import org.activiti.designer.integration.usertask.CustomUserTask;
import org.activiti.designer.util.DiagramHandler;
import org.activiti.designer.util.RestClient;
import org.activiti.designer.util.eclipse.ActivitiUiUtil;
import org.activiti.designer.util.extension.ExtensionUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class PropertyUserTaskSection extends ActivitiPropertySection implements ITabbedPropertyConstants {

	protected Text dueDateText;
	protected Text priorityText;
	protected Text categoryText;
	protected Text skipExpressionText;
	protected Text reccomendedFormText;

	private Combo formTypeCombo;
	protected Button formSelectButton;
	
	protected Button btnUser;
	protected Button btnGroup;
	private Combo userCombo;
	private Combo groupCombo;
	boolean userSelection = false;

	
	private Map<String, String> loadedUsers = new HashMap<String, String>();
	private Map<String, String> loadedGroups = new HashMap<String, String>();
	private Map<String, String> taskForms = new HashMap<String, String>();

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
				
		
		Map<String, String> loadedForms = DiagramHandler.loadForms();
		
		String[] usersValues = DiagramHandler.buildListFromMap(DiagramHandler.loadUsers());
		String[] groupsValues = DiagramHandler.buildListFromMap(DiagramHandler.loadGroups());
		String[] formsValues = DiagramHandler.buildListFromMap(loadedForms);
		userCombo = createComboboxMy(usersValues, 0);
		createLabel("Assignee", userCombo);
	    
		btnUser = getWidgetFactory().createButton(formComposite, "", SWT.RADIO);
		FormData data = new FormData();
	    data.left = new FormAttachment(userCombo, 0);
	    data.right = new FormAttachment(100, 0);
	    data.top = new FormAttachment(userCombo, +2, SWT.TOP);
	    btnUser.setLayoutData(data);	    
	    
	    groupCombo = createComboboxMy(groupsValues, 0);
	    createLabel("", groupCombo);
	    
		btnGroup = getWidgetFactory().createButton(formComposite, "", SWT.RADIO);
	    data = new FormData();
	    data.left = new FormAttachment(userCombo, 0);
	    data.right = new FormAttachment(100, 0);
	    data.top = new FormAttachment(groupCombo, +2, SWT.TOP);
	    btnGroup.setLayoutData(data); 	
	    
	    reccomendedFormText = createTextControl(false);
	    createLabel("Reccomended form", reccomendedFormText);
	    reccomendedFormText.setEnabled(false);
	    reccomendedFormText.setVisible(false);
	    
	    btnUser.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent evt) {
	        	if (btnGroup.getSelection()) {
	        		userSelection = false;
	        		changeSelection();
	        	}
	        	
	        }
	      });
	    
	    btnGroup.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent evt) {
	        	if (btnUser.getSelection()) {
	        		userSelection = true;
	        		changeSelection();
	        	}
	        }
	      });	
	    
	    changeSelection();
		
		
		formTypeCombo = createComboboxMy(formsValues, 0);
		createLabel("Form", formTypeCombo);
		
		formTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (formTypeCombo.getText().equalsIgnoreCase(reccomendedFormText.getText())) 
					reccomendedFormText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				else
					reccomendedFormText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				
			}
		});
		
		formSelectButton = getWidgetFactory().createButton(formComposite, "Launch form", SWT.PUSH);
		formSelectButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		data = new FormData();
		data.left = new FormAttachment(formTypeCombo, 0);
		data.right = new FormAttachment(90, 0);
		data.top = new FormAttachment(formTypeCombo, -2, SWT.TOP);
		formSelectButton.setLayoutData(data);
		formSelectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				String formId = (String) loadedForms.entrySet().stream()
						.filter(e -> e.getValue().equals(formTypeCombo.getText())).map(Map.Entry::getKey).findFirst()
						.orElse(null);
				String url = formId != null? "https://165.227.16.142.nip.io:8443/orbeon/fr/orbeon/builder/edit/" + formId :
						"https://165.227.16.142.nip.io:8443/orbeon/fr/orbeon/builder/new";if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(new URI(url));
					} catch (IOException | URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		dueDateText = createTextControl(false);
		createLabel("Due date (variable)", dueDateText);
		priorityText = createTextControl(false);
		createLabel("Priority", priorityText);
		categoryText = createTextControl(false);
		createLabel("Category", categoryText);
		skipExpressionText = createTextControl(false);
		createLabel("Skip expression", skipExpressionText);		
		
	}
	
	private void changeSelection() {
		userCombo.setEnabled(userSelection);
		btnGroup.setSelection(!userSelection);
		btnUser.setSelection(userSelection);
		groupCombo.setEnabled(!userSelection);		
	}


	@Override
	protected Object getModelValueForControl(Control control, Object businessObject) {
		UserTask task = (UserTask) businessObject;		
		
		if (control == userCombo) {
			userSelection = Boolean.valueOf(task.getAssignee());
			changeSelection();
			return getCommaSeperatedString(task.getCandidateUsers());
		} else if (control == groupCombo) {
			return getCommaSeperatedString(task.getCandidateGroups());
		} else if (control == dueDateText) {
			return task.getDueDate();
		} else if (control == priorityText) {
			return task.getPriority();
		} else if (control == categoryText) {
			return task.getCategory();
		} else if (control == skipExpressionText) {
			return task.getSkipExpression();
		} else if (control == formTypeCombo) {
			selectTaskForm(task);
			return task.getFormKey(); // TODO provide value (form name) instead of key (form id)
		}
		return null;
	}

	@Override
	protected void storeValueInModel(Control control, Object businessObject) {
		UserTask task = (UserTask) businessObject;
		if (control == userCombo) {
			task.setAssignee(Boolean.toString(true));
			task.getCandidateUsers().add(userCombo.getText());			
		} else if (control == groupCombo) {	
			task.setAssignee(Boolean.toString(false));
			task.getCandidateGroups().add(groupCombo.getText());
		} else if (control == dueDateText) {
			task.setDueDate(dueDateText.getText());
		} else if (control == priorityText) {
			task.setPriority(priorityText.getText());
		} else if (control == categoryText) {
			task.setCategory(categoryText.getText());
		} else if (control == skipExpressionText) {
			task.setSkipExpression(skipExpressionText.getText());
		} else if (control == formTypeCombo) {
			task.setFormKey(formTypeCombo.getText()); // TODO provide key (form id) instead of value (form name)
		}
	}		

	
	

	private void retrieveGroups() {
		loadedGroups = RestClient.getGroups();
	}

	private String[] buildGroupsList() {
		List<String> values = new ArrayList<String>(loadedGroups.values());
		Collections.sort(values);
		return values.toArray(new String[0]);
	}
	
	private void selectTaskForm(UserTask task) {
		CustomUserTask customerTask = findCustomUserTask(task);
		reccomendedFormText.setVisible(true);
		if (customerTask == null) {
			reccomendedFormText.setText("New Form");
		} else {
			String taskName = customerTask.getName();
			String selectedForm = taskForms.getOrDefault(taskName, "");
			reccomendedFormText.setText(selectedForm);			
		}
		if (formTypeCombo.getText().equalsIgnoreCase(reccomendedFormText.getText())) 
			reccomendedFormText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
		else
			reccomendedFormText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
	}	
	
	private CustomUserTask findCustomUserTask(UserTask userTask) {
	    CustomUserTask result = null;

	    if (userTask.isExtended()) {

	      final List<CustomUserTask> customUserTasks = ExtensionUtil.getCustomUserTasks(ActivitiUiUtil.getProjectFromDiagram(getDiagram()));

	      for (final CustomUserTask customUserTask : customUserTasks) {
	        if (userTask.getExtensionId().equals(customUserTask.getId())) {
	          result = customUserTask;
	          break;
	        }
	      }
	    }
	    return result;
	  }


}
