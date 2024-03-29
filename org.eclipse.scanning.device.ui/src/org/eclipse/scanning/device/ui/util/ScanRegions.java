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
package org.eclipse.scanning.device.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 *
 * Utility for managing scan regions.
 *
 * @author Matthew Gerring
 *
 */
public class ScanRegions {

	/**
	 * Find a plotting system by adaptable and then read the scan regions
	 * from it and then calculate their bounding box.
	 * @return
	 * @param the model or null if the bounds of all regions are required.
	 * @return null if there arn't any regions with a user object set to a ScanRegion
	 * @throws GeneratorException
	 */
	public static final BoundingBox createBoxFromPlot() throws GeneratorException {
		return createBoxFromPlot(null);
	}

	/**
	 * Find a plotting system by adaptable and then read the scan regions
	 * from it and then calculate their bounding box.
	 * @return
	 * @param the model or null if the bounds of all regions are required.
	 * @return null if there arn't any regions with a user object set to a ScanRegion
	 * @throws GeneratorException
	 */
	public static final BoundingBox createBoxFromPlot(IScanPointGeneratorModel model) throws GeneratorException {

		IPlottingSystem<?> system  = PlotUtil.getRegionSystem();
		if (system==null) return null;

		List<IROI> rois;
		if (model!=null) {
			rois = ServiceProvider.getService(IPointGeneratorService.class).findRegions(model, getScanRegions(system));
		} else {
			// TODO FIXME Analysis renames axes which then do not match the scannable name
			final List<String>     axes    = Arrays.asList(system.getSelectedXAxis().getTitle(),
	                                                       system.getSelectedYAxis().getTitle());
			List<ScanRegion> regions = ScanRegions.getScanRegions(system, axes);
			if (regions.isEmpty()) return null;
			rois = regions.stream().map(ScanRegion::getRoi).collect(Collectors.toList());
		}

		return bounds(rois);
	}

	private static final BoundingBox bounds(List<IROI> rois) {

		rois = Optional.of(rois).orElse(Collections.emptyList());
		IRectangularROI rect = rois.get(0).getBounds();
		for (IROI roi : rois) rect = rect.bounds(roi);

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(rect.getPoint()[0]);
		box.setyAxisStart(rect.getPoint()[1]);
		box.setxAxisLength(rect.getLength(0));
		box.setyAxisLength(rect.getLength(1));
		return box;
	}

	/**
	 * Create the plotted regions for this list of ScanRegions.
	 * @param system
	 * @param regions
	 * @throws Exception
	 */
	public static void createRegions(IPlottingSystem<?> system, List<ScanRegion> regions) throws Exception {
		if (regions!=null && !regions.isEmpty()) {
			for (ScanRegion scanRegion : regions) {
				IRegion region = createRegion(system, (RegionType)scanRegion.getType(), scanRegion.getRoi());
				if (region != null) {
					region.setUserObject(scanRegion); // Override default because we know it.
				}
			}
		}
	}

	/**
	 *
	 * @param system
	 * @param regionType
	 * @param roi - may be null. If null then the region is not added and the plotting UI is left in a state awaiting a drag event.
	 * @return
	 * @throws Exception
	 */
	public static IRegion createRegion(IPlottingSystem<?> system, RegionType regionType, IROI roi) throws Exception {

		if (system==null) return null;
		IRegion region = system.createRegion(RegionUtils.getUniqueName("Scan "+regionType.getName(), system), regionType);

		String x = system.getSelectedXAxis().getTitle();
		String y = system.getSelectedYAxis().getTitle();
		region.setUserObject(new ScanRegion(region.getName(), regionType, Arrays.asList(x,y)));
		region.setAlpha(30);
		if (roi!=null) {
			region.setROI(roi);
			system.addRegion(region);
			region.repaint();
		}
		return region;
	}

	/**
	 * Search for and return the regions which are to be involved in a scan and
	 * active over the current regions.
	 *
	 * @param system
	 * @return scan regions, never <code>null</code>
	 */
	public static List<ScanRegion> getScanRegions(IPlottingSystem<?> system) {
        return getScanRegions(system, null);
	}

	/**
	 * Search for and return the regions which are to be involved in a scan.
	 *
	 * @param system
	 * @return regions, never <code>null</code>
	 */
	public static List<ScanRegion> getScanRegions(IPlottingSystem<?> system, List<String> axes) {

		final Collection<IRegion> regions = system.getRegions();
		if (regions==null || regions.isEmpty()) return Collections.emptyList();

		final List<ScanRegion> ret = new ArrayList<>();
		for (IRegion region : regions) {
			if (region.getUserObject() instanceof ScanRegion) {
				@SuppressWarnings("unchecked")
				ScanRegion sr = (ScanRegion)region.getUserObject();
				if (axes!=null && !sr.getScannables().equals(axes)) continue;
				sr.setRoi(region.getROI());
				ret.add(sr);
			}
		}

		return ret;
	}

}
