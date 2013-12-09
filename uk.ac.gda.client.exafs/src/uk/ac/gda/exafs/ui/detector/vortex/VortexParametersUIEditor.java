/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
import gda.factory.Finder;
import gda.jython.accesscontrol.AccessDeniedException;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;

import com.swtdesigner.SWTResourceManager;

public class VortexParametersUIEditor extends DetectorEditor {
	private static final String GDA_DEVICE_VORTEX_SPOOL_DIR = "gda.device.vortex.spoolDir";
	private static final Logger logger = LoggerFactory.getLogger(VortexParametersUIEditor.class);
	public Label acquireFileLabel;
	protected VortexParameters vortexParameters;
	protected boolean writeToDisk = LocalProperties.check("gda.detectors.save.single.acquire");
	private FileDialog openDialog;
	private ComboWrapper countType;
	private Button autoSave;
	private Button live;
	private BooleanWrapper saveRawSpectrum;
	private LabelWrapper deadTimeLabel;
	private ScaleBox acquireTime;
	private Composite acquire;
	private Button acquireBtn;
	private boolean autoSaveEnabled;
	private Label lblDeadTime;
	private VortexData vortexData;
	
	public VortexParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "vortexConfig");
		vortexParameters = (VortexParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Vortex";
	}

	@Override
	protected String getDetectorName() {
		return vortexParameters.getDetectorName();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		vortexData = new VortexData();
		Composite left = sashPlotFormComposite.getLeft();
		createAcquireSpectraPanel(parent, left);
		createROIPanel(left);
		sashPlotFormComposite.setWeights(new int[] { 35, 74 });
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OUTPUT_IN_OUTPUT_PARAMETERS))
			addOutputPreferences(left);
		configureUI();
	}

	private void createROIPanel(final Composite left) {
		Composite grid = new Composite(left, SWT.BORDER);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grid.setLayout(new GridLayout());
		List<DetectorElement> detectorList = vortexParameters.getDetectorList();
		if (detectorList.size() > 1) {
			Composite buttonPanel = new Composite(grid, SWT.NONE);
			buttonPanel.setLayout(new GridLayout(2, false));
			Label applyToAllLabel = new Label(buttonPanel, SWT.NONE);
			applyToAllLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			applyToAllLabel.setText("Apply To All Elements ");
			Button applyToAllButton = new Button(buttonPanel, SWT.NONE);
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.widthHint = 60;
			gridData.minimumWidth = 60;
			applyToAllButton.setLayoutData(gridData);
			applyToAllButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class, "/icons/camera_go.png"));
			applyToAllButton.setToolTipText("Apply current detector regions of interest to all other detector elements.");
			SelectionAdapter applyToAllListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					applyToAll(true);
				}
			};
			applyToAllButton.addSelectionListener(applyToAllListener);
			Label sep = new Label(grid, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		Label detectorElementsLabel = new Label(grid, SWT.NONE);
		detectorElementsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (detectorList.size() > 1)
			detectorElementsLabel.setText(" Detector Element");
		else
			detectorElementsLabel.setText(" Regions of Interest");
		try {
			IDetectorROICompositeFactory factory = VortexParametersUIHelper.INSTANCE.getDetectorROICompositeFactory();
			createDetectorList(grid, DetectorElement.class, detectorList.size(), RegionOfInterest.class, factory, false);
			VortexParametersUIHelper.INSTANCE.setDetectorListGridOrder(getDetectorList());
			getDetectorElementComposite().setWindowsEditable(false);
			getDetectorElementComposite().setMinimumRegions(VortexParametersUIHelper.INSTANCE.getMinimumRegions());
			getDetectorElementComposite().setMaximumRegions(VortexParametersUIHelper.INSTANCE.getMaximumRegions());
		} catch (Exception e1) {
			logger.error("Cannot create ui for VortexParameters", e1);
		}
	}

	private void createAcquireSpectraPanel(Composite parent, final Composite left) {
		Group grpAcquire = new Group(left, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		grpAcquire.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpAcquire.setLayout(gridLayout);
		Button loadBtn = new Button(grpAcquire, SWT.NONE);
		loadBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadBtn.setText("Load");
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					final String filePath = openDialog.open();
					vortexData.load(openDialog, vortexParameters, filePath);
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							acquireFileLabel.setText("Loaded: " + filePath);
							getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
							plot(getDetectorList().getSelectedIndex(),false);
							setWindowsEnabled(true);
						}
					});
				} catch (Exception e1) {
					logger.error("Cannot acquire vortex data", e1);
				}
			}
		});
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

		acquireBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					if (!writeToDisk || !autoSaveEnabled)
						acquireFileLabel.setText("										");
					if (!live.getSelection())
						singleAcquire();
					else
						continuousAcquire();
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
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

	private void addOutputPreferences(Composite comp) {
		Group xspressParametersGroup = new Group(comp, SWT.NONE);
		xspressParametersGroup.setText("Output Preferences");
		xspressParametersGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		xspressParametersGroup.setLayout(gridLayout);
		saveRawSpectrum = new BooleanWrapper(xspressParametersGroup, SWT.NONE);
		saveRawSpectrum.setText("Save raw spectrum to file");
		saveRawSpectrum.setValue(false);
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		getDetectorElementComposite().setIndividualElements(true);
	}


	protected void acquire(IProgressMonitor monitor, double collectionTimeValue) throws Exception {
		int loopSleepTimeInMillis = 100;
		int numWorkUnits = (int) Math.round((collectionTimeValue * 1000 / loopSleepTimeInMillis) / 1000);
		if (monitor != null)
			numWorkUnits += 5; // for the extra steps
		if (monitor != null)
			monitor.beginTask("Acquire xMap data", numWorkUnits);
		String detectorName = vortexParameters.getDetectorName();
		XmapDetector xmapDetector = (XmapDetector) Finder.getInstance().find(detectorName);
		if (xmapDetector == null)
			throw new Exception("Unable to find Xmapdetector called :'" + detectorName + "'");
		String tfgName = vortexParameters.getTfgName();
		final Timer tfg = (Timer) Finder.getInstance().find(tfgName);
		if (tfg == null)
			throw new Exception("Unable to find tfg called :'" + tfgName + "'");
		try {
			xmapDetector.clearAndStart();
			if (monitor != null)
				monitor.worked(1);
			tfg.countAsync(collectionTimeValue);
			if (monitor != null)
				monitor.worked(10);
			while (tfg.getStatus() == Timer.ACTIVE) {
				try {
					Thread.sleep(loopSleepTimeInMillis);
					if (monitor != null) {
						if (monitor.isCanceled()) {
							xmapDetector.stop();
							return;
						}
						monitor.worked(1);
					}
				} catch (InterruptedException e) {
				}
			}
			if (monitor != null)
				if (monitor.isCanceled())
					return;
			if (monitor != null)
				logger.debug("Stopping xmap detector " + tfg.getStatus());
			xmapDetector.stop();
			xmapDetector.waitWhileBusy();
			if (monitor != null)
				monitor.worked(1);
			int[][] data = xmapDetector.getData();
			if (monitor != null)
				monitor.worked(1);
			int[][][] data3d = get3DArray(data);
			getDataWrapper().setValue(ElementCountsData.getDataFor(data3d));
			detectorData = getData(data3d);
			if (monitor != null)
				monitor.worked(1);
			// returns the icr and ocr
			Double[] liveStats = (Double[]) xmapDetector.getAttribute("countRates");
			final double deadTimeFinal = (Math.abs(liveStats[0] - liveStats[1]) / liveStats[0]) * 100;
			
			// Note: currently has to be in this order.
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					getDetectorElementComposite().setEndMaximum(detectorData[0][0].length - 1);
					plot(getDetectorList().getSelectedIndex(),true);
					setWindowsEnabled(true);
					deadTimeLabel.setValue(deadTimeFinal);
					lblDeadTime.setVisible(true);
					deadTimeLabel.setVisible(true);
					sashPlotFormComposite.getLeft().layout();
				}
			});
			
			if (monitor != null) {
				monitor.worked(1);
				sashPlotFormComposite.appendStatus("Collected data from detector successfully.", logger);
			}
			
		} catch (IllegalArgumentException e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog
							.openWarning(getSite().getShell(), "Cannot write out detector data",
									"The Java property gda.device.vortex.spoolDir has not been defined or is invalid. Contact Data Acquisition.");
				}
			});
			logger.error("Cannot read out detector data.", e);
			return;
		} catch (AccessDeniedException e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getSite().getShell(), "Cannot operate detector", "You do not hold the baton and so cannot operate the detector.");
				}
			});
			sashPlotFormComposite
					.appendStatus("Cannot read out detector data. Check the log and inform beamline staff.", logger);
			return;
		} catch (DeviceException e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getSite().getShell(), "Cannot read out detector data", "Problem acquiring data. See log for details.");
				}
			});
			sashPlotFormComposite.appendStatus(
					"Cannot get xMap data from Vortex detector. Check the log and inform beamline staff.", logger);
			return;
		}

		if (writeToDisk && autoSaveEnabled && monitor != null) {
			String msg = "Error saving detector data to file";
			String spoolFilePath = "";
			try {
				String spoolDirPath = PathConstructor.createFromProperty(GDA_DEVICE_VORTEX_SPOOL_DIR);
				if (spoolDirPath == null || spoolDirPath.length() == 0)
					throw new Exception("Error saving data. Vortex device spool dir is not defined in property " + GDA_DEVICE_VORTEX_SPOOL_DIR);
				long snapShotNumber = new NumTracker("Vortex_snapshot").incrementNumber();
				String fileName = "vortex_snap_" + snapShotNumber+ ".mca";
				File filePath = new File(spoolDirPath + "/" + fileName);
				spoolFilePath = filePath.getAbsolutePath();
				plotData.save(detectorData, spoolFilePath);
				msg = "Saved: " + spoolFilePath;
				logger.info("Vortex snapshot saved to " + spoolFilePath);
			} finally {
				final String msgFinal = msg;
				getSite().getShell().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						acquireFileLabel.setText(msgFinal);
					}
				});
			}
		}
		if (monitor != null)
			monitor.done();
	}

	protected int[][][] get3DArray(int[][] data) {
		int[][][] ret = new int[data.length][1][];
		for (int i = 0; i < data.length; i++)
			ret[i][0] = data[i];
		return ret;
	}

	/**
	 * Cannot be called in non-ui thread.
	 */
	@Override
	protected double getDetectorCollectionTime() {
		return (Double) getCollectionTime().getValue();
	}

	/**
	 * Cannot be called in non-ui thread.
	 */
	@Override
	protected long getAcquireWaitTime() {
		return Math.round((Double) getCollectionTime().getValue() * 0.1d);
	}

	@Override
	public void acquireStarted() {
		acquireBtn.setText("Stop");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/stop.png"));
		autoSave.setEnabled(false);
		autoSaveEnabled = false;
		acquireFileLabel.setText("										");
		live.setEnabled(false);
	}

	@Override
	public void acquireFinished() {
		acquireBtn.setText("Acquire");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/application_side_expand.png"));
		autoSave.setEnabled(false);
		autoSaveEnabled = false;
		live.setEnabled(true);
	}

	@Override
	public void notifyFileSaved(File file) {
		@SuppressWarnings("unchecked")
		final FluorescenceComposite comp = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters", DetectorParameters.class);
		if (comp == null || comp.isDisposed())
			return;
		comp.getDetectorType().setValue("Silicon");
		comp.getConfigFileName().setValue(file.getAbsolutePath());
	}

	public ScaleBox getCollectionTime() {
		return acquireTime;
	}

	public ComboWrapper getCountType() {
		return countType;
	}

	@Override
	public XMLCommandHandler getXMLCommandHandler() {
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(VortexParameters.class);
	}

	public BooleanWrapper getSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/vortex_editor_data.xml";
	}

	@Override
	public void dispose() {
		if (countType != null)
			countType.dispose();
		autoSave.dispose();
		live.dispose();
		saveRawSpectrum.dispose();
		acquireTime.dispose();
		acquire.dispose();
		acquireFileLabel.dispose();
		acquireBtn.dispose();
		super.dispose();
	}
	
}