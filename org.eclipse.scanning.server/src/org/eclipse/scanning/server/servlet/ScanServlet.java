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
package org.eclipse.scanning.server.servlet;

import java.lang.management.ManagementFactory;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet to do any scan type based on the information provided
 * in a ScanBean.
 *
 * @see example.xml
 *
     Spring config started, for instance:
    <pre>

    {@literal <bean id="scanServlet" class="org.eclipse.scanning.server.servlet.ScanServlet" init-method="connect">}
    {@literal    <property name="broker"      value="tcp://p45-control:61616" />}
    {@literal    <property name="submitQueue" value="uk.ac.diamond.p45.submitQueue" />}
    {@literal    <property name="statusTopic" value="uk.ac.diamond.p45.statusTopic" />}
    {@literal    <property name="durable"     value="true" />}
    {@literal </bean>}

    </pre>

    FIXME Add security via activemq layer. Anyone can run this now.

 *
 * @author Matthew Gerring
 *
 */
public class ScanServlet extends AbstractJobQueueServlet<ScanBean> {

	private static final Logger logger = LoggerFactory.getLogger(ScanServlet.class);

	public ScanServlet() {
		setPauseOnStart(true);
	}

	/**
	 * Utility constructor for test classes only- this forces the MVStore for JobQueueImpl (which uses
	 * scanServlet.getSubmitQueue()) to be unique per JVM, ensuring that multiple sets of running tests
	 * do not collide on trying to access their MVStores.
	 * The creation of submission queues and of their submitters both use getSubmitQueue so parity is conserved.
	 *
	 * Special care should be taken by any test using this method to clean up any MVStore files that are produced.
	 * See ScanningTestUtil.clearStore()
	 *
	 * @param forTest
	 */
	public ScanServlet(boolean forTest) {
		this();
		// Don't want to rely on ScanningTestUtils
		if (forTest) submitQueue = submitQueue.concat(ManagementFactory.getRuntimeMXBean().getName());
	}

	@Override
	public String getName() {
		return "Scan Consumer";
	}

	@Override
	public ScanProcess createProcess(ScanBean scanBean, IPublisher<ScanBean> response) throws EventException {
		if (scanBean.getScanRequest()==null) throw new EventException("The scan must include a request to run something!");

		debug("Accepting bean", scanBean, response);
		preprocess(scanBean);
		debug("After processing bean", scanBean, response);

		return new ScanProcess(scanBean, response, isBlocking());
	}

	private void debug(String message, ScanBean scanBean, IPublisher<ScanBean> response) {
		if (!logger.isDebugEnabled()) return;

		logger.debug("{} : {}", message, scanBean);
		try {
			logger.debug("from request : {}", Services.getEventService().getEventConnectorService().marshal(scanBean.getScanRequest()));
		} catch (Exception e) {
			logger.error("Error printing marshalled debugging scan request!", e);
		}
		logger.debug("at response URI {}", response.getUri());

	}

	private void preprocess(ScanBean scanBean) throws ProcessingException {
		ScanRequest req = scanBean.getScanRequest();
		if (req.isIgnorePreprocess()) {
			return;
		}
		for (IPreprocessor processor : Services.getPreprocessors()) {
			req = processor.preprocess(req);
		}
		scanBean.setScanRequest(req);
	}
}
