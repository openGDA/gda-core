package uk.ac.gda.sisa.monitor.ui;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraRegionPart extends MonitorPartsBase {
	
	private static final Logger logger = LoggerFactory.getLogger(CameraRegionPart.class);
	
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");
		ScrolledComposite scroller = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite scrollerContent = new Composite(scroller, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.fill = true;
		rowLayout.center = true;
		scrollerContent.setLayout(rowLayout);
		addControlGroup(scrollerContent, "X", monitorConfig.getCameraRegionControls(), 3);
		addControlGroup(scrollerContent, "Y", monitorConfig.getCameraRegionControls(), 3);
		setScrollingComposite(scrollerContent, scroller);
	}
	

}
