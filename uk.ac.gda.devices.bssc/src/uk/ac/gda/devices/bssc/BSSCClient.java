package uk.ac.gda.devices.bssc;

/*
 * Project: BSSC - BioSAXS Sample Changer Date Author Changes 01.07.09 Gobbo Created Copyright 2009 by European
 * Molecular Biology Laboratory - Grenoble
 */

import java.util.logging.Level;

import org.embl.BaseException;
import org.embl.ThreadTools;
import org.embl.bssc.scPrefs.ViscosityLevel;
import org.embl.bssc.scServerInterface;
import org.embl.ctrl.State;
import org.embl.data.Logger;
import org.embl.net.Event;
import org.embl.net.ExporterClient;
import org.embl.net.TransportProtocol;

/**
 * A BSSC Java client implementation example for remote controlling the Sample Changer.
 * 
 * @author <a href="mailto:alexgobbo@gmail.com">Alexandre Gobbo</a>
 */
public class BSSCClient extends ExporterClient {
	public static final TransportProtocol mProtocol = TransportProtocol.STREAM;
	public static final int SC_PORT = 9555;

	public BSSCClient(String server_address) {
		this(server_address, 2, 4000);
	}

	public BSSCClient(String server_address, int retries, int timeout) {
		super(server_address, SC_PORT, mProtocol, retries, timeout);
	}

	public scServerInterface getProxy() {
		scServerInterface sc = (scServerInterface) (super.getProxy(scServerInterface.class));
		return sc;
	}

	State getState() throws BaseException {
		String ret = execute("getState");
		return State.fromString(ret);
	}

	public void waitReady() throws BaseException {
		waitReady(60000);
	}

	public void waitReady(int timeout) throws BaseException {
		long start = System.currentTimeMillis();
		while (true) {
			State state = getState();
			switch (state.getID()) {
			case State.ID_RUNNING:
			case State.ID_MOVING:
			case State.ID_INIT:
			case State.ID_BUSY:
				break;
			default:
				return;
			}
			if ((System.currentTimeMillis() - start) > timeout) {
				throw new BaseException("Timeout waiting application ready");
			}
			ThreadTools.sleep(100);
		}
	}

	public void onEvent(Event event) {
		Logger.trace(this, "RECEIVED EVENT: " + event.toString());
	}

	// Testing
	public static void main(String[] args) {
		try {
			BSSCClient client = new BSSCClient("diamrd2336");
			client.setLogLevel(Level.INFO);
			int[] array = new int[] { 2, 4 };
			String str = createParameter(array);
			org.embl.data.DataTools.convertClass(str, new int[0].getClass());
			// sc.setTimeout(60000);
			System.out.println("Name:");
			System.out.println(client.getServerObjectName());
			System.out.println(client.getServerObjectName());
			System.out.println(client.getServerObjectName());
			String[] methods = client.getMethodList();
			System.out.println("Methods:");
			for (String method : methods) {
				System.out.println(method);
			}
			String[] properties = client.getPropertyList();
			System.out.println("Properties:");
			for (String property : properties) {
				System.out.println(property);
			}
			System.out.println("--------");

			System.out.println(client.execute("getState"));
			System.out.println(client.execute("getStatus"));
			System.out.println("--------");

			// Access through proxy
			scServerInterface sc = client.getProxy();
//			sc.setTemperatureSEU(25.0);
//			State state = sc.getState();
//			System.out.println(state.toString());
			String status = sc.getStatus();
			System.out.println(status);
			ViscosityLevel vl = sc.getViscosityLevel();
			System.out.println(vl.toString());
			sc.setViscosityLevel(ViscosityLevel.high);
			sc.getCurrentLiquidPosition();
			// Start an async task
			sc.clean();
			System.out.println("Executing cleaning");
			// Wait task completion
			client.waitReady(60000);
			System.out.println("Task info:");
			String[] task_info = sc.getLastTaskInfo();
			for (String s : task_info) {
				System.out.println(s);
			}

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}