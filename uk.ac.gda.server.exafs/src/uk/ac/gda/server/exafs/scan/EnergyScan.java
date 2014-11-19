package uk.ac.gda.server.exafs.scan;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.JEPScannable;
import gda.device.scannable.XasScannable;
import gda.exafs.scan.ExafsScanPointCreator;
import gda.exafs.scan.XanesScanPointCreator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.nfunk.jep.ParseException;
import org.python.core.PyTuple;

import uk.ac.gda.beans.exafs.SignalParameters;
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
		List<Scannable> signalParameters = getSignalList();
		Detector[] detectorList = getDetectors();
		Object[] args = buildScanArguments(detectorList, signalParameters);
		return args;
	}

	private XasScannable createAndconfigureXASScannable() {
		XasScannable xasScannable = new XasScannable();
		xasScannable.setName("xas_scannable");
		xasScannable.setEnergyScannable(energyScannable);
		return xasScannable;
	}

	private Object[] buildScanArguments(Detector[] detectorList, List<Scannable> signalParameters) throws Exception {
		XasScannable xas_scannable = createAndconfigureXASScannable();
		xas_scannable.setDetectors(detectorList);
		return addScannableArgs(xas_scannable, resolveEnergiesFromScanBean(), detectorList, signalParameters);
	}

	private Object[] addScannableArgs(XasScannable xas_scannable, PyTuple energies, Detector[] detectorList,
			List<Scannable> signalParameters) {
		Object[] args = new Object[] { xas_scannable, energies };
		args = ArrayUtils.addAll(args, detectorList);
		args = ArrayUtils.addAll(args, signalParameters.toArray());
		return args;
	}

	protected PyTuple resolveEnergiesFromScanBean() throws Exception {
		if (scanBean instanceof XanesScanParameters) {
			return XanesScanPointCreator.calculateEnergies((XanesScanParameters) scanBean);
		}
		return ExafsScanPointCreator.calculateEnergies((XasScanParameters) scanBean);
	}

	private List<Scannable> getSignalList() throws ParseException {
		List<Scannable> signalList = new ArrayList<Scannable>();
		for (SignalParameters signal : outputBean.getSignalList()) {
			int dp = signal.getDecimalPlaces();
			String dataFormat = "%6." + dp + 'f';// # construct data format from dp e.g. "%6.2f"
			Scannable scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(),
					dataFormat, signal.getName(), signal.getExpression());
			signalList.add(scannable);
		}
		return signalList;
	}

	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}
}
