/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.scanning;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NXObjectScannable<T extends NXobject> implements IScannable<Object>, INexusDevice<T> {

	private static final Logger logger = LoggerFactory.getLogger(NXObjectScannable.class);

	private String scannableName;
	private NXObjectProvider<T> provider;

	public NXObjectScannable(String scannableName, String objectName, T objectNode) {
		this.scannableName = scannableName;
		provider = new NXObjectProvider<T>(objectName, objectNode);
		logger.trace("NXObjectScannable({}, {}, {}), provider={}", scannableName, objectName, objectNode, provider);
	}

	public void updateNode(String objectName, T objectNode) {
		provider = new NXObjectProvider<T>(objectName, objectNode);
		logger.trace("updateNode({}, {}), provider={} for {}", objectName, objectNode, provider, this.scannableName);
	}

	@Override
	public void setLevel(int level) {
		logger.trace("setLevel({}) ignoring for {}", level, this.scannableName);
		//level does nothing here
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public String getName() {
		return scannableName;
	}

	@Override
	public void setName(String name) {
		this.scannableName = name;
	}

	@Override
	public NexusObjectProvider<T> getNexusProvider(NexusScanInfo info) throws NexusException {
		logger.trace("getNexusProvider({}) returning {} for {}", info, provider, this.scannableName);
		return provider;
	}

	@Override
	public Object getPosition() throws ScanningException {
		return null;
	}

	@Override
	public Object setPosition(Object value, IPosition position) throws ScanningException {
		logger.trace("setPosition({}, {}) ignoring for {}", value, position, this.scannableName);
		return null;
	}
}
