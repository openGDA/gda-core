package uk.ac.gda.server.exafs.scan;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;

import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.XasScannable;
import gda.exafs.scan.XasScanPointCreator;
import gda.scan.ScanPositionProviderFactory;
import uk.ac.gda.beans.exafs.XanesScanParameters;

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
	public Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception {
		Detector[] detectorList = getDetectors();
		Detector[] orderedDetectors = getOrderedDetectors(detectorList);
		return buildScanArguments(orderedDetectors);
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

		// Make PositionProvider object using list of energy-time values to be used for scan
		double[][] energyTimeArray = resolveEnergiesFromScanBean();
		List<List<Double>> positions = Stream.of(energyTimeArray)
				.map(arrVals -> List.of(arrVals[0], arrVals[1]))
				.toList();
		var positionProvider = ScanPositionProviderFactory.create(positions);

		return addScannableArgs(xas_scannable, positionProvider, detectorList);
	}

	private Object[] addScannableArgs(XasScannable xas_scannable, Object energies, Detector[] detectorList) {
		Object[] args = new Object[] { xas_scannable, energies };
		args = ArrayUtils.addAll(args, detectorList);
		return args;
	}

	protected double[][] resolveEnergiesFromScanBean() throws Exception {
		return XasScanPointCreator.build(scanBean).getEnergies();
	}

	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}
}
