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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.doe.DOEUtils;
import uk.ac.gda.doe.RangeInfo;

/**
 * Class used to expand BeanGroups to create multiple experiments.
 */
public class BeanGroups {

	/**
	 * This class expands the sample parameters and scan parameters and
	 * constructs a collection from the loop:
	 *  
	 *   foreach SampleParameters
	 *     foreach ScanParameters
	 *        ...
	 *    
	 * NOTE: Other parameters in the BeanGroup are not processed at the moment.
	 * Only ranges in the sample environment and the scan parameters.    
	 * 
	 * DOEUtils is used to expand the ranges so weightings are read and used.
	 *        
	 * @param original
	 * @return expanded set of bean groups used for the experiment.
	 * @throws Exception 
	 */
	public static List<BeanGroup> expand(BeanGroup original) throws Exception {

		@SuppressWarnings("unchecked") // FIXME need to refactor DOEUtils to stop using Object...
		final List<IScanParameters> scans      = (List<IScanParameters>) DOEUtils.expand(original.getScan());

		if(original.getSample() != null)
		{
			@SuppressWarnings("unchecked") // FIXME need to refactor DOEUtils to stop using Object...
			final List<ISampleParameters> sampleEnvs = (List<ISampleParameters>) DOEUtils.expand(original.getSample());
			final List<BeanGroup> ret = new ArrayList<BeanGroup>(sampleEnvs.size()*scans.size());
			
			for (ISampleParameters sampleBean : sampleEnvs) {
				for (IScanParameters scanBean : scans) {
					BeanGroup bg = original.clone();
					bg.setSample(sampleBean);
					bg.setScan(scanBean);
					ret.add(bg);
				}
			}
			return ret;
		}
						
		final List<BeanGroup> ret = new ArrayList<BeanGroup>(scans.size());
		for (IScanParameters scanBean : scans) {
			BeanGroup bg = original.clone();
			bg.setScan(scanBean);
			ret.add(bg);
		}


		return ret;
	}
	
	/**
	 * 
	 * @param original
	 * @return list containing all the expanded run set information.
	 * @throws Exception
	 */
	public static List<RangeInfo> getInfo(BeanGroup original) throws Exception {
		
		return DOEUtils.getInfoFromList(Arrays.asList(new Object[]{original.getSample(), original.getScan()}));
		
	}

}
