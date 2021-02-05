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

import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.designer.PluginImage;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateCustomGatewayFeature extends AbstractCreateFastBPMNFeature {

  public static final String FEATURE_ID_KEY = "equalgateway";
  public static final String CONDITION_EXPRESSION = "${A} = ${B}";
  public static final String FLOW_YES = "Yes";
  public static final String FLOW_NO = "No";
  
  private GatewayType getewayType;
  
  public enum GatewayType {
	  Equals,
	  NotEqual,
	  Contains, 
	  DoesNotContain,
	  Greater,
	  GreaterThanOrEqualTo,
	  LessThan,
	  LessThanOrEqualTo,
	  StartWith,
	  EndWith,
	  Range	  
  }
  
  private static final Map<GatewayType, String > nameMap = createMap();


  public CreateCustomGatewayFeature(IFeatureProvider fp, GatewayType getewayType) {
	  super(fp, "ExclusiveGateway", "Add " + getFeatureName(getewayType));
	  
	  this.getewayType = getewayType;	  
  }
  
  private static Map<GatewayType, String> createMap() {
	    Map<GatewayType, String> myMap = new HashMap<GatewayType, String>();
	    myMap.put(GatewayType.Equals, "EqualsGateway");
	    myMap.put(GatewayType.NotEqual, "NotEqualGateway");
	    myMap.put(GatewayType.Contains, "ContainsGateway");
	    myMap.put(GatewayType.DoesNotContain, "DoesNotContainGateway");
	    myMap.put(GatewayType.Greater, "GreaterGateway");
	    myMap.put(GatewayType.GreaterThanOrEqualTo, "GreaterThanOrEqualToGateway");
	    myMap.put(GatewayType.LessThan, "LessThanGateway");
	    myMap.put(GatewayType.LessThanOrEqualTo, "LessThanOrEqualToGateway");
	    myMap.put(GatewayType.StartWith, "StartWithGateway");
	    myMap.put(GatewayType.EndWith, "EndWithGateway");
	    myMap.put(GatewayType.Range, "RangeGateway");
	    return myMap;
	}
  
  public static String getFeatureName(GatewayType getewayType) {
	  return nameMap.get(getewayType);
  }
  
  public String getImageKey(GatewayType getewayType) {
	  switch(getewayType) {
	  	default:
	  	case Equals:
	  		return PluginImage.IMG_GATEWAY_Equals.getImageKey();
	  	case NotEqual:
	  		return PluginImage.IMG_GATEWAY_NotEqual.getImageKey();
	  	case Contains: 
	  		return PluginImage.IMG_GATEWAY_Contains.getImageKey();
	  	case DoesNotContain:
	  		return PluginImage.IMG_GATEWAY_Contains.getImageKey();
	  	case Greater:
	  		return PluginImage.IMG_GATEWAY_DoesNotContain.getImageKey();
	  	case GreaterThanOrEqualTo:
	  		return PluginImage.IMG_GATEWAY_GreaterThanOrEqualTo.getImageKey();
	  	case LessThan:
	  		return PluginImage.IMG_GATEWAY_LessThan.getImageKey();
	  	case LessThanOrEqualTo:
	  		return PluginImage.IMG_GATEWAY_LessThanOrEqualTo.getImageKey();
	  	case StartWith:
	  		return PluginImage.IMG_GATEWAY_StartWith.getImageKey();
	  	case EndWith:
	  		return PluginImage.IMG_GATEWAY_EndWith.getImageKey();
	  	case Range:
	  		return PluginImage.IMG_GATEWAY_Range.getImageKey();	  		  		
	  }
  }

  public Object[] create(ICreateContext context) {
    ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
    addObjectToContainer(context, exclusiveGateway, "Exclusive Gateway");

    return new Object[] { exclusiveGateway };
  }

  @Override
  public String getCreateImageId() {
    return getImageKey(getewayType);
  }

  @Override
  protected String getFeatureIdKey() {
    return getFeatureName(getewayType);
  }
}
