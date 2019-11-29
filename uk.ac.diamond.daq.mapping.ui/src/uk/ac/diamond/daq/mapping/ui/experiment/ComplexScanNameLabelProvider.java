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

package uk.ac.diamond.daq.mapping.ui.experiment;

import uk.ac.diamond.daq.client.gui.persistence.AbstractSearchResultLabelProvider;

public class ComplexScanNameLabelProvider  extends AbstractSearchResultLabelProvider {

	private int element;

	public ComplexScanNameLabelProvider(String columnName, String headingTitle,
			boolean primary, int element) {
		super(columnName, headingTitle, primary);

		this.element = element;
	}

	@Override
	public String convertValueToText(Object value) {
		if (value instanceof String) {
			String[] parts = ((String)value).split("\\.");
			if (element < parts.length) {
				return parts[element];
			}
		}
		return "";
	}
}
