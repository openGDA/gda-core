package org.opengda.detector.electronanalyser.api;

import java.io.Serializable;

import gda.device.Scannable;
import gda.factory.Finder;

/**
 * Class that defines the configuration for creating the default {@link SESExcitationEnergySource} used by
 * {@link SESSequence} and how the client displays it to the user. Designed to be created using spring beans with
 * {@link SESSettingsService#setSESConfigExcitationEnergySourceList(java.util.List)}
 */
public class SESConfigExcitationEnergySource implements Serializable {
	private static final long serialVersionUID = 5500438154892852605L;
	private String name;
	private String scannableName;
	private String displayName;
	private transient Scannable scannable;

	public SESConfigExcitationEnergySource(String name, String displayName, String scannableName) {
		this.name = name;
		this.scannableName = scannableName;
		this.displayName = displayName;
	}

	public String getName() {
		return name;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public Scannable getScannable() {
		if (scannable == null) {
			scannable = Finder.find(getScannableName());
		}
		return scannable;
	}

	public String getDisplayName() {
		return displayName;
	}
}