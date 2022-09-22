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

package uk.ac.diamond.tomography.reconstruction.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.diamond.tomography.reconstruction.views.CENTRE_OF_ROTATION_MODE;

public class TomoReconPrefPage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String HEADER_STEP_SIZE = "Step size";
	private static final String HEADER_TOTAL_STEPS = "Total steps";
	private static final String HEADER_BUTTON_NAME = "Button Name";

	public static final String ID = "uk.ac.diamond.tomography.reconstruction.prefpage";
	private PreferenceContent[] contents;
	private TableViewer tv;

	public abstract class BaseTomoReconEditingSupport extends EditingSupport {

		public BaseTomoReconEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(((TableViewer) getViewer()).getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
	}

	public class TotalStepsEditingSupport extends BaseTomoReconEditingSupport {

		public TotalStepsEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof PreferenceContent) {
				PreferenceContent pc = (PreferenceContent) element;
				return Integer.toString(pc.getTotSteps());
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof PreferenceContent) {
				PreferenceContent pc = (PreferenceContent) element;
				pc.setTotSteps(Integer.parseInt((String) value));
				getViewer().refresh();
			}
		}
	}

	public class StepSizeEditingSupport extends BaseTomoReconEditingSupport {

		public StepSizeEditingSupport(TableViewer viewer) {
			super(viewer);
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof PreferenceContent) {
				PreferenceContent pc = (PreferenceContent) element;
				return Double.toString(pc.getStepSize());
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof PreferenceContent) {
				PreferenceContent pc = (PreferenceContent) element;
				pc.setStepSize(Double.parseDouble((String) value));
				getViewer().refresh();
			}
		}
	}

	public TomoReconPrefPage() {
	}

	public TomoReconPrefPage(String title) {
		super(title);
	}

	public TomoReconPrefPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	private static class PreferenceContent {
		private int totSteps;

		private double stepSize;

		private final CENTRE_OF_ROTATION_MODE mode;

		public PreferenceContent(CENTRE_OF_ROTATION_MODE mode) {
			this.mode = mode;
			initValues();
		}

		protected void initValues() {
			totSteps = mode.getTotalSteps();
			stepSize = mode.getStepSize();
		}

		public double getStepSize() {
			return stepSize;
		}

		public void setTotSteps(int totSteps) {
			this.totSteps = totSteps;

		}

		public void setStepSize(double stepSize) {
			this.stepSize = stepSize;
		}

		public void saveContent() {
			mode.saveContent(totSteps, stepSize);

		}

		public int getTotSteps() {
			return totSteps;
		}

		public String getName() {
			return mode.getDisplayString();
		}

		public void setDefault() {
			mode.setDefaults();
			initValues();
		}

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite root = new Composite(parent, SWT.None);
		root.setLayout(new GridLayout());

		tv = new TableViewer(root, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = tv.getTable();

		table.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tv.setContentProvider(ArrayContentProvider.getInstance());

		TableViewerColumn buttonNameColumn = new TableViewerColumn(tv, SWT.NONE);
		buttonNameColumn.getColumn().setWidth(150);
		buttonNameColumn.getColumn().setText(HEADER_BUTTON_NAME);
		buttonNameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PreferenceContent p = (PreferenceContent) element;
				return p.getName();
			}
		});

		TableViewerColumn totalStepsColumn = new TableViewerColumn(tv, SWT.NONE);
		totalStepsColumn.getColumn().setWidth(90);
		totalStepsColumn.getColumn().setText(HEADER_TOTAL_STEPS);
		totalStepsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PreferenceContent p = (PreferenceContent) element;
				return Integer.toString(p.getTotSteps());
			}
		});
		totalStepsColumn.setEditingSupport(new TotalStepsEditingSupport(tv));

		TableViewerColumn stepSizeColumn = new TableViewerColumn(tv, SWT.NONE);
		stepSizeColumn.getColumn().setWidth(60);
		stepSizeColumn.getColumn().setText(HEADER_STEP_SIZE);
		stepSizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PreferenceContent p = (PreferenceContent) element;
				return Double.toString(p.getStepSize());
			}
		});
		stepSizeColumn.setEditingSupport(new StepSizeEditingSupport(tv));

		contents = new PreferenceContent[] { new PreferenceContent(CENTRE_OF_ROTATION_MODE.COARSE_MODE),
				new PreferenceContent(CENTRE_OF_ROTATION_MODE.FINE_MODE),
				new PreferenceContent(CENTRE_OF_ROTATION_MODE.VERY_FINE_MODE) };

		tv.setInput(contents);
		return root;
	}

	@Override
	protected void performDefaults() {
		if (contents != null) {
			for (PreferenceContent content : contents) {
				content.setDefault();
			}
			tv.refresh();

		}
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if (contents != null) {
			for (PreferenceContent content : contents) {
				content.saveContent();
			}
		}
		return super.performOk();
	}
}
