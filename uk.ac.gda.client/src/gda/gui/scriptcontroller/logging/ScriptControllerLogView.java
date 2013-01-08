/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.gui.scriptcontroller.logging;

import gda.rcp.GDAClientActivator;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.preferences.PreferenceConstants;

public class ScriptControllerLogView extends ViewPart implements SelectionListener {

	public static final String ID = "gda.gui.scriptcontroller.logging.scriptcontrollerlogview";
	private TreeViewer treeViewer;
	private ScriptControllerLogLabelProvider labelProvider;
	private ScriptControllerLogContentProvider contentProvider;
	private Combo cmbFilter;
	private String[] scriptTypes = new String[] { ScriptControllerLogFilter.ALL };
	private ScriptControllerLogFilter filter;

	@Override
	public void createPartControl(Composite parent) {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(parent);

		filter = new ScriptControllerLogFilter();

		cmbFilter = new Combo(parent, SWT.NONE);
		cmbFilter.setItems(scriptTypes);
		cmbFilter.select(0);
		cmbFilter.addSelectionListener(this);
		
		

		treeViewer = new TreeViewer(parent);
		treeViewer.addFilter(filter);
		treeViewer.setComparer(new ScriptControllerLogComparer());

		String controllers = GDAClientActivator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.GDA_LOGGINGSCRIPTCONTROLLERS);
		contentProvider = new ScriptControllerLogContentProvider(this, controllers);
		treeViewer.setContentProvider(contentProvider);

		labelProvider = new ScriptControllerLogLabelProvider(contentProvider);
		treeViewer.setLabelProvider(labelProvider);

		GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(treeViewer.getControl());

		treeViewer.setInput(contentProvider.getElements(null));
	}

	@Override
	public void setFocus() {
		//
	}

	/**
	 * handle for a refresh button
	 */
	public void refresh() {
		contentProvider.refresh();
		treeViewer.setInput(contentProvider.getElements(null));
		treeViewer.refresh(true);
//		treeViewer.collapseAll();
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		widgetSelected(arg0);
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				String currentChoice = cmbFilter.getItem(cmbFilter.getSelectionIndex());
				filter.setSelectedType(currentChoice);
				treeViewer.refresh(false);
			}
		});
	}

	protected void updateFilter(String[] knownScriptTypes) {
		String[] newScriptTypes = new String[] { ScriptControllerLogFilter.ALL };
		scriptTypes = (String[]) ArrayUtils.addAll(newScriptTypes, knownScriptTypes);

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				String currentChoice = cmbFilter.getItem(cmbFilter.getSelectionIndex());

				int newIndexOfCurrentChoice = ArrayUtils.indexOf(scriptTypes, currentChoice);
				if (newIndexOfCurrentChoice == -1) {
					newIndexOfCurrentChoice = 0;
				}

				cmbFilter.setItems(scriptTypes);
				cmbFilter.select(newIndexOfCurrentChoice);
			}
		});
	}
}
