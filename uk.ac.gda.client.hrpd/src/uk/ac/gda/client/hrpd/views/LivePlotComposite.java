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

package uk.ac.gda.client.hrpd.views;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.IProgressService;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsStringDataListener;
import uk.ac.gda.hrpd.cvscan.EpicsCVScanState;
import uk.ac.gda.hrpd.cvscan.event.FileNumberEvent;

import com.google.common.base.Joiner;

/**
 * Live plot composite for plotting detector data while acquiring. It is capable of plotting <b>multiple</b> traces of data from 
 * specified EPICS data listeners of {@link EpicsDoubleDataArrayListener} type. 
 * 
 * All traces are updated at the same time in the plot triggered by another PV listener of 
 * {@link EpicsIntegerDataListener} type signalling all traces are updated in the data listener list. 
 * 
 * It also provides <b>option</b> to plot the final reduced data at end of a collection. The available of this
 * reduced data is signalled by the detector state {@link EpicsCVScanState#Flyback} if reduced data listener exists. 
 * <p>
 * <li>view name is configurable using <code>setPlotName(String)</code> method;</li> 
 * <li>X-axis limits are configurable using <code>setxAxisMin(double)</code> (defualt 0.0) and 
 * <code>setxAxisMax(double)</code> (default 150.0);</li>
 * <li><b>MUST</b>specify live traces using <code>setLiveDataListeners(List)</code> list of {@link Triplet} of {@link String} trace name,
 * {@link EpicsDoubleDataArrayListener} x dataset, and {@link EpicsDoubleDataArrayListener} y dataset;</li> 
 * <li><b>MUST</b> specify live plot update control using <code>setDataUpdatedListener(EpicsIntegerDataListener)</code>;</li> 
 * <li>Specify <b>OPTIONAL</b> reduced dataset using <code>setFinalDataListener(Triplet)</code> of 
 * {@link EpicsDoubleDataArrayListener} x dataset, * {@link EpicsDoubleDataArrayListener} y dataset, 
 * and {@link EpicsDoubleDataArrayListener} error dataset</li> 
 * <li>specify <b>OPTIONAL</b> reduced data plotting using <code>setDetectorStateListener(EpicsEnumDataListener)</code>;</li> 
 * <li>Specify <b>OPTIONAL</b> data filename observer using <code>setDataFilenameObserverName(String)</code> 
 * to handle data file name changed event {@link FileNumberEvent} from an {@link Scriptcontroller} instance on the server
 * for title/legend display if required;</li>
 * <li><b>OPTIONAL</b> special data trimming or truncation also available for trace named 'mac1' to filter-out unwanted data using 
 * <code>setLowDataBound(int)</code> and <code>setHighDataBound(int)</code> methods;</li>
 * <li><b>OPTIONAL</b> EPICS progress monitor to be displayed on the status bar using {@link IProgressService} interface.</li>
 * </p>
 */
public class LivePlotComposite extends Composite implements IObserver {
	private Logger logger = LoggerFactory.getLogger(LivePlotComposite.class);
	private String PLOT_TITLE = "Live Detector Data";
	private String plotName = "DetectorData";
	private double xAxisMin = 0.000;
	private double xAxisMax = 150.000;
	
	private List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners = new ArrayList<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();
	private EpicsIntegerDataListener dataUpdatedListener;

	private Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener;
	private EpicsEnumDataListener detectorStateListener;
	private String detectorStateToPlotReducedData;
	private String detectorStateToRunProgressService;

	private String dataFilenameObserverName;
	private IRunnableWithProgress epicsProgressMonitor;

	private Scriptcontroller scriptController; // used for passing event from server to client without the need to
												// CORBArise this class.
	private int lowDataBound;
	private int highDataBound;

	private IPlottingSystem plottingSystem;
	private IWorkbenchPart workbenchpart;
	private EpicsProcessProgressMonitor progressMonitor;
	private EpicsIntegerDataListener totalWorkListener;
	private EpicsIntegerDataListener workListener;
	private EpicsStringDataListener messageListener;
	private Scannable stopScannable;	

	public LivePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.workbenchpart = part;
		this.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

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
		progressMonitor=new EpicsProcessProgressMonitor(progressComposite, null, true);
		progressMonitor.setTotalWorkListener(getTotalWorkListener());
		progressMonitor.setWorkedSoFarListener(getWorkListener());
		progressMonitor.setMessageListener(getMessageListener());
		progressMonitor.setStopScannable(getStopScannable());
}

	public void initialise() {
		if (getDataFilenameObserverName() != null) {
			// optional file name observing
			scriptController = Finder.getInstance().find(getDataFilenameObserverName());
			if (scriptController != null) {
				scriptController.addIObserver(this); // observe server cvscan process for data file name changes.
				logger.debug("Data filename observer added via script controller {}", getDataFilenameObserverName());
			} else {
				logger.debug("Cannot find the script controller {} to add data filename observer",
						getDataFilenameObserverName());
			}
		}
		if (detectorStateListener != null) {
			// optional reduced data observing
			detectorStateListener.addIObserver(this);
			logger.debug("detector state observer {} added", detectorStateListener.getName());
		} else {
			logger.debug("No detector state observer added");
		}
		// must have live updated data available observing
		dataUpdatedListener.addIObserver(this);
		progressMonitor.addIObservers();
	}

	@Override
	public void dispose() {
		// clean up resources used.
		if (scriptController != null) {
			scriptController.deleteIObserver(this); 
			logger.debug("Data filename observer removed from {}", getDataFilenameObserverName());
		} else {
			logger.debug("Cannot find the script controller {} to remove data filename observer",
					getDataFilenameObserverName());
		}
		if (detectorStateListener != null) {
			detectorStateListener.deleteIObserver(this);
			logger.debug("detector state observer removed from {}", detectorStateListener.getName());
		} else {
			logger.debug("No detector state observer removed");
		}
		dataUpdatedListener.deleteIObserver(this);
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		plottingSystem.dispose();
		// remove reference
		dataDisplayers.clear();
		liveDataListeners.clear();
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

	private void createTraces() {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					plottingSystem.clear();
					for (Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> item : getLiveDataListeners()) {
						ILineTrace trace = plottingSystem.createLineTrace(item.getValue0());
						Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace> with = item
								.add(trace);
						plottingSystem.addTrace(trace);
						dataDisplayers.add(with);
					}
					plottingSystem.setTitle(PLOT_TITLE);
				}
			});
		}
	}

	private List<Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace>> dataDisplayers = new ArrayList<Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace>>();

	private void updateLivePlot() {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					boolean visible = LivePlotComposite.this.isVisible();
					if (visible) {
						for (Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace> listener : dataDisplayers) {
							String traceName = listener.getValue0();
							EpicsDoubleDataArrayListener x = listener.getValue1();
							EpicsDoubleDataArrayListener y = listener.getValue2();
							ILineTrace trace = listener.getValue3();
							if (traceName.equalsIgnoreCase("mac1")) {
								trace.setData(new DoubleDataset(x.getValue()).getSlice(new int[] { getLowDataBound() },
										new int[] { getHighDataBound() }, new int[] { 1 }),
										new DoubleDataset(y.getValue()).getSlice(new int[] { getLowDataBound() },
												new int[] { getHighDataBound() }, new int[] { 1 }));
							} else {
								trace.setData(new DoubleDataset(x.getValue()), new DoubleDataset(y.getValue()));
							}

						}
						plottingSystem.repaint();
					}
				}
			});
		}
	}

	private void plotFinalData(final String legend) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					boolean visible = LivePlotComposite.this.isVisible();
					if (visible) {
						plottingSystem.clear();
						plottingSystem.setTitle(PLOT_TITLE);
						DoubleDataset x = new DoubleDataset(getFinalDataListener().getValue0().getValue());
						DoubleDataset y = new DoubleDataset(getFinalDataListener().getValue1().getValue());
						DoubleDataset error = new DoubleDataset(getFinalDataListener().getValue2().getValue());
						y.setError(error);
						y.setName(legend);
						List<Dataset> plotDatasets = new ArrayList<Dataset>();
						plotDatasets.add(y);
						plottingSystem.createPlot1D(x, plotDatasets, PLOT_TITLE, new NullProgressMonitor());
					}
				}
			});
		}

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

	public EpicsIntegerDataListener getDataUpdatedListener() {
		return dataUpdatedListener;
	}

	public void setDataUpdatedListener(EpicsIntegerDataListener dataUpdatedListener) {
		this.dataUpdatedListener = dataUpdatedListener;
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == dataUpdatedListener && arg instanceof Integer) {
			int intValue = ((Integer) arg).intValue();
			if (intValue == 0) {
				createTraces();
			} else {
				updateLivePlot();
			}
		} else if (scriptController != null && source == scriptController && arg instanceof FileNumberEvent) {
			Joiner stringJoiner = Joiner.on("-").skipNulls();
			PLOT_TITLE = stringJoiner.join(((FileNumberEvent) arg).getFilename(),
					String.format("%03d", ((FileNumberEvent) arg).getCollectionNumber()));
			plottingSystem.setTitle(PLOT_TITLE);
		} else if (detectorStateListener != null && source == detectorStateListener && arg instanceof Short) {
			short shortValue = ((Short) arg).shortValue();
			if (detectorStateListener.getPositions()[shortValue].equals(getDetectorStateToPlotReducedData()) && getFinalDataListener() != null) {
				String legend = PLOT_TITLE;
				PLOT_TITLE = "Reduced MAC Data";
				plotFinalData(legend);
			} else if (detectorStateListener.getPositions()[shortValue].equals(getDetectorStateToRunProgressService())) {
				if (getEpicsProgressMonitor() != null) {
					try { 
						IProgressService service = (IProgressService) workbenchpart.getSite().getService(IProgressService.class);
						service.run(true, true, getEpicsProgressMonitor());
					} catch (InvocationTargetException | InterruptedException e) {
						logger.error("TODO put description of error here", e);
					}
				}
			}
		}
	}

	public EpicsEnumDataListener getDetectorStateListener() {
		return detectorStateListener;
	}

	public void setDetectorStateListener(EpicsEnumDataListener detectorStateListener) {
		this.detectorStateListener = detectorStateListener;
	}

	public String getDataFilenameObserverName() {
		return dataFilenameObserverName;
	}

	public void setDataFilenameObserverName(String dataFilenameObserverName) {
		this.dataFilenameObserverName = dataFilenameObserverName;
	}

	public List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> getLiveDataListeners() {
		return liveDataListeners;
	}

	public void setLiveDataListeners(
			List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners) {
		this.liveDataListeners = liveDataListeners;
	}

	public int getLowDataBound() {
		return lowDataBound;
	}

	public void setLowDataBound(int lowDataBound) {
		this.lowDataBound = lowDataBound;
	}

	public int getHighDataBound() {
		return highDataBound;
	}

	public void setHighDataBound(int highDataBound) {
		this.highDataBound = highDataBound;
	}

	public void setFinalDataListener(
			Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener) {
		this.finalDataListener = finalDataListener;
	}
	public Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> getFinalDataListener() {
		return finalDataListener;
	}

	public IRunnableWithProgress getEpicsProgressMonitor() {
		return epicsProgressMonitor;
	}

	public void setEpicsProgressMonitor(IRunnableWithProgress epicsProgressMonitor) {
		this.epicsProgressMonitor = epicsProgressMonitor;
	}

	public String getDetectorStateToPlotReducedData() {
		return detectorStateToPlotReducedData;
	}

	public void setDetectorStateToPlotReducedData(String detectorStateToPlotReducedData) {
		this.detectorStateToPlotReducedData = detectorStateToPlotReducedData;
	}

	public String getDetectorStateToRunProgressService() {
		return detectorStateToRunProgressService;
	}

	public void setDetectorStateToRunProgressService(String detectorStateToRunProgressService) {
		this.detectorStateToRunProgressService = detectorStateToRunProgressService;
	}

	public EpicsIntegerDataListener getTotalWorkListener() {
		return totalWorkListener;
	}

	public void setTotalWorkListener(EpicsIntegerDataListener totalWorkListener) {
		this.totalWorkListener = totalWorkListener;
	}

	public EpicsIntegerDataListener getWorkListener() {
		return workListener;
	}

	public void setWorkListener(EpicsIntegerDataListener workListener) {
		this.workListener = workListener;
	}

	public EpicsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}
}
