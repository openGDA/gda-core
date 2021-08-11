/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.properties.acquisition;

import java.util.List;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;

public class AcquisitionTemplateConfiguration {

	private AcquisitionTemplateType template;

	private List<ScannableTrackDocumentProperty> tracks;

	private List<ScannablePropertiesValue> startPosition;

	private boolean defaultTemplate;

	public AcquisitionTemplateType getTemplate() {
		return template;
	}

	public void setTemplate(AcquisitionTemplateType template) {
		this.template = template;
	}

	public List<ScannableTrackDocumentProperty> getTracks() {
		return tracks;
	}

	public void setTracks(List<ScannableTrackDocumentProperty> tracks) {
		this.tracks = tracks;
	}

	public boolean isDefaultTemplate() {
		return defaultTemplate;
	}

	public void setDefaultTemplate(boolean defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}

	public List<ScannablePropertiesValue> getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(List<ScannablePropertiesValue> startPosition) {
		this.startPosition = startPosition;
	}
}
