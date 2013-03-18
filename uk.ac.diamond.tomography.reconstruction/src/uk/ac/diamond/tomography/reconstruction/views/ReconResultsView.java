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

package uk.ac.diamond.tomography.reconstruction.views;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail;

public class ReconResultsView extends ViewPart {

	public static final String ID = "uk.ac.diamond.tomography.reconstruction.results";

	private static final Logger logger = LoggerFactory.getLogger(ReconResultsView.class);

	public ReconResultsView() {
	}

	public static class ResultsTableContentProvider implements IStructuredContentProvider {

		private Viewer viewer;

		private Resource currentResource;

		private EContentAdapter notificationListener = new EContentAdapter() {
			@Override
			public void notifyChanged(org.eclipse.emf.common.notify.Notification notification) {
				int eventType = notification.getEventType();

				if (Notification.SET == eventType || Notification.ADD == eventType) {
					if (notification.getFeature() != null && !notification.getFeature().equals("null")) {
						viewer.refresh();
					}
				}
			}
		};

		@Override
		public void dispose() {
			if (currentResource != null) {
				currentResource.eAdapters().add(notificationListener);
			}
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.viewer = viewer;
			if (currentResource != null) {
				currentResource.eAdapters().remove(notificationListener);
			}
			if (newInput instanceof Resource) {
				Resource res = (Resource) newInput;
				currentResource = res;
				res.eAdapters().add(notificationListener);
				notificationListener.setTarget(currentResource);
			}

		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Resource) {
				EObject rootObject = ((Resource) inputElement).getContents().get(0);
				if (rootObject instanceof uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults) {
					uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults reconResults = (uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults) rootObject;
					return reconResults.getReconresult().toArray();
				}
			}
			return null;
		}

	}

	public static class ResultsTableLabelProvider extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {

			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ReconstructionDetail) {
				ReconstructionDetail detail = (ReconstructionDetail) element;

				if (columnIndex == 0) {
					return detail.getNexusFileName();
				} else if (columnIndex == 1) {
					return detail.getReconstructedLocation();
				} else if (columnIndex == 2) {
					return detail.getTimeReconStarted();
				}
			}

			return null;
		}

	}

	public void createColumns(TableViewer tableViewer, TableColumnLayout layout) {
		for (int i = 0; i < columnHeaders.length; i++) {
			TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
			tableViewerColumn.getColumn().setResizable(columnLayouts[i].resizable);
			tableViewerColumn.getColumn().setText(columnHeaders[i]);
			tableViewerColumn.getColumn().setToolTipText(columnHeaders[i]);
			layout.setColumnData(tableViewerColumn.getColumn(), columnLayouts[i]);
		}
	}

	private final String columnHeaders[] = { "FileName", "Reconstruction Output", "Time started at" };
	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(30, true), new ColumnWeightData(50, true),
			new ColumnWeightData(20, true) };

	@Override
	public void createPartControl(Composite parent) {
		Composite root = new Composite(parent, SWT.None);

		TableViewer resultsTableViewer = new TableViewer(root);
		resultsTableViewer.getTable().setHeaderVisible(true);
		resultsTableViewer.getTable().setLinesVisible(true);

		TableColumnLayout tableLayout = new TableColumnLayout();
		root.setLayout(tableLayout);

		createColumns(resultsTableViewer, tableLayout);

		resultsTableViewer.setContentProvider(new ResultsTableContentProvider());
		resultsTableViewer.setLabelProvider(new ResultsTableLabelProvider());

		resultsTableViewer.setInput(Activator.getDefault().getReconResultsResource());
	}

	@Override
	public void setFocus() {

	}

}
