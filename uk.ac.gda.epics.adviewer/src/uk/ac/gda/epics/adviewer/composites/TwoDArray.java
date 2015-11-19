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

package uk.ac.gda.epics.adviewer.composites;

import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDROI;
import gda.observable.Observable;
import gda.observable.Observer;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.io.h5.H5LazyDataset;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.dataset.impl.ByteDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IntegerDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LongDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.impl.ShortDataset;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFileUtils;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.eclipse.dawnsci.hdf5.Nexus;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.function.Histogram;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBeanException;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.ImageData;

public class TwoDArray extends Composite {

	public enum OptionIndex {
		I, I_MINUS_A, I_MUNIS_B, I_NORMALISED, I_OVER_A, I_OVER_B, A, B
	}

	private static final Logger logger = LoggerFactory.getLogger(TwoDArray.class);

	private ADController config;

	private IPlottingSystem plottingSystem;

	private Observable<Integer> arrayArrayCounterObservable;
	private Observer<Integer> arrayArrayCounterObserver;

	private boolean arrayMonitoring = false;
	private Button arrayMonitoringBtn;
	private Label arrayMonitoringLbl;

	protected boolean autoScale;

	Map<String, Dataset> stores = new HashMap<String, Dataset>();

	private ScrolledComposite leftScrolledComposite;

	private Button middle;

	UpdateArrayJob updateArrayJob;

	private IOCStatus statusComposite;

	private MinCallbackTimeComposite minCallbackTimeComposite;

	private boolean viewIsVisible;
	private Integer arrayCounter;
	private Button btnAutoscale;

	private Composite left;

	private Observable<Double> minCallbackTimeObservable;

	private NDPluginBase imageNDROIPluginBase;

	private Observer<Double> minCallbackTimeObserver;
	Dataset ads = null;
	private Group grpStores;
	private Button btnA;
	private Button btnB;
	private ComboViewer comboShow;
	private Group grpShow;

	ShowOption showOptionDefault = new ShowOption("I", OptionIndex.I);
	ShowOption showOption = showOptionDefault;

	// id used in DataBinding
	static final String showOptionName = "showOption";
	private Button btnSnapshot;

	public TwoDArray(IViewPart parentViewPart, Composite parent, int style, ADController adController) throws Exception {
		super(parent, style);
		this.plottingSystem = PlottingFactory.getLightWeightPlottingSystem();

		this.setLayout(new GridLayout(3,false));

		leftScrolledComposite= new ScrolledComposite(this,SWT.V_SCROLL| SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(leftScrolledComposite);
		left = new Composite(leftScrolledComposite, SWT.NONE);
		leftScrolledComposite.setContent(left);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(left);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.center = true;
		layout.pack = false;
		left.setLayout(new GridLayout(1, false));

		statusComposite = new IOCStatus(left, SWT.NONE);
		GridData gd_statusComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_statusComposite.widthHint = 154;
		statusComposite.setLayoutData(gd_statusComposite);

		minCallbackTimeComposite = new MinCallbackTimeComposite(left, SWT.NONE);
		GridData gd_minCallbackTimeComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_minCallbackTimeComposite.widthHint = 151;
		minCallbackTimeComposite.setLayoutData(gd_minCallbackTimeComposite);
		Group stateGroup = new Group(left, SWT.NONE);
		GridData gd_stateGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_stateGroup.widthHint = 151;
		stateGroup.setLayoutData(gd_stateGroup);
		stateGroup.setText("Array View");
		stateGroup.setLayout(new GridLayout(2, false));
		arrayMonitoringLbl = new Label(stateGroup, SWT.CENTER);
		GridData gd_arrayMonitoringLbl = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_arrayMonitoringLbl.widthHint = 81;
		arrayMonitoringLbl.setLayoutData(gd_arrayMonitoringLbl);
		arrayMonitoringBtn = new Button(stateGroup, SWT.PUSH | SWT.CENTER);
		GridData gd_arrayMonitoringBtn = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_arrayMonitoringBtn.widthHint = 48;
		arrayMonitoringBtn.setLayoutData(gd_arrayMonitoringBtn);

				grpStores = new Group(left, SWT.NONE);
				GridData gd_grpStores = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
				gd_grpStores.widthHint = 151;
				grpStores.setLayoutData(gd_grpStores);
				RowLayout rl_grpStores = new RowLayout(SWT.HORIZONTAL);
				grpStores.setLayout(rl_grpStores);
				grpStores.setText("Store I as");

						btnA = new Button(grpStores, SWT.NONE);
						btnA.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								Dataset clone = ads.clone();
								clone.setName("A");
								TwoDArray.this.setupStores("A", clone);
							}
						});
						btnA.setText("A");

								btnB = new Button(grpStores, SWT.NONE);
								btnB.setText("B");

										btnB.addSelectionListener(new SelectionAdapter() {
											@Override
											public void widgetSelected(SelectionEvent e) {
												Dataset clone = ads.clone();
												clone.setName("B");
												TwoDArray.this.setupStores("B", clone);
											}
										});

		grpShow = new Group(left, SWT.NONE);
		GridData gd_grpShow = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpShow.widthHint = 151;
		grpShow.setLayoutData(gd_grpShow);
		grpShow.setText("Show");
		grpShow.setLayout(new FillLayout(SWT.HORIZONTAL));

		comboShow = new ComboViewer(grpShow, SWT.READ_ONLY);
		comboShow.setContentProvider(ArrayContentProvider.getInstance());
		comboShow.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof ShowOption) {
					ShowOption opt = (ShowOption) element;
					return opt.getLabel();
				}
				return super.getText(element);
			}

		});

		ShowOption[] showOption = new ShowOption[] { showOptionDefault, new ShowOption("I-A", OptionIndex.I_MINUS_A),
				new ShowOption("I/A", OptionIndex.I_OVER_A), new ShowOption("I-B", OptionIndex.I_MUNIS_B),
				new ShowOption("I/B", OptionIndex.I_OVER_B), new ShowOption("(I-B)/(A-B)", OptionIndex.I_NORMALISED),
				new ShowOption("A", OptionIndex.A), new ShowOption("B", OptionIndex.B) };
		comboShow.setInput(showOption);

		IObservableValue comboShowObservableValue = ViewersObservables.observeSingleSelection(comboShow);
		IObservableValue showOptionObserveValue = PojoObservables.observeValue(this, showOptionName);

		DataBindingContext bindingContext = new DataBindingContext();
		bindingContext.bindValue(comboShowObservableValue, showOptionObserveValue);
		showOptionObserveValue.setValue(showOptionDefault);
		btnSnapshot = new Button(left, SWT.NONE);
		btnSnapshot.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnSnapshot.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					updateArrayJob.snapShot();
				} catch (Exception e1) {
					logger.error("Error taking snapshot", e1);
				}
			}
		});
		btnSnapshot.setText("Snapshot");

		btnAutoscale = new Button(left, SWT.CHECK);
		btnAutoscale.setText("Auto. scale");
		btnAutoscale.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				autoScale = btnAutoscale.getSelection();
			}

		});
		autoScale = true;
		btnAutoscale.setSelection(autoScale);

		setADController(left, adController);

		left.setSize(left.computeSize(SWT.DEFAULT, SWT.DEFAULT));



		middle = new Button(this,SWT.PUSH | SWT.TOP);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.BEGINNING).applyTo(middle);
		middle.setText(">");
		middle.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				showLeft(!getShowLeft());
			}});

/*		Composite right = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(right);
		right.setLayout(new FillLayout());
*/
		Composite right = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(right);
		GridLayoutFactory.fillDefaults().applyTo(right);

		Composite plotArea = new Composite(right, SWT.NONE);
		plotArea.setLayout(new FillLayout());
		{
			GridData gridData = new GridData();
			gridData.horizontalAlignment = SWT.FILL;
			gridData.grabExcessHorizontalSpace = true;
			gridData.grabExcessVerticalSpace = true;
			gridData.verticalAlignment = SWT.FILL;
			plotArea.setLayoutData(gridData);
		}


		plottingSystem.createPlotPart(plotArea, "", parentViewPart.getViewSite().getActionBars(), PlotType.IMAGE,
				parentViewPart);
		for (IAxis axis : plottingSystem.getAxes()) {
			axis.setTitle("");
		}


		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				try {
					stop();
				} catch (Exception ee) {
					logger.error("Error stopping histogram computation", ee);
				}
				if (minCallbackTimeObservable != null && minCallbackTimeObserver != null) {
					minCallbackTimeObservable.removeObserver(minCallbackTimeObserver);
					minCallbackTimeObserver = null;
				}
				if (plottingSystem != null) {
					plottingSystem.dispose();
					plottingSystem = null;
				}
			}
		});
	}
	private boolean showLeft;

	/**
	 * @param showLeft
	 */
	public void showLeft(Boolean showLeft) {
		this.showLeft = showLeft;
		GridData data = (GridData) leftScrolledComposite.getLayoutData();
		data.exclude = !showLeft;
		leftScrolledComposite.setVisible(showLeft);
		middle.setText(showLeft ? "<" : ">");
		layout(false);
	}

	/**
	 * @return true if left is hidden
	 */
	public Boolean getShowLeft() {
		return showLeft;
	}
	protected void setupStores(String storeName, Dataset ads) {
		stores.put(storeName, ads);
		Dataset storeA = stores.get("A");
		Dataset storeB = stores.get("B");
		stores.remove("A-B");
		if (storeA != null && storeB != null && Arrays.equals(storeA.getShape(), storeB.getShape())) {
			DoubleDataset storeAMinusB = new DoubleDataset(Maths.subtract(storeA, storeB));
			storeAMinusB.setName("A-B");
			stores.put("A-B", storeAMinusB);
		}
	}

	private void setADController(Composite left2, ADController config) throws Exception {
		this.config = config;

		// Configure AreaDetector
		NDPluginBase imageNDArrayBase = config.getImageNDArray().getPluginBase();
		minCallbackTimeComposite.setPluginBase(imageNDArrayBase);
		try {
			minCallbackTimeObservable = imageNDArrayBase.createMinCallbackTimeObservable();
			minCallbackTimeComposite.setMinTimeObservable(minCallbackTimeObservable);
			minCallbackTimeComposite.setMinCallbackTime(config.getArrayMinCallbackTime());
		} catch (Exception e2) {
			logger.error("Error setting min callback time", e2);
		}

		String sourcePortName = config.getImageNDArrayPortInput();
		String imageNDArrayPort = imageNDArrayBase.getNDArrayPort_RBV();
		if (imageNDArrayPort == null || !imageNDArrayPort.equals(sourcePortName))
			imageNDArrayBase.setNDArrayPort(sourcePortName);
		if (!imageNDArrayBase.isCallbacksEnabled_RBV())
			imageNDArrayBase.enableCallbacks();

		arrayMonitoringBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (arrayMonitoring) {
						stop();
					} else {
						start();
					}
				} catch (Exception ex) {
					logger.error("Error responding to start_stop button", ex);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		setStarted(arrayMonitoring);
		try {
			start();
		} catch (Exception e) {
			logger.error("Error starting  areaDetectorViewComposite", e);
		}
		try {
			statusComposite.setObservable(imageNDArrayBase.createConnectionStateObservable());
		} catch (Exception e1) {
			logger.error("Error monitoring connection state", e1);
		}

		NDROI imageNDROI = config.getImageNDROI();
		if (imageNDROI != null) {
			TwoDArrayROI twoDArrayROI;
			twoDArrayROI = new TwoDArrayROI(left2, SWT.NONE);
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd.widthHint = 151;
			twoDArrayROI.setLayoutData(gd);
			twoDArrayROI.setVisible(true);
//			layout(true);

			try {
				twoDArrayROI.setNDRoi(imageNDROI, getPlottingSystem());

				// setup Port for NDROI to match that of the Proc plugin - this should be the camera.
				imageNDROIPluginBase = imageNDROI.getPluginBase();
				String roiNDArrayPort = imageNDROIPluginBase.getNDArrayPort_RBV();
				if (roiNDArrayPort == null || !roiNDArrayPort.equals(sourcePortName))
					imageNDROIPluginBase.setNDArrayPort(sourcePortName);
				config.getImageNDArray().getPluginBase().setNDArrayPort(imageNDROIPluginBase.getPortName_RBV());

				imageNDROIPluginBase.enableCallbacks();

				twoDArrayROI.addMonitoringbtnSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						super.widgetSelected(e);
						if (!arrayMonitoring) {
							try {
								start();
							} catch (Exception e1) {
								logger.error("Error starting", e1);
							}
						}
					}

				});
				if (minCallbackTimeObservable != null) {
					minCallbackTimeObserver = new Observer<Double>() {

						@Override
						public void update(Observable<Double> source, Double mincallbacktime) {
							try {
								imageNDROIPluginBase.setMinCallbackTime(mincallbacktime);
							} catch (Exception e) {
								logger.error("Error setting minCallbackTime", e);
							}

						}
					};
					minCallbackTimeObservable.addObserver(minCallbackTimeObserver);
					minCallbackTimeObserver.update(null, config.getImageNDArray().getPluginBase()
							.getMinCallbackTime_RBV());
				}
			} catch (Exception e1) {
				logger.error("Error configuring the ROI", e1);
			}
		}
	}

	public void stop() throws Exception {
		config.getImageNDArray().getPluginBase().disableCallbacks();
		if (arrayArrayCounterObservable != null && arrayArrayCounterObserver != null) {
			arrayArrayCounterObservable.removeObserver(arrayArrayCounterObserver);
			arrayArrayCounterObserver = null;
			arrayArrayCounterObservable = null;
		}
		setStarted(false);
	}

	public ShowOption getShowOption() {
		return showOption;
	}

	public void setShowOption(ShowOption showOption) {
		this.showOption = showOption;
		if(updateArrayJob != null )
			updateArrayJob.schedule();
	}

	public void start() throws Exception {
		config.getImageNDArray().getPluginBase().enableCallbacks();
		if (arrayArrayCounterObservable == null) {
			arrayArrayCounterObservable = config.getImageNDArray().getPluginBase().createArrayCounterObservable();
		}
		if (updateArrayJob == null) {
			updateArrayJob = new UpdateArrayJob("Update array");
			updateArrayJob.setUser(false);
			updateArrayJob.setPriority(Job.SHORT);
		}
		if (arrayArrayCounterObserver == null) {
			arrayArrayCounterObserver = new Observer<Integer>() {

				@Override
				public void update(Observable<Integer> source, Integer arg) {
					if (isDisposed() || !viewIsVisible)
						return;
					if (arg == null)
						return;
					TwoDArray.this.arrayCounter = arg;
					updateArrayJob.schedule(); // rate is limited by min update time already

				}
			};
		}
		arrayArrayCounterObservable.addObserver(arrayArrayCounterObserver);
		setStarted(true);
	}

	private void setStarted(boolean b) {
		arrayMonitoring = b;
		arrayMonitoringBtn.setText(b ? "Stop" : "Start");
		arrayMonitoringLbl.setText(b ? "Running" : "Stopped");
		if( !viewIsVisible){
			arrayMonitoringLbl.setText("INACTIVE");
		}
		arrayMonitoringLbl.setForeground(getDisplay().getSystemColor(viewIsVisible ?
				(arrayMonitoring ? SWT.COLOR_GREEN: SWT.COLOR_BLACK) : SWT.COLOR_RED));
	}

	/**
	 * Needed for the adapter of the parent view to return IToolPageSystem.class
	 */
	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public int getHistSize() throws Exception {
		return config.getImageHistSize();
	}

	public double getImageMin() throws Exception {
		return config.getImageMin();
	}

	public double getImageMax() throws Exception {
		return config.getImageMax();
	}

	public void setViewIsVisible(boolean b) {
		this.viewIsVisible = b;
		if (viewIsVisible)
			arrayArrayCounterObserver.update(null, arrayCounter);
		setStarted(arrayMonitoring);
	}

	private String createParentEntry(IHierarchicalDataFile file, String fullEntry) throws Exception {
		return HierarchicalDataFileUtils.createParentEntry(file, fullEntry, Nexus.DATA);
	}

	protected void saveStores(String name) {
		try {
			String fileName = Activator.getDefault().getStateLocation().append(name + ".hdf").toOSString();
			IHierarchicalDataFile writer = HierarchicalDataFactory.getWriter(fileName);
			try {
				String parent = createParentEntry(writer, "/entry/stores");
				for (Entry<String, Dataset> store : stores.entrySet()) {
					Dataset data = store.getValue();
					String dataName = store.getKey();
					final String dataset = writer.replaceDataset(dataName, data, parent);
					writer.setNexusAttribute(dataset, Nexus.SDS);
				}
			} finally {
				if (writer != null)
					writer.close();
			}
		} catch (Exception e) {
			logger.error("Error saving state", e);
		}

	}

	void restoreStores(String name) {
		stores.clear();
		String fileName = Activator.getDefault().getStateLocation().append(name + ".hdf").toOSString();
		File file = new File(fileName);
		if (file.exists()) {
			try {
				IHierarchicalDataFile reader = HierarchicalDataFactory.getReader(fileName);
				try {
					final List<String> fullPaths = reader.getDatasetNames(IHierarchicalDataFile.NUMBER_ARRAY);
					for (String fullPath : fullPaths) {
						String[] entries = fullPath.split("/");
						String dsName = entries[entries.length - 1];
						if (dsName.equals("A-B")) continue;

						ILazyDataset lazy = new H5LazyDataset(reader, fullPath);
						Dataset store = DatasetUtils.convertToDataset(lazy.getSlice((Slice) null));
						store.setName(dsName);
						setupStores(dsName, store);
					}
				} finally {
					if (reader != null)
						reader.close();
				}
			} catch (Exception e) {
				logger.error("Error reading cache from " + fileName);
			}
		}
	}

	public void save(String name) {
		saveStores(name);
	}

	public void restore(String name) {
		restoreStores(name);
	}

	private class UpdateArrayJob extends Job {

		public UpdateArrayJob(String name) {
			super(name);
		}

		private IImageTrace trace;

		Boolean setMinMax;
		Integer min = null;
		Integer max = null;

		private Runnable updateUIRunnable;

		volatile boolean runnableScheduled = false;
		private Dataset nonNullDSToPlot;
		PlotView plotView;

		void snapShot() throws Exception {
			if (nonNullDSToPlot != null) {
				final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				IViewPart showView = window.getActivePage().showView("uk.ac.gda.epics.adviewer.snapshotView");
				if (showView instanceof PlotView) {
					plotView = (PlotView) showView;
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							try {
								DataBean dataBean = new DataBean(GuiPlotMode.TWOD);
								dataBean.putGuiParameter(GuiParameters.PLOTMODE, GuiPlotMode.TWOD);
								dataBean.putGuiParameter(GuiParameters.TITLE, nonNullDSToPlot.getName());

								DatasetWithAxisInformation axisData = new DatasetWithAxisInformation();
								AxisMapBean amb = new AxisMapBean();
								axisData.setAxisMap(amb);
								axisData.setData(nonNullDSToPlot);
								dataBean.addData(axisData);
								plotView.processPlotUpdate(dataBean);
							} catch (DataBeanException e) {
								logger.error("Error updating snapshot view", e);
							}
						}
					});

					/*
					 * if (xValues != null) { dataBean.addAxis(AxisMapBean.XAXIS, xValues); } if (yValues != null) {
					 * dataBean.addAxis(AxisMapBean.YAXIS, yValues); }
					 */
				}
			}
		}

		@Override
		public boolean belongsTo(Object family) {
			return super.belongsTo(family);
		}

		private int getPosToIncludeFractionOfPopulation(Dataset yData, Double fractionOfPopulationToInclude) {
			Double sum = (Double) yData.sum();
			double popIncluded = 0;
			int j = 0;
			double popRequired = sum * fractionOfPopulationToInclude;
			int size = yData.getSize();
			while (popIncluded < popRequired && j < size) {
				popIncluded += yData.getDouble(j);
				if (popIncluded < popRequired)
					j++;
			}
			return Math.min(j, size - 1);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				ImageData imageData;
				imageData = config.getImageData();
				imageData.toString();
				if (imageData.data.getClass().isArray()) {
					Object object = Array.get(imageData.data, 0);
					if (object instanceof Short) {
						ads = new ShortDataset((short[]) (imageData.data), imageData.dimensions);
					} else if (object instanceof Double) {
						ads = new DoubleDataset((double[]) (imageData.data), imageData.dimensions);
					} else if (object instanceof Long) {
						ads = new LongDataset((long[]) (imageData.data), imageData.dimensions);
					} else if (object instanceof Byte) {
						ads = new ByteDataset((byte[]) (imageData.data), imageData.dimensions);
					} else if (object instanceof Float) {
						ads = new FloatDataset((float[]) (imageData.data), imageData.dimensions);
					} else if (object instanceof Integer) {
						ads = new IntegerDataset((int[]) (imageData.data), imageData.dimensions);
					} else {
						throw new IllegalArgumentException("Type of data not recognised: "
								+ object.getClass().getName());
					}
					ads.setName(arrayCounter.toString());
					Dataset dsToShow = null;
					String explanation = "";

					OptionIndex showIndex = showOption.getIndex();
					switch (showIndex) {
					case I: {
						dsToShow = ads;
						break;
					}
					case B:
					case I_OVER_B:
					case I_MUNIS_B: {
						Dataset store = stores.get("B");
						if (store != null) {
							if (showIndex == OptionIndex.B) {
								dsToShow = store;
							} else {
								if (Arrays.equals(store.getShape(), ads.getShape())) {
									boolean isOver = showIndex == OptionIndex.I_OVER_B;
									if( isOver){
										DoubleDataset doubleDataset = new DoubleDataset(store);
										dsToShow = Maths.divide(ads,doubleDataset );
									} else {
										dsToShow = Maths.subtract(ads,store);
									}
									dsToShow.setName(ads.getName() + (isOver ? " / " : " - ") + store.getName());
								} else {
									explanation = new String("B does not match current image");
								}
							}
						} else {
							explanation = new String("B is empty");
						}
						break;
					}
					case A:
					case I_OVER_A:
					case I_MINUS_A: {
						Dataset store = stores.get("A");
						if (store != null) {
							if (showIndex == OptionIndex.A) {
								dsToShow = store;
							} else {
								if (Arrays.equals(store.getShape(), ads.getShape())) {
									boolean isOver = showIndex == OptionIndex.I_OVER_A;
									if( isOver){
										DoubleDataset doubleDataset = new DoubleDataset(store);
										dsToShow = Maths.divide(ads,doubleDataset );
									} else {
										dsToShow = Maths.subtract(ads,store);
									}
									dsToShow.setName(ads.getName() + (isOver ? " / " : " - ") + store.getName());
								} else {
									explanation = new String("A does not match current image");
								}
							}
						} else {
							explanation = new String("A is empty");
						}
						break;
					}
					case I_NORMALISED: {
						// I-B/A-B
						Dataset storeB = stores.get("B");
						DoubleDataset storeA_B = (DoubleDataset) stores.get("A-B");
						if (storeB != null && storeA_B != null && Arrays.equals(storeB.getShape(), ads.getShape())) {
							DoubleDataset ds = new DoubleDataset(Maths.subtract(ads, storeB));
							dsToShow = Maths.dividez(ds, storeA_B);
							dsToShow.setName("(" + ads.getName() + "-B)/(A-B)");
						} else {
							explanation = new String("A or B does not match current image");
						}
						break;
					}
					}
					if (dsToShow == null) {
						dsToShow = new IntegerDataset(new int[] { 0, 0, 0, 0 }, 2, 2);
						dsToShow.setName("Invalid selection:" + explanation);
					}
					nonNullDSToPlot = dsToShow;
					setMinMax = autoScale;
					if (min == null || setMinMax) {
						min = dsToShow.min().intValue();
					}
					if (max == null || setMinMax) {
						max = dsToShow.max().intValue();
					}
					if (max == min) {
						max = min + 1; // to ensure a range does exist
					}
					if (setMinMax) {
						// set min to .05 percentile value
						// set max to .95 percentile value
						// if these work out the same then resort to min and max of dataset
						int num_bins = 100;
						Histogram hist = new Histogram(num_bins, min, max, true);
						List<? extends Dataset> histogram_values = hist.value(dsToShow);
						if (histogram_values.size() > 1) {
							DoubleDataset histogramX = (DoubleDataset) histogram_values.get(1).getSlice(
									new int[] { 0 }, new int[] { num_bins }, new int[] { 1 });
							histogramX.setName("Intensity");
							Dataset histogramY = histogram_values.get(0);
							int jMax = getPosToIncludeFractionOfPopulation(histogramY, .95);
							jMax = Math.min(jMax + 1, histogramY.getSize() - 1);
							int jMin = getPosToIncludeFractionOfPopulation(histogramY, .05);
							jMin = Math.min(jMin - 1, histogramY.getSize() - 1);
							int lmin = min;
							int lmax = max;
							if (jMax >= 0) {
								lmax = (int) histogramX.getDouble(jMax);
							}
							if (jMin >= 0) {
								lmin = (int) histogramX.getDouble(jMin);
							}
							if (lmax != lmin) {
								max = lmax;
								min = lmin;
							}
						}
					}

					if (updateUIRunnable == null) {
						updateUIRunnable = new Runnable() {

							@Override
							public void run() {
								runnableScheduled = false;
								Dataset dataToPlot = getDataToPlot();
								if (trace == null || !Arrays.equals(trace.getData().getShape(), dataToPlot.getShape())) {
									trace = (IImageTrace) plottingSystem.updatePlot2D(dataToPlot, null, null);
								}
								String title = dataToPlot.getName();
								trace.setName(title);

								if(setMinMax){
									trace.setMin(getMin());
									trace.setMax(getMax());
								}
								trace.setRescaleHistogram(false);
								plottingSystem.setTitle(title);
								plottingSystem.updatePlot2D(dataToPlot, null, null);
							}

						};
					}
					if (!runnableScheduled) {
						if (!isDisposed()) {
							runnableScheduled = true;
							getDisplay().asyncExec(updateUIRunnable);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error reading image data", e);
			}
			return Status.OK_STATUS;
		}

		private Dataset getDataToPlot() {
			return nonNullDSToPlot;
		}

		private Integer getMin() {
			return min;
		}

		private Integer getMax() {
			return max;
		}
	}
}

class ShowOption {
	final String label;
	final TwoDArray.OptionIndex index;

	public ShowOption(String label, TwoDArray.OptionIndex index) {
		super();
		this.label = label;
		this.index = index;
	}

	public String getLabel() {
		return label;
	}

	public TwoDArray.OptionIndex getIndex() {
		return index;
	}

}
