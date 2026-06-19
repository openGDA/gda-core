package uk.ac.diamond.daq.arpes.ui.e4.dispatcher;

import java.util.ArrayList;
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

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;
import uk.ac.gda.apres.ui.config.AnalyserPVConfig;

public abstract class AbstractBaseArpesLiveDataDispatcher extends FindableConfigurableBase implements IObservable{

	private static final Logger logger = LoggerFactory.getLogger(AbstractBaseArpesLiveDataDispatcher.class);

	protected final ObservableComponent observableComponent = new ObservableComponent();

	protected final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>(1));

	protected final EpicsController epicsController = EpicsController.getInstance();

	protected LiveDataPlotUpdate dataUpdate = new LiveDataPlotUpdate();

	protected AcquisitionMode acquisitionMode = AcquisitionMode.FIXED;

	protected List<String> supportedAcquisitionModes = new ArrayList<>();
	protected List<String>  tags;

	// set in spring xml configuration
	protected String arrayPV;
	protected String frameNumberPV;
	protected String numExposuresPV;

	protected AnalyserPVConfig analyserPVConfig;

	/** Map that stores the channel against the PV name */
	protected final Map<String, Channel> channelMap = new HashMap<>();

	/** Implement this method in child classes */
	protected abstract void emitNewData(IDataset data) throws TimeoutException, CAException, InterruptedException;

	/** Implement this method in child classes */
	protected abstract void monitorNumExposures(final MonitorEvent event);

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			initialiseChannels();
			// when this channel fires we need to get image
			epicsController.setMonitor(getChannel(frameNumberPV), evt -> {
				try {
					executor.submit(this::prepareNewData); // use executor otherwise channel not connected errors
				} catch (RejectedExecutionException ree) {
					logger.trace("Exception for rejected execution", ree);
				}
			});
			// set monitor on acquisition mode - need manufacturer as Scienta AcqMode enums
			// are opposite to MBS
			epicsController.setMonitor(getChannel(getAnalyserAcquisitionModePv()), this::setAcquisitionMode);

			//NumExposuresCounter_RBV ( When it is 0 - that means start of new frame)
			epicsController.setMonitor(getChannel(numExposuresPV), this::monitorNumExposures);
		} catch (Exception e) {
			logger.error("Error setting up analyser live visualisation", e);
		}
		setConfigured(true);
	}

	protected void prepareNewData() {
		try {
			if (supportedAcquisitionModes.stream().noneMatch(acquisitionMode.getLabel()::contains)) {
				return;
			}
			IDataset xAxis = getXAxis();
			IDataset yAxis = getYAxis();
			// E4 view get it somehow transposed
			IDataset data = getArrayAsDataset(yAxis.getShape()[0], xAxis.getShape()[0]).getTransposedView(1,0);
			dataUpdate.resetLiveDataUpdate();
			dataUpdate.setxAxis(yAxis);
			dataUpdate.setyAxis(xAxis);
			dataUpdate.setAcquisitionMode(acquisitionMode);
			emitNewData(data);
		} catch (Exception e) {
			logger.error("Failed to prepare/send LiveDataPlotUpdate ", e);
		}
	}

	private void initialiseChannels() throws FactoryException {
		// create channels from analyserPVprovider
		getChannel(getAnalyserEnergyAxisPv());
		getChannel(getAnalyserEnergyAxisCountPv());
		getChannel(getAnalyserAngleAxisPv());
		getChannel(getAnalyserAngleAxisCountPv());
		getChannel(getAnalyserLensModePv());
		getChannel(getAnalyserManufacturerPv());
		getAnalyserAcquisitionModePv();
		// create channel for images
		getChannel(arrayPV);
		getChannel(numExposuresPV);
		getChannel(frameNumberPV);
		if ((arrayPV==null) || (frameNumberPV==null) || (numExposuresPV==null)) {
			throw new FactoryException("arrayPV, frameNumberPV and numExposuresPV must be all set in spring xml!");
			}
	}

	protected Channel getChannel(String pvName) {
		return channelMap.computeIfAbsent(pvName, t -> {
			try {
				return epicsController.createChannel(pvName);
			} catch (CAException | TimeoutException e) {
				logger.error("Failed to create channel: " + pvName, e);
				return null;
			}
		});
	}

	protected IDataset getArrayAsDataset(int x, int y) throws Exception {
		int[] dims = new int[] { x, y };
		int arraySize = dims[0] * dims[1];
		if (arraySize < 1) {
			throw new IllegalArgumentException(String.format("arraySize was less than 1. x=%d y=%d", x, y));
		}
		// Get as float[] not double[] for performance
		float[] array = epicsController.cagetFloatArray(getChannel(getArrayPV()), arraySize);
		logger.debug("Got image data array of size {}", array.length);
		Dataset newData = DatasetFactory.createFromObject(array, dims);
		// Flip the data across the 0 position of the Y axis I05-221
		return DatasetUtils.flipUpDown(newData);
	}

	protected IDataset getXAxis() throws Exception {
		double[] xdata = epicsController.cagetDoubleArray(getChannel(getAnalyserEnergyAxisPv()),
				epicsController.cagetInt(getChannel(getAnalyserEnergyAxisCountPv())));
		IDataset xAxis = DatasetFactory.createFromObject(xdata);
		xAxis.setName("energies (eV)");
		return xAxis;
	}

	protected IDataset getYAxis() throws Exception {
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

	protected void setAcquisitionMode(final MonitorEvent event) {
		DBR_Enum enumeration = (DBR_Enum) event.getDBR();
		acquisitionMode = AcquisitionMode.values()[enumeration.getEnumValue()[0]];
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

	public AnalyserPVConfig getAnalyserPVConfig() {
		return analyserPVConfig;
	}

	public String getNumExposuresPV() {
		return numExposuresPV;
	}

	public void setNumExposuresPV(String numExposuresPV) {
		this.numExposuresPV = numExposuresPV;
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

	public List<String> getSupportedAcquisitionModes() {
		return supportedAcquisitionModes;
	}

	public void setSupportedAcquisitionModes(List<String> supportedAcquisitionModes) {
		this.supportedAcquisitionModes = supportedAcquisitionModes;
	}

	public List<String>  getTags() {
		return tags;
	}

	public void setTags(List<String>  tags) {
		this.tags = tags;
	}

	protected void notifyListeners(LiveDataPlotUpdate evt) {
		logger.debug("Notifying listeners on new data");
		observableComponent.notifyIObservers(this, evt);
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}

