package org.activiti.designer.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.designer.features.CreateEqualGatewayFeature;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.bpmn.model.ExclusiveGateway;


public class CustomGatewayUtil { 
	
	static public void addYesSequencFlow(SequenceFlow sequenceFlow, BpmnMemoryModel model) {
		if (sequenceFlow.getSourceRef().contains(CreateEqualGatewayFeature.FEATURE_ID_KEY) && sequenceFlow.getName().isEmpty()) {			
			FlowElement sourceElement = model.getFlowElement(sequenceFlow.getSourceRef());
			ExclusiveGateway exclusiveGateway = (ExclusiveGateway)sourceElement; 
			int flows = exclusiveGateway.getOutgoingFlows().size();
			if (flows == 1) {
				sequenceFlow.setConditionExpression(CreateEqualGatewayFeature.CONDITION_EXPRESSION);
				sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_YES);
			} else {
				sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_NO);
			}	
		}
	}
	
    
}
 