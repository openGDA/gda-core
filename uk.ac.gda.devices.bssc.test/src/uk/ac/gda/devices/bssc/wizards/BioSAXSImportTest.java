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
			.getLogger(BSSCImportWizardPage.class);
	private static final int PLATE_COL_NO = 0;
	private static final int PLATE_ROW_COL_NO = 1;
	private static final int PLATE_COLUMN_COL_NO = 2;
	private static final int SAMPLE_NAME_COL_NO = 3;
	private static final int CONCENTRATION_COL_NO = 4;
	private static final int VISCOSITY_COL_NO = 5;
	private static final int MOLECULAR_WEIGHT_COL_NO = 6;
	private static final int BUFFER_PLATE_COL_NO = 7;
	private static final int BUFFER_ROW_COL_NO = 8;
	private static final int BUFFER_COLUMN_COL_NO = 9;
	private static final int RECOUP_COL_NO = 10;
	private static final int RECOUP_PLATE_COL_NO = 11;
	private static final int RECOUP_ROW_COL_NO = 12;
	private static final int RECOUP_COLUMN_COL_NO = 13;
	private static final int TIME_PER_FRAME_COL_NO = 14;
	private static final int FRAMES_COL_NO = 15;
	private static final int EXPOSURE_TEMP_COL_NO = 16;
	
	private float sampleStorageTemperature = 15;

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
		LocationBean location = new LocationBean();
		location.setPlate(parsePlateCell(platec));
		location.setRow(rowc.getStringCellValue().charAt(0));
		location.setColumn((short) columnc.getNumericCellValue());
		return location;
	}

	@Test
	public void testImport() {
		Workbook wb;
		try {
			wb = WorkbookFactory
					.create(new File(
							"/home/xlw00930/Desktop/TestTemplate.xls"));

			Sheet sheet = wb.getSheetAt(0);

			BSSCSessionBean sessionBean = new BSSCSessionBean();
			sessionBean.setSampleStorageTemperature(sampleStorageTemperature);
			List<TitrationBean> measurements = new ArrayList<TitrationBean>();

			for (Row row : sheet) {

				try {
					TitrationBean tibi = new TitrationBean();

					LocationBean location = locationFromCells(
							row.getCell(PLATE_COL_NO),
							row.getCell(PLATE_ROW_COL_NO),
							row.getCell(PLATE_COLUMN_COL_NO));
					if (!location.isValid())
						throw new Exception("invalid sample location");
					tibi.setLocation(location);

					tibi.setSampleName(row.getCell(SAMPLE_NAME_COL_NO)
							.getStringCellValue());

					location = locationFromCells(
							row.getCell(BUFFER_PLATE_COL_NO),
							row.getCell(BUFFER_ROW_COL_NO),
							row.getCell(BUFFER_COLUMN_COL_NO));
					if (!location.isValid())
						throw new Exception("invalid buffer location");
					tibi.setBufferLocation(location);

					try {
						location = locationFromCells(
								row.getCell(RECOUP_PLATE_COL_NO),
								row.getCell(RECOUP_ROW_COL_NO),
								row.getCell(RECOUP_COLUMN_COL_NO));
						if (!location.isValid())
							location = null;
					} catch (Exception e) {
						location = null;
					}
					tibi.setRecouperateLocation(location);
					tibi.setConcentration(row.getCell(CONCENTRATION_COL_NO)
							.getNumericCellValue());
					tibi.setViscosity(row.getCell(VISCOSITY_COL_NO)
							.getStringCellValue());
					tibi.setMolecularWeight(row
							.getCell(MOLECULAR_WEIGHT_COL_NO)
							.getNumericCellValue());
					tibi.setTimePerFrame(row.getCell(TIME_PER_FRAME_COL_NO)
							.getNumericCellValue());
					tibi.setFrames((int) row.getCell(FRAMES_COL_NO)
							.getNumericCellValue());
					tibi.setExposureTemperature((float) row.getCell(
							EXPOSURE_TEMP_COL_NO).getNumericCellValue());

					measurements.add(tibi);
				} catch (Exception e) {
					logger.debug("row rejected" + row.toString());
//					Assert.fail("row rejected" + row.toString());
				}
			}

			sessionBean.setMeasurements(measurements);
			Assert.assertEquals(sessionBean.getMeasurements().size(), 3);
		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	public void testImportFromMSExcel() {
		Workbook wb;
		try {
			wb = WorkbookFactory
					.create(new File(
							"/home/xlw00930/Desktop/U/BioSAXS Test Spreadsheets/ExportedTemplate.xls"));

			Sheet sheet = wb.getSheetAt(0);

			BSSCSessionBean sessionBean = new BSSCSessionBean();
			sessionBean.setSampleStorageTemperature(sampleStorageTemperature);
			List<TitrationBean> measurements = new ArrayList<TitrationBean>();

			for (Row row : sheet) {

				try {
					TitrationBean tibi = new TitrationBean();

					LocationBean location = locationFromCells(
							row.getCell(PLATE_COL_NO),
							row.getCell(PLATE_ROW_COL_NO),
							row.getCell(PLATE_COLUMN_COL_NO));
					if (!location.isValid())
						throw new Exception("invalid sample location");
					tibi.setLocation(location);

					tibi.setSampleName(row.getCell(SAMPLE_NAME_COL_NO)
							.getStringCellValue());

					location = locationFromCells(
							row.getCell(BUFFER_PLATE_COL_NO),
							row.getCell(BUFFER_ROW_COL_NO),
							row.getCell(BUFFER_COLUMN_COL_NO));
					if (!location.isValid())
						throw new Exception("invalid buffer location");
					tibi.setBufferLocation(location);

					try {
						location = locationFromCells(
								row.getCell(RECOUP_PLATE_COL_NO),
								row.getCell(RECOUP_ROW_COL_NO),
								row.getCell(RECOUP_COLUMN_COL_NO));
						if (!location.isValid())
							location = null;
					} catch (Exception e) {
						location = null;
					}
					tibi.setRecouperateLocation(location);
					tibi.setConcentration(row.getCell(CONCENTRATION_COL_NO)
							.getNumericCellValue());
					tibi.setViscosity(row.getCell(VISCOSITY_COL_NO)
							.getStringCellValue());
					tibi.setMolecularWeight(row
							.getCell(MOLECULAR_WEIGHT_COL_NO)
							.getNumericCellValue());
					tibi.setTimePerFrame(row.getCell(TIME_PER_FRAME_COL_NO)
							.getNumericCellValue());
					tibi.setFrames((int) row.getCell(FRAMES_COL_NO)
							.getNumericCellValue());
					tibi.setExposureTemperature((float) row.getCell(
							EXPOSURE_TEMP_COL_NO).getNumericCellValue());

					measurements.add(tibi);
				} catch (Exception e) {
					logger.debug("row rejected" + row.toString());
//					Assert.fail("row rejected" + row.toString());
				}
			}

			sessionBean.setMeasurements(measurements);
			Assert.assertEquals(sessionBean.getMeasurements().size(), 3);
		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
