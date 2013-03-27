package org.opengda.detector.electronanalyser.client.sequenceeditor;

import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;

public interface IRegionDefinitionView {
	RegionDefinitionResourceUtil getRegionDefinitionResourceUtil();
	
	void refreshTable(String seqFileName, boolean newFile);
}
