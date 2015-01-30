/*
 * Copyright © 2014 Diamond Light Source Ltd. This file is part of GDA. GDA is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software
 * Foundation. GDA is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with GDA. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.exafs.ui.detector.xspress3;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.jython.accesscontrol.AccessDeniedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.ComboWrapper;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.client.experimentdefinition.ExperimentBeanManager;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.exafs.ui.composites.FluorescenceComposite;
import uk.ac.gda.exafs.ui.detector.DetectorEditor;
import uk.ac.gda.exafs.ui.detector.IDetectorROICompositeFactory;
import uk.ac.gda.exafs.ui.detector.vortex.VortexParametersUIEditor;
import uk.ac.gda.richbeans.editors.DirtyContainer;

import com.swtdesigner.SWTResourceManager;

public class Xspress3ParametersUIEditor extends DetectorEditor {
	private static final String XSPRESS3_EDITOR_DATA_XML_FILENAME = "xspress3_editor_data.xml";
	private String detectorName;
	protected Xspress3Parameters xspress3Parameters;
	private static final String GDA_DEVICE_VORTEX_SPOOL_DIR = "gda.device.vortex.spoolDir";

	private static final Logger logger = LoggerFactory.getLogger(Xspress3ParametersUIEditor.class);

	public Label acquireFileLabel;
	protected boolean writeToDisk = LocalProperties.check("gda.detectors.save.single.acquire");

	private FileDialog openDialog;

	private ComboWrapper countType;
	private Button autoSave;
	private Button live;
	private LabelWrapper deadTimeLabel;

	private ScaleBox acquireTime;
	private Composite acquire;
	private Button acquireBtn;
	private boolean autoSaveEnabled;
	private Label lblDeadTime;

	public Xspress3ParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean, "xspress3Config");
		this.xspress3Parameters = (Xspress3Parameters) editingBean;
		detectorName = xspress3Parameters.getDetectorName();
	}

	@Override
	protected String getRichEditorTabText() {
		return "Xspress3";
	}

	@Override
	public void createPartControl(Composite parent) {

		super.createPartControl(parent);

		final Composite left = sashPlotForm.getLeft();

		createAcquireSpectraPanel(parent, left);

		createROIPanel(left);

		sashPlotForm.setWeights(new int[] { 35, 74 });

		configureUI();
	}

	private void createROIPanel(final Composite left) {
		final Composite grid = new Composite(left, SWT.BORDER);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grid.setLayout(new GridLayout());

		List<DetectorElement> detectorList = xspress3Parameters.getDetectorList();
		if (detectorList.size() > 1) {
			final Composite buttonPanel = new Composite(grid, SWT.NONE);
			buttonPanel.setLayout(new GridLayout(2, false));

			final Label applyToAllLabel = new Label(buttonPanel, SWT.NONE);
			applyToAllLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			applyToAllLabel.setText("Apply To All Elements ");

			final Button applyToAllButton = new Button(buttonPanel, SWT.NONE);
			final GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.widthHint = 60;
			gridData.minimumWidth = 60;
			applyToAllButton.setLayoutData(gridData);
			applyToAllButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class,
					"/icons/camera_go.png"));
			applyToAllButton
					.setToolTipText("Apply current detector regions of interest to all other detector elements.");
			final SelectionAdapter applyToAllListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					applyToAll(true);
				}
			};
			applyToAllButton.addSelectionListener(applyToAllListener);

			Label sep = new Label(grid, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}

		final Label detectorElementsLabel = new Label(grid, SWT.NONE);
		detectorElementsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (detectorList.size() > 1) {
			detectorElementsLabel.setText(" Detector Element");
		} else {
			detectorElementsLabel.setText(" Regions of Interest");
		}

		try {
			IDetectorROICompositeFactory factory = Xspress3ParametersUIHelper.INSTANCE.getDetectorROICompositeFactory();
			createDetectorList(grid, DetectorElement.class, detectorList.size(), DetectorROI.class, factory, false);
			Xspress3ParametersUIHelper.INSTANCE.setDetectorListGridOrder(getDetectorList());
			getDetectorElementComposite().setWindowsEditable(false);
			getDetectorElementComposite().setMinimumRegions(Xspress3ParametersUIHelper.INSTANCE.getMinimumRegions());
			getDetectorElementComposite().setMaximumRegions(Xspress3ParametersUIHelper.INSTANCE.getMaximumRegions());

		} catch (Exception e1) {
			logger.error("Cannot create ui for Xspress3Parameters", e1);
		}
	}

	private void createAcquireSpectraPanel(Composite parent, final Composite left) {
		Group grpAcquire = new Group(left, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		grpAcquire.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpAcquire.setLayout(gridLayout);

		Button loadBtn = new Button(grpAcquire, SWT.NONE);
		loadBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadBtn.setText("Load");
		loadBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					LoadAcquireFromFile();
				} catch (Exception e1) {
					logger.error("Cannot acquire xspress data", e1);
				}
			}
		});

		acquire = new Composite(grpAcquire, SWT.NONE);
		final GridLayout gridLayoutAcq = new GridLayout();
		gridLayoutAcq.numColumns = 9;
		gridLayoutAcq.marginWidth = 0;
		acquire.setLayout(gridLayoutAcq);

		acquireBtn = new Button(acquire, SWT.NONE);
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/camera.png"));
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
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		getDetectorElementComposite().setIndividualElements(true);
	}

	/**
	 * Not called in UI thread. This needs to be protected if data is obtained from ui objects.
	 * 
	 * @param monitor
	 * @throws Exception
	 */
	@Override
	protected void acquire(final IProgressMonitor monitor, final double collectionTimeValue) throws Exception {

		int numWorkUnits = 2;

		if (monitor != null)
			monitor.beginTask("Acquiring Xspress3 snapshot...", numWorkUnits);

		FluorescenceDetector theDetector = getDetector(detectorName);
		if (theDetector == null)
			throw new Exception("Unable to find Xspress3Detector called :'" + detectorName + "'");

		try {

			final double[][] theData = theDetector.getMCData(collectionTimeValue);
			storeDataInWrapper(theData);

			if (monitor != null) {
				monitor.worked(1);
			}

			int numDetectorChannels = theData.length;
			int mcaLength = theData[0].length;
			detectorData = new double[numDetectorChannels][1][mcaLength];
			for (int channel = 0; channel < numDetectorChannels; channel++) {
				for (int mcaChan = 0; mcaChan < mcaLength; mcaChan++) {
					detectorData[channel][0][mcaChan] = theData[channel][mcaChan];
				}
			}

			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					getDetectorElementComposite().setEndMaximum(detectorData[0][0].length - 1);
					plot(getDetectorList().getSelectedIndex(), true);
					setEnabled(true);
					lblDeadTime.setVisible(true);
					deadTimeLabel.setVisible(true);
					sashPlotForm.getLeft().layout();
				}
			});

			if (monitor != null) {
				monitor.worked(1);
				sashPlotForm.appendStatus("Collected data from detector successfully.", logger);
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
					MessageDialog.openWarning(getSite().getShell(), "Cannot operate detector",
							"You do not hold the baton and so cannot operate the detector.");
				}
			});
			sashPlotForm
					.appendStatus("Cannot read out detector data. Check the log and inform beamline staff.", logger);
			return;
		} catch (DeviceException e) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openWarning(getSite().getShell(), "Cannot read out detector data",
							"Problem acquiring data. See log for details.");
				}
			});
			sashPlotForm.appendStatus(
					"Cannot get Xspress3 data from Vortex detector. Check the log and inform beamline staff.", logger);
			return;
		}

		if (writeToDisk && autoSaveEnabled && monitor != null) {
			String msg = "Error saving detector data to file";
			String spoolFilePath = "";
			try {
				String spoolDirPath = PathConstructor.createFromProperty(GDA_DEVICE_VORTEX_SPOOL_DIR);
				if (spoolDirPath == null || spoolDirPath.length() == 0)
					throw new Exception("Error saving data. Xspress3 device spool dir is not defined in property "
							+ GDA_DEVICE_VORTEX_SPOOL_DIR);

				long snapShotNumber = new NumTracker("Xspress3_snapshot").incrementNumber();
				String fileName = "xspress3_snap_" + snapShotNumber + ".mca";
				File filePath = new File(spoolDirPath + "/" + fileName);
				spoolFilePath = filePath.getAbsolutePath();
				save(detectorData, spoolFilePath);
				msg = "Saved: " + spoolFilePath;
				logger.info("Xspress3 snapshot saved to " + spoolFilePath);
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
		if (monitor != null) {
			monitor.done();
		}
	}

	private void storeDataInWrapper(double[][] theData) {
		// first convert to a 3D int array to match with the Xspress2 editor
		final int[][][] ret = new int[theData.length][1][theData[0].length];
		for (int i = 0; i < theData.length; i++) {
			for (int mcaChan = 0; mcaChan < theData[0].length; mcaChan++){
				ret[i][0][mcaChan] = (int) Math.round(theData[i][mcaChan]);// Int array is [element][grade (1, 2 or all 16)][mca channel]
			}
		}
		getDataWrapper().setValue(ElementCountsData.getDataFor(ret));
	}

	protected int[][][] get3DArray(int[][] data) {
		final int[][][] ret = new int[data.length][1][];
		for (int i = 0; i < data.length; i++) {
			ret[i][0] = data[i];
		}
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
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/stop.png"));
		autoSave.setEnabled(false);
		autoSaveEnabled = false;
		acquireFileLabel.setText("										");
		live.setEnabled(false);
	}

	@Override
	public void acquireFinished() {
		acquireBtn.setText("Acquire");
		acquireBtn.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/camera.png"));
		autoSave.setEnabled(false);
		autoSaveEnabled = false;
		live.setEnabled(true);
	}

	@Override
	public void notifyFileSaved(File file) {
		@SuppressWarnings("unchecked")
		final FluorescenceComposite comp = (FluorescenceComposite) BeanUI.getBeanField("fluorescenceParameters",
				DetectorParameters.class);
		if (comp == null || comp.isDisposed()) {
			return;
		}
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
		return ExperimentBeanManager.INSTANCE.getXmlCommandHandler(Xspress3Parameters.class);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected void LoadAcquireFromFile() {
		String dataDir = PathConstructor.createFromDefaultProperty();
		dataDir += "processing";
		openDialog.setFilterPath(dataDir);
		final String filePath = openDialog.open();
		if (filePath != null) {
			final String msg = ("Loading map from " + filePath);
			Job job = new Job(msg) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					BufferedReader reader = null;
					try {

						reader = new BufferedReader(new FileReader(filePath));
						String line = reader.readLine();
						ArrayList<double[]> data = new ArrayList<double[]>();
						while (line != null) {
							StringTokenizer tokens = new StringTokenizer(line);
							double elementData[] = new double[tokens.countTokens()];
							for (int i = 0; i < elementData.length; i++) {
								elementData[i] = Double.parseDouble(tokens.nextToken());
							}
							data.add(elementData);
							line = reader.readLine();
						}
						// find the res grade

						int resGrade = data.size() / xspress3Parameters.getDetectorList().size();
						detectorData = new double[xspress3Parameters.getDetectorList().size()][resGrade][];
						int dataIndex = 0;
						// Int array above is [element][grade (1, 2 or all 16)][mca channel]
						for (int i = 0; i < detectorData.length; i++) {
							for (int j = 0; j < resGrade; j++) {
								detectorData[i][j] = data.get(dataIndex++);
							}
						}

						getSite().getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								acquireFileLabel.setText("Loaded: " + filePath);
								getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
								plot(getDetectorList().getSelectedIndex(), false);
								setEnabled(true);
							}
						});
					} catch (Exception e) {
						logger.warn("Exception whilst loading map", e);
					} finally {
						if (reader != null) {
							try {
								reader.close();
							} catch (IOException e) {
								// don't report
							}
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	@Override
	protected String getDataXMLName() {
		String varDir = LocalProperties.get(LocalProperties.GDA_VAR_DIR);
		return varDir + "/" + XSPRESS3_EDITOR_DATA_XML_FILENAME;
	}

	private final FluorescenceDetector getDetector(String detectorName) {
		FluorescenceDetector detector = (FluorescenceDetector) Finder.getInstance().find(detectorName);
		return detector;
	}

	@Override
	public void dispose() {
		if (countType != null)
			countType.dispose();
		autoSave.dispose();
		live.dispose();
		acquireTime.dispose();
		acquire.dispose();
		acquireFileLabel.dispose();
		acquireBtn.dispose();
		super.dispose();
	}

	@Override
	protected String getDetectorName() {
		return xspress3Parameters.getDetectorName();
	}

}
