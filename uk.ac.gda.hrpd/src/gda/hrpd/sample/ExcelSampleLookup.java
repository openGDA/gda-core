/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.hrpd.sample;

import static gda.hrpd.sample.ExcelSampleLookup.Column.COLLECTION_TIME;
import static gda.hrpd.sample.ExcelSampleLookup.Column.COMMENT;
import static gda.hrpd.sample.ExcelSampleLookup.Column.DELTA;
import static gda.hrpd.sample.ExcelSampleLookup.Column.DIRECTORY;
import static gda.hrpd.sample.ExcelSampleLookup.Column.EXTRAS;
import static gda.hrpd.sample.ExcelSampleLookup.Column.NAME;
import static gda.hrpd.sample.ExcelSampleLookup.Column.POSITION;
import static gda.hrpd.sample.ExcelSampleLookup.Column.REBINNING;
import static gda.hrpd.sample.ExcelSampleLookup.Column.SCAN;
import static gda.hrpd.sample.ExcelSampleLookup.Column.SPIN;
import static gda.hrpd.sample.ExcelSampleLookup.Column.SPOS;
import static gda.hrpd.sample.ExcelSampleLookup.Column.TITLE;
import static gda.hrpd.sample.ExcelSampleLookup.Column.VISIT;
import static gda.hrpd.sample.SampleDefinitionException.missing;
import static gda.hrpd.sample.SampleDefinitionException.parseError;
import static java.util.stream.Collectors.toList;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC;
import static org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING;
import static org.apache.poi.ss.usermodel.Row.RETURN_BLANK_AS_NULL;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.hrpd.sample.api.SampleMetadata;
import gda.hrpd.sample.api.ScanDescription;
import gda.hrpd.sample.scans.MacScan;
import gda.hrpd.sample.scans.PsdScan;

public class ExcelSampleLookup extends BaseSampleLookup {
	private static final Logger logger = LoggerFactory.getLogger(ExcelSampleLookup.class);

	protected enum Column {
		POSITION("Position"),
		NAME("Name"),
		TITLE("Title"),
		COMMENT("Comment"),
		DIRECTORY("Subdirectory"),
		VISIT("Visit"),
		SCAN("Scan Type"),
		COLLECTION_TIME("Collection Time"),
		DELTA("Delta"),
		SPIN("Spin"),
		SPOS("spos"),
		REBINNING("Rebinning Sizes"),
		EXTRAS("Extra Scannables");
		private String display;
		private Column(String display) {
			this.display = display;
		}
		@Override
		public String toString() {
			return display + "(" + (char)('A'+ordinal()) + ")";
		}
	}

	/** The filename of the excel file to load */
	private final String filename;
	/** The name used for samples when none is given - will have the sample number appended */
	private final String fallback;

	public ExcelSampleLookup(String file) throws IOException {
		filename = file;
		fallback = getFallbackName();
		Loader loader = filename.endsWith(".xls") ? HSSFWorkbook::new : XSSFWorkbook::new;
		Sheet sheet = loader.readWorkbook(file).getSheetAt(0);
		samples = IntStream.rangeClosed(sheet.getFirstRowNum() + 1, sheet.getLastRowNum())
				.mapToObj(sheet::getRow)
				.filter(row -> row.getFirstCellNum() >= 0)
				.map(this::getSample)
				.collect(toList());
	}

	private String getFallbackName() {
		String name = new File(filename).getName();
		int ext = name.lastIndexOf('.');
		return ext == -1 ? name : name.substring(0, ext);
	}

	@Override
	public String toString() {
		return "ExcelSampleData[filename="+filename+"]";
	}

	private SampleMetadata getSample(Row row) {
		return new SampleMetadataBean(new ExcelSampleMetadata(row));
	}

	private class ExcelSampleMetadata implements SampleMetadataBean.Info {
		private static final String SPIN_ON = "on";
		private Row row;

		public ExcelSampleMetadata(Row row) {
			this.row = row;
		}

		private <T> T checked(Supplier<T> source, Column column) {
			try {
				return source.get();
			} catch (Exception e) {
				throw parseError(row.getRowNum(), column.toString(), e);
			}
		}

		@Override
		public int getCarouselPosition() {
			return getCell(POSITION, CELL_TYPE_NUMERIC)
					.map(c -> checked(c::getNumericCellValue, POSITION))
					.map(Double::intValue)
					.orElseThrow(() -> missing(row.getRowNum(), POSITION.toString()));
		}

		@Override
		public String getName() {
			return getString(NAME, String.format("%s_%03d", fallback, getCarouselPosition()));
		}

		@Override
		public String getTitle() {
			return getString(TITLE, "");
		}

		@Override
		public String getComment() {
			return getString(COMMENT, "");
		}

		@Override
		public String getDirectory() {
			return getString(DIRECTORY, "");
		}

		@Override
		public String getVisit() {
			return getString(VISIT, getDefaultVisit());
		}

		@Override
		public ScanDescription getScan() {
			String type = getString(SCAN, "");
			ScanDescription scan;
			if (type.equalsIgnoreCase(MAC)) {
				scan = new MacScan(getCollectionTime(), getRebinningSizes(), getSpin(), getSpos(), new HashMap<>(), getExtras());
			} else if (type.equalsIgnoreCase(PSD)) {
				double[] delta = getDelta();
				if (delta.length == 1) {
					scan = new PsdScan(getCollectionTime(), delta[0], getSpin(), getSpos(), new HashMap<>(), getExtras());
				} else {
					// delta will be length 3 here by check in getDelta
					scan = new PsdScan(getCollectionTime(), delta[0], delta[1], delta[2], getSpin(), getSpos(), new HashMap<>(), getExtras());
				}
			} else if (type.isEmpty()){
				throw missing(row.getRowNum(), "scan");
			} else {
				throw parseError(row.getRowNum(), SCAN.toString(), type + " not recognised");
			}
			return scan;
		}

		private double getSpos() {
			return getCell(SPOS, CELL_TYPE_NUMERIC)
					.map(c -> checked(c::getNumericCellValue, SPOS))
					.orElse(0.0);
		}

		private boolean getSpin() {
			return getString(SPIN, SPIN_ON).equalsIgnoreCase(SPIN_ON);
		}

		public double getCollectionTime() {
			return getCell(COLLECTION_TIME, CELL_TYPE_NUMERIC)
					.map(c -> checked(c::getNumericCellValue, COLLECTION_TIME))
					.orElseThrow(() -> missing(row.getRowNum(), COLLECTION_TIME.toString()));
		}

		public double[] getDelta() {
			double[] values = getDoubleArray(DELTA);
			if (values.length != 1 && values.length != 3) {
				throw parseError(row.getRowNum(), DELTA.toString(), "value must be either \"position\" or \"start, stop, step\")");
			}
			return values;
		}

		public double[] getRebinningSizes() {
			return getDoubleArray(REBINNING);
		}

		private double[] getDoubleArray(Column column) {
			String[] values = getString(column, "").split(",");
			return Stream.of(values)
					.filter(s -> !s.isEmpty())
					.mapToDouble(v -> checked(() -> Double.valueOf(v), column))
					.toArray();
		}

		public Collection<String> getExtras() {
			return Stream.of(getString(EXTRAS, "")
						.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.collect(toList());
		}

		private Optional<Cell> getCell(Column column, int type) {
			Cell cell = row.getCell(column.ordinal(), RETURN_BLANK_AS_NULL);
			if (cell != null)
				try {
					cell.setCellType(type);
				} catch (IllegalStateException e) {
					logger.error("Unexpected cell type", e);
					throw parseError(row.getRowNum(), column.toString(), "Unexpected cell type");
				}
			return Optional.ofNullable(cell);
		}

		private String getString(Column column, String fallback) {
			return getCell(column, CELL_TYPE_STRING)
					.map(Cell::getStringCellValue)
					.orElse(fallback);
		}
	}

	@FunctionalInterface
	public static interface Loader {
		public Workbook readWorkbook(InputStream input) throws IOException;

		public default Workbook readWorkbook(String filename) throws IOException {
			try (InputStream in = Files.newInputStream(Paths.get(filename))) {
				return readWorkbook(in);
			}
		}
	}
}
