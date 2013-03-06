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

package uk.ac.gda.video.views;


public class EpicsCameraParameters extends CameraParameters {
	private String exposurePV;
	private String acqPeriodPV;
	private String gainPV;
	private String acquirePV;
	private String urlPV;
	public String getExposurePV() {
		return exposurePV;
	}
	public void setExposurePV(String exposurePV) {
		this.exposurePV = exposurePV;
	}

	public String getAcqPeriodPV() {
		return acqPeriodPV;
	}
	public void setAcqPeriodPV(String acqPeriodPV) {
		this.acqPeriodPV = acqPeriodPV;
	}
	public String getGainPV() {
		return gainPV;
	}
	public void setGainPV(String gainPV) {
		this.gainPV = gainPV;
	}

	public String getAcquirePV() {
		return acquirePV;
	}
	public void setAcquirePV(String acquirePV) {
		this.acquirePV = acquirePV;
	}
	
	public String getUrlPV() {
		return urlPV;
	}
	public void setUrlPV(String urlPV) {
		this.urlPV = urlPV;
		//set mpegUrl to a dummy value so that the check that the property has been set is ok
		setMjpegURL("DummyValueFromEpicsCameraParameters" );
	}
	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		checkStringNotNull(exposurePV, "exposurePV");
		checkStringNotNull(acqPeriodPV, "acqPeriodPV");
		checkStringNotNull(gainPV, "gainPV");
		checkStringNotNull(acquirePV, "acquirePV");
		checkStringNotNull(urlPV, "urlPV");
	}

}
