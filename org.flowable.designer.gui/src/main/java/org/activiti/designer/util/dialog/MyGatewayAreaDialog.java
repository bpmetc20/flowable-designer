package org.activiti.designer.util.dialog;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.activiti.designer.PluginImage;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.features.CreateCustomGatewayFeature.GatewayType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class MyGatewayAreaDialog extends TitleAreaDialog {

    private Combo conditionText;
    private String connectionLabel = ""; 
    private GatewayType gatewayType;
    private String gatewayLabel = ""; 
    private String title = "Please add condition to your %s %s connection";
    private String selectedValue = "";
    private Map<String, String> projectParams;
    String conditionExpression;
    private Text valueText; // store radio button selection
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

                
    public MyGatewayAreaDialog(String connectionLabel, String getewayLabel, GatewayType gatewayType,
    		String conditionExpression, Map<String, String> projectParams) {
    	super(ActivitiPlugin.getShell());
        
    	this.connectionLabel = connectionLabel;
    	this.conditionExpression = conditionExpression;
    	this.gatewayLabel = getewayLabel;
    	this.gatewayType = gatewayType;
    	this.projectParams = projectParams;
    }

    @Override
    public void create() {
        super.create();
        
        setTitle(String.format(title, gatewayLabel, connectionLabel));
        setMessage("", IMessageProvider.INFORMATION);                       
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);        
               
        Label connectionName = new Label(container, SWT.NONE);
        connectionName.setText("Condition");

        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;

        
        conditionText = new Combo(container, SWT.READ_ONLY | SWT.BORDER);
        conditionText.setLayoutData(gridData);
        Set<String> keys = projectParams.keySet();
        conditionText.setItems(keys.toArray(new String[keys.size()]));
        conditionText.select(0);
        
        conditionText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				valueText.setText(projectParams.get(conditionText.getText()));		        
			}
		});
        
        Label valueLabel = new Label(container, SWT.NONE);
        switch(gatewayType) {
			case Contains:			  		
			case DoesNotContain:		
				valueLabel.setText("Values separated by , ");				
				break;
			case Range:
				valueLabel.setText("Values range separated by , ");
				break;
			default: 
				valueLabel.setText("Value");
				break;		
        }
        valueText = new Text(container, SWT.BORDER);     
        return area;
    }    
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {    	
    	createButton(parent, IDialogConstants.OK_ID, "OK", true);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }    

    @Override
    protected void okPressed() {
    	String paramValue = valueText.getText();
    	if (paramValue.isEmpty()) {
    		showMessage("Value field should be Empty!");
    		return;
    	}    		
    	switch(gatewayType) {
  			case Greater:			  		
  			case GreaterThanOrEqualTo:			  		
  			case LessThan:			  		
  			case LessThanOrEqualTo:
  				if (!isNumeric(paramValue)) {
  					showMessage("Value Should be Numeric!");
  					return; 
  				}
  				break;
  			case Range:
  				if (!isNumericRange(paramValue)) {
  					showMessage("Incorrect Numeric range!");
  					return; 
  				}
  				break;
  			default:  	  			
  				break;		
    	}
    	String selectedParam = conditionText.getText();
    	switch(gatewayType) {
    		case Contains: 
	  		case DoesNotContain:
	  			selectedValue = String.format(conditionExpression, getStringArray(paramValue), selectedParam);
	  			break;
	  		case Range:
	  			List<String> items = Arrays.asList(paramValue.split("\\s*,\\s*"));
	  			selectedValue = String.format(conditionExpression, selectedParam, items.get(0), selectedParam, items.get(1));
	  			break;
	  		default:
	  			selectedValue = String.format(conditionExpression, selectedParam, paramValue);
	  			break;	
    	}
    	super.okPressed();        
    }

    public String getConditionValue() {
        return selectedValue;
    }    
    
    private void showMessage(String message) {
    	MessageBox messageBox = new MessageBox(ActivitiPlugin.getShell(), SWT.ICON_WARNING | SWT.OK);
    	messageBox.setText("Warning");
    	messageBox.setMessage(message);
    	messageBox.open();	
    }
    
    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false; 
        }
        return pattern.matcher(strNum).matches();
    }
    
    private boolean isNumericRange(String strRange) {
        if (strRange == null) {
            return false; 
        }
        List<String> items = Arrays.asList(strRange.split("\\s*,\\s*"));
        if (items.size() != 2)
        	return false;
        
        for(String item : items) {
        	if (!isNumeric(item))					
        		return false;				
        }  
        if (Integer.valueOf(items.get(1)) < Integer.valueOf(items.get(0))) 
        	return false;
        return true;
    }
    
    private String getStringArray(String paramValue) {	
    	List<String> items = Arrays.asList(paramValue.split("\\s*,\\s*"));
    	String out = "";
    	
    	int size = items.size();
    	for (int i = 0; i < items.size(); i++) {
    		out += ("'" + items.get(i) + "'"); 
    		if (size > 1 && i < (size - 1))
    			out += ", ";
    	}
    	
    	return out;
    	
    }
}