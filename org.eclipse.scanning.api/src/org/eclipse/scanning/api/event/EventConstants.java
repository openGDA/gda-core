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
package org.eclipse.scanning.api.event;


/**
 * A class with constants for the names of JMS queues and topics used by the
 * {@link IEventService} and its related classes.
 *
 * @author Matthew Gerring
 */
public class EventConstants {

	private EventConstants() {
		// private constructor to prevent instantiation
	}

    /**
     * A topic which may be used for scan events.
     * It is usually better to use your own topic rather than the default.
     */
	public static final String SCAN_TOPIC      = "org.eclipse.scanning.scan.topic";

    /**
     * The default topic used for status update events
     * It is usually better to use your own topic rather than the default.
     */
	public static final String STATUS_TOPIC      = "org.eclipse.scanning.status.topic";

	/**
	 * The default topic used for queue status events.
     * It is usually better to use your own topic rather than the default.
	 */
	public static final String QUEUE_STATUS_TOPIC = "org.eclipse.scanning.queue.status.topic";

	/**
	 * The default topic used for to send command to a job queue, e.g. pause/terminate
	 * or to reorder or remove beans from the queue. This topic is generally used internally.
	 */
	public static final String CMD_TOPIC = "org.eclipse.scanning.command.topic";

	/**
	 * The default topic used for acknowledgements from the job quue to commands sent on the
	 * command topic. This topic is generally used internally.
	 */
	public static final String ACK_TOPIC = "org.eclipse.scanning.ack.topic";

	/**
	 * The default queue used for submitting things (like ScanRequests) to a queue.
	 */
	public static final String SUBMISSION_QUEUE = "org.eclipse.scanning.submission.queue";

	/**
	 * Topic used to tell UI users that the status of the watchdogs. For example, if they
	 * are pausing or resuming.
	 */
	public static final String WATCHDOG_STATUS_TOPIC = "org.eclipse.scanning.api.event.status.WatchdogStatus";

    /**
     * The topic used for requests for device information.
     */
	public static final String DEVICE_REQUEST_TOPIC      = "org.eclipse.scanning.request.device.topic";

    /**
     * The topic used for responses to requests for device information.
     */
	public static final String DEVICE_RESPONSE_TOPIC      = "org.eclipse.scanning.response.device.topic";

	/**
	 * A topic on which the values of all scannables should publish. This can happen quite frequently however
	 * not at a rate that JMS should not be able to handle providing the message is kept small.
	 */
	public static final String POSITION_TOPIC              = "org.eclipse.scanning.request.position.topic";

	/**
     * The topic used for positioner requests. This can be used to set the position of one or more scannables.
     */
	public static final String POSITIONER_REQUEST_TOPIC      = "org.eclipse.scanning.request.positioner.topic";

    /**
     * The topic used for positioner requests.
     */
	public static final String POSITIONER_RESPONSE_TOPIC      = "org.eclipse.scanning.response.positioner.topic";

	/**
	 * The is the topic for requests to acquire data from a detector.
	 */
	public static final String ACQUIRE_REQUEST_TOPIC = "org.eclipse.scanning.request.acquire.topic";

	/**
	 * The is the topic for responses to acquire data from a detector.
	 */
	public static final String ACQUIRE_RESPONSE_TOPIC = "org.eclipse.scanning.response.acquire.topic";

	/**
	 * When the user sets up the axes, an AxisConfiguration object will be broadcast on this event.
	 */
	public static final String AXIS_CONFIGURATION_TOPIC      = "org.eclipse.scanning.axis.configuration.topic";

	/**
	 * Topic for information about sample transfer steps and their status.
	 */
	public static final String SAMPLE_TRANSFER_SERVER_TOPIC      = "org.eclipse.scanning.sample.transfer.step.topic";
	/**
	 * Topic for user input in sample transfer system.
	 */
	public static final String SAMPLE_TRANSFER_CMD_TOPIC      = "org.eclipse.scanning.sample.transfer.ui.topic";

}
