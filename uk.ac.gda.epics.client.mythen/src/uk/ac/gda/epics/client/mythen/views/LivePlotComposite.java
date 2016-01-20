/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.mythen.views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.device.detector.mythen.data.MythenDataFileUtils;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsByteArrayAsStringDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.devices.mythen.visualisation.event.PlotDataFileEvent;

/**
 * Live plot composite for plotting detector data from the data file collected by a server collection process.
 * An instance of this class must be add as an observer of the server process, e.g. a detector that produces 
 * the data file and handle the plot of data from the data file on notification from the server.
 * 
 * It may also <b>OPTIONAL</b> be wired up with an instance of {@link EpicsDetectorRunableWithProgress}
 * to display detector acquiring progress on the status bar using {@link IProgressService} interface.
 */
public class LivePlotComposite extends Composite implements IObserver {
	private Logger logger = LoggerFactory.getLogger(LivePlotComposite.class);
	private String PLOT_TITLE = "Live Detector Data";
	private String plotName = "DetectorData";
	private double xAxisMin = 0.000;
	private double xAxisMax = 100.000;
	private String eventAdminName;
	private IRunnableWithProgress epicsProgressMonitor;
	private EpicsEnumDataListener startListener;

	private Scriptcontroller eventAdmin; // used for passing event from server to client without the need to
												// CORBArise this class.
	private IPlottingSystem plottingSystem;
	private ExecutorService executor;
	
	private EpicsDetectorProgressMonitor progressMonitor;
	private EpicsDoubleDataListener exposureTimeListener;
	private EpicsDoubleDataListener timeRemainingListener;
	private EpicsByteArrayAsStringDataListener messageListener;
	private Scannable stopScannable; 
	private String taskName;

	public LivePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.workbenchpart=part;
		this.setBackground(ColorConstants.white);
		
		GridLayout gridLayout = new GridLayout ();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		this.setLayout (gridLayout);

		
		Composite plotComposite = new Composite(this, SWT.None);
		GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
		plotComposite.setLayoutData(data);
		plotComposite.setLayout(new FillLayout());
		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, getPlotName(), part instanceof IViewPart ? ((IViewPart) part)
				.getViewSite().getActionBars() : null, PlotType.XY, part);
		plottingSystem.setTitle(PLOT_TITLE);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("###.###");
		plottingSystem.setShowLegend(true);
		plottingSystem.getSelectedXAxis().setRange(getxAxisMin(), getxAxisMax());
		
		Composite progressComposite=new Composite(this, SWT.None);
		data = new GridData (SWT.FILL, SWT.CENTER, true, false);
		progressComposite.setLayoutData(data);
		progressComposite.setLayout(new FillLayout());
		progressComposite.setBackground(ColorConstants.cyan);
		progressMonitor=new EpicsDetectorProgressMonitor(progressComposite, SWT.None, true);

	}

	public void initialise() {
		logger.debug("initialise plot composite.");
		if (getEventAdminName() != null) {
			// optional file name observing
			eventAdmin = Finder.getInstance().find(getEventAdminName());
			if (eventAdmin != null) {
				eventAdmin.addIObserver(this); // observe server mythen detector task processes 
				logger.debug("Data filename observer added via script controller {}", getEventAdminName());
				executor=Executors.newFixedThreadPool(1);
			} else {
				logger.debug("Cannot find the script controller {} to add data filename observer",
						getEventAdminName());
			}
		} else {
			throw new IllegalStateException("event admin is required for plotting live data, but not set.");
		}
		//observer for IProgressService
		if (getStartListener()!=null) {
			getStartListener().addIObserver(this);
		}
		progressMonitor.setStartListener(getStartListener());
		progressMonitor.setMessageListener(getMessageListener());
		progressMonitor.setStopScannable(getStopScannable());
		progressMonitor.setTaskName(getTaskName());
		progressMonitor.initialise();
	}

	@Override
	public void dispose() {
		if (eventAdmin!=null) {
			eventAdmin.deleteIObserver(this);
			executor.shutdown();
			boolean terminated;
			try {
				terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
				if (!terminated) {
					throw new TimeoutException("Timed out waiting for plotting data file.t");
				}
			} catch (InterruptedException | TimeoutException e) {
				logger.error("Unable to plot data", e);
				throw new RuntimeException("Unable to plot data from data file.", e);
			} 
		}
		if (getStartListener()!=null) {
			getStartListener().deleteIObserver(this);
		}
		// clean up resources used.
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		plottingSystem.dispose();
		// remove reference
		super.dispose();
	}

	public void clearPlots() {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.setTitle("");
				plottingSystem.reset();
			}
		});
	}

	@Override
	public boolean setFocus() {
		plottingSystem.setFocus();
		return super.setFocus();
	}

	List<Dataset> plotDatasets = new ArrayList<Dataset>();
	private IWorkbenchPart workbenchpart;
	private void plotData(final String filename, final boolean clearFirst) {

		double[][] data = MythenDataFileUtils.readMythenProcessedDataFile(filename, false);
		final int numChannels = data.length;
		double[] angles = new double[numChannels];
		double[] counts = new double[numChannels];
//		double[] errors = new double[numChannels];
		for (int i = 0; i < numChannels; i++) {
			angles[i] = data[i][0];
			counts[i] = data[i][1];
//			errors[i] = data[i][2];
		}
		DoubleDataset x = new DoubleDataset(angles);
		DoubleDataset y = new DoubleDataset(counts);
//		DoubleDataset error = new DoubleDataset(errors);
//		y.setError(error);
		x.setName("delta (deg)");
		y.setName(FilenameUtils.getName(filename));
		if (clearFirst) {
			plotDatasets.clear();
			plottingSystem.clear();
		}
		plotDatasets.add(y);
//		openView();
		plottingSystem.createPlot1D(x, plotDatasets, PLOT_TITLE, new NullProgressMonitor());
	}

	private void openView() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.activate(this.workbenchpart);
	}
	
	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public double getxAxisMin() {
		return xAxisMin;
	}

	public void setxAxisMin(double xAxisMin) {
		this.xAxisMin = xAxisMin;
	}

	public double getxAxisMax() {
		return xAxisMax;
	}

	public void setxAxisMax(double xAxisMax) {
		this.xAxisMax = xAxisMax;
	}

	@Override
	public void update(Object source, Object arg) {
		if (eventAdmin != null && source == eventAdmin && arg instanceof PlotDataFileEvent) {
			final String filename = ((PlotDataFileEvent) arg).getFilename();
			final boolean clearFirst = ((PlotDataFileEvent) arg).isClearFirst();
			Runnable command = new Runnable() {
				
				@Override
				public void run() {
					plotData(filename, clearFirst);
				}
			};
			executor.execute(command);
		} else if (source == getStartListener() && arg instanceof Short) {
//			if (((Short) arg).shortValue() == 1	&& getEpicsProgressMonitor() != null) {
//				final IProgressService service = (IProgressService) workbenchpart.getSite().getService(IProgressService.class);
//				getDisplay().asyncExec(new Runnable() {
//					public void run() {
//						try {
//							service.run(true, true, getEpicsProgressMonitor());
//						} catch (InvocationTargetException
//								| InterruptedException e) {
//							logger.error("Fail to start progress service.", e);
//						}
//					}
//				});
//			}
		}
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public IRunnableWithProgress getEpicsProgressMonitor() {
		return epicsProgressMonitor;
	}

	public void setEpicsProgressMonitor(IRunnableWithProgress epicsProgressMonitor) {
		this.epicsProgressMonitor = epicsProgressMonitor;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}

	public EpicsEnumDataListener getStartListener() {
		return startListener;
	}

	public void setStartListener(EpicsEnumDataListener startListener) {
		this.startListener = startListener;
	}

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	public EpicsDoubleDataListener getExposureTimeListener() {
		return exposureTimeListener;
	}

	public void setExposureTimeListener(EpicsDoubleDataListener exposureTimeListener) {
		this.exposureTimeListener = exposureTimeListener;
	}

	public EpicsDoubleDataListener getTimeRemainingListener() {
		return timeRemainingListener;
	}

	public void setTimeRemainingListener(EpicsDoubleDataListener timeRemainingListener) {
		this.timeRemainingListener = timeRemainingListener;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public EpicsByteArrayAsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsByteArrayAsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}

}
