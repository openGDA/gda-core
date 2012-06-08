/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.simplescan;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

public class SimpleScan implements Serializable{

	static public final URL mappingURL = SimpleScan.class.getResource("simpleScanMapping.xml");
	static public final URL schemaURL = SimpleScan.class.getResource("simpleScanSchema.xsd");
	
	private String scannableName;
	private Double fromPos;
	private Double toPos;
	private Double stepSize;
	private Double acqTime;
	private List<ScannableManagerBean> scannables;
	private List<DetectorManagerBean> detectors;
	
	public SimpleScan(){
		scannables = new ArrayList<ScannableManagerBean>(50);
		detectors = new ArrayList<DetectorManagerBean>(50);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fromPos == null) ? 0 : fromPos.hashCode());
		result = prime * result + ((toPos == null) ? 0 : toPos.hashCode());
		result = prime * result + ((stepSize == null) ? 0 : stepSize.hashCode());
		result = prime * result + ((acqTime == null) ? 0 : acqTime.hashCode());
		result = prime * result + ((scannables == null) ? 0 : scannables.hashCode());
		result = prime * result + ((detectors == null) ? 0 : detectors.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) 
			return true;
		if (obj == null) 
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleScan other = (SimpleScan) obj;
		if (fromPos == null)
			if (other.fromPos != null)
				return false;
		else if (!fromPos.equals(other.fromPos))
			return false;
		if (toPos == null)
			if (other.toPos != null)
				return false;
		else if (!toPos.equals(other.toPos))
			return false;
		if (stepSize == null)
			if (other.stepSize != null)
				return false;
		else if (!stepSize.equals(other.stepSize))
			return false;
		if (acqTime == null)
			if (other.acqTime != null)
				return false;
		else if (!acqTime.equals(other.acqTime))
			return false;
		if (scannables == null) {
			if (other.scannables != null) {
				return false;
			}
		} else if (!scannables.equals(other.scannables)) {
			return false;
		}
		if (detectors == null) {
			if (other.detectors != null) {
				return false;
			}
		} else if (!detectors.equals(other.detectors)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	public void clear(){
		fromPos=null;
		toPos=null;
		stepSize=null;
		acqTime=null;
		scannables.clear();
		detectors.clear();
		scannableName=null;
	}
	
	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public Double getFromPos() {
		return fromPos;
	}

	public void setFromPos(Double fromPos) {
		this.fromPos = fromPos;
	}

	public Double getToPos() {
		return toPos;
	}

	public void setToPos(Double toPos) {
		this.toPos = toPos;
	}

	public Double getStepSize() {
		return stepSize;
	}

	public void setStepSize(Double stepSize) {
		this.stepSize = stepSize;
	}

	public Double getAcqTime() {
		return acqTime;
	}

	public void setAcqTime(Double acqTime) {
		this.acqTime = acqTime;
	}
	public List<ScannableManagerBean> getScannables() {
		return scannables;
	}

	public void setScannables(List<ScannableManagerBean> scannables) {
		this.scannables = scannables;
	}
	
	public void addScannable(ScannableManagerBean scannable) {
		scannables.add(scannable);
	}
	
	public void removeScannable(int[] index) {
		for(int i=0;i<index.length;i++){
			if(index.length>1){
			}
			else
				scannables.remove(index[i]);
		}
	}
	public List<DetectorManagerBean> getDetectors() {
		return detectors;
	}

	public void setDetectors(List<DetectorManagerBean> detectors) {
		this.detectors = detectors;
	}
	
	public void addDetector(DetectorManagerBean detector) {
		detectors.add(detector);
	}
	
	public void removeDetector(int[] index) {
		for(int i=0;i<index.length;i++){
			if(index.length>1){
			}
			else
				detectors.remove(index[i]);
		}
	}
}
