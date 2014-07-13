package org.opengda.lde.ui.viewfactories;

import gda.jython.scriptcontroller.Scriptcontroller;
import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.lde.ui.views.ReducedDataPlotView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReducedDataPlotViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ReducedDataPlotViewFactory.class);
	private String viewPartName;
	private String name;
	private Scriptcontroller eventAdmin;
	
	@Override
	public Object create() throws CoreException {
		logger.info("Creating Spectrum plot view");
		ReducedDataPlotView plotView = new ReducedDataPlotView();
		plotView.setViewPartName(viewPartName);
		plotView.setEventAdmin(eventAdmin);
		return plotView;
	}

	@Override
	public void setName(String name) {
		this.name=name;		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (eventAdmin == null ) {
			throw new IllegalArgumentException("'eventAdmin' cannot be null.");
		}
		
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
}
