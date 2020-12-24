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
package org.activiti.designer.util.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.activiti.designer.integration.usertask.CustomUserTask;

public class CustomUserTaskContextImpl implements CustomUserTaskContext {

  private static final String DEFAULT_ICON_PATH = "icons/defaultCustomUserTask.png";
  private static final String ERROR_ICON_PATH = "icons/errorCustomUserTask.png";
  private static final String FTD_ICON_PATH = "icons/Ftd.png";

  private static final String ERROR_ICON_MESSAGE_PATTERN = "The CustomUserTask '%s' has an incorrect icon path '%s', so the icon cannot be shown. A placeholder error icon will be shown instead.";

  private final CustomUserTask customUserTask;

  private final String extensionName;
  private final String extensionJarPath;
  private JarFile extensionJarFile;
  private UserTaskProperties customUserTaskkProperties;

  public CustomUserTaskContextImpl(final CustomUserTask customUserTask, final String extensionName, final String extensionJarPath) {
    this.customUserTask = customUserTask;
    this.extensionName = extensionName;
    this.extensionJarPath = extensionJarPath;
    customUserTaskkProperties = ExtensionUtil.getCustomTasksUserProperties();
    try {
      this.extensionJarFile = new JarFile(this.extensionJarPath);
    } catch (IOException e) {
      throw new IllegalArgumentException("Path is an invalid path for a JarFile", e);
    }
  }

  @Override
  public InputStream getSmallIconStream() {
	  InputStream result = null;
	    
	  try {
	    	result = getFtdCustomUserTaskIconStream();
	  } catch (Exception e) {
		  System.err.println(String.format(ERROR_ICON_MESSAGE_PATTERN, this.customUserTask.getId(), 
				  this.customUserTask.getSmallIconPath()));
	      result = getErrorCustomUserTaskIconStream();
	  }  
    

    /*
    
    final String path = this.customUserTask.getSmallIconPath();
    if (path != null) {
      JarEntry imgentry = extensionJarFile.getJarEntry(path);

      try {
        result = extensionJarFile.getInputStream(imgentry);
      } catch (Exception e) {
        System.err.println(String.format(ERROR_ICON_MESSAGE_PATTERN, this.customUserTask.getId(), path));
        result = getErrorCustomUserTaskIconStream();
      }
    } else {
      result = getDefaultCustomUserTaskIconStream();
    }
    */

    return result;
  }
  @Override
  public InputStream getLargeIconStream() {
	  InputStream result = null;
	    
	  try {
	    	result = getFtdCustomUserTaskIconStream();
	  } catch (Exception e) {
		  System.err.println(String.format(ERROR_ICON_MESSAGE_PATTERN, this.customUserTask.getId(), 
				  this.customUserTask.getSmallIconPath()));
	      result = getErrorCustomUserTaskIconStream();
	  }
	  
    /*
	InputStream result = null;

    final String path = this.customUserTask.getLargeIconPath();
    if (path != null) {
      JarEntry imgentry = extensionJarFile.getJarEntry(path);

      try {
        result = extensionJarFile.getInputStream(imgentry);
      } catch (Exception e) {
        System.err.println(String.format(ERROR_ICON_MESSAGE_PATTERN, this.customUserTask.getId(), path));
        result = getErrorCustomUserTaskIconStream();
      }
    } else {
      result = getDefaultCustomUserTaskIconStream();
    }
	*/
	
    return result;
  }

  @Override
  public InputStream getShapeIconStream() {
    InputStream result = null;
    
	  try {
	    	result = getFtdCustomUserTaskIconStream();
	  } catch (Exception e) {
		  System.err.println(String.format(ERROR_ICON_MESSAGE_PATTERN, this.customUserTask.getId(), 
				  this.customUserTask.getSmallIconPath()));
	      result = getErrorCustomUserTaskIconStream();
	  }
	
	/*  

    final String path = this.customUserTask.getShapeIconPath();
    if (path != null) {
      JarEntry imgentry = extensionJarFile.getJarEntry(path);

      try {
        result = extensionJarFile.getInputStream(imgentry);
      } catch (Exception e) {
        System.err.println(String.format(ERROR_ICON_MESSAGE_PATTERN, this.customUserTask.getId(), path));
        result = getErrorCustomUserTaskIconStream();
      }
    } else {
      result = getDefaultCustomUserTaskIconStream();
    }
    
    */

    return result;
  }

  @Override
  public CustomUserTask getUserTask() {
    return this.customUserTask;
  }

  @Override
  public String getExtensionName() {
    return this.extensionName;
  }

  @Override
  public String getSmallImageKey() {
    return getExtensionName() + "/small/" + getUserTask().getId();
  }

  @Override
  public String getLargeImageKey() {
    return getExtensionName() + "/large/" + getUserTask().getId();
  }

  @Override
  public String getShapeImageKey() {
    return getExtensionName() + "/shape/" + getUserTask().getId();
  }

  private InputStream getDefaultCustomUserTaskIconStream() {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_ICON_PATH);
  }

  private InputStream getErrorCustomUserTaskIconStream() {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(ERROR_ICON_PATH);
  }
  
  private InputStream getFtdCustomUserTaskIconStream() {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(FTD_ICON_PATH);
  }

  @Override
  public int compareTo(CustomUserTaskContext otherCustomUserTaskContext) {
    if (otherCustomUserTaskContext instanceof CustomUserTaskContext) {
      return getUserTask().getOrder().compareTo(otherCustomUserTaskContext.getUserTask().getOrder());
    }
    return 0;
  }

}
