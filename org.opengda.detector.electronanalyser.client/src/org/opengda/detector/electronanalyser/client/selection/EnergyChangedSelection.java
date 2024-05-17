package org.opengda.detector.electronanalyser.client.selection;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class EnergyChangedSelection implements ISelection {
	private List<Region> regions;
	private boolean excitationEnergyChange;
	private boolean fromLivePerspective;

	public EnergyChangedSelection(Region region, boolean fromExcitationEnergy) {
		this(Arrays.asList(region), fromExcitationEnergy);
	}

	public EnergyChangedSelection(List<Region> regions, boolean fromExcitationEnergy) {
		this.regions = regions;
		this.excitationEnergyChange = fromExcitationEnergy;
	}

	public List<Region> getRegions() {
		return regions;
	}

	public boolean isExcitationEnergyChange() {
		return excitationEnergyChange;
	}

	public boolean isFromLivePerspective() {
		return fromLivePerspective;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
