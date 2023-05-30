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

	/**
	 * Return true if the given row of spectrometer will be moved for scans of this colour type
	 * @param rowNum (0 or 1)
	 * @return true if the row is active
	 */
	public boolean useRow(int rowNum) {
		return useRow1() && rowNum==0 || useRow2() && rowNum == 1;
	}

	/**
	 *
	 * @return True if row 1 of the spectrometer will be active during a scan of this colour type
	 */
	public boolean useRow1() {
		return this == ONE_COLOUR_ROW1 || this == TWO_COLOUR || this == ONE_COLOUR;
	}

	/**
	 *
	 * @return True if row 2 of the spectrometer will be active during a scan of this colour type
	 */
	public boolean useRow2() {
		return this == ONE_COLOUR_ROW2 || this == TWO_COLOUR || this == ONE_COLOUR;
	}
}