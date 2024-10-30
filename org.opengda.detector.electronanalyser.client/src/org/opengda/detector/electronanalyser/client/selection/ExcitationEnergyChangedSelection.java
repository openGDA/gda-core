package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;

public class ExcitationEnergyChangedSelection implements ISelection {

	private String excitationEnergySource;
	private double excitationEnergy;

	public ExcitationEnergyChangedSelection(String excitationEnergySource, double excitationEnergy) {
		this.excitationEnergySource = excitationEnergySource;
		this.excitationEnergy = excitationEnergy;
	}

	public double getExcitationEnergy() {
		return excitationEnergy;
	}

	public String getExcitationEnergySource() {
		return excitationEnergySource;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
