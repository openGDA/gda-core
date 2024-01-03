package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class EnergyChangedSelection implements ISelection {
	private Region region;
	private boolean excitationEnergyChange;

	public EnergyChangedSelection(Region region, boolean fromExcitationEnergy) {
		this.region=region;
		this.excitationEnergyChange = fromExcitationEnergy;
	}
	public Region getRegion() {
		return region;
	}

	public boolean isExcitationEnergyChange() {
		return excitationEnergyChange;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
