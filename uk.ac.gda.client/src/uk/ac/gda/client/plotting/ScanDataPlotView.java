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

package uk.ac.gda.client.plotting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.plotting.model.DataNode;
import uk.ac.gda.client.plotting.model.RootDataNode;

public class ScanDataPlotView extends ViewPart {
	public static String ID = "uk.ac.gda.client.plotting.scandataplotview";

	private static final Logger logger = LoggerFactory.getLogger(ScanDataPlotView.class);

	private static final boolean SHOW_VIEW_ON_DATA_RECEIVE = true;
	private boolean thisViewIsVisible = false;

	ScanDataPlotterComposite scanDataPlotter;
	private RootDataNode rootNode;

	@Override
	public void createPartControl(Composite parent) {
		rootNode = new RootDataNode();
		scanDataPlotter = new ScanDataPlotterComposite(parent, SWT.None, this, rootNode);

		final IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (page == null) {
			return;
		}
		page.addPartListener(new IPartListener2() {
			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(ID)) {
					thisViewIsVisible = true;
				}
			}
			@Override
			public void partOpened(IWorkbenchPartReference partRef) {}
			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {}
			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(ID)) {
					thisViewIsVisible = false;
				}
			}
			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {}
			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {}
			@Override
			public void partActivated(IWorkbenchPartReference partRef) {}
		});

		rootNode.addPropertyChangeListener(DataNode.DATA_ADDED_PROP_NAME, new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent evt) {
				if (SHOW_VIEW_ON_DATA_RECEIVE & !thisViewIsVisible) {
					try {
						page.showView(ID);
					} catch (PartInitException e) {
						logger.error("Unable to show plotter view when receiving data", e);
					}
				}
			}
		});
	}


	@Override
	public void setFocus() {
		if (!scanDataPlotter.isDisposed()) {
			scanDataPlotter.setFocus();
		}
	}

	@Override
	public void dispose() {
		if (rootNode != null) {
			rootNode.disposeResources();
		}
	}
}
