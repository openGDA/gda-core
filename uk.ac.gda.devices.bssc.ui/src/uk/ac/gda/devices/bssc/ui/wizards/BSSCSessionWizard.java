package uk.ac.gda.devices.bssc.ui.wizards;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;
import uk.ac.gda.devices.hatsaxs.HatsaxsUtils;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.devices.hatsaxs.wizards.HatsaxsWizard;
import uk.ac.gda.devices.hatsaxs.wizards.HatsaxsWizardPage;

public class BSSCSessionWizard extends HatsaxsWizard implements INewWizard {
	private ISelection selection;
	private double concentration = 0.1;
	private int frames = 10;
	private double timePerFrame = 0.2;
	private String viscosity = "medium";
	private boolean yellowsample = false;

	public BSSCSessionWizard() {
		super(HatsaxsUtils.BIOSAXS_EXTENSION);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		page = new HatsaxsWizardPage(selection, HatsaxsUtils.BIOSAXS_EXTENSION);
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	@Override
	protected InputStream getContentStream() {
		BSSCSessionBean sessionBean = new BSSCSessionBean();
		List<TitrationBean> measurements = new ArrayList<TitrationBean>();
		LocationBean location = new LocationBean(BSSCSessionBean.BSSC_PLATES);
		location.setPlate((short) 1);
		location.setRow('A');
		location.setColumn((short) 1);
		LocationBean bufferLocation = new LocationBean(BSSCSessionBean.BSSC_PLATES);
		bufferLocation.setPlate((short) 1);
		bufferLocation.setRow('A');
		bufferLocation.setColumn((short) 1);
		TitrationBean tb = new TitrationBean();
		tb.setLocation(location);
		tb.setConcentration(concentration);
		tb.setMolecularWeight(0);
		tb.setFrames(frames);
		tb.setTimePerFrame(timePerFrame);
		tb.setViscosity(viscosity);
		tb.setYellowSample(yellowsample);
		measurements.add(tb);
		sessionBean.setMeasurements(measurements);
		
		return BSSCWizardUtils.sessionBeanToStream(sessionBean);
	}
}