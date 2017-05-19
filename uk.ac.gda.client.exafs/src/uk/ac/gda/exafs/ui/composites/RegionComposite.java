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

package uk.ac.gda.exafs.ui.composites;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.gda.beans.exafs.Region;
import uk.ac.gda.beans.exafs.XanesRegionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.exafs.ui.XanesScanParametersUIEditor;


public class RegionComposite extends Composite {
	private TableViewer viewer;
	private ArrayList<XanesRegionParameters> regions;
	private List<Region> beanRegions;
	XanesScanParameters bean;
	IWorkbenchPartSite site;
	Composite parent;
	XanesScanParametersUIEditor editor;
	Button addRegionBtn;
	Button removeRegionBtn;

	/**
	 * Create the composite
	 *
	 * @param parent
	 * @param style
	 * @param site
	 */
	public RegionComposite(final Composite parent, final int style, IWorkbenchPartSite site, XanesScanParameters newBean, XanesScanParametersUIEditor newEditor) {
		super(parent, style);
		this.parent=parent;
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		setLayout(gridLayout);
		bean = newBean;
		this.site = site;
		this.editor=newEditor;

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 380;
		gridData.heightHint=200;
		viewer.getControl().setLayoutData(gridData);

		createColumns(viewer);

		Table table = viewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(new ArrayContentProvider());

		updateTable();

		createEmptyLabel(parent);

		Composite buttons = new Composite(parent, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.HORIZONTAL;
		buttons.setLayout(fillLayout);

		addRegionBtn = new Button(buttons, SWT.NONE);
		removeRegionBtn = new Button(buttons, SWT.NONE);
		addRegionBtn.setText("           Add Region           ");
		removeRegionBtn.setText("           Remove Region           ");
		removeRegionBtn.setToolTipText("Select a region by region number and click Remove Region to delete");

		addRegionBtn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Region lastRegion = bean.getRegions().get(bean.getRegions().size()-1);
				Region newRegion = new Region();
				newRegion.setEnergy(lastRegion.getEnergy()+100.0);
				newRegion.setStep(lastRegion.getStep());
				newRegion.setTime(lastRegion.getTime());
				bean.getRegions().add(newRegion);
				updateTable();
				editor.doSave(null);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		removeRegionBtn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				bean.getRegions().remove(viewer.getTable().getSelectionIndex());
				updateTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		createEmptyLabel(parent);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	// This will create the columns for the table
	private void createColumns(final TableViewer viewer) {

		String[] titles = { "Region", "Start", "Step", "Time" };
		int[] bounds = { 90, 90, 90, 90 };

		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XanesRegionParameters r = (XanesRegionParameters) element;
				return String.valueOf(r.getRegion());
			}
			@Override
			public void update(ViewerCell cell) {
				int region = ((XanesRegionParameters) cell.getElement()).getRegion();
				String strRegion = String.valueOf(region);
				cell.setText(strRegion);
			}
		});

		col = createTableViewerColumn(titles[1], bounds[1]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XanesRegionParameters r = (XanesRegionParameters) element;
				return String.valueOf(r.getStartEnergy());
			}
			@Override
			public void update(ViewerCell cell) {
				double startEnergy = ((XanesRegionParameters) cell.getElement()).getStartEnergy();
				cell.setText(String.valueOf(startEnergy));
				try {
					if(editor.getElement().isOn())
						editor.updatePlottedPoints();
				} catch (Exception e) {
				}
			}
		});
		col.setEditingSupport(new StartEnergyEditingSupport(viewer, bean, editor));

		col = createTableViewerColumn(titles[2], bounds[2]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XanesRegionParameters r = (XanesRegionParameters) element;
				return String.valueOf(r.getStepEnergy());
			}
			@Override
			public void update(ViewerCell cell) {
				double step = ((XanesRegionParameters) cell.getElement()).getStepEnergy();
				String strStep = String.valueOf(step);
				cell.setText(strStep);
				try {
					if(editor.getElement().isOn())
						editor.updatePlottedPoints();
				} catch (Exception e) {
				}
			}
		});
		col.setEditingSupport(new StepEditingSupport(viewer, bean, editor));

		col = createTableViewerColumn(titles[3], bounds[2]);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XanesRegionParameters r = (XanesRegionParameters) element;
				return String.valueOf(r.getTime());
			}
			@Override
			public void update(ViewerCell cell) {
				double time = ((XanesRegionParameters) cell.getElement()).getTime();
				String strTime = String.valueOf(time);
				cell.setText(strTime);
				try {
					if(editor.getElement().isOn())
						editor.updatePlottedPoints();
				} catch (Exception e) {
				}
			}
		});
		col.setEditingSupport(new TimeEditingSupport(viewer, bean, site, editor));
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public List<Region> getBeanRegions() {
		return bean.getRegions();
	}

	public void updateTable(){
		beanRegions = bean.getRegions();
		regions = new ArrayList<XanesRegionParameters>();
		for (int i = 0; i < beanRegions.size(); i++) {
			if (beanRegions.get(i) != null) {
				double energy = beanRegions.get(i).getEnergy().doubleValue();
				double step = beanRegions.get(i).getStep().doubleValue();
				double time = beanRegions.get(i).getTime().doubleValue();
				regions.add(new XanesRegionParameters(i+1, energy, step, time));
			}
		}
		viewer.setInput(regions);
		if (site!= null) site.setSelectionProvider(viewer);
	}

	@SuppressWarnings("unused")
	private void createEmptyLabel(Composite composite){
		new Label(composite, SWT.NONE);
	}

}