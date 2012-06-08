/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ExperimentObjectEvent;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.common.rcp.util.ISortingUtils;
import uk.ac.gda.richbeans.xml.XMLBeanContentDescriberFactory;

/**
 * Will make this more of an editor later, allowing new XML and deletion / backup.
 * 
 * @author fcp94556
 *
 */
public class ExperimentFolderEditor extends EditorPart implements ExperimentObjectListener {

	private final static Logger logger = LoggerFactory.getLogger(ExperimentFolderEditor.class);

	static {
		XMLBeanContentDescriberFactory.getInstance().addFileExtension(".scan", ExperimentRunEditor.ID);
	}
	
	/**
	 * 
	 */
	public static final String ID = "uk.ac.gda.client.experimentdefinition.FolderEditor"; //$NON-NLS-1$
	
	private TableViewer fileViewer;
	private IFolder     currentDirectory;

	/**
	 * Create contents of the editor part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new FillLayout());
		scrolledComposite.setContent(container);
		
		this.fileViewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
        createContentProvider();
        ExperimentProviderUtils.createExafsLabelProvider(fileViewer);
		fileViewer.setInput(new Object());

		createDragSupport();
        createRightClickMenu();
        createDoubleClickListener();
        createEditor();

		scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		getSite().setSelectionProvider(fileViewer);
	}
	
	
	@Override
	public void dispose() {
		super.dispose();
		if (this.doubleClickListener!=null) {
			fileViewer.removeDoubleClickListener(doubleClickListener);
		}
		try {
			ExperimentFactory.removeRunObjectListener(currentDirectory, this);
		} catch (Exception e) {
			logger.error("Cannot remove run object listeners to folder '"+currentDirectory+"'.", e);
		}
		if (getSite().getSelectionProvider()!=null) {
			((Viewer)getSite().getSelectionProvider()).getControl().dispose();
		}
	}

	
	private boolean canModify = false;

	private IDoubleClickListener doubleClickListener;
	private void createEditor() {
		CellEditor[] editors  = new CellEditor[1];
		final TextCellEditor nameEd = new TextCellEditor(fileViewer.getTable());
		((Text)nameEd.getControl()).setTextLimit(60);
		editors[0] = nameEd;	
		fileViewer.setCellEditors(editors);
		
		fileViewer.setColumnProperties(new String[]{"File Name"});
		fileViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(Object element, String property) {
				return canModify;
			}
			@Override
			public Object getValue(Object element, String property) {
				canModify = false;
				return ((IFile)element).getName();
			}
			@Override
			public void modify(Object item, String property, Object value) {
				try {
					final TableItem tableItem = (TableItem)item;
					final Object    element   = tableItem.getData();
					final IFile     orig      = (IFile)element;

					if (value==null||"".equals(value)||!value.toString().matches("\\w+\\.\\w+")) return;
					if (value.equals(orig.getName())) return;
					
					try {
						ExperimentFactory.refactorFile(orig, value.toString());
						tableItem.setData(orig);
						fileViewer.refresh();
					} catch (Exception e) {
						final Status status = new Status(IStatus.ERROR, ID, e.getMessage(), e);
						ErrorDialog.openError(getSite().getShell(), "Cannot Rename File", "Cannot rename file '"+orig.getName()+"'.", status);
					}
				} finally {
					canModify = false;
				}
			}
			
		});
	}

	private void createDoubleClickListener() {
		this.doubleClickListener = new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				final IFile file = (IFile)((IStructuredSelection)fileViewer.getSelection()).getFirstElement();
				ExperimentFactory.getExperimentEditorManager().openEditor(file);
			}		
		};
		fileViewer.addDoubleClickListener(doubleClickListener);
	}

	private void createDragSupport() {
		// We allow people to copy files from the run folder into another folder.
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance()};
		fileViewer.addDragSupport(ops, transfers, new DragSourceListener() {
			@Override
			public void dragStart(DragSourceEvent event) {
				// Do nothing
			}
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.doit     = false;
				if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
					final List<IFile> sel = getSelected();
					final String[] files = new String[sel.size()];
					for (int i=0;i<sel.size(); ++i) files[i] = sel.get(i).getLocation().toString();
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

	private void createContentProvider() {
		fileViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				
				if (!currentDirectory.exists()) return new Object[]{new Object()};
				
			    Comparator<IResource> comp =  new Comparator<IResource>() {
					@Override
					public int compare(IResource o1, IResource o2) {
						boolean scan1 = o1.getName().endsWith(".scan");
						boolean scan2 = o2.getName().endsWith(".scan");
						if (scan1&&!scan2) return -1;
						if (!scan1&&scan2) return  1;							
						
						boolean xml1 = o1.getName().endsWith(".xml");
						boolean xml2 = o2.getName().endsWith(".xml");
						if (xml1&&!xml2) return -1;
						if (!xml1&&xml2) return  1;							
						
						return o1.getLocation().toString().compareTo(o2.getLocation().toString());
					}			    	
			    };
			    
				try {
					List<IResource> files = ISortingUtils.getSortedFileList(currentDirectory, comp);
				    if (files==null) return new Object[]{new Object()};
				    return files.toArray();
				} catch (CoreException e) {
					logger.error("Cannot get file model for ExperimentFolderEditor", e);
					return new Object[]{new Object()};
				}
			}
			
		});
	}

	/**
	 * Returns the selected files in the editor.
	 * @return files selected
	 */
	@SuppressWarnings("unchecked")
	public List<IFile> getSelected() {
		final IStructuredSelection sel = (IStructuredSelection)fileViewer.getSelection();
		return sel.toList();
	}


	private void createRightClickMenu() {	
	    final MenuManager menuManager = new MenuManager();
		final Menu        menu        = menuManager.createContextMenu(fileViewer.getControl());
		fileViewer.getControl().setMenu (menu);
		getSite().registerContextMenu(menuManager, fileViewer);
	}

	@Override
	public void setFocus() {
		fileViewer.getTable().setFocus();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// Do the Save operation
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}
	
	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		
		final String name            = EclipseUtils.getFile(getEditorInput()).getName();
	    this.currentDirectory        = ExperimentFactory.getExperimentEditorManager().getIFolder(name);
        setPartName(currentDirectory.getName());
	    try {
	    	ExperimentFactory.addRunObjectListener(currentDirectory, this);
		} catch (Exception e) {
			logger.error("Cannot add run object listeners to folder '"+currentDirectory+"'.", e);
		}

	}
	
	@Override
	public void runChangePerformed(ExperimentObjectEvent e) {
		if (fileViewer!=null&&!fileViewer.getControl().isDisposed()) {
	        fileViewer.refresh();
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Refresh the file list.
	 */
	public void refresh() {
		fileViewer.refresh();
	}

	/**
	 * @param b
	 */
	private void setCanModify(boolean b) {
		canModify = b;
	}

	/**
	 * 
	 */
	public void editSelectedElement() {
		setCanModify(true);
		final List<IFile> files = getSelected();
		if (files!=null&&files.size()>0) fileViewer.editElement(files.get(0), 0);
	}

	/**
	 * @return Returns the currentDirectory.
	 */
	public IFolder getCurrentDirectory() {
		return currentDirectory;
	}

	/**
	 * @param to
	 */
	public void setSelected(IFile to) {
		fileViewer.setSelection(new StructuredSelection(to));
	}

	/**
	 * Returns true if has focus.
	 * @return boolean
	 */
	public boolean isFocus() {
		return fileViewer.getTable().isFocusControl();
	}

}

	