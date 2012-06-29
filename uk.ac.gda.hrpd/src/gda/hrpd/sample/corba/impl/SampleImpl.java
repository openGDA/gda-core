/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.hrpd.sample.corba.impl;

import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.hrpd.SampleInfo;
import gda.hrpd.sample.corba.CorbaSampleInfoPOA;
import gda.observable.IObserver;

/**
 * A server side implementation for a distributed SampleInfo interface.
 */
public class SampleImpl extends CorbaSampleInfoPOA implements IObserver {

	private SampleInfo theObject = null;

	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param object
	 *            the SampleInfo implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public SampleImpl(SampleInfo object, org.omg.PortableServer.POA poa) {
		this.theObject = object;
		this.poa = poa;

		name = theObject.getName();
		dispatcher = EventService.getInstance().getEventDispatcher();
		theObject.addIObserver(this);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the SampleInfo implementation object
	 */
	public SampleInfo _delegate() {
		return theObject;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param theobject
	 *            set the SampleInfo implementation object
	 */
	public void _delegate(SampleInfo theobject) {
		this.theObject = theobject;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void update(Object o, Object arg) {
		dispatcher.publish(name, arg);
	}

	@Override
	public String getCarouselNo() {
		return theObject.getCarouselNo();
	}

	@Override
	public String getComment() {
		return theObject.getComment();
	}

	@Override
	public String getDescription() {
		return theObject.getDescription();
	}

	@Override
	public int getRowOffset() {
		return theObject.getRowOffset();
	}

	@Override
	public String getSampleID() {
		return theObject.getSampleID();
	}

	@Override
	public String getSampleInfoFile() {
		return theObject.getSampleInfoFile();
	}

	@Override
	public String getSampleName() {
		return theObject.getSampleName();
	}

	@Override
	public String getTitle() {
		return theObject.getTitle();
	}

	@Override
	public boolean isSaveExperimentSummary() {
		return theObject.isSaveExperimentSummary();
	}

	@Override
	public void loadSampleInfo(int arg0) {
		theObject.loadSampleInfo(arg0);
	}

	@Override
	public void saveExperimentInfo(int arg0) {
		theObject.saveExperimentInfo(arg0);
	}

	@Override
	public void saveSampleInfo(int arg0) {
		theObject.saveSampleInfo(arg0);
	}

	@Override
	public void setBeamline(String arg0) {
		theObject.setBeamline(arg0);
	}

	@Override
	public void setCarouselNo(String arg0) {
		theObject.setCarouselNo(arg0);
	}

	@Override
	public void setComment(String arg0) {
		theObject.setComment(arg0);
	}

	@Override
	public void setDate(String arg0) {
		theObject.setDate(arg0);
	}

	@Override
	public void setDescription(String arg0) {
		theObject.setDescription(arg0);
	}

	@Override
	public void setExperiment(String arg0) {
		theObject.setExperiment(arg0);
	}

	@Override
	public void setProject(String arg0) {
		theObject.setProject(arg0);
	}

	@Override
	public void setRowOffset(int arg0) {
		theObject.setRowOffset(arg0);
	}

	@Override
	public void setRunNumber(String arg0) {
		theObject.setRunNumber(arg0);
	}

	@Override
	public void setSampleID(String arg0) {
		theObject.setSampleID(arg0);
	}

	@Override
	public void setSampleInfoFile(String arg0) {
		theObject.setSampleInfoFile(arg0);
	}

	@Override
	public void setSampleName(String arg0) {
		theObject.setSampleName(arg0);
	}

	@Override
	public void setSaveExperimentSummary(boolean arg0) {
		theObject.setSaveExperimentSummary(arg0);
	}

	@Override
	public void setTemperature(String arg0) {
		theObject.setTemperature(arg0);
	}

	@Override
	public void setTime(String arg0) {
		theObject.setTime(arg0);
	}

	@Override
	public void setTitle(String arg0) {
		theObject.setTitle(arg0);
	}

	@Override
	public void setWavelength(String arg0) {
		theObject.setWavelength(arg0);
	}

	@Override
	public void close() {
		theObject.close();
	}

	@Override
	public boolean isConfigured() {
		return theObject.isConfigured();
	}

	@Override
	public void open() {
		theObject.open();
	}

	@Override
	public void setCarouselNoInt(int arg0) {
		theObject.setCarouselNo(arg0);
	}

	@Override
	public void values() {
		theObject.values();
	}

}
