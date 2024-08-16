package uk.ac.diamond.daq.arpes.ui.e4.dispatcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;
import uk.ac.gda.apres.ui.config.AnalyserPVConfig;

public class ArpesLiveDataDispatcherE4 extends FindableConfigurableBase {
	private static final Logger logger = LoggerFactory.getLogger(ArpesLiveDataDispatcherE4.class);
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>(1));
	private final EpicsController epicsController = EpicsController.getInstance();
	private IEventBroker eventBroker;

	private LiveDataPlotUpdate dataUpdate = new LiveDataPlotUpdate();

	private boolean monitorIterationProgress = false;
	private boolean sumFrames = false; // false by default to maintain backwards compatibility with Spring config
	private Dataset summedFrames;
	private AcquisitionMode acquisitionMode = AcquisitionMode.FIXED;
	private String analyserManufacturer;

	// set in spring xml configuration
	private String arrayPV;
	private String frameNumberPV;
	private String frameNumberPVSwept;
	private String numScansPV;
	private String progressCounterPV;
	private String acquirePV;
	private String eventTopic;
	private AnalyserPVConfig analyserPVConfig;

	/** Map that stores the channel against the PV name */
	private final Map<String, Channel> channelMap = new HashMap<>();

	private Runnable configureEventBroker() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			logger.error(e2.toString());
		}
		eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
		setConfigured(true);
		return null;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			// iterations monitor
			if (monitorIterationProgress) {
				getChannel(numScansPV);
				getChannel(progressCounterPV);
			}
			// create channel for images
			getChannel(getArrayPV());
			// create channels from analyserPVprovider
			getChannel(getAnalyserEnergyAxisPv());
			getChannel(getAnalyserEnergyAxisCountPv());
			getChannel(getAnalyserAngleAxisPv());
			getChannel(getAnalyserAngleAxisCountPv());
			getChannel(getAnalyserLensModePv());

			// when this channel fires we need to get image
			epicsController.setMonitor(getChannel(frameNumberPV), evt -> {
				try {
					executor.submit(this::emitNewData); // use executor otherwise channel not connected errors
				} catch (RejectedExecutionException ree) {
					logger.trace("Exception for rejected execution", ree);
				}
			});

			// set monitor on acquisition mode - need manufacturer as Scienta AcqMode enums
			// are opposite to MBS
			analyserManufacturer = epicsController.cagetString(getChannel(getAnalyserManufacturerPv()));
			epicsController.setMonitor(getChannel(getAnalyserAcquisitionModePv()), this::setAcquisitionMode);

			// If we are accumulating frames need to know when a new acquisition starts so
			// we can clear the summedFrames
			epicsController.setMonitor(getChannel(acquirePV), this::acquireStatusChanged);

		} catch (Exception e) {
			logger.error("Error setting up analyser live visualisation", e);
			throw new FactoryException("Cannot set up monitoring of arrays", e);
		}
		// delay configuring eventBroker until workbench is available
		executor.submit(this::configureEventBroker);
	}

	private Channel getChannel(String pvName) {
		return channelMap.computeIfAbsent(pvName, t -> {
			try {
				return epicsController.createChannel(pvName);
			} catch (CAException | TimeoutException e) {
				logger.error("Failed to create channel: " + pvName, e);
				return null;
			}
		});
	}

	public String getProgressCounterPV() {
		return progressCounterPV;
	}

	public void setProgressCounterPV(String progressCounterPV) {
		this.progressCounterPV = progressCounterPV;
	}

	public String getNumScansPV() {
		return numScansPV;
	}

	public void setNumScansPV(String numScansPV) {
		this.numScansPV = numScansPV;
	}

	private void emitNewData() {
		try {
			IDataset xAxis = getXAxis();
			IDataset yAxis = getYAxis();
			IDataset data = getArrayAsDataset(yAxis.getShape()[0], xAxis.getShape()[0]);

			dataUpdate.resetLiveDataUpdate();
			dataUpdate.setxAxis(xAxis);
			dataUpdate.setyAxis(yAxis);
			dataUpdate.setAcquisitionMode(acquisitionMode);

			// This bit is for Slicing view or any view that wants to get progress on
			// iterations
			if (monitorIterationProgress) {
				int totalScans = epicsController.cagetInt(getChannel(numScansPV));
				if (totalScans > 1) {
					int progressScans = epicsController.cagetInt(getChannel(progressCounterPV));
					dataUpdate.setUpdateSameFrame(progressScans != 1); // this is a bug fix that sometimes instead of
																		// max counter ioc returns 0.
				}
				dataUpdate.setData(data);// rely on PV that updates with accumulated data with iterations
			} else {
				if (isSumFrames()) {
					// summed
					if (summedFrames == null) { // i.e A new acquire has just started
						summedFrames = (Dataset) data;
					} else {
						// Add the new data to the existing summed data
						summedFrames.iadd(data);
					}
					dataUpdate.setData(summedFrames);
				} else {
					// fixed or swept
					dataUpdate.setData(data);
				}
			}
			eventBroker.post(eventTopic, dataUpdate);
		} catch (Exception e) {
			logger.error("Failed to prepare/send VGScientaLivePlotData update ", e);
		}
	}

	public boolean isMonitorIterationProgress() {
		return monitorIterationProgress;
	}

	public void setMonitorIterationProgress(boolean monitorIterationProgress) {
		this.monitorIterationProgress = monitorIterationProgress;
	}

	private void acquireStatusChanged(final MonitorEvent event) {
		logger.debug("Received change of acquire state: {}", event);
		// Remove the existing summed data if there is new Start acquire
		if (((short[]) event.getDBR().getValue())[0] == 1) {
			summedFrames = null;
		}
	}

	private IDataset getArrayAsDataset(int x, int y) throws Exception {
		int[] dims = new int[] { x, y };
		int arraySize = dims[0] * dims[1];
		if (arraySize < 1) {
			throw new IllegalArgumentException(String.format("arraySize was less than 1. x=%d y=%d", x, y));
		}
		// Get as float[] not double[] for performance
		float[] array = epicsController.cagetFloatArray(getChannel(getArrayPV()), arraySize);
		Dataset newData = DatasetFactory.createFromObject(array, dims);
		// Flip the data across the 0 position of the Y axis I05-221
		return DatasetUtils.flipUpDown(newData);
	}

	private IDataset getXAxis() throws Exception {
		double[] xdata = epicsController.cagetDoubleArray(getChannel(getAnalyserEnergyAxisPv()),
				epicsController.cagetInt(getChannel(getAnalyserEnergyAxisCountPv())));
		IDataset xAxis = DatasetFactory.createFromObject(xdata);
		xAxis.setName("energies (eV)");
		return xAxis;
	}

	private IDataset getYAxis() throws Exception {
		double[] ydata = epicsController.cagetDoubleArray(getChannel(getAnalyserAngleAxisPv()),
				epicsController.cagetInt(getChannel(getAnalyserAngleAxisCountPv())));
		// Get as a list so we can reverse it easily
		List<Double> list = Arrays.stream(ydata).boxed().collect(Collectors.toList());
		Collections.reverse(list); // Flips the Y scale I05-221
		IDataset yAxis = DatasetFactory.createFromObject(list);
		if ("Transmission".equalsIgnoreCase(epicsController.cagetString(getChannel(getAnalyserLensModePv())))) {
			yAxis.setName("location (mm)");
		} else {
			yAxis.setName("angles (deg)");
		}
		return yAxis;
	}

	private void setAcquisitionMode(final MonitorEvent event) {
		DBR_Enum enumeration = (DBR_Enum) event.getDBR();
		// Scienta epics enums for acquisition mode are reversed to AcquisitionMode: [
		// 0] Swept [ 1] Fixed
		acquisitionMode = analyserManufacturer.contains("Scienta")
				? AcquisitionMode.values()[1 - enumeration.getEnumValue()[0]]
				: AcquisitionMode.values()[enumeration.getEnumValue()[0]];
		logger.debug("acquisitionMode changed to {}", acquisitionMode);
	}

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

	public String getFrameNumberPV() {
		return frameNumberPV;
	}

	public void setFrameNumberPV(String frameNumberPV) {
		this.frameNumberPV = frameNumberPV;
	}

	public String getAcquirePV() {
		return acquirePV;
	}

	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}

	public boolean isSumFrames() {
		return sumFrames;
	}

	public void setSumFrames(boolean sumFrames) {
		this.sumFrames = sumFrames;
	}

	public String getFrameNumberPVSwept() {
		return frameNumberPVSwept;
	}

	public void setFrameNumberPVSwept(String frameNumberPVSwept) {
		this.frameNumberPVSwept = frameNumberPVSwept;
	}

	public String getEventTopic() {
		return eventTopic;
	}

	public void setEventTopic(String eventTopic) {
		this.eventTopic = eventTopic;
	}

	public AnalyserPVConfig getAnalyserPVConfig() {
		return analyserPVConfig;
	}

	public void setAnalyserPVConfig(AnalyserPVConfig analyserPVConfig) {
		this.analyserPVConfig = analyserPVConfig;
	}

	public String getAnalyserEnergyAxisPv() {
		return analyserPVConfig.getAnalyserEnergyAxisPv();
	}

	public String getAnalyserEnergyAxisCountPv() {
		return analyserPVConfig.getAnalyserEnergyAxisCountPv();
	}

	public String getAnalyserAngleAxisPv() {
		return analyserPVConfig.getAnalyserAngleAxisPv();
	}

	public String getAnalyserAngleAxisCountPv() {
		return analyserPVConfig.getAnalyserAngleAxisCountPv();
	}

	public String getAnalyserLensModePv() {
		return analyserPVConfig.getAnalyserLensModePv();
	}

	public String getAnalyserAcquisitionModePv() {
		return analyserPVConfig.getAnalyserAcquisitionModePv();
	}

	private String getAnalyserManufacturerPv() {
		return analyserPVConfig.getAnalyserManufacturerPV();
	}

}
