package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;

public class CanEditRegionSelection implements ISelection {

	private String filename;
	private boolean canEdit;

	public CanEditRegionSelection(String seqFileName, boolean canEdit) {
		this.filename = seqFileName;
		this.canEdit = canEdit;
	}

	public String getFilename() {
		return filename;
	}

	public boolean getCanEdit() {
		return canEdit;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}