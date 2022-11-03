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
package org.eclipse.scanning.example.scannable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.Location;
import org.eclipse.scanning.example.Services;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

public class MockScannableConnector implements IScannableDeviceService, IConnection {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(MockScannableConnector.class);
	protected String broker;
	private Map<String, INameable> cache;
	private IPublisher<Location> positionPublisher;
	private Set<String> globalPerScanMonitorNames;
	private Map<String, Set<String>> perScanMonitorPrerequisites = new HashMap<>();
	private boolean createIfNotThere = true;

	// Spring
	public MockScannableConnector() {
		// Called by Spring.
	}

	// Spring
	public void connect() throws URISyntaxException {
		IEventService eservice = Services.getEventService();
		this.positionPublisher = eservice.createPublisher(new URI(broker), EventConstants.POSITION_TOPIC);
		createMockObjects();
	}

	// Test decks
	public MockScannableConnector(IPublisher<Location> positionPublisher) {
		this.positionPublisher = positionPublisher;
		createMockObjects();
	}

	@Override
	public <T> void register(IScannable<T> mockScannable) {
		cache.put(mockScannable.getName(), mockScannable);
		if (mockScannable instanceof AbstractScannable) {
			((AbstractScannable<?>) mockScannable).setPublisher(positionPublisher);
			((AbstractScannable<?>) mockScannable).setScannableDeviceService(this);
		}
	}

	/**
	 * Makes a bunch of things that the tests and example user interface connect to.
	 */
	private void createMockObjects() {
		if (cache==null) cache = new HashMap<>(3);

		MockScannable energy = new MockScannable("energy", 10000d,  1, "eV");
		energy.setMinimum(0);
		energy.setMaximum(35000);
		register(energy);
		register(new MockPausingMonitor("pauser", 10d,  -1));
		register(new MockTopupScannable("topup", 1000));
		register(new MockScannable("beamcurrent", 5d,  1, "mA"));
		register(new MockStringScannable("portshutter", "Open", "Open", "Closed", "Error"));

		register(new MockScannable("period", 1000d, 1, "ms"));
		register(new MockBeamOnMonitor("beamon", 10d, 1));
		register(new MockScannable("bpos",  0.001,  -1));

		MockScannable a = new MockScannable("a", 10d, 1, "mm");
		register(a);
		register(new MockScannable("b", 10d, 1, "mm"));
		register(new MockScannable("c", 10d, 1, "mm"));

		MockScannable p = new MockScannable("p", 10d, 2, "µm");
		register(p);
		register(new MockScannable("q", 10d, 2, "µm"));
		register(new MockScannable("r", 10d, 2, "µm"));

		MockScannable x = new MockNeXusScannable("x", 0d,  3, "mm");
		x.setRealisticMove(true);
		x.setRequireSleep(false);
		x.setMoveRate(10000); // µm/s or 1 cm/s
		register(x);

		MockScannable y = new MockNeXusScannable("y", 0d,  3, "mm");
		y.setRealisticMove(true);
		y.setMoveRate(100); // µm/s, faster than real?
		register(y);

		x = new MockNeXusScannable("stage_x", 0d,  3, "mm");
		x.setRealisticMove(true);
		x.setRequireSleep(false);
		x.setMoveRate(10000); // µm/s or 1 cm/s
		register(x);

		y = new MockNeXusScannable("stage_y", 0d,  3, "mm");
		y.setRealisticMove(true);
		y.setMoveRate(100); // µm/s, faster than real?
		register(y);

		register(new MockNeXusScannable("z", 2d, 3, "mm"));
		register(new MockNeXusScannable("stage_z", 2d, 3, "mm"));
		register(new MockNeXusScannable("xNex", 0d, 3, "mm"));
		register(new MockNeXusScannable("yNex", 0d, 3, "mm"));
		register(new MockScannable("benchmark1", 0.0, -1, false));
		register(new MockScannable("myScannable", 0.0, -1, false));
		register(new MockNeXusScannable("theta", 0d, 3, "deg"));

		MockNeXusScannable temp = new MockNeXusScannable("T", 295d, 3, "K");
		temp.setRealisticMove(true);
		String srate = System.getProperty("org.eclipse.scanning.example.temperatureRate");
		if (srate==null) srate = "10.0";
		temp.setMoveRate(Double.valueOf(srate)); // K/s much faster than real but device used in tests.
		register(temp);

		temp= new MockNeXusScannable("temp", 295, 3, "K");// integer valued
		temp.setRealisticMove(false);
		temp.setRequireSleep(false);
		register(temp);

		for (int i = 0; i < 10; i++) {
			MockScannable t = new MockScannable("T"+i, 0d, 0, "K");
			t.setRequireSleep(false);
			register(t);

		}
		for (int i = 0; i < 10; i++) {
			register(new MockNeXusScannable("neXusScannable"+i, 0d, 3));
		}
		for (int i = 0; i < 10; i++) {
			MockNeXusScannable mon = new MockNeXusScannable("monitor"+i, 0d, 3);
			register(mon);
		}
		for (int i = 0; i < 10; i++) {
			MockNeXusScannable perScanMonitor = new MockNeXusScannable("perScanMonitor"+i, 0d, 3);
			perScanMonitor.setInitialPosition(i * 10.0);
			register(perScanMonitor);
		}
		MockStringNexusScannable stringPerScanMonitor = new MockStringNexusScannable("stringPerScanMonitor",
				"three", "one", "two", "three", "four", "five");
		register(stringPerScanMonitor);
	}

	public void register(INameable mockScannable) {
		cache.put(mockScannable.getName(), mockScannable);
		if (mockScannable instanceof AbstractScannable) {
			((AbstractScannable<?>)mockScannable).setPublisher(positionPublisher);
			((AbstractScannable<?>)mockScannable).setScannableDeviceService(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IScannable<T> getScannable(String name) throws ScanningException {
		if (name==null) throw new ScanningException("Invalid scannable "+name);
		if (cache==null) cache = new HashMap<>(3);
		if (cache.containsKey(name)) return (IScannable<T>)cache.get(name);
		if (createIfNotThere) {
			register(new MockScannable(name, 0d));
			return (IScannable<T>)cache.get(name);
		}
		return null;
	}


	@Override
	public List<String> getScannableNames() throws ScanningException {
		return cache.keySet().stream().filter(key -> cache.get(key) instanceof IScannable).collect(Collectors.toList());
	}

	public void setGlobalPerScanMonitorNames(String... globalMetadataScannableNames) {
		this.globalPerScanMonitorNames = new HashSet<>(Arrays.asList(globalMetadataScannableNames));
	}

	@Override
	public Set<String> getGlobalPerScanMonitorNames() {
		logger.deprecatedMethod("getGlobalPerScanMonitorNames()", null, "org.eclipse.scanning.api.device.IDefaultScanConfigurations");
		return globalPerScanMonitorNames == null ? Collections.emptySet() : globalPerScanMonitorNames;
	}

	public void setGlobalPerScanMonitorPrerequisiteNames(String metadataScannableName,
			String... prerequisiteMetadataScannableNames) {
		perScanMonitorPrerequisites.put(metadataScannableName,
				new HashSet<>(Arrays.asList(prerequisiteMetadataScannableNames)));
	}

	@Override
	@Deprecated(since="GDA 9.3", forRemoval=true)
	public Set<String> getRequiredPerScanMonitorNames(String scannableName) {
		logger.deprecatedMethod("getRequiredPerScanMonitorNames(String)");
		Set<String> prereqMetadataScannables = perScanMonitorPrerequisites.get(scannableName);
		return prereqMetadataScannables == null ? Collections.emptySet() : prereqMetadataScannables;
	}

	@Override
	public void disconnect() throws EventException {
		if (positionPublisher!=null) positionPublisher.disconnect();
		if (cache!=null && !cache.isEmpty()) {
			INameable[] devices = cache.values().toArray(new INameable[cache.size()]);
			for (INameable device : devices) {
				if (device instanceof IConnection) ((IConnection)device).disconnect();
			}
			cache.clear();
		}
	}

	@Override
	public boolean isConnected() {
		if (positionPublisher!=null) return positionPublisher.isConnected();
		return false;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public boolean isCreateIfNotThere() {
		return createIfNotThere;
	}

	public void setCreateIfNotThere(boolean createIfNotThere) {
		this.createIfNotThere = createIfNotThere;
	}

}
