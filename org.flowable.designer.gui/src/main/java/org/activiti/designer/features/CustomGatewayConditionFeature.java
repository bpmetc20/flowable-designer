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
package org.activiti.designer.features;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.eclipse.util.DiagramHandler;
import org.activiti.designer.features.CreateCustomGatewayFeature.GatewayType;
import org.activiti.designer.util.dialog.MyGatewayAreaDialog;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;

public class CustomGatewayConditionFeature extends AbstractCustomFeature {  

	private ExclusiveGateway exclusiveGateway;
	private String gatewayName;
	
	public CustomGatewayConditionFeature(IFeatureProvider fp, ExclusiveGateway exclusiveGateway, String gatewayName) {
		super(fp);
		
		this.exclusiveGateway = exclusiveGateway;	
		this.gatewayName = gatewayName;
	}	
	
	@Override
	public boolean canExecute(ICustomContext context) {
	  return true;
	}

	@Override
	public void execute(ICustomContext context) {
		//MyGatewayAreaDialog dialog = new MyGatewayAreaDialog(CreateCustomGatewayFeature.FLOW_YES, customGatewayName, conditionValue);
	}
	
	public String getGatewayName() {
		return gatewayName;
	}
	
	public GatewayType getGatewayType() {
		return CreateCustomGatewayFeature.getKey(gatewayName);
	}
	
	public String getImageKey() {
		return CreateCustomGatewayFeature.getImageKey(getGatewayType());
	}
	
}
