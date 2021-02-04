package org.activiti.designer.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.designer.features.CreateCustomGatewayFeature;
import org.activiti.designer.util.dialog.MyGatewayAreaDialog;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import java.util.List;

import org.activiti.bpmn.model.ExclusiveGateway;

public class CustomGatewayUtil { 
	static public void addSequencFlow(SequenceFlow sequenceFlow, BpmnMemoryModel model) {
		if (sequenceFlow.getSourceRef().contains(CreateCustomGatewayFeature.FEATURE_ID_KEY)) {
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
							flowYes = outgoingSequenceFlow.getName().equals(CreateCustomGatewayFeature.FLOW_YES);
							break;													
						}
					}
					if (flowYes) {
						sequenceFlow.setName(CreateCustomGatewayFeature.FLOW_NO);
					} else {
						yesSequenceFlowFlow(sequenceFlow);				
					}		
				}
			}			
		}
	}
	
	static private void yesSequenceFlowFlow(SequenceFlow sequenceFlow) {
		String[] strArray = new String[1];
		strArray[0] = CreateCustomGatewayFeature.CONDITION_EXPRESSION;
		MyGatewayAreaDialog dialog = new MyGatewayAreaDialog(CreateCustomGatewayFeature.FLOW_YES, CreateCustomGatewayFeature.FEATURE_ID_KEY, strArray);
	 	dialog.create();
		dialog.open();
		sequenceFlow.setConditionExpression(dialog.getConditionValue());
		sequenceFlow.setName(CreateCustomGatewayFeature.FLOW_YES);
	}
}
 