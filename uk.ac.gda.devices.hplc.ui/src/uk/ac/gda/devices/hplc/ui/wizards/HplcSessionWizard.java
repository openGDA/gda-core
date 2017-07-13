package uk.ac.gda.devices.hplc.ui.wizards;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import uk.ac.gda.devices.hplc.beans.HplcSessionBean;
import uk.ac.gda.devices.hplc.beans.HplcBean;
import uk.ac.gda.devices.hatsaxs.HatsaxsUtils;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.devices.hatsaxs.wizards.HatsaxsWizard;
import uk.ac.gda.devices.hatsaxs.wizards.HatsaxsWizardPage;

public class HplcSessionWizard extends HatsaxsWizard implements INewWizard {
	private ISelection selection;
	private double concentration = 0.1;
	private double timePerFrame = 0.2;

	public HplcSessionWizard() {
		super(HatsaxsUtils.HPLC_EXTENSION);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new HatsaxsWizardPage(selection, HatsaxsUtils.HPLC_EXTENSION);
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	protected InputStream getContentStream() {
		HplcSessionBean sessionBean = new HplcSessionBean();
		List<HplcBean> measurements = new ArrayList<HplcBean>();
		LocationBean location = new LocationBean(HplcSessionBean.HPLC_PLATES);
		location.setPlate((short) 1);
		location.setRow('A');
		location.setColumn((short) 1);
		HplcBean hb = new HplcBean();
		hb.setLocation(location);
		hb.setConcentration(concentration);
		hb.setBuffers("");
		hb.setMolecularWeight(0);
		hb.setTimePerFrame(timePerFrame);
		measurements.add(hb);
		sessionBean.setMeasurements(measurements);
		
		return HplcWizardUtils.sessionBeanToStream(sessionBean);
	}
}