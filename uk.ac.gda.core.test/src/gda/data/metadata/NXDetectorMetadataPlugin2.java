package gda.data.metadata;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.plugin.NXDetectorMetadataPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class NXDetectorMetadataPlugin2 extends NXDetectorMetadataPlugin{

	public NXDetectorMetadataPlugin2(NexusTreeProvider metaDataProvider) {
		super(metaDataProvider);
	}
	
	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		if (firstReadoutInScan) {
			INexusTree treeToAppend = getMetaDataProvider().getNexusTree();
			appenders.add(new NXDetectorDataChildNodeAppender1(treeToAppend));
		} else {
			appenders.add(new NXDetectorDataNullAppender());
		}
		firstReadoutInScan = false;
		return appenders;
	}	
}