/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.scan;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;

/**
 *
 * Holds state of whole scan. May be used in annotated methods like &#64;ScanStart
 * to provide information about whole scan. Should not be used to hold transient
 * state during the scan. One should be created per run.
 *
 * @author Matthew Gerring
 *
 */
public class ScanInformation {

	private final ScanMode scanMode;
	private final String filePath;
	private final int size;
	private final int rank;
	private final List<String> scannableNames;
	private final int[] shape;
	private final long timePerPoint; // in ms
	private final long estimatedScanTime; // in ms

	public ScanInformation(IPointGeneratorService pointGenService, ScanRequest scanRequest) throws GeneratorException {
		this(pointGenService.createCompoundGenerator(scanRequest.getCompoundModel()), scanRequest.getDetectors().values(), scanRequest.getFilePath());
	}

	public ScanInformation(IPointGenerator<?> pointGen, ScanRequest scanRequest) throws GeneratorException {
		this(pointGen, scanRequest.getDetectors().values(), scanRequest.getFilePath());
	}

	public ScanInformation(IPointGenerator<?> pointGen, Collection<Object> detectorModels, String filePath) throws GeneratorException {
		boolean isMalcolmScan = detectorModels.stream().anyMatch(IMalcolmModel.class::isInstance);
		this.scanMode = isMalcolmScan ? ScanMode.HARDWARE : ScanMode.SOFTWARE;

		this.size = pointGen.size();
		// pointGen returns rank as 0 for acquire scans (correctly), but datasets must have min rank of 1
		// as AreaDetector writes a 1D datasets of size 1 in this case. See DAQ-2004
		this.rank = Math.max(pointGen.getRank(), 1);
		this.shape = pointGen.getShape();

		// the time per point is the maximum exposure time of a detector
		this.timePerPoint = detectorModels.stream()
				.filter(IDetectorModel.class::isInstance)
				.map(IDetectorModel.class::cast)
				.mapToDouble(IDetectorModel::getExposureTime)
				.map(e -> e * 1000)
				.mapToLong(Math::round)
				.reduce(0l, Math::max);
		this.estimatedScanTime = this.timePerPoint * this.size;

		this.scannableNames = pointGen.getNames();
		this.filePath = filePath;
	}

	public ScanMode getScanMode() {
		return scanMode;
	}

	public String getFilePath() {
		return filePath;
	}

	public int getSize() {
		return size;
	}

	public int getRank() {
		return rank;
	}

	public List<String> getScannableNames() {
		return scannableNames;
	}

	public int[] getShape() {
		return shape;
	}

	public long getTimePerPoint() {
		return timePerPoint;
	}

	public long getEstimatedScanTime() {
		return estimatedScanTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (estimatedScanTime ^ (estimatedScanTime >>> 32));
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + rank;
		result = prime * result + ((scanMode == null) ? 0 : scanMode.hashCode());
		result = prime * result + ((scannableNames == null) ? 0 : scannableNames.hashCode());
		result = prime * result + Arrays.hashCode(shape);
		result = prime * result + size;
		result = prime * result + (int) (timePerPoint ^ (timePerPoint >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanInformation other = (ScanInformation) obj;
		if (estimatedScanTime != other.estimatedScanTime)
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (rank != other.rank)
			return false;
		if (scanMode != other.scanMode)
			return false;
		if (scannableNames == null) {
			if (other.scannableNames != null)
				return false;
		} else if (!scannableNames.equals(other.scannableNames))
			return false;
		if (!Arrays.equals(shape, other.shape))
			return false;
		if (size != other.size)
			return false;
		if (timePerPoint != other.timePerPoint)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScanInformation [scanMode=" + scanMode + ", filePath=" + filePath + ", size=" + size + ", rank=" + rank
				+ ", scannableNames=" + scannableNames + ", shape=" + Arrays.toString(shape) + ", timePerPoint="
				+ timePerPoint + ", estimatedScanTime=" + estimatedScanTime + "]";
	}

}
