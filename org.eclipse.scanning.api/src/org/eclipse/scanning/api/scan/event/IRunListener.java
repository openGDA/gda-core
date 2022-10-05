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
package org.eclipse.scanning.api.scan.event;

import java.util.EventListener;
import java.util.function.Consumer;

import org.eclipse.scanning.api.scan.ScanningException;


/**
 * A listener which is fired before and after a device is run.
 *
 * @author Matthew Gerring
 *
 */
public interface IRunListener extends EventListener {

	/**
	 * Called when the device changes state
	 * @param evt
	 * @throws ScanningException which will terminate the scan
	 */
	@SuppressWarnings("unused")
	default void stateChanged(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Called before a run() is made on the device. Can
	 * be used to modify the model before a given run of the device.
	 * @param evt
	 * @throws ScanningException which will terminate the scan
	 */
	@SuppressWarnings("unused")
	default void runWillPerform(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Used to notify that a given device has been run.
	 * @param evt
	 * @throws ScanningException which will terminate the scan
	 */
	@SuppressWarnings("unused")
	default void runPerformed(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Called before a run() is made on the device. Can
	 * be used to modify the model before a given run of the device.
	 * @param evt
	 * @throws ScanningException which will terminate the scan
	 */
	@SuppressWarnings("unused")
	default void writeWillPerform(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	/**
	 * Used to notify that a given device as been run.
	 * @param evt
	 * @throws ScanningException which will terminate the scan
	 */
	@SuppressWarnings("unused")
	default void writePerformed(RunEvent evt) throws ScanningException {
		// default implementation does nothing, subclasses should override as necessary
	}

	public static IRunListener createRunWillPerformListener(Consumer<RunEvent> eventConsumer) {
		return new IRunListener() {
			@Override
			public void runWillPerform(RunEvent runEvent) {
				eventConsumer.accept(runEvent);
			}
		};
	}

	public static IRunListener createRunPerformedListener(Consumer<RunEvent> eventConsumer) {
		return new IRunListener() {
			@Override
			public void runPerformed(RunEvent runEvent) {
				eventConsumer.accept(runEvent);
			}
		};
	}

	public static IRunListener createStateChangedListener(Consumer<RunEvent> eventConsumer) {
		return new IRunListener() {
			@Override
			public void stateChanged(RunEvent runEvent) {
				eventConsumer.accept(runEvent);
			}
		};
	}

}
