/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.ispyb;

import java.util.ArrayList;
import java.util.List;

public class ISpyBStatusInfo {
	private ISpyBStatus status;
	private double progress;
	private List<String> fileNames = new ArrayList<String>();
	private String message = "";

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public void addFileName(String fileName) {
		fileNames.add(fileName);
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage()
	{
		return this.message;
	}

	public ISpyBStatus getStatus() {
		return status;
	}

	public void setStatus(ISpyBStatus status) {
		this.status = status;
	}
}
