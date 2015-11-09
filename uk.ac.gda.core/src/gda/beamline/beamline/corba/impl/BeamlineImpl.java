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

package gda.beamline.beamline.corba.impl;

import gda.beamline.BeamlineInfo;
import gda.beamline.corba.CorbaBeamlineInfoPOA;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.observable.IObserver;

/**
 * A server side implementation for a distributed BeamlineInfo interface.
 */
public class BeamlineImpl extends CorbaBeamlineInfoPOA implements IObserver {

	private BeamlineInfo theObject = null;

	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param object
	 *            the BeamlineInfo implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public BeamlineImpl(BeamlineInfo object, org.omg.PortableServer.POA poa) {
		this.theObject = object;
		this.poa = poa;

		name = theObject.getName();
		dispatcher = EventService.getInstance().getEventDispatcher();
		theObject.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Get the implementation object
	 *
	 * @return the BeamlineInfo implementation object
	 */
	public BeamlineInfo _delegate() {
		return theObject;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param theobject
	 *            set the BeamlineInfo implementation object
	 */
	public void _delegate(BeamlineInfo theobject) {
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
	public String getDataDir() {
		return theObject.getDataDir();
	}

	@Override
	public String getExperimentName() {
		return theObject.getExperimentName();
	}

	@Override
	public String getFileExtension() {
		return theObject.getFileExtension();
	}

	@Override
	public int getFileNumber() {
		return theObject.getFileNumber();
	}

	@Override
	public String getFilePrefix() {
		return theObject.getFilePrefix();
	}

	@Override
	public String getFileSuffix() {
		return theObject.getFileSuffix();
	}

	@Override
	public String getHeader() {
		return theObject.getHeader();
	}

	@Override
	public int getNextFileNumber() {
		return theObject.getNextFileNumber();
	}

	@Override
	public String getProjectName() {
		return theObject.getProjectName();
	}

	@Override
	public String getSubHeader() {
		return theObject.getSubHeader();
	}

	@Override
	public void setDataDir(String arg0) {
		theObject.setDataDir(arg0);
	}

	@Override
	public void setExperimentName(String arg0) {
		theObject.setExperimentName(arg0);
	}

	@Override
	public void setFileExtension(String arg0) {
		theObject.setFileExtension(arg0);
	}

	@Override
	public void setFilePrefix(String arg0) {
		theObject.setFilePrefix(arg0);
	}

	@Override
	public void setFileSuffix(String arg0) {
		theObject.setFileSuffix(arg0);
	}

	@Override
	public void setHeader(String arg0) {
		theObject.setHeader(arg0);
	}

	@Override
	public void setProjectName(String arg0) {
		theObject.setProjectName(arg0);
	}

	@Override
	public void setSubHeader(String arg0) {
		theObject.setSubHeader(arg0);
	}

}
