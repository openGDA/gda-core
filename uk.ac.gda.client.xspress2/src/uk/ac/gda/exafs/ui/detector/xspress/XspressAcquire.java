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

package uk.ac.gda.exafs.ui.detector.xspress;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;
import gda.factory.Finder;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.detector.Acquire;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

import com.swtdesigner.SWTResourceManager;

public class XspressAcquire extends Acquire {
	private static final Logger logger = LoggerFactory.getLogger(XspressAcquire.class);
	private String detectorFileLocation;
	private String originalResolutionGrade;
	private String originalReadoutMode;
	private int[][][] mcaData;
	private static final String xspressSaveDir = "gda.device.xspress.spoolDir";
	private SashFormPlotComposite sashPlotFormComposite;
	private XspressDetector xspressDetector;
	private double collectionTime;
	private Counts counts;
	private ComboWrapper readoutMode;
	private ComboAndNumberWrapper resolutionGrade;
	private Plot plot;
	private XspressData xspressData;
	private Button loadBtn;
	private FileDialog openDialog;
	
	public XspressAcquire(Composite parent, final SashFormPlotComposite sashPlotFormComposite, Display display, final ComboWrapper readoutMode, final ComboAndNumberWrapper resolutionGrade, final Plot plot, XspressDetector xspressDetector, Counts counts){
		super(display);
		this.display = display;
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.readoutMode = readoutMode;
		this.resolutionGrade = resolutionGrade;
		this.plot = plot;
		this.xspressDetector = xspressDetector;
		
		Group grpAcquire = new Group(parent, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		grpAcquire.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		grpAcquire.setLayout(gridLayout);
		
		loadBtn = new Button(grpAcquire, SWT.NONE);
		loadBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		loadBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadBtn.setText("Load Saved mca");
		openDialog = new FileDialog(grpAcquire.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
		
		xspressData = new XspressData();
		
		acquireBtn = new Button(grpAcquire, SWT.NONE);
		acquireBtn.setText("Acquire");
		
		acquireTime = new ScaleBox(grpAcquire, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");
		acquireTime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		autoSave = new Button(grpAcquire, SWT.CHECK);
		autoSave.setText("Save on Acquire");
		autoSave.setSelection(LocalProperties.check("gda.detectors.save.single.acquire"));
		autoSave.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		autoSave.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				saveMcaOnAcquire = autoSave.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		autoSave.setSelection(saveMcaOnAcquire);
		
		acquireFileLabel = new Label(grpAcquire, SWT.WRAP);
		acquireFileLabel.setText("										 ");
		
		acquireFileLabel.setToolTipText("The file path for the acquire data");
		GridData gridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
		gridData.horizontalSpan=3;
		gridData.heightHint=50;
		acquireFileLabel.setLayoutData(gridData);
		
		this.counts = counts;
	}
	
	public void addLoadListener(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int detectorListLength){
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {			
				try {
					final String filePath = openDialog.open();
					if(filePath!=null){
						xspressData.load(openDialog, detectorListLength, filePath);
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								detectorElementComposite.setEndMaximum((mcaData[0][0].length) - 1);
								plot.plot(detectorList.getSelectedIndex(), mcaData, false, resolutionGrade);
							}
						});
					}
				} catch (Exception e1) {
					logger.error("Cannot acquire xspress data", e1);
				}
			}
		});
	}
	
	public void writeToDisk(String xspressSaveDir, int[][][] detectorData) throws Exception{
		String xspressSaveFullDir = PathConstructor.createFromProperty(xspressSaveDir);
		if (xspressSaveFullDir == null || xspressSaveFullDir.length() == 0)
			throw new Exception("Error saving data. Xspress device spool dir is not defined in property " + xspressSaveDir);
		long snapShotNumber = new NumTracker("Xspress_snapshot").incrementNumber();
		String fileName = "xspress_snap_" + snapShotNumber + ".mca";
		File detectorFile = new File(xspressSaveFullDir + "/" + fileName);
		detectorFileLocation = detectorFile.getAbsolutePath();
		xspressData.save(detectorData, detectorFileLocation);
		sashPlotFormComposite.appendStatus("Xspress snapshot saved to " + detectorFile, logger);
	}
	
	public String getDetectorFileLocation(){
		return detectorFileLocation;
	}
	
	@Override
	public void acquire(double collectionTime) throws DeviceException, InterruptedException{
		
		this.collectionTime = collectionTime;
		
		try {
			originalResolutionGrade = xspressDetector.getResGrade();
			originalReadoutMode = xspressDetector.getReadoutMode();
		} catch (DeviceException e) {
			logger.error("Cannot get current resolution grade", e);
			return;
		}
		
		String uiReadoutMode = (String) readoutMode.getValue();
		String uiResolutionGrade = uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) resolutionGrade.getValue() : ResGrades.NONE;
		
		sashPlotFormComposite.appendStatus("Collecting a single frame of MCA data with resolution grade set to '" + uiResolutionGrade + "'.", logger);
		
		try {
			xspressDetector.setAttribute("readoutModeForCalibration", new String[] { uiReadoutMode, uiResolutionGrade });
			mcaData = xspressDetector.getMCData((int) collectionTime);
		} catch (DeviceException e) {
			sashPlotFormComposite.appendStatus("Cannot read out xspress detector data", logger);
			logger.error("Cannot read out xspress detector data", e);
		}
		
		sashPlotFormComposite.appendStatus("Collected data from detector successfully.", logger);
		
		//TODO why is the following here? The data has already been collected.
		try {
			xspressDetector.setResGrade(originalResolutionGrade);
			xspressDetector.setReadoutMode(originalReadoutMode);
		} catch (DeviceException e) {
			sashPlotFormComposite.appendStatus("Cannot reset res grade, detector may be in an error state.", logger);
			logger.error("Cannot reset res grade, detector may be in an error state", e);
		}
		sashPlotFormComposite.appendStatus("Reset detector to resolution grade '" + originalResolutionGrade + "'.", logger);
		
	}
	
	@Override
	public void plotData(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex)  throws DeviceException, InterruptedException {
		detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(currentSelectedElementIndex, mcaData));
		detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
		if (saveMcaOnAcquire)
			saveMca(sashPlotFormComposite, xspressSaveDir);
		detectorElementComposite.setEndMaximum((mcaData[0][0].length) - 1);
		plot.plot(detectorList.getSelectedIndex(), mcaData, false, resolutionGrade);
	}
	
	public int[][][] getMcaData(){
		return mcaData;
	}
	
	public String getOriginalResolutionGrade(){
		return originalResolutionGrade;
	}
	
	public String getOriginalReadoutMode(){
		return originalReadoutMode;
	}
	
	public void saveMca(SashFormPlotComposite sashPlotFormComposite, String xspressSaveDir){
		try {
			writeToDisk(xspressSaveDir, mcaData);
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					acquireFileLabel.setText("Saved: " + detectorFileLocation);
				}
			});
		} catch (Exception e) {
			sashPlotFormComposite.appendStatus("Cannot write xspress detector data to disk", logger);
			logger.error("Cannot write xspress detector data to disk.", e);
			return;
		}
	}
	
	public double getCollectionTime(){
		return collectionTime;
	}
	
}
