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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IPositionListener;
import org.eclipse.dawnsci.plotting.api.axis.PositionEvent;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.NDStats;
import gda.observable.Observable;
import gda.observable.Observer;
import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Ids;

public class Histogram extends Composite {
	private static final String PROFILE = "PROFILE";

	private static final Logger logger = LoggerFactory.getLogger(Histogram.class);

	private ADController adController;

	private IPlottingSystem plottingSystem;

	private ILineTrace histogramTrace = null;
	private Dataset histogramXAxisRange = null;
	private Observable<Integer> statsArrayCounterObservable;
	private Observer<Integer> statsArrayCounterObserver, statsArrayCounterObserverStats;

	private String mpegROIRegionName;
	private Observable<Double> mpegProcOffsetObservable;
	private Observable<Double> mpegProcScaleObservable;
	private Observer<Double> mpegProcObserver;

	/**
	 * To prevent cycles of Gui updates Epics, Epics update GUI, Gui updates EPICS... only update the GUI if the values
	 * from EPICS do not match those used to last update the GUI
	 */
	double current_mpegROIMin = -Double.MAX_VALUE;
	double current_mpegROIMax = Double.MAX_VALUE;
	private RectangularROI current_mpegROI;

	private boolean grabOnceStats;


	private IViewPart parentViewPart;

	private Label txtPos;
	private ScrolledComposite leftScrolledComposite;

	private Button middle;

	public Histogram(IViewPart parentViewPart, Composite parent, int style) throws Exception {
		super(parent, style);
		this.parentViewPart =parentViewPart;
		setLayout(new GridLayout(3, false));

		leftScrolledComposite= new ScrolledComposite(this, SWT.V_SCROLL| SWT.H_SCROLL);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(leftScrolledComposite);
		Composite left = new Composite(leftScrolledComposite, SWT.NONE);
		leftScrolledComposite.setContent(left);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(left);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.center = true;
		layout.pack = false;
		GridLayout gl_left = new GridLayout(1, false);
		gl_left.verticalSpacing = 2;
		gl_left.marginWidth = 2;
		gl_left.marginHeight = 2;
		gl_left.horizontalSpacing = 2;
		left.setLayout(gl_left);

		statusComposite = new IOCStatus(left, SWT.NONE);
		GridData gd_statusComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_statusComposite.widthHint = 164;
		statusComposite.setLayoutData(gd_statusComposite);

		minCallbackTimeComposite = new MinCallbackTimeComposite(left, SWT.NONE);

		histogramStatus = new HistogramStatus(left, SWT.NONE);
		GridLayout gl_histogramStatus = new GridLayout(1, false);
		gl_histogramStatus.horizontalSpacing = 2;
		gl_histogramStatus.verticalSpacing = 2;
		gl_histogramStatus.marginWidth = 2;
		gl_histogramStatus.marginHeight = 2;
		histogramStatus.setLayout(gl_histogramStatus);

		@SuppressWarnings("unused")
		Label label = new Label(histogramStatus, SWT.SEPARATOR | SWT.HORIZONTAL);

		statisticsStatus = new StatisticsStatus(left, SWT.NONE);

		grpMjpegRange = new Group(left, SWT.NONE);
		grpMjpegRange.setText("MJPeg Range");
		grpMjpegRange.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout gl_grpMjpegRange = new GridLayout(2, false);
		gl_grpMjpegRange.verticalSpacing = 2;
		gl_grpMjpegRange.marginWidth = 2;
		gl_grpMjpegRange.marginHeight = 2;
		gl_grpMjpegRange.horizontalSpacing = 2;
		grpMjpegRange.setLayout(gl_grpMjpegRange);

		btnDisplayMJPegRange = new Button(grpMjpegRange, SWT.CHECK);
		btnDisplayMJPegRange.setSelection(true);
		btnDisplayMJPegRange.setText("Display");

		autoScaleBtn = new Button(grpMjpegRange, SWT.PUSH);
		autoScaleBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		autoScaleBtn.setText("Auto-Scale");

		//now all components are added to left we can set the size
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

		Composite right = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(right);
		GridLayoutFactory.fillDefaults().applyTo(right);

		txtPos = new Label(right, SWT.LEFT);
		txtPos.setText("XY value");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(txtPos);

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


		this.plottingSystem = PlottingFactory.getLightWeightPlottingSystem();
		plottingSystem.createPlotPart(plotArea, "", parentViewPart.getViewSite().getActionBars(), PlotType.XY,
				parentViewPart);
		plottingSystem.setXFirst(true);
		plottingSystem.setShowLegend(false);
		plottingSystemPositionListener = new IPositionListener() {

			@Override
			public void positionChanged(PositionEvent evt) {
				txtPos.setText(String.format("X:%.7g Y:%.7g", evt.x, evt.y));
			}
		};
		plottingSystem.addPositionListener(plottingSystemPositionListener);
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (mpegProcObserver != null) {
					if (mpegProcOffsetObservable != null && mpegProcObserver != null)
						mpegProcOffsetObservable.removeObserver(mpegProcObserver);
					if (mpegProcScaleObservable != null && mpegProcObserver != null)
						mpegProcScaleObservable.removeObserver(mpegProcObserver);
					mpegProcObserver = null;
				}
				mpegProcOffsetObservable = null;
				mpegProcScaleObservable = null;
				if (plottingSystem != null) {
					if( plottingSystemPositionListener != null){
						plottingSystem.removePositionListener(plottingSystemPositionListener);
						plottingSystemPositionListener = null;
					}
					plottingSystem.dispose();
					plottingSystem = null;
				}
				if (statsArrayCounterObservable != null && statsArrayCounterObserverStats != null) {
					statsArrayCounterObservable.removeObserver(statsArrayCounterObserverStats);
				}
				if (statsArrayCounterObservable != null && statsArrayCounterObserver != null) {
					statsArrayCounterObservable.removeObserver(statsArrayCounterObserver);
				}

			}
		});

	}
	private boolean showLeft;

	//Do not update the plot whilst the roi is being changed as otherwise the
	//region suddenly changed
	protected boolean roiBeingChanged=false;

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

	public void setADController(ADController config) {
		this.adController = config;

		try {
			createOrUpdateROI();
		} catch (Exception e1) {
			logger.error("Error creating region", e1);
		}


		try {
			NDStats imageNDStats = config.getImageNDStats();
			statusComposite.setObservable(imageNDStats.getPluginBase().createConnectionStateObservable());
			statisticsStatus.setEnableObservable(imageNDStats.getPluginBase().createEnableObservable());
			histogramStatus.setEnableObservable(imageNDStats.getPluginBase().createEnableObservable());
			histogramStatus.setComputeHistogramObservable(imageNDStats.createComputeHistogramObservable());
			statisticsStatus.setComputeObservable(imageNDStats.createComputeStatisticsObservable());
			statisticsStatus.setMinObservable(imageNDStats.createMinObservable());
			statisticsStatus.setMaxObservable(imageNDStats.createMaxObservable());
			statisticsStatus.setMeanObservable(imageNDStats.createMeanObservable());
			statisticsStatus.setTotalObservable(imageNDStats.createTotalObservable());
			statisticsStatus.setSigmaObservable(imageNDStats.createSigmaObservable());

			btnDisplayMJPegRange.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IRegion iRegion = getPlottingSystem().getRegion(mpegROIRegionName);
					if (iRegion != null)
						iRegion.setVisible(btnDisplayMJPegRange.getSelection());
				}
			});
			autoScaleBtn.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						ICommandService cs = (ICommandService) parentViewPart.getSite().getService(
								ICommandService.class);
						Command command = cs.getCommand(Ids.COMMANDS_SET_LIVEVIEW_SCALE);
						IParameter parameter = command
								.getParameter(Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME);
						String name = Histogram.this.adController.getServiceName();
						Parameterization[] parameterizations = new Parameterization[] { new Parameterization(parameter,
								name	) };
						ParameterizedCommand cmd = new ParameterizedCommand(command, parameterizations);
						ExecutionEvent executionEvent = ((IHandlerService) parentViewPart.getSite().getService(
								IHandlerService.class)).createExecutionEvent(cmd, null);
						command.executeWithChecks(executionEvent);
					} catch (Exception e1) {
						logger.error("Error setting live view scaling", e1);
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		} catch (Exception e1) {
			logger.error("Error in monitoring connection state", e1);
		}

		histogramStatus.addHistogramMonitoringbtnSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (histogramStatus.getHistogramMonitoring()) {
						stop();
					} else {
						start();
					}
				} catch (Exception ex) {
					logger.error("Error responding to start_stop button", ex);
				}
			}
		});
		statisticsStatus.addMonitoringbtnSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (statisticsStatus.getMonitoring()) {
						stopStats();
					} else {
						startStats();
					}
				} catch (Exception ex) {
					logger.error("Error responding to start_stop button", ex);
				}
			}
		});

		try {
			minCallbackTimeComposite.setPluginBase(config.getImageNDStats().getPluginBase());
			minCallbackTimeComposite.setMinTimeObservable(config.getImageNDStats().getPluginBase()
					.createMinCallbackTimeObservable());
			minCallbackTimeComposite.setMinCallbackTime(config.getHistogramMinCallbackTime());
		} catch (Exception e1) {
			logger.error("Error setting up minCallback", e1);
		}

		try {
			if (statsArrayCounterObservable == null) {
				statsArrayCounterObservable = config.getImageNDStats().getPluginBase().createArrayCounterObservable();
			}
			if (statsArrayCounterObserverStats == null) {
				statsArrayCounterObserverStats = new Observer<Integer>() {

					boolean first = true;

					@Override
					public void update(Observable<Integer> source, Integer arg) {
						if (isDisposed())
							return;
						if (first) {
							first = false;
							return; // ignore first update
						}
						if (grabOnceStats) {
							try {
								stopStats();
							} catch (Exception e) {
								logger.error("Error stopping histogram update", e);
							}
							grabOnceStats = false;
						}
					}
				};
			}
			statsArrayCounterObservable.addObserver(statsArrayCounterObserverStats);
		} catch (Exception e) {
			logger.error("Error monitoring stats", e);
		}
		try {
			final int histSize = getHistSize();
			double histMin = getImageMin();
			double histMax = getImageMax();
			config.getImageNDStats().setHistSize(histSize);
			config.getImageNDStats().setHistMin(histMin);
			config.getImageNDStats().setHistMax(histMax);
			double step = (histMax - histMin) / histSize;
			double[] range = new double[histSize];
			range[0] = histMin;
			for (int i = 1; i < histSize; i++) {
				range[i] = range[i - 1] + step;
			}
			histogramXAxisRange = DatasetFactory.createFromObject(range);
			histogramXAxisRange.setName("Counts");
			if (statsArrayCounterObservable == null) {
				statsArrayCounterObservable = config.getImageNDStats().getPluginBase().createArrayCounterObservable();
			}
			if (statsArrayCounterObserver == null) {
				statsArrayCounterObserver = new Observer<Integer>() {

					boolean first = true;

					@Override
					public void update(Observable<Integer> source, Integer arg) {
						if (isDisposed())
							return;
						if (first) {
							first = false;
							return; // ignore first update
						}
						if (histogramStatus.isFreezeSelected()  || !histogramStatus.getHistogramMonitoring())
							return;
						if( roiBeingChanged)
							return;
						if (updateHistogramJob == null) {
							updateHistogramJob = new Job("Update histogram") {

								private Runnable updateUIRunnable;
								volatile boolean runnableScheduled = false;

								@Override
								public boolean belongsTo(Object family) {
									return super.belongsTo(family);
								}

								@Override
								protected IStatus run(IProgressMonitor monitor) {
									if (plottingSystem == null)
										return Status.OK_STATUS;
									double[] histogram_RBV;
									try {
										histogram_RBV = Histogram.this.adController.getImageNDStats().getHistogram_RBV(histSize);
									} catch (Exception e) {
										logger.error("Error getting histogram", e);
										return Status.OK_STATUS;
									}

									if (histogram_RBV.length != histogramXAxisRange.getSize()) {
										logger.error("Length of histogram does not match histSize");
										return Status.OK_STATUS;
									}
									Dataset ds = DatasetFactory.createFromObject(histogram_RBV);
									Number max = ds.max();
									int numItemsToShow=histogram_RBV.length;
									for( int i=numItemsToShow; i>0;i-- ){
										if( histogram_RBV[i-1] > max.doubleValue()/1E6){
											numItemsToShow=i;
											break;
										}
									}


									if (histogramTrace == null) {
										histogramTrace = plottingSystem.createLineTrace(PROFILE);
										histogramTrace.setTraceColor(ColorConstants.blue);
									}
									Slice slice = new Slice(0, numItemsToShow, 1); //stop 1 past last value
									Dataset dsToShow = ds.getSlice(slice);
									dsToShow.setName("");
									Dataset histogramXAxisRangeToShow = histogramXAxisRange.getSlice(slice);
									histogramTrace.setData(histogramXAxisRangeToShow, dsToShow);

									if (updateUIRunnable == null) {
										updateUIRunnable = new Runnable() {

											@Override
											public void run() {
												runnableScheduled = false;
												boolean firstTime = plottingSystem.getTrace(PROFILE) == null;
												if (firstTime) {
													plottingSystem.addTrace(histogramTrace);
													plottingSystem.setTitle("Histogram");
													IAxis yaxis = plottingSystem.getSelectedYAxis();
													yaxis.setFormatPattern("#####");
													yaxis.setTitle("Number of Pixels");
													IAxis xaxis = plottingSystem.getSelectedXAxis();
													xaxis.setFormatPattern("#####");
													xaxis.setTitle("Counts");
												}
												plottingSystem.repaint();
											}

										};
									}
									if (!runnableScheduled) {
										getDisplay().asyncExec(updateUIRunnable);
										runnableScheduled = true;
									}
									return Status.OK_STATUS;
								}
							};
							updateHistogramJob.setUser(false);
							updateHistogramJob.setPriority(Job.SHORT);
						}
						updateHistogramJob.schedule(200); // limit to 5Hz

					}
				};
			}
			statsArrayCounterObservable.addObserver(statsArrayCounterObserver);
		} catch (Exception e) {
			logger.error("Error monitoring histogram", e);
		}

	}

	public void grabOnceStats() throws Exception {
		grabOnceStats = !isComputingStats();
		startStats();
	}

	public void startStats() throws Exception {
		adController.getImageNDStats().getPluginBase().enableCallbacks();
		adController.getImageNDStats().setComputeStatistics(1);
	}

	public void stopStats() throws Exception {
		adController.getImageNDStats().setComputeStatistics(0);
	}

	private double getMPEGProcOffset() throws Exception {
		return adController.getLiveViewNDProc().getOffset();
	}

	private double getMPEGProcScale() throws Exception {
		return adController.getLiveViewNDProc().getScale();
	}

	protected void updateROIInGuiThread() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					createOrUpdateROI();
				} catch (Exception e) {
					logger.error("Error responding to external update of scale and offset", e);
				}
			}
		});
	}

	protected void createOrUpdateROI() throws Exception {
		double scale = Histogram.this.getMPEGProcScale();
		double offset = Histogram.this.getMPEGProcOffset();
		RectangularROI roi;
		double min =  -offset;
		double max = (255.0 / scale + min);

		if (min < getImageMin())
			min = getImageMin(); // The lowest intensity is 0
		if (max > getImageMax())
			max = getImageMax();

		roi = current_mpegROI;
		if (min == current_mpegROIMin && max == current_mpegROIMax && roi != null)
			return;
		if (roi == null) {
			roi = new RectangularROI();
		}
		roi.setPoint(new double[] { min, 0 });
		roi.setLengths(new double[] { max - min, 0 });

		if (mpegROIRegionName == null) {
			mpegROIRegionName = RegionUtils.getUniqueName("Scaling Range", getPlottingSystem());
		}
		IRegion iRegion = getPlottingSystem().getRegion(mpegROIRegionName);
		if (iRegion == null) {
			iRegion = getPlottingSystem().createRegion(mpegROIRegionName, IRegion.RegionType.XAXIS);
			iRegion.addROIListener(new IROIListener() {

				@Override
				public void roiDragged(ROIEvent evt) {
					try {
						handleROIChangeEvent(evt, false);
					} catch (Exception e) {
						logger.error("Error handling change to scaling roi", e);
					}
				}

				private void handleROIChangeEvent(ROIEvent evt, boolean changeComplete) throws Exception {
					roiBeingChanged = !changeComplete;
					final IRegion region = (IRegion) evt.getSource();
					RectangularROI roi = (RectangularROI) region.getROI();
					double min = roi.getPointX();
					double max = min + roi.getLengths()[0];
					double offset = -min;
					double scale = 255.0 / (max - min);
					Histogram.this.adController.getLiveViewNDProc().setScale(scale);
					Histogram.this.adController.getLiveViewNDProc().setOffset(offset);
					Histogram.this.adController.getLiveViewNDProc().setEnableOffsetScale(1);
				}

				@Override
				public void roiChanged(ROIEvent evt) {
					try {
						handleROIChangeEvent(evt, true);
					} catch (Exception e) {
						logger.error("Error handling change to scaling roi", e);
					}
				}

				@Override
				public void roiSelected(ROIEvent evt) {
				}
			});
			mpegProcOffsetObservable = Histogram.this.adController.getLiveViewNDProc().createOffsetObservable();
			mpegProcScaleObservable = Histogram.this.adController.getLiveViewNDProc().createScaleObservable();
			mpegProcObserver = new Observer<Double>() {

				@Override
				public void update(Observable<Double> source, Double arg) {
					updateROIInGuiThread();
				}
			};
			mpegProcOffsetObservable.addObserver(mpegProcObserver);
			mpegProcScaleObservable.addObserver(mpegProcObserver);

			iRegion.setVisible(true);
			getPlottingSystem().addRegion(iRegion);
		}
		iRegion.setROI(roi);
		current_mpegROI = roi;
		current_mpegROIMax = max;
		current_mpegROIMin = min;
	}

	public void stop() throws Exception {
		adController.getImageNDStats().setComputeHistogram(0);
	}

	Job updateHistogramJob;

	private Button autoScaleBtn;
	private IOCStatus statusComposite;

	private HistogramStatus histogramStatus;
	private StatisticsStatus statisticsStatus;
	private Group grpMjpegRange;
	private Button btnDisplayMJPegRange;
	private MinCallbackTimeComposite minCallbackTimeComposite;

	private IPositionListener plottingSystemPositionListener;

	boolean isComputingHistogram() throws Exception {
		NDStats imageNDStats = adController.getImageNDStats();
		return imageNDStats.getPluginBase().isCallbacksEnabled_RBV() && imageNDStats.getComputeHistogram_RBV() == 1;
	}

	boolean isComputingStats() throws Exception {
		NDStats imageNDStats = adController.getImageNDStats();
		return imageNDStats.getPluginBase().isCallbacksEnabled_RBV() && imageNDStats.getComputeStatistics_RBV() == 1;
	}

	public void start() throws Exception {
		adController.getImageNDStats().getPluginBase().enableCallbacks();
		adController.getImageNDStats().setComputeHistogram(1);
	}

	/**
	 * Needed for the adapter of the parent view to return IToolPageSystem.class
	 */
	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public int getHistSize() throws Exception {
		return adController.getImageHistSize();
	}

	public double getImageMin() throws Exception {
		return adController.getImageMin();
	}

	public double getImageMax() throws Exception {
		return adController.getImageMax();
	}

}
