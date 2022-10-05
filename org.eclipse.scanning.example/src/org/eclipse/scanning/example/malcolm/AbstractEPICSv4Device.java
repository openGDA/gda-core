package org.eclipse.scanning.example.malcolm;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.epics.pvaccess.server.ServerContext;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdatabase.PVDatabase;
import org.epics.pvdatabase.PVDatabaseFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(AbstractEPICSv4Device.class);

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
		ServerContext context = startPvaServerWithReflection(getPvaProviderName(), 0, true, System.out);
		logger.info("PVA Server Started");
		latch.await();
		master.removeRecord(pvRecord);
		context.destroy();
	}

	protected abstract String getPvaProviderName();


	// TODO replace this once there is a solution to https://github.com/epics-base/epicsCoreJava/issues/100
	/**
	 * Since Tycho runs in OSGi it cannot see internal class from EpicsV4:
	 * {@code org.epics.pvaccess.server.impl.remote.ServerContextImpl}
	 * <p>
	 * The Epics library currently provides no other exported API mechanism to
	 * start a PVA server apart from ServerFactory.create() but this is not good
	 * for tests as there is no way to stop the created server.
	 * <p>
	 * This workaround uses reflection to call the method and return the result as a
	 * ServerContext interface object which is from an exported package.
	 * <p>
	 * Keep checking <a href="https://github.com/epics-base/epicsCoreJava/issues/100"> the issue
	 * for solution so this can be removed.
	 */
	private ServerContext startPvaServerWithReflection(String providerNames, int timeToRun,
			boolean runInSeparateThread, PrintStream printInfoStream) throws Exception {
		String implClassName = "org.epics.pvaccess.server.impl.remote.ServerContextImpl";
		Class<?> implClass;
		Bundle bundle = FrameworkUtil.getBundle(ServerContext.class);
		if (bundle != null) {
			implClass = bundle.loadClass(implClassName);
		} else {
			implClass = Class.forName(implClassName);
		}
		Method startPvaMethod = implClass.getMethod("startPVAServer", String.class, int.class, boolean.class,
				PrintStream.class);
		return (ServerContext) startPvaMethod.invoke(null, providerNames, timeToRun, runInSeparateThread,
				printInfoStream);
	}

}
