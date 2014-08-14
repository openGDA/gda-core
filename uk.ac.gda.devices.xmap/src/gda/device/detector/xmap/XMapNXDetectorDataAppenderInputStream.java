package gda.device.detector.xmap;

import java.util.List;
import java.util.NoSuchElementException;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.scannable.PositionInputStream;

/**
 * Reads a raw HDF5 file from Xia XMap and returns deadtime corrected MCAs and Regions Of Interest totals.
 * 
 * @author rjw82
 *
 */
public class XMapNXDetectorDataAppenderInputStream implements PositionInputStream<NXDetectorDataAppender>{
	
	String fileName;
	ROI[] rois;

	
	
	public XMapNXDetectorDataAppenderInputStream(String fileName, ROI[] rois) {
		super();
		this.fileName = fileName;
		this.rois = rois;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

}
