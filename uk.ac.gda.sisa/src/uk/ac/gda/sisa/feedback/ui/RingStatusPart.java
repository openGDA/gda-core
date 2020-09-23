package uk.ac.gda.sisa.feedback.ui;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

public class RingStatusPart extends FeedbackPartsBase {
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		ScrolledComposite scroller = new ScrolledComposite(parent, SWT.V_SCROLL);
		Composite scrollerContent = new Composite(scroller, SWT.NONE);
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		//rowLayout.fill = true;
		//rowLayout.center = true;
		scrollerContent.setLayout(rowLayout);
		addControlGroup(scrollerContent, feedbackControls.getRingStatusControls(), 5);
		setScrollerContentAndSize(scrollerContent, scroller);
	}

}
