package gda.device.detector.xmap;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.scannable.PositionInputStream;

import java.util.List;
import java.util.NoSuchElementException;

import uk.ac.gda.beans.DetectorROI;

/**
 * Reads a raw HDF5 file from Xia XMap and returns deadtime corrected MCAs and
 * Regions Of Interest totals.
 * 
 * @author rjw82
 * 
 */
public class XMapNXDetectorDataAppenderInputStream implements
		PositionInputStream<NXDetectorDataAppender> {

	String fileName;
	DetectorROI[] rois;

	public XMapNXDetectorDataAppenderInputStream(String fileName,
			DetectorROI[] rois) {
		super();
		this.fileName = fileName;
		this.rois = rois;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {
		// TODO under development! Need to use the XmapNexusFileLoader to
		// extract the raw MCAs and then reuse the code inside
		// NexusXmap.readout() to create the chunks for Nexus file. NB:
		// NexusXmap returns NexusTreeProvider but this method needs to return
		// NXDetectorDataAppender.
		return null;
	}

}
