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

package uk.ac.gda.client.properties.stage.position;

import uk.ac.gda.client.properties.stage.ScannableGroupProperties;
import uk.ac.gda.client.properties.stage.ScannableProperties;

/**
 * The keys to identify a scannable based on the client scannable configuration properties.
 * See <a href="https://confluence.diamond.ac.uk/display/DIAD/Scannables+Groups">ScannableGroups in Confluence</a>
 *
 * @author Maurizio Nagni
 *
 * @see ScannableGroupProperties
 * @see ScannableProperties
 */
public class ScannableKeys {

	private String groupId;
	private String scannableId;

	/**
	 * Returns an ID associated with a {@link ScannableGroupProperties#getId()}
	 *
	 * @return the scannable group ID
	 *
	 * @see ScannableGroupProperties
	 */
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Returns an ID associated with a {@link ScannableProperties#getId()}
	 *
	 * @return the scannable element ID
	 *
	 * @see ScannableProperties
	 */
	public String getScannableId() {
		return scannableId;
	}
	public void setScannableId(String scannableId) {
		this.scannableId = scannableId;
	}
}
