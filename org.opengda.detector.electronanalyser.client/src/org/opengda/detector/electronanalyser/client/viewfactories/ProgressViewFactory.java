package org.opengda.detector.electronanalyser.client.viewfactories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.ProgressView;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ProgressViewFactory.class);
	private String viewPartName;
	private String name;
	private IVGScientaAnalyser analyser;
	@Override
	public Object create() throws CoreException {
		logger.info("Creating Progress view");
		ProgressView progressView = new ProgressView();
		progressView.setViewPartName(viewPartName);
		if (analyser != null) {
			progressView.setAnalyser(analyser);
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
		if (analyser == null ) {
			throw new IllegalArgumentException("analyser cannot be null in progress View.");
		}
		
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

}
