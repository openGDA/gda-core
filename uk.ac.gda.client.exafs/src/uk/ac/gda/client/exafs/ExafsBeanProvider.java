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

package uk.ac.gda.client.exafs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.gda.beans.exafs.i20.CryostatParameters;
import uk.ac.gda.beans.exafs.i20.FurnaceParameters;
import uk.ac.gda.beans.exafs.i20.SampleStageParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.ui.doe.DOEBeanProvider;

public class ExafsBeanProvider implements DOEBeanProvider {

	private final Map<String,String> labels;
	public ExafsBeanProvider() {
		this.labels = new HashMap<String, String>(3); 
		labels.put(FurnaceParameters.class.getName()+":temperature",  "Temperature");
		labels.put(CryostatParameters.class.getName()+":temperature", "Set Point");
		labels.put(CryostatParameters.class.getName()+":sampleNumber", "Sample Number");
		labels.put(CryostatParameters.class.getName()+":position", "Position");
		labels.put(CryostatParameters.class.getName()+":finePosition", "Fine Position");
		labels.put(SampleStageParameters.class.getName()+":yaw",      "Yaw");
		labels.put(SampleStageParameters.class.getName()+":roll",     "Roll");
		labels.put(SampleStageParameters.class.getName()+":rotation", "Rotation");
	}

	@Override
	public List<Object> getBeans() throws Exception {
		
		if (EclipseUtils.getActivePage()!=null) EclipseUtils.getActivePage().saveAllEditors(true);
		
		final ScanObject runOb = (ScanObject) ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		if (runOb!=null) {
			final Object samp = runOb.getSampleParameters();
			final Object scan = runOb.getScanParameters();
            return Arrays.asList(new Object[]{samp,scan});
		}
		return null;
	}

	@Override
	public String getColumnLabel(String fieldName, Class<?> bean) {
		final String label = labels.get(bean.getName()+":"+fieldName);
		return label;
	}

}
