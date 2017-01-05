/*-
 * Copyright © 2012 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.devices.edxd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;

public class AdDetectorExtRoiDraw extends ADDetector {

	private static final long serialVersionUID = 8072943990523173735L;

	private static Logger logger = LoggerFactory.getLogger(AdDetectorExtRoiDraw.class);

	private NDOverlay ndDraw;

	private NDROI ndRoi1;
	private NDROI ndRoi2;

	private FfmpegStream mjpeg1;
	private FfmpegStream mjpeg2;

	private NDProcess ndProc1;
	private NDProcess ndProc2;

	private NDFileHDF5 hdf;

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		try {
			getAdBase().stopAcquiring();
			getAdBase().setImageMode(0);
		} catch (Exception e) {
			logger.error("Cannot set Image Mode", e);
		}
	}

	public void setNdDraw(NDOverlay ndDraw) {
		this.ndDraw = ndDraw;
	}

	public NDOverlay getNdDraw() {
		return ndDraw;
	}

	public void setNdRoi1(NDROI ndRoi) {
		this.ndRoi1 = ndRoi;
	}

	public NDROI getNdRoi1() {
		return ndRoi1;
	}

	public FfmpegStream getMjpeg1() {
		return mjpeg1;
	}

	public void setMjpeg1(FfmpegStream mjpeg) {
		this.mjpeg1 = mjpeg;
	}

	@Override
	public void reset() throws Exception {
		super.reset();
		if (ndRoi1 != null) {
			ndRoi1.reset();
		}
		if (ndRoi2 != null) {
			ndRoi2.reset();
		}

		if (ndDraw != null) {
			ndDraw.reset();
		}

		if (mjpeg1 != null) {
			mjpeg1.reset();
		}
		if (mjpeg2 != null) {
			mjpeg2.reset();
		}

		if (ndProc1 != null) {
			ndProc1.reset();
		}
		if (ndProc2 != null) {
			ndProc2.reset();
		}
		if (hdf != null) {
			hdf.reset();
		}
	}

	public NDROI getNdRoi2() {
		return ndRoi2;
	}

	public void setNdRoi2(NDROI ndRoi2) {
		this.ndRoi2 = ndRoi2;
	}

	public FfmpegStream getMjpeg2() {
		return mjpeg2;
	}

	public void setMjpeg2(FfmpegStream mjpeg2) {
		this.mjpeg2 = mjpeg2;
	}

	public NDProcess getNdProc1() {
		return ndProc1;
	}

	public void setNdProc1(NDProcess ndProc1) {
		this.ndProc1 = ndProc1;
	}

	public NDProcess getNdProc2() {
		return ndProc2;
	}

	public void setNdProc2(NDProcess ndProc2) {
		this.ndProc2 = ndProc2;
	}

	public NDFileHDF5 getHdf() {
		return hdf;
	}

	public void setHdf(NDFileHDF5 hdf) {
		this.hdf = hdf;
	}
}
