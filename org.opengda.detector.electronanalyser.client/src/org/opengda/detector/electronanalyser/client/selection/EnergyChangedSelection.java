package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class EnergyChangedSelection implements ISelection {
	private Region region;

	public EnergyChangedSelection(Region region) {
		this.region=region;
	}
	public Region getRegion() {
		return region;
	}
	@Override
	public boolean isEmpty() {
		return false;
	}

}
