package uk.ac.diamond.daq.beamline.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public abstract class AbstractCSVLookupService implements ScannablePositionLookupService {

	private File csvFile;
	private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withHeader();

	protected AbstractCSVLookupService (File csvFile) {
		this.csvFile = csvFile;
	}

	protected abstract boolean rowMatches(Object value, String[] row);

	private String[] readRecord(List<String> columns, CSVRecord csvRecord) throws WorkflowException {
		String columnValue;
		String[] row = new String[columns.size()];
		for (int i=0;i<columns.size();i++) {
			try {
				columnValue = csvRecord.get(columns.get(i));
			} catch (IllegalArgumentException e) {
				throw new WorkflowException("No column '" + columns.get(i) + "' found in file '" + csvFile.getName() + "'");
			}

			row[i] = columnValue;
		}

		return row;
	}

	protected String[] readCSVFile(Object value, List<String> columns) throws WorkflowException {
		try (FileReader fileReader = new FileReader(csvFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			CSVParser csvParser = CSVParser.parse(bufferedReader, CSV_FORMAT);

			for (CSVRecord csvRecord : csvParser) {
				String[] row = readRecord(columns, csvRecord);
				if (rowMatches(value, row)) {
					return row;
				}
			}
		} catch (IOException e) {
			throw new WorkflowException("Error reading " + csvFile.getName(), e);
		}
		throw new WorkflowException("No rows matched for value " + value);
	}
}
