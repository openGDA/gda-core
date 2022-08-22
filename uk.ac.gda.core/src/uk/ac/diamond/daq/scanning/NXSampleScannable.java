/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.AbstractNameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Where NXSampleSCannable is currently used, NXObjectScannable may now be used, since the generic form
 * works with any kind of NXobject, not just NXsample.
 *
 * For example:
 *
 *   xpdfNxSample = NXSampleScannable("xpdfNxSample","sample",self.sample.getNexus())
 *
 * can be replaced with
 *
 *   xpdfNxSample = NXObjectScannable("xpdfNxSample","sample",self.sample.getNexus())
 *
 */
@Deprecated(since = "gda-9.23", forRemoval = false)
public class NXSampleScannable extends AbstractNameable implements IScannable<Object>, INexusDevice<NXsample> {

	private static final Logger logger = LoggerFactory.getLogger(NXSampleScannable.class);

			private NXObjectProvider<NXsample> provider;

	public NXSampleScannable(String scannableName, String sampleName, NXsample sampleNode) {
		logger.warn("NXSampleScannable will be removed when a suitable alternative has been confirmed as working");
		setName(scannableName);
		provider = new NXObjectProvider<>(sampleName, sampleNode);
	}

	public void updateNode(String sampleName, NXsample sampleNode) {
		provider = new NXObjectProvider<>(sampleName, sampleNode);
	}

	@Override
	public void setLevel(int level) {
		//level does nothing here
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public NexusObjectProvider<NXsample> getNexusProvider(NexusScanInfo info) throws NexusException {
		return provider;
	}

	@Override
	public Object getPosition() throws ScanningException {
		return null;
	}

	@Override
	public Object setPosition(Object value, IPosition position) throws ScanningException {
		return null;
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
		// Deprecated, unused class. Method added to remove compiler errors but class likely deletion bound.
	}


}
