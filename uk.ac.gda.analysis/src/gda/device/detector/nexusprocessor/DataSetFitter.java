/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;

public class DataSetFitter extends DataSetProcessorBase implements InitializingBean{
	private static final String[] NAMES_PER_DIM = { "centre", "fwhm", "area", "offset" };

	private static final Logger logger = LoggerFactory.getLogger(DataSetFitter.class);

	List<String> extraNames;
	List<String> formats;

	int dimensions=2;

	String prefix="";


	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		if( dimensions <1 || dimensions > 2)
			throw new RuntimeException("Only dimensions of 1 or 2 are valid");
		this.dimensions = dimensions;
	}

	@Override
	public GDANexusDetectorData process(String detName, String dataName, Dataset dataset) throws Exception {
		checkAfterPropertiesSetCalled();
		if(!enable)
			return null;
		int numFits = Math.min(dimensions, dataset.getShape().length);

		Double[] vals = new Double[extraNames.size()];
		Arrays.fill(vals, -1.0); //means failed or data not present
		try {
			CompositeFunction fit[] = new CompositeFunction[numFits];
			for (int i = 0; i < numFits; i++) {
				Dataset sum = dataset.sum(i);
				Dataset arange = DatasetFactory.createRange(sum.getShape()[0], Dataset.FLOAT64);
				double position_max = arange.max().doubleValue();
				double position_min = arange.min().doubleValue();
				double fwhm_max = arange.max().doubleValue();
				double maxVal = sum.max().doubleValue();
				double area_max = maxVal * fwhm_max;
				Gaussian gaussian = new Gaussian(position_min, position_max, fwhm_max, area_max);
				gaussian.getParameter(2).setLowerLimit(0.); // background
				double minVal = sum.min().doubleValue();
				fit[i] = Fitter.fit(arange, sum, new GeneticAlg(.01), gaussian, new Offset(minVal, maxVal));

				IParameter[] p = fit[i].getParameters();
				vals[i*4+0] = p[0].getValue();
				vals[i*4+1] = p[1].getValue();
				vals[i*4+2] = p[2].getValue();
				vals[i*4+3] = p[3].getValue() / dataset.getShape()[0];
			}
		} catch (Exception ex) {
			logger.error("Unable to fit to data", ex);
		}

		NXDetectorData res = new NXDetectorData();

		//the NexusDataWriter requires that the number of entries must be the same for all scan data points in a scan.
		for (int i = 0; i < extraNames.size(); i++) {
			String name = extraNames.get(i);
			res.addData(detName, dataName + "." + name, new NexusGroupData(vals[i]), null, 1);
		}

		res.setDoubleVals(vals);
		return res;
	}

	@Override
	protected Collection<String> _getExtraNames() {
		checkAfterPropertiesSetCalled();
		return enable ? extraNames : null;
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		checkAfterPropertiesSetCalled();
		return enable ? formats : null;
	}

	private void checkAfterPropertiesSetCalled(){
		if( !afterPropertiesSetCalled )
			throw new RuntimeException("afterPropertiesSet has not been called");
	}
	boolean afterPropertiesSetCalled=false;

	@Override
	public void afterPropertiesSet() throws Exception {
		extraNames = new Vector<String>();
		formats = new Vector<String>();
		for( int i=0; i< dimensions; i++){
			for (String name : NAMES_PER_DIM){
				extraNames.add( prefix + (i+1) + "_" + name);
				formats.add("%5.5g");
			}
		}
		afterPropertiesSetCalled=true;
	}

}
