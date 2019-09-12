package uk.ac.diamond.daq.beamline.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.beamline.configuration.api.ScannablePositionLookupService;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public abstract class AbstractCSVLookupService implements ScannablePositionLookupService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractCSVLookupService.class);

	private File csvFile;
	private static final CSVFormat csvFormat = CSVFormat.EXCEL.withHeader();

	protected AbstractCSVLookupService (File csvFile) {
		this.csvFile = csvFile;
	}

	protected abstract boolean rowMatches (double value, double[] row);

	private void readRecord (double[] row, List<String> columns, CSVRecord csvRecord) throws WorkflowException {
		String columnValue;
		for (int i=0;i<columns.size();i++) {
			columnValue = "None";
			try {
				columnValue = csvRecord.get(columns.get(i));
				row[i] = Double.parseDouble(columnValue);
			} catch (NumberFormatException e) {
				throw new WorkflowException("Unable to convert column " + columns.get(i)
				+ " value \"" + columnValue + "\" to a number");
			} catch (IllegalArgumentException e) {
				throw new WorkflowException("No column '" + columns.get(i) + "' found in file '" + csvFile.getName() + "'");
			}
		}
	}

	protected double[] readCSVFile (double value, List<String> columns) throws WorkflowException {
		try(FileReader fileReader = new FileReader(csvFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			CSVParser csvParser = CSVParser.parse(bufferedReader, csvFormat);

			double[] row = new double[columns.size()];
			for (CSVRecord csvRecord : csvParser) {
				readRecord(row, columns, csvRecord);
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
