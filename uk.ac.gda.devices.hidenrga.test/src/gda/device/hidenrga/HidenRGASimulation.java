package gda.device.hidenrga;

import java.io.IOException;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.hidenrga.HidenRGAController;
import gda.device.hidenrga.HidenRGAScannable;

/**
 * Not a unit test, but connects to Hiden RGA for testing against the
 * simulation.
 *
 * @author rjw82
 *
 */
public class HidenRGASimulation {

	public static void main(String[] args) throws IOException {
		try {
			LocalProperties.set("gov.aps.jca.JCALibrary.properties",
					HidenRGASimulation.class.getResource("JCALibrary.properties").getFile());

			HidenRGAController controller = new HidenRGAController("ME12G-EA-RGA-01");
			HidenRGAScannable rga = new HidenRGAScannable();
			rga.setController(controller);
			rga.setName("rga");
			rga.configure();

			int[] masses = new int[] { 16, 32, 48 };
			rga.setMasses(masses);

			//rga.setMasses([16, 32 ,48]);

//			// scan simulation
			rga.atScanStart();
			for (int i = 0; i < 20; i++){
				waitAndPrintValues(rga, masses);
			}
			rga.atScanEnd();

			Thread.sleep(5000);

			// file writing simulation
			LocalProperties.set("gda.data.scan.datawriter.datadir","/tmp");
			LocalProperties.set("gda.var","/tmp");
//			rga.setCollectionRate(5);  // demo how to throttle data collection
			rga.startRecording();
			Thread.sleep(10000);
			rga.stopRecording();

			Thread.sleep(5000);

			System.exit(0);

		} catch (Exception e) {
			System.exit(1);
		}

	}

	private static void waitAndPrintValues(HidenRGAScannable rga, int[] masses) throws DeviceException,
			InterruptedException {
		rga.waitWhileBusy();
		double[] newValues = (double[]) rga.getPosition();
		int i = 0;
		for (int mass : masses) {
			System.out.println(String.format("%d: %.3f", mass, newValues[i]));
			i++;
		}
		System.out.println("");
	}
}
