package uk.ac.gda.devices.bssc.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.beans.BSSCSessionBean;
import uk.ac.gda.devices.bssc.beans.LocationBean;
import uk.ac.gda.devices.bssc.beans.TitrationBean;

public class BioSAXSImportTest {
	private static final Logger logger = LoggerFactory
			.getLogger(BioSAXSImportTest.class);
	private static final int PLATE_COL_NO = 0;
	private static final int PLATE_ROW_COL_NO = 1;
	private static final int PLATE_COLUMN_COL_NO = 2;
	private static final int SAMPLE_NAME_COL_NO = 3;
	private static final int CONCENTRATION_COL_NO = 4;
	private static final int VISCOSITY_COL_NO = 5;
	private static final int MOLECULAR_WEIGHT_COL_NO = 6;
//	private static final int BUFFER_PLATE_COL_NO = 7;
//	private static final int BUFFER_ROW_COL_NO = 8;
//	private static final int BUFFER_COLUMN_COL_NO = 9;
	private static final int RECOUP_PLATE_COL_NO = 10;
	private static final int RECOUP_ROW_COL_NO = 11;
	private static final int RECOUP_COLUMN_COL_NO = 12;
	private static final int TIME_PER_FRAME_COL_NO = 13;
	private static final int FRAMES_COL_NO = 14;
	private static final int EXPOSURE_TEMP_COL_NO = 15;

//	private float sampleStorageTemperature = 15;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private short parsePlateCell(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return (short) cell.getNumericCellValue();
		}
		short result = 0;
		String str = cell.getStringCellValue();
		for (int i = 0; i < str.length(); i++) {
			if ("I".equalsIgnoreCase(str.substring(i, i + 1)))
				result++;
		}
		return result;
	}

	private LocationBean locationFromCells(Cell platec, Cell rowc, Cell columnc) {
		LocationBean location = new LocationBean(BSSCSessionBean.BSSC_PLATES);
		location.setPlate(parsePlateCell(platec));
		location.setRow(rowc.getStringCellValue().charAt(0));
		location.setColumn((short) columnc.getNumericCellValue());
		return location;
	}

	@Test
	public void testImportFromLibreOffice() {
		try {
			Workbook wb = WorkbookFactory.create(new File(
					"/home/xlw00930/Desktop/TestTemplate.xls"));
			testImport(wb);
		} catch (InvalidFormatException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Test
	public void testImportFromExcel() {
		try {
			Workbook wb = WorkbookFactory
					.create(new File(
							"/home/xlw00930/Desktop/U/BioSAXS Test Spreadsheets/ExportedTemplate.xls"));
			testImport(wb);
		} catch (InvalidFormatException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private void testImport(Workbook wb) {
		Row spreadSheetRow = null;

		Sheet sheet = wb.getSheetAt(0);

		BSSCSessionBean sessionBean = new BSSCSessionBean();
		List<TitrationBean> measurements = new ArrayList<TitrationBean>();

		int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();

		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {

			try {
				TitrationBean tibi = new TitrationBean();
				spreadSheetRow = sheet.getRow(rowIndex + 1);

				LocationBean plateLocation = tibi.getLocation();
//				LocationBean bufferLocation = tibi.getBufferLocation();
				LocationBean recoupLocation = tibi.getRecouperateLocation();

				plateLocation = locationFromCells(
						spreadSheetRow.getCell(PLATE_COL_NO),
						spreadSheetRow.getCell(PLATE_ROW_COL_NO),
						spreadSheetRow.getCell(PLATE_COLUMN_COL_NO));

				if (!plateLocation.isValid())
					throw new Exception("invalid sample location");
				tibi.setLocation(plateLocation);
				Assert.assertEquals(plateLocation.getPlate(),
						parsePlateCell(spreadSheetRow.getCell(PLATE_COL_NO)));
				Assert.assertEquals(String.valueOf(plateLocation.getRow()),
						spreadSheetRow.getCell(PLATE_ROW_COL_NO)
								.getStringCellValue());
				Assert.assertEquals(plateLocation.getColumn(), Math
						.round(spreadSheetRow.getCell(PLATE_COLUMN_COL_NO)
								.getNumericCellValue()));

				tibi.setSampleName(spreadSheetRow.getCell(SAMPLE_NAME_COL_NO)
						.getStringCellValue());
				Assert.assertEquals(tibi.getSampleName(), spreadSheetRow
						.getCell(SAMPLE_NAME_COL_NO).getStringCellValue());

//				bufferLocation = locationFromCells(
//						spreadSheetRow.getCell(BUFFER_PLATE_COL_NO),
//						spreadSheetRow.getCell(BUFFER_ROW_COL_NO),
//						spreadSheetRow.getCell(BUFFER_COLUMN_COL_NO));
//				if (!bufferLocation.isValid())
//					throw new Exception("invalid buffer location");
//				tibi.setBufferLocation(bufferLocation);
//				Assert.assertEquals(bufferLocation.getPlate(),
//						parsePlateCell(spreadSheetRow
//								.getCell(BUFFER_PLATE_COL_NO)));

//				Assert.assertEquals(String.valueOf(bufferLocation.getRow()),
//						spreadSheetRow.getCell(BUFFER_ROW_COL_NO)
//								.getStringCellValue());
//				Assert.assertEquals(bufferLocation.getColumn(), Math
//						.round(spreadSheetRow.getCell(BUFFER_COLUMN_COL_NO)
//								.getNumericCellValue()));

				try {
					recoupLocation = locationFromCells(
							spreadSheetRow.getCell(RECOUP_PLATE_COL_NO),
							spreadSheetRow.getCell(RECOUP_ROW_COL_NO),
							spreadSheetRow.getCell(RECOUP_COLUMN_COL_NO));
					if (!recoupLocation.isValid())
						recoupLocation = null;
				} catch (Exception e) {
					recoupLocation = null;
				}
				tibi.setRecouperateLocation(recoupLocation);
				if (recoupLocation != null) {
					Assert.assertEquals(recoupLocation.getPlate(),
							parsePlateCell(spreadSheetRow
									.getCell(RECOUP_PLATE_COL_NO)));
					Assert.assertEquals(
							String.valueOf(recoupLocation.getRow()),
							spreadSheetRow.getCell(RECOUP_ROW_COL_NO)
									.getStringCellValue());
					Assert.assertEquals(recoupLocation.getColumn(), Math
							.round(spreadSheetRow.getCell(RECOUP_COLUMN_COL_NO)
									.getNumericCellValue()));
				}

				tibi.setConcentration(spreadSheetRow.getCell(
						CONCENTRATION_COL_NO).getNumericCellValue());
				Assert.assertEquals(
						String.valueOf(tibi.getConcentration()),
						String.valueOf(spreadSheetRow.getCell(
								CONCENTRATION_COL_NO).getNumericCellValue()));

				tibi.setViscosity(spreadSheetRow.getCell(VISCOSITY_COL_NO)
						.getStringCellValue());
				Assert.assertEquals(tibi.getViscosity(), spreadSheetRow
						.getCell(VISCOSITY_COL_NO).getStringCellValue());

				tibi.setMolecularWeight(spreadSheetRow.getCell(
						MOLECULAR_WEIGHT_COL_NO).getNumericCellValue());
				Assert.assertEquals(
						String.valueOf(tibi.getMolecularWeight()),
						String.valueOf(spreadSheetRow.getCell(
								MOLECULAR_WEIGHT_COL_NO).getNumericCellValue()));

				tibi.setTimePerFrame(spreadSheetRow.getCell(
						TIME_PER_FRAME_COL_NO).getNumericCellValue());
				Assert.assertEquals(
						String.valueOf(tibi.getTimePerFrame()),
						String.valueOf(spreadSheetRow.getCell(
								TIME_PER_FRAME_COL_NO).getNumericCellValue()));

				tibi.setFrames((int) spreadSheetRow.getCell(FRAMES_COL_NO)
						.getNumericCellValue());
				Assert.assertEquals(
						String.valueOf(tibi.getFrames()),
						String.valueOf(Math.round(spreadSheetRow.getCell(
								FRAMES_COL_NO).getNumericCellValue())));

				tibi.setExposureTemperature((float) spreadSheetRow.getCell(
						EXPOSURE_TEMP_COL_NO).getNumericCellValue());
				Assert.assertEquals(
						String.valueOf(tibi.getExposureTemperature()),
						String.valueOf(spreadSheetRow.getCell(
								EXPOSURE_TEMP_COL_NO).getNumericCellValue()));

				measurements.add(tibi);
			} catch (Exception e) {
				logger.debug("row rejected" + spreadSheetRow.toString());
				Assert.fail("row rejected" + spreadSheetRow.toString());
			}
		}

		sessionBean.setMeasurements(measurements);
		Assert.assertEquals(sessionBean.getMeasurements().size(), 3);
	}
}
