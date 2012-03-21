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

package uk.ac.gda.exafs.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;

import uk.ac.gda.beans.exafs.IonChamberParameters;

/**
 * A bean used to get information from the GainWizard and send to the GainCalculation.
 * Not curently designed for use outside EXAFS plugin.
 */
public abstract class GainBean {

	private Logger logger;
	
	// Vairables that change
	private Double               energy;
    private IonChamberParameters ionChamber;
	
    // Once set these stay.
	private String scannableName;
    private Long   collectionTime = 1000L;
    
    private Double referenceEdgeEnergy;
	private Double sampleEdgeEnergy;
	private Double finalEnergy;
	private Double tolerance;

	private IProgressMonitor monitor;
	
	/**
	 * @return Returns the referenceEdgeEnergy.
	 */
	public Double getReferenceEdgeEnergy() {
		return referenceEdgeEnergy;
	}
	/**
	 * @param referenceEdgeEnergy The referenceEdgeEnergy to set.
	 */
	public void setReferenceEdgeEnergy(Double referenceEdgeEnergy) {
		this.referenceEdgeEnergy = referenceEdgeEnergy;
	}
	/**
	 * @return Returns the sampleEdgeEnergy.
	 */
	public Double getSampleEdgeEnergy() {
		return sampleEdgeEnergy;
	}
	/**
	 * @param sampleEdgeEnergy The sampleEdgeEnergy to set.
	 */
	public void setSampleEdgeEnergy(Double sampleEdgeEnergy) {
		this.sampleEdgeEnergy = sampleEdgeEnergy;
	}
	/**
	 * @return Returns the finalEnergy.
	 */
	public Double getFinalEnergy() {
		return finalEnergy;
	}
	/**
	 * @param finalEnergy The finalEnergy to set.
	 */
	public void setFinalEnergy(Double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}
	/**
	 * @return Returns the tolerance.
	 */
	public Double getTolerance() {
		return tolerance;
	}
	/**
	 * @param tolerance The tolerance to set.
	 */
	public void setTolerance(Double tolerance) {
		this.tolerance = tolerance;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((collectionTime == null) ? 0 : collectionTime.hashCode());
		result = prime * result + ((energy == null) ? 0 : energy.hashCode());
		result = prime * result
				+ ((finalEnergy == null) ? 0 : finalEnergy.hashCode());
		result = prime * result
				+ ((ionChamber == null) ? 0 : ionChamber.hashCode());
		result = prime
				* result
				+ ((referenceEdgeEnergy == null) ? 0 : referenceEdgeEnergy
						.hashCode());
		result = prime
				* result
				+ ((sampleEdgeEnergy == null) ? 0 : sampleEdgeEnergy.hashCode());
		result = prime * result
				+ ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result
				+ ((tolerance == null) ? 0 : tolerance.hashCode());
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
		GainBean other = (GainBean) obj;
		if (collectionTime == null) {
			if (other.collectionTime != null) {
				return false;
			}
		} else if (!collectionTime.equals(other.collectionTime)) {
			return false;
		}
		if (energy == null) {
			if (other.energy != null) {
				return false;
			}
		} else if (!energy.equals(other.energy)) {
			return false;
		}
		if (finalEnergy == null) {
			if (other.finalEnergy != null) {
				return false;
			}
		} else if (!finalEnergy.equals(other.finalEnergy)) {
			return false;
		}
		if (ionChamber == null) {
			if (other.ionChamber != null) {
				return false;
			}
		} else if (!ionChamber.equals(other.ionChamber)) {
			return false;
		}
		if (referenceEdgeEnergy == null) {
			if (other.referenceEdgeEnergy != null) {
				return false;
			}
		} else if (!referenceEdgeEnergy.equals(other.referenceEdgeEnergy)) {
			return false;
		}
		if (sampleEdgeEnergy == null) {
			if (other.sampleEdgeEnergy != null) {
				return false;
			}
		} else if (!sampleEdgeEnergy.equals(other.sampleEdgeEnergy)) {
			return false;
		}
		if (scannableName == null) {
			if (other.scannableName != null) {
				return false;
			}
		} else if (!scannableName.equals(other.scannableName)) {
			return false;
		}
		if (tolerance == null) {
			if (other.tolerance != null) {
				return false;
			}
		} else if (!tolerance.equals(other.tolerance)) {
			return false;
		}
		return true;
	}
	/**
	 * @return Returns the scannableName.
	 */
	public String getScannableName() {
		return scannableName;
	}
	/**
	 * @param scannableName The scannableName to set.
	 */
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}
	/**
	 * @return Returns the counterTimerName.
	 */
	public String getCounterTimerName() {
		if (ionChamber==null) return null;
		return ionChamber.getDeviceName();
	}
	/**
	 * @return Returns the collectionTime.
	 */
	public Long getCollectionTime() {
		return collectionTime;
	}
	/**
	 * @param collectionTime The collectionTime to set.
	 */
	public void setCollectionTime(Long collectionTime) {
		this.collectionTime = collectionTime;
	}
	/**
	 * @param ionChamber The ionChamber to set.
	 */
	public void setIonChamber(IonChamberParameters ionChamber) {
		this.ionChamber = ionChamber;
	}
	/**
	 * @return Returns the currentAmplifierName.
	 */
	public String getCurrentAmplifierName() {
		if (ionChamber==null) return null;
		return ionChamber.getCurrentAmplifierName();
	}
	/**
	 * @return Returns the currentAmpSetting.
	 */
	public String getCurrentGain() {
		if (ionChamber==null) return null;
		return ionChamber.getGain();
	}
	/**
	 * @return Returns the energy.
	 */
	public Double getEnergy() {
		return energy;
	}
	/**
	 * @param energy The energy to set.
	 */
	public void setEnergy(Double energy) {
		this.energy = energy;
	}
	/**
	 * @return df
	 */
	public String getIonChamberName() {
		if (ionChamber==null) return null;
		return ionChamber.getName();
	}
	/**
	 * @return channel
	 */
	public int getChannel() {
		if (ionChamber==null) return -1;
		return ionChamber.getChannel();
	}
	/**
	 * @return Returns the logger.
	 */
	public Logger getLogger() {
		return logger;
	}
	/**
	 * @param logger The logger to set.
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	/**
	 * 
	 * @param monitor
	 */
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	/**
	 * 
	 * @param message
	 */
	public void setMonitorMessage(final String message) {
		if (monitor!=null) monitor.subTask(message);
	}
	
	/**
	 * 
	 */
	public void worked() {
		if (monitor!=null) monitor.worked(1);
	}
	
	/**
	 * Called to see if user cancelled it.
	 * @return isCancelled
	 */
	public boolean isCancelled() {
		return monitor!=null ? monitor.isCanceled() : false;
	}
	
	/**
	 * Call to log messages to the log file as info and to the user under the details toggle.
	 * @param message 
	 */
	public void log(final String message) {
		if (getLogger()!=null) getLogger().info(message);
		
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateMessage(message);
			}
        });
	}
	
	/**
	 * Notified when message should be updated. Saves adding a full listener/event
	 * capability to this class.
	 * @param line
	 */
	public abstract void updateMessage(final String line);
}
