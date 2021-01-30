package org.activiti.designer.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.designer.features.CreateEqualGatewayFeature;
import org.activiti.designer.handlers.MyGatewayAreaDialog;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import java.util.List;

import org.activiti.bpmn.model.ExclusiveGateway;

public class CustomGatewayUtil { 
	static public void addSequencFlow(SequenceFlow sequenceFlow, BpmnMemoryModel model) {
		if (sequenceFlow.getSourceRef().contains(CreateEqualGatewayFeature.FEATURE_ID_KEY)) { 
			if (sequenceFlow.getName().isEmpty()) {			
				FlowElement sourceElement = model.getFlowElement(sequenceFlow.getSourceRef());
				ExclusiveGateway exclusiveGateway = (ExclusiveGateway)sourceElement; 
				List<SequenceFlow> flows = exclusiveGateway.getOutgoingFlows();
				if (flows.size() == 1) {
					yesSequenceFlowFlow(sequenceFlow);
				} else {
					boolean flowYes = false;
					for (SequenceFlow outgoingSequenceFlow : exclusiveGateway.getOutgoingFlows()) {					
						if (!outgoingSequenceFlow.getName().isEmpty()) {
							flowYes = outgoingSequenceFlow.getName().equals(CreateEqualGatewayFeature.FLOW_YES);
							break;													
						}
					}
					if (flowYes) {
						sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_NO);					
					} else {
						yesSequenceFlowFlow(sequenceFlow);				
					}		
				}
			}			
		}
	}
	
	static private void yesSequenceFlowFlow(SequenceFlow sequenceFlow) {
		MyGatewayAreaDialog dialog = new MyGatewayAreaDialog(CreateEqualGatewayFeature.FLOW_YES, CreateEqualGatewayFeature.FEATURE_ID_KEY, 
				CreateEqualGatewayFeature.CONDITION_EXPRESSION);
	 	dialog.create();
		dialog.open();
		sequenceFlow.setConditionExpression(dialog.getConditionValue());
		sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_YES);
	}
}
 