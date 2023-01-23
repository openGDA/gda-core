package uk.ac.gda.beans.exafs;

import java.util.stream.Stream;

/**
 * Enum containing options for the type of XES 'colour scan' that can be done.
 * i.e. energies to be used for each row of the spectrometer during a scan.
 *
 * <li> ONE_COLOUR --> both spectrometer rows scan the same energies
 * <li> ONE_COLOUR_ROW1 --> scan only 1 row of spectrometer
 * <li> ONE_COLOUR_ROW1 --> scan only 2 row of spectrometer
 * <li> TWO_COLOUR --> both spectrometer rows scan different energies
 */
public enum ScanColourType {
	ONE_COLOUR(0, "One colour (both rows)"),
	ONE_COLOUR_ROW1(1, "One colour (row 1)"),
	ONE_COLOUR_ROW2(2, "One colour (row 2)"),
	TWO_COLOUR(3, "Two colour");

	private final String description;
	private final int index;

	private ScanColourType(int index, String description) {
		this.index = index;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Return {@link ScanColourType} for given index value
	 * @param index
	 * @return ScanColourType
	 */
	public static ScanColourType fromIndex(int index) {
		return Stream.of(values())
				.filter(c -> c.getIndex() == index)
				.findFirst()
				.orElse(null);
	}

	public int getIndex() {
		return index;
	}

	public boolean useRow1Controls() {
		return this == ONE_COLOUR_ROW1 || this == TWO_COLOUR || this == ONE_COLOUR;
	}

	public boolean useRow2Controls() {
		return this == ONE_COLOUR_ROW2 || this == TWO_COLOUR;
	}
}