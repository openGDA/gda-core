package uk.ac.gda.sisa.feedback.ui;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemperaturesPart extends FeedbackPartsBase {
	
	private static final Logger logger = LoggerFactory.getLogger(TemperaturesPart.class);
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");
		ScrolledComposite scroller = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite scrollerContent = new Composite(scroller, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.fill = true;
		rowLayout.center = true;
		scrollerContent.setLayout(rowLayout);
		drawControls(scrollerContent, feedbackControls.getTemperatureControls(), 1);
		setScrollerContentAndSize(scrollerContent, scroller);
	}
}
