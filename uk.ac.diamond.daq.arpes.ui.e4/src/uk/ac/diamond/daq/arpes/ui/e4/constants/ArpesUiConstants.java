package uk.ac.diamond.daq.arpes.ui.e4.constants;

public class ArpesUiConstants {

	public static final String ARPES_LIVE_DATA_UPDATE_TOPIC = "uk/ac/gda/arpes/ui/e4/dataUpdate/live";
	public static final String ARPES_SUM_DATA_UPDATE_TOPIC = "uk/ac/gda/arpes/ui/e4/dataUpdate/sum";
	public static final String ARPES_SWEPT_DATA_UPDATE_TOPIC = "uk/ac/gda/arpes/ui/e4/dataUpdate/swept";

	public static final String ARPES_EXPERIMENT_PERSPECTIVE_E4_ID = "uk.ac.diamond.daq.arpes.ui.e4.experiment";
	public static final String ARPES_SLICING_PERSPECTIVE_E4_ID = "uk.ac.diamond.daq.arpes.ui.e4.slicing";
	public static final String ARPES_I05J_ALIGNMENT_PERSPECTIVE_E4_ID = "uk.ac.gda.beamline.i05-1.perspective.alignment.e4";

	public static final String ARPES_SLICING_PERSPECTIVE_E3_ID = "uk.ac.gda.arpes.perspectives.ArpesSlicingPerspective";

	private ArpesUiConstants() {
	}

	public static String getConstantValue(String name) {
		try {
			return (String) ArpesUiConstants.class.getDeclaredField(name).get(null);
		} catch (Exception e) {
			throw new IllegalArgumentException("Constant value not found: " + name, e);
		}
	}
}
