package uk.ac.gda.server.exafs.scan;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyTuple;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.XasScannable;
import gda.exafs.scan.ExafsScanPointCreator;
import gda.exafs.scan.XanesScanPointCreator;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;

public class EnergyScan extends XasScanBase {
	protected Scannable energyScannable;

	protected EnergyScan() {
		// Used by XasScanFactory
	}

	@Override
	public String getScanType() {
		if (scanBean instanceof XanesScanParameters) {
			return "Xanes";
		}
		return "Exafs";
	}

	@Override
	protected Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception {
		Detector[] detectorList = getDetectors();
		Object[] args = buildScanArguments(detectorList);
		return args;
	}

	private XasScannable createAndconfigureXASScannable() {
		XasScannable xasScannable = new XasScannable();
		xasScannable.setName("xas_scannable");
		xasScannable.setEnergyScannable(energyScannable);
		return xasScannable;
	}

	private Object[] buildScanArguments(Detector[] detectorList) throws Exception {
		XasScannable xas_scannable = createAndconfigureXASScannable();
		xas_scannable.setDetectors(detectorList);
		return addScannableArgs(xas_scannable, resolveEnergiesFromScanBean(), detectorList);
	}

	private Object[] addScannableArgs(XasScannable xas_scannable, PyTuple energies, Detector[] detectorList) {
		Object[] args = new Object[] { xas_scannable, energies };
		args = ArrayUtils.addAll(args, detectorList);
		return args;
	}

	protected PyTuple resolveEnergiesFromScanBean() throws Exception {
		if (scanBean instanceof XanesScanParameters) {
			return XanesScanPointCreator.calculateEnergies((XanesScanParameters) scanBean);
		}
		return ExafsScanPointCreator.calculateEnergies((XasScanParameters) scanBean);
	}

	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}
}
