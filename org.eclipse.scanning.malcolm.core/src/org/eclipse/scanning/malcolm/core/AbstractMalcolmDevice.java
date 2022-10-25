/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.malcolm.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.malcolm.event.MalcolmEvent;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Base class for Malcolm devices
 *
 */
public abstract class AbstractMalcolmDevice extends AbstractRunnableDevice<IMalcolmModel> implements IMalcolmDevice, INexusDevice<NXobject> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractMalcolmDevice.class);

	// Events
	private MalcolmEventDelegate eventDelegate;

	protected ScanModel scanModel;
	protected IPointGenerator<? extends IScanPointGeneratorModel> pointGenerator;
	protected String outputDir;
	protected boolean isMultiScan = false;
	private int[] breakpoints = null;

	protected AbstractMalcolmDevice(IRunnableDeviceService runnableDeviceService) {
		super(runnableDeviceService);
		this.eventDelegate = new MalcolmEventDelegate(this);
		setRole(DeviceRole.MALCOLM);
		setSupportedScanMode(ScanMode.HARDWARE);
	}

	@Override
	public void register() {
		try {
			super.register();
			initialize();
		} catch (MalcolmDeviceException e) {
			logger.error("Could not initialize malcolm device " + getName(), e);
		}
	}

	@Override
	public void initialize() throws MalcolmDeviceException {
		// does nothing by default, subclasses may override
	}

	@PreConfigure
	public void configureScan(ScanModel scanModel) throws MalcolmDeviceException {
		if (scanModel == null) return;

		logger.debug("Configuring malcolm device {} for scan", getName());
		this.scanModel = scanModel;
		setPointGenerator(scanModel.getPointGenerator());

		final Optional<InterpolatedMultiScanModel> multiModel = getMultiScanModel();
		isMultiScan = getMultiScanModel().isPresent();
		breakpoints = calculateBreakpoints(multiModel);

		String outputDir = null;
		if (scanModel.getFilePath() != null) {
			outputDir = FilenameUtils.removeExtension(scanModel.getFilePath());
			File outputDirFile = new File(outputDir);
			outputDirFile.mkdirs();
		}
		setOutputDir(outputDir);
		logger.debug("Finished configuring malcolm device {} for scan, output dir set to {}", getName(), getOutputDir());
	}

	private int[] calculateBreakpoints(Optional<InterpolatedMultiScanModel> multiScanModel) {
		return multiScanModel.map(x -> x.getModels().stream().mapToInt(AbstractMalcolmDevice::getModelSize).toArray())
				.orElse(null);
	}

	private static int getModelSize(IScanPointGeneratorModel model) {
		try {
			return Services.getPointGeneratorService().createGenerator(model).size();
		} catch (GeneratorException e) {
			throw new IllegalStateException("Cannot create point generator for model: " + model);
		}
	}

	/**
	 * The breakpoints array to pass to malcolm, only used with {@link InterpolatedMultiScanModel},
	 * otherwise <code>null</code>. The array has the same size as the number of models in the
	 * {@link InterpolatedMultiScanModel}, where each value is the number of points in the scan
	 * for that model
	 * @return breakpoints array
	 */
	protected int[] getBreakpoints() {
		return breakpoints;
	}

	public ScanModel getConfiguredScan() {
		return scanModel;
	}

	@Override
	public void configure(IMalcolmModel model) throws ScanningException {
		// check that all the axes in axesToMove are in the set of available axes
		final List<String> availableAxes = getAvailableAxes();
		if (model.getAxesToMove() != null && !availableAxes.containsAll(model.getAxesToMove())) {
			throw new MalcolmDeviceException("Unknown axis: " + model.getAxesToMove().stream()
					.filter(axisName -> !availableAxes.contains(axisName)).findFirst().orElseThrow());
		}

		super.configure(model);
	}

	@Override
	public List<String> getConfiguredAxes() throws ScanningException {
		return getConfiguredAxes(getModel());
	}

	public List<String> getConfiguredAxes(IMalcolmModel model) throws ScanningException {
		final List<String> configuredAxes = (model != null && model.getAxesToMove() != null) ? model.getAxesToMove() : getAvailableAxes();

		return pointGenerator == null ? configuredAxes : calculateAxesToMove(configuredAxes);
	}

	/**
	 * Calculate the value of the axesToMove property of the EpicsMalcolmModel to send to malcolm,
	 * based on the given configured axes and the currently configured point generator
	 * @param configuredAxes the axes malcolm is or would have been configured with
	 * @return the axes that malcolm should move in the scan
	 */
	protected List<String> calculateAxesToMove(List<String> configuredAxes) {
		final List<String> scannableNames = pointGenerator.getNames();
		int i = scannableNames.size() - 1;
		while (i >= 0 && configuredAxes.contains(scannableNames.get(i))) {
			i--;
		}
		// i is now the index of the first non-malcolm axis, or -1 if all axes are malcolm controlled
		return new ArrayList<>(scannableNames.subList(i + 1, scannableNames.size()));
	}

	@Override
	public void setPointGenerator(IPointGenerator<? extends IScanPointGeneratorModel> pointGenerator) {
		this.pointGenerator = pointGenerator;
	}

	protected Optional<InterpolatedMultiScanModel> getMultiScanModel() {
		if (scanModel == null) return Optional.empty(); // this should only be the case in unit tests
		final IScanPointGeneratorModel scanPathModel = scanModel.getScanPathModel();
		if (scanPathModel instanceof InterpolatedMultiScanModel) {
			return Optional.of((InterpolatedMultiScanModel) scanPathModel);
		} else if (scanPathModel instanceof CompoundModel) {
			final List<IScanPointGeneratorModel> models = ((CompoundModel) scanPathModel).getModels();
			final IScanPointGeneratorModel lastModel = models.get(models.size() - 1);
			if (lastModel instanceof InterpolatedMultiScanModel) {
				return Optional.of((InterpolatedMultiScanModel) lastModel);
			}
		}
		return Optional.empty();
	}

	@Override
	public IPointGenerator<? extends IScanPointGeneratorModel> getPointGenerator() {
		return pointGenerator;
	}

	@Override
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public String getOutputDir() {
		return outputDir;
	}

	@SuppressWarnings("unused")
	@ScanFinally
	public void scanFinally() throws ScanningException {
		// clear the configured scan model, point generator and output dir when the scan has finished. These are set per scan
		this.scanModel = null;
		this.pointGenerator = null;
		this.outputDir = null;
		this.isMultiScan = false;
		this.breakpoints = null;
	}

	@Override
	public DeviceInformation<IMalcolmModel> getDeviceInformation(boolean includeNonAlive) throws ScanningException {
		final DeviceInformation<IMalcolmModel> info = super.getDeviceInformation(includeNonAlive);
		if (includeNonAlive || info.isAlive()) {
			info.setAvailableAxes(getAvailableAxes());
			info.setConfiguredAxes(getConfiguredAxes());
			info.setMalcolmVersion(getVersion());
			info.setMalcolmDetectorInfos(getDetectorInfos());
		}

		return info;
	}

	/**
	 * Enacts any pre-actions or conditions before the device attempts to run the task block.
	 *
	 * @throws Exception
	 */
	protected void beforeExecute() throws Exception {
		logger.debug("Entering beforeExecute, state is: {}", getDeviceState());
	}

	/**
	 * Enacts any post-actions or conditions after the device completes a run of the task block.
	 *
	 * @throws Exception
	 */
	protected void afterExecute() throws Exception {
		logger.debug("Entering afterExecute, state is: {}", getDeviceState());
	}

	protected void setTemplateBean(MalcolmEvent bean) {
		eventDelegate.setTemplateBean(bean);
	}

	@Override
	public void start(final IPosition pos) throws ScanningException, InterruptedException {

		final List<Throwable> exceptions = new ArrayList<>(1);

		final Runnable runPosition = () -> {
			try {
				AbstractMalcolmDevice.this.run(pos);
			} catch (Exception e) {
				e.printStackTrace();
				exceptions.add(e);
			}
		};

		final Thread thread = new Thread(runPosition, "Device Runner Thread "+getName());
		thread.start();

		// We delay by 500ms just so that we can
		// immediately throw any connection exceptions
		Thread.sleep(500);

		if (!exceptions.isEmpty()) throw new ScanningException(exceptions.get(0));
	}

	protected void close() {
		eventDelegate.close();
	}

	@Override
	public void dispose() throws MalcolmDeviceException {
		try {
			try {
				if (getDeviceState().isRunning()) abort();
			} finally {
				close();
			}
		} catch (Exception e) {
			throw new MalcolmDeviceException(this, "Cannot dispose of '"+getName()+"'!", e);
		}
	}

	@Override
	public List<NexusObjectProvider<?>> getNexusProviders(NexusScanInfo info) throws NexusException {
		try {
			MalcolmNexusObjectBuilder malcolmNexusBuilder = new MalcolmNexusObjectBuilder(this);
			return malcolmNexusBuilder.buildNexusObjects(info);
		} catch (Exception e) {
			throw new NexusException("Could not create nexus objects for malcolm device " + getName(), e);
		}
	}

	@Override
	public void addMalcolmListener(IMalcolmEventListener listener) {
		eventDelegate.addMalcolmListener(listener);
	}

	@Override
	public void removeMalcolmListener(IMalcolmEventListener listener) {
		eventDelegate.removeMalcolmListener(listener);
	}

	protected void sendEvent(MalcolmEvent event) throws Exception {
		eventDelegate.sendEvent(event);
	}

}
