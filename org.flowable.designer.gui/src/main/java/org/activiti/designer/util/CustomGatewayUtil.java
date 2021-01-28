package org.activiti.designer.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.designer.features.CreateEqualGatewayFeature;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import java.util.List;
import org.activiti.bpmn.model.ExclusiveGateway;


public class CustomGatewayUtil { 
	
	static public void addSequencFlow(SequenceFlow sequenceFlow, BpmnMemoryModel model) {
		if (sequenceFlow.getSourceRef().contains(CreateEqualGatewayFeature.FEATURE_ID_KEY) && sequenceFlow.getName().isEmpty()) {			
			FlowElement sourceElement = model.getFlowElement(sequenceFlow.getSourceRef());
			ExclusiveGateway exclusiveGateway = (ExclusiveGateway)sourceElement; 
			List<SequenceFlow> flows = exclusiveGateway.getOutgoingFlows();
			if (flows.size() == 1) {
				sequenceFlow.setConditionExpression(CreateEqualGatewayFeature.CONDITION_EXPRESSION);
				sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_YES);
			} else {
				boolean yes = false;
				for (SequenceFlow outgoingSequenceFlow : exclusiveGateway.getOutgoingFlows()) {					
					if (!outgoingSequenceFlow.getName().isEmpty()) {
						yes = outgoingSequenceFlow.getName().equals(CreateEqualGatewayFeature.FLOW_YES);
						break;
													
					}
				}
				if (yes) {
					sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_NO);					
				} else {
					sequenceFlow.setConditionExpression(CreateEqualGatewayFeature.CONDITION_EXPRESSION);
					sequenceFlow.setName(CreateEqualGatewayFeature.FLOW_YES);				
				}		
			}
		}	
	}	
}
 