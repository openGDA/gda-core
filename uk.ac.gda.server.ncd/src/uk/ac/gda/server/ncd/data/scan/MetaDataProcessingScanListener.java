/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.data.scan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gda.data.metadata.MetadataEntry;

public class MetaDataProcessingScanListener extends DataProcessingScanListenerBase {
	List<MetadataEntry> metadataEntries = new ArrayList<>();

	public void setMetadataEntries(Collection<MetadataEntry> entries) {
		metadataEntries = new ArrayList<MetadataEntry>(entries);
	}

	@Override
	public void doProcessing() {
		List<String> values = new ArrayList<>(metadataEntries.size());
		values.add(filepath);
		for (MetadataEntry entry : metadataEntries) {
			values.add(entry.getMetadataValue());
		}
		try {
			logger.info("{} - Processing running with '{}'", getName(), values);
			runner.triggerProcessing(values.toArray(new String[] {}));
		} catch (IOException e) {
			logger.error("{} - Couldn't run data reduction/processing", getName(), e);
		}
	}
}