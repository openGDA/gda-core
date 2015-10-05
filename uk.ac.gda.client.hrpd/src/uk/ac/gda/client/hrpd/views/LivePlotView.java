package uk.ac.gda.client.hrpd.views;

import gda.jython.scriptcontroller.Scriptcontroller;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsStringDataListener;
import uk.ac.gda.client.hrpd.typedpvscannables.EpicsEnumPVScannable;
import uk.ac.gda.hrpd.cvscan.event.FileNumberEvent;
/**
 * Live plotting of detector data during acquisition. 
 * It consists of two parts:
 * <p>
 * <ol>
 * <li>It plots multiple traces of data from specified EPICS data listeners of {@link EpicsDoubleDataArrayListener} type at the top</li>.
 * <li>It displays a process progress monitor at the bottom</li>
 * </ol> 
 * </p>
 * All traces are updated at the same time in the plot based on an event provided by another PV listener of 
 * {@link EpicsIntegerDataListener} type. 
 * 
 * It also OPTIONAL plots the final reduced data at end of the collection, triggered by a specified 
 * detector state if reduced data listener exists. 
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
public class LivePlotView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(LivePlotView.class);

	private String plotName;
	private double xAxisMin=0.000;
	private double xAxisMax=150.000;
	private List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners = new ArrayList<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();
	private EpicsIntegerDataListener dataUpdatedListener;
	private Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener;
	private EpicsEnumDataListener detectorStateListener;
	private String detectorStateToPlotReducedData;
	private String detectorStateToRunProgressService;
	private IRunnableWithProgress epicsProgressMonitor;
	private String dataFilenameObserverName;
	private int lowDataBound;
	private int highDataBound;
	
	private LivePlotComposite plotComposite;

	private EpicsIntegerDataListener totalWorkListener;
	private EpicsIntegerDataListener workListener;
	private EpicsStringDataListener messageListener;
	private EpicsEnumPVScannable stopScannable;	

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
			plotComposite.setDetectorStateToPlotReducedData(getDetectorStateToPlotReducedData());
			plotComposite.setDetectorStateToRunProgressService(getDetectorStateToRunProgressService());
			plotComposite.setDataFilenameObserverName(getDataFilenameObserverName());
			plotComposite.setLowDataBound(getLowDataBound());
			plotComposite.setHighDataBound(getHighDataBound());
			plotComposite.setTotalWorkListener(getTotalWorkListener());
			plotComposite.setWorkListener(getWorkListener());
			plotComposite.setMessageListener(getMessageListener());
			plotComposite.setStopScannable(getStopScannable());
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

	public IRunnableWithProgress getEpicsProgressMonitor() {
		return epicsProgressMonitor;
	}

	public void setEpicsProgressMonitor(IRunnableWithProgress epicsProgressMonitor) {
		this.epicsProgressMonitor = epicsProgressMonitor;
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

	public EpicsEnumPVScannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(EpicsEnumPVScannable stopScannable) {
		this.stopScannable = stopScannable;
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

}
