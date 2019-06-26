package org.opengda.lde.ui.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.lde.ui.views.ReducedDataPlotView;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import gda.rcp.views.FindableExecutableExtension;

public class ReducedDataPlotViewFactory extends FindableBase implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ReducedDataPlotViewFactory.class);
	private String viewPartName;
	private String eventAdminName;
	private LDEResourceUtil resUtil;

	@Override
	public Object create() throws CoreException {
		logger.info("Creating Spectrum plot view");
		ReducedDataPlotView plotView = new ReducedDataPlotView();
		plotView.setViewPartName(viewPartName);
		plotView.setEventAdminName(eventAdminName);
		plotView.setResUtil(getResUtil());
		return plotView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (eventAdminName == null ) {
			throw new IllegalArgumentException("'eventAdmin' cannot be null.");
		}

	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}
}
