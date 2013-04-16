package org.opengda.detector.electronanalyser.client.viewfactories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.ProgressView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ProgressViewFactory.class);
	private String viewPartName;
	private String name;
	private String currentPointPV;
	private String totalPointsPV;
	@Override
	public Object create() throws CoreException {
		logger.info("Creating Progress view");
		ProgressView progressView = new ProgressView();
		progressView.setViewPartName(viewPartName);
		if (currentPointPV != null) {
			progressView.setCurrentPointPV(currentPointPV);
		}
		if (totalPointsPV != null) {
			progressView.setTotalPointsPV(totalPointsPV);
		}
		
		return progressView;
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
		if (currentPointPV == null ) {
			throw new IllegalArgumentException("Current point PV cannot be null in progress View.");
		}
		if (totalPointsPV == null ) {
			throw new IllegalArgumentException("Total points PV cannot be null in progress View.");
		}
		
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	public String getCurrentPointPV() {
		return currentPointPV;
	}

	public void setCurrentPointPV(String currentPointPV) {
		this.currentPointPV = currentPointPV;
	}

	public String getTotalPointsPV() {
		return totalPointsPV;
	}

	public void setTotalPointsPV(String totalPointsPV) {
		this.totalPointsPV = totalPointsPV;
	}

}
