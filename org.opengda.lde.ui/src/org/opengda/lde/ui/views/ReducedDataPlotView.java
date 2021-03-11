package org.opengda.lde.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.ColorConstants;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.events.NewDataFileEvent;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObservable;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class ReducedDataPlotView extends ViewPart implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ReducedDataPlotView.class);
	public static final String ID = "org.opengda.lde.ui.views.reducdeddataplotview";
	private IPlottingSystem<Composite> plotting;
	/** Names and internal paths to data */
	private ReducedDataConfig config;
	private IObservable eventSource;
	private LDEResourceUtil resUtil;

	public ReducedDataPlotView() {
		setTitleToolTip("Live display of integrated spectrum");
		setPartName("Reduced Data");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		Composite plotComposite = new Composite(rootComposite, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());
		try {
			plotting = PlottingFactory.createPlottingSystem();
			plotting.createPlotPart(plotComposite, "Reduced Data", getViewSite().getActionBars(), PlotType.XY_STACKED, this);
			plotting.setTitle("Reduced Data");
			plotting.getSelectedYAxis().setFormatPattern("######.#");
			plotting.getSelectedXAxis().setFormatPattern("#####.#");
		} catch (Exception e) {
			logger.error("Could not create plotting system", e);
		}
	}

	@Override
	public void update(Object source, final Object arg) {
		if (source == eventSource && arg instanceof NewDataFileEvent) {
			Async.execute(() -> {
					try {
						updatePlot(((NewDataFileEvent)arg).getFilename());
					} catch (InterruptedException ie) {
						logger.error("Thread interrupted while updating plot {}", getPartName(), ie);
						Thread.currentThread().interrupt();
					} catch (DatasetException e) {
						logger.error("Could not read reduced data from file", e);
					}
			});
		}
	}


	private void updatePlot(String filename)
			throws InterruptedException, DatasetException {
		File file = new File(filename);
		long starttime = System.currentTimeMillis();
		while (!file.exists()) {
			Thread.sleep(50);
			if (System.currentTimeMillis() - starttime > 10_000) {
				logger.error("Timeout: Cannot find file '{}' within 10 seconds.", filename);
				return;
			}
		}
		IDataHolder dataHolder = null;
		try {
			dataHolder = LoaderFactory.getData(filename);
		} catch (Exception e) {
			logger.error("Exception loading data from file {}", filename);
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		IDataset xAxis = dataHolder.getLazyDataset(config.xPath).getSlice((Slice) null);
		xAxis.setName(config.xName);
		IDataset yds = dataHolder.getLazyDataset(config.yPath).getSlice((Slice)null);
		yds.setName(config.yName);
		ArrayList<IDataset> plotDataSets = new ArrayList<>();
		plotDataSets.add(yds);
		plotting.clear();
		final List<ITrace> profileLineTraces = plotting.createPlot1D(xAxis, plotDataSets, new NullProgressMonitor());
		if (!profileLineTraces.isEmpty() && !Display.getDefault().isDisposed()) {
			Display.getDefault().asyncExec(() -> {
					plotting.setShowLegend(false);
					plotting.setTitle(file.getName());
					ILineTrace profileLineTrace = (ILineTrace) profileLineTraces.get(0);
					profileLineTrace.setTraceColor(ColorConstants.blue);
			});
		}
	}

	@Override
	public void setFocus() {
		plotting.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		plotting.dispose();
		if (eventSource != null) eventSource.deleteIObserver(this);
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public IObservable getEventSource() {
		return eventSource;
	}

	public void setEventSource(IObservable eventSource) {
		if (this.eventSource != null) this.eventSource.deleteIObserver(this);
		this.eventSource = eventSource;
		if (this.eventSource != null) this.eventSource.addIObserver(this);
	}

	public ReducedDataConfig getConfig() {
		return config;
	}

	public void setConfig(ReducedDataConfig config) {
		this.config = config;
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}

	/** Data class to contain names and paths to reduced data */
	public static class ReducedDataConfig {
		private String xName;
		private String yName;
		private String xPath;
		private String yPath;
		public ReducedDataConfig(String xName, String yName, String xPath, String yPath) {
			this.xName = xName;
			this.yName = yName;
			this.xPath = xPath;
			this.yPath = yPath;
		}
	}
}
