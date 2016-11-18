package uk.ac.gda.devices.bssc.ui.perspectives;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

public class PerspectivePropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		/*
		 * Returns true if the actual perspective is a BioSAXS
		 */
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getPerspective();
			String perspectiveId = perspective.getId();
			if (perspectiveId.startsWith("uk.ac.gda.devices.bssc.biosaxs")) {
				return true;
			}
			return false;
		}
		return false;
	}
}