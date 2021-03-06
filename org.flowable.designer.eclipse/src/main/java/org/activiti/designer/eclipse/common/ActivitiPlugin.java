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
package org.activiti.designer.eclipse.common;

import java.util.List;
import java.awt.Menu;
import java.awt.MenuItem;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditorInput;
import org.activiti.designer.eclipse.util.DiagramHandler;
import org.activiti.designer.eclipse.util.FileService;
import org.activiti.designer.eclipse.util.PaletteExtensionUtil;
import org.activiti.designer.eclipse.util.RestClient;
import org.activiti.designer.util.extension.ExtensionUtil;
import org.activiti.designer.util.extension.UserTaskProperties;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ActivitiPlugin extends AbstractUIPlugin {

  public static final String PLUGIN_ID = "org.activiti.designer.eclipse"; //$NON-NLS-1$

  public static final String GUI_PLUGIN_ID = "org.activiti.designer.gui"; //$NON-NLS-1$

  /**
   * The name used for the user library that contains extensions for the
   * Activiti Designer.
   */
  public static final String USER_LIBRARY_NAME_EXTENSIONS = "Activiti Designer Extensions";

  public static final String DESIGNER_EXTENSIONS_USER_LIB_PATH = "org.eclipse.jdt.USER_LIBRARY/" + USER_LIBRARY_NAME_EXTENSIONS;

  public static final String EXPORT_MARSHALLER_EXTENSIONPOINT_ID = "org.activiti.designer.eclipse.extension.ExportMarshaller";

  public static final String PROCESS_VALIDATOR_EXTENSIONPOINT_ID = "org.activiti.designer.eclipse.extension.ProcessValidator";

  public static final String ICON_PROVIDER_EXTENSIONPOINT_ID = "org.activiti.designer.eclipse.extension.IconProvider";

  public static final String PALETTE_EXTENSION_PROVIDER_EXTENSIONPOINT_ID = "org.activiti.designer.eclipse.extension.PaletteExtensionProvider";
  
  public static final String NEW_FORM = "New Form";

  private static ActivitiPlugin _plugin;
  
  
  /// from cloud /////
  private static List<Map<String, String>> models = null;
  private static Map<String, String> forms = null;
  private static Map<String, String> taskCategories = null;
  private static Map<String, String> groups = null;
  private static Map<String, String> users = null;
  private static List<UserTaskProperties> userTaskProperties = null;
  private static Map<String, String> projectParams = null;
  
  ///////////////

  // The image cache object used in the plugin
  private static ImageCache imageCache;

  /**
   * Creates the Plugin and caches its default instance.
   */
  public ActivitiPlugin() {
    _plugin = this;
  }

  // ============ overwritten methods of AbstractUIPlugin ====================

  /**
   * This method is called upon plug-in activation.
   * 
   * @param context
   *          the context
   * 
   * @throws Exception
   *           the exception
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    // Initialize the image cache
    imageCache = new ImageCache();
        
    PaletteExtensionUtil.pushPaletteExtensions();
    
    hideMenu();
    startTabtListener();
    
    //load data from cloud
    getModels(true); 
    getForms(true); 
    getTaskCategories(true);
    getGroups(true);
    getUsers(true);
    getTasksUserProperties(true); 
    getProjectsParam(true);
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    // Allow the image cache to destroy itself so image references are cleaned
    // up
    imageCache.dispose();
  }

  // ======================== static access methods ==========================

  /**
   * Gets the default-instance of this plugin. Actually the default-instance
   * should always be the only instance -> Singleton.
   * 
   * @return the default
   */
  public static ActivitiPlugin getDefault() {
    return _plugin;
  }

  // =========================== public helper methods ======================

  /**
   * Returns the current Workspace.
   * 
   * @return The current Workspace.
   */
  public static IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  /**
   * Returns the URL, which points to where this Plugin is installed.
   * 
   * @return The URL, which points to where this Plugin is installed.
   */
  public static URL getInstallURL() {
    return getDefault().getBundle().getEntry("/");
  }

  /**
   * Returns the Plugin-ID.
   * 
   * @return The Plugin-ID.
   */
  public static String getID() {
    return getDefault().getBundle().getSymbolicName();
  }

  /**
   * Returns the currently active WorkbenchPage.
   * 
   * @return The currently active WorkbenchPage.
   */
  public static IWorkbenchPage getActivePage() {
    IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    if (workbenchWindow != null)
      return workbenchWindow.getActivePage();
    return null;
  }

  /**
   * Returns the currently active Shell.
   * 
   * @return The currently active Shell.
   */
  public static Shell getShell() {
    return getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in
   * relative path
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }
  
  ///// data from cloud //////////////////  
  public static List<Map<String, String>> getModels(boolean reload) { 
	if (reload) 
		models = RestClient.getModels();
	return models;
  }
  
  public static Map<String, String> getForms(boolean reload) { 
	 if (reload) 
		 forms = RestClient.getForms();
	 forms.put("", NEW_FORM);
	 return forms;
  }	
  
  /**
   * Returns the custom tasks properties.
   * 
   * @return the custom tasks properties.
   */
  public static Map<String, String> getTaskCategories(boolean reload) {
	  if (reload)  
		  taskCategories = RestClient.getTaskCategories();
	  return taskCategories;
  }
  
  /**
   * Returns the groups.
   * 
   * @return the groups.
   */
  public static Map<String, String> getGroups(boolean reload) {
	  if (reload) 
		  groups = RestClient.getGroups();
	  return groups;
  }
  
  /**
   * Returns the users.
   * 
   * @return the users.
   */
  public static Map<String, String> getUsers(boolean reload) {
	  if (reload) 
		  users = RestClient.getUsers();
	  return users;
  }
  
  public static List<UserTaskProperties> getTasksUserProperties(boolean reload) {
	  if (reload)
		  userTaskProperties = RestClient.getUserTaskProperties();
	  return userTaskProperties;
  } 
  
  public static Map<String, String> getProjectsParam(boolean reload) {
	  if (reload)
		  projectParams = RestClient.getProjectsParamNames();
	  return projectParams;
  } 
  
  ////////////////////////////////
  
  /**
   * Gets an image from this plugin and serves it from the {@link ImageCache}.
   * 
   * @param pluginImage
   *          the PluginImage to get the image for
   * 
   * @return an Image if the image was found, null otherwise
   */
  public static Image getImage(PluginImage pluginImage) {
    return imageCache.getImage(pluginImage);
  }

  /**
   * Gets an image from this plugin and serves it from the {@link ImageCache}.
   * 
   * @param imageDescriptor
   *          the ImageDescriptor to get the image for
   * 
   * @return an Image if the image was found, null otherwise
   */
  public static Image getImage(ImageDescriptor imageDescriptor) {
    return ImageCache.getImage(imageDescriptor);
  }

  /**
   * Gets an image descriptor for an image from this plugin.
   * 
   * @param pluginImage
   *          the PluginImage to get the image descriptor for
   * 
   * @return an ImageDescriptor if the image was found, null otherwise
   */
  public static ImageDescriptor getImageDescriptor(PluginImage pluginImage) {
    return getImageDescriptor(pluginImage.getImagePath());
  }
  
  public static void startTabtListener() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    workbenchWindow.getPartService().addPartListener(new IPartListener() {                
	            
			@Override
			public void partActivated(IWorkbenchPart part) {
				// TODO Auto-generated method stub
					
			}

			@Override
			public void partBroughtToTop(IWorkbenchPart part) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if (part instanceof ActivitiDiagramEditor) {
					ActivitiDiagramEditor editor = (ActivitiDiagramEditor) part;
					IEditorInput input = editor.getEdiotrInput();
					if (input instanceof ActivitiDiagramEditorInput) {
						 FileService.setActiveDiagram(((ActivitiDiagramEditorInput) input).getDataFile());
					}					
				}	
			}

			@Override
			public void partClosed(IWorkbenchPart part) {
				// TODO Auto-generated method stub
				if (part instanceof ActivitiDiagramEditor) {
					ActivitiDiagramEditor editor = (ActivitiDiagramEditor) part;
					IEditorInput input = editor.getEdiotrInput();
					if (input instanceof ActivitiDiagramEditorInput) {
						FileService.diagramClosed(((ActivitiDiagramEditorInput) input).getDataFile());						 
					}					
				}	
			}

			@Override
			public void partDeactivated(IWorkbenchPart part) {
				// TODO Auto-generated method stub
					
			}

			@Override
			public void partOpened(IWorkbenchPart part) {
				// TODO Auto-generated method stub
				if (part instanceof ActivitiDiagramEditor) {
					ActivitiDiagramEditor editor = (ActivitiDiagramEditor) part;
					IEditorInput input = editor.getEdiotrInput();
					if (input instanceof ActivitiDiagramEditorInput) {
						 FileService.diagramOpened(((ActivitiDiagramEditorInput) input).getDataFile());
					}					
				}	
			}
	    });		
	}
  
  public static void hideMenu() {
	  IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	  IContributionItem[] items = ((WorkbenchWindow)workbenchWindow).getMenuBarManager().getItems();
      for (IContributionItem item : items) {
    	  boolean visible = item.getId().equals("processMenu");
    	  item.setVisible(visible);
      }
      ((WorkbenchWindow)workbenchWindow).getMenuBarManager().update();     
  }  
}
