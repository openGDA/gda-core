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

package uk.ac.gda.exafs.ui.detectors.composites;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.xspress.ResGrades;
import gda.device.detector.xspress.XspressDetector;
import gda.factory.Finder;
import gda.jython.accesscontrol.AccessDeniedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.DetectorElementComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.wrappers.ComboAndNumberWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;

import com.swtdesigner.SWTResourceManager;

public class XspressAcquireComposite extends Composite implements AcquireComposite {

	private static final Logger logger = LoggerFactory.getLogger(XspressAcquireComposite.class);
	private static final String GDA_DEVICE_XSPRESS_SPOOL_DIR = "gda.device.xspress.spoolDir";

	
	// Used for temporary storage of data
	protected volatile double[/* element */][/* grade */][/* mca */] detectorData;

	
	private Composite acquireComposite;
	private ScaleBox acquireTime;
	private Button autoSave;
	private boolean writeToDisk;
	private Label acquireFileLabel;
	private FileDialog openDialog;
	private String uiReadoutMode;
	private String uiResGrade;
	private ComboAndNumberWrapper resGrade;
	private XspressParameters xspressParameters;

	public XspressAcquireComposite(Composite parent, int style, XspressParameters xspressParameters) {
		super(parent, style);
		this.xspressParameters = xspressParameters;
		
		acquireComposite = new Composite(parent, SWT.NONE);
		final GridLayout gridLayoutAcq = new GridLayout();
		gridLayoutAcq.numColumns = 5;
		gridLayoutAcq.marginWidth = 0;
		acquireComposite.setLayout(gridLayoutAcq);

		Button acquireBtn = new Button(acquireComposite, SWT.NONE);
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/application_side_expand.png"));
		acquireBtn.setText("Acquire");
		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					singleAcquire();
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
			}
		});

		acquireTime = new ScaleBox(acquireComposite, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");
		acquireTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		autoSave = new Button(acquireComposite, SWT.CHECK);
		autoSave.setText("Save on Acquire");
		autoSave.setSelection(writeToDisk);
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

		acquireFileLabel = new Label(acquireComposite, SWT.NONE);
		acquireFileLabel.setText("										");
		acquireFileLabel.setToolTipText("The file path for the acquire data");
		acquireFileLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		openDialog = new FileDialog(parent.getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));
	}
	
	
	protected void singleAcquire() throws Exception {

		final double time = getDetectorCollectionTime();
		// FIXME to implement
//		IProgressService service = (IProgressService) getSite().getService(IProgressService.class);
//		service.run(true, true, new IRunnableWithProgress() {
//
//			@Override
//			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//				try {
//					acquire(monitor, time);
//				} catch (Exception e) {
//					logger.error("Error performing single acquire", e);
//				}
//			}
//		});
	}

	protected double getDetectorCollectionTime() {
		return acquireTime.getNumericValue(); // convert to ms
	}

	protected void acquire(IProgressMonitor monitor, double collectionTime) {

		if (monitor != null)
			monitor.beginTask("Acquire xspress data", 100);

		// Get detector
		final XspressDetector xsDetector = Finder.getInstance().find(xspressParameters.getDetectorName());

		String resGrade_orig;
		String readoutMode_orig;
		try {
			resGrade_orig = xsDetector.getResGrade();
			readoutMode_orig = xsDetector.getReadoutMode();
		} catch (DeviceException e1) {
			logger.error("Cannot get current res grade", e1);
			return;
		}

		// Get res grade for calibration.
		getShell().getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				uiReadoutMode = (String) getReadoutMode().getValue();
				uiResGrade = getResGradeAllowingForReadoutMode();
			}

		});

		appendStatus("Collecting a single frame of MCA data with resolution grade set to '" + uiResGrade
				+ "'.", logger);


		try {
			xsDetector.setAttribute("readoutModeForCalibration", new String[] { uiReadoutMode, uiResGrade });

			// Get MCA Data
			final int[][][] data = xsDetector.getMCData((int) collectionTime);
			// Int array above is [element][grade (1, 2 or all 16)][mca channel]

			// TODO do we need this line?
//			getDataWrapper().setValue(ElementCountsData.getDataFor(data));
//			this.dirtyContainer.setDirty(true);
			detectorData = getData(data);
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					// FIXME need to re-implement?
//					calculateAndPlotCountTotals(showIndividualElements.getValue());
				}
			});

			if (writeToDisk) {
				String spoolDirPath = PathConstructor.createFromProperty(GDA_DEVICE_XSPRESS_SPOOL_DIR);
				if (spoolDirPath == null || spoolDirPath.length() == 0)
					throw new Exception("Error saving data. Xspress device spool dir is not defined in property "
							+ GDA_DEVICE_XSPRESS_SPOOL_DIR);
				long snapShotNumber = new NumTracker("Xspress_snapshot").incrementNumber();
				String fileName = "xspress_snap_" + snapShotNumber + ".mca";
				final File filePath = new File(spoolDirPath + "/" + fileName);
				save(detectorData, filePath.getAbsolutePath());
				appendStatus("Xspress snapshot saved to " + filePath, logger);
				getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						acquireFileLabel.setText("Saved: " + filePath.getAbsolutePath());
					}
				});
			}

			if (monitor != null)
				monitor.done();
			appendStatus("Collected data from detector successfully.", logger);

		} catch (IllegalArgumentException e) {
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog
							.openWarning(getShell(), "Cannot write out detector data",
									"The Java property gda.device.xspress.spoolDir has not been defined or is invalid. Contact Data Acquisition.");
				}
			});
			logger.error("Cannot read out detector data.", e);
			return;
		} catch (AccessDeniedException e) {
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getShell(), "Cannot operate detector",
							"You do not hold the baton and so cannot operate the detector.");
				}
			});
			appendStatus("Cannot read out detector data. Check the log and inform beamline staff.", logger);
			return;
		} catch (Exception e) {
			getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getShell(), "Cannot read out detector data",
							"Problem acquiring data. See log for details.");
				}
			});
			appendStatus("Cannot read out detector data. Check the log and inform beamline staff.", logger);
			return;
		} finally {
			try {
				xsDetector.setResGrade(resGrade_orig);
				xsDetector.setReadoutMode(readoutMode_orig);
			} catch (DeviceException e) {
				appendStatus("Cannot reset res grade, detector may be in an error state.", logger);
			}
			appendStatus("Reset detector to resolution grade '" + resGrade_orig + "'.", logger);
		}

		// Note: currently has to be in this order.
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
				plot(getDetectorList().getSelectedIndex(),true);
				setEnabled(true);
			}
		});
	}

	private String getResGradeAllowingForReadoutMode() {

		final String uiReadoutMode = (String) getReadoutMode().getValue();
		return uiReadoutMode.equals(XspressDetector.READOUT_ROIS) ? (String) getResGrade().getValue() : ResGrades.NONE;
	}
	
	public ComboWrapper getReadoutMode() {
		// FIXME make a connection to the mode composite
		return null;
	}

	private void appendStatus(String string, Logger logger2) {
		// TODO Auto-generated method stub
		
	}
	
	public GridListEditor getDetectorList() {
		return getDetectorListComposite().getDetectorList();
	}
	
	private DetectorEditor getDetectorListComposite() {
		// TODO Auto-generated method stub
		return null;
	}


	public DetectorElementComposite getDetectorElementComposite() {
		if ( getDetectorListComposite() == null) {
			return null;
		}
		return  getDetectorListComposite().getDetectorElementComposite();
	}


	// could be in a base class?
	protected double[][][] getData(int[][][] int_data) {
		double[][][] data = new double[int_data.length][int_data[0].length][int_data[0][0].length];
		for (int i = 0; i < int_data.length; i++) {
			for (int j = 0; j < int_data[i].length; j++) {
				for (int k = 0; k < int_data[i][j].length; k++) {
					data[i][j][k] = int_data[i][j][k];
				}
			}
		}
		return data;
	}

	
	// could be in a base class?
	public void save(double[][][] data, String filePath) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

			StringBuffer toWrite = new StringBuffer();
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					for (int k = 0; k < data[0][0].length; k++) {
						toWrite.append(data[i][j][k] + "\t");
					}
					writer.write(toWrite.toString() + "\n");
					toWrite = new StringBuffer();
				}
			}
			writer.close();
		} catch (IOException e) {
			logger.warn("Exception writing acquire data to xml file", e);
		}
	}

	// could be in a base class?
	private String plotTitle = "Saved Data";
	protected void plot(final int ielement, boolean updateTitle) {
		final List<AbstractDataset> data = unpackDataSets(ielement);

		if (updateTitle) {
			Date now = new Date();
			SimpleDateFormat dt = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
			plotTitle = "Acquire at " + dt.format(now);
		}
		
		for (int i = 0; i < data.size(); i++) {
			String name = getChannelName(ielement);
			if (data.size() > 1){
				name += " " + i;
			}
			name += " " + plotTitle;
			data.get(i).setName(name);
		}

		//FIXME make a connection to the DetectorPlotter composite
//		sashPlotForm.setDataSets(data.toArray(new AbstractDataset[data.size()]));
//		sashPlotForm.plotData();
//		sashPlotForm.getPlottingSystem().setTitle(plotTitle);
//		calculateAndPlotCountTotals(true);
	}

	protected String getChannelName(int iChannel) {
		return "" + iChannel;
	}

	public ComboWrapper getResGrade() {
		return resGrade;
	}
	
	protected List<AbstractDataset> unpackDataSets(int ielement) {

		final List<AbstractDataset> ret = new ArrayList<AbstractDataset>(7);
		if (ielement < 0 || detectorData == null) {
			DoubleDataset ds = new DoubleDataset(new double[] { 0d });
			ret.add(ds);
			return ret;
		}

		final double[][] data = detectorData[ielement];
		for (int i = 0; i < data.length; i++) {
			DoubleDataset ds = new DoubleDataset(data[i]);
			ret.add(ds);
		}
		return ret;
	}


}
