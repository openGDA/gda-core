package uk.ac.diamond.daq.experiment.ui.driver;

public class CustomLoadEditor extends CustomProfileEditor {

	@Override
	String getQuantityName() {
		return "Load";
	}

	@Override
	String getQuantityUnits() {
		return "N";
	}

}
