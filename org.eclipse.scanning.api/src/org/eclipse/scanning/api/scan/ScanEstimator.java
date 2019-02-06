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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;

/**
 *
 * Most scans are static and therefore they can have their shape and size
 * discovered. Those scans which truely are iterators and on the fly decide
 * the next position, can only have their shapes and sizes estimated.
 *
 * This class is an estimator and not a data holder. Please use ScanInformation
 * to hold data to be sent around.
 *
 * @author Matthew Gerring
 *
 */
public class ScanEstimator {

	/**
	 * Size, number of points in scan
	 */
	private final int   size;

	/**
	 * The rank of the scan
	 */
	private final int rank;

	/**
	 * Estimated time of scan
	 */
	private final long  estimatedScanTime;

	/**
	 *
	 */
	private long  timePerPoint = -1;

	/**
	 *
	 */
	private int[] shape;

	/**
	 *
	 * @param pservice
	 * @param bean
	 */
	public ScanEstimator(IPointGeneratorService pservice, ScanRequest<?> request) throws GeneratorException{
		this(pservice, request, 0);
	}

	/**
	 *
	 * @param pservice
	 * @param bean
	 * @param timePerPoint ms
	 * @throws GeneratorException
	 */
	public ScanEstimator(IPointGeneratorService pservice, ScanRequest<?> request, long timePerPoint) throws GeneratorException {
		this(pservice.createCompoundGenerator(request.getCompoundModel()), request.getDetectors(), timePerPoint);
	}

	public ScanEstimator(IPointGenerator<?> pointGenerator, List<IRunnableDevice<?>> detectors) throws GeneratorException {
		this(pointGenerator, detectorsToModels(detectors), 0);
	}

	private static Collection<Object> detectorsToModels(List<IRunnableDevice<?>> detectors) {
		return detectors == null ? null : detectors.stream().map(IRunnableDevice::getModel).collect(toList());
	}

	/**
	 *
	 * @param pservice
	 * @param request
	 * @param timePerPoint
	 * @throws GeneratorException
	 */
	public ScanEstimator(IPointGenerator<?> gen, Map<String, Object> detectors, long timePerPoint) throws GeneratorException {
		this(gen, detectors == null ? null : detectors.values(), timePerPoint);
	}

	/**
	 * Create a scan estimator for the given positions, (optional) detectors and (optional) time per point
	 * @param pointGenerator the generator of the positions in the scan
	 * @param detectorModels detector models, may be <code>null</code>
	 * @param timePerPoint time per point, only used if <code> detectorModels</code> is <code>null</code>
	 * @throws GeneratorException if the scan estimator cannot be created
	 */
	public ScanEstimator(IPointGenerator<?> pointGenerator, Collection<Object> detectorModels, long timePerPoint) throws GeneratorException {
		// TODO FIXME If some detectors are malcolm, they may have a wait time.
		// If some are malcolm we may wish to ignore the input point time from the user
		// in favour of the malcolm time per point or maybe the device tells us how long it will take?
		if (detectorModels != null && !detectorModels.isEmpty()) {
			timePerPoint = detectorModels.stream().filter(IDetectorModel.class::isInstance)
					.map(m -> Math.round(((IDetectorModel) m).getExposureTime() * 1000))
					.reduce(0l, Math::max);
		}

		this.size = pointGenerator.size();
		int scanRank = pointGenerator.getRank();
		this.rank = scanRank == 0 ? 1 : scanRank; // TODO fix this, see DAQ-2004
		this.shape = pointGenerator.getShape();
		this.timePerPoint = timePerPoint;
		this.estimatedScanTime = size * timePerPoint;
	}

	public int getSize() {
		return size;
	}

	public long getTimePerPoint() {
		return timePerPoint;
	}

	public void setTimePerPoint(long timePerPoint) {
		this.timePerPoint = timePerPoint;
	}

	public long getEstimatedScanTime() {
		return timePerPoint * size;
	}

	public int getRank() {
		return rank;
	}

	public int[] getShape() {
		return shape;
	}

}
