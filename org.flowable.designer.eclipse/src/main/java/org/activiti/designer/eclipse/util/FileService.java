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
package org.activiti.designer.eclipse.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.Set;

import org.activiti.designer.eclipse.editor.ActivitiDiagramEditor;
import org.activiti.designer.eclipse.editor.ActivitiDiagramEditorInput;
import org.activiti.designer.util.ActivitiConstants;
import org.activiti.designer.util.workspace.ActivitiWorkspaceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

public class FileService {	
	public static final String defaultProjectName = "FtdSolution";
		
	public static IFile getDiagramFile(String diagramName, Boolean created) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = getProject();
		if (project == null)
			project = root.getProject(defaultProjectName);
		if (!project.exists()) 
			project.create(null);
		if (!project.isOpen()) 
			project.open(null);
		IFolder folder = project.getFolder(ActivitiConstants.DIAGRAM_FOLDER);
		if (!folder.exists()) 
			folder.create(IResource.NONE, true, null);
		IFile file = folder.getFile(diagramName + ".bpmn");
		created = false;
		if (!file.exists()) {
			try {
				created = true;
				writeDiagramToIFile(file, "");				
			}
			catch (Exception e) {
				created = false;
			}
		}
		return file;
	}
		
	public static IProject getProject() {
		Set<IProject> projects = ActivitiWorkspaceUtil.getOpenProjects();
		if (!projects.isEmpty()) {
			return projects.iterator().next();
		}
		return null;
	}	
	
	/**
	 * Opens the given diagram specified by the given data file in a new editor. In case an error
	 *
	 * @param dataFile the data file to use for the new editor to open
	 *
	 * TODO: this is a copy from PropertyCallActivitySection. Figure out how to make sure we do not double this
	*/  
	public static void openDiagramForBpmnFile(IFile dataFile) throws PartInitException {
		 final IWorkbenchPage activePage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();	        
	     IDE.openEditor(activePage, dataFile, ActivitiConstants.DIAGRAM_EDITOR_ID, true);
	}
	
	
	public static IFile getCurrentDiagramFile() {
		 IEditorInput input = ActivitiDiagramEditor.get().getEdiotrInput();
		 if (input instanceof ActivitiDiagramEditorInput) {
			 return ((ActivitiDiagramEditorInput) input).getDataFile();			 
		 }
		 return null;
	}	 
	
	public static void writeDiagramToIFile(IFile file, String xmlString) throws IOException, CoreException {
		ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
		if ( file.exists() ) {
			file.setContents(bais, IFile.FORCE, null);
		} else {
			file.create(bais, IResource.NONE, null);
		}
		bais.close();
	}
	
		
	public static void createNewFile(IFile file, String newContent, IProgressMonitor monitor) throws CoreException, UnsupportedEncodingException {
		file.create(new ByteArrayInputStream(newContent.getBytes("UTF-8")), false, monitor); //$NON-NLS-1$
	}	
	
	public static String getDiagramName(IFile file) {		
		String fileName = file.getName();
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}	
	
	public static void deleteFile(IFile file) throws CoreException {
		if (file.exists()) 
			file.delete(true, null);		     
	}
	
	public static String getFileContent(IFile file) throws IOException, CoreException, UnsupportedEncodingException {
		Reader reader = new InputStreamReader(file.getContents(), file.getCharset());
		
		BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
	}
	
	public static void copy(String sourceFile, String dstFile) throws IOException {
		java.nio.file.Path sourceFilePath = Paths.get(sourceFile);
		java.nio.file.Path dstFilePath = Paths.get(dstFile);
		 
		Files.copy(sourceFilePath, dstFilePath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public static void createNewFile(String fullFileName) throws IOException {
		java.nio.file.Path path = Paths.get(fullFileName);
		Files.createFile(path);					 
	}
	
	public static long getLastModifiedTime(String fullFileName) throws IOException {
		java.nio.file.Path path = Paths.get(fullFileName);
		FileTime fileTime = Files.getLastModifiedTime(path);
		return fileTime.toMillis(); 
	}
	
	public static IFile fromFullName2IFIle(String fullFileName) {
		 IPath location = Path.fromOSString(fullFileName); 
		 return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
	}
	 
	public static String ifile2FullName(IFile dataFile) {
		 return dataFile.getLocationURI().getPath();
	}
	 
	public static String getPathFromFullPath(String diagramFileString) throws IOException {
		java.nio.file.Path p = Paths.get(diagramFileString);
		return p.getParent().toString();
	}
	 
	 public static String getNameFromFullPath(String diagramFileString)  throws IOException {
	 	 java.nio.file.Path p = Paths.get(diagramFileString);
	 	 String diagramName = p.getFileName().toString();
	 	 return diagramName.substring(0, diagramName.lastIndexOf('.'));	 	
     } 
   	

   /**
   * Returns a temporary file used as diagram file. Conceptually, this is a placeholder used by
   * Graphiti as editor input file. The real data file is found at the given data file path.
   *
   * @param dataFilePath path of the actual BPMN2 model file
   * @param diagramFileTempFolder folder containing the diagram files
   * @return an IFile for the temporary file. If the file exists, it is first
   *         deleted.
   */
  public static IFile getTemporaryDiagramFile(IPath dataFilePath, IFolder diagramFileTempFolder) {

    final IPath path = dataFilePath.removeFileExtension().addFileExtension(
            ActivitiConstants.DIAGRAM_FILE_EXTENSION_RAW);
    final IFile tempFile = diagramFileTempFolder.getFile(path.lastSegment());

    // We don't need anything from that file and to be sure there are no side
    // effects we delete the file
    if (tempFile.exists()) {
      try {
        tempFile.delete(true, null);
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }
    return tempFile;
  }

  /**
   * Returns or constructs a temporary folder for diagram files used as Graphiti editor input
   * files. The given path reflects the path where the original data file is located. The folder
   * is constructed in the project root named after the data file extension
   * {@link #DATA_FILE_EXTENSION_RAW}.
   *
   * @param dataFilePath path of the actual BPMN2 model file
   * @return an IFolder for the temporary folder.
   * @throws CoreException in case the folder could not be created.
   */
  public static IFolder getOrCreateTempFolder(IPath dataFilePath) throws CoreException {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    String name = dataFilePath.getFileExtension();
    if (name == null || name.length() == 0) {
      name = "bpmn";
    }

    String dir = dataFilePath.segment(0);
    IFolder folder = root.getProject(dir).getFolder("." + name);
    if (!folder.exists()) {
      folder.create(true, true, null);
    }
    String[] segments = dataFilePath.segments();
    for (int i = 1; i < segments.length - 1; i++) {
      String segment = segments[i];
      folder = folder.getFolder(segment);
      if (!folder.exists()) {
        folder.create(true, true, null);
      }
    }
    return folder;
  }

  /**
   * Recreates the data file from the given input path. In case the given path reflects a temporary
   * diagram file, it's path is used to recreate the data file, otherwise the given path is simply
   * made absolute and returned.
   *
   * @param inputPath the path to recreate the data file from
   * @return a file object representing the data file
   */
  public static IFile recreateDataFile(final IPath inputPath) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IProject project = root.getFile(inputPath).getProject();

    final int matchingSegments = project.getFullPath().matchingFirstSegments(inputPath);
    final int totalSegments = inputPath.segmentCount();
    final String extension = inputPath.getFileExtension();

    IFile result = null;

    if (totalSegments > matchingSegments) {
      // it shall be more than just the project

      IPath resultPath = null;

      if (ActivitiConstants.DIAGRAM_FILE_EXTENSION_RAW.equals(extension)) {
        // we got a temporary file here, so rebuild the file of the model from its path
        String originalExtension = inputPath.segment(matchingSegments);
        if (originalExtension.startsWith(".")) {
          originalExtension = originalExtension.substring(1);
        }

        final String[] segments = inputPath.segments();
        IPath originalPath = project.getFullPath();
        for (int index = matchingSegments + 1; index < segments.length; ++index) {
          originalPath = originalPath.append(segments[index]);
        }

        resultPath = originalPath.removeFileExtension().addFileExtension(originalExtension);
      }
      else {
        resultPath = inputPath.makeAbsolute();
      }

      result = root.getFile(resultPath);
    }

    return result;
  }

  /**
   * Returns the appropriate data file for the given input. The data file is the BPMN file with
   * the file extension {@link #DATA_FILE_EXTENSION_RAW}. This method can handle various different
   * editor input versions:
   *
   * <ul>
   *     <li>the special {@link ActivitiDiagramEditorInput} that directly references the data file</li>
   *     <li>a {@link DiagramEditorInput} that may either point to a data- or diagram file</li>
   *     <li>a {@link FileEditorInput} that points to a data file</li>
   *     <li>a {@link IURIEditorInput} that points to an external file outside Eclipse</li>
   * </ul>
   *
   * @param input the input to handle
   * @return the appropriate data file or <code>null</code> if none could be determined.
   */
  public static IFile getDataFileForInput(final IEditorInput input) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    if (input instanceof ActivitiDiagramEditorInput) {
      final ActivitiDiagramEditorInput adei = (ActivitiDiagramEditorInput) input;

      return adei.getDataFile();
    } else if (input instanceof DiagramEditorInput) {
      final DiagramEditorInput dei = (DiagramEditorInput) input;

      IPath path = new Path(dei.getUri().trimFragment().toPlatformString(true));

      return recreateDataFile(path);
    } else if (input instanceof FileEditorInput) {
      final FileEditorInput fei = (FileEditorInput) input;

      return fei.getFile();
    } else if (input instanceof IURIEditorInput) {
      // opened externally to Eclipse
      final IURIEditorInput uei = (IURIEditorInput) input;
      final java.net.URI uri = uei.getURI();
      final String path = uri.getPath();

      try {
        final IProject importProject = root.getProject("import");
        if (!importProject.exists()) {
          importProject.create(null);
        }

        importProject.open(null);

        final InputStream is = new FileInputStream(path);

        final String fileName;
        if (path.contains("/")) {
          fileName = path.substring(path.lastIndexOf("/") + 1);
        } else {
          fileName = path.substring(path.lastIndexOf("\\") + 1);
        }

        IFile importFile = importProject.getFile(fileName);
        if (importFile.exists()) {
          importFile.delete(true, null);
        }

        importFile.create(is, true, null);

        return importProject.getFile(fileName);
      } catch (CoreException exception) {
        exception.printStackTrace();
      } catch (FileNotFoundException exception) {
        exception.printStackTrace();
      }
    }

    return null;
  }



	public static TransactionalEditingDomain createEmfFileForDiagram(final URI diagramResourceUri
	                                                               , final Diagram diagram
	                                                               , final ActivitiDiagramEditor diagramEditor
	                                                               , final InputStream contentStream
	                                                               , final IFile resourceFile) {

		TransactionalEditingDomain editingDomain = null;
		ResourceSet resourceSet = null;

		if (diagramEditor == null || diagramEditor.getDiagramBehavior() == null || 
		    diagramEditor.getDiagramBehavior().getResourceSet() == null || diagramEditor.getEditingDomain() == null) {
		  
		  // nothing found, create a new one
		  resourceSet = new ResourceSetImpl();

		  // try to retrieve an editing domain for this resource set
		  editingDomain = TransactionUtil.getEditingDomain(resourceSet);

		  if (editingDomain == null) {
		    // not existing yet, create a new one
		    editingDomain = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(resourceSet);
		  }
		} else {
		  editingDomain = diagramEditor.getEditingDomain();
		  resourceSet = diagramEditor.getDiagramBehavior().getResourceSet();
		}

		// Create a resource for this file.
		final Resource resource = resourceSet.createResource(diagramResourceUri);
		final CommandStack commandStack = editingDomain.getCommandStack();
		commandStack.execute(new RecordingCommand(editingDomain) {

			@Override
			protected void doExecute() {
				resource.setTrackingModification(true);

				if (contentStream == null || resourceFile == null) {
	        resource.getContents().add(diagram);
				} else {
				  try {
				    resourceFile.create(contentStream, true, null);
				  } catch (CoreException exception) {
				    exception.printStackTrace();
				  }
				}

			}
		});

		save(editingDomain, Collections.<Resource, Map<?, ?>> emptyMap());
		return editingDomain;
	}

	private static void save(TransactionalEditingDomain editingDomain, Map<Resource, Map<?, ?>> options) {
		saveInWorkspaceRunnable(editingDomain, options);
	}

	private static void saveInWorkspaceRunnable(final TransactionalEditingDomain editingDomain,
			final Map<Resource, Map<?, ?>> options) {

		final Map<URI, Throwable> failedSaves = new HashMap<URI, Throwable>();
		final IWorkspaceRunnable wsRunnable = new IWorkspaceRunnable() {
			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {

				final Runnable runnable = new Runnable() {

					@Override
					public void run() {
						Transaction parentTx;
						if (editingDomain != null
								&& (parentTx = ((TransactionalEditingDomainImpl) editingDomain).getActiveTransaction()) != null) {
							do {
								if (!parentTx.isReadOnly()) {
									throw new IllegalStateException(
											"FileService.save() called from within a command (likely produces a deadlock)"); //$NON-NLS-1$
								}
							} while ((parentTx = ((TransactionalEditingDomainImpl) editingDomain)
									.getActiveTransaction().getParent()) != null);
						}

						final EList<Resource> resources = editingDomain.getResourceSet().getResources();
						// Copy list to an array to prevent
						// ConcurrentModificationExceptions
						// during the saving of the dirty resources
						Resource[] resourcesArray = new Resource[resources.size()];
						resourcesArray = resources.toArray(resourcesArray);
						final Set<Resource> savedResources = new HashSet<Resource>();
						for (final Resource resource : resourcesArray) {
							if (resource.isModified()) {
								try {
									resource.save(options.get(resource));
									savedResources.add(resource);
								} catch (final Throwable t) {
									failedSaves.put(resource.getURI(), t);
								}
							}
						}
					}
				};

				try {
					editingDomain.runExclusive(runnable);
				} catch (final InterruptedException e) {
					throw new RuntimeException(e);
				}
				editingDomain.getCommandStack().flush();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wsRunnable, null);
			if (!failedSaves.isEmpty()) {
				throw new WrappedException(createMessage(failedSaves), new RuntimeException());
			}
		} catch (final CoreException e) {
			final Throwable cause = e.getStatus().getException();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(e);
		}
	}

	private static String createMessage(Map<URI, Throwable> failedSaves) {
		final StringBuilder buf = new StringBuilder("The following resources could not be saved:");
		for (final Entry<URI, Throwable> entry : failedSaves.entrySet()) {
			buf.append("\nURI: ").append(entry.getKey().toString()).append(", cause: \n")
					.append(getExceptionAsString(entry.getValue()));
		}
		return buf.toString();
	}

	private static String getExceptionAsString(Throwable t) {
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		final String result = stringWriter.toString();
		try {
			stringWriter.close();
		} catch (final IOException e) {
			// $JL-EXC$ ignore
		}
		printWriter.close();
		return result;
	}
}
