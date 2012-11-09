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

import org.apache.commons.beanutils.BeanUtils;

public class FrameSetParameters {

	public static final String[] displayUnits = { "ns", "usec", "msec", "sec", "min", "hour" };
	private Integer group;
	private Integer nframes = 1;
	private Integer nwait = 1;
	private String waitUnit = displayUnits[2];
	private Integer nrun = 10;
	private String runUnit = displayUnits[3];
//	private String pause = "00";           // for TFG version 1 only
	private String waitPause = "No Pause"; // for TFG version 2 only
	private String runPause = "No Pause";  // for TFG version 2 only
	private String waitPulse = "00000000";
	private String runPulse = "00000000";
	
	public FrameSetParameters() {
	}

	public FrameSetParameters(FrameSetParameters fsp) {
		nframes = fsp.getNframes();
		nwait = fsp.getNwait();
		waitUnit = fsp.getWaitUnit();
		nrun = fsp.getNrun();
		runUnit = fsp.getRunUnit();
//		pause = fsp.getPause();           // for TFG version 1 only
		waitPause = fsp.getWaitPause(); // for TFG version 2 only
		runPause = fsp.getRunPause();  // for TFG version 2 only
		waitPulse = fsp.getWaitPulse();
		runPulse = fsp.getRunPulse();
	}

	public Integer getGroup() {
		return group;
	}
	public Integer getNframes() {
		return nframes;
	}
	public Integer getNwait() {
		return nwait;
	}
	public String getWaitUnit() {
		return waitUnit;
	}
	public Integer getNrun() {
		return nrun;
	}
	public String getRunUnit() {
		return runUnit;
	}
	public String getWaitPause() {
		return waitPause;
	}
	public String getRunPause() {
		return runPause;
	}
//	public String getPause() {
//		return pause;
//	}
	public String getWaitPulse() {
		return waitPulse;
	}
	public String getRunPulse() {
		return runPulse;
	}
	public void setGroup(Integer group) {
		this.group = group;
	}
	public void setNframes(Integer nframes) {
		this.nframes = nframes;
	}
	public void setNwait(Integer nwait) {
		this.nwait = nwait;
	}
	public void setWaitUnit(String waitUnit) {
		this.waitUnit = waitUnit;
	}
	public void setNrun(Integer nrun) {
		this.nrun = nrun;
	}
	public void setRunUnit(String runUnit) {
		this.runUnit = runUnit;
	}
	public void setWaitPause(String waitPause) {
		this.waitPause = waitPause;
	}
	public void setRunPause(String runPause) {
		this.runPause = runPause;
	}
//	public void setPause(String pause) {
//		this.pause = pause;
//	}
	public void setWaitPulse(String waitPulse) {
		this.waitPulse = waitPulse;
	}
	public void setRunPulse(String runPulse) {
		this.runPulse = runPulse;
	}

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
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((nframes == null) ? 0 : nframes.hashCode());
		result = prime * result + ((nwait == null) ? 0 : nwait.hashCode());
		result = prime * result + ((waitUnit == null) ? 0 : waitUnit.hashCode());
		result = prime * result + ((nrun == null) ? 0 : nrun.hashCode());
		result = prime * result + ((runUnit == null) ? 0 : runUnit.hashCode());
		result = prime * result + ((waitPause == null) ? 0 : waitPause.hashCode());
		result = prime * result + ((runPause == null) ? 0 : runPause.hashCode());
//		result = prime * result + ((pause == null) ? 0 : pause.hashCode());
		result = prime * result + ((waitPulse == null) ? 0 : waitPulse.hashCode());
		result = prime * result + ((runPulse == null) ? 0 : runPulse.hashCode());
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
		FrameSetParameters other = (FrameSetParameters) obj;
		if (nframes == null) {
			if (other.nframes != null) {
				return false;
			}
		} else if (!nframes.equals(other.nframes)) {
			return false;
		}
		if (nwait == null) {
			if (other.nwait != null) {
				return false;
			}
		} else if (!nwait.equals(other.nwait)) {
			return false;
		}
		if (waitUnit == null) {
			if (other.waitUnit != null) {
				return false;
			}
		} else if (!waitUnit.equals(other.waitUnit)) {
			return false;
		}		
		if (nrun == null) {
			if (other.nrun != null) {
				return false;
			}
		} else if (!nrun.equals(other.nrun)) {
			return false;
		}
		if (runUnit == null) {
			if (other.runUnit != null) {
				return false;
			}
		} else if (!runUnit.equals(other.runUnit)) {
			return false;
		}		
		if (waitPause == null) {
			if (other.waitPause != null) {
				return false;
			}
		} else if (!waitPause.equals(other.waitPause)) {
			return false;
		}		
		if (runPause == null) {
			if (other.runPause != null) {
				return false;
			}
		} else if (!runPause.equals(other.runPause)) {
			return false;
		}		
//		if (pause == null) {
//			if (other.pause != null) {
//				return false;
//			}
//		} else if (!pause.equals(other.pause)) {
//			return false;
//		}		
		if (waitPulse == null) {
			if (other.waitPulse != null) {
				return false;
			}
		} else if (!waitPulse.equals(other.waitPulse)) {
			return false;
		}		
		if (runPulse == null) {
			if (other.runPulse != null) {
				return false;
			}
		} else if (!runPulse.equals(other.runPulse)) {
			return false;
		}		
		return true;
	}
	/**
	 * @return the numeric value of the run time.
	 */
	public double getActualRunTime() {
		double factor = 1.0;
		if (runUnit.equals(displayUnits[0]))
			factor = 0.000001;
		else if (runUnit.equals(displayUnits[1]))
			factor = 0.001;
		else if (runUnit.equals(displayUnits[2]))
			factor = 1.0;
		else if (runUnit.equals(displayUnits[3]))
			factor = 1000.0;
		else if (runUnit.equals(displayUnits[4]))
			factor = 60000.0;
		else if (runUnit.equals(displayUnits[5]))
			factor = 3600000.0;

		return nrun * factor;
	}

	/**
	 * @return the numeric value of the wait time.
	 */
	public double getActualWaitTime() {
		double factor = 1.0;
		if (waitUnit.equals(displayUnits[0]))
			factor = 0.000001;
		else if (waitUnit.equals(displayUnits[1]))
			factor = 0.001;
		else if (waitUnit.equals(displayUnits[2]))
			factor = 1.0;
		else if (waitUnit.equals(displayUnits[3]))
			factor = 1000.0;
		else if (waitUnit.equals(displayUnits[4]))
			factor = 60000.0;
		else if (waitUnit.equals(displayUnits[5]))
			factor = 3600000.0;

		return nwait * factor;
	}

	/**
	 * @return the numeric value of the wait port.
	 */
	public int getWaitPort() {
		int value = 0;
		for (int i = waitPulse.length() - 1; i >= 0; i--) {
			value *= 2;
			value += Character.getNumericValue(waitPulse.charAt(i));
		}
		return value;
	}

	/**
	 * @return the numeric value of the run port.
	 */
	public int getRunPort() {
		int value = 0;
		for (int i = runPulse.length() - 1; i >= 0; i--) {
			value *= 2;
			value += Character.getNumericValue(runPulse.charAt(i));
		}
		return value;
	}
	
	public void setRunTimes(double requestedTime) {
		int runTime;
		if ((runTime = (int) requestedTime/3600000) >= 1) {
			runUnit = displayUnits[5];
			nrun = runTime;
		} else if ((runTime = (int) requestedTime/60000) >= 1) {
			runUnit = displayUnits[4];
			nrun = runTime;			
		} else if ((runTime = (int) requestedTime/1000) >= 1) {
			runUnit = displayUnits[3];
			nrun = runTime;			
		} else if ((runTime = (int) requestedTime) >= 1) {
			runUnit = displayUnits[2];
			nrun = runTime;			
		} else if ((runTime = (int) requestedTime*1000) >= 1) {
			runUnit = displayUnits[1];
			nrun = runTime;			
		} else if ((runTime = (int) requestedTime*1000000) >= 1) {
			runUnit = displayUnits[0];
			nrun = runTime;			
		}
	}

	public void setWaitTimes(double requestedTime) {
		int waitTime;
		if ((waitTime = (int) (requestedTime/3600000)) >= 1) {
			waitUnit = displayUnits[5];
			nwait = waitTime;
		} else if ((waitTime = (int) (requestedTime/60000)) >= 1) {
			waitUnit = displayUnits[4];
			nwait = waitTime;			
		} else if ((waitTime = (int) (requestedTime/1000)) >= 1) {
			waitUnit = displayUnits[3];
			nwait = waitTime;			
		} else if ((waitTime = (int) requestedTime) >= 1) {
			waitUnit = displayUnits[2];
			nwait = waitTime;			
		} else if ((waitTime = (int) (requestedTime*1000.0)) >= 1) {
				waitUnit = displayUnits[1];
				nwait = waitTime;
		} else if ((waitTime = (int) (requestedTime*1000000.0)) >= 1) {
			waitUnit = displayUnits[0];
			nwait = waitTime;			
		}
		
	}
}
