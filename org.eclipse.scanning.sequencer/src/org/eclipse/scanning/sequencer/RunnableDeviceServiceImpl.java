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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public final class RunnableDeviceServiceImpl implements IRunnableDeviceService, IScanService {

	private static final Logger logger = LoggerFactory.getLogger(RunnableDeviceServiceImpl.class);

	/**
	 * The default Malcolm Hostname can be injected by spring. Otherwise
	 * this machine will be used for the malcolm host for instance 'beamline'-control.
	 */
	public static String defaultMalcolmHostname = null;

	/**
	 * This service can not be present for some tests which run in OSGi
	 * but mock the test laster.
	 */
	private static IScannableDeviceService deviceConnectorService;

	/**
	 * Map of device name to created device. Used to avoid
	 * recreating non-virtual devices many times.
	 *
	 * TODO Should this be populated by spring?
	 */
	private static final Map<String, IRunnableDevice> namedDevices;


	// This field is used to provide the getActiveScanner() method on the service.
	// It should not be accessed from elsewhere.
	private static IRunnableDevice<?> currentScanningDevice;


	// Use a factory pattern to register the types.
	// This pattern can always be extended by extension points
	// to allow point generators to be dynamically registered.
	static {
		logger.info("Starting device service");
		namedDevices = new HashMap<>(3);
	}

	/**
	 * Main constructor used in the running server by OSGi (only)
	 */
	public RunnableDeviceServiceImpl() {
		try {
			readExtensions();
		} catch (CoreException e) {
			logger.error("Problem reading extension points, non-fatal as spring may be used.", e);
		}
	}

	// Test, we clear the devices so that each test is clean
	public RunnableDeviceServiceImpl(IScannableDeviceService deviceConnectorService) {
		this();
		RunnableDeviceServiceImpl.deviceConnectorService = deviceConnectorService;
		namedDevices.clear();
	}


	private static void readExtensions() throws CoreException {
		if (Platform.getExtensionRegistry() != null) {
			final IConfigurationElement[] eles = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.scanning.api.device");

			for (IConfigurationElement e : eles) {

				if (e.getName().equals("device")) {

					final IRunnableDevice device = (IRunnableDevice)e.createExecutableExtension("class");
					String name = e.getAttribute("name");
					if (name == null) name = e.getAttribute("id");
					device.setName(name);

	                // If the model has a name we send it from the extension point.
					final Object     mod = e.createExecutableExtension("model");
	                try {
	                    final Method setName = mod.getClass().getMethod("setName", String.class);
	                    setName.invoke(mod, name);
	                } catch (Exception ignored) {
				// getName() is not compulsory in the model
	                }

	                if (!device.getRole().isVirtual()) { // We have to make a good instance which will be used in scanning.

						final DeviceInformation<?> info   = new DeviceInformation<>();
						info.setLabel(e.getAttribute("label"));
						info.setDescription(e.getAttribute("description"));
						info.setId(e.getAttribute("id"));
						info.setIcon(e.getContributor().getName()+"/"+e.getAttribute("icon"));

						if (device instanceof AbstractRunnableDevice) {
							AbstractRunnableDevice adevice = (AbstractRunnableDevice)device;
							adevice.setDeviceInformation(info);

							if (adevice.getModel()==null) adevice.setModel(mod); // Empty Model
						}
	                }

	                globalRegisterIfReal(device);

				} else {
					throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.scanning.sequencer", "Unrecognized device "+e.getName()));
				}
			}
		}
	}

	@Override
	public <T> void register(IRunnableDevice<T> device) {
		globalRegisterIfReal(device);
	}

	private static <T> void globalRegisterIfReal(IRunnableDevice<T> device) {
		if (!device.getRole().isVirtual()) {
			namedDevices.put(device.getName(), device);
		}
	}

	@Override
	public final IPositioner createPositioner(INameable parent) throws ScanningException {
		// Try to set a deviceService if it is null
		if (deviceConnectorService==null) deviceConnectorService = getDeviceConnector();
		return new ScannablePositioner(deviceConnectorService, parent);
	}

	@Override
	public final IPositioner createPositioner(String name) throws ScanningException {
		// Try to set a deviceService if it is null
		if (deviceConnectorService==null) deviceConnectorService = getDeviceConnector();

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

		return new ScannablePositioner(deviceConnectorService, nameable);
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
	public IScannableDeviceService getDeviceConnectorService() {
		return deviceConnectorService;
	}

	public static void setDeviceConnectorService(IScannableDeviceService connectorService) {
		RunnableDeviceServiceImpl.deviceConnectorService = connectorService;
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
	 */
	public void _register(String name, IRunnableDevice<?> device) {
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

		Collection<DeviceInformation<?>> ret = new ArrayList<>();
		final Collection<String> names = getRunnableDeviceNames();
		for (String name : names) {
			try {
				if (name==null) continue;

				IRunnableDevice<Object> device = getRunnableDevice(name);
				if (device instanceof AbstractRunnableDevice) {
					DeviceInformation<?> info = ((AbstractRunnableDevice<?>)device).getDeviceInformation(getNonAliveDeviceInformation);
					ret.add(info);
				}
			} catch (Exception ex) {
				logger.warn("Error getting device info for : " + name);
			}
		}
		return ret;
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
	public IScanDevice createScanDevice(
			ScanModel model,
			IPublisher<ScanBean> eventPublisher,
			boolean configure) throws ScanningException {
		ensureDeviceConnectorService();

		// Create Acquisition Device
		AcquisitionDevice scanner = new AcquisitionDevice();

		// Set attributes using this service
		scanner.setRunnableDeviceService(this);
		scanner.setConnectorService(deviceConnectorService);
		if (eventPublisher != null)
			scanner.setPublisher(eventPublisher);

		// Configure the device if requested
		if (configure)
			configureAndFireAnnotations(scanner, model);

		// Automatically register the device
		globalRegisterIfReal(scanner);

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

	/**
	 * Make sure the deviceConnectorService is not null or throw
	 * a {@link ScanningException}
	 * @throws ScanningException
	 */
	private void ensureDeviceConnectorService() throws ScanningException {
		if (deviceConnectorService == null)
			deviceConnectorService = getDeviceConnector();
	}

    /**
     * Try to get the connector service from the {@link BundleContext}
     * @return The {@link IScannableDeviceService}
     * @throws ScanningException
     */
	private IScannableDeviceService getDeviceConnector() throws ScanningException {
		if (context != null) {
			ServiceReference<IScannableDeviceService> ref = context
					.getServiceReference(IScannableDeviceService.class);
			return context.getService(ref);
		} else {
			throw new ScanningException(
					"RunnableDeviceServiceImpl has no bundle "
					+ "context to get a device connector service");
		}
	}
}
