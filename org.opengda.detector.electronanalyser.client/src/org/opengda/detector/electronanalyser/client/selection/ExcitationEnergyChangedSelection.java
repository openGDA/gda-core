package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;

public class ExcitationEnergyChangedSelection implements ISelection {

	private String excitationEnergySourceName;
	private double excitationEnergy;

	public ExcitationEnergyChangedSelection(String excitationEnergySourceName, double excitationEnergy) {
		this.excitationEnergySourceName = excitationEnergySourceName;
		this.excitationEnergy = excitationEnergy;
	}

	public double getExcitationEnergy() {
		return excitationEnergy;
	}

	public String getExcitationEnergySourceName() {
		return excitationEnergySourceName;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
