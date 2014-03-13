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
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.XmapDetector;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.exafs.ui.detector.Acquire;
import uk.ac.gda.exafs.ui.detector.Counts;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.exafs.ui.detector.Plot;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;

import com.swtdesigner.SWTResourceManager;

public class VortexAcquire extends Acquire {
	private int[][][] mcaData;
	private static final Logger logger = LoggerFactory.getLogger(VortexAcquire.class);
	private XmapDetector xmapDetector;
	private Timer tfg;
	private SashFormPlotComposite sashPlotFormComposite;
	private FileDialog openDialog;
	private LabelWrapper deadTimeLabel;
	private Composite acquire;
	private Label lblDeadTime;
	private VortexData vortexData;
	private Button loadBtn;
	private Plot plot;
	private Counts counts;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public VortexAcquire(SashFormPlotComposite sashPlotFormComposite, Detector xmapDetector, Timer tfg, Display display, final Plot plot, Counts counts){
		super(display);
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.xmapDetector = (XmapDetector)xmapDetector;
		this.tfg = tfg;
		this.plot = plot;
		this.counts = counts;
		vortexData = new VortexData();
	}
	
	@Override
	public void plotData(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex) {
		plot.plot(detectorList.getSelectedIndex(), getMcaData(), false, null);
	}
	
	@Override
	public void updateStats(final GridListEditor detectorList, final DetectorElementComposite detectorElementComposite, final int currentSelectedElementIndex){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				detectorElementComposite.setEndMaximum(mcaData[0][0].length - 1);
				
				detectorElementComposite.setTotalElementCounts(counts.getTotalElementCounts(currentSelectedElementIndex, mcaData));
				detectorElementComposite.setTotalCounts(counts.getTotalCounts(mcaData));
				
				Double[] liveStats = null;
				try {
					liveStats = (Double[]) xmapDetector.getAttribute("countRates");
				} catch (DeviceException e) {
					logger.error("Problem getting attribute countRates from xmap", e);
				}
				if(liveStats!=null){
					double deadTimeFinal = (Math.abs(liveStats[0] - liveStats[1]) / liveStats[0]) * 100;
					deadTimeLabel.setValue(deadTimeFinal);
				}
			}
		});
	}
	
	@Override
	public void writeToDisk() throws IOException{
		String msg = "Error saving detector data to file";
		String detectorFile = "";
		try {
			String vortexSaveDir = PathConstructor.createFromProperty("gda.device.vortex.spoolDir");
			long snapShotNumber = new NumTracker("Vortex_snapshot").incrementNumber();
			String fileName = "vortex_snap_" + snapShotNumber+ ".mca";
			File filePath = new File(vortexSaveDir + "/" + fileName);
			detectorFile = filePath.getAbsolutePath();
			vortexData.save(mcaData, detectorFile);
			msg = "Saved: " + detectorFile;
			logger.info("Vortex snapshot saved to " + detectorFile);
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
		sashPlotFormComposite.appendStatus("Xspress snapshot saved to " + detectorFile, logger);
	}
	
	public void addLoadListener(final GridListEditor detectorGridList, final DetectorElementComposite detectorElementComposite, final List<DetectorElement> detectorList){
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					final String filePath = openDialog.open();
					if(filePath!=null){
						vortexData.load(openDialog, filePath, detectorList);
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								acquireFileLabel.setText("Loaded: " + filePath);
								detectorElementComposite.setEndMaximum((mcaData[0][0].length) - 1);
								plot.plot(detectorGridList.getSelectedIndex(), vortexData.getDetectorData(), false, null);
							}
						});
					}
				} catch (Exception e1) {
					logger.error("Cannot acquire vortex data", e1);
				}
			}
		});
	}
	
	@Override
	public void acquire(double collectionTime) throws DeviceException, InterruptedException{
		xmapDetector.clearAndStart();
		tfg.countAsync(collectionTime);
		xmapDetector.waitWhileBusy();
		xmapDetector.stop();
		int[][] data = xmapDetector.getData();
		mcaData = convert2DTo3DArray(data);
		sashPlotFormComposite.appendStatus("Collected data from detector successfully.", logger);
	}
	
	public int[][][] getMcaData() {
		return mcaData;
	}

	protected int[][][] convert2DTo3DArray(int[][] data) {
		int[][][] ret = new int[data.length][1][];
		for (int i = 0; i < data.length; i++)
			ret[i][0] = data[i];
		return ret;
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

		live = new Button(acquire, SWT.CHECK);
		live.setText("Live");
		live.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		live.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				autoSave.setEnabled(!live.getSelection());
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