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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import uk.ac.gda.common.rcp.CommonRCPActivator;

public class ExperimentProviderUtils {

	private static final class LabelProvider extends ColumnLabelProvider {
		private WorkbenchLabelProvider workbenchLabelProvider;

		public LabelProvider() {
			workbenchLabelProvider = new WorkbenchLabelProvider();
		}

		@Override
		public Image getImage(Object element) {
			if (!(element instanceof IFile))
				return null;
			return workbenchLabelProvider.getImage(element);
		}

		@Override
		public String getText(Object element) {
			if (element instanceof IResource) {
				return workbenchLabelProvider.getText(element);
			}
			return element.toString();
		}

		@Override
		public Point getToolTipShift(Object object) {
			return new Point(5, 5);
		}

		@Override
		public int getToolTipDisplayDelayTime(Object object) {
			return 50;
		}

		@Override
		public int getToolTipTimeDisplayed(Object object) {
			return 10000;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
			super.addListener(listener);
			workbenchLabelProvider.addListener(listener);
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			super.removeListener(listener);
			workbenchLabelProvider.removeListener(listener);
		}
	}

	/**
	 * Creates a label provider on the file viewer for showing any exafs file.
	 * 
	 * @param fileViewer
	 */
	public static void createExafsLabelProvider(final TableViewer fileViewer) {

		ColumnViewerToolTipSupport.enableFor(fileViewer, ToolTip.NO_RECREATE);

		final TableViewerColumn name = new TableViewerColumn(fileViewer, SWT.NONE);
		name.getColumn().setText("File");
		name.getColumn().setWidth(500);

		fileViewer.setLabelProvider(new DecoratingLabelProvider(new LabelProvider(), CommonRCPActivator.getDefault()
				.getWorkbench().getDecoratorManager().getLabelDecorator()));
	}

}
