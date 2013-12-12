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

package uk.ac.gda.exafs.ui.detector.vortex;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.XmapDetector;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.exafs.ui.detector.Acquire;
import uk.ac.gda.exafs.ui.detector.Data;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.richbeans.components.data.DataWrapper;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;

import com.swtdesigner.SWTResourceManager;

public class VortexAcquire extends Acquire {
	private int[][][] data3d;
	private static final Logger logger = LoggerFactory.getLogger(Acquire.class);
	private boolean continuousAquire = false;
	private Thread continuousThread;
	private XmapDetector xmapDetector;
	private Timer tfg;
	private SashFormPlotComposite sashPlotFormComposite;
	private FileDialog openDialog;
	private LabelWrapper deadTimeLabel;
	private ScaleBox acquireTime;
	private Composite acquire;
	private Button live;
	private Label lblDeadTime;
	private Display display;
	private boolean autoSaveEnabled;
	protected boolean writeToDisk = LocalProperties.check("gda.detectors.save.single.acquire");
	private VortexData vortexData;
	private Button loadBtn;
	private Plot plot;
	
	public VortexAcquire(SashFormPlotComposite sashPlotFormComposite, XmapDetector xmapDetector, Timer tfg, Display display, final Plot plot){
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.xmapDetector = xmapDetector;
		this.tfg = tfg;
		this.display = display;
		this.plot = plot;
		vortexData = new VortexData();
	}
	
	private void plotData(final DataWrapper dataWrapper, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) throws DeviceException{
		data3d = getData3d();
		dataWrapper.setValue(ElementCountsData.getDataFor(data3d));
		Double[] liveStats = (Double[]) xmapDetector.getAttribute("countRates");
		final double deadTimeFinal = (Math.abs(liveStats[0] - liveStats[1]) / liveStats[0]) * 100;
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				detectorElementComposite.setEndMaximum(data3d[0][0].length - 1);
				if(detectorList!=null)
					plot.plot(detectorList.getSelectedIndex(),true, data3d, detectorElementComposite, currentSelectedElementIndex, false, null);
				deadTimeLabel.setValue(deadTimeFinal);
				lblDeadTime.setVisible(true);
				deadTimeLabel.setVisible(true);
				sashPlotFormComposite.getLeft().layout();
			}
		});
	}
	
	public void writeToDisk(final Data plotData) throws IOException{
		if (writeToDisk && autoSaveEnabled) {
			String msg = "Error saving detector data to file";
			String vortexFilePath = "";
			try {
				String vortexSaveDir = PathConstructor.createFromProperty("gda.device.vortex.spoolDir");
				long snapShotNumber = new NumTracker("Vortex_snapshot").incrementNumber();
				String fileName = "vortex_snap_" + snapShotNumber+ ".mca";
				File filePath = new File(vortexSaveDir + "/" + fileName);
				vortexFilePath = filePath.getAbsolutePath();
				plotData.save(data3d, vortexFilePath);
				msg = "Saved: " + vortexFilePath;
				logger.info("Vortex snapshot saved to " + vortexFilePath);
			}
			finally {
				final String msgFinal = msg;
				display.syncExec(new Runnable() {
					@Override
					public void run() {
						acquireFileLabel.setText(msgFinal);
					}
				});
			}
		}
	}
	
	public void addAcquireListener(final Data plotData, final DataWrapper dataWrapper, final int currentSelectedElementIndex, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite){
		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					if (!live.getSelection()){
						acquire(acquireTime.getNumericValue());
						plotData(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
						if(writeToDisk)
							writeToDisk(plotData);
						else
							acquireFileLabel.setText("										");
					}
					else{
						continuousAquire=!continuousAquire;
						if(continuousAquire)
							continuousAcquire(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
					}
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
			}
		});
	}
	
	public void addLoadListener(final VortexParameters vortexParameters, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex){
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					final String filePath = openDialog.open();
					vortexData.load(openDialog, vortexParameters, filePath);
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							acquireFileLabel.setText("Loaded: " + filePath);
							detectorElementComposite.setEndMaximum((data3d[0][0].length) - 1);
							plot.plot(detectorList.getSelectedIndex(),false, vortexData.getDetectorData(), detectorElementComposite, currentSelectedElementIndex, false, null);
						}
					});
				} catch (Exception e1) {
					logger.error("Cannot acquire vortex data", e1);
				}
			}
		});
	}
	
	protected void acquire(double collectionTime) throws Exception {
		xmapDetector.clearAndStart();
		tfg.countAsync(collectionTime);
		xmapDetector.stop();
		xmapDetector.waitWhileBusy();
		int[][] data = xmapDetector.getData();
		data3d = convert2DTo3DArray(data);
	}
	
	public int[][][] getData3d() {
		return data3d;
	}

	protected int[][][] convert2DTo3DArray(int[][] data) {
		int[][][] ret = new int[data.length][1][];
		for (int i = 0; i < data.length; i++)
			ret[i][0] = data[i];
		return ret;
	}
	
	public void continuousAcquire(final DataWrapper dataWrapper, final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) {
		try {
			continuousThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (continuousAquire) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {								
								try {
									acquire(acquireTime.getNumericValue());
								} catch (Exception e) {
									logger.error("Error acquiring vortex data", e);
								}
								try {
									plotData(dataWrapper, detectorList, detectorElementComposite, currentSelectedElementIndex);
								} catch (DeviceException e) {
									logger.error("Error plotting vortex data", e);
								}
							}
						});
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					logger.error("Continuous acquire problem with detector.", e);
				} catch (Throwable e) {
					logger.error("Continuous acquire problem with detector.", e);
				}
			}
		}, "Detector Live Runner");
		continuousThread.start();
		} 
		catch (Exception e) {
			logger.error("Internal errror process continuous data from detector.", e);
		}
	}
	
	public void createAcquire(Composite parent, final Composite left) {
		Group grpAcquire = new Group(left, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		grpAcquire.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpAcquire.setLayout(gridLayout);
		loadBtn = new Button(grpAcquire, SWT.NONE);
		loadBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadBtn.setText("Load");
		acquire = new Composite(grpAcquire, SWT.NONE);
		GridLayout gridLayoutAcq = new GridLayout();
		gridLayoutAcq.numColumns = 9;
		gridLayoutAcq.marginWidth = 0;
		acquire.setLayout(gridLayoutAcq);
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
		autoSave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		autoSave.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				writeToDisk = autoSave.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		autoSave.setSelection(writeToDisk);
		autoSaveEnabled = true;

		live = new Button(acquire, SWT.CHECK);
		live.setText("Live");
		live.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		live.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				autoSave.setEnabled(!live.getSelection());
				autoSaveEnabled = !live.getSelection();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		acquireFileLabel = new Label(grpAcquire, SWT.NONE);
		acquireFileLabel.setText("										");
		acquireFileLabel.setToolTipText("The file path for the acquire data");
		acquireFileLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
		Composite composite_1 = new Composite(grpAcquire, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite_1);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite_1);
		lblDeadTime = new Label(composite_1, SWT.NONE);
		lblDeadTime.setText("Dead Time");
		lblDeadTime.setVisible(false);
		deadTimeLabel = new LabelWrapper(composite_1, SWT.NONE);
		deadTimeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		deadTimeLabel.setText("12");
		deadTimeLabel.setUnit("%");
		deadTimeLabel.setDecimalPlaces(3);
		deadTimeLabel.setVisible(false);
	}

	public ScaleBox getAcquireTime() {
		return acquireTime;
	}

}