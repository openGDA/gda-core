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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import uk.ac.gda.client.experimentdefinition.IExperimentBeanDescription;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.components.cell.IXMLFileListProvider;
import uk.ac.gda.ui.dialog.OKCancelDialog;

/**
 * NOTE: The paradigm of &lt;New... &gt; in the file list is a confusing and bad one and not commonly used in user
 * interfaces.
 * <p>
 * This chooser could do with quite a bit more work to make it obvious to the user how to change scan type.
 * <p>
 * Scan type can also be changed in the multi-scan perspective in the run editor. This has a drop down of available scan
 * files and a right click menu able to create new ones.
 * <p>
 * See GDA-3433
 */
public final class XMLFileDialog extends OKCancelDialog {

	private Map<String, IExperimentBeanDescription> ACTIONS;

	private Table table;
	private IFolder currentDirectory;
	private TableViewer fileViewer;
	private Collection<IExperimentBeanDescription> exafsBeanDescriptions;

	private String title;
	private String info;

	/**
	 * @param parent
	 *            shell to create dialog in
	 * @param exafsBeanDescriptions
	 *            list of descriptions to use to derive "<New ...>" entries, or <code>null</code> for no "New"
	 */
	public XMLFileDialog(Shell parent, Collection<IExperimentBeanDescription> exafsBeanDescriptions, String title,
			String info) {
		super(parent, SWT.OPEN);

		ACTIONS = new LinkedHashMap<String, IExperimentBeanDescription>();
		if (exafsBeanDescriptions != null) {
			for (IExperimentBeanDescription desc : exafsBeanDescriptions) {
				ACTIONS.put("<New " + desc.getName() + ">", desc);
			}
		}
		this.exafsBeanDescriptions = exafsBeanDescriptions;
		this.title = title;
		this.info = info;
	}

	/**
	 * @param shell
	 * @param userObject
	 */
	@Override
	public void createUserUI(final Shell shell, final Object userObject) {

		shell.setText(title);

		// Your code goes here (widget creation, set result, etc).
		this.fileViewer = new TableViewer(shell, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		table = fileViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		currentDirectory = (IFolder) userObject;

		createContentProvider();
		ExperimentProviderUtils.createExafsLabelProvider(fileViewer);
		fileViewer.setInput(new Object());
		fileViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				XMLFileDialog.this.ok = true;
				shell.dispose();
			}
		});

		fileViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final StructuredSelection sel = (StructuredSelection) fileViewer.getSelection();
				currentSelection = sel.getFirstElement();

			}
		});

		final Label infoLabel = new Label(shell, SWT.WRAP);
		infoLabel.setText(info);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, -1, 12));
	}

	/**
	 * This specialisation requires userData to be an IFolder, or adaptable to one.
	 */
	@Override
	public IFile open(final Object userData) {
		IFolder folder = (IFolder) EclipseUtils.getAdapter(userData, IFolder.class);
		if (folder != null) {
			return open(folder);
		}
		throw new IllegalArgumentException(userData.toString() + " is not adaptable to IFolder");
	}

	public IFile open(final IFolder userData) {
		Object iFileOrString = super.open(userData);
		IFile file;

		if (iFileOrString instanceof String) {
			final String fileType = (String) iFileOrString;
			file = ACTIONS.get(fileType).getXmlCommandHander().doCopy(currentDirectory);
		} else {
			file = (IFile) iFileOrString;
		}
		return file;
	}

	private void createContentProvider() {
		fileViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				List<Object> objects = new ArrayList<Object>();
				if (exafsBeanDescriptions != null) {
					for (IXMLFileListProvider fileListProvider : exafsBeanDescriptions) {
						objects.addAll(fileListProvider.getSortedFileList(currentDirectory));
					}
				}
				objects.addAll(ACTIONS.keySet());
				return objects.toArray();
			}
		});
	}
}
