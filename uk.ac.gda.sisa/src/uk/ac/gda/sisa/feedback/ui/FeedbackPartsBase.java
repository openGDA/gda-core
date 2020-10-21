package uk.ac.gda.sisa.feedback.ui;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.ControlSet;

public class FeedbackPartsBase {
	
	private static final Logger logger = LoggerFactory.getLogger(FeedbackPartsBase.class);
	protected final FeedbackControls feedbackControls;
	
	public FeedbackPartsBase() {
		
		try {
			feedbackControls = Finder.findLocalSingleton(FeedbackControls.class);
		} catch (IllegalArgumentException exception) {
			String msg = "No FeedbackControls was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}
	
	protected void addControlGroup(Composite composite, ControlSet controlSet, int columns) {
		Group group = new Group(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(columns).spacing(10, 20).applyTo(group);
		controlSet.getControls().stream().forEachOrdered(c -> c.createControl(group));
	}
	
	protected void addControlGroup(Composite composite, String groupName, ControlSet controlSet, int columns) {
		Group group = new Group(composite, SWT.NONE);
		group.setText(groupName);
		GridLayoutFactory.swtDefaults().numColumns(columns).spacing(10, 20).applyTo(group);
		controlSet.getControls().stream().filter(c -> groupName.equals(c.getGroup())).forEachOrdered(c -> c.createControl(group));
	}
	
	protected void setScrollerContentAndSize(Composite scrollerContent, ScrolledComposite scroller) {
		// Set the child as the scrolled content of the ScrolledComposite
		scroller.setContent(scrollerContent);
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		scroller.setMinSize(scrollerContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

}
