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

package uk.ac.gda.server.ncd.msg;

public class NcdMsgFactory {

	private NcdMetaType metaType;
	private String detectorType;

	public NcdMsgFactory(String detType, NcdMetaType type) {
		this.detectorType = detType;
		this.metaType = type;
	}

	public NcdMetadataFileMsg update(String filename, String internal) {
		return new NcdMsg.StatusUpdate(detectorType, metaType, filename, internal);
	}

	public NcdMetadataMsg refresh() {
		return new NcdMsg.Refresh(detectorType, metaType);
	}

	public NcdMetadataMsg changeRequest(String newFile, String newInternal) {
		return new NcdMsg.ChangeRequest(detectorType, metaType, newFile, newInternal);
	}
}
