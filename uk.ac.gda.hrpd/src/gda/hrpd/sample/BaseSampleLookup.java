/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.hrpd.sample;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import gda.hrpd.sample.api.SampleLookup;
import gda.hrpd.sample.api.SampleMetadata;

public abstract class BaseSampleLookup implements SampleLookup {
	protected static final String MAC = "mac";
	protected static final String PSD = "psd";

	private String defaultVisit;
	protected Collection<SampleMetadata> samples;

	public BaseSampleLookup() throws IOException {
		try {
			defaultVisit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
		} catch (DeviceException de) {
			throw new IOException("Couldn't read default visit directory");
		}
	}
	public String getDefaultVisit() {
		return defaultVisit;
	}
	@Override
	public Iterator<SampleMetadata> iterator() {
		return samples.iterator();
	}
	@Override
	public Collection<SampleMetadata> getSamples() {
		return samples;
	}
}
