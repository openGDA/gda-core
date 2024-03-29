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

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;

public class DatasetFitter extends DatasetProcessorBase implements InitializingBean{

	public static final List<String> NAMES_PER_DIM = List.of("centre", "fwhm", "area", "offset"); // NOSONAR suppress modifiable

	private static final Logger logger = LoggerFactory.getLogger(DatasetFitter.class);

	private List<String> extraNames;
	private List<String> formats;

	private int dimensions=2;

	private String prefix="";

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
			throw new IllegalArgumentException("Only dimensions of 1 or 2 are valid");
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
			final CompositeFunction[] fit = new CompositeFunction[numFits];
			for (int i = 0; i < numFits; i++) {
				Dataset sum = dataset.sum(i);
				Dataset arange = DatasetFactory.createRange(sum.getShapeRef()[0]);
				double positionMax = arange.max().doubleValue();
				double positionMin = arange.min().doubleValue();
				double fwhmMax = arange.max().doubleValue();
				double maxVal = sum.max().doubleValue();
				double areaMax = maxVal * fwhmMax;
				Gaussian gaussian = new Gaussian(positionMin, positionMax, fwhmMax, areaMax);
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

		final NXDetectorData detectorData = new NXDetectorData();

		//the NexusDataWriter requires that the number of entries must be the same for all scan data points in a scan.
		int fieldIndex = 0;
		for (String fieldName : extraNames) {
			final INexusTree extraNameNode = detectorData.addData(detName, dataName + "." + fieldName,
					new NexusGroupData(vals[fieldIndex]), null, 1);
			extraNameNode.addChildNode(new NexusTreeNode(ATTRIBUTE_NAME_LOCAL_NAME, NexusExtractor.AttrClassName,
					extraNameNode, new NexusGroupData(detName + "." + fieldName)));
			fieldIndex++;
		}

		detectorData.setDoubleVals(vals);
		return detectorData;
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
		if (!afterPropertiesSetCalled)
			throw new IllegalStateException("afterPropertiesSet has not been called");
	}

	private boolean afterPropertiesSetCalled = false;

	@Override
	public void afterPropertiesSet() throws Exception {
		extraNames = new ArrayList<>();
		formats = new ArrayList<>();
		for (int i = 0; i < dimensions; i++){
			for (String name : NAMES_PER_DIM) {
				extraNames.add(prefix + (i+1) + "_" + name);
				formats.add("%5.5g");
			}
		}
		afterPropertiesSetCalled = true;
	}

}
