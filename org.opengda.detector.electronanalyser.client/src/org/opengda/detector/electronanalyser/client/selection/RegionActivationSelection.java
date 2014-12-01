package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class RegionActivationSelection implements ISelection {
	private Region region;

	public RegionActivationSelection(Region region) {
		this.region=region;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	public Region getRegion() {
		return region;
	}


}
