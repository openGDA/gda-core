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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * Controller for the metadata of a Mapping scan
 */
@OsgiService(MetadataController.class)
public class MetadataController extends AbstractMappingController {

	private Set<Consumer<MetadataUpdateEvent>> listeners = new CopyOnWriteArraySet<>();

	@Override
	protected void oneTimeInitialisation() {
		// not needed
	}

	public void addListener(Consumer<MetadataUpdateEvent> listener) {
		listeners.add(listener);
	}

	public String getAcquisitionName() {
		String name = getMappingBean().getSampleMetadata().getSampleName();
		return name == null ? "" : name;
	}

	public void setAcquisitionName(String acquisitionName) {
		getMappingBean().getSampleMetadata().setSampleName(acquisitionName);
		MetadataUpdateEvent update = new MetadataUpdateEvent(acquisitionName);
		listeners.forEach(listener -> listener.accept(update));
	}

	public class MetadataUpdateEvent {
		private final String name;

		public MetadataUpdateEvent(String acquisitionName) {
			this.name = acquisitionName;
		}

		public String getAcquisitionName() {
			return name;
		}
	}

}
