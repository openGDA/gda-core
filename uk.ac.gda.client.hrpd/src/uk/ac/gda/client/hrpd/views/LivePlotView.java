package uk.ac.gda.client.hrpd.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.hrpd.cvscan.event.FileNumberEvent;
/**
 * A View to display live plotting of detector data while acquiring. It plots multiple traces of data from specified EPICS detector data listeners of {@link EpicsDoubleDataArrayListener} type.
 * All traces are updated at the same time in the plot triggered by another PV listener of {@link EpicsIntegerDataListener} type.
 * It also plots the final reduced data set at end of a collection process, triggered by a specified detector state returned by {@link EpicsEnumDataListener} instance if reduced data listener exists.
 * <li>view name is configurable {@link #setPlotName(String)}</li>
 * <li>X-axis limits are configurable using {@link #setxAxisMin(double)} (default 0.0) and {@link #setxAxisMax(double)} (default 150.0)</li>
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
public class LivePlotView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(LivePlotView.class);

	private String plotName;
	private double xAxisMin=0.000;
	private double xAxisMax=150.000;
	private List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners = new ArrayList<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();
	private EpicsIntegerDataListener dataUpdatedListener;
	private Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener;
	private EpicsEnumDataListener detectorStateListener;
	private String dataFilenameObserverName;
	private int lowDataBound;
	private int highDataBound;
	
	private LivePlotComposite plotComposite;	

	public LivePlotView() {
		setTitleToolTip("live display of 1D detector data");
		// setContentDescription("A view for displaying integrated spectrum.");
	}

	@Override
	public void createPartControl(Composite parent) {
		setPartName(getPlotName());
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			plotComposite = new LivePlotComposite(this, rootComposite, SWT.None);
			plotComposite.setPlotName(getPlotName());
			plotComposite.setxAxisMin(getxAxisMin());
			plotComposite.setxAxisMax(getxAxisMax());
			plotComposite.setLiveDataListeners(getLiveDataListeners());
			plotComposite.setDataUpdatedListener(getDataUpdatedListener());
			plotComposite.setFinalDataListener(getFinalDataListener());
			plotComposite.setDetectorStateListener(getDetectorStateListener());
			plotComposite.setDataFilenameObserverName(getDataFilenameObserverName());
			plotComposite.setLowDataBound(getLowDataBound());
			plotComposite.setHighDataBound(getHighDataBound());
			plotComposite.initialise();
			
		} catch (Exception e) {
			logger.error("Cannot create live plot composite.", e);
		}
	}
	
	@Override
	public void setFocus() {
		plotComposite.setFocus();
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

	public List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> getLiveDataListeners() {
		return liveDataListeners;
	}

	public void setLiveDataListeners(List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners) {
		this.liveDataListeners = liveDataListeners;
	}

	public EpicsIntegerDataListener getDataUpdatedListener() {
		return dataUpdatedListener;
	}

	public void setDataUpdatedListener(EpicsIntegerDataListener dataUpdatedListener) {
		this.dataUpdatedListener = dataUpdatedListener;
	}

	public Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> getFinalDataListener() {
		return finalDataListener;
	}

	public void setFinalDataListener(Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener) {
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
