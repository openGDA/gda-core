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

import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.hrpd.cvscan.EpicsCVScanState;
import uk.ac.gda.hrpd.cvscan.event.FileNumberEvent;

import com.google.common.base.Joiner;
/**
 * Live plotting of detector data while acquiring. It plots multiple traces of data from specified EPICS detector data listeners of {@link EpicsDoubleDataArrayListener} type.
 * All traces are updated at the same time in the plot triggered by another PV listener of {@link EpicsIntegerDataListener} type.
 * It also plots the final reduced data set at end of a collection process, triggered by a specified detector state {@link EpicsCVScanState#Flyback} if reduced data listener exists.
 * <li>view name is configurable {@link #setPlotName(String)}</li>
 * <li>X-axis limits are configurable using {@link #setxAxisMin(double)} (defualt 0.0) and {@link #setxAxisMax(double)} (default 150.0)</li>
 * <li><b>MUST</b> specify live traces using {@link #setLiveDataListeners(List)} list of {@link Triplet} of {@link String} trace name, {@link EpicsDoubleDataArrayListener} x dataset, and {@link EpicsDoubleDataArrayListener} y dataset</li>
 * <li><b>MUST</b> specify live plot update control using {@link EpicsIntegerDataListener} instance</li>
 * <li>Specify <b>OPTIONAL</b> reduced dataset using {@link Pair} of {@link EpicsDoubleDataArrayListener} x dataset, and {@link EpicsDoubleDataArrayListener} y dataset</li>
 * <li>specify <b>OPTIONAL</b> reduced data plotting using {@link EpicsEnumDataListener} instance</li>
 * <li>Specify <b>OPTIONAL</b> data filename observer of {@link String} type to handle data file name changed event {@link FileNumberEvent} for title/legend display</li>
 * 
 * for MAC data, stage 'mac1' requires special data slice to get ride of negative detector positions. These slice limits can be set by
 * <li> {@link #setLowDataBound(int)}</li>
 * <li> {@link #setHighDataBound(int)}</li>
 */
public class LivePlotComposite extends Composite implements IObserver {
	private Logger logger = LoggerFactory.getLogger(LivePlotComposite.class);
	private String PLOT_TITLE = "Live Detector Data";
	private String plotName="DetectorData";
	private double xAxisMin=0.000;
	private double xAxisMax=150.000;
	private IPlottingSystem plottingSystem;
	private List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners = new ArrayList<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();
	private EpicsIntegerDataListener dataUpdatedListener;
	
	private Pair<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener;
	private EpicsEnumDataListener detectorStateListener;
	
	private Scriptcontroller scriptController; //used for passing event from server to client without the need to CORBArise this class.
	private String dataFilenameObserverName;
	private int lowDataBound;
	private int highDataBound;
	
	
	public LivePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, getPlotName(), part instanceof IViewPart ? ((IViewPart) part)
				.getViewSite().getActionBars() : null, PlotType.XY, part);
		plottingSystem.setTitle(PLOT_TITLE);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("###.###");
		plottingSystem.setShowLegend(true);
		plottingSystem.getSelectedXAxis().setRange(getxAxisMin(), getxAxisMax());
	}
	public void initialize() {
		if (getDataFilenameObserverName()!=null) {
			//optional file name observing
			scriptController = Finder.getInstance().find(getDataFilenameObserverName());
			if (scriptController!=null) {
				scriptController.addIObserver(this); //observe server cvscan process for data file name changes.
				logger.debug("Data filename observer added via script controller {}", getDataFilenameObserverName());
			} else {
				logger.debug("Cannot find the script controller {} to add data filename observer", getDataFilenameObserverName());
			}
		}
		if(detectorStateListener!=null) {
			//optional reduced data observing
			detectorStateListener.addIObserver(this);
			logger.debug("detector state observer {} added", detectorStateListener.getName());
		} else {
			logger.debug("No detector state observer added");
		}
		//must have live updated data available observing 
		dataUpdatedListener.addIObserver(this);
	}

	@Override
	public void dispose() {
		//clean up resources used.
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		for (Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> listener : getLiveDataListeners()) {
			listener.getValue1().dispose();
			listener.getValue2().dispose();
		}
		if (finalDataListener!=null) {
			finalDataListener.getValue0().dispose();
			finalDataListener.getValue1().dispose();
		}
		dataUpdatedListener.dispose();
		if (detectorStateListener!=null) {
			detectorStateListener.dispose();
		}
		dataDisplayers.clear();
		liveDataListeners.clear();
		plottingSystem.dispose();
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
						Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace> with = item.add(trace);
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
								trace.setData(new DoubleDataset(x.getValue()).getSlice(new int[] {getLowDataBound()}, new int[] {getHighDataBound()}, new int[] {1}),
										new DoubleDataset(y.getValue()).getSlice(new int[] {getLowDataBound()}, new int[] {getHighDataBound()}, new int[] {1}));
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
						DoubleDataset x = new DoubleDataset(finalDataListener.getValue0().getValue());
						DoubleDataset y = new DoubleDataset(finalDataListener.getValue1().getValue());
						y.setName(legend);
						List<AbstractDataset> plotDatasets=new ArrayList<AbstractDataset>();
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
			int intValue = ((Integer)arg).intValue();
			if (intValue==0) {
				createTraces();
			} else {
				updateLivePlot();
			}
		} else if (scriptController!=null && source==scriptController && arg instanceof FileNumberEvent) {
			Joiner stringJoiner = Joiner.on("-").skipNulls();
			PLOT_TITLE=stringJoiner.join(((FileNumberEvent)arg).getFilename(),String.format("%03d", ((FileNumberEvent)arg).getCollectionNumber()));
			plottingSystem.setTitle(PLOT_TITLE);
		} else if(detectorStateListener!=null && source==detectorStateListener && arg instanceof Short) {
			short shortValue = ((Short)arg).shortValue();
			if (EpicsCVScanState.values()[shortValue]==EpicsCVScanState.Flyback && finalDataListener != null){
				String legend=PLOT_TITLE;
				PLOT_TITLE="Reduced MAC Data";
				plotFinalData(legend);
			}
		}
	}
	
	public Pair<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> getFinalDataListener() {
		return finalDataListener;
	}
	public void setFinalDataListener(Pair<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener) {
		this.finalDataListener = finalDataListener;
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
	public void setLiveDataListeners(List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners) {
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


}
