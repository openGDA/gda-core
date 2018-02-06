package org.dawnsci.datavis.live;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.scanning.api.event.IEventService;

public class ServiceManager {
	
	private static IRemoteDatasetService dservice;
	private static IEventService eventService;
	private static ILoaderService loaderService;

	public static IRemoteDatasetService getRemoteDatasetService() {
		return dservice;
	}

	public void setRemoteDatasetService(IRemoteDatasetService d) {
		dservice = d;
	}
	
	public void setIEventService(IEventService service) {
		eventService = service;
	}

	public static IEventService getIEventService() {
		return eventService;
	}
	
	public static ILoaderService getILoaderService() {
		return loaderService;
	}
	
	public void setILoaderService(ILoaderService service) {
		loaderService = service;
	}
}

