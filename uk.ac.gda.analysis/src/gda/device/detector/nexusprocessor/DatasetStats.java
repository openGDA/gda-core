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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.eclipse.january.dataset.Dataset;

public class DatasetStats extends DataSetProcessorBase {

	static List<String> formats;
	static{
		formats = java.util.Arrays.asList(new String[] { "%5.5g",  "%5.5g" });
	}
	String totalName="total";
	String averageName="average";


	public String getTotalName() {
		return totalName;
	}

	public void setTotalName(String totalName) {
		this.totalName = totalName;
	}

	public String getAverageName() {
		return averageName;
	}

	public void setAverageName(String averageName) {
		this.averageName = averageName;
	}

	List<String> outputNames;
	private boolean profileX;
	private boolean profileY;

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception {
		if(!enable)
			return null;
		Object sum = dataset.sum();
		Double vals[] = new Double[]{0.0, 0.0};
		if( sum instanceof Number){
			vals[0]= ((Number)sum).doubleValue();
		} else {
			return null;
		}
		int size = dataset.getSize();
		if( size > 0)
			vals[1] = vals[0]/dataset.getSize();

		NXDetectorData res = new NXDetectorData();

		//the NexusDataWriter requires that the number of entries must be the same for all scan data points in a scan.
		List<String> names = getOutputNames();
		for (int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			res.addData(detectorName, dataName + "." + name, new NexusGroupData(vals[i]), null, 1);
		}
		if(profileX){
			Dataset sum2 = dataset.sum(0);
			Serializable buffer = sum2.getBuffer();
			long[] sum0 = (long[] )buffer;//TODO must deal with other types
			res.addData(detectorName, dataName + "." + "profileX", new NexusGroupData(sum0), null, 1);
		}
		if(profileY){
			long[] sum1 = (long[] )dataset.sum(1).getBuffer();
			res.addData(detectorName, dataName + "." + "profileY", new NexusGroupData(sum1), null, 1);
		}

		res.setDoubleVals(vals);
		return res;
	}

	public boolean isProfileX() {
		return profileX;
	}

	public void setProfileX(boolean profileX) {
		this.profileX = profileX;
	}

	public boolean isProfileY() {
		return profileY;
	}

	public void setProfileY(boolean profileY) {
		this.profileY = profileY;
	}

	private List<String> getOutputNames(){
		if( outputNames == null){
			outputNames = java.util.Arrays.asList(new String[] { totalName, averageName });
		}
		return outputNames;
	}

	@Override
	protected Collection<String> _getExtraNames() {
		return getOutputNames();
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return formats;
	}

	@Override
	public String toString() {
		return "DatasetStats [enable=" + enable + "]";
	}

}
