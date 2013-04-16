package gda.data.metadata;

import gda.data.nexus.tree.INexusTree;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;

public class NXDetectorDataChildNodeAppender1 implements NXDetectorDataAppender {

	private final INexusTree treeToAppend;
	
	public NXDetectorDataChildNodeAppender1(INexusTree treeToAppend) {
		this.treeToAppend = treeToAppend;
		
	}
	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		data.getNexusTree().addChildNode(treeToAppend);
	}

}