package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;

public class FileSelection implements ISelection {

	private String filename;

	public FileSelection(String seqFileName) {
		this.filename=seqFileName;
	}
	public String getFilename() {
		return filename;
	}
	@Override
	public boolean isEmpty() {
		return true;
	}

}
