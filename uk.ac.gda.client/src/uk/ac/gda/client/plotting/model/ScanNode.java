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

package uk.ac.gda.client.plotting.model;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.Expose;

public abstract class ScanNode extends Node {

	@Expose
	private final String identifier;
	@Expose
	private final String fileName;

	@Expose
	protected final Set<String> selectedLineTraceNames = new HashSet<String>();

	public ScanNode(String identifier, String fileName, Node parent) {
		super(parent);
		this.identifier = identifier;
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	public void addSelection(String name) {
		selectedLineTraceNames.add(name);
	}

	public void removeSelection(String name) {
		selectedLineTraceNames.remove(name);
	}


	public String[] getSelectedLineTraceNames() {
		return selectedLineTraceNames.toArray(new String[]{});
	}
}
