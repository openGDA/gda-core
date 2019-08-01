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
package org.eclipse.scanning.api.event.core;

import org.eclipse.scanning.api.event.EventException;

/**
 * The interface provided to an {@link IJobQueue} which defines the process to run
 * for a bean that has been removed from the queue.
 *
 * @author Matthew Gerring
 *
 * @param <T>
 */
@FunctionalInterface
public interface IProcessCreator<T> {

	IBeanProcess<T> createProcess(T bean, IPublisher<T> statusNotifier) throws EventException;
}
