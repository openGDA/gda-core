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

import gda.device.Detector;
import gda.device.Device;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.exceptionUtils;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.CTRL;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Enum;
import gov.aps.jca.dbr.GR;
import gov.aps.jca.dbr.STS;
import gov.aps.jca.dbr.TIME;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyString;
import org.python.expose.ExposedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Control devices using the Epics Valve/Shutter template.
 * <P>
 * This class operates two Epics records: a record which controls the device and a record which holds the status. The
 * positions are: "Open", "Close" and "Reset". There are 5 values for the status: "Open", "Opening", "Closed", "Closing"
 * and "Fault".
 * <P>
 * The stop method in this class does nothing as the valves operate too fast for such a method to be meaningful.
 */
@ExposedType(name="epicsdevice")
public class EpicsDevice extends DeviceBase implements IEpicsDevice, IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsDevice.class);
	
	private EpicsController controller;

	/*
	 * Valid getAttribute strings: [CTRL:|NATIVE:]REC:<record>:[FLD:<field>] Valid setAttribute strings:
	 * [Add|DEL:CTRL|NATIVE:]REC:<record>[FLD:<field>]
	 */
	private static final String Timeout = "Timeout:", Fld = "Fld:", Rec = "Rec:", DoNotCorbarise = "DoNotCorbarise:",
			CTRL = "DBR_CTRL:", NATIVE = "DBR_NATIVE:", STS = "DBR_STS:", TIME = "DBR_TIME:", GR = "DBR_GR:",
			Delimiter = "://:", Add = "Add:", Del = "Del:", Close = "Close:", CloseAll = "CloseAll:";

	private static HashMap<String, ReturnType> returnTypeMap = new HashMap<String, ReturnType>();

	final private boolean InDummyMode;
	static {
		returnTypeMap.put(CTRL, ReturnType.DBR_CTRL);
		returnTypeMap.put(NATIVE, ReturnType.DBR_NATIVE);
		returnTypeMap.put(STS, ReturnType.DBR_STS);
		returnTypeMap.put(GR, ReturnType.DBR_GR);
		returnTypeMap.put(TIME, ReturnType.DBR_TIME);
	}
	private final HashMap<String, String> recordPVs;
	private final Map.Entry<String, String> firstRecordPV;
	private HashMap<String, Channel> channels = new HashMap<String, Channel>();
	private final HashMap<EpicsRegistrationRequest, Registration> registrations = new HashMap<EpicsRegistrationRequest, Registration>();
	private final HashMap<EpicsRegistrationRequest, DummyRegistration> dummyRegistrations = new HashMap<EpicsRegistrationRequest, DummyRegistration>();
	private HashMap<String, DummyChannel> dummyChannels = new HashMap<String, DummyChannel>();
	private HashMap<String, Object> fields = new HashMap<String, Object>();

	
	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * @return docString 
	 */
	public String getDocString() {
		return docString;
	}

	/**
	 * @param docString
	 */
	public void setDocString(String docString) {
		this.docString = docString;
	}

	private String docString;
	private enum SetMode {
		/**
		 * 
		 */
		VALUE, /**
		 * 
		 */
		ADDOBSERVER, /**
		 * 
		 */
		DELETEOBSERVER, /**
		 * 
		 */
		CLOSECHANNEL
	}

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param recordPVs
	 * @param InDummyMode
	 * @throws DeviceException
	 */
	public EpicsDevice(String name, HashMap<String, String> recordPVs, boolean InDummyMode) throws DeviceException {
		this(recordPVs, InDummyMode);
		setName(name);
	}

	/**
	 * @param recordPVs
	 * @param InDummyMode
	 * @throws DeviceException
	 */
	public EpicsDevice(HashMap<String, String> recordPVs, boolean InDummyMode) throws DeviceException {
		this.recordPVs = recordPVs;
		if (this.recordPVs.size() == 1) {
			firstRecordPV = this.recordPVs.entrySet().iterator().next();
		} else {
			firstRecordPV = null;
		}
		this.InDummyMode = InDummyMode;
		if (!this.InDummyMode) {
			controller = EpicsController.getInstance();
		} else {
			controller = null;
		}
		setAttribute("Records", recordPVs);
		setAttribute("Configured", true);
		setAttribute("Dummy", InDummyMode);

		for (String s : recordPVs.keySet())
			fields.put(s, null);
	}

	/**
	 * @see gda.device.epicsdevice.IEpicsDevice#dispose()
	 */
	@Override
	public void dispose() throws DeviceException {
		/*
		 * stop all monitors from epics remove references to observers in the observable components close all epics
		 * channels
		 */
		if (InDummyMode) {
			return; // do not bother in dummy mode
		}
		{
			synchronized (registrations) {
				Iterator<Map.Entry<EpicsRegistrationRequest, Registration>> iter = registrations.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<EpicsRegistrationRequest, Registration> e = iter.next();
					controller.clearMonitor(e.getValue().monitor);
					e.getValue().obsComp.deleteIObservers();
					iter.remove();
				}
			}
		}
		closeUnUsedChannels();
	}

	private Channel getChannel(String pvName, Double timeout) throws CAException, TimeoutException {
		if (InDummyMode) {
			throw new IllegalArgumentException("getChannel called with controller == null. Try getDummyChannel");
		}
		Channel channel = null;
		if (!channels.containsKey(pvName)) {
			channel = timeout != null ? controller.createChannel(pvName, timeout) : controller.createChannel(pvName);
			{
				channels.put(pvName, channel);
			}
		} else {
			channel = channels.get(pvName);
		}
		return channel;
	}

	private DummyChannel getDummyChannel(String pvName) {
		if (!dummyChannels.containsKey(pvName)) {
			DummyChannel dummyChannel = new DummyChannel();
			dummyChannel.setValue("");
			dummyChannels.put(pvName, dummyChannel);
		}
		return dummyChannels.get(pvName);
	}

	private Object getNativeValue(Channel ch) throws DeviceException {
		try {
			Object value = null;
			DBRType fieldType = ch.getFieldType();
			int elementCount = ch.getElementCount();
			if (fieldType.isBYTE()) {
				if (elementCount == 1) {
					value = controller.cagetByte(ch);
				} else {
					value = controller.cagetByteArray(ch);
				}
			} else if (fieldType.isINT()) {
				if (elementCount == 1) {
					value = controller.cagetInt(ch);
				} else {
					value = controller.cagetIntArray(ch);
				}
			} else if (fieldType.isFLOAT()) {
				if (elementCount == 1) {
					value = controller.cagetFloat(ch);
				} else {
					value = controller.cagetFloatArray(ch);
				}
			} else if (fieldType.isDOUBLE()) {
				if (elementCount == 1) {
					value = controller.cagetDouble(ch);
				} else {
					value = controller.cagetDoubleArray(ch);
				}
			} else if (fieldType.isSHORT()) {
				if (elementCount == 1) {
					value = controller.cagetShort(ch);
				} else {
					value = controller.cagetShortArray(ch);
				}
			} else if (fieldType.isENUM()) {
				if (elementCount == 1) {
					value = controller.cagetEnum(ch);
				} else {
					value = controller.cagetEnumArray(ch);
				}
			} else {
				if (elementCount == 1) {
					value = controller.cagetString(ch);
				} else {
					value = controller.cagetStringArray(ch);
				}
			}
			return value;
		} catch (Exception e) {
			throw new DeviceException("getNativeValue exception ", e);
		}
	}

	private Object getValue(String pvName, ReturnType returnType) throws DeviceException {
		if (InDummyMode) {
			DummyChannel dummy = getDummyChannel(pvName);
			Object val = dummy.getValue();
			if (val == null) {
				logger.warn("Value of " + pvName + " is null");
			}
			return val;
		}

		try {
			Channel channel = getChannel(pvName, null);// use default timeout
			switch (returnType.getTrueReturnType()) {
			case NATIVE:
				return getNativeValue(channel);
			case CTRL:
				return controller.getCTRL(channel);
			case STS:
				return controller.getSTS(channel);
			case GR:
				return controller.getGR(channel);
			case TIME:
				return controller.getTIME(channel);
			default:
				throw new DeviceException("EpicsDevice.getValue - invalid return type - " + returnType);
			}
		} catch (CAStatusException e) {
			throw new DeviceException("EpicsDevice.getValue failed for " + pvName + ". " + e.getStatus().getMessage(),
					e);
		} catch (Exception e) {
			throw new DeviceException("EpicsDevice.getValue failed for " + pvName + "'.  ReturnType = "
					+ returnType.toString() + ". " + e.getMessage(), e);
		}
	}

	void setValueInNewThread(String pvName, Object value) {
		// logger.info(" observer thread setValueInNewThread - in " + pvName);
		try {
			Channel channel = getChannel(pvName, null); // use default timeout
			if (value instanceof Double) {
				controller.caput(channel, (Double) value);
			} else if (value instanceof Integer) {
				controller.caput(channel, (Integer) value);
			} else if (value instanceof Float) {
				controller.caput(channel, (Float) value);
			} else if (value instanceof Short) {
				controller.caput(channel, (Short) value);
			} else
				controller.caput(channel, value.toString());
		} catch (Exception e) {
			exceptionUtils.logException(logger, "EpicsDevice.setValueInNewThread " + pvName, e);
		} finally {
			// logger.info(" observer thread setValueInNewThread - out " +
			// pvName);
		}
	}

	private void setValue(final String pvName, final Object value, final double timeout) throws DeviceException {
		if (InDummyMode) {
			DummyChannel dummy = getDummyChannel(pvName);
			dummy.setValue(value);
			return;
		}
		try {
			if (timeout > 0.) {
				Channel channel = getChannel(pvName, timeout);
				if (value instanceof Double) {
					controller.caput(channel, (Double) value, timeout);
				} else if (value instanceof Integer) {
					controller.caput(channel, (Integer) value, timeout);
				} else if (value instanceof Float) {
					controller.caput(channel, (Float) value, timeout);
				} else if (value instanceof Short) {
					controller.caput(channel, (Short) value, timeout);
				} else if (value instanceof double[]) {
					controller.caput(channel, (double[]) value, timeout);
				} else if (value instanceof int[]) {
					controller.caput(channel, (int[]) value, timeout);
				} else if (value instanceof float[]) {
					controller.caput(channel, (float[]) value, timeout);
				} else if (value instanceof short[]) {
					controller.caput(channel, (short[]) value, timeout);
				} else
					controller.caput(channel, value.toString(), timeout);
			} else {
				// Release current corba thread as creating a channel can be
				// blocking
				Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
					@Override
					public void run() {
						setValueInNewThread(pvName, value);
					}
				});
				t.setPriority(java.lang.Thread.MIN_PRIORITY);
				t.start();
			}

		} catch (CAStatusException e) {
			String msg = "EpicsDevice.setValue failed for " + pvName + " with value = " + value.toString() + " "
					+ e.getStatus().getMessage();
			throw new DeviceException(msg, e);
		} catch (Exception e) {
			String msg = "EpicsDevice.setValue failed for " + pvName + " with value = " + value.toString();
			throw new DeviceException(msg, e);
		}
	}

	public void setValue(String record, String field,  final Object value, final double timeout) throws DeviceException {
		String pvName = getRecord(record);
		if( field != null && !field.isEmpty()){
			pvName += field;
		}
		if (InDummyMode) {
			DummyChannel dummy = getDummyChannel(pvName);
			dummy.setValue(value);
			return;
		}
		try {
			if (timeout > 0.) {
				Channel channel = getChannel(pvName, timeout);
				if (value instanceof Double) {
					controller.caput(channel, (Double) value, timeout);
				} else if (value instanceof Integer) {
					controller.caput(channel, (Integer) value, timeout);
				} else if (value instanceof Float) {
					controller.caput(channel, (Float) value, timeout);
				} else if (value instanceof Short) {
					controller.caput(channel, (Short) value, timeout);
				} else if (value instanceof double[]) {
					controller.caput(channel, (double[]) value, timeout);
				} else if (value instanceof int[]) {
					controller.caput(channel, (int[]) value, timeout);
				} else if (value instanceof float[]) {
					controller.caput(channel, (float[]) value, timeout);
				} else if (value instanceof short[]) {
					controller.caput(channel, (short[]) value, timeout);
				} else
					controller.caput(channel, value.toString(), timeout);
			} else {
				// Release current corba thread as creating a channel can be
				// blocking
				final String pvNameFinal = pvName;
				Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
					@Override
					public void run() {
						setValueInNewThread(pvNameFinal, value);
					}
				});
				t.setPriority(java.lang.Thread.MIN_PRIORITY);
				t.start();
			}

		} catch (CAStatusException e) {
			String msg = "EpicsDevice.setValue failed for " + pvName + " with value = " + value.toString() + " "
					+ e.getStatus().getMessage();
			throw new DeviceException(msg, e);
		} catch (Exception e) {
			String msg = "EpicsDevice.setValue failed for " + pvName + " with value = " + value.toString();
			throw new DeviceException(msg, e);
		}
	}
	
	/**
	 * @see gda.device.DeviceBase#getAttribute(java.lang.String)
	 */
	@Override
	public synchronized Object getAttribute(String _attributeName) throws DeviceException {

		boolean toWrap = true;
		String attributeName = _attributeName;
		if (attributeName.startsWith(DoNotCorbarise)) {
			attributeName = attributeName.substring(DoNotCorbarise.length());
			toWrap = false;
		}

		ReturnType returnType = null;
		for (Map.Entry<String, ReturnType> returnTypeEntry : returnTypeMap.entrySet()) {
			if (attributeName.startsWith(returnTypeEntry.getKey())) {
				// remove this key
				attributeName = attributeName.substring(returnTypeEntry.getKey().length());
				returnType = returnTypeEntry.getValue();
				break;
			}
		}
		if (returnType != null) {
			String pvName = null;
			if (attributeName.startsWith(Rec)) {
				String subRemoved = attributeName.substring(Rec.length());
				// now extract the device name
				int delimiterIndex = subRemoved.indexOf(Delimiter);
				if (delimiterIndex < 0)
					throw new DeviceException("EpicsDevice.setAttribute: Unable to find device delimiter in "
							+ attributeName);
				String recName = subRemoved.substring(0, delimiterIndex);
				attributeName = subRemoved.substring(delimiterIndex + Delimiter.length());
				pvName = getRecord(recName);
			}
			if (pvName != null && attributeName.startsWith(Fld)) {
				pvName = pvName + attributeName.substring(Fld.length());
			}
			Object val = getValue(pvName, returnType);
			return toWrap ? WrapEpicsDBR(val) : val;
		}

		return super.getAttribute(attributeName);
	}

	void addObserver(EpicsRegistrationRequest request, IObserver observer) throws DeviceException,
			gov.aps.jca.CAException, gov.aps.jca.TimeoutException, InterruptedException {
		/*
		 * if channel is already being monitored just add to list of items to be updated Each channel can have an
		 * IObserve
		 */
		synchronized (registrations) {
			if (InDummyMode) {
				if (!dummyRegistrations.containsKey(request)) {
					if (request.returnType == ReturnType.DBR_GR || request.returnType == ReturnType.DBR_STS
							|| request.returnType == ReturnType.DBR_TIME)
						throw new DeviceException("EpicsDevice.addObserver - invalid ReturnType");
					ObservableComponent obsComp = new ObservableComponent();
					obsComp.addIObserver(observer);
					DummyMonitorListener listener = new DummyMonitorListener(request, this);
					DummyChannel dummy = getDummyChannel(request.pvName);
					dummy.addListener(listener);
					dummyRegistrations.put(request, new DummyRegistration(listener, obsComp));
				} else {
					DummyRegistration registration = dummyRegistrations.get(request);
					registration.obsComp.addIObserver(observer);
					registration.listener.sendLastEvent();
				}
				return;
			}
			if (!registrations.containsKey(request)) {
				if (request.returnType == ReturnType.DBR_GR || request.returnType == ReturnType.DBR_STS
						|| request.returnType == ReturnType.DBR_TIME)
					throw new DeviceException("EpicsDevice.addObserver - invalid ReturnType");
				Channel channel = getChannel(request.pvName.isEmpty() ? (getRecord(request.record) + request.field)
						: request.pvName, null); // use default
				// timeout -
				// this is
				// already in a
				// new thread
				ObservableComponent obsComp = new ObservableComponent();
				MonitorListenerImpl listener = new MonitorListenerImpl(request, this);
				obsComp.addIObserver(observer);
				gov.aps.jca.Monitor mon = controller.setMonitor(channel, listener, request.returnType
						.getTrueReturnType());
				registrations.put(request, new Registration(listener, obsComp, mon));
			} else {
				Registration registration = registrations.get(request);
				registration.obsComp.addIObserver(observer);
				registration.listener.sendLastEvent();
			}
		}
	}

	/**
	 * @see gda.device.DeviceBase#notifyIObservers(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void notifyIObservers(Object theObserved, Object theArgument) {
		if (theArgument instanceof EpicsDeviceEvent && !((EpicsDeviceEvent) theArgument).request.toWrap) {
			synchronized (registrations) {
				EpicsRegistrationRequest requestWithPVName = ((EpicsDeviceEvent) theArgument).request;
				EpicsRegistrationRequest request = requestWithPVName.removePVName();
				Registration reg = registrations.get(request);
				if (reg != null)
					reg.obsComp.notifyIObservers(request, ((EpicsDeviceEvent) theArgument).event);
			}
		} else
			super.notifyIObservers(theObserved, theArgument);
	}

	void deleteObserver(EpicsRegistrationRequest request, IObserver observer) {
		/*
		 * if channel is already being monitored just add to list of items to be updated Each channel can have an
		 * IObserve
		 */
		synchronized (registrations) {
			if (InDummyMode) {
				if (dummyRegistrations.containsKey(request)) {
					String pvName = request.pvName;
					ObservableComponent obsComp = dummyRegistrations.get(request).obsComp;
					obsComp.deleteIObserver(observer);
					if (!obsComp.IsBeingObserved()) {
						DummyChannel channel = getDummyChannel(pvName);
						channel.removeListener(dummyRegistrations.get(request).listener);
						dummyRegistrations.remove(request);
					}
				}
				return;
			}
			if (registrations.containsKey(request)) {
				String pvName = request.pvName;
				ObservableComponent obsComp = registrations.get(request).obsComp;
				obsComp.deleteIObserver(observer);
				if (!obsComp.IsBeingObserved()) {

					controller.clearMonitor(registrations.get(request).monitor);
					registrations.remove(request);
					if (canCloseChannel(pvName)) {
						controller.destroy(channels.get(pvName));
						channels.remove(pvName);
					}
				}
			}
		}
	}

	private boolean canCloseChannel(String pvName) {
		/*
		 * for each PV in the channel list if not being observed then remove
		 */
		synchronized (registrations) {
			if (channels.containsKey(pvName)) {
				boolean found = false;
				for (Map.Entry<EpicsRegistrationRequest, Registration> entry : registrations.entrySet()) {
					if (pvName.equals(entry.getKey().pvName)) {
						found = true;
						break;
					}
				}
				return !found;
			}
			return false;
		}
	}

	@Override
	public void closeUnUsedChannels() throws DeviceException {
		/*
		 * for each PV in the channel list if not being observed then remove
		 */
		if (InDummyMode) {
			return; // in dummy mode do not bother with this
		}
		Iterator<Map.Entry<String, Channel>> iter = channels.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Channel> e = iter.next();
			if (canCloseChannel(e.getKey())) {
				controller.destroy(channels.get(e.getKey()));
				iter.remove();
			}
		}
	}

	private String getRecord(String recName) throws DeviceException {
		String pvName = null;
		if (recName.isEmpty()) {
			if (firstRecordPV != null) {
				pvName = firstRecordPV.getValue();
			} else {
				throw new DeviceException("EpicsDevice.getRecord: Record name " + StringUtils.quote(recName) + " is ambiguous in EpicsDevice device " + StringUtils.quote(getName()));
			}
		} else if (recordPVs.containsKey(recName)) {
			pvName = recordPVs.get(recName);
		}
		if (pvName == null) {
			throw new DeviceException("EpicsDevice.getRecord: Unable to find record " + recName + " in device "
					+ getName());
		}
		return pvName;
	}

	/**
	 * @see gda.device.DeviceBase#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	// synchronized until this is seen to cause a bottleneck
	synchronized public void setAttribute(String _attributeName, Object value) throws DeviceException {
		String record = null;
		String field = null;
		String attributeName = _attributeName;
		if (attributeName.equals(CloseAll)) {
			closeUnUsedChannels();
			return;
		}

		boolean toWrap = true;
		if (attributeName.startsWith(DoNotCorbarise)) {
			attributeName = attributeName.substring(DoNotCorbarise.length());
			toWrap = false;
		}
		SetMode setMode = SetMode.VALUE;
		ReturnType returnType = null;
		if (attributeName.startsWith(Add)) {
			attributeName = attributeName.substring(Add.length());
			setMode = SetMode.ADDOBSERVER;
		} else if (attributeName.startsWith(Del)) {
			attributeName = attributeName.substring(Del.length());
			setMode = SetMode.DELETEOBSERVER;
		} else if (attributeName.startsWith(Close)) {
			attributeName = attributeName.substring(Close.length());
			setMode = SetMode.CLOSECHANNEL;
		}
		if (setMode == SetMode.ADDOBSERVER || setMode == SetMode.DELETEOBSERVER) {
			for (Map.Entry<String, ReturnType> returnTypeEntry : returnTypeMap.entrySet()) {
				if (attributeName.startsWith(returnTypeEntry.getKey())) {
					attributeName = attributeName.substring(returnTypeEntry.getKey().length());
					returnType = returnTypeEntry.getValue();
					break;
				}
			}
		}
		String pvName = null;
		double putTimeout = 30.0;
		if (attributeName.startsWith(Rec)) {
			String subRemoved = attributeName.substring(Rec.length());
			// now extract the device name
			int delimiterIndex = subRemoved.indexOf(Delimiter);
			if (delimiterIndex < 0)
				throw new DeviceException("EpicsDevice.setAttribute: Unable to find device delimiter in "
						+ attributeName);
			record = subRemoved.substring(0, delimiterIndex);
			attributeName = subRemoved.substring(delimiterIndex + Delimiter.length());
			pvName = getRecord(record);
		}

		if (pvName != null && attributeName.startsWith(Fld)) {
			String subRemoved = attributeName.substring(Fld.length());
			int delimiterIndex = subRemoved.indexOf(Delimiter);
			if (delimiterIndex < 0)
				throw new DeviceException("EpicsDevice.setAttribute: Unable to find device delimiter in "
						+ attributeName);
			field = subRemoved.substring(0, delimiterIndex);
			attributeName = subRemoved.substring(delimiterIndex + Delimiter.length());
			pvName = pvName + field;
		}
		if (attributeName.startsWith(Timeout)) {
			putTimeout = Double.valueOf(attributeName.substring(Timeout.length()));
		}
		if (pvName == null) {
			super.setAttribute(attributeName, value);
		} else {
			if (setMode != SetMode.VALUE) {
				if (setMode == SetMode.CLOSECHANNEL) {
					if (InDummyMode) {
						return; // do not bother in dummy mode
					}
					if (canCloseChannel(pvName)) {
						controller.destroy(channels.get(pvName));
						channels.remove(pvName);
					}
					return;
				}
				if (setMode == SetMode.ADDOBSERVER || setMode == SetMode.DELETEOBSERVER) {
					final EpicsRegistrationRequest request = new EpicsRegistrationRequest(returnType, record, field,
							pvName, putTimeout, toWrap);
					final SetMode fsetMode = setMode;
					final Object fvalue = value;
					final EpicsDevice fepicsDevice = this;
					final String fpvName = pvName;
					// Release current corba thread as creating a channel can be
					// blocking
					Thread t = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
						@Override
						public void run() {
							try {
								// try{
								switch (fsetMode) {
								case VALUE:
								case CLOSECHANNEL:
								case ADDOBSERVER:
									addObserver(request, (fvalue == null) ? fepicsDevice : (IObserver) fvalue);
									break;
								case DELETEOBSERVER:
									deleteObserver(request, (fvalue == null) ? fepicsDevice : (IObserver) fvalue);
									break;
								}
								// }
								// catch (CAException e){
								// throw new DeviceException("AddObserver", e);
								// }
								// catch (TimeoutException e){
								// throw new DeviceException("AddObserver", e);
								// }
							} catch (Exception e) {
								exceptionUtils.logException(logger, "EpicsDevice.setAttribute:addObserver fails "
										+ fpvName, e);
							}
						}
					});
					t.setPriority(java.lang.Thread.MIN_PRIORITY);
					t.start();
				}
			} else {
				setValue(pvName, value, putTimeout);
			}
		}
	}

	static Object WrapEpicsDBR(Object objectToWrap) {

		if (objectToWrap instanceof DBR_CTRL_Enum) {
			return new EpicsCtrlEnum((DBR_CTRL_Enum) objectToWrap);
		} else if (objectToWrap instanceof CTRL) {
			return new EpicsCTRL((CTRL) objectToWrap);
		} else if (objectToWrap instanceof GR) {
			return new EpicsGR((GR) objectToWrap);
		} else if (objectToWrap instanceof TIME) {
			return new EpicsTime((TIME) objectToWrap);
		} else if (objectToWrap instanceof STS) {
			return new EpicsSTS((STS) objectToWrap);
		} else if (objectToWrap instanceof DBR) {
			return new EpicsDBR((DBR) objectToWrap);
		}
		return objectToWrap;
	}

	/**
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		notifyIObservers(theObserved, changeCode);
	}

	static void getRegistration(EpicsRegistrationRequest request, Device dev) throws DeviceException {
		String attributeName = Add + (request.returnType == ReturnType.DBR_CTRL ? CTRL : NATIVE) + Rec + request.record
				+ Delimiter + Fld + request.field + Delimiter;
		dev.setAttribute(attributeName, null);
	}

	static void removeRegistration(EpicsRegistrationRequest request, Device dev) throws DeviceException {
		String attributeName = Del + (request.returnType == ReturnType.DBR_CTRL ? CTRL : NATIVE) + Rec + request.record
				+ Delimiter + Fld + request.field + Delimiter;
		dev.setAttribute(attributeName, null);
	}

	static Object getValueDev(ReturnType returnType, String record, String field, Device dev) throws DeviceException {
		String attributeName = (returnType == ReturnType.DBR_CTRL ? CTRL
				: (returnType == ReturnType.DBR_NATIVE ? NATIVE
						: (returnType == ReturnType.DBR_GR ? GR : (returnType == ReturnType.DBR_STS ? STS
								: (returnType == ReturnType.DBR_TIME ? TIME : NATIVE)))))
				+ Rec + record + Delimiter + Fld + field;
		return dev.getAttribute(attributeName);
	}

	static void setValueDev(String record, String field, Object value, double putTimeOutInSec, Device dev)
			throws DeviceException {
		String attributeName = Rec + record + Delimiter + Fld + field + Delimiter + Timeout
				+ Double.valueOf(putTimeOutInSec).toString();
		dev.setAttribute(attributeName, value);
	}

	public Object getValue(ReturnType returnType, String record, String field) throws DeviceException {
		String attributeName = DoNotCorbarise + (returnType == ReturnType.DBR_CTRL ? CTRL : NATIVE) + Rec + record
				+ Delimiter + Fld + field;
		return getAttribute(attributeName);
	}

	/**
	 * @param record
	 * @param field
	 * @return String
	 * @throws DeviceException
	 */
	public String getValueAsString(String record, String field) throws DeviceException {
		Object value = getValue(ReturnType.DBR_CTRL, record, field);
		String valStr = null;
		if (value instanceof DBR_CTRL_Enum) {
			valStr = ((DBR_CTRL_Enum) value).getLabels()[((DBR_CTRL_Enum) value).getEnumValue()[0]];
		} else if (value instanceof DBR) {
			EpicsDBR epicsDbr = new EpicsDBR((DBR) value);
			if (epicsDbr._value instanceof byte[]) {
				// convert to ASCII string
				byte[] data = (byte[]) epicsDbr._value;
				int length = 0;
				for (; length < epicsDbr._count; length++) {
					if (data[length] == 0)
						break;
				}
				valStr = new String((byte[]) epicsDbr._value, 0, length);
			} else {
				valStr = epicsDbr._toString();
			}
		} else {
			valStr = value.toString();
		}
		return valStr;
	}

	/**
	 * @param record
	 * @param field
	 * @return Element Count
	 * @throws DeviceException
	 */
	public int getElementCount(String record, String field) throws DeviceException {
		String pvName = getRecord(record) + field;
		try {
			Channel channel = getChannel(pvName, null);// use default timeout
			return channel.getElementCount();
		} catch (IllegalStateException e) {
			throw new DeviceException("EpicsDevice.getElementCount failed for " + pvName + ". " + e.getMessage(), e);
		} catch (CAException e) {
			throw new DeviceException("EpicsDevice.getElementCount failed for " + pvName + ". " + e.getMessage(), e);
		} catch (TimeoutException e) {
			throw new DeviceException("EpicsDevice.getElementCount failed for " + pvName + ". " + e.getMessage(), e);
		}
	}

	void setValue(Object type, String record, String field, Object val, double putTimeOutInSec) throws DeviceException {
		// for now only allow null for type. For now value.toString value is
		// used in call to caput
		if (type != null)
			throw new DeviceException("EpicsDevice.setValue - error, Only null is allowed for type value");
		setValueDev(record, field, val, putTimeOutInSec, this);
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field, double putTimeOutInSec) {
		return new EpicsRecord(this, null, returnType, record, field, putTimeOutInSec);
	}

	/**
	 * @param name
	 * @param returnType
	 * @param record
	 * @param field
	 * @param putTimeOutInSec
	 * @return IEpicsChannel
	 */
	public IEpicsChannel createEpicsChannel(String name, ReturnType returnType, String record, String field,
			double putTimeOutInSec) {
		return new EpicsRecord(this, name, returnType, record, field, putTimeOutInSec);
	}

	/**
	 * @param record
	 * @param field
	 * @return Detector
	 */
	public Detector createDetector(String record, String field) {
		return new EpicsDetector(this, null, record, field, defPutTimeOutInSec);
	}

	/**
	 * @param name
	 * @param record
	 * @param field
	 * @return Detector
	 */
	public Detector createDetector(String name, String record, String field) {
		return new EpicsDetector(this, name, record, field, defPutTimeOutInSec);
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field) {
		return new EpicsRecord(this, null, returnType, record, field, defPutTimeOutInSec);
	}

	/**
	 * @param name
	 * @param returnType
	 * @param record
	 * @param field
	 * @return IEpicsChannel
	 */
	public IEpicsChannel createEpicsChannel(String name, ReturnType returnType, String record, String field) {
		return new EpicsRecord(this, name, returnType, record, field, defPutTimeOutInSec);
	}

	static void closeUnUsedChannelsDev(Device dev) throws DeviceException {
		dev.setAttribute(CloseAll, null);
	}

	/**
	 * @param deviceName
	 * @param record
	 * @param field
	 * @return double value
	 * @throws DeviceException
	 */
	public static Double getValue(String deviceName, String record, String field) throws DeviceException {
		Object obj = Finder.getInstance().find(deviceName);
		if (obj == null || !(obj instanceof EpicsDevice))
			throw new DeviceException("EpicsDevice.getValue. unable to find EpicsDevice " + deviceName);
		EpicsDevice experimentEpicsDevice = (EpicsDevice) obj;
		obj = experimentEpicsDevice.getValue(ReturnType.DBR_NATIVE, record, field);
		if (!(obj instanceof Double))
			throw new DeviceException("EpicsDevice.getValue. value returned from  getValue is of type "
					+ obj.getClass().getName());
		return (Double) obj;
	}
	
	

	
	
	/**
	 * See __findattr__(String name)
	 * @param name
	 * @return the
	 */
	public Object __getattr__(PyString name) {
		return __getattr__(name.internedString());
	}

	/**
	 * The Python interpreter calls this to find an attribute before it looks for a real attribute on an object. Here
	 * it returns the an EpicsDevice field (or docstring).
	 * @param name
	 * @return EpicsDevice field (or docstring).
	 */
	public Object __getattr__(String name) {
		if( name.equals("__doc__") && docString != null)
			return docString;

		if (fields.containsKey(name)) {
			Object field = fields.get(name);
			if (field == null) {
				int elemCount = 1;
				if( !isInDummyMode()){
					try {
						elemCount = getElementCount(name, "");
					} catch (Exception e) {
						exceptionUtils.logException(logger, "EpicsDevice.__findattr__ " + name, e);
					}
				}
				if (elemCount > 1)
					field = createDetector(getName() + "_" + name, name, "");
				else
					field = createEpicsChannel(getName() + "_" + name, ReturnType.DBR_NATIVE, name, "");
				fields.put(name, field);
			}
			return field;
		}
		throw Py.AttributeError(name);
	}

	/**
	 * @return Returns the inDummyMode.
	 */
	public boolean isInDummyMode() {
		return InDummyMode;
	}

	public void setValue(String record, String field, Object value, double connection_timeout, PutListener listener) throws DeviceException {
		String pvName = getRecord(record);
		if( field != null && !field.isEmpty()){
			pvName += field;
		}
		if (InDummyMode) {
			DummyChannel dummy = getDummyChannel(pvName);
			dummy.setValue(value);
			listener.putCompleted(null);
			return;
		}
		try {
			Channel channel = getChannel(pvName, connection_timeout);
			if (value instanceof Double) {
				controller.caput(channel, (Double) value, listener);
			} else if (value instanceof Integer) {
				controller.caput(channel, (Integer) value, listener);
			} else if (value instanceof Float) {
				controller.caput(channel, (Float) value, listener);
			} else if (value instanceof Short) {
				controller.caput(channel, (Short) value, listener);
			} else if (value instanceof double[]) {
				controller.caput(channel, (double[]) value, listener);
			} else if (value instanceof int[]) {
				controller.caput(channel, (int[]) value, listener);
			} else if (value instanceof float[]) {
				controller.caput(channel, (float[]) value, listener);
			} else if (value instanceof short[]) {
				controller.caput(channel, (short[]) value, listener);
			} else
				controller.caput(channel, value.toString(), listener);
		} catch (CAStatusException e) {
			String msg = "EpicsDevice.setValue failed for " + pvName + " with value = " + value.toString() + " "
					+ e.getStatus().getMessage();
			throw new DeviceException(msg, e);
		} catch (Exception e) {
			String msg = "EpicsDevice.setValue failed for " + pvName + " with value = " + value.toString();
			throw new DeviceException(msg, e);
		}
		
	}
}

final class MonitorListenerImpl implements MonitorListener {
	final EpicsRegistrationRequest request;
	EpicsDeviceEvent lastEvent;
	final EpicsDevice epicsDevice;

	/**
	 * @param request
	 * @param epicsDevice
	 */
	public MonitorListenerImpl(EpicsRegistrationRequest request, EpicsDevice epicsDevice) {
		this.request = request;
		this.epicsDevice = epicsDevice;
	}

	/**
	 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
	 */
	@Override
	public void monitorChanged(MonitorEvent arg0) {
		// logger.debug("monitorChanged for " + request.pvName);
		EpicsDeviceEvent event = new EpicsDeviceEvent(request, new EpicsMonitorEvent(arg0, request.toWrap));
		lastEvent = event;
		epicsDevice.notifyIObservers(this, event);
	}

	/**
	 * 
	 */
	public void sendLastEvent() {
		// I need to use a separate thread otherwise this is part of the
		// addObserver thread from the client
		// It is run from within epicsDevice.addObserver which is synchronized
		uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				epicsDevice.notifyIObservers(this, lastEvent);
			}
		}).start();
	}
}

final class Registration {
	MonitorListenerImpl listener;
	ObservableComponent obsComp;
	gov.aps.jca.Monitor monitor;

	Registration(MonitorListenerImpl listener, ObservableComponent obsComp, gov.aps.jca.Monitor monitor) {
		this.listener = listener;
		this.obsComp = obsComp;
		this.monitor = monitor;
	}
}

final class DummyRegistration {
	DummyMonitorListener listener;
	ObservableComponent obsComp;

	DummyRegistration(DummyMonitorListener listener, ObservableComponent obsComp) {
		this.listener = listener;
		this.obsComp = obsComp;
	}
}

final class DummyChannel {
	private Object value;
	private ObservableComponent obsComp = new ObservableComponent();

	Object getValue() {
		return value;
	}

	void setValue(Object value) {
		this.value = value;
		if (obsComp != null) {
			Object data[] = { value };
			obsComp.notifyIObservers(this, new EpicsDBR(1, data));
		}
	}

	void addListener(DummyMonitorListener listener) {
		obsComp.addIObserver(listener);
	}

	void removeListener(DummyMonitorListener listener) {
		obsComp.deleteIObserver(listener);
	}
}

final class DummyMonitorListener implements IObserver {
	final EpicsRegistrationRequest request;
	private EpicsDeviceEvent lastEvent;
	final EpicsDevice epicsDevice;

	/**
	 * @param request
	 * @param epicsDevice
	 */
	public DummyMonitorListener(EpicsRegistrationRequest request, EpicsDevice epicsDevice) {
		this.request = request;
		this.epicsDevice = epicsDevice;
	}

	/**
	 * @see gda.observable.IObserver#update(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void update(Object theObserved, Object changeCode) {
		EpicsDeviceEvent event = new EpicsDeviceEvent(request, new EpicsMonitorEvent(changeCode));
		lastEvent = event;
		epicsDevice.notifyIObservers(this, event);

	}

	/**
	 * 
	 */
	public void sendLastEvent() {
		epicsDevice.notifyIObservers(this, lastEvent);
	}
}
