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
package org.eclipse.scanning.api.event.queue;

import org.eclipse.scanning.api.event.bean.IBeanClassListener;

public interface IQueueStatusBeanListener extends IBeanClassListener<QueueStatusBean> {

	public void queueStatusChanged(QueueStatusBeanEvent evt);

	@Override
	default Class<QueueStatusBean> getBeanClass() {
		return QueueStatusBean.class;
	}

}
