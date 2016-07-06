/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.rcp.views.dashboard;

import gda.device.scannable.ScannableSnapshot;

public class ScannableObject {

	private final String name;
	private String output;
	private String toolTip;
	private boolean valid;
	private ScannableSnapshotProvider snapshotProvider;

	public ScannableObject(String name, ScannableSnapshotProvider snapshotProvider) {
		this.name = name;
		valid = true;
		this.snapshotProvider = snapshotProvider;
	}

	public String getName() {
		return name;
	}

	public String getOutput() {
		return output;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	public void refresh() {
		if (!valid) {
			return;
		}
		ScannableSnapshot si = null;
		try {
			si = snapshotProvider.getSnapshot(name);
		} catch (Exception e) {
		}
		if (si != null) {
			if (si.lastPosition == null) {
				output = "";
				return;
			} else if (si.extraNames.length == 0 && si.inputNames.length == 1 && si.inputNames[0].equals(name)) {
				// a single value only - do not bother printing any names
				output = String.format(si.outputFormat[0], si.lastPosition) + " " + si.units[0];
			} else {
				// TODO: Units will not be displayed correctly in a scannable group
				// where members have multiple input or any extra names
				int length = Math.max(si.outputFormat.length, si.inputNames.length + si.extraNames.length);
				String[] names = new String[length];
				String[] formats = new String[length];
				System.arraycopy(si.inputNames, 0, names, 0, si.inputNames.length);
				System.arraycopy(si.extraNames, 0, names, si.inputNames.length, si.extraNames.length);
				System.arraycopy(si.outputFormat, 0, formats, 0, si.outputFormat.length);
				StringBuilder sb = new StringBuilder();
				Object[] position = (Object[]) si.lastPosition;
				for (int i = 0; i < names.length; i++) {
					if (formats[i] == null || names[i] == null) {
						break;
					}
					if (position[i] instanceof String) {
						sb.append(String.format("%s: %s", names[i], position[i]));
					} else {
						sb.append(String.format("%s: " + formats[i], names[i], position[i]));
					}
					if (si.units[i] != null && ! si.units[i].isEmpty()) {
						sb.append(" ");
						sb.append(si.units[i]);
					}
					if (i + 1 != names.length) {
						sb.append(",  ");
					}
				}
				output = sb.toString();
			}
			output = output.trim();
		} else {
			valid = false;
			output = "unavailable";
		}
	}

}