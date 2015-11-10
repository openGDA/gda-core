/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.client.experimentdefinition;

import gda.rcp.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.reflection.RichBeanUtils;
import org.eclipse.richbeans.xml.XMLBeanContentDescriberFactory;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ClientManager;
import uk.ac.gda.client.experimentdefinition.components.ExperimentExperimentView;
import uk.ac.gda.client.experimentdefinition.components.ExperimentFolderEditor;
import uk.ac.gda.client.experimentdefinition.components.ExperimentPerspective;
import uk.ac.gda.client.experimentdefinition.components.ExperimentProjectNature;
import uk.ac.gda.client.experimentdefinition.components.ExperimentRunEditor;
import uk.ac.gda.client.experimentdefinition.components.XMLFileDialog;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class ExperimentEditorManager implements IExperimentEditorManager {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentEditorManager.class);

	protected IProject currentProject;

	protected List<ISelectionListener> listeners;

	/** DO not set this, use off() and on() **/
	private boolean on = true;

	private Properties prefs;

	protected Object selected;

	private ISelectionListener selectionListener;

	public ExperimentEditorManager() {

		XMLHelpers.setUrlResolver(EclipseUtils.getUrlResolver());

		cleanWorkspace();

		currentProject = createExperimentProject();

		Job refreshExafs = new Job("Refresh Experiment Project") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					currentProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (Exception ne) {
					logger.error("Cannot refresh project.", ne);
				}
				return Status.OK_STATUS;
			}
		};

		refreshExafs.setUser(true);
		refreshExafs.schedule();

		addSelectedProjectListener();
		restoreSelected();
	}

	/*
	 * Delete any existing references to the project used by this class, as they could be looking at the wrong folder.
	 */
	private void cleanWorkspace() {
		// Get all the projects and if none is exafs, create a new one.
		IProject preexistingProject = findPreexistingProject();

		if (preexistingProject != null) {
			try {
				preexistingProject.delete(false, true, null);
				preexistingProject = null;
			} catch (CoreException e) {
				logger.error("Exception error removing old project: workspace may be corrupt. You may need to restart the GDA client using the --reset option.", e);
			}
		}
	}

	private IProject findPreexistingProject() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject[] projects = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			try {
				if (!projects[i].isOpen()) {
					continue;
				}
				if (projects[i].hasNature(ExperimentProjectNature.ID)) {
					return projects[i];
				}
			} catch (CoreException ignored) {
				// Carry on, we just want to find one that is Exafs the others
				// we dont care about.
			}
		}
		return null;
	}

	private void addSelectedProjectListener() {
		ISelectionService service = (ISelectionService) PlatformUI.getWorkbench().getService(ISelectionService.class);
		if (service != null && selectionListener == null) {
			this.selectionListener = new ISelectionListener() {
				@Override
				public void selectionChanged(IWorkbenchPart part, ISelection selection) {
					if (selection instanceof StructuredSelection) {
						final Object sel = ((StructuredSelection) selection).getFirstElement();
						if (sel instanceof IProject) {
							currentProject = (IProject) sel;
						}
					}
				}
			};
			service.addSelectionListener(selectionListener);
		}
	}

	@Override
	public void addSelectionListener(ISelectionListener selectionListener) {
		if (listeners == null) {
			listeners = new ArrayList<ISelectionListener>(7);
		}
		listeners.add(selectionListener);
	}

	@Override
	public void closeAllEditors(boolean b) {
		getActivePage().closeAllEditors(b);
	}

	@Override
	public void closeEditor(IFile file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (getActivePage() == null) {
			return;
		}

		final IEditorInput data = new FileEditorInput(file);
		final IEditorReference[] refs = getActivePage().getEditorReferences();
		for (int i = 0; i < refs.length; i++) {
			try {
				if (refs[i].getEditorInput().equals(data)) {
					getActivePage().closeEditors(new IEditorReference[] { refs[i] }, false);
				}
			} catch (PartInitException e) {
				logger.error("Editor cannot return input file ", e);
			}
		}
	}

	private IProject createExperimentProject() {

		String projectName = ExperimentFactory.getExperimentProjectName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		try {

			if (project.exists()) {
				project.open(null);
			} else {

				IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
				IPath location = new Path(Application.getXmlPath());
				desc.setLocation(location);
				// note: do not add ExperimentProjectNature to the description here as
				// it expects to be added after project is created and opened.

				project.create(desc, null);

				project.open(null);

				IProjectDescription description = project.getDescription();
				description.setNatureIds(new String[] { ExperimentProjectNature.ID });
				project.setDescription(description, null);
			}

			return project;

		} catch (CoreException e) {
			if (!ClientManager.isTestingMode()) {
				logger.error("Cannot create .exafs project: " + e.getStatus().toString(), e);
			}
		}

		return project;
	}

	@Override
	public void editSelectedElement() {
		if (getViewer() == null) {
			return;
		}
		getViewer().editElement(getSelected());

	}

	/**
	 * Belt and braces approach to ensuring of the editors are talking to the same resource.
	 *
	 * @param orig
	 * @param cur
	 * @return true if same file.
	 */
	private boolean equals(IEditorInput orig, IEditorInput cur) {
		if (orig.equals(cur)) {
			return true;
		}
		if (orig instanceof IFileEditorInput && cur instanceof IFileEditorInput) {
			final IFile of = ((IFileEditorInput) orig).getFile();
			final IFile cf = ((IFileEditorInput) cur).getFile();

			if (of.getLocation().equals(cf.getLocation())) {
				return true;
			}
		}
		if (orig instanceof FileStoreEditorInput && cur instanceof FileStoreEditorInput) {
			final File of = EclipseUtils.getFile(orig);
			final File cf = EclipseUtils.getFile(cur);

			if (of.getAbsoluteFile().equals(cf.getAbsoluteFile())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ExperimentFolderEditor getActiveFolderEditor() {
		try {
			final IWorkbenchPage page = getActivePage();
			ExperimentFolderEditor ed = (ExperimentFolderEditor) page.getActiveEditor();
			return ed.isFocus() || (getViewer() != null && getViewer().isFocus()) ? ed : null;
		} catch (Throwable ne) {
			return null;
		}
	}

	protected IWorkbenchPage getActivePage() {
		return EclipseUtils.getActivePage();
	}

	@Override
	public ExperimentRunEditor getActiveRunEditor() {
		try {
			final IWorkbenchPage page = getActivePage();
			ExperimentRunEditor ed = (ExperimentRunEditor) page.getActiveEditor();
			return ed.isFocus() ? ed : null;
		} catch (Throwable ne) {
			return null;
		}
	}

	@Override
	public IProject getCurrentProject() {
		return currentProject;
	}

	@Override
	public RichBeanMultiPageEditorPart getEditor(IFile file) {
		if (file == null || !file.exists()) {
			return null;
		}
		if (getActivePage() == null) {
			return null;
		}

		final String id = XMLBeanContentDescriberFactory.getInstance().getId(file);
		if (id != null && !"".equals(id)) {
			if (getActivePage().findEditor(new FileEditorInput(file)) instanceof RichBeanMultiPageEditorPart) {
				return (RichBeanMultiPageEditorPart) getActivePage().findEditor(new FileEditorInput(file));
			}
		}
		return null;
	}

	public IEditorInput getEditorInput(File file) {

		final File projectFolder = getProjectFolder();

		if (projectFolder.equals(file.getParentFile())) {
			final IFile in = getIFile(file);
			return new FileEditorInput(in);
		}
		return new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(file));
	}

	@Override
	public IFile getIFile(File file) {
		String parentFileName = file.getParentFile().getName();
		IFolder parentFolder = currentProject.getFolder(parentFileName);
		String fileName = file.getName();
		return parentFolder.getFile(fileName);
	}

	@Override
	public IFile getIFile(IEditorInput input) {
		IFile file = EclipseUtils.getIFile(input);
		if (file != null) {
			return file;
		}

		if (input instanceof FileStoreEditorInput) {
			final FileStoreEditorInput in = (FileStoreEditorInput) input;
			return getIFile(EclipseUtils.getFile(in));
		}

		return null;
	}

	@Override
	public IFolder getIFolder(String folder) {
		return currentProject.getFolder(folder);
	}

	private Properties getPreferences() {
		if (prefs == null) {
			prefs = new Properties();
			try {
				final File file = new File(getProjectFolder(), ".prefs");
				if (file.exists()) {
					final FileInputStream in = new FileInputStream(file);
					try {
						prefs.load(in);
					} finally {
						in.close();
					}
				}
			} catch (Exception ne) {
				logger.error("Cannot read properties", ne);
			}
		}
		return prefs;
	}

	@Override
	public File getProjectFolder() {
		return currentProject.getLocation().toFile();
	}

	@Override
	public Object getSelected() {
		return selected;
	}

	@Override
	public IFile getSelectedFile() {
		final IWorkbenchPage page = getActivePage();
		final IEditorPart ed = page != null ? page.getActiveEditor() : null;

		IFile ret = null;
		if (ed != null) {
			if (ed instanceof ExperimentRunEditor) {
				ret = ((ExperimentRunEditor) ed).getScanFile();
			}
		}

		final Object ob = getSelected();
		if (ob instanceof IExperimentObjectManager) {
			ret = ((IExperimentObjectManager) ob).getFile();
		} else if (ob instanceof IExperimentObject) {
			IExperimentObjectManager man;
			man = ExperimentFactory.getManager(((IExperimentObject) ob).getFolder(),
					((IExperimentObject) ob).getMultiScanName());
			ret = man.getFile();
		}

		return ret;
	}

	@Override
	public IFolder getSelectedFolder() {
		final IWorkbenchPage page = getActivePage();
		final IEditorPart ed = page != null ? page.getActiveEditor() : null;
		IFolder ret = null;
		if (ed != null) {
			if (ed instanceof ExperimentFolderEditor) {
				ret = ((ExperimentFolderEditor) ed).getCurrentDirectory();
			} else if (ed instanceof ExperimentRunEditor) {
				ret = ((ExperimentRunEditor) ed).getCurrentDirectory();
			}
		}

		final Object ob = getSelected();
		if (ob instanceof IFolder) {
			ret = (IFolder) ob;
		} else if (ob instanceof IExperimentObjectManager) {
			ret = ((IExperimentObjectManager) ob).getContainingFolder();
		} else if (ob instanceof IExperimentObject) {
			ret = ((IExperimentObject) ob).getFolder();
		}

		return ret;
	}

	@Override
	public IExperimentObjectManager getSelectedMultiScan() {
		final IWorkbenchPage page = getActivePage();
		final IEditorPart ed = page != null ? page.getActiveEditor() : null;
		if (ed != null) {
			if (ed instanceof ExperimentRunEditor) {
				return ((ExperimentRunEditor) ed).getRunObjectManager();
			}
		}

		final Object sel = getSelected();
		if (sel instanceof IExperimentObjectManager) {
			return (IExperimentObjectManager) sel;

		} else if (sel instanceof IExperimentObject) {
			((IExperimentObject) sel).getMultiScanName();
			IExperimentObject ob = ((IExperimentObject) sel);
			try {
				return ExperimentFactory.getManager(ob.getFolder(), ob.getMultiScanName());
			} catch (Exception e) {
				logger.error("Exception trying to find the run manager for " + ob.getRunName(), e);
			}

		}
		return null;
	}

	@Override
	public IExperimentObject getSelectedScan() {
		IWorkbenchPage page = getActivePage();
		IExperimentObject ret = null;
		if (page != null) { // It's null when a dialog is open
			final ExperimentExperimentView controls = (ExperimentExperimentView) page
					.findView(ExperimentExperimentView.ID);
			if (controls != null && controls.isFocus())
				return controls.getSelectedScan();

			final IEditorPart ed = page.getActiveEditor();
			if (ed != null)
				if (ed instanceof ExperimentRunEditor)
					ret = ((ExperimentRunEditor) ed).getSelectedRun();
		}
		final Object ob = getSelected();
		if (ob instanceof IExperimentObject)
			ret = (IExperimentObject) ob;
		return ret;
	}

	@SafeVarargs
	@Override
	public final Object getValueFromUIOrBean(final String fieldName, IBeanController control, final Class<? extends XMLRichBean>... classes) throws Exception {

		IFieldWidget uiBox = control.getBeanField(fieldName, classes);
		if (uiBox != null)
			return uiBox.getValue();

		IExperimentObject ob = ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		List<XMLRichBean> params = ob.getParameters();
		for (Object object : params)
			for (int i = 0; i < classes.length; i++)
				if (classes[i].isInstance(object))
					return RichBeanUtils.getBeanValue(object, fieldName);

		return null;
	}

	@Override
	public ExperimentExperimentView getViewer() {
		final IWorkbenchPage page = getActivePage();
		if (page == null)
			return null;
		return (ExperimentExperimentView) page.findView(ExperimentExperimentView.ID);
	}

	protected boolean isEditor(IFile file) {
		final IEditorPart part = getEditor(file);
		return getActivePage().isPartVisible(part);
	}

	protected boolean isOn() {
		return on;
	}

	private void moveEditorToTheLeftEnd(final IWorkbenchPage page, final IEditorPart part) {
		// hide and then reshow all editors, with new one leftmost
		IEditorReference[] references = page.getEditorReferences();
		IEditorReference newpartRef = null;
		for (IEditorReference ref : references) {
			try {
				if (ref.getEditorInput() == part.getEditorInput())
					newpartRef = ref;
			} catch (PartInitException e) {
				// ignore this, we just want to hide the editors. Should never
				// get this by this point.
			}
			page.hideEditor(ref);
		}

		if (newpartRef != null)
			page.showEditor(newpartRef);

		for (IEditorReference ref : references) {
			try {
				if (ref.getEditorInput() != part.getEditorInput())
					page.showEditor(ref);
			} catch (PartInitException e) {
				// ignore this as we want to show all the editors again
			}
		}
	}

	@Override
	public void notifyFileNameChange(String oldName, IFile newFile) throws CoreException {
		notifyNameChange(oldName, newFile);

	}

	@Override
	public void notifyFileNameChange(String origName, IFolder to) throws CoreException {
		notifyNameChange(origName, to);

	}

	protected void notifyNameChange(String oldName, IResource newFile) throws CoreException {
		final IWorkbenchPage page = getActivePage();
		if (page == null)
			return;

		final IEditorReference[] refs = page.getEditorReferences();

		final IEditorInput orig;
		final IEditorInput chan;
		newFile.refreshLocal(IResource.DEPTH_ONE, null);
		if (newFile instanceof IFile) {
			orig = new FileEditorInput(((IFolder) newFile.getParent()).getFile(oldName));
			chan = new FileEditorInput((IFile) newFile);
		} else if (newFile instanceof IFolder) {
			orig = getEditorInput(((IProject) newFile.getParent()).getFolder(oldName).getLocation().toFile());
			chan = getEditorInput(newFile.getLocation().toFile());
		} else {
			throw new RuntimeException("Must only notify with IFile or IFolder.");
		}

		for (int i = 0; i < refs.length; i++) {
			final IEditorReference ref = refs[i];
			try {
				final IEditorInput cur = ref.getEditorInput();
				if (equals(orig, cur)) {
					IEditorPart part = ref.getEditor(false);
					if (part == null)
						continue;
					// If setInput public then call it
					try {
						final Method setInput = part.getClass().getMethod("setInput", IEditorInput.class);
						setInput.invoke(part, chan);
					} catch (Exception e) {
						logger.error("Editor without public setInput method detected " + part, e);
					}
				}
			} catch (PartInitException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void notifySelectionListeners() {
		if (!isOn())
			return;
		if (listeners == null)
			return;
		final StructuredSelection evt = new StructuredSelection(getSelected());
		for (ISelectionListener l : listeners)
			l.selectionChanged(getViewer(), evt);
	}

	/**
	 * off listeners, and notifications.
	 */
	protected void off() {
		this.on = false;
	}

	/**
	 * on listeners, and notifications.
	 */
	protected void on() {
		this.on = true;
	}

	/**
	 * Opens the editors for the first scan encountered.
	 *
	 * @param ob
	 * @param checkCurrentPerspective
	 * @return true if selected someone
	 */
	@Override
	public boolean openDefaultEditors(final IExperimentObject ob, boolean checkCurrentPerspective) {
		if (ClientManager.isTestingMode())
			return true; // We do not open other editors.
		if (ob == null)
			return false;
		if (checkCurrentPerspective && !EclipseUtils.isActivePerspective(ExperimentPerspective.ID))
			return false;
		try {
			openRequiredEditors(ob);
		} catch (Throwable ne) {
			logger.error("Cannot open default editors", ne);
		}
		return true;
	}

	/*
	 * Returns the files to open. This resolves when files are missing.
	 */
	private List<IFile> listFilesToOpen(final IExperimentObject ob) {
		Map<String, IFile> mapOfTypesToFiles = ob.getFilesWithTypes();
		Collection<IExperimentBeanDescription> allBeanDescriptions = ExperimentBeanManager.INSTANCE
				.getBeanDescriptions();

		// reorder Map based on order in allBeanDescriptions as this is the same order registered in the Extension Point
		// which should be the order to be shown in the UI
		mapOfTypesToFiles = orderMapOfTypes(ob, mapOfTypesToFiles, allBeanDescriptions);

		// start list of files to open and list of missing file types
		List<IFile> filteredEditorList = new ArrayList<IFile>();
		for (String fileType : mapOfTypesToFiles.keySet()) {
			IFile file = mapOfTypesToFiles.get(fileType);
			if (file != null && file.exists())
				filteredEditorList.add(file);
			else {
				// now match missing file types to descriptions
				Vector<IExperimentBeanDescription> beansOfType = new Vector<IExperimentBeanDescription>();
				Vector<IExperimentBeanDescription> beanType = new Vector<IExperimentBeanDescription>();
				for (IExperimentBeanDescription beanDesc : allBeanDescriptions) {
					if (beanDesc.getBeanType().equalsIgnoreCase(fileType)) {
						beansOfType.add(beanDesc);
						if (beanDesc.includeInNew()) {
							beanType.add(beanDesc); // only want one here
							break;
						}
					}
				}
				// if a problem then take any type.
				if (beanType.size() == 0)
					beanType.set(0, beansOfType.get(0));
				IFile selection;
				if (file == null) {
					XMLFileDialog xmlFileDialog = new XMLFileDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), beanType, "Incomplete Setup for "
							+ beanType.get(0).getBeanType(), "The file has not been set yet.\n\n"
							+ "Choose an existing file, or to create a new file, or press Cancel");
					selection = xmlFileDialog.open(ob.getFolder());
				} else {
					XMLFileDialog xmlFileDialog = new XMLFileDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), beanType, "Incomplete Setup for "
							+ beanType.get(0).getBeanType(), "The file '" + file.getName() + "' does not exist in '"
							+ file.getParent().getName() + "'\n\n"
							+ "Choose an existing file, create a new file, or press Cancel");
					selection = xmlFileDialog.open(file.getParent());
				}
				if (selection != null) {
					String name = selection.getName();
					ob.setFileName(beanType.get(0).getBeanType(), name);
					if (!"None".equalsIgnoreCase(name))
						filteredEditorList.add(selection);
					try {
						IExperimentObjectManager man = ExperimentFactory.getManager(ob.getFolder(),
								ob.getMultiScanName());
						man.write();
					} catch (Exception e) {
						logger.error("Cannot write: " + ob.getRunName(), e);
					}
				}
			}
		}
		return filteredEditorList;
	}

	/*
	 * Orders the Map to match the order of bean types stored in the extension, the order of editors expected in the UI
	 * by users
	 */
	protected Map<String, IFile> orderMapOfTypes(IExperimentObject ob, Map<String, IFile> mapOfTypesToFiles,
			Collection<IExperimentBeanDescription> allBeanDescriptions) {

		IExperimentObjectManager man = ExperimentFactory.getManager(ob.getFolder(), ob.getMultiScanName());
		String[] typesInOrder = man.getOrderedColumnBeanTypes();

		HashMap<String, IFile> orderedMap = new HashMap<String, IFile>();

		for (String type : typesInOrder) {
			for (IExperimentBeanDescription desc : allBeanDescriptions) {
				if (type.equalsIgnoreCase(desc.getBeanType()))
					orderedMap.put(type, mapOfTypesToFiles.get(type));
			}
		}
		return orderedMap;
	}

	protected IEditorPart[] openRequiredEditors(final IExperimentObject ob) {
		boolean anyEditors = false;
		try {
			anyEditors = getActivePage().getEditorReferences().length > 0;
		} catch (Exception e) {
			anyEditors = false;
		}
		List<IFile> filesToOpen = listFilesToOpen(ob);
		IEditorPart[] editors = new IEditorPart[filesToOpen.size()];
		if (anyEditors) {
			// open each editor required and put it all thw
			for (int i = editors.length - 1; i >= 0; i--) {
				if (isEditor(filesToOpen.get(i))) {
					editors[i] = getEditor(filesToOpen.get(i));
					moveEditorToTheLeftEnd(getActivePage(), editors[i]);
				} else
					editors[i] = openEditorAndMoveToTheLeft(filesToOpen.get(i), false);
			}

		} else
			for (int i = editors.length - 1; i >= 0; i--)
				editors[i] = openEditorAndMoveToTheLeft(filesToOpen.get(i), false);

		if (editors.length > 0) {
			closeUnwantedEditors(editors);
			getActivePage().activate(editors[0]);
		}
		return editors;
	}

	private void closeUnwantedEditors(IEditorPart[] ourEditors) {
		IEditorReference[] openEdRefs = getActivePage().getEditorReferences();
		IEditorInput[] ourEdParts = new IEditorInput[ourEditors.length];
		for (int i = 0; i < ourEditors.length; i++)
			ourEdParts[i] = ourEditors[i].getEditorInput();
		IEditorReference[] edRefsToClose = new IEditorReference[0];
		for (IEditorReference edRef : openEdRefs) {
			try {
				if (!ArrayUtils.contains(ourEdParts, edRef.getEditorInput()))
					edRefsToClose = (IEditorReference[]) ArrayUtils.add(edRefsToClose, edRef);
			} catch (PartInitException e) {
				logger.warn("Exception initialising " + edRef.getContentDescription(), e);
			}
		}
		getActivePage().closeEditors(edRefsToClose, true);
	}

	@Override
	public IEditorPart openEditor(IFile file) {
		return openEditor(file, true);
	}

	@Override
	public IEditorPart openEditor(IFile file, boolean activate) {
		if (file == null || !file.exists())
			return null;
		if (getActivePage() == null)
			return null;
		return openEditor(getActivePage(), file, activate);
	}

	@Override
	public IEditorPart openEditor(IFile file, String id, boolean closeOtherEditors) {
		return openEditor(new FileEditorInput(file), id, closeOtherEditors);
	}

	private IEditorPart openEditor(IEditorInput input, String id, boolean closeOtherEditors) {
		IWorkbenchPage page = getActivePage();
		if (page == null)
			return null;
		if (page.getActiveEditor() != null)
			if (page.getActiveEditor().getEditorSite().getId().equals(id))
				if (page.getActiveEditor().getEditorInput().equals(input))
					return page.getActiveEditor();
		if (closeOtherEditors)
			page.closeAllEditors(true);
		try {
			return page.openEditor(input, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IEditorPart openEditor(IFolder file, String id, boolean closeOtherEditors) {
		return openEditor(getEditorInput(file.getLocation().toFile()), id, closeOtherEditors);
	}

	protected IEditorPart openEditor(final IWorkbenchPage page, final IFile file, final boolean activate) {
		if (file == null || !file.exists() || page == null)
			return null;
		IEditorInput input = new FileEditorInput(file);
		String id = XMLBeanContentDescriberFactory.getInstance().getId(file);
		if (id != null && !"".equals(id)) {
			try {
				return page.openEditor(input, id, activate);
			} catch (PartInitException e) {
				logger.error("Cannot open editor " + id, e);
			}
		}
		return null;
	}

	private IEditorPart openEditorAndMoveToTheLeft(IFile iFile, boolean activate) {
		IEditorPart newpart = openEditor(iFile, activate);
		if (newpart != null)
			moveEditorToTheLeftEnd(getActivePage(), newpart);
		return newpart;
	}

	@Override
	public IEditorPart openEditorAndMoveToTheLeft(IFile file) {
		return openEditorAndMoveToTheLeft(file, true);
	}

	@Override
	public void refreshViewers() {
		if (getViewer() == null)
			return;
		getViewer().refreshTree();
	}

	@Override
	public void removeSelectionListener(ISelectionListener selectionListener) {
		if (listeners == null)
			listeners = new ArrayList<ISelectionListener>(7);
		listeners.remove(selectionListener);
	}

	private void restoreSelected() {
		final Properties prefs = getPreferences();
		if (!prefs.containsKey("path"))
			return;
		final IFile file = currentProject.getFile(new Path(prefs.getProperty("path")));
		if (!file.exists())
			return;
		IExperimentObjectManager man = null;
		try {
			man = ExperimentFactory.getManager(file);
		} catch (Exception e) {
			logger.error("Cannot restore " + file, e);
		}

		final int index = Integer.parseInt(prefs.getProperty("index"));
		if (index > -1 && man != null && index < man.getExperimentList().size()) {
			selected = man.getExperimentList().get(index);
			if (selected == null)
				selected = man.getExperimentList().get(0);
		} else if (man != null)
			selected = man;
		else
			selected = file;
	}

	private void saveProperties(final Properties prefs) {
		try {
			final File file = new File(getProjectFolder(), ".prefs");
			final FileOutputStream out = new FileOutputStream(file);
			try {
				prefs.store(out, "Automatically generated properties.");
			} finally {
				out.close();
			}

		} catch (Exception ne) {
			logger.error("Cannot read properties", ne);
		}

	}

	private void saveSelectedPath(final Object selected) {
		if (selected == null)
			return;
		IFile selPath = null;
		int scanIndex = -1;
		if (selected instanceof IFile)
			selPath = (IFile) selected;
		else if (selected instanceof IExperimentObjectManager)
			selPath = ((IExperimentObjectManager) selected).getFile();
		else if (selected instanceof IExperimentObject) {
			final IExperimentObject ob = (IExperimentObject) selected;
			final IExperimentObjectManager man = ExperimentFactory.getManager(ob);
			selPath = man.getFile();
			scanIndex = man.getExperimentList().indexOf(ob);
		}
		if (selPath == null)
			return;
		Properties prefs = getPreferences();
		prefs.setProperty("path", selPath.getProjectRelativePath().toString());
		prefs.setProperty("index", String.valueOf(scanIndex));
		saveProperties(prefs);
	}

	@Override
	public void select(Object element) {
		if (element == null)
			return;
		if (getViewer() == null)
			return;
		try {
			off();
			getViewer().setSelected(element); // Fires listeners
		} finally {
			on();
		}
	}

	@Override
	public void setSelected(Object selected) {
		this.selected = selected;
		saveSelectedPath(selected);
	}

}