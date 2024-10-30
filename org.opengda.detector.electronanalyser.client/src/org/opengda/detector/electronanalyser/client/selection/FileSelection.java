package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.api.SESSequence;

public class FileSelection implements ISelection {

	private String filename;
	private SESSequence sequence;

	public FileSelection(String seqFileName, SESSequence sequence) {
		this.filename=seqFileName;
		this.sequence = sequence;
	}
	public String getFilename() {
		return filename;
	}
	@Override
	public boolean isEmpty() {
		return true;
	}
	public SESSequence getSequence() {
		return sequence;
	}
	public void setSequence(SESSequence sequence) {
		this.sequence = sequence;
	}

}
