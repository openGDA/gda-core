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

package gda.device.epicsdevice;

import gda.device.DeviceException;
import gda.epics.generated.Device;
import gda.epics.generated.Interface;
import gda.epics.generated.Subsystem;
import gda.epics.generated.Type;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.xml.DevicesParser;
import gda.epics.xml.TypesParser;
import gda.factory.Factory;
import gda.factory.FactoryBase;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class EpicsDeviceFactory extends FactoryBase implements Factory {

	private ArrayList<Findable> findables = new ArrayList<Findable>();
	boolean simulated=false;
	/**
	 * @return True if devices are simulated
	 */
	public boolean isSimulated() {
		return simulated;
	}

	/**
	 * @param simulated
	 */
	public void setSimulated(boolean simulated) {
		this.simulated = simulated;
	}

	/**
	 * Constructor only used by this class's main method.
	 * 
	 * @param devicesFile
	 * @param typesFile
	 * @throws DeviceException
	 */
	public EpicsDeviceFactory(String devicesFile, String typesFile) throws DeviceException {
		/*
		 * Get list of devices from devices xml For each device build recordPVs hasMap. Construct EpicsDevice and add to
		 * the list
		 */
		DevicesParser devicesParser = DevicesParser.createDevicesParser(devicesFile);
		TypesParser typesParser = TypesParser.createTypesParser(typesFile);
		ArrayList<Device> deviceArrayList = devicesParser.getDeviceList();
		for (Device device : deviceArrayList) {
			HashMap<String, String> recordPVs = new HashMap<String, String>();
			String devName = device.getName().toString();
			String pvPrefix = device.getEpicsname().toString();
			Type[] types = device.getType();
			for (Type type : types) {
				Object obj = type.getName();
				if (obj instanceof String) {
					String interfaceName = (String) obj;
					Interface devInterface = typesParser.getInterfaceByName(interfaceName);
					Subsystem subsystems[] = devInterface.getSubsystem();
					for (Subsystem subsystem : subsystems) {
						String pv = subsystem.getPv().toString();
						String name = subsystem.getName().toString();
						if (pv.startsWith("/")) {
							recordPVs.put(name, pvPrefix + pv.substring(1, pv.length() - 1));
						} else {
							recordPVs.put(name, pvPrefix + ":" + pv);
						}
					}
				}
			}
			EpicsDevice ed = new EpicsDevice(devName, recordPVs, true);
			findables.add(ed);
		}
	}

	/**
	 * The constructor called by object server.
	 */
	public EpicsDeviceFactory() {
	}

	@Override
	public void configure() throws FactoryException {
		try {

			List<String> deviceNames = GDAEpicsInterfaceReader.getAllDeviceNames();
			for (String devName : deviceNames) {
				FindableEpicsDevice fed = new FindableEpicsDevice();
				fed.setName(devName.replace(".", "_")+nameSuffix);
				fed.setDeviceName(devName);
				// put into simulation mode if java properties set
				if (simulated) {
					fed.setDummy(true);
				}
				fed.configure();
				findables.add(fed);
			}
		} catch (Exception e) {
			final String msg = String.format("Could not configure %s \"%s\"", getClass().getSimpleName(), getName());
			throw new IllegalArgumentException(msg, e);
		}
		super.configure();
	}

	@Override
	public void addFindable(Findable findable) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public Findable getFindable(String name) {
		for (Findable f : findables) {
			if (f.getName().equals(name))
				return f;
		}
		return null;
	}

	@Override
	public ArrayList<Findable> getFindables() {
		return findables;
	}

	@Override
	public List<String> getFindableNames() {
		List<String> findableNames = new Vector<String>();
		for (Findable findable : findables) {
			findableNames.add(findable.getName());
		}
		return findableNames;
	}

	private String name = "EpicsDeviceFactory";

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private String nameSuffix="";
	public void setNameSuffix(String nameSuffix){
		this.nameSuffix = nameSuffix;
	}
	
	@Override
	public boolean containsExportableObjects() {
		// All objects in an EpicsDeviceFactory should be exported
		return true;
	}

	@Override
	public boolean isLocal() {
		return true;
	}

}
