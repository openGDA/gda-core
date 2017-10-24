/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.components;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.richbeans.widgets.cell.TreeTextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ExperimentObjectEvent;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObjectManager;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.util.io.FileUtils;

/**
 * View which shows a tree of the experiments in the project.
 * <p>
 * Selection on parts of the tree changes which editors are opened.
 */
public class ExperimentExperimentView extends ViewPart implements ExperimentObjectListener {

	public static final String ADD_RUN_COMMAND = "uk.ac.gda.client.experimentdefinition.AddRunCommand";

	public static final String ADD_SCAN_COMMAND = "uk.ac.gda.client.experimentdefinition.AddScanCommand";

	private static final Logger logger = LoggerFactory.getLogger(ExperimentExperimentView.class);

	public static final String ID = "uk.ac.diamond.gda.client.experimentdefinition.ExperimentView";

	private TreeViewer treeViewer;
	private boolean lastClickWasRHButton;

	@Override
	public void createPartControl(Composite parent) {

		this.treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		final ExperimentContentProvider model = new ExperimentContentProvider(this);
		treeViewer.setContentProvider(model);
		treeViewer.setLabelProvider(new ExperimentLabelProvider(model));
		treeViewer.setInput(model.getRoot());
		treeViewer.expandToLevel(3);

		createDropSupport();

		treeViewer.setCellEditors(new CellEditor[] { new TreeTextCellEditor(treeViewer.getTree(), SWT.BORDER) });
		treeViewer.setColumnProperties(new String[] { "folder", "scan file", "run name" }); // Not used but have to set
		// for editor.
		treeViewer.setCellModifier(new ExperimentRunModifier(this));

		initializeContextMenu(treeViewer);

		addSelectionListener(treeViewer);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(treeViewer.getControl(), "exafs.runs.viewer");

		ExperimentProjectNature nature = new ExperimentProjectNature();
		try {
			String projectName = ExperimentFactory.getExperimentProjectName();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			nature.setProject(project);
			nature.configure();

			// refresh the tree to show the experiments.
			this.refreshTree();

			updateSelectedFromController();

		} catch (CoreException e) {
			logger.error("Exception while starting ExperimentView",e);
		}


	}

	@Override
	public void dispose() {
		if (selectionChangedListener != null) {
			this.treeViewer.removeSelectionChangedListener(selectionChangedListener);
		}
		ExperimentFactory.removeRunObjectListener(this);
		if (getSite().getSelectionProvider() != null) {
			((Viewer) getSite().getSelectionProvider()).getControl().dispose();
		}
		super.dispose();
	}

	@Override
	public void runChangePerformed(final ExperimentObjectEvent e) {
		// Could check all properties to only update if prop interested in has changed.
		if (treeViewer != null && !treeViewer.getControl().isDisposed()) {
			try {
				getSite().getWorkbenchWindow().getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						treeViewer.refresh(e.getRunObject());
						treeViewer.refresh(ExperimentFactory.getManager(e.getRunObject()));
					}
				});
			} catch (Exception e1) {
				logger.warn("Exception while refreshing", e1);
			}
		}
	}

	private void createDropSupport() {

		Transfer[] types = new Transfer[] { ExperimentObjectTransfer.getInstance() };
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;

		final DragSource source = new DragSource(treeViewer.getTree(), operations);
		source.setTransfer(types);

		source.addDragListener(new DragSourceListener() {
			TreeItem dragSourceItem; // can be a ScanDataObject

			@Override
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = treeViewer.getTree().getSelection();
				if (selection.length > 0 && selection[0].getItemCount() == 0) {
					event.doit = true;
					dragSourceItem = selection[0];
				} else {
					event.doit = false;
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				if (dragSourceItem.getData() instanceof IExperimentObject) {
					event.data = dragSourceItem.getData();
					event.doit = true;
				} else {
					event.doit = false;
				}
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					dragSourceItem.dispose();
					dragSourceItem = null;
				}
				IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();

				if (man != null) {
					ExperimentFolderEditor ed = man.getActiveFolderEditor();
					man.refreshViewers();
					if (ed != null) {
						ed.refresh();
					}
				}
			}
		});

		DropTarget target = new DropTarget(treeViewer.getTree(), operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					// logger.info(event.item.toString());
					TreeItem item = (TreeItem) event.item;
					Point pt = getSite().getShell().getDisplay().map(null, treeViewer.getTree(), event.x, event.y);
					Rectangle bounds = item.getBounds();
					if (pt.y < bounds.y + bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_BEFORE;
					} else if (pt.y > bounds.y + 2 * bounds.height / 3) {
						event.feedback |= DND.FEEDBACK_INSERT_AFTER;
					} else {
						event.feedback |= DND.FEEDBACK_SELECT;
					}
				}
			}

			@Override
			public void drop(DropTargetEvent event) {

				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				TreeItem item = (TreeItem) event.item;
				TreeItem parent = item.getParentItem();

				// don't add to top level items i.e. empty folders without multiscans
				if (parent == null) {
					event.detail = DND.DROP_NONE;
					return;
				}

				// get information about where the drop occurred
				IExperimentObject scanToBeMoved = (IExperimentObject) event.data;
				Point pt = getSite().getShell().getDisplay().map(null, treeViewer.getTree(), event.x, event.y);
				Rectangle bounds = item.getBounds();
				Object itemData = item.getData();
				TreeItem[] items = parent.getItems();
				int index = getItemIndex(item, items);

				try {
					// identify the IExperimentObject next to which the scanToBeMoved is to be dropped
					IExperimentObject targetMultiScan = null;
					if (itemData instanceof IExperimentObject){
						targetMultiScan = (IExperimentObject) itemData;
					} else if (itemData instanceof IExperimentObject){
						targetMultiScan = ((IExperimentObjectManager) itemData).getExperimentList().get(0);
					} else if (itemData instanceof IFolder){
						targetMultiScan = ExperimentFactory.getRunManagers((IFolder) itemData).get(0).getExperimentList().get(0);
					}

					moveExperiment(scanToBeMoved, pt, bounds, targetMultiScan, items, index);
				} catch (Exception e) {
					logger.warn("Exception while trying to move IExperimentObject", e);
				} finally {
					ExperimentFactory.getExperimentEditorManager().notifySelectionListeners();
				}
			}

			protected void moveExperiment(IExperimentObject scanToBeMoved, Point pt, Rectangle bounds,
					IExperimentObject targetLocObj, TreeItem[] items, int index) throws Exception {
				if (ExperimentFactory.getManager(scanToBeMoved).equals(ExperimentFactory.getManager(targetLocObj))) {
					placeExperimentWithinMultiscan(scanToBeMoved, pt, bounds, items, index, targetLocObj);
				} else {
					final IExperimentObject copy = ExperimentFactory.getManager(scanToBeMoved).cloneExperiment(scanToBeMoved);
					copyFilesToDifferentMultiscan(scanToBeMoved, ExperimentFactory.getManager(targetLocObj), copy);
					placeExperimentWithinMultiscan(copy, pt, bounds, items, index, targetLocObj);
					ExperimentFactory.getManager(scanToBeMoved).removeExperiment(scanToBeMoved);
				}
			}

			protected void copyFilesToDifferentMultiscan(IExperimentObject scanToBeMoved, IExperimentObjectManager man,
					final IExperimentObject copy) throws CoreException {
				Map<String, IFile> files = copy.getFilesWithTypes();
				Map<String, IFile> targetFiles = new HashMap<String, IFile>(files.size());
				for (String fileType : files.keySet()) {
					IFile file = files.get(fileType);
					IFile targetFile = EclipseUtils.getUniqueFile(man.getContainingFolder(), file.getName(), "xml");
					file.copy(targetFile.getFullPath(), true, null);
					targetFiles.put(fileType, targetFile);
				}
				copy.setFiles(targetFiles);

				// rename the copy
				copy.setRunName(scanToBeMoved.getRunName());
				copy.setMultiScanName(man.getName());
				copy.setFolder(man.getContainingFolder());
			}

			protected void placeExperimentWithinMultiscan(IExperimentObject scanToBeMoved, Point pt, Rectangle bounds,
					TreeItem[] items, int index, IExperimentObject targetLocObj) throws Exception {
				// before index
				if (pt.y < bounds.y + bounds.height / 3) {
					if (index == 0) {
						ExperimentFactory.getManager(scanToBeMoved).insertExperimentAfter(null, scanToBeMoved);
					} else {
						IExperimentObject theOneBefore = (IExperimentObject) items[index - 1].getData();
						ExperimentFactory.getManager(scanToBeMoved).insertExperimentAfter(theOneBefore, scanToBeMoved);
					}
				}
				// in index (treat as after)
				else if (pt.y > bounds.y + 2 * bounds.height / 3) {
					ExperimentFactory.getManager(scanToBeMoved).insertExperimentAfter(targetLocObj, scanToBeMoved);
				}
				// after index
				else {
					ExperimentFactory.getManager(scanToBeMoved).insertExperimentAfter(targetLocObj, scanToBeMoved);
				}
			}

			protected int getItemIndex(TreeItem item, TreeItem[] items) {
				int index = 0;
				for (int i = 0; i < items.length; i++) {
					if (items[i] == item) {
						index = i;
						break;
					}
				}
				return index;
			}
		});

	}

	protected void copy(IFolder targetFolder, java.io.File from) throws Exception {
		final IFile to = targetFolder.getFile(from.getName());
		if (to.exists()) {
			final boolean overwrite = MessageDialog.openQuestion(getSite().getShell(), "Confirm Overwrite",
					"The file '" + from.getName() + "' exists in '" + targetFolder.getName() + "'\n\n"
							+ "Would you like to overwrite this file?");
			if (!overwrite)
				return;
			to.delete(true, null);
		}
		FileUtils.copy(from, to.getLocation().toFile());
		to.refreshLocal(IResource.DEPTH_ZERO, null);
	}

	@Override
	public void setFocus() {
		if (!treeViewer.getTree().isDisposed())
			treeViewer.getTree().setFocus();
	}

	private void initializeContextMenu(final TreeViewer treeViewer) {

		final MenuManager menuManager = new MenuManager();
		final Menu menu = menuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, treeViewer);
	}

	private boolean off = false;
	private ISelectionChangedListener selectionChangedListener;

	private MouseListener selectionMouseListener;

	/**
	 * Rather complex method. Need to extract this functionality to a generic common class to make making views for
	 * controlling editors easy.
	 *
	 * @param treeViewer
	 */
	private void addSelectionListener(final TreeViewer treeViewer) {
		this.selectionChangedListener = new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				if (off)
					return;
				// if the selection is empty clear the label
				if (event.getSelection().isEmpty()) {
					return;
				}

				updateSelected(event.getSelection(),!lastClickWasRHButton);
			}

		};
		treeViewer.addSelectionChangedListener(selectionChangedListener);

		this.selectionMouseListener = new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3){
					lastClickWasRHButton = true;
				} else {
					lastClickWasRHButton = false;
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		};
		treeViewer.getTree().addMouseListener(this.selectionMouseListener);
	}

	private void updateSelected(ISelection sel, boolean openEditors) {
		try {
			off = true;
			if (sel instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) sel;
				final Object element = selection.getFirstElement();
				ExperimentFactory.getExperimentEditorManager().setSelected(element);

				if (openEditors) {
					try {
						if (element instanceof IExperimentObject) {
							final IExperimentObject ob = (IExperimentObject) element;
							ExperimentFactory.getExperimentEditorManager().openDefaultEditors(ob, true);

						} else if (element instanceof IFolder) {
							ExperimentFactory.getExperimentEditorManager().openEditor((IFolder) element,
									ExperimentFolderEditor.ID, true);

						} else if (element instanceof IExperimentObjectManager) {
							ExperimentFactory.getExperimentEditorManager().openEditor(
									((IExperimentObjectManager) element).getFile(), ExperimentRunEditor.ID, true);
						}
					} finally {
						ExperimentFactory.getExperimentEditorManager().notifySelectionListeners();
					}
				}
			}
		} finally {
			off = false;
		}
	}

	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		return window.getActivePage();
	}

	public ExperimentLabelProvider getLabelProvider() {
		return (ExperimentLabelProvider) treeViewer.getLabelProvider();
	}

	public void refreshTree() {
		treeViewer.refresh();
		treeViewer.expandToLevel(3);
	}

	private void updateSelectedFromController() {
		// Set selected, no update
		try {
			off = true;
			setSelected(ExperimentFactory.getExperimentEditorManager().getSelected());
		} finally {
			off = false;
		}
	}

	public void setSelected(final Object element) {

		if (element == null) {
			treeViewer.setSelection(null);
			return;
		}
		if (!checkObject(element))
			throw new RuntimeException("Cannot select objects of type: " + element);

		final TreeSelection treeSelection = new TreeSelection(createTreePath(element));
		treeViewer.setSelection(treeSelection, true);
	}

	private boolean checkObject(Object element) {
		if (element instanceof IFolder)
			return true;
		if (element instanceof IExperimentObjectManager)
			return true;
		if (element instanceof IExperimentObject)
			return true;
		return false;
	}

	private TreePath createTreePath(Object element) {
		if (element instanceof IFolder) {
			return new TreePath(new Object[] { element });

		} else if (element instanceof IExperimentObjectManager) {

			final IExperimentObjectManager fact = (IExperimentObjectManager) element;
			return new TreePath(new Object[] { fact.getContainingFolder(), element });

		} else if (element instanceof IExperimentObject) {
			final IExperimentObject ob = (IExperimentObject) element;
			return new TreePath(new Object[] { ob.getFolder(), ExperimentFactory.getManager(ob),
					element });
		}
		return null;
	}

	public void editElement(Object selected) {
		final ExperimentRunModifier mod = (ExperimentRunModifier) treeViewer.getCellModifier();
		mod.setEnabled(true);
		treeViewer.editElement(selected, 0);
	}

	public boolean isFocus() {
		return treeViewer.getTree().isFocusControl();
	}

	public IExperimentObject getSelectedScan() {
		final IStructuredSelection treeSelection = (IStructuredSelection) treeViewer.getSelection();
		final Object element = treeSelection.getFirstElement();

		if (element instanceof IExperimentObject) {
			return (IExperimentObject) element;
		} else if (element instanceof IFolder) {
			return ExperimentFactory.getExperimentEditorManager().getSelectedMultiScan().getExperimentList().get(0);
		} else if (element instanceof IExperimentObjectManager) {
			IExperimentObjectManager man = (IExperimentObjectManager) element;
			return man.getExperimentList().get(0);
		}
		return null;
	}

	public void collapseAllTree() {
		treeViewer.collapseAll();
	}

	public void expandAllTree() {
		treeViewer.expandAll();
	}

}
