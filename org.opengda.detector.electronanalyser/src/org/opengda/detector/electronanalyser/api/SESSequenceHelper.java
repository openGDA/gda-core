/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Static class for loading and saving JSON files with its data represented by class {@link SESSequence}.
 */
public class SESSequenceHelper {

	private static final Logger logger = LoggerFactory.getLogger(SESSequenceHelper.class);

	private SESSequenceHelper() {}

	/**
	 * Convert XML files to JSON format so they can be loaded into {@link SESSequence}
	 * @param inputFileName to to read the data from.
	 * @param renameXMLFileName if not null, xml file will be renamed so that
	 * @throws Exception if an error occurred during the loading of the data.
	 */
	public static SESSequence convertSequenceFileFromXMLToJSON(String inputFileName) throws Exception {
		// parsing XML file to get as String using DOM Parser
		final File xmlFile = new File(inputFileName);
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// to be compliant, prohibit the use of all protocols by external entities:
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		final DocumentBuilder dBuilder = factory.newDocumentBuilder();
		final Document xmlDom = dBuilder.parse(xmlFile);
		final Transformer transformer = TransformerFactory.newInstance().newTransformer();

		final StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(xmlDom), new StreamResult(writer));
		final String xml = writer.getBuffer().toString();

		logger.warn("XMLToJSON conversion. \nFile = {}, \nXML = {}", inputFileName, xml);

		final XmlMapper xmlMapper = new XmlMapper();
		final JsonNode node = xmlMapper.readTree(xml.getBytes());

		final ObjectMapper objectMapper = new ObjectMapper();
		final JsonNode sequenceNode = node.get("sequence");
		final SESSequence sequence = objectMapper.readValue(sequenceNode.toPrettyString(), new TypeReference<SESSequence>() {});
		final SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);

		Map<String, Double> excitationEnergySourceNameToValueMap = new HashMap<>();
		final JsonNode regionNodes = sequenceNode.get("region");
		for (int i = 0 ; regionNodes.has(i); i++) {
			final JsonNode regionNode = regionNodes.get(i);
			final String regionName = regionNode.get("name").textValue();

			//Record what each excitationEnergy value is and map it to the excitation energy source name.
			final double excitationEnergy = regionNode.get("excitationEnergy").asDouble();
			final String excitationEnergySourceName = settings.convertLegacyExcitationEnergyToExcitationEnergySourceName(excitationEnergy);
			excitationEnergySourceNameToValueMap.put(excitationEnergySourceName, excitationEnergy);

			final JsonNode runModeNode = regionNode.get("runMode");
			//Must preserve numIterations from old format to new.
			final int numIterators = runModeNode.get("numIterations").asInt();
			final SESRegion region = sequence.getRegionByName(regionName);
			region.setIterations(numIterators);
		}
		final List<SESExcitationEnergySource> excitationEnergySources = settings.getSESExcitationEnergySourceList();
		//Preserve the excitationEnergy values by mapping it to the corresponding name
		excitationEnergySources.forEach(e -> {
			final String name = e.getName();
			if (excitationEnergySourceNameToValueMap.containsKey(name)) {
				e.setValue(excitationEnergySourceNameToValueMap.get(name));
			}
		});
		sequence.setExcitationEnergySource(excitationEnergySources);
		return sequence;
	}

	/**
	 * Load a JSON file into {@link SESSequence}.
	 * @param filePath to to read the data from.
	 * @returns {@code SESSequence} class that represents the data read from the filePath.
	 * @throws Exception if an error occurred during the loading of the data.
	 */
	public static SESSequence loadSequence(String filePath) throws Exception {
		logger.info("Loading sequence from file \"{}\"", filePath);
		final File file = new File(filePath);
		final String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		final ObjectMapper objectMapper = new ObjectMapper();
		final SESSequence sequence = objectMapper.readValue(contents, new TypeReference<SESSequence>() {});
		checkSequenceIsCompatibleWithConfigurationAndConvert(sequence);
		return sequence;
	}

	/**
	 * Check sequence file is compatible with the defined configuration. Useful for sequence files defined one way using different configuration
	 * be converted if not compatible to stop any unexpected crashing.
	 * @param seq the data to test is compatible with the configuration
	 */
	private static void checkSequenceIsCompatibleWithConfigurationAndConvert(SESSequence seq) {
		if(seq == null) throw new IllegalArgumentException("SESSequence is null!");
		final SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);
		final List<SESExcitationEnergySource> configuredEnergySources = settings.getSESExcitationEnergySourceList();
		convertAndRemoveIncompatibleExcitationEnergySourcesFromSequence(seq, configuredEnergySources);
		checkExcitationEnergySourcesHaveValidScannablesAndAddToSequence(seq, configuredEnergySources);
		final boolean isSingleSource = !settings.isExcitationEnergySourceSelectable() && !configuredEnergySources.isEmpty();
		//If single source, we need to convert all regions to be that single source because the selector is removed from the UI and user won't be able to change it.
		changeAllRegionsToSingleSource(isSingleSource, seq);
	}

	private static void convertAndRemoveIncompatibleExcitationEnergySourcesFromSequence(final SESSequence seq, List<SESExcitationEnergySource> configuredEnergySources) throws NoSuchElementException {
		final List<String> configuredEnergySourceNames = configuredEnergySources.stream().map(e -> e.getName()).toList();
		final List<String> configuredEnergySourceScannableNames = configuredEnergySources.stream().map(e -> e.getScannableName()).toList();

		final List<SESExcitationEnergySource> toRemove = new ArrayList<>();
		for (final SESExcitationEnergySource seqEnergySource : seq.getExcitationEnergySources()) {
			final String seqEnergySourceName = seqEnergySource.getName();
			final String seqEnergySourceScannableName = seqEnergySource.getScannableName();
			final boolean containsName = configuredEnergySourceNames.contains(seqEnergySourceName);
			final boolean containsScannableName = configuredEnergySourceScannableNames.contains(seqEnergySourceScannableName);
			if (!containsName && !containsScannableName) {
				toRemove.add(seqEnergySource);
			}
			else if (containsName && !containsScannableName) {
				final String configuredScannableName = configuredEnergySources.stream()
					.filter(e -> e.getName().equals(seqEnergySourceName))
					.map(e -> e.getScannableName())
					.findFirst()
					.orElseThrow();
				logger.warn("Converting {} scannableName from {} to {} to make comptaible.", seqEnergySource, seqEnergySourceScannableName, configuredScannableName);
				seqEnergySource.setScannableName(configuredScannableName);
			} else if (containsScannableName && !containsName) {
				final String configuredSourceName = configuredEnergySources.stream()
					.filter(e -> e.getName().equals(seqEnergySourceScannableName))
					.map(e -> e.getName())
					.findFirst()
					.orElseThrow();
				logger.warn("Converting {} name from {} to {} to make comptaible.", seqEnergySource, seqEnergySourceName, configuredSourceName);
				seqEnergySource.setName(configuredSourceName);
			}
		}
		toRemove.stream().forEach(seq::removeExcitationEnergySource);
	}

	private static void checkExcitationEnergySourcesHaveValidScannablesAndAddToSequence(final SESSequence seq, List<SESExcitationEnergySource> excitationEnergySourcesToCheck) throws NullPointerException {
		final List<String> seqEnergySourceNames = seq.getExcitationEnergySources().stream().map(e -> e.getName()).toList();
		final List<String> seqEnergySourceScannableNames = seq.getExcitationEnergySources().stream().map(e -> e.getScannableName()).toList();
		for (final SESExcitationEnergySource energySourceToCheck : excitationEnergySourcesToCheck) {
			if (energySourceToCheck.getScannable() == null) {
				throw new NullPointerException(
					"Excitation energy source " + energySourceToCheck + " has scannableName " +
							energySourceToCheck.getScannableName() + ". The scannable is null!"
				);
			}
			if (!seqEnergySourceNames.contains(energySourceToCheck.getName()) && !seqEnergySourceScannableNames.contains(energySourceToCheck.getScannableName())) {
				seq.addExcitationEnergySource(energySourceToCheck);
			}
		}
	}

	private static void changeAllRegionsToSingleSource(final boolean singleSource, SESSequence seq) {
		if (!singleSource) return;
		final String singleExcitationEnergySource = seq.getExcitationEnergySources().get(0).getName();
		seq.getRegions().stream().forEach(r -> {
			if (!r.getExcitationEnergySource().equals(singleExcitationEnergySource)) {
				logger.warn("Converting region {} excitationEnergySource from {} to {} to make comptaible.", r.getName(), r.getExcitationEnergySource(), singleExcitationEnergySource);
				r.setExcitationEnergySource(singleExcitationEnergySource);
			}
		});
	}

	/**
	 * Saves {@link SESSequence} to a JSON file. If an existing XML file already exists and
	 * {@link SESSettings#legacyFileFormatOverwrittenForSESSequenceJSONHanlder} is false, it will rename the
	 * file to be /path/to/file.ext to /path/to/file_&lt;config&gt;.ext, where &lt;config&gt; is defined by
	 * {@link SESSettingsService#getLegacyFileExtensionForSESSequenceJSONHanlder()}, so the XML file isn't
	 * overwritten. Useful in case of a roll back is ever required and user can still use old sequence files.
	 * @param sequence to save to JSON.
	 * @param filePath to save data into.
	 * @throws IOException if an error occurred during the save.
	 */
	public static void saveSequence(SESSequence sequence, String filePath) throws IOException {
		logger.info("Saving sequence to file \"{}\"", filePath);
		if(sequence == null) throw new IllegalArgumentException("SESSequence cannot be null when saving");
		final ObjectMapper objectMapper = new ObjectMapper();
		final ObjectWriter writer = objectMapper.writer().withDefaultPrettyPrinter();
		final String json = writer.writeValueAsString(sequence);

		final File sequenceFile = new File(filePath);
		final Path sequencePath = sequenceFile.toPath().getParent();
		if (!Files.isDirectory(sequencePath)) {
			logger.info("Creating directory {}", sequencePath);
			Files.createDirectories(sequencePath);
		}

		final SESSettingsService settings = ServiceProvider.getService(SESSettingsService.class);
		final boolean overrideLegacyXMLFile = settings.isLegacyFileFormatOverwrittenForSESSequenceJSONHanlder();
		//Check if file to save to exits and is in xml (legacy) format. If we don't override, rename file using configured settings.
		if (isFileXMLFormat(filePath)) {
			if (!overrideLegacyXMLFile) {
				final File oldFile = new File(filePath);
				final String extension = oldFile.getName().substring(oldFile.getName().lastIndexOf("."), oldFile.getName().length());
				final File renamedFile = new File(filePath.replace(extension, "_" + settings.getLegacyFileExtensionForSESSequenceJSONHanlder() + extension));
				final boolean successful = oldFile.renameTo(renamedFile);
				if (successful) {
					logger.info("Preserved legacy file \"{}\" by renaming it to \"{}\"", oldFile, renamedFile);
				} else {
					throw new IOException("Unable to rename legacy file \"" + filePath + "\" to \"" + renamedFile + "\"");
				}
			} else {
				logger.debug("Overriding legacy file to new seqeunce file format.");
			}
		}
		try (final FileWriter fileWriter = new FileWriter(filePath)) {
			fileWriter.write(json);
		}
		logger.info("Saved sequence file successfully!");
	}

	/**
	 * @returns the file path that the default initial sequence file is created in.
	 */
	public static String getDefaultFilePath() {
		return InterfaceProvider.getPathConstructor().createFromProperty("gda.ses.electronanalyser.seq.dir");
	}

	public static boolean isFileJSONFormat(String filePath) {
		final File file = new File(filePath);
		if (!file.isFile()) return false;
		try {
			new ObjectMapper().readTree(Files.readString(Path.of(filePath)));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean isFileXMLFormat(String filePath) {
		final File file = new File(filePath);
		if (!file.isFile()) return false;
		try {
			DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(filePath);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}