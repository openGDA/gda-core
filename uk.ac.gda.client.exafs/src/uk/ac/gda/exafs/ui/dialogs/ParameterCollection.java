/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import gda.exafs.xml.XmlSerializationMappers;
import uk.ac.gda.beans.exafs.DetectorParameters;


/**
 * Class containing collection of {@link ParametersForScan} - i.e. a list of the full set of parameters for several scans.
 * Also contains methods to serialize to and from XML and CSV format file using {@link #toXML()}, {@link #loadFromFile(String)},
 * {@link #toCSV()}, {@link #saveToFile(String)}.
 */
@JsonInclude(Include.NON_NULL)
public class ParameterCollection {

	private static final Logger logger = LoggerFactory.getLogger(ParameterCollection.class);

	@JsonProperty("ParametersForScan")
	private List<ParametersForScan> parametersForScans;

	private String csvCommentString;

	public ParameterCollection() {
		parametersForScans = new ArrayList<>();
	}

	public ParameterCollection(List<ParametersForScan> overridesForScans) {
		this.parametersForScans = new ArrayList<>(overridesForScans);
	}

	/**
	 *
	 * @return The {@link ParametersForScan} object for each scan
	 */
	public List<ParametersForScan> getParametersForScans() {
		return parametersForScans;
	}

	public void addParametersForScan(ParametersForScan p) {
		parametersForScans.add(p);
	}

	public String toXML() throws IOException {
		return XmlFileHandling.toXML(parametersForScans);
	}

	public static List<ParametersForScan> fromXML(String xmlString) throws IOException {
		try (Reader r = new StringReader(xmlString)) {
			return XmlFileHandling.fromXML(r);
		}
	}

	public static  List<ParametersForScan> loadFromFile(String filePath) throws IOException {
		try(Reader in = new FileReader(filePath)) {
			return XmlFileHandling.fromXML(in);
		}
	}

	private void saveString(String string, String filePath) throws IOException {
		try(BufferedWriter bufWriter = new BufferedWriter(new FileWriter(filePath))) {
			bufWriter.write(string);
		}
	}

	public static void saveToFile(List<ParametersForScan> overrideForScans, String filePath) throws IOException {
		ParameterCollection collection = new ParameterCollection(overrideForScans);
		collection.saveToFile(filePath);
	}

	public void saveToFile(String filePath) throws IOException {
		String xmlString = XmlFileHandling.toXML(parametersForScans);
		saveString(xmlString, filePath);
	}

	public String toCSV() throws IOException {
		String csvString = toCSV(parametersForScans);
		if (csvString != null && StringUtils.hasLength(csvCommentString)) {
			csvString = "# " + csvCommentString + "\n" + csvString;
		}
		return csvString;
	}

	public String getCsvCommentString() {
		return csvCommentString;
	}

	public void setCsvCommentString(String csvCommentString) {
		this.csvCommentString = csvCommentString;
	}

	private static String toCSV(List<ParametersForScan> paramsForScans) throws IOException {
		return CsvFileHandling.toCsv(paramsForScans);
	}

	public void saveCsvToFile(String filePath) throws IOException {
		saveString(toCSV(), filePath);
	}

	public static void saveCsvToFile(List<ParametersForScan> parametersForScan, String filePath) throws IOException {
		ParameterCollection params = new ParameterCollection(parametersForScan);
		params.saveCsvToFile(filePath);
	}

	public static List<ParametersForScan> fromCSV(String csvString) throws IOException {
		try(Reader r = new StringReader(csvString)) {
			return CsvFileHandling.fromCSV(r);
		}
	}

	public static  List<ParametersForScan> loadCsvFromFile(String filePath) throws IOException {
		try(Reader in = new FileReader(filePath)) {
			return CsvFileHandling.fromCSV(in);
		}
	}

	/**
	 * Static methods to facilitate XML serialization.
	 */
	private static class XmlFileHandling {

		private XmlFileHandling() {
		}

		public static String toXML(List<ParametersForScan> overrideForScans) throws IOException {
			ParameterCollection scans = new ParameterCollection(overrideForScans);
			XmlMapper mapper = XmlSerializationMappers.getXmlMapper();
			return mapper.writeValueAsString(scans);
		}

		public static List<ParametersForScan> fromXML(Reader reader) throws IOException {
			XmlMapper mapper = XmlSerializationMappers.getXmlMapper();
			ParameterCollection newParams = mapper.readValue(reader, ParameterCollection.class);
			return newParams.getParametersForScans();
		}
	}

	/**
	 * Static methods for serialization using CSV format files.
	 */
	private static class CsvFileHandling {
		/** Name of package containing the bean classes - used to identify column names containing bean types */
		private static final String BEAN_TYPE_PACKAGE = DetectorParameters.class.getPackage().getName();
		private static final String REPETITIONS = "Repetitions";

		private CsvFileHandling() {
		}

		public static String toCsv(List<ParametersForScan> paramsForScans) throws IOException {
			List<String> headerString = new ArrayList<>();
			// extract column labels from parameters for first scan
			List<ParameterValuesForBean> valuesForFirstScan = paramsForScans.get(0).getParameterValuesForScanBeans();

			valuesForFirstScan.forEach(p -> {
				// xml scan file columns
				headerString.add(p.getBeanTypeNiceName()+":"+p.getBeanType());
				// parameter columns
				headerString.addAll(p.getCsvColumnNames());
			});
			headerString.add(REPETITIONS);

			StringBuilder builder = new StringBuilder();
			try (CSVPrinter csvPrinter = new CSVPrinter(builder, CSVFormat.DEFAULT)) {
				csvPrinter.printRecord(headerString); // first record is the header
				for(ParametersForScan paramsForScan : paramsForScans) {
					csvPrinter.printRecord(getRowValues(paramsForScan));
				}
			}
			return builder.toString();
		}

		private static List<Object> getRowValues(ParametersForScan allParamForScan) {
			List<Object> row = new ArrayList<>();
			allParamForScan.getParameterValuesForScanBeans().forEach(p -> {
				// file name of bean
				row.add(p.getBeanFileName());
				// parameter values
				row.addAll(p.getNewValues());
			});
			row.add(allParamForScan.getNumberOfRepetitions());
			return row;
		}

		public static List<ParametersForScan> fromCSV(Reader csvInput) throws IOException {
			List<ParametersForScan> paramsForAllScans = Collections.emptyList();
			CSVFormat format = CSVFormat.DEFAULT.withHeader().withIgnoreEmptyLines().withCommentMarker('#');
			try(CSVParser parser = new CSVParser(csvInput, format)) {
				// Make list of headers to be processed (everything apart from 'repetitions' value)
				List<String> headersToProcess = parser.getHeaderMap().keySet()
					.stream()
					.filter(k -> !k.equals(REPETITIONS))
					.collect(Collectors.toList());

				checkHeaderFormat(headersToProcess);

				// Read and parse the CSV records ...
				paramsForAllScans = parser.getRecords()
					.stream()
					.map(csvRecord -> parseCsvRecord(csvRecord, headersToProcess))
					.collect(Collectors.toList());
			}
			return paramsForAllScans;
		}

		/**
		 * Check the headers to make sure they are correct format :
		 * {@code <bean type>:<function> }
		 *
		 * @param headers
		 * @throws IOException
		 * @throws DataFormatException if header does not match expected format
		 */
		private static void checkHeaderFormat(List<String> headers) throws IOException {
			for(String headerValue : headers) {
				String[] splitHeader = headerValue.split(":");
				if (splitHeader.length != 2) {
					throw new IOException("Header "+headers.indexOf(headerValue)+" does not have expected format.\n"
							+"Expected <type>:<function name> but found '"+headerValue+"'");
				}
			}
		}

		private static ParametersForScan parseCsvRecord(CSVRecord csvRecord, List<String> headersToProcess) {
			Map<String, ParameterValuesForBean> valuesForBeans = new LinkedHashMap<>();

			// Iterate over the columns in order :
			for(String headerValue : headersToProcess) {
				String value = csvRecord.get(headerValue); // get value from the CSVRecord

				// Split the header value at the ':' :
				//  first field is the bean type, 2nd field is the 'getter' string or class type of bean
				String[] splitHeader = headerValue.split(":");
				String beanType = splitHeader[0].trim();
				String valueInHeader = headerValue.replace(beanType+":", "");

				// Add new object to store bean parameters if necessary :
				logger.info("Beantype : {}", beanType);
				valuesForBeans.computeIfAbsent(beanType, key -> new ParameterValuesForBean());

				// Get parameters for current bean type :
				ParameterValuesForBean valuesForBean = valuesForBeans.get(beanType);
				if (splitHeader.length==2) {
					if (valueInHeader.startsWith(BEAN_TYPE_PACKAGE)) {
						// set the file name and bean class type
						valuesForBean.setBeanFileName(value);
						valuesForBean.setBeanType(valueInHeader);
					} else {
						// set the parameter value ('getter' string and new value).
						valuesForBean.addParameterValue(valueInHeader, convertValue(value));
					}
				}
			}

			// Add the parameters for each bean to the parameters for the scan
			ParametersForScan paramsForScan = new ParametersForScan();
			valuesForBeans.values().forEach(paramsForScan::addValuesForScanBean);

			int numRepetitions = (int) convertValue(csvRecord.get(REPETITIONS));
			paramsForScan.setNumberOfRepetitions(numRepetitions);

			return paramsForScan;
		}

		/**
		 * Try to convert value stored as string to appropriate type. Attempts to convert (in order)
		 * to Boolean, Integer and Double.
		 * @param value
		 * @return Boolean, Integer, Double for String value (if all conversions fail).
		 */
		private static Object convertValue(String value) {
			logger.info("Trying to convert {} to proper type", value);
			if (value == null || value.isEmpty()) {
				return null;
			}
			if (Boolean.TRUE.toString().equalsIgnoreCase(value) ||
				Boolean.FALSE.toString().equalsIgnoreCase(value)) {
				logger.debug("{} is Boolean", value);
				return Boolean.parseBoolean(value);
			}
			try {
				Object newValue = Integer.parseInt(value);
				logger.debug("{} is Integer", value);
				return newValue;
			}catch(NumberFormatException e) {
				// not an integer
			}
			try {
				Object newValue = Double.parseDouble(value);
				logger.debug("{} is Double", value);
				return newValue;
			}catch(NumberFormatException e) {
				// not a double
			}
			logger.debug("{} is String", value);
			// Something else, probably is actually just a string.
			return value;
		}
	}
}
