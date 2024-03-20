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
package org.eclipse.scanning.sequencer;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.IConfigurable;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.ModelReflection;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.osgi.framework.BundleContext;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.osgi.services.ServiceProvider;

@SuppressWarnings("rawtypes")
public final class RunnableDeviceServiceImpl implements IRunnableDeviceService, IScanService {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(RunnableDeviceServiceImpl.class);

	/**
	 * The default Malcolm Hostname can be injected by spring. Otherwise
	 * this machine will be used for the malcolm host for instance 'beamline'-control.
	 */
	public static String defaultMalcolmHostname = null;

	/**
	 * Map of device name to created device. Used to avoid
	 * recreating non-virtual devices many times.
	 */
	private final Map<String, IRunnableDevice> namedDevices = new HashMap<>();

	// This field is used to provide the getActiveScanner() method on the service.
	// It should not be accessed from elsewhere.
	private static IRunnableDevice<?> currentScanningDevice;


	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered.
	static {
		logger.info("Starting device service");
	}

	/**
	 * Main constructor used in the running server by OSGi (only)
	 */
	public RunnableDeviceServiceImpl() {
		// do nothing - can be removed when 1-arg constructor below is removed, the compiler will add a default constructor.
	}

	@Deprecated(since = "GDA 9.34", forRemoval = true)
	public RunnableDeviceServiceImpl(@SuppressWarnings("unused") IScannableDeviceService scannableDeviceService) {
		this();
		logger.deprecatedMethod("RunnableDeviceServiceImpl(IScannableDeviceService)", "GDA 9.36", "RunnableDeviceServiceImpl()");
	}

	@Override
	public <T> void register(IRunnableDevice<T> device) {
		if (!device.getRole().isVirtual()) {
			namedDevices.put(device.getName(), device);
		}
	}

	@Override
	public final IPositioner createPositioner(INameable parent) throws ScanningException {
		return new ScannablePositioner(parent);
	}

	@Override
	public final IPositioner createPositioner(String name) throws ScanningException {
		final INameable nameable = new INameable() {

			@Override
			public void setName(String name) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getName() {
				return name;
			}
		};

		return new ScannablePositioner(nameable);
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name) throws ScanningException {
		return getRunnableDevice(name, null);
	}

	@Override
	public <T> IRunnableDevice<T> getRunnableDevice(String name, IPublisher<ScanBean> publisher) throws ScanningException {

		@SuppressWarnings("unchecked")
		IRunnableDevice<T> device = namedDevices.get(name);
		if (device!=null && publisher!=null && device instanceof AbstractRunnableDevice) {
			AbstractRunnableDevice<T> adevice = (AbstractRunnableDevice<T>)device;
			adevice.setPublisher(publisher); // Now all its moves will be reported by this publisher.
		}
		return device;
	}

	@Override
	@Deprecated(since = "GDA 9.33", forRemoval = true)
	public IScannableDeviceService getDeviceConnectorService() {
		logger.deprecatedMethod("getDeviceConnectorService", "GDA 9.35", "ServiceProvider.getService(IScannableDeviceService.class)");
		return ServiceProvider.getService(IScannableDeviceService.class);
	}

	@Deprecated(since = "GDA 9.33", forRemoval = true)
	public static void setDeviceConnectorService(IScannableDeviceService connectorService) {
		logger.deprecatedMethod("setDeviceConnectorService(IScannableDeviceService)", "GDA 9.35",
				"ServiceProvider.setService(IScannableDeviceService.class, scannableDeviceService)");
		throw new UnsupportedOperationException("");
	}

	private BundleContext context;

	public void start(BundleContext context) {
		this.context = context;
	}

	public void stop() {
		this.context = null;
	}

	/**
	 * Used for testing only
	 * @param device
	 * @deprecated use {@link #register(IRunnableDevice)} instead
	 */
	@Deprecated(since = "GDA 9.34", forRemoval = true)
	public void _register(String name, IRunnableDevice<?> device) {
		logger.deprecatedMethod("_register(String, IRunnableDevice)", "GDA 9.36", "register(IRunnableDevice");
		namedDevices.put(name, device);
	}

	@Override
	public Collection<String> getRunnableDeviceNames() throws ScanningException {
		return namedDevices.keySet();
	}

	@Override
	public Collection<DeviceInformation<?>> getDeviceInformation() throws ScanningException {
		return getDeviceInformation(false);
	}

	@Override
	public Collection<DeviceInformation<?>> getDeviceInformationIncludingNonAlive() throws ScanningException {
		return getDeviceInformation(true);
	}

	private Collection<DeviceInformation<?>> getDeviceInformation(boolean getNonAliveDeviceInformation) throws ScanningException {
		return getRunnableDeviceNames().stream()
				.map(name -> getDeviceInformation(name, getNonAliveDeviceInformation))
				.filter(Objects::nonNull)
				.collect(toList());
	}

	private DeviceInformation getDeviceInformation(String name, boolean includeNonAlive) {
		if (name==null) return null;
		try {
			final IRunnableDevice<Object> device = getRunnableDevice(name);
			if (device instanceof AbstractRunnableDevice) {
				return ((AbstractRunnableDevice<?>)device).getDeviceInformation(includeNonAlive);
			}
		} catch (Exception e) {
			logger.error("Error getting device info for device {}", name, e);
		}
		return null;
	}

	@Override
	public Collection<DeviceInformation<?>> getDeviceInformation(final DeviceRole role) throws ScanningException {
		Collection<DeviceInformation<?>> infos = getDeviceInformation();
		return infos.stream().filter(info -> info.getDeviceRole()==role).collect(Collectors.toList());
	}

	@Override
	public DeviceInformation<?> getDeviceInformation(String name) throws ScanningException {
		IRunnableDevice<Object> device = getRunnableDevice(name);
		if (device==null)  return null;
		if (!(device instanceof AbstractRunnableDevice)) return null;
		return ((AbstractRunnableDevice<?>)device).getDeviceInformation();
	}

	private Collection<Object> participants;

	@Override
	public void addScanParticipant(Object device) {
		if (participants==null) participants = Collections.synchronizedSet(new LinkedHashSet<>(7));
		participants.add(device);
	}

	@Override
	public void removeScanParticipant(Object device) {
		participants.remove(device);
	}

	@Override
	public Collection<Object> getScanParticipants() {
		if (participants == null) {
			return Collections.emptyList();
		}
		return participants;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IRunnableDevice<T> getActiveScanner() {
		return (IRunnableDevice<T>)RunnableDeviceServiceImpl.currentScanningDevice; // Package private method. Do not use globally!
	}

	/**
	 * Package private, think before stopping this.
	 * @param currentScanningDevice
	 */
	static void setCurrentScanningDevice(IRunnableDevice<?> currentScanningDevice) {
		RunnableDeviceServiceImpl.currentScanningDevice = currentScanningDevice;
	}

	@Override
	public IScanDevice createScanDevice(ScanModel model) throws ScanningException {
		return createScanDevice(model, null, true);
	}

	@Override
	public IScanDevice createScanDevice(ScanModel model, boolean configure) throws ScanningException {
		return createScanDevice(model, null, configure);
	}

	@Override
	public IScanDevice createScanDevice(ScanModel model, IPublisher<ScanBean> eventPublisher) throws ScanningException {
		return createScanDevice(model, eventPublisher, true);
	}

	@Override
	public IScanDevice createScanDevice(ScanModel model, IPublisher<ScanBean> eventPublisher, boolean configure) throws ScanningException {
		// Create Acquisition Device
		AcquisitionDevice scanner = new AcquisitionDevice();

		// Set attributes using this service
		scanner.setRunnableDeviceService(this);
		scanner.setConnectorService(ServiceProvider.getService(IScannableDeviceService.class)); // TODO: remove this from the scanner
		if (eventPublisher != null)
			scanner.setPublisher(eventPublisher);

		// Configure the device if requested
		if (configure)
			configureAndFireAnnotations(scanner, model);

		// Automatically register the device
		register(scanner);

		return scanner;
	}

	/**
	 * Configure a RunnableDevice using a compatible model,
	 * fire the {@link PreConfigure} and {@link PostConfigure}
	 * annotations at the appropriate times.
	 * TODO: This should be encapsulated inside the devices themselves.
	 * @param <M> The type of the model, must be compatible with the device.
	 * @param configurable A device implementing the IConfigurable interface.
	 * @param model The model providing the configuration
	 * @throws ScanningException
	 */
	public static <M> void configureAndFireAnnotations(
			IConfigurable<M> configurable,
			M model) throws ScanningException {
		AnnotationManager manager = new AnnotationManager(SequencerActivator.getInstance());
		manager.addDevices(configurable);

		// Invoke the @PreConfigure annotation inside the configurable, then
		// invoke configurable.configure(), then the @PostConfigure annotation.
		manager.invoke(PreConfigure.class, model);
		configurable.configure(model);
		manager.invoke(PostConfigure.class, model);
	}

	/**
	 * Search a model for a name with reflection. If found, transfer it to an
	 * INameable.
	 * @param nameable The nameable to possibly name
	 * @param model The model possibly providing the name
	 */
	public static void applyNameIfInModel(INameable nameable, Object model) {
		final String name = ModelReflection.getName(model);
		if (name != null)
			nameable.setName(name);
		else
			logger.warn("Could not find a name to give device in model {}", model);
	}

}
