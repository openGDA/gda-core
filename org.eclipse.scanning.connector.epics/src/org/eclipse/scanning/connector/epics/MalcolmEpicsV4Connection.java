/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.connector.epics;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnection;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMessageGenerator;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientChannelStateChangeRequester;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientMonitorRequester;
import org.epics.pvaClient.PvaClientPut;
import org.epics.pvaClient.PvaClientPutData;
import org.epics.pvaClient.PvaClientRPC;
import org.epics.pvaClient.PvaClientUnlistenRequester;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses EpicsV4 class to connect to an Epics V4 endpoint.
 * It provides the ability to get a pv, set a pv, call a method, and subsribe and unsubscribe to a pv.
 *
 * @author Matt Taylor
 *
 */
public class MalcolmEpicsV4Connection implements IMalcolmConnection {

	private static final String ERROR_MESSAGE_PATTERN_FAILED_TO_CONNECT =  ERROR_MESSAGE_PREFIX_FAILED_TO_CONNECT + " ''{0}'' ({1}: {2})";

	private static final double REQUEST_TIMEOUT = 1.0;

	private static final double NO_TIMEOUT = 0.0;

	private static final Logger logger = LoggerFactory.getLogger(MalcolmEpicsV4Connection.class);

	private final MalcolmMessageGenerator messageGenerator = new MalcolmMessageGenerator();

	private EpicsV4MessageMapper mapper;

	private PvaClient pvaClient;

    private Map<Long, Collection<EpicsV4MonitorListener>> listeners;

    public MalcolmEpicsV4Connection() {
		mapper = new EpicsV4MessageMapper();
		this.listeners = new ConcurrentHashMap<>();
		pvaClient = PvaClient.get("pva");
	}

	@Override
	public IMalcolmMessageGenerator getMessageGenerator() {
		return messageGenerator;
	}

	@Override
	public void disconnect() {
		//pvaClient.destroy(); // Commented out as we never need to disconnect
	}

	public PVStructure pvMarshal(Object anyObject) throws Exception {
		return mapper.pvMarshal(anyObject);
	}

	public <U> U pvUnmarshal(PVStructure anyObject, Class<U> beanClass) throws Exception {
		return mapper.pvUnmarshal(anyObject, beanClass);
	}

	@Override
	public MalcolmMessage send(IMalcolmDevice<?> device, MalcolmMessage message) throws MalcolmDeviceException {
		try {
			switch (message.getType()) {
			case CALL:
				return sendCallMessage(device, message);
			case GET:
				return sendGetMessage(device, message);
			case PUT:
				return sendPutMessage(device, message);
			default:
				throw new IllegalArgumentException("Unexpected MalcolmMessage type: " + message.getType());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			final MalcolmMessage errorMalcolmMessage = new MalcolmMessage();
			errorMalcolmMessage.setEndpoint(message.getEndpoint());
			errorMalcolmMessage.setId(message.getId());
			errorMalcolmMessage.setMessage("Error sending message " + message.getEndpoint() + ": " + e.getMessage());
			errorMalcolmMessage.setType(Type.ERROR);
			return errorMalcolmMessage;
		}
	}

	@Override
	public void subscribe(IMalcolmDevice<?> device, MalcolmMessage subscribeMessage, IMalcolmConnectionEventListener listener)
			throws MalcolmDeviceException {

		try {
			EpicsV4ClientMonitorRequester monitorRequester = new EpicsV4ClientMonitorRequester(listener, subscribeMessage);
			PvaClientChannel pvaChannel = createAndCheckChannel(device, REQUEST_TIMEOUT);

			PvaClientMonitor monitor = pvaChannel.monitor(subscribeMessage.getEndpoint(), monitorRequester, monitorRequester);

			Collection<EpicsV4MonitorListener> ls = listeners.get(subscribeMessage.getId());
			if (ls == null) {
				ls = Collections.synchronizedList(new ArrayList<>());
				listeners.put(subscribeMessage.getId(), ls);
			}

			EpicsV4MonitorListener monitorListener = new EpicsV4MonitorListener(listener, monitor);
			ls.add(monitorListener);
		} catch (Exception e) {
			logger.error("Could not subscribe to endpoint " + subscribeMessage.getEndpoint(), e);
			throw new MalcolmDeviceException(device, e.getMessage());
		}
	}

	@Override
	public void subscribeToConnectionStateChange(IMalcolmDevice<?> device, IMalcolmConnectionStateListener listener)
			throws MalcolmDeviceException {

		try {
			PvaClientChannel pvaChannel = createAndCheckChannel(device, 0);
			pvaChannel.setStateChangeRequester(new StateChangeRequester(listener));

		} catch (Exception e) {
			logger.error("Could not subscribe to connection state changes", e);
			throw new MalcolmDeviceException(device, e.getMessage());
		}
	}

	private PvaClientChannel createAndCheckChannel(IMalcolmDevice<?> device, double timeout) throws MalcolmDeviceException {
		PvaClientChannel pvaChannel = pvaClient.createChannel(device.getName(), "pva");
		pvaChannel.issueConnect();
		Status status = pvaChannel.waitConnect(timeout);
		if (!status.isOK()) {
			String errorMessage = MessageFormat.format(ERROR_MESSAGE_PATTERN_FAILED_TO_CONNECT, device.getName(), status.getType(), status.getMessage());
			logger.error(errorMessage);
			throw new MalcolmDeviceException(device, errorMessage);
		}
		return pvaChannel;
	}

	@Override
	public MalcolmMessage unsubscribe(IMalcolmDevice<?> device, MalcolmMessage msg, IMalcolmConnectionEventListener... removeListeners)
			throws MalcolmDeviceException {

		MalcolmMessage result = new MalcolmMessage();
		result.setType(Type.RETURN);
		result.setId(msg.getId());

		try {
			if (removeListeners==null) { // Kill every subscriber

				for (EpicsV4MonitorListener monitorListener : listeners.get(msg.getId())) {
					monitorListener.getMonitor().stop();
				}
				listeners.remove(msg.getId());
			} else {
				Collection<EpicsV4MonitorListener> ls = listeners.get(msg.getId());
				if (ls!=null) {
					ArrayList<EpicsV4MonitorListener> toRemove = new ArrayList<>();
					for (EpicsV4MonitorListener monitorListener : ls) {
						if (Arrays.asList(removeListeners).contains(monitorListener.getMalcolmListener())) {
							toRemove.add(monitorListener);
							monitorListener.getMonitor().stop();
						}
					}

					ls.removeAll(toRemove);
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMessage("Error unsubscribing from message Id " + msg.getId() + ": " + e.getMessage());
			result.setType(Type.ERROR);
			throw new MalcolmDeviceException(device, result.getMessage());
		}
		return result;
	}

	protected MalcolmMessage sendGetMessage(IMalcolmDevice<?> device, MalcolmMessage message) {

		MalcolmMessage returnMessage = new MalcolmMessage();
		PvaClientChannel pvaChannel = null;
		try {
			PVStructure pvResult = null;
			pvaChannel = createAndCheckChannel(device, REQUEST_TIMEOUT);

			String requestString = message.getEndpoint();
			logger.debug("Get '{}'", requestString);
			PvaClientGet pvaGet = pvaChannel.createGet(requestString);
			pvaGet.issueConnect();
			Status status = pvaGet.waitConnect();
			if (!status.isOK()) {
				String errMessage = "CreateGet failed for '" + requestString + "' (" + status.getType() + ": "
						+ status.getMessage() + ")";
				throw new Exception(errMessage);
			}
			PvaClientGetData pvaData = pvaGet.getData();
			pvResult = pvaData.getPVStructure();
			logger.debug("Get response = \n{}\nEND", pvResult);
			returnMessage = mapper.convertGetPVStructureToMalcolmMessage(pvResult, message);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage(e.getMessage());
		}

		if (pvaChannel != null) {
			pvaChannel.destroy();
		}

		return returnMessage;
	}

	private MalcolmMessage sendPutMessage(IMalcolmDevice<?> device, MalcolmMessage message) {
		MalcolmMessage returnMessage = new MalcolmMessage();
		returnMessage.setType(Type.RETURN);
		returnMessage.setId(message.getId());

		if (message.getValue() == null) {
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage("Unable to set field value to null: " + message.getEndpoint());
		}

		PvaClientChannel pvaChannel = null;

		try {
			String requestString = message.getEndpoint();

			pvaChannel = createAndCheckChannel(device, REQUEST_TIMEOUT);
			PvaClientPut pvaPut = pvaChannel.createPut(requestString);
			pvaPut.issueConnect();
			Status status = pvaPut.waitConnect();
			if (!status.isOK()) {
				String errMessage = "CreatePut failed for '" + requestString + "' (" + status.getType() + ": " + status.getMessage() + ")";
				throw new MalcolmDeviceException(device, errMessage);
			}
			PvaClientPutData putData = pvaPut.getData();
			PVStructure pvStructure = putData.getPVStructure();

			mapper.populatePutPVStructure(pvStructure, message);

			pvaPut.put();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage("Error putting value into field " + message.getEndpoint() + ": " + e.getMessage());
		}

		if (pvaChannel != null) {
			pvaChannel.destroy();
		}

		return returnMessage;
	}

	private MalcolmMessage sendCallMessage(IMalcolmDevice<?> device, MalcolmMessage message) {

		MalcolmMessage returnMessage = new MalcolmMessage();
		PvaClientChannel pvaChannel = null;

		try {
			PVStructure pvResult = null;
			PVStructure pvRequest = mapper.convertMalcolmMessageToPVStructure(message);

			// Mapper outputs two nested structures, one for the method, one for the parameters
			PVStructure methodStructure = pvRequest.getStructureField("method");
			PVStructure parametersStructure = pvRequest.getStructureField("parameters");

			pvaChannel = createAndCheckChannel(device, REQUEST_TIMEOUT);

			logger.debug("Call method = \n{}\nEND", methodStructure);
			PvaClientRPC rpc = pvaChannel.createRPC(methodStructure);
			rpc.issueConnect();
			Status status = rpc.waitConnect();

			if (!status.isOK()) {
				String errMessage = "CreateRPC failed for '" + message.getMethod() + "' (" + status.getType() + ": "
						+ status.getMessage() + ")";
				throw new MalcolmDeviceException(errMessage);
			}
			logger.debug("Call param = \n{}\nEND", parametersStructure);
			pvResult = rpc.request(parametersStructure);
			logger.debug("Call response = \n{}\nEND", pvResult);
			returnMessage = mapper.convertCallPVStructureToMalcolmMessage(pvResult, message);
		} catch (Exception e) {
			logger.error("Error sending call to {} to malcolm with argument {}",
					message.getMethod(), message.getArguments(), e);
			returnMessage.setType(Type.ERROR);
			returnMessage.setMessage(e.getMessage());
		}

		if (pvaChannel != null) {
			pvaChannel.destroy();
		}

		return returnMessage;
	}

	class EpicsV4ClientMonitorRequester implements PvaClientMonitorRequester, PvaClientUnlistenRequester {

		private final IMalcolmConnectionEventListener listener;
		private final MalcolmMessage subscribeMessage;

		public EpicsV4ClientMonitorRequester(IMalcolmConnectionEventListener listener, MalcolmMessage subscribeMessage) {
			this.listener = listener;
			this.subscribeMessage = subscribeMessage;
		}

		@Override
		public void event(PvaClientMonitor monitor) {
			while (monitor.poll()) {
				PvaClientMonitorData monitorData = monitor.getData();

				MalcolmMessage message = new MalcolmMessage();
				try {
					message = mapper.convertSubscribeUpdatePVStructureToMalcolmMessage(monitorData.getPVStructure(), subscribeMessage);
				} catch (Exception ex) {
					logger.error(ex.getMessage());
					message.setType(Type.ERROR);
					message.setMessage("Error converting subscription update: " + ex.getMessage());
				}
				listener.eventPerformed(message);
				monitor.releaseEvent();
			}
		}

		@Override
		public void unlisten(PvaClientMonitor arg0) {
			// TODO What to do when unlisten is called?
		}
	}

	private static class StateChangeRequester implements PvaClientChannelStateChangeRequester {
		private final IMalcolmConnectionStateListener listener;

		public StateChangeRequester(IMalcolmConnectionStateListener listener) {
			this.listener = listener;
		}

		@Override
		public void channelStateChange(PvaClientChannel channel, boolean isConnected) {
			listener.connectionStateChanged(isConnected);
		}
	}

}
