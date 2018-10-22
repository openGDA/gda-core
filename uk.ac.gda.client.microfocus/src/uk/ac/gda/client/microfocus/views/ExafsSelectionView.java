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

package uk.ac.gda.client.microfocus.views;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.microfocus.views.scan.MicroFocusElementListView;

public class ExafsSelectionView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.microfocus.SelectExafsView";
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusElementListView.class);

	private List exafsScanList;
	private List selectedScanList;
	private Text pointText;
	protected final IExperimentEditorManager controller;
	private Text multiScanNameText;
	private DecimalFormat format = new DecimalFormat(".###");

	/**
	 * If the sample prefix is not consistent across mapping stage,
	 * this label will be visible to warn the user.
	 */
	private Label warningLabel;

	private String sampleStagePrefix;

	public ExafsSelectionView() {
		super();
		controller = ExperimentFactory.getExperimentEditorManager();
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite exafsRunComp = new Composite(parent, SWT.BORDER);
		GridLayout grid = new GridLayout();
		grid.numColumns = 2;
		GridData gridData;
		exafsRunComp.setLayout(grid);

		Label pointLabel = new Label(exafsRunComp, SWT.LEFT);
		pointLabel.setText("Selected Point");
		pointText = new Text(exafsRunComp, SWT.BORDER | SWT.READ_ONLY | SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		pointText.setLayoutData(gridData);

		Label scanNameLabel = new Label(exafsRunComp, SWT.LEFT);
		scanNameLabel.setText("Scan Name");
		multiScanNameText = new Text(exafsRunComp, SWT.BORDER | SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		multiScanNameText.setLayoutData(gridData);

		Label separator = new Label(exafsRunComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label availableExafsLabel = new Label(exafsRunComp, SWT.LEFT);
		availableExafsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		availableExafsLabel.setText("Available Exafs Scans");

		warningLabel = new Label(exafsRunComp, SWT.NONE);
		warningLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		warningLabel.setText("Inconsistent axes selection!");
		warningLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		hideWarning();

		// List of scans to select
		exafsScanList = new List(exafsRunComp, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		// exafsScanList.setS
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.heightHint = exafsScanList.getItemHeight() * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		exafsScanList.setLayoutData(gridData);
		populateExafsScanList();

		Label selectedExafsLabel = new Label(exafsRunComp, SWT.LEFT);
		selectedExafsLabel.setText("Selected Scans");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		selectedExafsLabel.setLayoutData(gridData);

		// List of selected scans
		selectedScanList = new List(exafsRunComp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.heightHint = selectedScanList.getItemHeight() * 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		selectedScanList.setLayoutData(gridData);
	}

	private void populateExafsScanList() {
		File projectDir = controller.getProjectFolder();
		ScanFilter scanFilter = new ScanFilter();
		File[] dirList = projectDir.listFiles();
		for (File dir : dirList) {
			if (dir.isDirectory()) {
				String[] files = dir.list(scanFilter);
				for (String file : files) {
					exafsScanList.add(dir.getName() + File.separator + file);
				}
			}
		}

	}

	public String[] getScanSelection() {
		return this.selectedScanList.getItems();
	}

	public String getNewMultiScanName() {
		return multiScanNameText.getText();
	}

	public void add() {
		final String point = pointText.getText();
		final String[] selection = exafsScanList.getSelection();
		Display.getDefault().asyncExec(() -> {
			for (String s : selection) {
				selectedScanList.add(point + s);
			}
		});

	}

	public void delete() {
		String[] sel = selectedScanList.getSelection();
		for (String s : sel) {
			selectedScanList.remove(s);
		}
	}

	@Override
	public void setFocus() {
		// ignore
	}

	public void setSelectedPoint(final Double[] xyzPosition) {
		logger.trace("Info from Exafs Selection view {} {} {}", xyzPosition[0], xyzPosition[1], xyzPosition[2]);

		if (xyzPosition[2] == null){
			xyzPosition[2] = Double.valueOf(0);
		}

		final String formattedPosition = "(" + format.format(xyzPosition[0]) + "," + format.format(xyzPosition[1]) + ","
				+ format.format(xyzPosition[2]) + ")";

		Display.getDefault().asyncExec(() -> {
			setStatusLine(formattedPosition);
			pointText.setText(formattedPosition);
		});
	}

	public void refresh() {
		exafsScanList.removeAll();
		populateExafsScanList();
	}

	private void setStatusLine(String message) {
		// Get the status line and set the text
		IActionBars bars = getViewSite().getActionBars();
		bars.getStatusLineManager().setMessage(message);
	}

	class ScanFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return (name.endsWith(".scan"));
		}
	}

	public void setSampleStagePrefix(String sampleStagePrefix) {
		if (sampleStagePrefix != null) {
			this.sampleStagePrefix = sampleStagePrefix;
			hideWarning();
		} else {
			showWarning();
		}
	}

	public String getSampleStagePrefix() {
		return sampleStagePrefix;
	}

	private void hideWarning() {
		warningLabel.setVisible(false);
	}

	private void showWarning() {
		warningLabel.setVisible(true);
	}
}
