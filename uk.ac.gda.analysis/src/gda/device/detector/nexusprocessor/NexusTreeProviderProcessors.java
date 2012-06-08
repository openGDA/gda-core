/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.observable.ObservableComponent;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * Implementation of NexusTreeProviderProcessor that delegates to a list of NexusTreeProviderProcessor
 */
public class NexusTreeProviderProcessors implements NexusTreeProviderProcessor {

	private List<NexusTreeProviderProcessor> processors;

	public List<NexusTreeProviderProcessor> getProcessors() {
		return processors;
	}

	// only to be called before a scan as it effect extraNames and outputFormat
	public void setProcessors(List<NexusTreeProviderProcessor> newProcessors) {
		this.processors = newProcessors;
	}

	@Override
	public Collection<String> getExtraNames() {
		if (processors == null) {
			return null;
		}
		if (!isEnabled())
			return null;
		Collection<String> allExtraNames = new Vector<String>();
		for (NexusTreeProviderProcessor processor : processors) {
			Collection<String> extraNames = processor.isEnabled() ? processor.getExtraNames() : null;
			if (extraNames != null) {
				allExtraNames.addAll(extraNames);
			}
		}
		return allExtraNames.isEmpty() ? null : allExtraNames;
	}

	@Override
	public Collection<String> getOutputFormat() {
		if (processors == null) {
			return null;
		}
		if (!isEnabled())
			return null;
		Collection<String> totalList = new Vector<String>();
		for (NexusTreeProviderProcessor processor : processors) {
			Collection<String> itemList = processor.isEnabled() ? processor.getOutputFormat() : null;
			if (itemList != null) {
				totalList.addAll(itemList);
			}
		}
		return totalList.isEmpty() ? null : totalList;
	}

	ObservableComponent obsComponent = new ObservableComponent();

	@Override
	public GDANexusDetectorData process(final GDANexusDetectorData nexusTreeProvider) throws Exception {
		if (!isEnabled())
			return null;
		GDANexusDetectorData result = null;
		for (NexusTreeProviderProcessor processor : processors) {
			GDANexusDetectorData res = processor.isEnabled() ? processor.process(nexusTreeProvider) : null;
			if (res != null) {
				if (result == null)
					result = new NXDetectorData();
				result = result.mergeIn(res);
			}
		}
		return result;
	}

	@Override
	public boolean isEnabled() {
		for (NexusTreeProviderProcessor processor : processors) {
			if (processor.isEnabled())
				return true;
		}
		return false;
	}

}
