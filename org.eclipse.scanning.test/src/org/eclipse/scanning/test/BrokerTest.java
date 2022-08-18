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
package org.eclipse.scanning.test;

import java.net.URI;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Doing this works better than using vm:// uris.
 *
 * Please do not use vm:// as it does not work when many tests are started and stopped
 * in a big unit testing system because each test uses the same in VM broker.
 *
 *
 *  TODO Should have static start of broker or per test start for problematic tests
 *
 * @author Matthew Gerring.
 *
 */
public abstract class BrokerTest {

	protected static URI uri;

	private static BrokerDelegate delegate;

	private boolean startEveryTime;

	protected BrokerTest() {
		this(false);
	}

	protected BrokerTest(boolean startEveryTime) {
		this.startEveryTime = startEveryTime;
	}

	@BeforeAll
	public static final void startBroker() throws Exception {
		delegate = new BrokerDelegate();
		delegate.start();
		uri = delegate.uri;
		ScanningTestUtils.clearTmp();
	}

	@BeforeEach
	public final void startLocalBroker() throws Exception {
		if (startEveryTime) {
			if (delegate!=null) delegate.stop();
			delegate = new BrokerDelegate();
			delegate.start();
			uri      = delegate.uri;
		}
	}

	@AfterAll
	public static final void stopBroker() throws Exception {
		if (delegate!=null) delegate.stop();
	}

	/*
	 *  With multiple AfterClass methods, they are run alphabetically.
	 *  Store comes after Stop so this is final thing to happen
	 */
	@AfterAll
	public static final void storeClose() {
		ScanningTestUtils.clearMVStore();
	}

}
