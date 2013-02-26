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

import gda.device.detector.areadetector.v17.NDStats;
import gda.observable.Observable;
import gda.observable.Observer;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.axis.IPositionListener;
import org.dawb.common.ui.plot.axis.PositionEvent;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
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

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Ids;

public class Histogram extends Composite {
	private static final String PROFILE = "PROFILE";

	private static final Logger logger = LoggerFactory.getLogger(Histogram.class);

	private ADController config;

	private AbstractPlottingSystem plottingSystem;

	private ILineTrace histogramTrace = null;
	private DoubleDataset histogramXAxisRange = null;
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
	long current_mpegROIMin = Long.MIN_VALUE;
	long current_mpegROIMax = Long.MAX_VALUE;
	private RectangularROI current_mpegROI;

	private boolean grabOnceStats;


	private IViewPart parentViewPart;

	private Label txtPos;

	public Histogram(IViewPart parentViewPart, Composite parent, int style) throws Exception {
		super(parent, style);
		this.parentViewPart =parentViewPart;
		setLayout(new GridLayout(2, false));
		Composite left = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(left);
		RowLayout layout = new RowLayout(SWT.VERTICAL);
		layout.center = true;
		layout.pack = false;
		left.setLayout(new GridLayout(1, false));

		statusComposite = new IOCStatus(left, SWT.NONE);
		GridData gd_statusComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_statusComposite.widthHint = 164;
		statusComposite.setLayoutData(gd_statusComposite);

		minCallbackTimeComposite = new MinCallbackTimeComposite(left, SWT.NONE);

		histogramStatus = new HistogramStatus(left, SWT.NONE);
		histogramStatus.setLayout(new GridLayout(1, false));

		statisticsStatus = new StatisticsStatus(left, SWT.NONE);

		Composite right = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(right);
		GridLayoutFactory.fillDefaults().applyTo(right);

		txtPos = new Label(right, SWT.LEFT);
		txtPos.setText("XY value");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(txtPos);
		Composite plotArea = new Composite(right, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(plotArea);

		plotArea.setLayout(new FillLayout());

		this.plottingSystem = PlottingFactory.getLightWeightPlottingSystem();
		plottingSystem.createPlotPart(plotArea, "", parentViewPart.getViewSite().getActionBars(), PlotType.XY,
				parentViewPart);
		plottingSystem.setXfirst(true);
		plottingSystemPositionListener = new IPositionListener() {
			
			@Override
			public void positionChanged(PositionEvent evt) {
				txtPos.setText(String.format("X:%.7g Y:%.7g", evt.x, evt.y));
			}
		};
		plottingSystem.addPositionListener(plottingSystemPositionListener);
		grpMjpegRange = new Group(left, SWT.NONE);
		grpMjpegRange.setText("MJPeg Range");
		grpMjpegRange.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		grpMjpegRange.setLayout(new GridLayout(2, false));

		btnDisplayMJPegRange = new Button(grpMjpegRange, SWT.CHECK);
		btnDisplayMJPegRange.setSelection(true);
		btnDisplayMJPegRange.setText("Display");

		autoScaleBtn = new Button(grpMjpegRange, SWT.PUSH);
		autoScaleBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		autoScaleBtn.setText("Auto-Scale");

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (mpegProcObserver != null) {
					if (mpegProcOffsetObservable != null && mpegProcObserver != null)
						mpegProcOffsetObservable.deleteIObserver(mpegProcObserver);
					if (mpegProcScaleObservable != null && mpegProcObserver != null)
						mpegProcScaleObservable.deleteIObserver(mpegProcObserver);
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
					statsArrayCounterObservable.deleteIObserver(statsArrayCounterObserverStats);
				}
				if (statsArrayCounterObservable != null && statsArrayCounterObserver != null) {
					statsArrayCounterObservable.deleteIObserver(statsArrayCounterObserver);
				}

			}
		});

	}

	public void setADController(ADController config) {
		this.config = config;

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
						Parameterization[] parameterizations = new Parameterization[] { new Parameterization(parameter,
								"i13") };
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

		minCallbackTimeComposite.setPluginBase(config.getImageNDStats().getPluginBase());
		try {
			minCallbackTimeComposite.setMinTimeObservable(config.getImageNDStats().getPluginBase()
					.createMinCallbackTimeObservable());
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
			int histMin = getImageMin();
			int histMax = getImageMax();
			config.getImageNDStats().setHistSize(histSize);
			config.getImageNDStats().setHistMin(histMin);
			config.getImageNDStats().setHistMax(histMax);
			double step = (histMax - histMin) / histSize;
			double[] range = new double[histSize];
			range[0] = histMin;
			for (int i = 1; i < histSize; i++) {
				range[i] = range[i - 1] + step;
			}
			histogramXAxisRange = new DoubleDataset(range);
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
										histogram_RBV = Histogram.this.config.getImageNDStats().getHistogram_RBV(histSize);
									} catch (Exception e) {
										logger.error("Error getting histogram", e);
										return Status.OK_STATUS;
									}

									if (histogram_RBV.length != histogramXAxisRange.getSize()) {
										logger.error("Length of histogram does not match histSize");
										return Status.OK_STATUS;
									}
									DoubleDataset ds = new DoubleDataset(histogram_RBV);
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
									AbstractDataset dsToShow = ds.getSlice(slice);
									dsToShow.setName("");
									AbstractDataset histogramXAxisRangeToShow = histogramXAxisRange.getSlice(slice);
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
		config.getImageNDStats().getPluginBase().enableCallbacks();
		config.getImageNDStats().setComputeStatistics(1);
	}

	public void stopStats() throws Exception {
		config.getImageNDStats().setComputeStatistics(0);
	}

	private double getMPEGProcOffset() throws Exception {
		return config.getLiveViewNDProc().getOffset();
	}

	private double getMPEGProcScale() throws Exception {
		return config.getLiveViewNDProc().getScale();
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
		long min = (long) -offset;
		long max = (long) (255.0 / scale + min);

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
						handleROIChangeEvent(evt);
					} catch (Exception e) {
						logger.error("Error handling change to scaling roi", e);
					}
				}

				private void handleROIChangeEvent(ROIEvent evt) throws Exception {
					final IRegion region = (IRegion) evt.getSource();
					RectangularROI roi = (RectangularROI) region.getROI();
					double min = roi.getPointX();
					double max = min + roi.getLengths()[0];
					double offset = -min;
					double scale = 255.0 / (max - min);
					Histogram.this.config.getLiveViewNDProc().setScale(scale);
					Histogram.this.config.getLiveViewNDProc().setOffset(offset);
					Histogram.this.config.getLiveViewNDProc().setEnableOffsetScale(1);
				}

				@Override
				public void roiChanged(ROIEvent evt) {
					try {
						handleROIChangeEvent(evt);
					} catch (Exception e) {
						logger.error("Error handling change to scaling roi", e);
					}
				}

				@Override
				public void roiSelected(ROIEvent evt) {
				}
			});
			mpegProcOffsetObservable = Histogram.this.config.getLiveViewNDProc().createOffsetObservable();
			mpegProcScaleObservable = Histogram.this.config.getLiveViewNDProc().createScaleObservable();
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
		config.getImageNDStats().setComputeHistogram(0);
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
		NDStats imageNDStats = config.getImageNDStats();
		return imageNDStats.getPluginBase().isCallbacksEnabled_RBV() && imageNDStats.getComputeHistogram_RBV() == 1;
	}

	boolean isComputingStats() throws Exception {
		NDStats imageNDStats = config.getImageNDStats();
		return imageNDStats.getPluginBase().isCallbacksEnabled_RBV() && imageNDStats.getComputeStatistics_RBV() == 1;
	}

	public void start() throws Exception {
		config.getImageNDStats().getPluginBase().enableCallbacks();
		config.getImageNDStats().setComputeHistogram(1);
	}

	/**
	 * Needed for the adapter of the parent view to return IToolPageSystem.class
	 */
	public AbstractPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	public int getHistSize() {
		return config.getImageHistSize();
	}

	public int getImageMin() {
		return config.getImageMin();
	}

	public int getImageMax() {
		return config.getImageMax();
	}

}
