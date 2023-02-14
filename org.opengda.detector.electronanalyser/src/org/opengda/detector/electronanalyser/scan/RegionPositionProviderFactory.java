package org.opengda.detector.electronanalyser.scan;

import gda.scan.ScanPositionProvider;

public class RegionPositionProviderFactory {

	private RegionPositionProviderFactory() {
		// private constructor to prevent instantiation
	}

	/**
	 * @param regionsList
	 * @return ScanPositionProvider
	 */
	public static ScanPositionProvider create(String filename) {
		return new RegionPositionProvider(filename);
	}


}
