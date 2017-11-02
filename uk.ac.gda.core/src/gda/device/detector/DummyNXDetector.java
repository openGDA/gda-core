/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.observable.IObserver;

/**
 *
 */
public class DummyNXDetector implements NexusDetector {

	private String name;
	int numElements=1;

	/**
	 *
	 * @param name - prefix for each element
	 * @param numElements  - number of elements - if 1 then the name is simply the value of name
	 */
	public DummyNXDetector(String name, int numElements) {
		this.name = name;
		this.numElements = numElements;
	}

	@Override
	public NXDetectorData readout() throws DeviceException {
		NXDetectorData data = new NXDetectorData();

		for(int i=0; i< numElements; i++){
			String elementName = name+"_"+Integer.toString(i);
			if( numElements == 1)
				elementName = name;
			data.addNote(elementName,"Just a test");

			double[] dataVals = {1.1,2.2,3.3,4.4,5.5,6.6,7.7,8.8,9.9,0.0};
			data.addData(elementName, new NexusGroupData(dataVals), "counts", 1);

			int[] axis = {100,200,300,400,500,600,700,800,900,1000};
			data.addAxis(elementName,"useless", new NexusGroupData(axis), 1, 1, "pixels", false);


			{
				NexusGroupData dead_time_data = new NexusGroupData(0.11);
				dead_time_data.isDetectorEntryData = true;
				NexusTreeNode dead_time = new NexusTreeNode("dead_time",NexusExtractor.SDSClassName, null, dead_time_data);
				dead_time.addChildNode(new NexusTreeNode("signal",NexusExtractor.AttrClassName, dead_time,new NexusGroupData("2")));
				dead_time.setIsPointDependent(true);
				data.getDetTree(elementName).addChildNode(dead_time);
			}
		}

		return data;
	}


	@Override
	public void collectData() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void endCollection() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getStatus() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		// TODO Auto-generated method stub

	}



	@Override
	public void setCollectionTime(double time) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getCollectionTime() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atCommandFailure() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atEnd() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atLevelStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atLevelEnd() throws DeviceException {
	}

	@Override
	public void atPointEnd() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atPointStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atScanEnd() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atScanLineStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atScanStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void atStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] getExtraNames() {
		// TODO Auto-generated method stub
		return new String[]{};
	}

	@Override
	public String[] getInputNames() {
		// TODO Auto-generated method stub
		return new String[]{};
	}

	@Override
	public int getLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getOutputFormat() {
		return null;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return null;
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return false;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public String checkPositionValid(Object position) {
		return null;
	}

	@Override
	public void moveTo(Object position) throws DeviceException {

	}

	@Override
	public void setExtraNames(String[] names) {

	}

	@Override
	public void setInputNames(String[] names) {

	}

	@Override
	public void setLevel(int level) {

	}

	@Override
	public void setOutputFormat(String[] names) {

	}

	@Override
	public void stop() throws DeviceException {

	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {

	}

	@Override
	public void close() throws DeviceException {

	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return null;
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return 0;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {

	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {

	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {

	}

	@Override
	public void deleteIObservers() {

	}

	@Override
	public void reconfigure() throws FactoryException {

	}

	@Override
	public String toString() {
		return getName() + "<" + this.getClass().toString() + ">";
	}

	@Override
	public String toFormattedString() {
		try {
			return getName() + " : " + getStatus();
		} catch (DeviceException e) {
			return String.format("%s : %s", getName(), VALUE_UNAVAILABLE);
		}
	}
}