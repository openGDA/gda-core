/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.hrpd.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;

/**
 * This class provides access to a user specified Excel file and methods to access the contents of the workbook, such as
 * sheet, row and cell.
 */
public class ExcelWorkbook {
	private static final Logger logger = LoggerFactory.getLogger(ExcelWorkbook.class);

	private String filename;
	private FileInputStream fileInput;
	private POIFSFileSystem fs;
	private HSSFWorkbook wb;

	private boolean writeable = false;
	private boolean readable = false;


	/**
	 *
	 */
	public ExcelWorkbook() {

	}

	/**
	 * Constructor - opens a file input stream object, creates a workbook for accessing to the content of the Excel
	 * file.
	 *
	 * @param filename
	 * @throws InstantiationException
	 */
	public ExcelWorkbook(String filename) throws InstantiationException {
		if (filename != null) {
			this.filename = filename;
		} else {
			throw new InstantiationException("Filename is null");
		}
		if (fileInput != null) {
			// close existing file input stream to ensure only one stream available
			try {
				fileInput.close();
			} catch (IOException e) {
				throw new InstantiationException("Can not close existing file input stream");
			}
		}

		// check file permission
		File file = new File(this.filename);

		readable = file.canRead();
		if (readable) {
			logger.info("GDA can read from the file: {}", this.filename);
		} else {
			logger.warn("GDA cannot read from the file: {}. Please make sure file permission is correct for GDA to read.", this.filename);
		}

		writeable = file.canWrite();
		if (writeable) {
			logger.info("GDA can write to the file: {}", this.filename);
		} else {
			logger.warn("GDA cannot write to the file: {}. Please make sure file permission is correct for GDA to write.", this.filename);
		}

		// open a new file input stream
		try {
			fileInput = new FileInputStream(this.filename);
		} catch (FileNotFoundException e) {
			logger.error("Cannot find sample information file {}.", filename);
			JythonServerFacade.getInstance().print("please specify the URL for file.");
			throw new InstantiationException(e.getMessage());
		}
		// open the workbook
		if (fileInput != null) {
			try {
				fs = new POIFSFileSystem(fileInput);
				wb = new HSSFWorkbook(fs);
			} catch (IOException e) {
				logger.error("failed to open Excel file {}.", this.filename);
				throw new InstantiationException("can not open Excel file: " + this.filename);
			}
		}
	}

	/**
	 * gets a spreadsheet at the specified index (the index starts from 0)
	 *
	 * @param index
	 * @return HSSFSheet
	 */
	public HSSFSheet getSheetAt(int index) {
		return wb.getSheetAt(index);
	}

	/**
	 * gets the spreadsheet for the specified name
	 *
	 * @param name
	 * @return HSSFSheet
	 */
	public HSSFSheet getSheet(String name) {
		return wb.getSheet(name);
	}

	/**
	 * add a new spreadsheet to the workbook
	 *
	 * @return HSSFSheet
	 * @throws IOException
	 */
	public HSSFSheet createSheet() throws IOException {
		if (!writeable) {
			logger.error("Cannot create a new sheet in file {}.", this.filename);
			throw new IOException("Cannot write to file {}." + this.filename);
		}
		return wb.createSheet();
	}

	/**
	 * creates a new sheet with specified name in the workbook
	 *
	 * @param name
	 * @return HSSFSheet
	 * @throws IOException
	 */
	public HSSFSheet createSheet(String name) throws IOException {
		if (!writeable) {
			logger.error("Cannot create a new sheet in file {}.", this.filename);
			throw new IOException("Cannot write to file {}." + this.filename);
		}
		return wb.createSheet(name);
	}

	/**
	 * returns the specified row from the specified sheet
	 *
	 * @param sheet
	 * @param rownum
	 * @return HSSFRow
	 */
	public HSSFRow getRow(HSSFSheet sheet, int rownum) {
		return sheet.getRow(rownum);
	}

	/**
	 * creates the specified row in the specified sheet.
	 *
	 * @param sheet
	 * @param rownum
	 * @return HSSFRow
	 * @throws IOException
	 */
	public HSSFRow createRow(HSSFSheet sheet, int rownum) throws IOException {
		if (!writeable) {
			logger.error("Cannot create a new row in file {}.", this.filename);
			throw new IOException("Cannot write to file {}." + this.filename);
		}
		return sheet.createRow(rownum);
	}

	/**
	 * returns the specified cell from the specified row.
	 *
	 * @param row
	 * @param cellnum
	 * @return HSSFCell
	 */
	public HSSFCell getCell(HSSFRow row, int cellnum) {
		return row.getCell(cellnum);
	}

	/**
	 * creates the specified cell in the specified row.
	 *
	 * @param row
	 * @param cellnum
	 * @return HSSFCell
	 * @throws IOException
	 */
	public HSSFCell createCell(HSSFRow row, short cellnum) throws IOException {
		if (!writeable) {
			logger.error("Cannot create a new cell in file {}.", this.filename);
			throw new IOException("Cannot write to file {}." + this.filename);
		}
		return row.createCell(cellnum);
	}

	/**
	 * gets value from the specified cell and return it as String.
	 *
	 * @param cell
	 * @return value from cell as a String
	 */
	public String getCellValue(HSSFCell cell) {
		// If the cell is null return an empty string
		if (cell == null) {
			return "";
		}

		String value = null;
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			value = cell.getRichStringCellValue().toString();
		} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			value = String.valueOf(cell.getNumericCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			value = "        ";
		} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			value = String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
			value = String.valueOf(cell.getErrorCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			value = cell.getCellFormula();
		}
		return value;
	}

	/**
	 * modify if exist, or create if not exist, a cell in the specified row at specified position with the specified
	 * value.
	 *
	 * @param row
	 * @param column
	 * @param value
	 * @throws IOException
	 */
	public void setCellValue(HSSFRow row, int column, String value) throws IOException {
		HSSFCell cell = row.getCell(column);
		if (cell == null)
			// add more cell to the row
			if (!writeable) {
				logger.error("Cannot create a new cell in file {}.", this.filename);
				throw new IOException("Cannot write to file {}." + this.filename);
			}

			cell = row.createCell((short) column);
		cell.setCellValue(new HSSFRichTextString(value));
	}

	/**
	 * modify if exist, or create if not exist, a cell in the specified row at specified position with the specified
	 * value.
	 *
	 * @param row
	 * @param column
	 * @param value
	 * @throws IOException
	 */
	public void setCellValue(HSSFRow row, int column, double value) throws IOException {
		HSSFCell cell = row.getCell(column);
		if (cell == null)
			if (!writeable) {
				logger.error("Cannot create a new sheet in file {}.", this.filename);
				throw new IOException("Cannot write to file {}." + this.filename);
			}

			// add more cell to the row
			cell = row.createCell((short) column);
		cell.setCellValue(value);
	}

	/**
	 * modify if exist, or create if not exist, a cell in the specified row at specified position with the specified
	 * value.
	 *
	 * @param row
	 * @param column
	 * @param value
	 * @throws IOException
	 */
	public void setCellValue(HSSFRow row, int column, boolean value) throws IOException {
		HSSFCell cell = row.getCell(column);
		if (cell == null)
			if (!writeable) {
				logger.error("Cannot create a new sheet in file {}.", this.filename);
				throw new IOException("Cannot write to file {}." + this.filename);
			}

			// add more cell to the row
			cell = row.createCell((short) column);
		cell.setCellValue(value);
	}

	/**
	 * modify if exist, or create if not exist, a cell in the specified row at specified position with the specified
	 * value.
	 *
	 * @param row
	 * @param column
	 * @param date
	 * @throws IOException
	 */
	public void setCellValue(HSSFRow row, int column, Date date) throws IOException {
		HSSFCell cell = row.getCell(column);
		if (cell == null) {
			if (!writeable) {
				logger.error("Cannot create a new sheet in file {}.", this.filename);
				throw new IOException("Cannot write to file {}." + this.filename);
			}

			// we style the cell as a date (and time). It is important to
			// create a new cell style from the workbook otherwise you can end
			// up modifying the built in style and effecting not only this cell
			// but other cells.
			HSSFCellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
			cell = row.createCell((short) column);
			cell.setCellValue(date);
			cell.setCellStyle(cellStyle);
		} else {
			cell.setCellValue(date);
		}
	}

	/**
	 * writes the workbook data back to the Excel file
	 *
	 * @param filename
	 * @throws IOException
	 */
	public void write(String filename) throws IOException {
		if (!writeable) {
			logger.error("Cannot write to file {}.", this.filename);
			throw new IOException("Cannot write to file {}." + this.filename);
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(filename);
			wb.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			logger.error("Cannot find sample information file {}.", filename);
			JythonServerFacade.getInstance().print("please specify the URL for the sample information file.");
		} catch (IOException e) {
			logger.error("Cannot write the file output stream", e);
			e.printStackTrace();
		}
	}

	/**
	 * closes the file input stream
	 */
	public void close() {
		if (fileInput != null) {
			try {
				fileInput.close();
				wb = null;
				fs = null;
			} catch (IOException e) {
				logger.error("Cannot close the file input stream", e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * gets the workbook
	 *
	 * @return HSSFWorkbook
	 */
	public HSSFWorkbook getWorkbook() {
		return wb;
	}
}
