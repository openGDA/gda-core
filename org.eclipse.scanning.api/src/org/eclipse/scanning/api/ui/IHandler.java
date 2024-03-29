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
package org.eclipse.scanning.api.ui;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.JobQueueConfiguration;
import org.eclipse.scanning.api.event.status.StatusBean;

public interface IHandler<T extends StatusBean> {

	default void init(IEventService eventService, JobQueueConfiguration conf) {

	}

	/**
	 * Defines if this handler can open the result in this bean.
	 * @param bean
	 * @return
	 */
	boolean isHandled(StatusBean bean);

}
