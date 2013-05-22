package org.opengda.detector.electronanalyser.event;

import java.io.Serializable;

/**
 * Sequence file change event for broadcasting by {@link analyserscan} to observers of a script driven by an Scriptcontroller (or ScriptExecutor).
 * 
 */

public class SequenceFileChangeEvent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2658563187772008370L;
	private String filename;

	public SequenceFileChangeEvent(String filename) {
		this.setFilename(filename);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
