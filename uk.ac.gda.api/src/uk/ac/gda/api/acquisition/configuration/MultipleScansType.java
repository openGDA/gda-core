package uk.ac.gda.api.acquisition.configuration;

public enum MultipleScansType {
	/**
	 * Repeats the scan sequence as [Start to End], [Start to End], [Start to End]....
	 */
	REPEAT_SCAN,
	/**
	 * Repeats the scan sequence as [Start to End], [End to Start], [Start to End]....
	 */
	SWITCHBACK_SCAN
}
