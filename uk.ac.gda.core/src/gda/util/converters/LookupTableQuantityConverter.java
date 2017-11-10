/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.util.converters;

import java.util.ArrayList;

import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.Unit;
import org.springframework.util.StringUtils;

import gda.function.ColumnDataFile;
import gda.function.InterpolationFunction;
import gda.function.InterpolationWithoutExtrapolationFunction;

/**
 * Class to perform conversion between a Source and Target quantity using a lookup table
 *
 * @see gda.util.converters.LookupTableConverterHolder
 * @see org.nfunk.jep.JEP
 */
public final class LookupTableQuantityConverter implements IQuantityConverter {
	private final InterpolationFunction interpolateFunctionStoT, interpolateFunctionTtoS;

	private final ColumnDataFile columnDataFile;

	private final int sColumn, tColumn;

	private final ArrayList<Unit<? extends Quantity>> acceptableSourceUnits, acceptableTargetUnits;

	private final String columnDataFileName;

	private final boolean sourceMinIsTargetMax;

	/*
	 * The lookup tables need to be constant in direction in the X values. Normally both X and Y are constant in
	 * direction allowing the same table to be used to go in both direction. There are occasions when a table cannot be
	 * used to go in both directions - eg.the Y values change direction due to there being a 1-1 mapping between X and Y
	 * but not between Y and X. If such cases a coupled converter is used that uses 2 LookupTableQuantityConverter
	 * objects one for ToS and the other for ToT. We use a mode flag to indicate how the object is to be used to control
	 * which checks of the data in the table are performed. For example InterpolationFunction will throw an exception if
	 * the xvalues change direction.
	 */
	private final Mode mode;

	private boolean performTtoS() {
		return (mode == Mode.BOTH_DIRECTIONS) || (mode == Mode.T_TO_S_ONLY);
	}

	private boolean performStoT() {
		return (mode == Mode.BOTH_DIRECTIONS) || (mode == Mode.S_TO_T_ONLY);
	}

	public enum Mode {

		BOTH_DIRECTIONS,

		S_TO_T_ONLY,

		T_TO_S_ONLY
	}

	public static String Mode_Both = "Both";

	public static String Mode_TtoS = "TtoS";

	public static String Mode_StoT = "StoT";

	static public Mode getMode(String modeString) {
		if (modeString == null)
			throw new IllegalArgumentException("LookupTableQuantityConverter.getMode. modeString is null");
		if (modeString.equals(Mode_Both))
			return Mode.BOTH_DIRECTIONS;
		if (modeString.equals(Mode_TtoS))
			return Mode.T_TO_S_ONLY;
		if (modeString.equals(Mode_StoT))
			return Mode.S_TO_T_ONLY;
		throw new IllegalArgumentException("LookupTableQuantityConverter.getMode. Mode is invalid - " + modeString);
	}

	public LookupTableQuantityConverter(String columnDataFileName, boolean filenameIsFull, int sColumn, int tColumn) {
		this(columnDataFileName, filenameIsFull, sColumn, tColumn, Mode.BOTH_DIRECTIONS);
	}

	public LookupTableQuantityConverter(String columnDataFileName, boolean filenameIsFull, int sColumn, int tColumn,
			Mode mode) {
		this(columnDataFileName, filenameIsFull, sColumn, tColumn, mode, true);
	}

	public LookupTableQuantityConverter(String columnDataFileName, boolean filenameIsFull, int sColumn, int tColumn,
			Mode mode, boolean extrapolate) {
		this.sColumn = sColumn;
		this.tColumn = tColumn;
		this.mode = mode;
		this.columnDataFile = new ColumnDataFile();
		this.columnDataFileName = columnDataFileName;
		columnDataFile.setFilename(columnDataFileName, filenameIsFull);

		try {
			columnDataFile.configure();
		} catch (Exception exception) {
			throw new RuntimeException("Could not create LookupTableQuantityConverter because configuration of its ColumnDataFile (using file " + StringUtils.quote(columnDataFileName) + ") failed", exception);
		}

		try {
			// to do if the x values are the same then duplicate the first
			// and last so that conversion of min and max target gives min
			// and max source.
			if (performStoT()) {
				if (extrapolate) {
					interpolateFunctionStoT = new InterpolationFunction(columnDataFile.getColumn(sColumn), columnDataFile
							.getColumn(tColumn), columnDataFile.getColumnUnits(sColumn), columnDataFile
							.getColumnUnits(tColumn));
				} else {
					interpolateFunctionStoT = new InterpolationWithoutExtrapolationFunction(columnDataFile.getColumn(sColumn), columnDataFile
							.getColumn(tColumn), columnDataFile.getColumnUnits(sColumn), columnDataFile
							.getColumnUnits(tColumn));
				}
			} else
				interpolateFunctionStoT = null;

			if (performTtoS()) {
				if (extrapolate) {
					interpolateFunctionTtoS = new InterpolationFunction(columnDataFile.getColumn(tColumn), columnDataFile
							.getColumn(sColumn), columnDataFile.getColumnUnits(tColumn), columnDataFile
							.getColumnUnits(sColumn));
				} else {
					interpolateFunctionTtoS = new InterpolationWithoutExtrapolationFunction(columnDataFile.getColumn(tColumn), columnDataFile
							.getColumn(sColumn), columnDataFile.getColumnUnits(tColumn), columnDataFile
							.getColumnUnits(sColumn));
				}

				double firstT = columnDataFile.getColumn(tColumn)[0];
				double lastT = columnDataFile.getColumn(tColumn)[columnDataFile.getColumn(tColumn).length - 1];
				double firstS = columnDataFile.getColumn(sColumn)[0];
				double lastS = columnDataFile.getColumn(sColumn)[columnDataFile.getColumn(sColumn).length - 1];
				sourceMinIsTargetMax = (firstT - lastT > 0) ^ (firstS - lastS > 0);

			} else {
				interpolateFunctionTtoS = null;
				sourceMinIsTargetMax = false;
			}

			acceptableSourceUnits = new ArrayList<Unit<? extends Quantity>>();
			acceptableSourceUnits.add(columnDataFile.getColumnUnits(sColumn));

			acceptableTargetUnits = new ArrayList<Unit<? extends Quantity>>();
			acceptableTargetUnits.add(columnDataFile.getColumnUnits(tColumn));
		} catch (ArrayIndexOutOfBoundsException exception) {
			throw new IllegalArgumentException(
					"LookupTableQuantityConverter.LookupTableQuantityConverter: Error accessing data from ColumnDataFile - check the column indices are correct. "
							+ toString(), exception);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("LookupTableQuantityConverter.LookupTableQuantityConverter: " + exception.getMessage(), exception);
		} catch (Exception exception) {
			throw new IllegalArgumentException(
					"LookupTableQuantityConverter.LookupTableQuantityConverter: Error accessing data from ColumnDataFile. "
							+ toString(), exception);
		}

	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableSourceUnits() {
		return acceptableSourceUnits;
	}

	@Override
	public ArrayList<Unit<? extends Quantity>> getAcceptableTargetUnits() {
		return acceptableTargetUnits;
	}

	@Override
	public Quantity toSource(Quantity target) throws Exception {
		if (!performTtoS()) {
			throw new UnsupportedConversionException(
					"LookupTableQuantityConverter.toSource: Mode does not allow T→S conversion. "
							+ this.toString());
		}
		final Unit<? extends Quantity> acceptableUnits = getAcceptableTargetUnits().get(0);
		if (!target.getUnit().isCompatible(acceptableUnits)) {
			throw new InvalidUnitsException("LookupTableQuantityConverter.toSource: target units ("
					+ target.getUnit() + ") are not compatible with acceptable units (" + acceptableUnits + "). "
					+ this.toString());
		}
		try {
			return interpolateFunctionTtoS.evaluate(target);
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException &&
					e.getMessage().startsWith("Input value")) {
				throw e;
			}
			throw new Exception(toString() + " - exception " + e.getMessage(), e);
		}
	}

	@Override
	public Quantity toTarget(Quantity source) throws Exception {
		if (!performStoT()) {
			throw new UnsupportedConversionException(
					"LookupTableQuantityConverter.toTarget: Mode does not allow S→T conversion. "
							+ this.toString());
		}
		final Unit<? extends Quantity> acceptableUnits = getAcceptableSourceUnits().get(0);
		if (!source.getUnit().isCompatible(acceptableUnits)) {
			throw new InvalidUnitsException("LookupTableQuantityConverter.toTarget: source units ("
					+ source.getUnit() + ") are not compatible with acceptable units (" + acceptableUnits + "). "
					+ this.toString());
		}
		try {
			return interpolateFunctionStoT.evaluate(source);
		} catch (Exception e) {
			if (e instanceof IllegalArgumentException &&
					e.getMessage().startsWith("Input value")) {
				throw e;
			}
			throw new Exception(toString() + " - exception " + e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return "LookupTableQuantityConverter using details in " + columnDataFile.getFilename() + ". sColumn=" + sColumn
				+ " tColumn=" + tColumn + " mode = " + mode;
	}

	public String getColumnDataFileName() {
		return columnDataFileName;
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return sourceMinIsTargetMax;
	}

	@Override
	public boolean handlesStoT() {
		return performStoT();
	}

	@Override
	public boolean handlesTtoS() {
		return performTtoS();
	}

}
