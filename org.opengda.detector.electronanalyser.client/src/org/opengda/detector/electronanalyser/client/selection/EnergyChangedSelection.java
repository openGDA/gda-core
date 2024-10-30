package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.api.SESRegion;

public class EnergyChangedSelection implements ISelection {
	private SESRegion region;
	private boolean showInvalidDialog = true;

	/**
	 * @return Returns the showInvalidDialog.
	 */
	public boolean isShowInvalidDialog() {
		return showInvalidDialog;
	}

	public EnergyChangedSelection(SESRegion region) {
		this.region = region;
	}

	public EnergyChangedSelection(SESRegion region, boolean showInvalidDialog) {
		this.region = region;
		this.showInvalidDialog = showInvalidDialog;
	}

	public SESRegion getRegion() {
		return region;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

}
