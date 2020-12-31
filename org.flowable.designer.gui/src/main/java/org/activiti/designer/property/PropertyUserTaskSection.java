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
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.util.DiagramHandler;
import org.activiti.designer.eclipse.util.RestClient;
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
	protected Text taskDurationText;
	//protected Text priorityText;
	protected Combo categoryCombo;
	//protected Text skipExpressionText;
	protected Text selectAssignee; // store radio button selection

	private Combo formTypeCombo;
	protected Button formSelectButton;
	
	protected Button btnUser;
	protected Button btnGroup;
	private Combo userCombo;
	private Combo groupCombo;
	private String lastFormId = "";	
	private Map<String, String> loadedForms = new HashMap<String, String>();
	
	protected Combo createComboboxMy(String[] values, int defaultSelectionIndex, boolean change) {
		Combo comboControl = new Combo(formComposite, change ? SWT.DROP_DOWN : SWT.READ_ONLY);
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
		loadedForms = ActivitiPlugin.getForms(true);		
		String[] usersValues = DiagramHandler.buildListFromMap(ActivitiPlugin.getUsers(false));
		String[] groupsValues = DiagramHandler.buildListFromMap(ActivitiPlugin.getGroups(false));
		String[] formsValues = DiagramHandler.buildListFromMap(loadedForms);
		String[] categoryValues = DiagramHandler.buildListFromMap(ActivitiPlugin.getTaskCategories(false));
		userCombo = createComboboxMy(usersValues, 0, false);
		createLabel("Assignee", userCombo);
	    
		btnUser = getWidgetFactory().createButton(formComposite, "", SWT.RADIO);
		FormData data = new FormData();
	    data.left = new FormAttachment(userCombo, 0);
	    data.right = new FormAttachment(100, 0);
	    data.top = new FormAttachment(userCombo, +2, SWT.TOP);
	    btnUser.setLayoutData(data);	    
	    
	    groupCombo = createComboboxMy(groupsValues, 0, false);
	    createLabel("", groupCombo);
	    
		btnGroup = getWidgetFactory().createButton(formComposite, "", SWT.RADIO);
	    data = new FormData();
	    data.left = new FormAttachment(userCombo, 0);
	    data.right = new FormAttachment(100, 0);
	    data.top = new FormAttachment(groupCombo, +2, SWT.TOP);
	    btnGroup.setLayoutData(data); 		    
	    
	    btnUser.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent evt) {
	        	if (btnGroup.getSelection()) 
	        		changeSelection("false");
	        }
	      });
	    
	    btnGroup.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent evt) {
	        	if (btnUser.getSelection()) 
	        		changeSelection("true");
	        }
	      });	
	    
	    formTypeCombo = createComboboxMy(formsValues, 0, true);
		createLabel("Form", formTypeCombo);
		
		formTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				//if (formTypeCombo.getText().equalsIgnoreCase(reccomendedFormText.getText())) 
				//	reccomendedFormText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				//else
				//	reccomendedFormText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				
			}
		});
		
		formSelectButton = getWidgetFactory().createButton(formComposite, "Launch form", SWT.PUSH);
		formSelectButton.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
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
						.orElse("");
				String url = !formId.isEmpty() ? "https://165.227.16.142.nip.io:8443/orbeon/fr/orbeon/builder/edit/" + formId :
						"https://165.227.16.142.nip.io:8443/orbeon/fr/orbeon/builder/new";
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
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
		taskDurationText = createTextControl(false);
		createLabel("Task Duration", taskDurationText);
		//priorityText = createTextControl(false);
		//createLabel("Priority", priorityText);
		
		categoryCombo = createComboboxMy(categoryValues, 0, false);
		createLabel("Category", categoryCombo);
		//skipExpressionText = createTextControl(false);
		//createLabel("Skip expression", skipExpressionText);
		
		selectAssignee = createTextControl(false);
	    selectAssignee.setVisible(false); //should be hidden
	}
	
	private void changeSelection(String selectionValue) {
		boolean selection = selectionValue.equals("true");
		userCombo.setEnabled(selection);
		btnGroup.setSelection(!selection);
		btnUser.setSelection(selection);
		groupCombo.setEnabled(!selection);
		selectAssignee.setText(selectionValue); 
	}


	@Override
	protected Object getModelValueForControl(Control control, Object businessObject) {
		UserTask task = (UserTask) businessObject;		
		
		if (control == userCombo) {			
			List <String> user = task.getCandidateUsers();
			if (user == null || user.isEmpty())
				return "";
			String userValue = ActivitiPlugin.getUsers(false).get(user.get(0));
			if (userValue != null && !userValue.isEmpty())
				userCombo.setText(userValue);
			return userValue;
		} else if (control == groupCombo) {
			List <String> group = task.getCandidateGroups();
			if (group == null || group.isEmpty())
				return "";
			String roleAssigneeValue = ActivitiPlugin.getGroups(false).get(group.get(0));
			if (roleAssigneeValue != null && !roleAssigneeValue.isEmpty())
				groupCombo.setText(roleAssigneeValue);
			return roleAssigneeValue;
		} else if (control == dueDateText) {
			return task.getDueDate();
		} else if (control == taskDurationText) {
			return task.getPriority();
		} else if (control == categoryCombo) {
			control.setEnabled(!task.isExtended());
			String taskKey = task.getCategory();
			if (taskKey != null && !taskKey.isEmpty()) {
				//String categoryValue = ActivitiPlugin.getTaskCategories(false).get(taskKey);
				//if (categoryValue != null)
					categoryCombo.setText(taskKey);
			}
			return taskKey;					
		//} else if (control == skipExpressionText) {
		//	return task.getSkipExpression();
		} else if (control == formTypeCombo) {
			String taskKey = task.getFormKey();	
			if (taskKey == null || taskKey.isEmpty())
				formTypeCombo.setText(ActivitiPlugin.NEW_FORM);
			else {			
				if (lastFormId.isEmpty() || !lastFormId.equals(taskKey))
					formTypeCombo.setText(loadedForms.get(taskKey));
				lastFormId = taskKey;
			}			
			return taskKey; 
		} else if (control == selectAssignee) {
			String assigneeSekection = task.getAssignee();
			changeSelection(assigneeSekection);
			return assigneeSekection;
		}
		return null;
	}

	@Override
	protected void storeValueInModel(Control control, Object businessObject) {
		UserTask task = (UserTask) businessObject;
		task.setAssignee(selectAssignee.getText());
		if (control == userCombo) {
			task.getCandidateUsers().clear();
			String userId = DiagramHandler.keys(ActivitiPlugin.getUsers(false), userCombo.getText()).findFirst().get();
			if (userId != null && !userId.isEmpty())
				task.getCandidateUsers().add(userId);			
		} else if (control == groupCombo) {				
			task.getCandidateGroups().clear();
			String groupId = DiagramHandler.keys(ActivitiPlugin.getGroups(false), groupCombo.getText()).findFirst().get();
			if (groupId != null && !groupId.isEmpty())
				task.getCandidateGroups().add(groupId);
		} else if (control == dueDateText) {
			task.setDueDate(dueDateText.getText());
		} else if (control == taskDurationText) {
			task.setPriority(taskDurationText.getText());
		} else if (control == categoryCombo) {
			String categoryValue = categoryCombo.getText();
			//String categoryId = DiagramHandler.keys(ActivitiPlugin.getTaskCategories(false), categoryValue).findFirst().get();
			//if (categoryId != null)
				task.setCategory(categoryValue); 
		//} else if (control == skipExpressionText) {
		//	task.setSkipExpression(skipExpressionText.getText());
		} else if (control == formTypeCombo) {	
			String formName = formTypeCombo.getText();
			String formId = DiagramHandler.keys(loadedForms, formName).findFirst().get();
			task.setFormKey(formId);  
		} 
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
