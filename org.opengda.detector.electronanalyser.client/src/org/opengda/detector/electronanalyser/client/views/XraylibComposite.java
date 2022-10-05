/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.dawnsci.common.widgets.periodictable.IPeriodicTableButtonPressedListener;
import org.dawnsci.common.widgets.periodictable.PeriodicTableButtonPressedEvent;
import org.dawnsci.common.widgets.periodictable.PeriodicTableComposite;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.github.tschoonj.xraylib.XraylibException;

public class XraylibComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(XraylibComposite.class);
	private Color defaultButtonColor;
	private HashMap<Integer, ElementSelectionData> activeElements = new HashMap<>();
	private int activeElement;
	private String activeElementName;
	private PeriodicTableComposite periodicTable;
	private Table energyTable;
	private TableColumn column1;
	private TableColumn column2;

	private IPlottingSystem<Composite> plottingSystem;
	private Random random = new Random();
	private ArrayList<Integer> colors = new ArrayList<>(Arrays.asList(2,3,4,5,6,7,8,9,10,11,12,13,14,16));
	private Color buttonAndLineColor;

	private SelectionListener tableSelectionListener;
	private IPeriodicTableButtonPressedListener buttonPressedListener;

	private Button selectAll;


	private static final String[] shellNames = new String[]{
			"1s",
			"2s", "2p¹/₂", "2p³⁄₂",
			"3s", "3p¹/₂", "3p³⁄₂", "3d³⁄₂", "3d⁵⁄₂",
			"4s", "4p¹/₂", "4p³⁄₂", "4d³⁄₂", "4d⁵⁄₂", "4f⁵⁄₂", "4f⁷⁄₂",
			"5s", "5p¹/₂", "5p³⁄₂", "5d³⁄₂", "5d⁵⁄₂", "5f⁵⁄₂", "5f⁷⁄₂",
			"6s", "6p¹/₂", "6p³⁄₂", "6d³⁄₂", "6d⁵⁄₂",
			"7s", "7p¹/₂", "7p³⁄₂"
	};


	private class ElementSelectionData {

		private Color color;
		private ArrayList<TableRowData> rowData = new ArrayList<>();
		private boolean allSelected;


		public Color getColor() {
			return color;
		}
		public void setColor(Color color) {
			this.color = color;
		}
		public void setRowData(TableRowData rowData) {
			this.rowData.add(rowData);
		}
		public ArrayList<TableRowData> getRowData() {
			return rowData;
		}
		public boolean isAllSelected() {
			return allSelected;
		}
		public void setAllSelected(boolean allSelected) {
			this.allSelected = allSelected;
		}
	}

	private class TableRowData {

		private boolean checked;
		private String shell;
		private String energy;

		public void setChecked(boolean checked) {
			this.checked = checked;
		}
		public void setShell(String shell) {
			this.shell = shell;
		}
		public void setEnergy(String energy) {
			this.energy = energy;
		}
	}



	public XraylibComposite(Composite parent, int style, IPlottingSystem<Composite> plottingSystem) {
		super(parent, style);

		this.plottingSystem = plottingSystem;

		GridLayout grid_layout = new GridLayout(2, false);
		this.setLayout(grid_layout);
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		try {
			periodicTable = new PeriodicTableComposite(this);
		} catch (Exception e) {
			logger.error("Could not construct PeriodicTableComposite", e);
		}

		periodicTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite elementData = new Composite(this, SWT.BORDER);
		GridData elementGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		elementGridData.minimumWidth = 300;
		elementGridData.heightHint = 450;
		elementData.setLayout(new GridLayout(1, false));
		elementData.setLayoutData(elementGridData);

		Composite elementInfo = new Composite(elementData, SWT.BORDER);
		GridData elementInfoData = new GridData(SWT.LEFT, SWT.FILL, false, false);
		//elementInfoData.horizontalIndent = 10;
		elementInfo.setLayout(new GridLayout(2, false));
		elementInfo.setLayoutData(elementInfoData);

		Label elementLabel = new Label(elementInfo, SWT.LEFT);
		elementLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		FontDescriptor descriptor = FontDescriptor.createFrom(elementLabel.getFont()).setHeight(14);
		Font bigFont = descriptor.createFont(elementLabel.getDisplay());
		elementLabel.setText("Element: ");
		Text elementText = new Text(elementInfo, SWT.NONE);
		elementText.setFont(bigFont);

		Label atomicNumberLabel = new Label(elementInfo, SWT.LEFT);
		atomicNumberLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		atomicNumberLabel.setText("Atomic number: ");
		Text atomicNumberText = new Text(elementInfo, SWT.NONE);

		Label atomicWeightLabel = new Label(elementInfo, SWT.LEFT);
		atomicWeightLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		atomicWeightLabel.setText("Atomic weight: ");
		Text atomicWeightText = new Text(elementInfo, SWT.NONE);

		Label elementDensityLabel = new Label(elementInfo, SWT.LEFT);
		elementDensityLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		elementDensityLabel.setText("Density: ");
		Text elementDensityText = new Text(elementInfo, SWT.NONE);

		selectAll = new Button(elementData, SWT.CHECK);
		selectAll.setText("Select all");
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (selectAll.getSelection()) {
					try {
						if(energyTable.getItemCount() != 0) {
							addAllPeaksForActiveElement();
						}
					} catch (ExecutionException e) {
						logger.debug("Could not plot peaks");
					}
				} else {
					if(energyTable.getItemCount() != 0) {
						removeAllPeaksForActiveElement();
					}
				}
			}
		});

		// Just create table without creating new composite
		energyTable = new Table(elementData, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.CHECK);
		energyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		energyTable.setLayout(new GridLayout());
		column1 = new TableColumn(energyTable, SWT.CENTER);
		column2 = new TableColumn(energyTable, SWT.CENTER);
		column1.setText("Shell");
		column2.setText("Energy (eV)");
		energyTable.setHeaderVisible(true);
		column1.setWidth(150);
		column2.setWidth(150);


		tableSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {

				if(event.detail == SWT.CHECK) {
					TableItem item = (TableItem)event.item;
					String regionName = String.format(" %s -%n %s", activeElementName, item.getText(0));
					if(item.getChecked()) {
						try {
							createPeakRegion(regionName, item.getText(1));
						} catch (ExecutionException e) {
							logger.debug("Could not plot energy peak");
						}
					} else {
						removeSelectedPeak(regionName);
					}
					// Check if element data should get cached
					if(tableHasCheckedRows()) {
						selectAll.setSelection(tableHasAllRowsChecked());
						cacheTableData(activeElement);
					} else {
						if(selectAll.getSelection()) {
							selectAll.setSelection(false);
						}
						activeElements.remove(activeElement);
					}
				}
			}
		};

		energyTable.addSelectionListener(tableSelectionListener);

		defaultButtonColor = periodicTable.getButton(1).getBackground();

		buttonPressedListener = new IPeriodicTableButtonPressedListener() {

			@Override
			public void buttonPressed(PeriodicTableButtonPressedEvent event) {

				// Check if previously selected element is cached
				if(!activeElements.containsKey(activeElement) && activeElement != 0) {
					periodicTable.getButton(activeElement).setBackground(defaultButtonColor);
				}

				activeElement = event.getZ();
				activeElementName = event.getElement();

				// Clear up table for displaying newly selected element
				energyTable.removeAll();
				selectAll.setSelection(false);

				if(!activeElements.containsKey(activeElement)) {
					buttonAndLineColor = Display.getCurrent().getSystemColor(getNextRandomColor());
					periodicTable.getButton(activeElement).setBackground(buttonAndLineColor);
					// Populate table with data from xraylib database
					for (int shell = Xraylib.K_SHELL ; shell <= Xraylib.Q3_SHELL; shell++) {
						try {
							double edgeEnergy = Xraylib.EdgeEnergy(activeElement, shell);
							new TableItem(energyTable, SWT.NONE).setText(new String[]{shellNames[shell], ""+edgeEnergy*1000});
						} catch (XraylibException e) {
							logger.debug("Problem with Xraylib API database");
						}
					}
				} else {
					buttonAndLineColor = activeElements.get(activeElement).getColor();
					// Populate table with cached data to recover checked rows
					ElementSelectionData allTableItemData = activeElements.get(activeElement);
					selectAll.setSelection(allTableItemData.isAllSelected());
					for (TableRowData cachedTableItemData: allTableItemData.getRowData()) {
						TableItem item = new TableItem(energyTable, SWT.NONE);
						item.setText(new String[]{cachedTableItemData.shell, cachedTableItemData.energy});
						item.setChecked(cachedTableItemData.checked);
					}
				}

				// Populate element info
				elementText.setText(Xraylib.AtomicNumberToSymbol(activeElement));
				atomicNumberText.setText(""+activeElement);
				try {
					atomicWeightText.setText(Xraylib.AtomicWeight(activeElement) + " g/mol");
				} catch (XraylibException e) {
					atomicWeightText.setText("unknown");
				}
				try {
					elementDensityText.setText(Xraylib.ElementDensity(activeElement) + " g/cm\u00B3");
				} catch (XraylibException e) {
					elementDensityText.setText("unknown");
				}
			}
		};



		periodicTable.addPeriodicTableButtonPressedListener(buttonPressedListener);

		//Choose the default element selected on initialization (Co)
		//periodicTable.getButton(27).notifyListeners(SWT.Selection, new Event());
	}

	private void createPeakRegion(String name, String energy) throws ExecutionException {
		IRegion region;
		try {
			region = plottingSystem.createRegion(name, RegionType.LINE);
		} catch (Exception e) {
			throw new ExecutionException("Exception on creating region", e);
		}
		region.setRegionColor(buttonAndLineColor);

		IROI roix = new LinearROI(new double[] {Float.parseFloat(energy), 0}, new double[] {Float.parseFloat(energy), getPlotHigh()});
		region.setROI(roix);
		region.setMobile(false);
		region.setUserRegion(true);
		region.setAlpha(255);
		region.setLabel(name);
		region.setShowLabel(true);
		plottingSystem.addRegion(region);
	}

	private void removeSelectedPeak(String name) {
		IRegion regionToRemove = plottingSystem.getRegion(name);
		plottingSystem.removeRegion(regionToRemove);
	}

	private void cacheTableData(int selectedElement) {
		ElementSelectionData elementSelectionData = new ElementSelectionData();
		elementSelectionData.setColor(buttonAndLineColor);
		elementSelectionData.setAllSelected(selectAll.getSelection());
		TableItem[] items = energyTable.getItems();
		for (TableItem item: items) {
			TableRowData tableRowData = new TableRowData();
			tableRowData.setChecked(item.getChecked());
			tableRowData.setShell(item.getText(0));
			tableRowData.setEnergy(item.getText(1));
			elementSelectionData.setRowData(tableRowData);
		}
		activeElements.put(selectedElement, elementSelectionData);
	}

	private boolean tableHasCheckedRows() {
		TableItem[] tableItems = energyTable.getItems();
		for(TableItem item: tableItems) {
			if (item.getChecked()) {
				return true;
			}
		}
		return false;
	}

	private boolean tableHasAllRowsChecked() {
		TableItem[] tableItems = energyTable.getItems();
		for(TableItem item: tableItems) {
			if (!item.getChecked()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Draw colours from a list
	 * refills list when no more colours
	 * @return
	 */
	private int getNextRandomColor() {

		if (colors.isEmpty()) {
			Collections.addAll(colors, 2, 3, 4, 5, 6, 7 , 8, 9, 10, 11, 12, 13, 14, 16);
		}
		int randomIndex = random.nextInt(colors.size());
	    return colors.remove(randomIndex);
	}

	public void removeAllRegions() {
		Display.getDefault().asyncExec(() -> {
			plottingSystem.clearRegions();
		});
	}

	private void removeAllPeaksForActiveElement() {
		TableItem[] tableItems = energyTable.getItems();
		for(TableItem item: tableItems) {
			if(item.getChecked()) {
				item.setChecked(false);
				String regionName = String.format(" %s -%n %s", activeElementName, item.getText(0));
				removeSelectedPeak(regionName);
			}
		}
		activeElements.remove(activeElement);
	}

	private void addAllPeaksForActiveElement() throws ExecutionException {
		TableItem[] tableItems = energyTable.getItems();
		for(TableItem item: tableItems) {
			if(!item.getChecked()) {
				item.setChecked(true);
				String regionName = String.format(" %s -%n %s", activeElementName, item.getText(0));
				createPeakRegion(regionName, item.getText(1));
			}
		}
		cacheTableData(activeElement);
	}

	public void cleanUp() {
		energyTable.removeSelectionListener(tableSelectionListener);
		periodicTable.removePeriodicTableButtonPressedListener(buttonPressedListener);
	}

	private double getPlotHigh() {
		return plottingSystem.getSelectedYAxis().getUpper();
	}

	private double getPlotLow() {
		return plottingSystem.getSelectedYAxis().getLower();
	}

}
