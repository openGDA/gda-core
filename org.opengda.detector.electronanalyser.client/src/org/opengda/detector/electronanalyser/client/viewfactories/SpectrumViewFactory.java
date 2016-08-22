package org.opengda.detector.electronanalyser.client.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.SpectrumView;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.FindableExecutableExtension;

public class SpectrumViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(SpectrumViewFactory.class);
	private String viewPartName;
	private String name;
	private IVGScientaAnalyser analyser;
	private String updatePV;
	private double updatesPerSecond = 0; // Initialise to 0 if set it will be different.

	@Override
	public Object create() throws CoreException {
		logger.info("Creating Spectrum plot view");
		SpectrumView spectrumView = new SpectrumView();
		spectrumView.setViewPartName(viewPartName);
		if (analyser != null) {
			spectrumView.setAnalyser(analyser);
		}
		if (updatePV != null) {
			spectrumView.setUpdatePV(updatePV);
		}
		if (updatesPerSecond != 0) {
			spectrumView.setUpdatesPerSecond(updatesPerSecond);
		}

		return spectrumView;
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

	public double getUpdatesPerSecond() {
		return updatesPerSecond;
	}

	public void setUpdatesPerSecond(double updatesPerSecond) {
		this.updatesPerSecond = updatesPerSecond;
	}
}
