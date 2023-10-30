/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.ui.view;

public enum QueueAction {
	OPEN_RESULTS("Open results for selected run", "icons/results.png"),
	RERUN("Rerun...", "icons/rerun.png"),
	EDIT("Edit...", "icons/modify.png"),
	UP("Move backward (run later)", "icons/arrow-090.png"),
	DOWN("Move forward (run earlier)","icons/arrow-270.png"),
	PAUSE("Pause job", "icons/control-pause.png"),
	DEFER("Defer submitted scan(s) until undefered", "icons/alarm-clock-select.png"),
	STOP("Stop job", "icons/control-stop-square.png"),
	OPEN("Open...", "icons/application-dock-090.png"),
	DETAILS("Show details...", "icons/clipboard-list.png"),
	REMOVE("Remove job", "icons/remove.png"),
	REFRESH("Refresh", "icons/arrow-circle-double-135.png"),
	CONFIGURE("Configure...", "icons/document--pencil.png"),
	CLEAR_QUEUE("Clear entire queue", "icons/remove.png"),
	CLEAR_SUBMISSION_QUEUE("Clear submitted jobs only", "icons/remove.png");

	private final String label;
	private final String imageName;

	private QueueAction(String label, String imageName) {
		this.label = label;
		this.imageName = imageName;
	}

	public String getLabel() {
		return label;
	}

	public String getImageName() {
		return imageName;
	}

}
