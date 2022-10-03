package org.opengda.detector.electronanalyser.scan;

import gda.scan.ScanPositionProvider;
import gda.scan.ScanPositionProviderFactory;

public class RegionPositionProviderFactory extends ScanPositionProviderFactory {
	/**
	 * @param regionsList
	 * @return ScanPositionProvider
	 */
	public static ScanPositionProvider create(String filename){
		return new RegionPositionProvider(filename);
	}


}
