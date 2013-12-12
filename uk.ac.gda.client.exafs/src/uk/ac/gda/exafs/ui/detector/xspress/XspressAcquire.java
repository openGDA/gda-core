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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.detector.Acquire;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.Data;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.richbeans.components.data.DataWrapper;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
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
	private String acquireFileLabelText;
	private static final String xspressSaveDir = "gda.device.xspress.spoolDir";
	private SashFormPlotComposite sashPlotFormComposite;
	private XspressDetector xspressDetector;
	private double collectionTime;
	private Counts counts;
	private ComboWrapper readoutMode;
	private ComboAndNumberWrapper resolutionGrade;
	private DirtyContainer dirtyContainer;
	private BooleanWrapper showIndividualElements;
	private Plot plot;
	
	public XspressAcquire(Composite acquire, final SashFormPlotComposite sashPlotFormComposite, Display display, Data plotData, final ComboWrapper readoutMode, final ComboAndNumberWrapper resolutionGrade, final Plot plot, final DirtyContainer dirtyContainer){
		super(display);
		this.display = display;
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.plotData = plotData;
		this.readoutMode = readoutMode;
		this.resolutionGrade = resolutionGrade;
		this.dirtyContainer = dirtyContainer;
		this.plot = plot;
		
		acquireBtn = new Button(acquire, SWT.NONE);
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/application_side_expand.png"));
		acquireBtn.setText("Acquire");
		acquireTime = new ScaleBox(acquire, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");
		acquireTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		autoSave = new Button(acquire, SWT.CHECK);
		autoSave.setText("Save on Acquire");
		autoSave.setSelection(LocalProperties.check("gda.detectors.save.single.acquire"));
		autoSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
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
		
		acquireFileLabel = new Label(acquire, SWT.NONE);
		acquireFileLabel.setText("									                                                    	                                                                        ");
		acquireFileLabel.setToolTipText("The file path for the acquire data");
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		gridData.horizontalSpan=6;
		acquireFileLabel.setLayoutData(gridData);
	}
	
	public void init(XspressParameters xspressParameters, final DataWrapper dataWrapper, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex, final GridListEditor detectorList, final Counts counts, final BooleanWrapper showIndividualElements){
		this.counts = counts;
		this.showIndividualElements = showIndividualElements;
		xspressDetector = Finder.getInstance().find(xspressParameters.getDetectorName());
		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				plotData(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
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
		plotData.save(detectorData, detectorFileLocation);
		sashPlotFormComposite.appendStatus("Xspress snapshot saved to " + detectorFile, logger);
	}
	
	public String getDetectorFileLocation(){
		return detectorFileLocation;
	}
	
	protected void acquire(XspressDetector xspressDetector, double collectionTime, String uiReadoutMode, String uiResolutionGrade) {
		this.collectionTime = collectionTime;
		try {
			originalResolutionGrade = xspressDetector.getResGrade();
			originalReadoutMode = xspressDetector.getReadoutMode();
		} catch (DeviceException e) {
			logger.error("Cannot get current resolution grade", e);
			return;
		}
		sashPlotFormComposite.appendStatus("Collecting a single frame of MCA data with resolution grade set to '" + uiResolutionGrade + "'.", logger);
		try {
			xspressDetector.setAttribute("readoutModeForCalibration", new String[] { uiReadoutMode, uiResolutionGrade });
			mcaData = xspressDetector.getMCData((int) collectionTime);
		} catch (DeviceException e) {
			sashPlotFormComposite.appendStatus("Cannot read out xspress detector data", logger);
			logger.error("Cannot read out xspress detector data", e);
		}
		sashPlotFormComposite.appendStatus("Collected data from detector successfully.", logger);
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
	public void plotData(DataWrapper dataWrapper, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) {
		String uiReadoutMode = (String) readoutMode.getValue();
		String uiResolutionGrade = uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) resolutionGrade.getValue() : ResGrades.NONE;
		acquire(xspressDetector, acquireTime.getNumericValue(), uiReadoutMode, uiResolutionGrade);
		ElementCountsData[] elementCountsData = ElementCountsData.getDataFor(mcaData);
		dataWrapper.setValue(elementCountsData);
		Boolean showIndividualElementsValue = showIndividualElements.getValue();
		counts.calculateAndPlotCountTotals(showIndividualElementsValue, true, mcaData, detectorElementComposite, currentSelectedElementIndex);
		if (saveMcaOnAcquire)
			saveMca(sashPlotFormComposite, xspressSaveDir);
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				String acquireFileLabelText = getAcquireFileLabelText();
				if(acquireFileLabelText!=null)
					acquireFileLabel.setText(acquireFileLabelText);
				detectorElementComposite.setEndMaximum((mcaData[0][0].length) - 1);
				plot.plot(detectorList.getSelectedIndex(),true, mcaData, detectorElementComposite, currentSelectedElementIndex, false, resolutionGrade);
				dirtyContainer.setDirty(true);
			}
		});
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
			acquireFileLabel.setText("Saved: " + detectorFileLocation);
		} catch (Exception e) {
			sashPlotFormComposite.appendStatus("Cannot write xspress detector data to disk", logger);
			logger.error("Cannot write xspress detector data to disk.", e);
			return;
		}
	}

	public String getAcquireFileLabelText() {
		return acquireFileLabelText;
	}
	
	public double getCollectionTime(){
		return collectionTime;
	}

}