/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import gda.observable.IObserver;

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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.client.microfocus.views.scan.MicroFocusElementListView;

public class ExafsSelectionView extends ViewPart implements IObserver {

	public static final String ID = "uk.ac.gda.client.microfocus.SelectExafsView";
	public ExafsSelectionView() {
		super();
		controller = ExperimentFactory.getExperimentEditorManager();
	}
	
	private List exafsScanList;
	private List selectedScanList;
	private Text pointText;
	private MicroFocusElementListView MFView;
	protected final IExperimentEditorManager    controller;
	private Text multiScanNameText;
	private DecimalFormat format = new DecimalFormat(".###");

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusElementListView.class);
	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		Composite exafsRunComp = new Composite(parent, SWT.BORDER);
		GridLayout grid = new GridLayout();
		grid.numColumns = 2;
		GridData gridData;
		exafsRunComp.setLayout(grid);
		Label pointLabel = new Label(exafsRunComp, SWT.LEFT);
		pointLabel.setText("Selected Point");
		pointText  = new Text(exafsRunComp, SWT.BORDER|SWT.READ_ONLY| SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		pointText.setLayoutData(gridData);
		Label scanNameLabel = new Label(exafsRunComp, SWT.LEFT);
		scanNameLabel.setText("Scan Name");
		multiScanNameText = new Text(exafsRunComp, SWT.BORDER| SWT.RIGHT);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		multiScanNameText.setLayoutData(gridData);
		Label label = new Label(exafsRunComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		//new Label(exafsRunComp, SWT.NONE);
		Label availableExafsLabel = new Label(exafsRunComp, SWT.LEFT);
		availableExafsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		availableExafsLabel.setText("Available Exafs Scans");
		exafsScanList = new List(exafsRunComp,SWT.BORDER |SWT.SINGLE | SWT.V_SCROLL );
		//exafsScanList.setS
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.heightHint = 125;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		exafsScanList.setLayoutData(gridData);
		populateExafsScanList();
		new Label(exafsRunComp, SWT.NONE);
		Label selectedExafsLabel = new Label(exafsRunComp, SWT.LEFT);
		selectedExafsLabel.setText("Selected Scans");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;		
		selectedExafsLabel.setLayoutData(gridData);
		selectedScanList = new List (exafsRunComp,SWT.BORDER |SWT.MULTI | SWT.V_SCROLL );
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.heightHint = 179;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		selectedScanList.setLayoutData(gridData);
		try {
			// get an instance of the plotView we want to use
			MFView = (MicroFocusElementListView) getSite().getPage().showView("uk.ac.gda.client.microfocus.XspressElementListView",
					null, IWorkbenchPage.VIEW_CREATE);
			MFView.addIObserver(this);
			MFView = (MicroFocusElementListView) getSite().getPage().showView("uk.ac.gda.client.microfocus.VortexElementListView",
					null, IWorkbenchPage.VIEW_CREATE);
			MFView.addIObserver(this);
			
		}
		catch (Exception e) {
			logger.error("Error while finding the MF view", e);
		}
			
		
		
	}

	private void populateExafsScanList() {
		// TODO Auto-generated method stub
		File projectDir = controller.getProjectFolder();
		ScanFilter scanFilter = new ScanFilter(); 
		File dirList[] =projectDir.listFiles();
		for (File dir : dirList){
			if(dir.isDirectory() && dir.getName().startsWith("Experiment")){
				String[] files = dir.list(scanFilter);
				for (String file : files){
					exafsScanList.add(dir.getName()+ File.separator+ file);
				}
			}
		}
		
	}

	public String[] getScanSelection()
	{
		return this.selectedScanList.getItems();
	}
	
	public String getNewMultiScanName()
	{
		return multiScanNameText.getText();
	}
	
	public void add()
	{
		final String point = pointText.getText();
		final String[] selection = exafsScanList.getSelection();
		getSite().getShell().getDisplay().asyncExec(new Runnable()  {
			@Override
			public void run() {
		for (String s : selection)
		{
			selectedScanList.add(point + s);
		}
			}
		});
		
	}
	
	public void delete()
	{
		String[] sel = selectedScanList.getSelection();
		for(String s : sel){
			selectedScanList.remove(s);
		}
	}
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Object source, Object arg) {
		logger.info("Info from the Exafs Selection View called" );
		if(arg instanceof double[])
		{
			final double[] ob = (double[])arg;
			logger.info("Info from Exafs Selection view " + ob[0] + " " + ob[1]); 
			getSite().getShell().getDisplay().asyncExec(new Runnable()  {
				@Override
				public void run() {
					setStatusLine("("+format.format(ob[0])+","+format.format(ob[1])+ ","+ format.format(ob[2])+")");
					pointText.setText("("+format.format(ob[0])+","+format.format(ob[1])+ ","+ format.format(ob[2])+")");
					//selectedScanList.add("("+ob[0]+","+ob[1]+ ","+ ob[2]+")");
				}
			});
			
		}
	}

	public void refresh()
	{
		logger.info("REfresh called from ExafsSelectionView");
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
