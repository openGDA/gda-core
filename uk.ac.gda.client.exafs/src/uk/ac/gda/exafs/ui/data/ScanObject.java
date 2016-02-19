/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.data;

import gda.exafs.scan.ExafsTimeEstimator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.richbeans.reflection.RichBeanUtils;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentObject;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.IXMLCommandHandler;
import uk.ac.gda.client.experimentdefinition.ui.handlers.XMLCommandHandler;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * This class looks a bit like a bean but it is not designed to be a bean. It is an interface to the .scan file. setting
 * the file names can also be committed to the file by calling the write() method.
 */
public class ScanObject extends ExperimentObject implements IExperimentObject {

	public static final String DETECTORBEANTYPE = "Detector";
	public static final String OUTPUTBEANTYPE = "Output";
	public static final String SAMPLEBEANTYPE = "Sample";
	public static final String SCANBEANTYPE = "Scan";

	@Override
	public void createFilesFromTemplates(IXMLCommandHandler xmlCH) {
		// temporary ruse to all stage checkin of changes
	}

	@Override
	public void createFilesFromTemplates() {
		final IFolder folder = getFolder();
		XMLCommandHandler xmlCH = new XMLCommandHandler();

		if (ScanObjectManager.isXESOnlyMode()) {
			IFile scanFile = xmlCH.doTemplateCopy(folder, "XES_Parameters.xml");
			getTypeToFileMap().put(SCANBEANTYPE, scanFile.getName());
		} else if (ScanObjectManager.isQEXAFSDefaultScanType()){
			IFile scanFile = xmlCH.doTemplateCopy(folder, "QEXAFS_Parameters.xml");
			getTypeToFileMap().put(SCANBEANTYPE, scanFile.getName());
		} else {
			IFile scanFile = xmlCH.doTemplateCopy(folder, "XAS_Parameters.xml");
			getTypeToFileMap().put(SCANBEANTYPE, scanFile.getName());
		}

		IFile sampleFile = xmlCH.doTemplateCopy(folder, "Sample_Parameters.xml");
		getTypeToFileMap().put(SAMPLEBEANTYPE, sampleFile.getName());

		if (ScanObjectManager.isXESOnlyMode()) {
			IFile detFile = xmlCH.doTemplateCopy(folder, "XESDetector_Parameters.xml");
			getTypeToFileMap().put(DETECTORBEANTYPE, detFile.getName());

		} else {
			IFile detFile = xmlCH.doTemplateCopy(folder, "Detector_Parameters.xml");
			getTypeToFileMap().put(DETECTORBEANTYPE, detFile.getName());
		}

		IFile outFile = xmlCH.doTemplateCopy(folder, "Output_Parameters.xml");
		getTypeToFileMap().put(OUTPUTBEANTYPE, outFile.getName());

	}

	@Override
	public Map<String, IFile> getFilesWithTypes() {
		HashMap<String,String> typeToFiles = getTypeToFileMap();
		Map<String, IFile> targetFiles = new HashMap<String, IFile>(typeToFiles.size());

		for (Object fileType : typeToFiles.keySet()) {
			IFile file = getFolder().getFile(typeToFiles.get(fileType));
			targetFiles.put((String) fileType, file);
		}
		return targetFiles;
	}

	public IFile getSampleFile() {
		if (getSampleFileName() == null)
			return null;
		return getFolder().getFile(getSampleFileName());
	}

	public IFile getScanFile() {
		if (getScanFileName() == null)
			return null;
		return getFolder().getFile(getScanFileName());
	}

	public IFile getDetectorFile() {
		if (getDetectorFileName() == null)
			return null;
		return getFolder().getFile(getDetectorFileName());
	}

	public IFile getOutputFile() {
		if (getOutputFileName() == null)
			return null;
		return getFolder().getFile(getOutputFileName());
	}

	@Override
	public String toPersistenceString() {
		final StringBuilder buf = new StringBuilder(getRunName());
		buf.append(" ");
		buf.append(getSampleFileName());
		buf.append(" ");
		buf.append(getScanFileName());
		buf.append(" ");
		buf.append(getDetectorFileName());
		buf.append(" ");
		buf.append(getOutputFileName());
		buf.append(" ");
		buf.append(getNumberRepetitions());
		return buf.toString();
	}

	public String getSampleFileName() {
		return getTypeToFileMap().get(SAMPLEBEANTYPE);
	}

	public void setSampleFileName(String fileName) {
		if (fileName.indexOf(' ') > -1)
			throw new RuntimeException("Sample name cannot contain a space.");
		getTypeToFileMap().put(SAMPLEBEANTYPE, fileName);
	}

	public String getScanFileName() {
		return getTypeToFileMap().get(SCANBEANTYPE);
	}

	public void setScanFileName(String fileName) {
		if (fileName.indexOf(' ') > -1)
			throw new RuntimeException("Scan name cannot contain a space.");
		getTypeToFileMap().put(SCANBEANTYPE, fileName);
	}

	public String getDetectorFileName() {
		return getTypeToFileMap().get(DETECTORBEANTYPE);
	}

	public void setDetectorFileName(String fileName) {
		if (fileName.indexOf(' ') > -1)
			throw new RuntimeException("Detector name cannot contain a space.");
		getTypeToFileMap().put(DETECTORBEANTYPE, fileName);
	}

	public String getOutputFileName() {
		return getTypeToFileMap().get(OUTPUTBEANTYPE);
	}

	public void setOutputFileName(String fileName) {
		if (fileName.indexOf(' ') > -1)
			throw new RuntimeException("Output name cannot contain a space.");
		getTypeToFileMap().put(OUTPUTBEANTYPE, fileName);
		this.outputPath = null;
	}

	private String outputPath;

	@Override
	public String getOutputPath() {
		if (outputPath == null) {
			try {
				final IOutputParameters params = getOutputParameters();
				if (params != null)
					this.outputPath = params.getAsciiDirectory() + "/" + params.getAsciiFileName();
			} catch (Exception e) {
				e.printStackTrace();
				outputPath = "";
			}
		}
		return outputPath;
	}

	/**
	 * @return true if xanes file, false if not xanes or if file cannot be found.
	 * @throws Exception
	 */
	public boolean isXanes() throws Exception {
		return isDescribed(XanesScanParameters.class);
	}

	/**
	 * @return true if xanes file, false if not xanes or if file cannot be found.
	 * @throws Exception
	 */
	public boolean isMicroFocus() throws Exception {
		return isDescribed(MicroFocusScanParameters.class);
	}

	/**
	 * @return true if qexafs file, false if not qeaxfs or if file cannot be found.
	 * @throws Exception
	 */
	public boolean isQexafs() throws Exception {
		return isDescribed(QEXAFSParameters.class);
	}

	/**
	 * @return true if xas file, false if not xas or if file cannot be found.
	 * @throws Exception
	 */
	public boolean isXas() throws Exception {
		return isDescribed(XasScanParameters.class);
	}

	public boolean isXes() throws Exception {
		return isDescribed(XesScanParameters.class);
	}

	private boolean isDescribed(Class<? extends XMLRichBean> beanClass) throws Exception {
		if (getScanFile() == null)
			return false;
		final IFile scanFile = getScanFile();
		if (!scanFile.exists())
			return false;
		return BeansFactory.isBean(scanFile.getLocation().toFile(), beanClass);
	}

	/**
	 * Returns a new bean. NOTE: Should not be used to get beans for editor, this is not the editors version but a
	 * representation of the current file.
	 *
	 * @return a new bean from the file.
	 * @throws Exception
	 */
	public IScanParameters getScanParameters() throws Exception {
		if (getScanFileName() == null)
			return null;

		final IFile file = getFolder().getFile(getScanFileName());
		if (!file.exists())
			return null;
		return (IScanParameters) XMLHelpers.getBean(file.getLocation().toFile());

	}

	/**
	 * Returns the name of the energy scannable being used.
	 *
	 * @return name
	 * @throws Exception
	 */
	public String getScannableName() throws Exception {

		final Object params = getScanParameters();

		if (params instanceof MicroFocusScanParameters)
			return ""; // not used by MicroFocusScanParameters, the object reference is held in the map scan objects

		return (String) RichBeanUtils.getBeanValue(params, "scannableName");
	}

	/**
	 * Returns a new bean. NOTE: Should not be used to get beans for editor, this is not the editors version but a
	 * representation of the current file.
	 *
	 * @return a new bean from the file.
	 * @throws Exception
	 */
	public IOutputParameters getOutputParameters() throws Exception {
		if (getOutputFileName() == null)
			return null;
		final IFile file = getFolder().getFile(getOutputFileName());
		if (!file.exists())
			return null;
		return (IOutputParameters) XMLHelpers.getBean(file.getLocation().toFile());

	}

	/**
	 * Returns a new bean. NOTE: Should not be used to get beans for editor, this is not the editors version but a
	 * representation of the current file.
	 *
	 * @return a new bean from the file.
	 * @throws Exception
	 */
	public ISampleParameters getSampleParameters() throws Exception {
		if (getSampleFileName() == null)
			return null;
		final IFile file = getFolder().getFile(getSampleFileName());
		if (!file.exists())
			return null;

		return (ISampleParameters) XMLHelpers.getBean(file.getLocation().toFile());

	}

	/**
	 * Returns a new bean. NOTE: Should not be used to get beans for editor, this is not the editors version but a
	 * representation of the current file.
	 *
	 * @return a new bean from the file.
	 * @throws Exception
	 */
	public IDetectorParameters getDetectorParameters() throws Exception {
		if (getDetectorFileName() == null)
			return null;
		final IFile file = getFolder().getFile(getDetectorFileName());
		if (!file.exists())
			return null;
		return (IDetectorParameters) XMLHelpers.getBean(file.getLocation().toFile());
	}

	/**
	 * @return energy for end of scan
	 * @throws Exception
	 */
	public double getFinalEnergy() throws Exception {
		final Object s = getScanParameters();
		if (s instanceof XanesScanParameters) {
			return ((XanesScanParameters) s).getFinalEnergy();
		} else if (s instanceof QEXAFSParameters) {
			return ((QEXAFSParameters) s).getFinalEnergy();
		} else if (s instanceof XesScanParameters) {
			return ((XesScanParameters) s).getXesFinalEnergy();
		}
		return ((XasScanParameters) s).getFinalEnergy();
	}

	/**
	 * @return energy for start of scan
	 * @throws Exception
	 */
	public double getInitialEnergy() throws Exception {
		final Object s = getScanParameters();
		if (s instanceof XanesScanParameters) {
			return ((XanesScanParameters) s).getRegions().get(0).getEnergy();
		}
		if (s instanceof QEXAFSParameters) {
			return ((QEXAFSParameters) s).getInitialEnergy();
		}
		if (s instanceof XesScanParameters) {
			return ((XesScanParameters) s).getMonoInitialEnergy();
		}
		return ((XasScanParameters) s).getInitialEnergy();
	}

	@Override
	public long estimateTime() throws Exception {
		return ExafsTimeEstimator.getTime(getScanParameters());
	}

	@Override
	public String getCommandString() throws Exception {
		return getExptCommand() + " " + getArgs();
	}

	private String getExptCommand() throws Exception {
		if (isXanes()) {
			return "xas";
		} else if (isQexafs()) {
			return "qexafs";
		} else if (isMicroFocus()) {
			return "map";
		} else if (isXes()) {
			return "xes";
		}
		return "xas";
	}

	private String getArgs() {
		final StringBuilder buf = new StringBuilder();
		buf.append("\"" + getFileKey(getSampleFileName()) + "\"");
		buf.append(" \"");
		buf.append(getFileKey(getScanFileName()));
		buf.append("\" \"");
		buf.append(getFileKey(getDetectorFileName()));
		buf.append("\" \"");
		buf.append(getFileKey(getOutputFileName()));
		buf.append("\" ");
		buf.append("\"" + getFolder().getLocation()+ "\"");
		buf.append(" " + getNumberRepetitions() + " ");
		buf.append("False");
		return buf.toString();
	}

	private static String getFileKey(final String fileName) {
		if (fileName == null || fileName.equals("None"))
			return "None";
		if (fileName.indexOf("[") != -1) {
			return fileName.substring(0, fileName.lastIndexOf('.'));
		}
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	@Override
	public void parseEditorFile(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();
		br.close();
		if(line!=null){
			String[] parts = line.split(" ");
			if (parts.length != 8)
				throw new Exception("File contents incorrect! "  + fileName);
			setSampleFileName(parts[1]);
			setScanFileName(parts[2]);
			setDetectorFileName(parts[3]);
			setOutputFileName(parts[4]);
			setNumberRepetitions(Integer.parseInt(parts[6]));
		}
		else
			throw new Exception("Cannot parse editor file");
	}
}
