/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimeProfileParameters {
	private static final Logger logger = LoggerFactory.getLogger(TimeProfileParameters.class);
	private List<FrameSetParameters> frameSetParameters;
	private String name = "Profile"; //default name
	private Integer cycles = 1;
	private Integer repeat = 1;
	private String startMethod = "Software";
	private Boolean extInhibit = false;
	private Boolean softwareStart;        // for TFG version 1 only
	private Boolean hardwareStart;        // for TFG version 1 only 
	private Boolean softStartHardTrigger; // for TFG version 1 only
	private String outputTriggerInversion= "11110000";
	private String outputTriggerDrive = "11100111";
	private String inputTriggerDebounce = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
	private String inputTriggerThreshold = "0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0";
	private static int profileNumber = 1;

	/**
	 * 
	 */
	public TimeProfileParameters() {
		frameSetParameters = new ArrayList<FrameSetParameters>();
		name += profileNumber++;
	}
	
	/**
	 * @return the frameSetParameters
	 */
	public List<FrameSetParameters> getFrameSetParameters() {
		return frameSetParameters;
	}

	/**
	 * @param frameSetParameters the frameSetParameters to set
	 */
	public void addFrameSetParameter(FrameSetParameters frameSetParameters) {
		frameSetParameters.setGroup(this.frameSetParameters.size()+1);
		this.frameSetParameters.add(frameSetParameters);
	}
	
	/**
	 * @param frameSetParameters the frameSetParameters to set
	 */
	public void setFrameSetParameters(List<FrameSetParameters> frameSetParameters) {
		this.frameSetParameters = frameSetParameters;
	}

	public void clear() {
		softwareStart=hardwareStart=softStartHardTrigger=false;
		startMethod=null;
		frameSetParameters.clear();		
	}

	public Integer getCycles() {
		return cycles;
	}

	public Integer getRepeat() {
		return repeat;
	}

	public String getStartMethod() {
		return startMethod;
	}

	public Boolean getExtInhibit() {
		return extInhibit;
	}

	public void setCycles(Integer cycles) {
		this.cycles = cycles;
	}

	public void setRepeat(Integer repeat) {
		this.repeat = repeat;
	}

	public void setStartMethod(String startMethod) {
		this.startMethod = startMethod;
	}

	public void setExtInhibit(Boolean extInhibit) {
		this.extInhibit = extInhibit;
	}

	public Boolean getSoftwareStart() {
		return softwareStart;
	}

	public Boolean getHardwareStart() {
		return hardwareStart;
	}

	public Boolean getSoftStartHardTrigger() {
		return softStartHardTrigger;
	}

	public void setSoftwareStart(Boolean softwareStart) {
		this.softwareStart = softwareStart;
	}

	public void setHardwareStart(Boolean hardwareStart) {
		this.hardwareStart = hardwareStart;
	}

	public void setSoftStartHardTrigger(Boolean softStartHardTrigger) {
		this.softStartHardTrigger = softStartHardTrigger;
	}

	public String getOutputTriggerInversion() {
		return outputTriggerInversion;
	}

	public String getOutputTriggerDrive() {
		return outputTriggerDrive;
	}

	public void setOutputTriggerInversion(String outputTriggerInversion) {
		this.outputTriggerInversion = outputTriggerInversion;
	}

	public void setOutputTriggerDrive(String outputTriggerDrive) {
		this.outputTriggerDrive = outputTriggerDrive;
	}

	public String getName() {
		return name;
	}

	public String getInputTriggerDebounce() {
		return inputTriggerDebounce;
	}

	public String getInputTriggerThreshold() {
		return inputTriggerThreshold;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setInputTriggerDebounce(String inputTriggerDebounce) {
		this.inputTriggerDebounce = inputTriggerDebounce;
	}

	public void setInputTriggerThreshold(String inputTriggerThreshold) {
		this.inputTriggerThreshold = inputTriggerThreshold;
	}
	
	public void resetProfileNumber() {
		profileNumber = 1;
	}
	
	public List<InputTriggerParameters> getInputTriggerParameters() {
		List<InputTriggerParameters> triggerList = new ArrayList<InputTriggerParameters>();
		StringTokenizer parse1 = new StringTokenizer(inputTriggerDebounce, " ");
		StringTokenizer parse2 = new StringTokenizer(inputTriggerThreshold, " ");
		for (String name : InputTriggerParameters.triggers) {
			InputTriggerParameters itp = new InputTriggerParameters();
			itp.setName(name);
			itp.setDebounce(Double.valueOf(parse1.nextToken()));
			itp.setThreshold(Double.valueOf(parse2.nextToken()));
			triggerList.add(itp);
		}
		return triggerList;
	}
	
	public int getInversionValue() {
		int inversion = 0;
		for (int i = 0; i < outputTriggerInversion.length(); i++) {
			if (outputTriggerInversion.charAt(i) == '1') {
				inversion += 1 << i;
			}
		}
		logger.debug(String.format("inversion value %d", inversion & 0xFF));
		return inversion & 0xFF;
	}

	/**
	 * @return the value for the TFG2 drive setting
	 */
	public int getDriveValue() {
		int drive = 0;
		for (int i = 0; i < outputTriggerDrive.length(); i++) {
			if (outputTriggerDrive.charAt(i) == '1') {
				drive += 1 << i;
			}
		}
		logger.debug(String.format("drive value %d", drive & 0xFF));
		return drive & 0xFF;
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((cycles == null) ? 0 : cycles.hashCode());
		result = prime * result + ((repeat == null) ? 0 : repeat.hashCode());
		result = prime * result + ((startMethod == null) ? 0 : startMethod.hashCode());
		result = prime * result + ((extInhibit == null) ? 0 : extInhibit.hashCode());
		result = prime * result + ((softwareStart == null) ? 0 : softwareStart.hashCode());
		result = prime * result + ((hardwareStart == null) ? 0 : hardwareStart.hashCode());
		result = prime * result + ((softStartHardTrigger == null) ? 0 : softStartHardTrigger.hashCode());
		result = prime * result + ((outputTriggerInversion == null) ? 0 : outputTriggerInversion.hashCode());
		result = prime * result + ((outputTriggerDrive == null) ? 0 : outputTriggerDrive.hashCode());
		result = prime * result + ((inputTriggerDebounce == null) ? 0 : inputTriggerDebounce.hashCode());
		result = prime * result + ((inputTriggerThreshold == null) ? 0 : inputTriggerThreshold.hashCode());
		result = prime
				* result
				+ ((frameSetParameters == null) ? 0 : frameSetParameters
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TimeProfileParameters other = (TimeProfileParameters) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (cycles == null) {
			if (other.cycles != null) {
				return false;
			}
		} else if (!cycles.equals(other.cycles)) {
			return false;
		}
		if (repeat == null) {
			if (other.repeat != null) {
				return false;
			}
		} else if (!repeat.equals(other.repeat)) {
			return false;
		}
		if (extInhibit == null) {
			if (other.extInhibit != null) {
				return false;
			}
		} else if (!extInhibit.equals(other.extInhibit)) {
			return false;
		}
		if (softwareStart == null) {
			if (other.softwareStart != null) {
				return false;
			}
		} else if (!softwareStart.equals(other.softwareStart)) {
			return false;
		}
		if (hardwareStart == null) {
			if (other.hardwareStart != null) {
				return false;
			}
		} else if (!hardwareStart.equals(other.hardwareStart)) {
			return false;
		}
		if (softStartHardTrigger == null) {
			if (other.softStartHardTrigger != null) {
				return false;
			}
		} else if (!softStartHardTrigger.equals(other.softStartHardTrigger)) {
			return false;
		}
		if (outputTriggerInversion == null) {
			if (other.outputTriggerInversion != null) {
				return false;
			}
		} else if (!outputTriggerInversion.equals(other.outputTriggerInversion)) {
			return false;
		}
		if (outputTriggerDrive== null) {
			if (other.outputTriggerDrive != null) {
				return false;
			}
		} else if (!outputTriggerDrive.equals(other.outputTriggerDrive)) {
			return false;
		}
		if (inputTriggerDebounce== null) {
			if (other.inputTriggerDebounce != null) {
				return false;
			}
		} else if (!inputTriggerDebounce.equals(other.inputTriggerDebounce)) {
			return false;
		}
		if (inputTriggerThreshold== null) {
			if (other.inputTriggerThreshold != null) {
				return false;
			}
		} else if (!inputTriggerThreshold.equals(other.inputTriggerThreshold)) {
			return false;
		}
		if (frameSetParameters == null) {
			if (other.frameSetParameters != null) {
				return false;
			}
		} else if (!frameSetParameters.equals(other.frameSetParameters)) {
			return false;
		}
		return true;
	}
}
