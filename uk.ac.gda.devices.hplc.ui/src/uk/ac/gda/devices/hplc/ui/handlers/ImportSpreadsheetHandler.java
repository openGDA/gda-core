package uk.ac.gda.devices.hplc.ui.handlers;

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
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.hatsaxs.HatsaxsUtils;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;
import uk.ac.gda.devices.hplc.beans.HplcBean;
import uk.ac.gda.devices.hplc.beans.HplcSessionBean;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class ImportSpreadsheetHandler implements IHandler {
	private Logger logger = LoggerFactory.getLogger(ImportSpreadsheetHandler.class);
	private static final int ROW_COL = 0;
	private static final int COLUMN_COL = 1;
	private static final int SAMPLE_NAME_COL = 2;
	private static final int CONCENTRATION_COL = 3;
	private static final int MOLECULAR_WEIGHT_COL = 4;
	private static final int BUFFERS_COL = 5;
	private static final int TIME_PER_FRAME_COL = 6;
	private static final int COMMENT_COL = 7;
	private static final int VISIT_COL = 8;
	private static final int USERNAME_COL = 9;

	private static final int DEFAULT_PLATE = 1;
	
	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		fd.setText("Open");
		fd.setFilterPath(System.getProperty("user.home"));
		String[] filters = new String[] {"*.xls"};
		fd.setFilterExtensions(filters);
		String selected = fd.open();
		if (selected == null) { return null; }
		
		File fileToOpen = new File(selected);
		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			int i = 0;
			try {
				Workbook wb = WorkbookFactory.create(fileToOpen);
				Sheet sheet = wb.getSheetAt(0);

				HplcSessionBean sessionBean = new HplcSessionBean();	
				List<HplcBean> measurements = new ArrayList<HplcBean>();

				for (i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
					Row row = sheet.getRow(i);
					HplcBean hb = new HplcBean();
	
					LocationBean location = locationFromCells(DEFAULT_PLATE, row.getCell(ROW_COL), row.getCell(COLUMN_COL));
					if (!location.isValid())
						throw new Exception("invalid sample location");
					hb.setLocation(location);
					hb.setSampleName(row.getCell(SAMPLE_NAME_COL).getStringCellValue());
					hb.setBuffers(row.getCell(BUFFERS_COL).getStringCellValue());
					hb.setComment(row.getCell(COMMENT_COL).getStringCellValue());
					hb.setConcentration(row.getCell(CONCENTRATION_COL).getNumericCellValue()); 
					hb.setMolecularWeight(row.getCell(MOLECULAR_WEIGHT_COL).getNumericCellValue());
					hb.setTimePerFrame(row.getCell(TIME_PER_FRAME_COL).getNumericCellValue());
					hb.setMode(HplcBean.DEFAULT_HPLC_MODE);
					
					Cell visit = row.getCell(VISIT_COL, Row.RETURN_BLANK_AS_NULL);
					if (visit != null) {
						hb.setVisit(visit.getStringCellValue());
					}
					
					Cell username = row.getCell(USERNAME_COL, Row.RETURN_BLANK_AS_NULL);
					if (username != null) {
						hb.setUsername(username.getStringCellValue());
					}
	
					measurements.add(hb);
				}

				sessionBean.setMeasurements(measurements);

				// Need to convert file to .hplc and put in default location in the visit directory
				String spreadSheetFileName = fileToOpen.getName().substring(0, fileToOpen.getName().lastIndexOf('.'));
				File nativeFile = HatsaxsUtils.getHplcFileFromName(spreadSheetFileName);
				
				// if file exists then create a new instance of it with an increment (i.e. TestTemplate.biosaxs will be opened as TestTemplate-1.biosaxs)
				int fileIndex = 0;
				while (nativeFile.exists())
				{
					fileIndex++;
					nativeFile = HatsaxsUtils.getHplcFileFromName(spreadSheetFileName + "-" + fileIndex); 
				}
				
				XMLHelpers.writeToXML(HplcSessionBean.mappingURL, sessionBean, nativeFile);
				IFileStore hplcFileStore = EFS.getLocalFileSystem().getStore(nativeFile.toURI());
				IDE.openEditorOnFileStore(page, hplcFileStore);
			} catch (PartInitException e) {
				logger.error("PartInitException opening editor", e);
			} catch (InvalidFormatException e1) {
				logger.error("InvalidFormatException creating Workbook", e1);
			} catch (IOException e1) {
				MessageDialog.openError(
						new Shell(Display.getCurrent()),
						"Could not read .xls file",
						"Is the file valid?");
				logger.error("Could not read workbook", e1);
			} catch (IllegalArgumentException iae) {
				logger.debug("Row rejected", iae);
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Could not import spreadsheet", String.format("Error on row %d\n%s",i-1, iae.getMessage()));
			} catch (Exception e) {
				logger.error("Exception writing to xml", e);
			}
		}
		return null;
	}

	private LocationBean locationFromCells(int 	platec, Cell rowc, Cell columnc) {
		LocationBean location = new LocationBean(HplcSessionBean.HPLC_PLATES);
		location.setPlate((short) platec);
		location.setRow(rowc.getStringCellValue().charAt(0));
		location.setColumn((short) columnc.getNumericCellValue());
		return location;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
