package uk.ac.diamond.daq.experiment.ui.plan;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * Currently just a placeholder for upcoming view
 * 
 * @author fri44821
 *
 */
public class PlanOverview extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub

		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(composite);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		Text holder = new Text(composite, SWT.NORMAL);
		holder.setText("Coming soon");
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
 