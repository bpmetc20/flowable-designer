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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.designer.PluginImage;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateCustomGatewayFeature extends AbstractCreateFastBPMNFeature {
  
  private static final String CONDITION_EXPRESSION_EQUAL = "${%s} = '%s'";
  private static final String CONDITION_EXPRESSION_NOTEQUAL = "${%s} != '%s'";
  private static final String CONDITION_EXPRESSION_CONTAINS = "[%s].includes(${%s})";
  private static final String CONDITION_EXPRESSION_CONTAINSNOT = "!${%s}.includes(${%s})";
  private static final String CONDITION_EXPRESSION_GRATER = "${%s} > '%s'";
  private static final String CONDITION_EXPRESSION_GRATEREQUAL = "${%s} >= '%s'";
  private static final String CONDITION_EXPRESSION_LESS = "${%s} < '%s'";
  private static final String CONDITION_EXPRESSION_LESSEQUAL = "${%s} <= '%s'";
  private static final String CONDITION_EXPRESSION_STARTWITH = "${%s}.startsWith('%s'}";
  private static final String CONDITION_EXPRESSION_ENDWITH = "${%s}.endsWith('%s'}";
  private static final String CONDITION_EXPRESSION_RANGE = "${%s} >= '%s' && ${%s} <= '%s'" ;
    
  public static final String FLOW_YES = "Yes";
  public static final String FLOW_NO = "No";
  
  private GatewayType getewayType;
  
  public enum GatewayType {
	  Equals,
	  NotEqual,
	  Contains, 
	  ContainsNot,
	  Greater,
	  GreaterEqual,
	  Less,
	  LessEqual,
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
	    myMap.put(GatewayType.Equals, "EqualGateway");
	    myMap.put(GatewayType.NotEqual, "NotEqualGateway");
	    myMap.put(GatewayType.Contains, "ContainsGateway");
	    myMap.put(GatewayType.ContainsNot, "DoesNotContainGateway");
	    myMap.put(GatewayType.Greater, "GreaterGateway");
	    myMap.put(GatewayType.GreaterEqual, "GreaterEqualGateway");
	    myMap.put(GatewayType.Less, "LessGateway");
	    myMap.put(GatewayType.LessEqual, "LessEqualGateway");
	    myMap.put(GatewayType.StartWith, "StartWithGateway");
	    myMap.put(GatewayType.EndWith, "EndWithGateway");
	    myMap.put(GatewayType.Range, "RangeGateway");
	    return myMap;
	}
  
  public static String getFeatureName(GatewayType getewayType) {
	  return nameMap.get(getewayType);
  }
  
  public static String isCustomGatewayRef(String sourceRef) {
	  List<String> gatewayNames = new ArrayList<String>(nameMap.values());
	  for (String name : gatewayNames) {
		  if (sourceRef.toLowerCase().startsWith(name.toLowerCase()))
			  return name;		 
	  }
	  return "";
  }
  
  public static GatewayType getKey(String value) {
	  Stream<GatewayType> stream = keys(nameMap, value);
	  return stream.collect(Collectors.toList()).get(0); 	  
  }
  
  public static String getCondition(String value) {
	  switch(getKey(value)) {
	  	default:
	  	case Equals:
	  		return CONDITION_EXPRESSION_EQUAL;
	  	case NotEqual:
	  		return CONDITION_EXPRESSION_NOTEQUAL;
	  	case Contains: 
	  		return CONDITION_EXPRESSION_CONTAINS;
	  	case ContainsNot:
	  		return CONDITION_EXPRESSION_CONTAINSNOT;
	  	case Greater:
	  		return CONDITION_EXPRESSION_GRATER;
	  	case GreaterEqual:
	  		return CONDITION_EXPRESSION_GRATEREQUAL;
	  	case Less:
	  		return CONDITION_EXPRESSION_LESS;
	  	case LessEqual:
	  		return CONDITION_EXPRESSION_LESSEQUAL;
	  	case StartWith:
	  		return CONDITION_EXPRESSION_STARTWITH;
	  	case EndWith:
	  		return CONDITION_EXPRESSION_ENDWITH;
	  	case Range:
	  		return CONDITION_EXPRESSION_RANGE;	  		  		
	  }
  }
  
  public static String getImageKey(GatewayType getewayType) {
	  switch(getewayType) {
	  	default:
	  	case Equals:
	  		return PluginImage.IMG_GATEWAY_Equals.getImageKey();
	  	case NotEqual:
	  		return PluginImage.IMG_GATEWAY_NotEqual.getImageKey();
	  	case Contains: 
	  		return PluginImage.IMG_GATEWAY_Contains.getImageKey();
	  	case ContainsNot:
	  		return PluginImage.IMG_GATEWAY_ContainsNot.getImageKey();
	  	case Greater:
	  		return PluginImage.IMG_GATEWAY_Greater.getImageKey();
	  	case GreaterEqual:
	  		return PluginImage.IMG_GATEWAY_GreaterEqual.getImageKey();
	  	case Less:
	  		return PluginImage.IMG_GATEWAY_Less.getImageKey();
	  	case LessEqual:
	  		return PluginImage.IMG_GATEWAY_LessEqual.getImageKey();
	  	case StartWith:
	  		return PluginImage.IMG_GATEWAY_StartWith.getImageKey();
	  	case EndWith:
	  		return PluginImage.IMG_GATEWAY_EndWith.getImageKey();
	  	case Range:
	  		return PluginImage.IMG_GATEWAY_Range.getImageKey();	  		  		
	  }
  }
  
  public static String getImageKeyLarge(GatewayType getewayType) {
	  switch(getewayType) {
	  	default:
	  	case Equals:
	  		return PluginImage.IMG_GATEWAY_EqualsLarge.getImageKey();
	  	case NotEqual:
	  		return PluginImage.IMG_GATEWAY_NotEqualLarge.getImageKey();
	  	case Contains: 
	  		return PluginImage.IMG_GATEWAY_ContainsLarge.getImageKey();
	  	case ContainsNot:
	  		return PluginImage.IMG_GATEWAY_ContainsNotLarge.getImageKey();
	  	case Greater:
	  		return PluginImage.IMG_GATEWAY_GreaterLarge.getImageKey();
	  	case GreaterEqual:
	  		return PluginImage.IMG_GATEWAY_GreaterEqualLarge.getImageKey();
	  	case Less:
	  		return PluginImage.IMG_GATEWAY_LessLarge.getImageKey();
	  	case LessEqual:
	  		return PluginImage.IMG_GATEWAY_LessEqualLarge.getImageKey();
	  	case StartWith:
	  		return PluginImage.IMG_GATEWAY_StartWithLarge.getImageKey();
	  	case EndWith:
	  		return PluginImage.IMG_GATEWAY_EndWithLarge.getImageKey();
	  	case Range:
	  		return PluginImage.IMG_GATEWAY_RangeLarge.getImageKey();	  		  		
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
  
  public static <K, V> Stream<K> keys(Map<K, V> map, V value) {
	  return map
	  .entrySet()
	  .stream()
	  .filter(entry -> value.equals(entry.getValue()))
	  .map(Map.Entry::getKey);
  }
}
