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

package gda.rcp.views;

public class OpenViewOptionImpl implements OpenViewOption {

	private final String secondaryId;
	private final String label;
	private final String viewId;

	public OpenViewOptionImpl(String label, String viewId, String secondaryId) {
		this.label = label;
		this.viewId = viewId;
		this.secondaryId = secondaryId;
	}

	@Override
	public ViewDefinition getViewDefinition() {
		return new ViewDefinition(viewId, secondaryId);
	}

	@Override
	public String getLabel() {
		return label;
	}
}
