package org.opengda.detector.electronanalyser.client.viewfactories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.views.SlicesView;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlicesViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(SlicesViewFactory.class);
	private String viewPartName;
	private String name;
	private IVGScientaAnalyser analyser;
	private String arrayPV;

	@Override
	public Object create() throws CoreException {
		logger.info("Creating slices plot view");
		SlicesView slicesView = new SlicesView();
		slicesView.setViewPartName(viewPartName);
		if (analyser != null) {
			slicesView.setAnalyser(analyser);
		}
		
		if (arrayPV != null) {
				slicesView.setArrayPV(arrayPV);
		}
		return slicesView;
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
			throw new IllegalArgumentException("analyser cannot be null in slices View.");
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
