/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import gda.device.detector.xspress.XspressDetector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import uk.ac.gda.beans.validation.AbstractValidator;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.util.list.IntersectionException;
import uk.ac.gda.util.list.IntersectionUtils;

public class DetectorValidator extends AbstractValidator {
	

	private static AbstractValidator staticInstance;

	public static AbstractValidator getInstance() {
		if (staticInstance == null) staticInstance = new DetectorValidator();
		return staticInstance;
	}
	
	@Override
	public void validate(IExperimentObject bean) throws InvalidBeanException {
		throw new InvalidBeanException("Cannot deal with ValidationBean's in DetectorValidator");
	}

	/**
	 * Checks that the rois are the same in number for all elements.
	 * @param vp
	 * @return l
	 */
	public List<InvalidBeanMessage> validateVortexParameters(final VortexParameters vp) {
		
		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		
		final List<DetectorElement> dl = vp.getDetectorList();
		final int numRegions           = dl.get(0).getRegionList().size();
		for (int i = 0; i < dl.size(); i++) {
			final DetectorElement e = dl.get(i);
			if (e.getRegionList().size()!=numRegions) {
				errors.add(new InvalidBeanMessage("The element '"+e.getName()+"' does not have '"+numRegions+"' regions of interest defined.", "All elements must have the same number of regions."));
			}
			
			final List<RegionOfInterest> regions = e.getRegionList();
			for (RegionOfInterest roi : regions) {
				checkBounds("Start", roi.getWindowStart(), 0, roi.getWindowEnd(), errors, "The start is out of bounds.");
				checkBounds("End",   roi.getWindowEnd(),   roi.getWindowStart(), 2048, errors, "The end is out of bounds.");
				
				checkBounds("Size of Region", roi.getWindowEnd()-roi.getWindowStart(), 1d, 900d, errors, "The size of the region is incorrect, please change start or end of the region.");
			    checkNotNull("Name", roi.getRoiName(), errors, "The region name must be set.");
			}
			
			
			try {
				final List<Object[]> vals = getList(regions, "windowStart", "windowEnd", "roiName");
			    IntersectionUtils.checkIntersection(vals);
			} catch (IntersectionException ne) {
				errors.add(new InvalidBeanMessage("The regions for '"+ne.getFirstName()+"' and '"+ne.getSecondName()+"' in element '"+e.getName()+"' are intersecting."));
			} catch (Exception ne) {
				errors.add(new InvalidBeanMessage("An internal error occurred checking region intersection."));
			}

		}
		
		
		return errors;
	}

	
	/**
	 * Does nothing currently
	 * @param xp
	 * @return l
	 */
	public List<InvalidBeanMessage> validateXspressParameters(final XspressParameters xp) {
		
		// Check for intersecting windows or regions.
		final List<InvalidBeanMessage> errors = new ArrayList<InvalidBeanMessage>(31);
		if (!xp.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			for (uk.ac.gda.beans.xspress.DetectorElement e : xp.getDetectorList()) {
				checkBounds("Start", e.getWindowStart(), 0, e.getWindowEnd(), errors, "The start is out of bounds.");
				checkBounds("End",   e.getWindowEnd(),   e.getWindowStart(), 4095, errors, "The end is out of bounds.");
			}

		} else {
			for (uk.ac.gda.beans.xspress.DetectorElement e : xp.getDetectorList()) {
			
				final List<XspressROI> rs = e.getRegionList();
				for (XspressROI roi : rs) {
					checkBounds("Start", roi.getRoiStart(), 0, roi.getRoiEnd(), errors, "The start is out of bounds.");
					checkBounds("End",   roi.getRoiEnd(),   roi.getRoiStart(), 4095, errors, "The end is out of bounds.");				
				    checkNotNull("Name", roi.getRoiName(), errors, "The region name must be set.");
				}
			
				try {
					final List<Object[]> regions = getList(e.getRegionList(), "roiStart", "roiEnd", "roiName");
				    IntersectionUtils.checkIntersection(regions);
				} catch (IntersectionException ne) {
					errors.add(new InvalidBeanMessage("The regions for '"+ne.getFirstName()+"' and '"+ne.getSecondName()+"' in element '"+e.getName()+"' are intersecting."));
				} catch (Exception ne) {
					errors.add(new InvalidBeanMessage("An internal error occurred checking region intersection."));
				}
				
			}
		}
		
		return errors;
	}

	private List<Object[]> getList(List<? extends Object> elementOrRegionList, String startName, String endName, final String nameName) throws Exception {
		
		final List<Object[]> ret = new ArrayList<Object[]>(elementOrRegionList.size());
//		int index = 1;
		for (Object e : elementOrRegionList) {
			final Number start =  (Number)getBeanValue(e, startName);
			final Number end   =  (Number)getBeanValue(e, endName);
			Object name  =  getBeanValue(e, nameName);
			if (name==null) continue; // There must be a name
			ret.add(new Object[]{start,end,name});
//			index++;
		}
		return ret;
	}

	
	/**
	 * There must be a smarter way of doing this 
	 * i.e. a JDK method I cannot find. However
	 * it is one line of Java so after spending
	 * some time looking have coded self.
	 * 
	 * @param fieldName
	 * @return String
	 */
	private static String getGetterName(final String fieldName) {
		if (fieldName == null) return null;
		return getName("get", fieldName);
	}
	private static String getName(final String prefix, final String fieldName) {
		return prefix+getFieldWithUpperCaseFirstLetter(fieldName);
	}

	/**
	 * 
	 * @param fieldName
	 * @return field with upper case first letter.
	 */
	private static String getFieldWithUpperCaseFirstLetter(final String fieldName) {
		return fieldName.substring(0,1).toUpperCase(Locale.US)+fieldName.substring(1);
	}
	/**
	 * Method gets value out of bean using reflection.
	 * @param bean
	 * @param fieldName
	 * @return value
	 * @throws Exception 
	 */
	private static Object getBeanValue(final Object bean, final String fieldName) throws Exception {
		final String getterName = getGetterName(fieldName);
		final Method method     = bean.getClass().getMethod(getterName);
		return method.invoke(bean);
	}

}
