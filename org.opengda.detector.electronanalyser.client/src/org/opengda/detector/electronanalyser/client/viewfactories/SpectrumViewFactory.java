package org.opengda.detector.electronanalyser.client.viewfactories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.SpectrumView;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectrumViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(SpectrumViewFactory.class);
	private String viewPartName;
	private String name;
	private IVGScientaAnalyser analyser;
	private String arrayPV;
	@Override
	public Object create() throws CoreException {
		logger.info("Creating Spectrum plot view");
		SpectrumView spectrumView = new SpectrumView();
		spectrumView.setViewPartName(viewPartName);
		if (analyser != null) {
			spectrumView.setAnalyser(analyser);
			spectrumView.setArrayPV(arrayPV);
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

	public String getArrayPV() {
		return arrayPV;
	}

	public void setArrayPV(String arrayPV) {
		this.arrayPV = arrayPV;
	}

}
