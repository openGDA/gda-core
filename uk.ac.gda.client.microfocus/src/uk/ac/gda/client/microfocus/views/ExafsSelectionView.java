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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
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

		Label availableScansLabel = new Label(exafsRunComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		availableScansLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		// new Label(exafsRunComp, SWT.NONE);
		Label availableExafsLabel = new Label(exafsRunComp, SWT.LEFT);
		availableExafsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		availableExafsLabel.setText("Available Exafs Scans");

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
		File dirList[] = projectDir.listFiles();
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
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				for (String s : selection) {
					selectedScanList.add(point + s);
				}
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
		logger.debug("Info from Exafs Selection view " + xyzPosition[0] + " " + xyzPosition[1] + " " + xyzPosition[2]);
		if (xyzPosition[2] == null){
			xyzPosition[2] = Double.parseDouble(InterfaceProvider.getCommandRunner().evaluateCommand("sc_sample_z()"));
		}
		getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				setStatusLine("(" + format.format(xyzPosition[0]) + "," + format.format(xyzPosition[1]) + ","
						+ format.format(xyzPosition[2]) + ")");
				pointText.setText("(" + format.format(xyzPosition[0]) + "," + format.format(xyzPosition[1]) + ","
						+ format.format(xyzPosition[2]) + ")");
			}
		});
	}

	public void refresh() {
//		logger.info("REfresh called from ExafsSelectionView");
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

}
