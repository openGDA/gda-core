package org.opengda.lde.ui.views;

import gda.jython.scriptcontroller.Scriptcontroller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReducedDataPlotView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(ReducedDataPlotView.class);

	private ReducedDataPlotComposite plotComposite;
	private Scriptcontroller eventAdmin;
	
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
			plotComposite.setEventAdmin(eventAdmin);
			plotComposite.setPlotName(getPartName());
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

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
}
