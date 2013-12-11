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

import gda.jython.InterfaceProvider;
import gda.jython.gui.JythonGuiConstants;
import gda.jython.scriptcontroller.ScriptExecutor;
import gda.jython.scriptcontroller.corba.impl.ScriptcontrollerAdapter;
import gda.observable.IObserver;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.jreality.util.PlotColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.beans.exafs.IDetectorElement;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
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

public abstract class DetectorEditor extends RichBeanEditorPart {
	private static final String EXAFS_SCRIPT_OBSERVER = "ExafsScriptObserver";// name of ScriptController that must be in the system for uploading to device to work
	private static final Logger logger = LoggerFactory.getLogger(DetectorEditor.class);
	protected SashFormPlotComposite sashPlotFormComposite;
	protected volatile int[/* element */][/* grade */][/* mca */] detectorData;
	protected DataWrapper dataWrapper;
	protected String serverCommand;
	private ExpansionAdapter expansionListener;
	private DetectorListComposite detectorListComposite;
	private Composite importComposite;
	private int lastSelectedElementIndex;
	private Object bean;
	private Action uploadAction;
	private volatile Boolean updatingAfterROIDrag = null;
	protected Data plotData;
	private ValueListener autoApplyToAllListener;
	protected Counts counts;
	public Plot plot;
	
	public DetectorEditor(final String path, final URL mappingURL, final DirtyContainer dirtyContainer,
			final Object editingBean, final String serverCommand) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.serverCommand = serverCommand;
		this.bean = editingBean;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class)
			return sashPlotFormComposite.getPlottingSystem();
		return super.getAdapter(clazz);
	}

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

	/**
	 * @return - String- the full path of the xml where the acquired data is persisted.
	 */
	protected abstract String getDataXMLName();

	@Override
	public void createPartControl(Composite parent) {
		plotData = new Data();
		counts = new Counts();
		this.dataWrapper = plotData.readStoredData(getDataXMLName());
		try {
			sashPlotFormComposite = createSashPlot(parent);
			sashPlotFormComposite.getPlottingSystem().setRescale(false);
		} catch (Exception e) {
			logger.error("Exception while creating detector editor", e);
		}
		plot = new Plot(sashPlotFormComposite, counts);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		plotData.writeStoredData(getDataXMLName(), (ElementCountsData[]) dataWrapper.getValue());
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
		return new SashFormPlotComposite(parent, this, new RegionSynchronizer(), createUpLoadAction());
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
		if (getDataWrapper().getValue() != null)
			setWindowsEnabled(true);
		else
			setWindowsEnabled(false);
	}

	protected DetectorListComposite createDetectorList(final Composite parent,
	final Class<? extends IDetectorElement> editorClass, final int elementListSize,
	final Class<? extends DetectorROI> regionClass, final IDetectorROICompositeFactory regionEditorFactory,
	final Boolean showAdvanced) {
		importComposite = new Composite(parent, SWT.NONE);
		importComposite.setLayout(new GridLayout(2, false));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(importComposite);
		Label importLabel = new Label(importComposite, SWT.NONE);
		importLabel.setText("Import Regions Of Interest");
		final Button importButton = new Button(importComposite, SWT.NONE);
		GridDataFactory grab = GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false);
		grab.hint(60, SWT.DEFAULT).applyTo(importButton);
		importButton.setImage(SWTResourceManager.getImage(VortexParametersUIEditor.class, "/icons/calculator_edit.png"));
		importButton.setToolTipText("Import Regions Of Interest from other Parameters files");
		final SelectionAdapter importButtonListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardDialog dialog = new WizardDialog(importButton.getShell(), new ImportROIWizard(elementListSize, DetectorEditor.this));
				dialog.create();
				dialog.open();
			}
		};
		importButton.addSelectionListener(importButtonListener);
		
		detectorListComposite = new DetectorListComposite(parent, editorClass, elementListSize, regionClass, regionEditorFactory, showAdvanced);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(detectorListComposite);
		expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				sashPlotFormComposite.getLeftScroll().setMinSize(sashPlotFormComposite.getLeft().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		};
		
		expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				sashPlotFormComposite.getLeftScroll().setMinSize(sashPlotFormComposite.getLeft().computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		};
		detectorListComposite.addExpansionListener(expansionListener);

		getDetectorElementComposite().getRegionList().addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				if (getDetectorList().getSelectedIndex() == lastSelectedElementIndex) {
					if (bean instanceof XspressParameters) {
						XspressParameters detBean = (XspressParameters) bean;
						detBean.setSelectedRegionNumber(evt.getSelectionIndex());
					} 
					else if (bean instanceof VortexParameters) {
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
				plot.plot(evt.getSelectionIndex(),false, detectorData, getDetectorElementComposite(), getCurrentSelectedElementIndex(), false, null);
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

		ExafsActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().compareTo(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED) == 0)
					setRegionEditableFromPreference();
			}
		});
		setRegionEditableFromPreference();
		return detectorListComposite;
	}
	
	private void setRegionEditableFromPreference(){
		sashPlotFormComposite.getRegionOnDisplay().setMobile(ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_OVERLAY_ENABLED));
	}

	@Override
	public void dispose() {
		if (detectorListComposite != null && !detectorListComposite.isDisposed())
			detectorListComposite.removeExpansionListener(expansionListener);
		if (sashPlotFormComposite != null)
			sashPlotFormComposite.dispose();
		if (autoApplyToAllListener != null)
			autoApplyToAll(false); // remove all auto-apply to all listeners
		super.dispose();
	}

	public void _testAddRegionOfInterest(final String name) throws Exception {
		getDetectorElementComposite().getRegionList().addBean();
		getDetectorElementComposite().getRegionList().setField("roiName", name);
	}

	public void _testDeleteRegionOfInterest() {
		getDetectorElementComposite().getRegionList().deleteBean();
	}

	public void _testMoveRegionOfInterest(final int value) {
		getDetectorElementComposite().getRegionList().moveBean(value);
	}

	protected void upload() throws Exception {
		// the bean from the enclosing scan (may be null if no scan selected)
		IExperimentObject experimentObject = ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		final IOutputParameters outputBean;
		if (experimentObject != null && experimentObject instanceof ScanObject)
			outputBean = ((ScanObject) experimentObject).getOutputParameters();
		else
			outputBean = null;
		final boolean ok = MessageDialog.openConfirm(getSite().getShell(),"Confirm Configure",
			"Are you sure you would like to permanently change the detector configuration?\n\n" + 
			"Please note, this will overwrite the detector configuration and ask the detector to reconfigure." + 
			"\n\n(A local copy of the file has been saved if you choose to cancel.)");
		if (!ok)
			return;

		IProgressService service = (IProgressService) getSite().getService(IProgressService.class);
		service.run(true, false, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Configure Detector", 100);
				try {
					Map<String, Serializable> data = new HashMap<String, Serializable>(1);
					data.put("XMLFileNameToLoad", path);
					data.put("OutputParametersToLoad", outputBean);
					monitor.worked(10);
					ScriptExecutor.Run(EXAFS_SCRIPT_OBSERVER, createObserver(), data, serverCommand + "(XMLFileNameToLoad,OutputParametersToLoad)", JythonGuiConstants.TERMINALNAME);
					monitor.worked(50);
					String configureResult = InterfaceProvider.getCommandRunner().evaluateCommand(serverCommand + ".getConfigureResult()");
					sashPlotFormComposite.appendStatus(configureResult, logger);
				} catch (Exception e) {
					logger.error("Internal error cannot get data from detector.", e);
				} finally {
					monitor.done();
				}
			}
		});
	}

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
		} 
		else {
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
			if (!MessageDialog.openConfirm(getSite().getShell(), "Confirm Apply To All",
			"Do you want to apply currently selected elements regions of interest to all detecors?\n\nThis will write new regions for the elements automatically."))
				return false;
		}
		
		int currentIndex = getDetectorList().getSelectedIndex();

		// Uses bean utils to clone the region list and reflection to send it
		// to the other elements.
		Object startWindow = getDetectorElementComposite().getWindowStart() != null ? getDetectorElementComposite().getWindowStart().getValue() : null;
		Object endWindow = getDetectorElementComposite().getWindowEnd() != null ? getDetectorElementComposite().getWindowEnd().getValue() : null;
		List<?> regions = (List<?>) getDetectorElementComposite().getRegionList().getValue();
		List<?> elements = (List<?>) getDetectorList().getValue();
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
				if (theObserved instanceof ScriptcontrollerAdapter)
					if (changeCode instanceof String)
						sashPlotFormComposite.appendStatus((String) changeCode, logger);
			}
		};
	}

	protected void configureUI() {
		sashPlotFormComposite.setXAxisLabel("Channel Number");
		sashPlotFormComposite.setYAxisLabel("Counts");
		sashPlotFormComposite.computeSizes();
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
			try {
				updatingAfterROIDrag = false;
				counts.calculateAndPlotCountTotals(null, true, detectorData, getDetectorElementComposite(), getCurrentSelectedElementIndex());
				updateROIAfterElementCompositeChange();
			} finally {
				updatingAfterROIDrag = null;
			}
		}
	}

	protected void updateROIAfterElementCompositeChange() {
		double roiStart = ((Number) getDetectorElementComposite().getStart().getValue()).doubleValue();
		double roiEnd = ((Number) getDetectorElementComposite().getEnd().getValue()).doubleValue();
		sashPlotFormComposite.getRegionOnDisplay().setROI(new RectangularROI(roiStart, 0, roiEnd - roiStart, 0, 0));
		sashPlotFormComposite.getRegionOnDisplay().repaint();
	}

	protected int getCurrentSelectedElementIndex() {
		return this.detectorListComposite.getDetectorList().getSelectedIndex();
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
				try {
					updatingAfterROIDrag = true;
					double start = ((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getPoint()[0];
					double end = ((RectangularROI) sashPlotFormComposite.getRegionOnDisplay().getROI()).getEndPoint()[0];
					getDetectorElementComposite().getStart().setValue(start);
					getDetectorElementComposite().getEnd().setValue(end);
					// then update the totals
					counts.calculateAndPlotCountTotals(true, true, detectorData, getDetectorElementComposite(), getCurrentSelectedElementIndex());
				} finally {
					updatingAfterROIDrag = null;
				}
			}
		}

		@Override
		public void roiSelected(ROIEvent evt) {
		}
	}
	
	

	protected java.awt.Color getChannelColor(int i) {
		return PlotColorUtility.getDefaultColour(i);
	}

	protected double getMin(Collection<AbstractDataset> data) {
		double ret = Double.MAX_VALUE;
		for (AbstractDataset dataSet : data)
			ret = Math.min(ret, dataSet.min().doubleValue());
		return ret;
	}

	protected double getMax(Collection<AbstractDataset> data) {
		double ret = -Double.MAX_VALUE;
		for (AbstractDataset dataSet : data)
			ret = Math.max(ret, dataSet.max().doubleValue());
		return ret;
	}

	protected DataWrapper getDataWrapper() {
		return dataWrapper;
	}

	protected void setImportCompositeVisible(boolean visible) {
		GridUtils.setVisibleAndLayout(importComposite, visible);
	}

	/**
	 * Allows users to enter windows.
	 * 
	 * @param isEnabled
	 */
	public void setWindowsEnabled(final boolean isEnabled) {
		getDetectorList().setEnabled(isEnabled);
		uploadAction.setEnabled(isEnabled);
	}

	public DetectorElementComposite getDetectorElementComposite() {
		if (detectorListComposite == null)
			return null;
		return detectorListComposite.getDetectorElementComposite();
	}

}