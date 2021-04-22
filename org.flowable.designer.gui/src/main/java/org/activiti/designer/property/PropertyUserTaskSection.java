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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.UserTask;
import org.activiti.designer.integration.usertask.CustomUserTask;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.activiti.designer.eclipse.util.DiagramHandler;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class PropertyUserTaskSection extends ActivitiPropertySection implements ITabbedPropertyConstants {

	protected Text dueDateText;
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
	private String lastUserId = "";
	private String lastGroupId = "";
	private String lastCategoryId = "";
	
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
		createLabel("Task Duration (in days)", dueDateText);
		//priorityText = createTextControl(false);
		//createLabel("Priority", priorityText);
		//task.getPriority();
		//task.setPriority(taskDurationText.getText());
		
		categoryCombo = createComboboxMy(categoryValues, 0, false);
		createLabel("Category", categoryCombo);
		//skipExpressionText = createTextControl(false);
		//createLabel("Skip expression", skipExpressionText);
		
		selectAssignee = createTextControl(false);
	    selectAssignee.setVisible(false); //should be hidden
	}

	@Override
	protected Object getModelValueForControl(Control control, Object businessObject) {
		UserTask task = (UserTask) businessObject;		
		
		if (control == userCombo) {			
			List <String> user = task.getCandidateUsers();
			if (user == null || user.isEmpty())
				return "";
			String taskKey = user.get(0);
			String userValue = ActivitiPlugin.getUsers(false).get(taskKey);
			if (userValue == null || userValue.isEmpty())
				return "";			
			if (lastUserId.isEmpty() || !lastUserId.equals(taskKey))
				userCombo.setText(userValue);
			lastUserId = taskKey;
			return userValue;
		} else if (control == groupCombo) {
			List <String> group = task.getCandidateGroups();
			if (group == null || group.isEmpty())
				return "";
			String taskKey = group.get(0);
			String roleAssigneeValue = ActivitiPlugin.getGroups(false).get(taskKey);
			if (roleAssigneeValue == null || roleAssigneeValue.isEmpty())
				return "";			
			if (lastGroupId.isEmpty() || !lastGroupId.equals(taskKey))
				groupCombo.setText(roleAssigneeValue);
			lastGroupId = taskKey;
			return roleAssigneeValue;			
		} else if (control == dueDateText) {			
			String dueDate = task.getDueDate();
			if (dueDate == null || dueDate.isEmpty())
				return "";
			dueDateText.setText(dueDate);
			return dueDate;
		} else if (control == categoryCombo) {
			control.setEnabled(!task.isExtended());
			String taskKey = task.getCategory();
			if (taskKey == null || taskKey.isEmpty()) 
				return "";
			String categoryValue = ActivitiPlugin.getTaskCategories(false).get(taskKey);
			if (categoryValue == null || categoryValue.isEmpty())
				return "";
			if (lastCategoryId.isEmpty() || !lastCategoryId.equals(taskKey))
				categoryCombo.setText(categoryValue);
			lastCategoryId = taskKey;
			return categoryValue;					
		//} else if (control == skipExpressionText) {
		//	return task.getSkipExpression();
		} else if (control == formTypeCombo) {
			String taskKey = task.getFormKey();	
			if (taskKey == null || taskKey.isEmpty()) {
				formTypeCombo.setText(ActivitiPlugin.NEW_FORM);
				return "";
			}
			String formValue = loadedForms.get(taskKey);
			if (formValue == null || formValue.isEmpty())
				return "";
			if (lastFormId.isEmpty() || !lastFormId.equals(taskKey))
				formTypeCombo.setText(formValue);
			lastFormId = taskKey;
			return formValue; 
		} else if (control == selectAssignee) {
			String assigneeSelection = task.getAssignee();
			if (assigneeSelection == null || assigneeSelection.isEmpty()) 
				assigneeSelection = "false";
			changeSelection(assigneeSelection);
			return assigneeSelection;
		}
		return null;
	}

	@Override
	protected void storeValueInModel(Control control, Object businessObject) {
		UserTask task = (UserTask) businessObject;
		task.setAssignee(selectAssignee.getText());
		if (control == userCombo) {
			task.getCandidateUsers().clear();
			String userValue = userCombo.getText();
			if (userValue == null || userValue.isEmpty())
				return;
			String userId = DiagramHandler.keys(ActivitiPlugin.getUsers(false), userValue).findFirst().get();
			if (userId != null && !userId.isEmpty())
				task.getCandidateUsers().add(userId);			
		} else if (control == groupCombo) {				
			task.getCandidateGroups().clear();
			String groupValue = groupCombo.getText();
			if (groupValue == null || groupValue.isEmpty())
				return;
			String groupId = DiagramHandler.keys(ActivitiPlugin.getGroups(false), groupValue).findFirst().get();
			if (groupId != null && !groupId.isEmpty())
				task.getCandidateGroups().add(groupId);
		} else if (control == dueDateText) {
			String dueDate = dueDateText.getText();	
			if (dueDate == null || dueDate.isEmpty())
				return;
			try{
				Integer.parseInt(dueDate);
				task.setDueDate(dueDate);
			} catch(NumberFormatException e){
				MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ICON_WARNING | SWT.OK);
				messageBox.setText("Warning");
				messageBox.setMessage("Incorrect value " + e.getLocalizedMessage());
				messageBox.open();
				dueDateText.setText("");
			}			
		} else if (control == categoryCombo) {
			String categoryValue = categoryCombo.getText();
			if (categoryValue == null || categoryValue.isEmpty())
				return;
			String categoryId = DiagramHandler.keys(ActivitiPlugin.getTaskCategories(false), categoryValue).findFirst().get();
			if (categoryId != null && !categoryId.isEmpty())
				task.setCategory(categoryId); 
		//} else if (control == skipExpressionText) {
		//	task.setSkipExpression(skipExpressionText.getText());
		} else if (control == formTypeCombo) {	
			String formName = formTypeCombo.getText();
			if (formName == null || formName.isEmpty())
				return;
			String formId = DiagramHandler.keys(loadedForms, formName).findFirst().get();
			if (formId != null && !formId.isEmpty())
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
	
	private void changeSelection(String selectionValue) {
		boolean selection = selectionValue.equals("true");
		userCombo.setEnabled(selection);
		btnGroup.setSelection(!selection);
		btnUser.setSelection(selection);
		groupCombo.setEnabled(!selection);
		selectAssignee.setText(selectionValue);
	}
}
