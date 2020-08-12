package org.eclipse.scanning.example.malcolm;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.epics.pvaccess.server.ServerContext;
import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;

/**
 * This class creates an Epics V4 service, that listens for connections and handles RPC, GET, PUT etc. The modelled
 * device is meant to represent a typical Malcolm Device, and has attributes and methods set up accordingly. Any RPC
 * call made to the device just pause for 2 seconds and then return an empty Map
 *
 * @author Matt Taylor
 *
 */
public abstract class AbstractEPICSv4Device implements IEPICSv4Device {

	protected String recordName = "mydevice";
	protected static int traceLevel = 0;
	protected final CountDownLatch latch = new CountDownLatch(1);
	protected DummyMalcolmRecord pvRecord = null;

	public AbstractEPICSv4Device(String deviceName) {
		recordName = deviceName;
	}

	@Override
	public String getRecordName() {
		return recordName;
	}

	@Override
	public void stop() {
		latch.countDown();
	}

	@Override
	public Map<String, PVStructure> getReceivedRPCCalls() {
		return pvRecord.getReceivedRPCCalls();
	}

	@Override
	public void start() throws Exception {
		PVDatabase master = PVDatabaseFactory.getMaster();
		pvRecord = DummyMalcolmRecord.create(recordName);
		pvRecord.setTraceLevel(traceLevel);
		master.addRecord(pvRecord);
		ServerContext context = ServerContextImpl.startPVAServer(getPvaProviderName(), 0, true,
				System.out);
		latch.await();
		master.removeRecord(pvRecord);
		context.destroy();
	}

	protected abstract String getPvaProviderName();

}
