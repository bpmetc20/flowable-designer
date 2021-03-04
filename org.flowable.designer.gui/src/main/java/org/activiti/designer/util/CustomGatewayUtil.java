package org.activiti.designer.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.TextAnnotation;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.util.RefreshDiagramHandler;
import org.activiti.designer.features.CreateCustomGatewayFeature;
import org.activiti.designer.features.CreateCustomGatewayFeature.GatewayType;
import org.activiti.designer.util.dialog.MyGatewayAreaDialog;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import java.util.List;

import org.activiti.bpmn.model.ExclusiveGateway;

public class CustomGatewayUtil { 
	static public void addSequencFlow(SequenceFlow sequenceFlow, BpmnMemoryModel model) {
		String customGatewayName = CreateCustomGatewayFeature.isCustomGatewayRef(sequenceFlow.getSourceRef());	
	    if (!customGatewayName.isEmpty()) {		
	    	if (sequenceFlow.getName().isEmpty()) {			
				FlowElement sourceElement = model.getFlowElement(sequenceFlow.getSourceRef());
				ExclusiveGateway exclusiveGateway = (ExclusiveGateway)sourceElement; 
				List<SequenceFlow> flows = exclusiveGateway.getOutgoingFlows();
				if (flows.size() == 1) {
					yesSequenceFlowFlow(sequenceFlow, customGatewayName);
				} else {
					boolean flowYes = false;
					for (SequenceFlow outgoingSequenceFlow : exclusiveGateway.getOutgoingFlows()) {					
						if (!outgoingSequenceFlow.getName().isEmpty()) {
							flowYes = outgoingSequenceFlow.getName().equals(CreateCustomGatewayFeature.FLOW_YES);
							break;													
						}
					}
					if (flowYes) {
						sequenceFlow.setName(CreateCustomGatewayFeature.FLOW_NO);
					} else {
						yesSequenceFlowFlow(sequenceFlow, customGatewayName);				
					}		
				}
			}			
		}
	}
	
	static private void yesSequenceFlowFlow(SequenceFlow sequenceFlow, String customGatewayName) {
		String conditionExpression = CreateCustomGatewayFeature.getCondition(customGatewayName);
		GatewayType gatewayType = CreateCustomGatewayFeature.getKey(customGatewayName);
		MyGatewayAreaDialog dialog = new MyGatewayAreaDialog(CreateCustomGatewayFeature.FLOW_YES, customGatewayName, 
				gatewayType, conditionExpression, ActivitiPlugin.getProjectsParam(false), sequenceFlow.getConditionExpression());
	 	dialog.create();
		dialog.open();
		sequenceFlow.setConditionExpression(dialog.getConditionValue());
		sequenceFlow.setName(CreateCustomGatewayFeature.FLOW_YES);	
		RefreshDiagramHandler.refreshDiagram(null);
	}
	
	static public void setGatewayCondition(ExclusiveGateway bo, TextAnnotation ta) {
		  String gatewayName = CreateCustomGatewayFeature.isCustomGatewayRef(bo.getId());
	      if (!gatewayName.isEmpty()) { 
	    	  for (SequenceFlow outgoingSequenceFlow : bo.getOutgoingFlows()) {					
	    		  if (!outgoingSequenceFlow.getName().isEmpty()) {
	    			  if (outgoingSequenceFlow.getName().equals(CreateCustomGatewayFeature.FLOW_YES)) {
	    				  String expression = outgoingSequenceFlow.getConditionExpression();
	    				  String text = String.format("%s: %s", gatewayName, expression);
	    				  ta.setText(text);
	    				  RefreshDiagramHandler.refreshDiagram(null);
	    				  break;
	    			  }																	
	    		  }
	    	  }
	      }
	  }
}
 