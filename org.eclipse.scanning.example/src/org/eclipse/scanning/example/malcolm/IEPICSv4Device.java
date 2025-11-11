package org.eclipse.scanning.example.malcolm;

import java.util.Map;

import org.epics.pvdata.pv.PVStructure;

public interface IEPICSv4Device {

	void start() throws Exception;

	String getRecordName();

	void stop();

	Map<String, PVStructure> getReceivedRPCCalls();
}
