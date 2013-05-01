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

package uk.ac.gda.exafs.ui.detector;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.factory.Finder;
import gda.jython.gui.JythonGuiConstants;
import gda.jython.scriptcontroller.ScriptExecutor;
import gda.jython.scriptcontroller.corba.impl.ScriptcontrollerAdapter;
import gda.observable.IObserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.plotting.jreality.util.PlotColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.detector.vortex.VortexParametersUIEditor;
import uk.ac.gda.exafs.ui.detector.wizards.ImportROIWizard;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.data.DataWrapper;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import uk.ac.gda.richbeans.components.selector.GridListEditor;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

import com.swtdesigner.SWTResourceManager;

/**
 * Class to contain plotting which some detector editors require.
 */
public abstract class DetectorEditor extends RichBeanEditorPart {

	/*
	 * name of ScriptController that must be in the system for uploading to device to work
	 */
	private static final String EXAFS_SCRIPT_OBSERVER = "ExafsScriptObserver";
	private static final Logger logger = LoggerFactory.getLogger(DetectorEditor.class);
	protected boolean saveToExtension = LocalProperties.check("gda.xml.save.extension");
	protected SashFormPlotComposite sashPlotForm;

	// Used for temporary storage of data
	protected volatile double[/* element */][/* grade */][/* mca */] detectorData;
	// NOTE: Grades often not applicable in which case that dimension is size 1.

	// Used for saving data to XML
	protected DataWrapper dataWrapper;

	// The command run to send the parameters to the server
	protected final String command;

	private ExpansionAdapter expansionListener;

	private volatile boolean continuousAquire = false;
	private Thread continuousThread;
	private ReentrantLock lock = new ReentrantLock();
	private DetectorListComposite detectorListComposite;
	private Composite importComposite;
	private Boolean calculateSingleElement = true;

	int selectedRegionIndex;
	int lastSelectedElementIndex;
	private Object bean;
	private Action uploadAction;
	private volatile Boolean updatingAfterROIDrag = null;

	public DetectorEditor(final String path, final URL mappingURL, final DirtyContainer dirtyContainer,
			final Object editingBean, final String command) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.command = command;
		this.bean = editingBean;
	}

	protected abstract Logger getLogger();

	/**
	 * monitor can be null if the task is not being monitored.
	 * 
	 * @param monitor
	 * @throws Exception
	 */
	protected abstract void acquire(final IProgressMonitor monitor, final double collectionTimeValue) throws Exception;

	/**
	 * @return the time that is waited during the acquisition loop. Called in the display thread.
	 */
	protected abstract double getDetectorCollectionTime();

	/**
	 * @return the time that is waited during the acquisition loop. Called in the display thread.
	 */
	protected abstract long getAcquireWaitTime();

	/**
	 * Name of the Detector which should be findable.
	 * 
	 * @return detector.
	 */
	protected abstract String getDetectorName();

	/**
	 * @return the copier which can get the template file from the server.
	 */
	public abstract XMLCommandHandler getXMLCommandHandler();

	protected abstract void LoadAcquireFromFile();

	/**
	 * Called when acquire started in display thread.
	 */
	public abstract void acquireStarted();

	/**
	 * Called when acquire finished in display thread.
	 */
	public abstract void acquireFinished();

	/**
	 * @return - String- the full path of the xml where the acquired data is persisted.
	 */
	protected abstract String getDataXMLName();

	@Override
	public void createPartControl(Composite parent) {
		this.dataWrapper = readStoredData();
		try {
			this.sashPlotForm = createSashPlot(parent);
		} catch (Exception e) {
			logger.error("Exception while creating detector editor", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		writeStoredData(monitor);
		super.doSave(monitor);
	}

	/**
	 * Override to ass custom actions.
	 * 
	 * @param parent
	 * @return SashFormPlotComposite
	 * @throws Exception 
	 */
	protected SashFormPlotComposite createSashPlot(Composite parent) throws Exception {
		return new SashFormPlotComposite(parent, this,new RegionSynchronizer(),   createUpLoadAction());
	}

	protected Action createUpLoadAction() {
		this.uploadAction = new Action("Configure") {
			@Override
			public void run() {
				try {
					doSave(new NullProgressMonitor());
					upload();
				} catch (Exception ne) {
					logger.error("Cannot configure Detector", ne);
				}
			}
		};
		uploadAction.setEnabled(false);
		uploadAction.setText("Configure");
		uploadAction.setToolTipText("Applies the configuration settings to the detector.");
		return uploadAction;
	}

	/**
	 * Call to select particular detector element.
	 * 
	 * @param index
	 */
	public void _testSetSelectedElement(final int index) {
		getDetectorList().setSelectedIndex(index);
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);

		if (this.getDataWrapper().getValue() != null) {
			this.detectorData = getData(ElementCountsData.getDataFrom(getDataWrapper().getValue()));
			if (detectorData != null) {
				getDetectorElementComposite().setEndMaximum((detectorData[0][0].length) - 1);
			}
			plot(0);
			setEnabled(true);
		} else {
			plot(-1);
			setEnabled(false);
		}
	}

	// There has been a long standing work item to reduce how many parameters this method takes.
	// While there are still many parameters, at least they are now unique types so it is much
	// easier to use the method
	protected DetectorListComposite createDetectorList(final Composite parent,
			final Class<? extends IDetectorElement> editorClass, final int elementListSize,
			final Class<? extends DetectorROI> regionClass, final IDetectorROICompositeFactory regionEditorFactory, final Boolean showAdvanced) {

		importComposite = new Composite(parent, SWT.NONE);
		{
			importComposite.setLayout(new GridLayout(2, false));
			GridDataFactory.fillDefaults().grab(true, false).applyTo(importComposite);

			final Label importLabel = new Label(importComposite, SWT.NONE);
			importLabel.setText("Import Regions Of Interest");
			final Button importButton = new Button(importComposite, SWT.NONE);
			GridDataFactory grab = GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false);
			grab.hint(60, SWT.DEFAULT).applyTo(importButton);
			importButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class, "/icons/calculator_edit.png"));
			importButton.setToolTipText("Import Regions Of Interest from other Parameters files");
			final SelectionAdapter importButtonListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					WizardDialog dialog = new WizardDialog(importButton.getShell(), new ImportROIWizard(
							elementListSize, DetectorEditor.this));
					dialog.create();
					dialog.open();
				}
			};
			importButton.addSelectionListener(importButtonListener);
		}

		detectorListComposite = new DetectorListComposite(parent, editorClass, elementListSize, regionClass,
				regionEditorFactory, showAdvanced);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detectorListComposite);
		expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				sashPlotForm.getLeftScroll().setMinSize(sashPlotForm.getLeft().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		};
		detectorListComposite.addExpansionListener(expansionListener);

		getDetectorElementComposite().getRegionList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {

				if (getDetectorList().getSelectedIndex() == lastSelectedElementIndex) {
					System.out.println(getDetectorList().getSelectedIndex());
					System.out.println(lastSelectedElementIndex);
					selectedRegionIndex = evt.getSelectionIndex();
					if (bean instanceof XspressParameters) {
						XspressParameters detBean = (XspressParameters) bean;
						detBean.setSelectedRegionNumber(evt.getSelectionIndex());
					}
					else if(bean instanceof VortexParameters){
						VortexParameters detBean = (VortexParameters) bean;
						detBean.setSelectedRegionNumber(evt.getSelectionIndex());
					}
				}

				lastSelectedElementIndex = getDetectorList().getSelectedIndex();
				updateROIAfterElementCompositeChange();
			}
		});

		getDetectorList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				plot(evt.getSelectionIndex());
				if (bean instanceof XspressParameters) {
					XspressParameters xspress = (XspressParameters) bean;
					getDetectorElementComposite().getRegionList().setSelectedIndex(xspress.getSelectedRegionNumber());
				}
				else if (bean instanceof VortexParameters) {
					VortexParameters vortex = (VortexParameters) bean;
					getDetectorElementComposite().getRegionList().setSelectedIndex(vortex.getSelectedRegionNumber());
				}
			}
		});

		if (!ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED)) {
			getDetectorElementComposite().getEnableDragRegions().addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					sashPlotForm.getRegionOnDisplay().setMobile(getDetectorElementComposite().getEnableDragRegions()
							.getSelection());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		return detectorListComposite;
	}

	@Override
	public void dispose() {
		if (detectorListComposite != null && !detectorListComposite.isDisposed()) {
			detectorListComposite.removeExpansionListener(expansionListener);
		}
		if (sashPlotForm != null)
			sashPlotForm.dispose();
		if (autoApplyToAllListener != null) {
			autoApplyToAll(false); // remove all auto-apply to all listeners
		}
		super.dispose();
	}

	/**
	 * Call to add a region of interest programatically.
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void _testAddRegionOfInterest(final String name) throws Exception {
		getDetectorElementComposite().getRegionList().addBean();
		getDetectorElementComposite().getRegionList().setField("roiName", name);
	}

	/**
	 * Call to add a region of interest programatically.
	 */
	public void _testDeleteRegionOfInterest() {
		getDetectorElementComposite().getRegionList().deleteBean();
	}

	/**
	 * Call to add a region of interest programatically.
	 * 
	 * @param value
	 */
	public void _testMoveRegionOfInterest(final int value) {
		getDetectorElementComposite().getRegionList().moveBean(value);
	}

	/**
	 * Testing only
	 * 
	 * @param index
	 * @return name at index
	 */
	@SuppressWarnings("unchecked")
	public String _testGetRegionName(final int index) {
		final List<XspressROI> rois = (List<XspressROI>) getDetectorElementComposite().getRegionList().getValue();
		return rois.get(index).getRoiName();
	}

	protected void upload(Object... upLoadbean) throws Exception {
		final Serializable bean;
		if (upLoadbean.length == 0) {
			// We save
			bean = (Serializable) updateFromUIAndReturnEditingBean();
		} else {
			bean = (Serializable) upLoadbean[0];
		}

		ScanObject scanObject = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		// The user may be editing a file e.g. VortexParameters outside of the ExperimentPerspective with no selected
		// scan
		final Serializable outputBean = scanObject != null ? scanObject.getOutputParameters() : null;

		final boolean ok = MessageDialog
				.openConfirm(
						getSite().getShell(),
						"Confirm Configure",
						"Are you sure you would like to permanently change the detector configuration?\n\n"
								+ "Please note, this will overwrite the detector configuration and ask the detector to reconfigure."
								+ "\n\n(A local copy of the file has been saved if you choose to cancel.)");
		if (!ok)
			return;

		IProgressService service = (IProgressService) getSite().getService(IProgressService.class);
		service.run(true, false, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				monitor.beginTask("Configure Detector", 100);

				try {
					final Map<String, Serializable> data = new HashMap<String, Serializable>(1);
					data.put("XspressParametersToLoad", bean);
					data.put("OutputParametersToLoad", outputBean);
					monitor.worked(10);
					if (saveToExtension) {
						String additionalSaveName = new File(path).getName();
						ScriptExecutor.Run(EXAFS_SCRIPT_OBSERVER, createObserver(), data, command
								+ "(XspressParametersToLoad,OutputParametersToLoad,\"" + additionalSaveName + "\")",
								JythonGuiConstants.TERMINALNAME);
					} else
						ScriptExecutor.Run(EXAFS_SCRIPT_OBSERVER, createObserver(), data, command
								+ "(XspressParametersToLoad,OutputParametersToLoad)", JythonGuiConstants.TERMINALNAME);
					monitor.worked(50);

				} catch (Exception e) {
					logger.error("Internal error cannot get data from detector.", e);
				} finally {
					monitor.done();
				}
			}
		});
	}

	private ValueListener autoApplyToAllListener;

	/**
	 * Turn on auto-apply to all of other detectors to match the current one.
	 * 
	 * @param on
	 *            true to turn on auto-apply to all, false to turn it off
	 */
	protected void autoApplyToAll(boolean on) {
		if (autoApplyToAllListener == null) {
			autoApplyToAllListener = new ValueAdapter("autoApplyToAllListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					applyToAll(false);
				}
			};
		}

		ScaleBox windowStart = getDetectorElementComposite().getWindowStart();
		ScaleBox windowEnd = getDetectorElementComposite().getWindowEnd();
		VerticalListEditor regionList = getDetectorElementComposite().getRegionList();

		if (on) {
			if (windowStart != null)
				windowStart.addValueListener(autoApplyToAllListener);
			if (windowEnd != null)
				windowEnd.addValueListener(autoApplyToAllListener);
			if (regionList != null)
				regionList.addValueListener(autoApplyToAllListener);
			if (getDetectorList() != null)
				getDetectorList().addValueListener(autoApplyToAllListener);
		} else {
			if (windowStart != null)
				windowStart.removeValueListener(autoApplyToAllListener);
			if (windowEnd != null)
				windowEnd.removeValueListener(autoApplyToAllListener);
			if (regionList != null)
				regionList.removeValueListener(autoApplyToAllListener);
			if (getDetectorList() != null)
				getDetectorList().removeValueListener(autoApplyToAllListener);

		}
	}

	/**
	 * Currently copies all the regions from the current element to all the other elements, later read an offset file
	 * when doing this step.
	 * 
	 * @param showMessage
	 *            if true prompts user to confirm
	 * @return Returns true if the user accepted to do the apply to all, or returns false if the user did not want to
	 *         perform the update. If showMessage == false always returns true.
	 */
	protected boolean applyToAll(boolean showMessage) {

		if (showMessage) {
			if (!MessageDialog
					.openConfirm(
							getSite().getShell(),
							"Confirm Apply To All",
							"Do you want to apply currently selected elements regions of interest to all detecors?\n\nThis will write new regions for the elements automatically.")) {
				return false;
			}
		}
		final int currentIndex = getDetectorList().getSelectedIndex();

		// Uses bean utils to clone the region list and reflection to send it
		// to the other elements.
		final Object startWindow = getDetectorElementComposite().getWindowStart() != null ? getDetectorElementComposite()
				.getWindowStart().getValue() : null;
		final Object endWindow = getDetectorElementComposite().getWindowEnd() != null ? getDetectorElementComposite()
				.getWindowEnd().getValue() : null;

		final List<?> regions = (List<?>) getDetectorElementComposite().getRegionList().getValue();
		final List<?> elements = (List<?>) getDetectorList().getValue();
		int index = -1;
		try {
			for (Object element : elements) {
				++index;
				if (index == currentIndex)
					continue;

				final List<?> regionClone = BeanUI.cloneBeans(regions);
				final Method setRegionList = element.getClass().getMethod("setRegionList", java.util.List.class);
				setRegionList.invoke(element, regionClone);

				// If there are a window start and end, set them
				try {
					BeansFactory.setBeanValue(element, "windowStart", startWindow);
					BeansFactory.setBeanValue(element, "windowEnd", endWindow);
				} catch (IllegalArgumentException ignored) {
					// The bean may not have windowStart and windowEnd to synchronize
				}
			}
		} catch (Exception e1) {
			logger.error("Error apply current detector regions to all detectors.", e1);
		}
		this.dirtyContainer.setDirty(true);
		return true;
	}

	protected IObserver createObserver() {
		return new IObserver() {
			@Override
			public void update(Object theObserved, Object changeCode) {
				if (theObserved instanceof ScriptcontrollerAdapter) {
					if (changeCode instanceof String) {
						sashPlotForm.appendStatus((String) changeCode, getLogger());
					}
				}
			}
		};
	}

	protected void configureUI() {

		sashPlotForm.setXAxisLabel("Channel Number");
		sashPlotForm.setYAxisLabel("Counts");
		sashPlotForm.computeSizes();

		try {
			getDetectorElementComposite().addStartListener(new ValueAdapter("windowStartListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateUIAfterDetectorElementCompositeChange();
				}
			});
			getDetectorElementComposite().addEndListener(new ValueAdapter("windowEndListener") {
				@Override
				public void valueChangePerformed(ValueEvent e) {
					updateUIAfterDetectorElementCompositeChange();
				}
			});
		} catch (Exception ne) {
			logger.error("Cannot add listeners", ne);
		}
	}
	
	private void updateUIAfterDetectorElementCompositeChange() {
		if (updatingAfterROIDrag == null){
			updatingAfterROIDrag = false;
			calculateAndPlotCountTotals(null);
			updateROIAfterElementCompositeChange();
			updatingAfterROIDrag = null;
		}
	}

	protected void updateROIAfterElementCompositeChange() {
		double roiStart =((Number)  getDetectorElementComposite().getStart().getValue()).doubleValue();
		double roiEnd = ((Number) getDetectorElementComposite().getEnd().getValue()).doubleValue();
		sashPlotForm.getRegionOnDisplay().setROI(new RectangularROI(roiStart,0,roiEnd-roiStart,0,0));
		sashPlotForm.getRegionOnDisplay().repaint();
	}

	protected void calculateAndPlotCountTotals(Boolean currentEditIndividual) {

		// use last value or store new value;
		if (currentEditIndividual == null) {
			currentEditIndividual = calculateSingleElement;
		} else {
			calculateSingleElement = currentEditIndividual;
		}

		if (detectorData == null)
			return;

		if (getDetectorElementComposite().getCount() == null || getDetectorElementComposite().getStart() == null
				|| getDetectorElementComposite().getEnd() == null) {
			return;
		}
		if (getDetectorElementComposite().getStart().getValue() == null
				|| getDetectorElementComposite().getEnd().getValue() == null) {
			return;
		}

		final int start = (Integer) getDetectorElementComposite().getStart().getValue();
		final int end = (Integer) getDetectorElementComposite().getEnd().getValue();

		int total = getInWindowsCounts(currentEditIndividual, start, end);
		getDetectorElementComposite().getCount().setValue(total);
		getDetectorElementComposite().setTotalCounts(getTotalCounts());
		getDetectorElementComposite().setTotalElementCounts(getTotalElementCounts(getCurrentSelectedElementIndex()));			

	}
	
	protected int getCurrentSelectedElementIndex() {
		return this.detectorListComposite.getDetectorList().getSelectedIndex();
	}

	private int getInWindowsCounts(Boolean currentEditIndividual, final int start, final int end) {
		int total = 0;
		if (currentEditIndividual) {
			total = sumElementInWindowCounts(start, end, total, getCurrentSelectedElementIndex());
		} else {
			for (int element = 0; element < detectorData.length; element++) {
				total = sumElementInWindowCounts(start, end, total, element);
			}
		}
		return total;
	}

	protected int sumElementInWindowCounts(final int start, final int end, int total, int element) {
		if (start == -1)
			return 0;
		final int numGrades = detectorData[element].length;
		for (int igrade = 0; igrade < numGrades; ++igrade) {
			for (int icount = start; icount <= end; ++icount) {
				if (icount >= detectorData[element][igrade].length)
					continue;
				total = total + (int) detectorData[element][igrade][icount];
			}
		}
		return total;
	}

	public GridListEditor getDetectorList() {
		return detectorListComposite.getDetectorList();
	}

	@Override
	public void setFocus() {
		getDetectorList().setFocus();
	}
	
	public class RegionSynchronizer implements IROIListener {
		@Override
		public void roiDragged(ROIEvent evt) {
		}
		
		@Override
		public void roiChanged(ROIEvent evt) {
			if (updatingAfterROIDrag == null) {
				updatingAfterROIDrag = true;
				final double start = ((RectangularROI)sashPlotForm.getRegionOnDisplay().getROI()).getPoint()[0];
				final double end = ((RectangularROI)sashPlotForm.getRegionOnDisplay().getROI()).getEndPoint()[0];
				getDetectorElementComposite().getStart().setValue(start);
				getDetectorElementComposite().getEnd().setValue(end);
				// then update the totals
				calculateAndPlotCountTotals(null);
				updatingAfterROIDrag = null;
			}
		}
		
		@Override
		public void roiSelected(ROIEvent evt) {
		}
	}

	protected void plot(final int ielement) {		
		final List<AbstractDataset> data = unpackDataSets(ielement);
		for (int i = 0; i < data.size(); i++) {
			data.get(i).setName(getChannelName(ielement));
		}
		sashPlotForm.setDataSets(data.toArray(new AbstractDataset[data.size()]));
		sashPlotForm.plotData();
		calculateAndPlotCountTotals(true);
	}

	private double getTotalElementCounts(int elementNumber) {
		double sum = 0;
		for (int j = 0; j < detectorData[elementNumber].length; j++) {
			for (int k = 0; k < detectorData[elementNumber][j].length; k++) {
				sum += detectorData[elementNumber][j][k];
			}
		}
		return sum;
	}

	private Double getTotalCounts() {
		if (detectorData == null)
			return Double.NaN;
		double sum = 0;
		for (int i = 0; i < detectorData.length; i++) {
			sum += getTotalElementCounts(i);
		}
		return sum;
	}

	protected java.awt.Color getChannelColor(int i) {
		return PlotColorUtility.getDefaultColour(i);
	}

	protected String getChannelName(int iChannel) {
		return "" + iChannel;
	}

	protected double getMin(Collection<AbstractDataset> data) {
		double ret = Double.MAX_VALUE;
		for (AbstractDataset dataSet : data) {
			ret = Math.min(ret, dataSet.min().doubleValue());
		}
		return ret;
	}

	protected double getMax(Collection<AbstractDataset> data) {
		double ret = -Double.MAX_VALUE;
		for (AbstractDataset dataSet : data) {
			ret = Math.max(ret, dataSet.max().doubleValue());
		}
		return ret;
	}

	protected List<AbstractDataset> unpackDataSets(int ielement) {

		final List<AbstractDataset> ret = new ArrayList<AbstractDataset>(7);
		if (ielement < 0 || detectorData == null) {
			ret.add(new DoubleDataset(new double[] { 0d }));
			return ret;
		}

		final double[][] data = detectorData[ielement];
		for (int i = 0; i < data.length; i++) {
			ret.add(new DoubleDataset(data[i]));
		}
		return ret;
	}


	protected DataWrapper getDataWrapper() {
		return dataWrapper;
	}

	/**
	 * @param int_data
	 * @return a double [][][] from an int[][][]
	 */
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

	protected void singleAcquire() throws Exception {

		final double time = getDetectorCollectionTime();
		IProgressService service = (IProgressService) getSite().getService(IProgressService.class);
		service.run(true, true, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					acquire(monitor, time);
				} catch (Exception e) {
					logger.error("Error performing single acquire", e);
				}
			}
		});
	}

	protected void setImportCompositeVisible(boolean visible) {
		GridUtils.setVisibleAndLayout(importComposite, visible);
	}

	protected void continuousAcquire() {

		this.continuousAquire = !continuousAquire;

		if (continuousAquire && lock != null && lock.isLocked()) {
			final String msg = "There is currently an acquire running. You cannot run another one.";
			logger.info(msg);
			sashPlotForm.appendStatus(msg, getLogger());
			return;
		}

		final long aquireTime = getAcquireWaitTime();
		final double collectiontime = getDetectorCollectionTime();

		try {
			if (continuousAquire) {
				acquireStarted();
				continuousThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							lock.lock();
							while (continuousAquire) {
								if (!lock.isLocked())
									break;
								if (isDisposed())
									break;
								acquire(null, collectiontime);
								if (!lock.isLocked())
									break;
								if (isDisposed())
									break;
								Thread.sleep(aquireTime);
							}
						} catch (InterruptedException e) {
							// Expected
						} catch (Throwable e) {
							logger.error("Continuous acquire problem with detector.", e);
						} finally {
							lock.unlock();
						}
					}
				}, "Detector Live Runner");
				continuousThread.start();
			} else {
				// Run later otherwise button looks unresponsive.
				// Even though this is the display thread already.
				getSite().getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {

							lock.lock();
							acquireFinished();

							final Detector detector = (Detector) Finder.getInstance().find(getDetectorName());
							logger.debug("Stopping detector");
							detector.stop();
						} catch (Exception e) {
							logger.error("Continuous problem configuring detector -  cannot stop detector.", e);
						} finally {
							lock.unlock();
						}
					}
				});
			}
		} catch (Exception e) {
			logger.error("Internal errror process continuous data from detector.", e);
			acquireFinished();
		}
	}

	/**
	 * Allows users to enter windows.
	 * 
	 * @param isEnabled
	 */
	public void setEnabled(final boolean isEnabled) {
		getDetectorList().setEnabled(isEnabled);
		uploadAction.setEnabled(isEnabled);
	}

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

	public DetectorElementComposite getDetectorElementComposite() {
		if (detectorListComposite == null) {
			return null;
		}
		return detectorListComposite.getDetectorElementComposite();
	}

	protected DataWrapper readStoredData() {
		DataWrapper newwrapper = new DataWrapper();

		try {
			File dataFile = new File(getDataXMLName());
			if (!dataFile.exists()) {
				return newwrapper;
			}
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			ElementCountsData[] elements = new ElementCountsData[0];
			String strLine;
			while ((strLine = in.readLine()) != null) {
				// Print the content on the console
				ElementCountsData newData = new ElementCountsData();
				newData.setDataString(strLine);
				elements = (ElementCountsData[]) ArrayUtils.add(elements, newData);
			}
			// Close the input stream
			in.close();

			if (elements.length == 0) {
				return newwrapper;
			}

			newwrapper.setValue(elements);

		} catch (IOException e) {
			logger.error("IOException whilst reading stored detector editor data from file " + getDataXMLName());
			return newwrapper;
		}

		return newwrapper;
	}

	protected void writeStoredData(@SuppressWarnings("unused") IProgressMonitor monitor) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(getDataXMLName()));
			ElementCountsData[] elements = (ElementCountsData[]) this.dataWrapper.getValue();
			for (int i = 0; i < elements.length; i++) {
				out.write(elements[i].getDataString());
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			logger.error("IOException whilst writing stored detector editor data from file " + getDataXMLName());
		}
	}

}
