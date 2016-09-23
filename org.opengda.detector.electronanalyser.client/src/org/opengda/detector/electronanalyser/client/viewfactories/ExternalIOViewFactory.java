package org.opengda.detector.electronanalyser.client.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.ExternalIOView;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.FindableExecutableExtension;

public class ExternalIOViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(ExternalIOViewFactory.class);
	private String viewPartName;
	private String updatePV;
	private String name;
	private IVGScientaAnalyser analyser;
	@Override
	public Object create() throws CoreException {
		logger.info("Creating external IO plot view");
		ExternalIOView externalIOView = new ExternalIOView();
		externalIOView.setViewPartName(viewPartName);
		if (analyser != null) {
			externalIOView.setAnalyser(analyser);
		}
		if (updatePV != null) {
			externalIOView.setUpdatePV(updatePV);
		}

		return externalIOView;
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
			throw new IllegalArgumentException("analyser cannot be null in Spectrum View.");
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

	public String getUpdatePV() {
		return updatePV;
	}

	public void setUpdatePV(String updatePV) {
		this.updatePV = updatePV;
	}

}
