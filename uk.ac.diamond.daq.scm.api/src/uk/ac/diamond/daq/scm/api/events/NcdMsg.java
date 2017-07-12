/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scm.api.events;

import uk.ac.diamond.daq.msgbus.MsgBus.Msg;

public abstract class NcdMsg extends Msg implements NcdMetadataFileMsg {

	private final String detectorType;
	private final NcdMetaType metaType;

	private final String filepath;
	private final String internalPath;

	public NcdMsg(String detectorType, NcdMetaType metaType, String filepath, String internalPath) {
		this.detectorType = detectorType;
		this.metaType = metaType;
		this.filepath = filepath;
		this.internalPath = internalPath;
	}

	@Override
	public String getDetectorType() {
		return detectorType;
	}

	@Override
	public String getFilepath() {
		return filepath;
	}

	@Override
	public String getInternalPath() {
		return internalPath;
	}

	@Override
	public NcdMetaType getMetaType() {
		return metaType;
	}

	@Override
	public String toString() {
		return String.format("%s-%s %s: %s, %s", detectorType, metaType, this.getClass().getSimpleName(), filepath, internalPath);
	}

	public static class StatusUpdate extends NcdMsg {
		public StatusUpdate(String detectorType, NcdMetaType metaType, String filepath, String internalPath) {
			super(detectorType, metaType, filepath, internalPath);
		}
	}
	public static class ChangeRequest extends NcdMsg {
		public ChangeRequest(String detectorType, NcdMetaType metaType, String filepath, String internalPath) {
			super(detectorType, metaType, filepath, internalPath);
		}
	}
	public static class Refresh extends NcdMsg {
		public Refresh(String detectorType, NcdMetaType metaType) {
			super(detectorType, metaType, null, null);
		}
	}
}