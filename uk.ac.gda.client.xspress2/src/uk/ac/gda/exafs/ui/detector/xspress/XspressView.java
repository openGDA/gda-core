package uk.ac.gda.exafs.ui.detector.xspress;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.beans.xspress.XspressParameters;

public class XspressView extends ViewPart {
	private Xspress xspress;
	
	@Override
	public void createPartControl(Composite parent) {
		//xspress = new Xspress(xmlPath, this.getSite(), parent, xspressParameters, dirtyContainer);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
