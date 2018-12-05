package uk.ac.diamond.daq.experiment.ui.driver;

public class CustomDisplacementEditor extends CustomProfileEditor {

	@Override
	String getQuantityName() {
		return "Displacement";
	}

	@Override
	String getQuantityUnits() {
		return "mm";
	}

}
