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

package gda.device.detector.addetector;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADDriverPilatus;
public class ADPilatus extends HardwareTriggerableADDetector {

	private ADDriverPilatus adDriverPilatus;

	
	public void setAdDriverPilatus(ADDriverPilatus adDriverPilatus) {
		this.adDriverPilatus=adDriverPilatus;
	}

	public ADDriverPilatus getAdDriverPilatus() {
		return adDriverPilatus;
	}
	
	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		this.collectionTime = collectionTime;

		// Configure the acquire and period times
		try {
			this.getCollectionStrategy().configureAcquireAndPeriodTimes(collectionTime);
		} catch (Exception e) {
			if (e instanceof DeviceException)
				throw (DeviceException) e;
			throw new DeviceException("Error in setCollectionTime for " + getName(), e);
			}
		}

	public void setFilePath(String filepath) throws Exception {
		getNdFile().setFilePath(filepath);
	}
	
	public void setFileName(String filename) throws Exception {
		getNdFile().setFileName(filename);
	}

	public void setFileTemplate(String fileTemplate) throws Exception {
		getNdFile().setFileTemplate(fileTemplate);
	}

	public void setFileNumber(int fileNumber) throws Exception {
		getNdFile().setFileNumber(fileNumber);
	}

	public int getFileNumber() throws Exception {
		return getNdFile().getFileNumber();
	}

	public void setAutoIncrement(boolean autoIncrement) throws Exception {
		getNdFile().setAutoIncrement((short) (autoIncrement ? 1 : 0));
	}

	public String getFilePath() throws Exception {
		return getNdFile().getFilePath_RBV();
	}

	public String getFileName() throws Exception {
		return getNdFile().getFileName_RBV();
	}

	public String getFileTemplate() throws Exception {
		return getNdFile().getFileTemplate_RBV();
	}

	public boolean getAutoIncrement() throws Exception {
		return getNdFile().getAutoIncrement_RBV() == ((short) 1);
	}

}
