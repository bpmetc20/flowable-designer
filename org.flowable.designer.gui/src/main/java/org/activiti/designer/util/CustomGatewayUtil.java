package org.activiti.designer.util;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.TextAnnotation;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.activiti.designer.eclipse.util.RefreshDiagramHandler;
import org.activiti.designer.features.CreateCustomGatewayFeature;
import org.activiti.designer.features.CreateCustomGatewayFeature.GatewayType;
import org.activiti.designer.util.dialog.MyGatewayAreaDialog;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
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
					yesSequenceFlowFlow(sequenceFlow, customGatewayName, exclusiveGateway);
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
						yesSequenceFlowFlow(sequenceFlow, customGatewayName, exclusiveGateway);				
					}		
				}
			}			
		}
	}
	
	static private void yesSequenceFlowFlow(SequenceFlow sequenceFlow, String customGatewayName, ExclusiveGateway exclusiveGateway) {
		String conditionExpression = CreateCustomGatewayFeature.getCondition(customGatewayName);
		GatewayType gatewayType = CreateCustomGatewayFeature.getKey(customGatewayName);
		MyGatewayAreaDialog dialog = new MyGatewayAreaDialog(CreateCustomGatewayFeature.FLOW_YES, customGatewayName, 
				gatewayType, conditionExpression, ActivitiPlugin.getProjectsParam(false), sequenceFlow.getConditionExpression());
	 	dialog.create();
		dialog.open();
		String conditionValue = dialog.getConditionValue();
		sequenceFlow.setConditionExpression(conditionValue);
		sequenceFlow.setName(CreateCustomGatewayFeature.FLOW_YES);
		updateCustomGatewayAssociation(exclusiveGateway, conditionValue);
		RefreshDiagramHandler.refreshDiagram();
	}
	
	static public void setGatewayCondition(ExclusiveGateway bo, TextAnnotation ta) {
		  String gatewayName = CreateCustomGatewayFeature.isCustomGatewayRef(bo.getId());
	      if (!gatewayName.isEmpty()) { 
	    	  for (SequenceFlow outgoingSequenceFlow : bo.getOutgoingFlows()) {					
	    		  if (!outgoingSequenceFlow.getName().isEmpty()) {
	    			  if (outgoingSequenceFlow.getName().equals(CreateCustomGatewayFeature.FLOW_YES)) {
	    				  String expression = outgoingSequenceFlow.getConditionExpression();
	    				  ta.setText(expression);
	    				  RefreshDiagramHandler.refreshDiagram();
	    				  break;
	    			  }																	
	    		  }
	    	  }
	      }
	}
	
	static public boolean updateCustomGatewayAssociation(ExclusiveGateway bo, String conditionExopression) {		
		boolean needRefresh = false;
		List<Process> processes = ActivitiDiagramEditor.get().getProcesses();
  	  	for (Process process : processes) {
  	  		List<Association> associations = new ArrayList<Association>();
  	  		//review association pointed to gateway
  	  		for (Artifact artifact : process.getArtifacts()) {
  	  			if (artifact instanceof Association) {
  	  				Association association = (Association) artifact;
  	  				if (association.getSourceRef().equals(bo.getId()) || association.getTargetRef().equals(bo.getId())) 
  	  					associations.add(association);       
  	  				
  	  			}
  	  		}	      
  	  		//review annotations associated with gateway
  	  		for (Artifact artifact : process.getArtifacts()) {
  	  			if (artifact instanceof TextAnnotation) {
  	  				TextAnnotation annotation = (TextAnnotation) artifact;
  	  				for (Association association : associations) {
  	  					if (association.getSourceRef().equals(annotation.getId()) || association.getTargetRef().equals(annotation.getId())) { 
  	  						annotation.setText(conditionExopression); 
  	  						needRefresh = true;
  	  					}
  	  				}
  	  			}
  	  		}  	  		
  	  	}
  	  	return needRefresh;
	}
}
 