package org.eclipse.scanning.test.epics;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.connector.epics.MalcolmEpicsV4Connection;

public class HangingGetConnectorService extends MalcolmEpicsV4Connection {

	private CountDownLatch latch;

	public HangingGetConnectorService() {
		super();
		this.latch = new CountDownLatch(1);
	}

	@Override
	protected MalcolmMessage sendGetMessage(IMalcolmDevice device, MalcolmMessage message) {
		try {
			latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public void disconnect() {
		latch.countDown();
		super.disconnect();
	}
}
