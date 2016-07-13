package uk.ac.diamond.daq.detectors.vgscienta.electronanalyser.api;

public enum DA30LensMode {
	TRANSMISSION("Transmission"),
	A14_08("A14_08"),
	A7_08("A7_08"),
	A30_08("A30_08"),
	DA30_01("DA30_01"),
	DA30_08("DA30_08"),
	DA14_01("DA14_01"),
	DA14_08("DA14_08"),
	DA7_08("DA7_08"),
	DA30Q_01("DA7_08"),
	DA30Q_08("DA30Q_08"),
	A14_01("A14_01"),
	A30_01("A30_01");

	private String name;

	DA30LensMode(String name) {
		this.name= name;
	}

	@Override
	public String toString() {
		return name;
	}

}
