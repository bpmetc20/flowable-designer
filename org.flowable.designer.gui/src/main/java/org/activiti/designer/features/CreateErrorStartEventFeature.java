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

import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.designer.PluginImage;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateErrorStartEventFeature extends AbstractCreateBPMNFeature {

  public static final String FEATURE_ID_KEY = getUUid();

  public CreateErrorStartEventFeature(IFeatureProvider fp) {
    // set name and description of the creation feature
    super(fp, "ErrorStartEvent", "Add error start event");
  }

  public boolean canCreate(ICreateContext context) {
    Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
    return (parentObject instanceof EventSubProcess);
  }

  public Object[] create(ICreateContext context) {
    StartEvent startEvent = new StartEvent();
    ErrorEventDefinition errorEvent = new ErrorEventDefinition();
    startEvent.getEventDefinitions().add(errorEvent);
    addObjectToContainer(context, startEvent, "Error start");

    // return newly created business object(s)
    return new Object[] { startEvent };
  }

  @Override
  public String getCreateImageId() {
    return PluginImage.IMG_EVENT_ERROR.getImageKey();
  }

  @Override
  protected String getFeatureIdKey() {
    return FEATURE_ID_KEY;
  }
}
