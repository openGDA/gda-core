package org.opengda.lde.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.opengda.lde.ui.views.ReducedDataPlotComposite.ReducedDataConfig;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObservable;

public class ReducedDataPlotView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(ReducedDataPlotView.class);
	public static final String ID = "org.opengda.lde.ui.views.reducdeddataplotview";
	private ReducedDataPlotComposite plotComposite;
	private ReducedDataConfig config;
	private IObservable eventSource;
	private LDEResourceUtil resUtil;

	public ReducedDataPlotView() {
		setTitleToolTip("live display of integrated spectrum");
		// setContentDescription("A view for displaying integrated spectrum.");
		setPartName("Reduced Data");
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());

		try {
			plotComposite = new ReducedDataPlotComposite(this, rootComposite, SWT.None);
			plotComposite.setEventSource(eventSource);
			plotComposite.setResUtil(getResUtil());
			plotComposite.setPlotName(getPartName());
			plotComposite.setConfig(config);
			plotComposite.initialise();

		} catch (Exception e) {
			logger.error("Cannot create spectrum plot composite.", e);
		}
	}

	@Override
	public void setFocus() {
		plotComposite.setFocus();
	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public IObservable getEventSource() {
		return eventSource;
	}

	public void setEventSource(IObservable eventSource) {
		this.eventSource = eventSource;
	}

	public ReducedDataConfig getConfig() {
		return config;
	}

	public void setConfig(ReducedDataConfig config) {
		this.config = config;
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}
}
