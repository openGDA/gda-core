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

import java.net.URL;
import java.util.List;

/**
 * Represents a processing requests client configuration
 *
 * @author Maurizio Nagni
 */
public class ProcessingRequestProperties {

	/**
	 * A list of nexus templates files paths
	 */
	private List<URL> nexusTemplates;

	/**
	 * A list of cameras Id available for the frame capture processing request
	 */
	private List<String> frameCapture;

	/**
	 * Returns the nexus templates available for the template prcessing request
	 * @return a list of templates URLs, otherwise null.
	 */
	public List<URL> getNexusTemplates() {
		return nexusTemplates;
	}

	public void setNexusTemplates(List<URL> nexusTemplates) {
		this.nexusTemplates = nexusTemplates;
	}

	/**
	 * Returns the cameras Ids available for the frame capture processing request
	 * @return a list of cameras ids, otherwise null.
	 */
	public List<String> getFrameCapture() {
		return frameCapture;
	}

	public void setFrameCapture(List<String> frameCapture) {
		this.frameCapture = frameCapture;
	}

}
