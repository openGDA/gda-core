package uk.ac.gda.epics.client.mythen.views;

import gda.device.Scannable;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsByteArrayAsStringDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;


/**
 * This view consists of two <code>Composite</code> parts. The top part shows plot of 
 * detector data from data file collected by a server process, and the bottom part displays 
 * the progress of detector acquiring for data if acquisition is over 1 seconds.
 */
public class LivePlotView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(LivePlotView.class);

	private String plotName;
	private double xAxisMin=0.000;
	private double xAxisMax=100.000;
	private String eventAdminName;
	private IRunnableWithProgress epicsProgressMonitor;
	private EpicsEnumDataListener startListener;	
	
	private LivePlotComposite plotComposite;

	private EpicsDoubleDataListener exposureTimeListener;
	private EpicsDoubleDataListener timeRemainingListener;
	private EpicsByteArrayAsStringDataListener messageListener;
	private Scannable stopScannable;
	private String taskName;



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
			logger.debug("create plot composite.");
			plotComposite = new LivePlotComposite(this, rootComposite, SWT.None);
			plotComposite.setPlotName(getPlotName());
			plotComposite.setxAxisMin(getxAxisMin());
			plotComposite.setxAxisMax(getxAxisMax());
			plotComposite.setEventAdminName(eventAdminName);
			plotComposite.setEpicsProgressMonitor(epicsProgressMonitor);
			plotComposite.setStartListener(getStartListener());
			plotComposite.setExposureTimeListener(getExposureTimeListener());
			plotComposite.setTimeRemainingListener(getTimeRemainingListener());
			plotComposite.setMessageListener(getMessageListener());
			plotComposite.setStopScannable(getStopScannable());
			plotComposite.setTaskName(getTaskName());
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

	public IRunnableWithProgress getEpicsProgressMonitor() {
		return epicsProgressMonitor;
	}

	public void setEpicsProgressMonitor(IRunnableWithProgress epicsProgressMonitor) {
		this.epicsProgressMonitor = epicsProgressMonitor;
	}

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
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

	public EpicsEnumDataListener getStartListener() {
		return startListener;
	}

	public void setStartListener(EpicsEnumDataListener startListener) {
		this.startListener = startListener;
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
