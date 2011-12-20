/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.results.navigator.actions.OpenDataFileAction;
import uk.ac.gda.common.rcp.util.IFileUtils;
import uk.ac.gda.common.rcp.util.PathUtils;
import uk.ac.gda.ui.file.AlphaNumericFileSorter;
import uk.ac.gda.ui.file.DateFileSorter;
import uk.ac.gda.ui.file.IFileTreeColumnProvider;
import uk.ac.gda.ui.file.IFileTreeContentProvider;
import uk.ac.gda.ui.file.IFileTreeLabelProvider;
import uk.ac.gda.ui.file.SizeFileSorter;
import uk.ac.gda.ui.viewer.ViewerFilterFactory;

/**
 * 
 */
// no longer used by GDA client - to be removed after release 8.14
@Deprecated
public class DataNavigator extends ViewPart {

	public static final String DATA_PROJECT_NAME = "Data";

	public static final String DATA_WORKINGSET_NAME = "Data";

	private static final Logger logger = LoggerFactory.getLogger(DataNavigator.class);
	
	/**
	 * Do not break encapsulation, leave private.
	 */
	private static String DEFAULT_FILE_PATH = PathUtils.createFromDefaultProperty();

	/**
	 * 
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.results.navigator.ResultsNavigator"; //$NON-NLS-1$

	private TreeViewer resultsViewer;
	private IProject   project;
	
	/**
	 * 
	 */
	public DataNavigator() {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		this.project = root.getProject(DATA_PROJECT_NAME);
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
				
		this.resultsViewer  = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI );
		
		ColumnViewerToolTipSupport.enableFor(resultsViewer);

		final TreeViewerColumn name = new TreeViewerColumn(resultsViewer, SWT.NONE, 0);
		name.setLabelProvider(new IFileTreeColumnProvider());
		name.getColumn().setText("Name");
		name.getColumn().setWidth(350);
		name.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultsViewer.setComparator(new AlphaNumericFileSorter());
			}
		});

		final TreeViewerColumn size = new TreeViewerColumn(resultsViewer, SWT.NONE, 1);
		size.getColumn().setText("Size");
		size.getColumn().setWidth(100);
		size.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultsViewer.setComparator(new SizeFileSorter());
			}
		});

		final TreeViewerColumn date = new TreeViewerColumn(resultsViewer, SWT.NONE, 2);
		date.getColumn().setText("Date Modified");
		date.getColumn().setWidth(250);
		date.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resultsViewer.setComparator(new DateFileSorter());
			}
		});
		
		resultsViewer.setColumnProperties(new String[]{"Name","Size","Date Modified"});
		resultsViewer.setUseHashlookup(true);
		resultsViewer.setContentProvider(new IFileTreeContentProvider(project));
		resultsViewer.setLabelProvider(new IFileTreeLabelProvider());
		resultsViewer.getTree().setHeaderVisible(true);

		//resultsViewer.setInput(rootFile);
		//resultsViewer.expandToLevel(3);
		try {
			setupProject(project, null);
		} catch (Exception e1) {
			logger.error("Cannot create project", e1);
		}
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		if(workingSetManager.getWorkingSet(DATA_WORKINGSET_NAME)==null){
			workingSetManager.addWorkingSet(workingSetManager.createWorkingSet(DATA_WORKINGSET_NAME, new IAdaptable[]{project}));
		}			

		getSite().setSelectionProvider(resultsViewer);

		createRightClickMenu();
		createDragSupport(resultsViewer);
		createActions();
		initializeToolBar();
		initializeMenu();
		
        addSelectionListener(resultsViewer);
        
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (getSite().getSelectionProvider()!=null) {
			((Viewer)getSite().getSelectionProvider()).getControl().dispose();
		}
	}
	
	private void createDragSupport(final TreeViewer viewer) {
		// We allow people to copy files from the run folder into another folder.
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance()};
		viewer.addDragSupport(ops, transfers, new DragSourceListener() {
			@Override
			public void dragStart(DragSourceEvent event) {
				// Do nothing
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.doit     = false;
				if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
					final IResource file = (IResource)((IStructuredSelection)viewer.getSelection()).getFirstElement();
					if (file == null)       return;
					if (!file.exists())     return;
					if (file instanceof IContainer) return;
					final String[] files = new String[1];
					files[0] = file.getLocation().toOSString();
					event.data     = files;
					event.doit     = true;
				}
			}
			@Override
			public void dragFinished(DragSourceEvent event) {
				// Do nothing
			}
			
		});
	}

//    @Override
//	public void init(IViewSite site, IMemento memento) throws PartInitException {
//    	super.init(site, memento);
//    }
 
    private void addSelectionListener(TreeViewer resultsViewer) {
		resultsViewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent evt) {
				try {
					(new OpenDataFileAction(true)).execute(null);
				} catch (ExecutionException e) {
					logger.error("Cannot open nexus file", e);
				}
			}			
		});
	}

	/** Adds any actions from the extensions tree to the right click menu.**/
	private void createRightClickMenu() {	
	    final MenuManager menuManager = new MenuManager();
	    
	    final Control control = ((Viewer)getSite().getSelectionProvider()).getControl();
	    control.setMenu (menuManager.createContextMenu(control));
		getSite().registerContextMenu(menuManager, getSite().getSelectionProvider());
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		if (getSite().getShell().isDisposed()) return;
		((TreeViewer)getSite().getSelectionProvider()).getControl().setFocus();
	}

	/**
	 * FIXME may need to deal with large file systems better.
	 * Refresh the view (reload files).
	 */
	public void refresh() {
		((TreeViewer)getSite().getSelectionProvider()).refresh();
	}

	/**
	 * 
	 */
	public void refreshFolder() {
		final TreeViewer viewer = (TreeViewer)getSite().getSelectionProvider();
		final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel == null) return;
		if (sel.getFirstElement()==null) return;
		if (sel.getFirstElement() instanceof IResource) {
			IResource file = (IResource)sel.getFirstElement();
			if (!(file instanceof IContainer)) file = file.getParent();
			try {
				file.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {
				logger.error("Cannot refresh "+file, e);
			}
			viewer.refresh(file, true);
		}
	}
	/**
	 * Call to set the root path of the data tree.
	 * @param path
	 */
	public void setSelectedPath(String path) {
		final TreeViewer            viewer = (TreeViewer)getSite().getSelectionProvider();
		try {
			setupProject(project, path);
			viewer.refresh();
		} catch(Exception ne) {
			logger.error("Cannot set project "+path, ne);
		}
			
	}
	
	public void setSelected(final ISelection i) {
		resultsViewer.setSelection(i);
	}

    /**
     * Used by tests to override the folder that the navigator will start with.
     * @param folder
     */
	public static void setDefaultDataFolder(final String folder) {
		DEFAULT_FILE_PATH = folder;
	}

	/**
	 * Call with string path fragment below the current selected folder.
	 * @param frag
	 * @throws Exception 
	 */
	public void setSelected(String frag) throws Exception {
		final IResource sel = getResource(frag);
		if (!sel.exists()) throw new Exception("Cannot find file "+sel);
		
		final TreeViewer viewer = (TreeViewer)getSite().getSelectionProvider();
		viewer.setSelection(new StructuredSelection(sel));
	}
	
	private ViewerFilter nexusFilter;
	private ViewerFilter datFilter;
	
	public void setNexusFilter(boolean filter) {
		
		if (nexusFilter==null)  nexusFilter = ViewerFilterFactory.createFileExtensionFilter(".nxs");
		if (filter) {
		    resultsViewer.addFilter(nexusFilter);
		} else {
			resultsViewer.removeFilter(nexusFilter);
		}
	}
	
	public void setAsciiFilter(boolean filter) {
		
		if (datFilter==null) datFilter = ViewerFilterFactory.createFileExtensionFilter(".dat");
		if (filter) {
		    resultsViewer.addFilter(datFilter);
		} else {
			resultsViewer.removeFilter(datFilter);
		}
	}

	public void resetFilters() {
		resultsViewer.resetFilters();
	}
	
	
	/**
	 * Gets the data project (made new) with this absolute path connected as the only
	 * sub-folder.
	 * 
	 * @param dataPath
	 * @throws CoreException 
	 */
	public void setupProject(final IProject data, String dataPath) throws Exception {
		
		if (dataPath==null) {
			if (data.exists()) {
				final IResource[] members = project.members();
				for (int i = 0; i < members.length; i++) {
					if (members[i] instanceof IContainer) dataPath = members[i].getLocation().toOSString();
				}
			} else {
				dataPath = PathUtils.createFromDefaultProperty();
			}
		}
		
		final Path   path = new Path(dataPath);
		if (data.exists()) {
			final IFolder src = data.getFolder(path.toFile().getName());
			final String  cp  = src.getLocation().toOSString();
			final String  pp  = path.toOSString();
			if (src.exists() && cp.equals(pp)) {
				resultsViewer.setInput(data);
				return;
			}
		}
		
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!data.exists()) {
					data.create(monitor);
				    data.open(monitor);
				}
				
				final IResource[] members = project.members();
				for (int i = 0; i < members.length; i++) {
					if (members[i] instanceof IContainer) members[i].delete(false, monitor);
				}
				 
				// Create links to subfolders of data folder so that we can ignore .workspace
				// in case it is a sub-folder of the data folder.
				final IFolder src = data.getFolder(path.lastSegment());
				final IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
				if( !path.isPrefixOf(workspacePath)){
					src.createLink(path, IResource.DEPTH_INFINITE, monitor);
				} else {
					src.create(false, true, monitor);
					// Deals with .workspace being under what is linked.
					IFileUtils.createLinks(src, path, monitor);
				}
				
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
				workspace.validateProjectLocation(project, project.getFullPath());
				
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						resultsViewer.setInput(data);
					}
				});
			}
		};
		workspace.run(runnable,  workspace.getRuleFactory().modifyRule(workspace.getRoot()), IResource.NONE, null);
	}

	
	private IResource getResource(String frag) {
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject       data = root.getProject(DATA_PROJECT_NAME);
		if (data == null || !data.exists()) return null;
		final Path path = new Path(frag);
		IResource member = data.findMember(path);
		return member;
	}

	public static String getDefaultFilePath() {
		return DEFAULT_FILE_PATH;
	}
}
