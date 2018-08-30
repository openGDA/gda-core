/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.excalibur.config.model.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.excalibur.ChipAnper;
import uk.ac.gda.devices.excalibur.ChipPixel;
import uk.ac.gda.devices.excalibur.ExcaliburNodeWrapper.ReadoutNodeWrapper;
import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.ExcaliburSummaryAdbase;
import uk.ac.gda.devices.excalibur.Gap;
import uk.ac.gda.devices.excalibur.Master;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;
import uk.ac.gda.devices.excalibur.NodeFix;
import uk.ac.gda.excalibur.config.model.AnperModel;
import uk.ac.gda.excalibur.config.model.ExcaliburConfig;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigFactory;
import uk.ac.gda.excalibur.config.model.ExcaliburConfigPackage;
import uk.ac.gda.excalibur.config.model.FixModel;
import uk.ac.gda.excalibur.config.model.GapModel;
import uk.ac.gda.excalibur.config.model.MasterModel;
import uk.ac.gda.excalibur.config.model.MpxiiiChipRegModel;
import uk.ac.gda.excalibur.config.model.PixelModel;
import uk.ac.gda.excalibur.config.model.ReadoutNode;
import uk.ac.gda.excalibur.config.model.ReadoutNodeFemModel;
import uk.ac.gda.excalibur.config.model.SummaryAdbaseModel;
import uk.ac.gda.excalibur.config.model.SummaryNode;

/**
 * Model helper for the excalibur config. Methods include reading from the hardware and transforming it into the
 * in-memory excalibur config model, and further saving it to xml.
 */
public class ExcaliburConfigModelHelper {
	private static final String FILE_EXTN_EXCALIBURCONFIG = "excaliburconfig";

	private final static Logger logger = LoggerFactory.getLogger(ExcaliburConfigModelHelper.class);

	public static ExcaliburConfigModelHelper INSTANCE = new ExcaliburConfigModelHelper();

	private ResourceSet resourceSet;

	/**
	 * @param fileName
	 * @return {@link Resource}
	 */
	private Resource createResource(String fileName) {
		ResourceSet rSet = getResourceSet();
		// Get the URI of the model file.
		//
		URI fileURI = URI.createFileURI(fileName);
		// Create a resource for this file.
		//
		return rSet.createResource(fileURI);
	}

	public void reloadResource(Resource resource) throws IOException {
		if (resource instanceof ExcaliburConfigResourceImpl) {
			ExcaliburConfigResourceImpl excaliburConfigRes = (ExcaliburConfigResourceImpl) resource;
			excaliburConfigRes.unload();
			excaliburConfigRes.load(excaliburConfigRes.getDefaultLoadOptions());
		}
	}

	/**
	 * @param fileName
	 * @return {@link Resource}
	 */
	private Resource getResource(String fileName) {
		ResourceSet rSet = getResourceSet();
		// Get the URI of the model file.
		//
		URI fileURI = URI.createFileURI(fileName);
		// Create a resource for this file.
		//
		Resource resource = rSet.getResource(fileURI, true);
		return resource;
	}

	/**
	 * @return {@link ResourceSet}
	 */
	protected ResourceSet getResourceSet() {
		if (resourceSet == null) {
			resourceSet = new ResourceSetImpl();
			// To initialize the resourceset resource factory registry with the excalibur config package
			EPackage.Registry.INSTANCE.put(ExcaliburConfigPackage.eNS_URI, ExcaliburConfigPackage.eINSTANCE);
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(FILE_EXTN_EXCALIBURCONFIG,
					new ExcaliburConfigResourceFactoryImpl());
		}
		return resourceSet;
	}

	/**
	 * Saves the {@link ExcaliburConfig} to an excalibur file - the excalibur file should end in an extension
	 * .excaliburconfig for it to be resolved in the client when imported.
	 * 
	 * @param fileName
	 * @param excaliburConfig
	 * @throws IOException 
	 */
	public void saveToXML(String fileName, ExcaliburConfig excaliburConfig) throws Exception {
		try{
			Resource resource = createResource(fileName);
			resource.getContents().add(excaliburConfig);
			// Save the contents of the resource to the file system.
			//
			Map<Object, Object> options = new HashMap<Object, Object>();
			options.put(XMLResource.OPTION_ENCODING, "UTF-8");
			resource.save(options);
		} catch(Exception e){
			throw new Exception("Error saving to file '" + fileName + "'", e);
		}

	}

	/**
	 * @param summary
	 * @return {@link SummaryNode} which can be set on the {@link ExcaliburConfig}
	 * @throws Exception
	 */
	public SummaryNode createSummaryAdBaseModel(ExcaliburSummaryAdbase summary) throws Exception {
		SummaryNode summaryNode = ExcaliburConfigFactory.eINSTANCE.createSummaryNode();
		SummaryAdbaseModel summaryAdbaseModel = ExcaliburConfigFactory.eINSTANCE.createSummaryAdbaseModel();
		summaryAdbaseModel.setCounterDepth(summary.getCounterDepth());
		summaryAdbaseModel.setFrameDivisor(summary.getFrameDivisor());
		summaryAdbaseModel.setGapFillConstant(summary.getGapFillConstant());
		summaryNode.setSummaryFem(summaryAdbaseModel);
		return summaryNode;
	}

	/**
	 * @param readOutNodes
	 * @return {@link ExcaliburConfig} which is the root node - this includes the {@link ReadoutNodeFemModel} for the
	 *         readout nodes passed in.
	 * @throws Exception
	 */
	public ExcaliburConfig createExcaliburConfig(List<ReadoutNodeWrapper> readOutNodes) throws Exception {
		ExcaliburConfig excaliburConfig = ExcaliburConfigFactory.eINSTANCE.createExcaliburConfig();

		int count = 1;
		for (ReadoutNodeWrapper readoutNodeWrapper : readOutNodes) {
			ReadoutNode readoutNode = ExcaliburConfigFactory.eINSTANCE.createReadoutNode();
			readoutNode.setId(count++);

			// Fem
			ExcaliburReadoutNodeFem fem = readoutNodeWrapper.getAdBase();
			ReadoutNodeFemModel readoutNodeFem = ExcaliburConfigFactory.eINSTANCE.createReadoutNodeFemModel();
			readoutNodeFem.setCounterDepth(fem.getCounterDepth());
			readoutNodeFem.setCounterSelect(fem.getCounterSelect());
			readoutNodeFem.setOperationMode(fem.getOperationMode());
			//readoutNodeFem.setDacExternal(fem.getDacExternal()); // kw 30 June 2015
			//readoutNodeFem.setDacSense(fem.getDacSense()); // kw 30 June 2015
			readoutNodeFem.setMpxiiiChipReg1(getMpxiiiChipRegModel(fem.getMpxiiiChipReg1()));
			readoutNodeFem.setMpxiiiChipReg2(getMpxiiiChipRegModel(fem.getMpxiiiChipReg2()));
			readoutNodeFem.setMpxiiiChipReg3(getMpxiiiChipRegModel(fem.getMpxiiiChipReg3()));
			readoutNodeFem.setMpxiiiChipReg4(getMpxiiiChipRegModel(fem.getMpxiiiChipReg4()));
			readoutNodeFem.setMpxiiiChipReg5(getMpxiiiChipRegModel(fem.getMpxiiiChipReg5()));
			readoutNodeFem.setMpxiiiChipReg6(getMpxiiiChipRegModel(fem.getMpxiiiChipReg6()));
			readoutNodeFem.setMpxiiiChipReg7(getMpxiiiChipRegModel(fem.getMpxiiiChipReg7()));
			readoutNodeFem.setMpxiiiChipReg8(getMpxiiiChipRegModel(fem.getMpxiiiChipReg8()));

			readoutNode.setReadoutNodeFem(readoutNodeFem);
			// Fix model
			NodeFix fix = readoutNodeWrapper.getFix();
			FixModel fixModel = ExcaliburConfigFactory.eINSTANCE.createFixModel();
			fixModel.setScaleEdgePixelsEnabled(fix.isScaleEdgePixelsEnabled());
			fixModel.setStatisticsEnabled(fix.isStatisticsEnabled());

			readoutNode.setFix(fixModel);
			// Gap
			Gap gap = readoutNodeWrapper.getGap();
			GapModel gapModel = ExcaliburConfigFactory.eINSTANCE.createGapModel();
			gapModel.setGapFillConstant(gap.getGapFillConstant());
			gapModel.setGapFillingEnabled(gap.isGapFillingEnabled());
			gapModel.setGapFillMode(gap.getGapFillMode());
			readoutNode.setGap(gapModel);

			// Mst
			Master mst = readoutNodeWrapper.getMst();
			MasterModel mstModel = ExcaliburConfigFactory.eINSTANCE.createMasterModel();
			mstModel.setFrameDivisor(mst.getFrameDivisor());
			readoutNode.setMst(mstModel);

			excaliburConfig.getReadoutNodes().add(readoutNode);
		}
		return excaliburConfig;
	}

	private MpxiiiChipRegModel getMpxiiiChipRegModel(MpxiiiChipReg chip) throws Exception {
		MpxiiiChipRegModel chipRegModel = ExcaliburConfigFactory.eINSTANCE.createMpxiiiChipRegModel();
		
		//chipRegModel.setAnper(getAnperModel(chip.getAnper()));	// kw 30 June 2015 
		chipRegModel.setPixel(getPixelModel(chip.getPixel()));
		chipRegModel.setChipDisable(!chip.isChipEnabled());
		//chipRegModel.setDacIntoMpx(chip.getDacIntoMpx());			// kw thu 17 dec 2015
		//chipRegModel.setDacOutFromMpx(chip.getDacOutFromMpx());	// kw thu 17 dec 2015

		return chipRegModel;
	}

	private PixelModel getPixelModel(ChipPixel pixel) throws Exception {
		PixelModel pixelModel = ExcaliburConfigFactory.eINSTANCE.createPixelModel();
		//pixelModel.setGainMode(Arrays.copyOf(pixel.getGainMode(), pixel.getGainMode().length));			// kw thu 17 dec 2015
		pixelModel.setMask(Arrays.copyOf(pixel.getMask(), pixel.getMask().length));
		pixelModel.setTest(Arrays.copyOf(pixel.getTest(), pixel.getTest().length));
		//pixelModel.setThresholdA(Arrays.copyOf(pixel.getThresholdA(), pixel.getThresholdA().length));		// kw thu 17 dec 2015
		//pixelModel.setThresholdB(Arrays.copyOf(pixel.getThresholdB(), pixel.getThresholdB().length));		// kw thu 17 dec 2015
		return pixelModel;
	}

	private AnperModel getAnperModel(ChipAnper anper) throws Exception {
		AnperModel anperModel = ExcaliburConfigFactory.eINSTANCE.createAnperModel();
		anperModel.setCas(anper.getCas());
		anperModel.setDacPixel(anper.getDacPixel());
		anperModel.setDelay(anper.getDelay());
		anperModel.setDisc(anper.getDisc());
		anperModel.setDiscls(anper.getDiscls());
		anperModel.setFbk(anper.getFbk());
		anperModel.setGnd(anper.getGnd());
		anperModel.setIkrum(anper.getIkrum());
		anperModel.setPreamp(anper.getPreamp());
		anperModel.setRpz(anper.getRpz());
		anperModel.setShaper(anper.getShaper());
		anperModel.setThreshold0(anper.getThreshold0());
		anperModel.setThreshold1(anper.getThreshold1());
		anperModel.setThreshold2(anper.getThreshold2());
		anperModel.setThreshold3(anper.getThreshold3());
		anperModel.setThreshold4(anper.getThreshold4());
		anperModel.setThreshold5(anper.getThreshold5());
		anperModel.setThreshold6(anper.getThreshold6());
		anperModel.setThreshold7(anper.getThreshold7());
		anperModel.setThresholdn(anper.getThresholdn());
		anperModel.setTpBufferIn(anper.getTpBufferIn());
		anperModel.setTpBufferOut(anper.getTpBufferOut());
		anperModel.setTpref(anper.getTpref());
		anperModel.setTprefA(anper.getTprefA());
		anperModel.setTprefB(anper.getTprefB());
		// There is a slight doubt here - setting it as it sounds right
		
		return anperModel;
	}

	/**
	 * @param filename
	 * @return the {@link ExcaliburConfig} obtained from the contents of a file.
	 */
	public ExcaliburConfig getExcaliburConfigFromFile(String filename) {
		Resource resource = getResource(filename);
		EObject eObject = resource.getContents().get(0);
		if (eObject instanceof ExcaliburConfig) {
			ExcaliburConfig excaliburConfig = (ExcaliburConfig) eObject;
			return excaliburConfig;
		}
		return null;
	}

	/**
	 * Sends the configuration parameters persisted in the {@link ExcaliburConfig} to the excalibur detector.
	 * 
	 */
	Exception sendToExcaliburException=null;
	public void sendToExcalibur(List<ExcaliburReadoutNodeFem> readOutNodes, ExcaliburConfig excaliburConfig)
			throws Exception {
		sendToExcaliburException = null;
		Vector<Thread> threads = new Vector<Thread>();
		for (int count = 0; count < excaliburConfig.getReadoutNodes().size(); count++) {
			final ReadoutNodeFemModel modelReadoutNodeFem = excaliburConfig.getReadoutNodes().get(count).getReadoutNodeFem();
			final ExcaliburReadoutNodeFem detectorNode = readOutNodes.get(count);
			Thread thread = new Thread(new Runnable(){

				@Override
				public void run() {
					try{
						detectorNode.setCounterDepth(modelReadoutNodeFem.getCounterDepth());
						detectorNode.setCounterSelect(modelReadoutNodeFem.getCounterSelect());
						detectorNode.setOperationMode(modelReadoutNodeFem.getOperationMode());
						//detectorNode.setDacExternal(modelReadoutNodeFem.getDacExternal()); // kw 30 June 2015
						//detectorNode.setDacSense(modelReadoutNodeFem.getDacSense()); // kw 30 June 2015
						
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg1(), modelReadoutNodeFem.getMpxiiiChipReg1());	// kw thu 17 dec 2015
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg2(), modelReadoutNodeFem.getMpxiiiChipReg2());
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg3(), modelReadoutNodeFem.getMpxiiiChipReg3());
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg4(), modelReadoutNodeFem.getMpxiiiChipReg4());
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg5(), modelReadoutNodeFem.getMpxiiiChipReg5());
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg6(), modelReadoutNodeFem.getMpxiiiChipReg6());
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg7(), modelReadoutNodeFem.getMpxiiiChipReg7());
						//setDetectorChipReg(detectorNode.getMpxiiiChipReg8(), modelReadoutNodeFem.getMpxiiiChipReg8());
					} catch(Exception ex){
						sendToExcaliburException = ex;
					}
				}});
			threads.add(thread);
			thread.start();
			
		}
		for(Thread thread : threads){
			thread.join();
		}
		if( sendToExcaliburException != null)
			throw sendToExcaliburException;
	}

	private void setDetectorChipReg(MpxiiiChipReg detectorChip, MpxiiiChipRegModel modelChipReg) throws Exception {
		if (modelChipReg.isChipDisable()) {
			detectorChip.disableChip();
		} else {
			detectorChip.enableChip();
		}
		detectorChip.setDacIntoMpx(modelChipReg.getDacIntoMpx());
		setDetectorPixel(detectorChip.getPixel(), modelChipReg.getPixel());
		detectorChip.loadPixelConfig();
		setDetectorAnper(detectorChip.getAnper(), modelChipReg.getAnper());
		detectorChip.loadDacConfig();
	}

	private void setDetectorAnper(ChipAnper detectorAnper, AnperModel modelAnper) throws Exception {
		detectorAnper.setCas(modelAnper.getCas());
		detectorAnper.setDacPixel(modelAnper.getDacPixel());
		detectorAnper.setDelay(modelAnper.getDelay());
		detectorAnper.setDisc(modelAnper.getDisc());
		detectorAnper.setDiscls(modelAnper.getDiscls());
		detectorAnper.setFbk(modelAnper.getFbk());
		detectorAnper.setGnd(modelAnper.getGnd());
		detectorAnper.setIkrum(modelAnper.getIkrum());
		detectorAnper.setPreamp(modelAnper.getPreamp());
		detectorAnper.setRpz(modelAnper.getRpz());
		detectorAnper.setShaper(modelAnper.getShaper());
		detectorAnper.setThreshold0(modelAnper.getThreshold0());
		detectorAnper.setThreshold1(modelAnper.getThreshold1());
		detectorAnper.setThreshold2(modelAnper.getThreshold2());
		detectorAnper.setThreshold3(modelAnper.getThreshold3());
		detectorAnper.setThreshold4(modelAnper.getThreshold4());
		detectorAnper.setThreshold5(modelAnper.getThreshold5());
		detectorAnper.setThreshold6(modelAnper.getThreshold6());
		detectorAnper.setThreshold7(modelAnper.getThreshold7());
		detectorAnper.setThresholdn(modelAnper.getThresholdn());
		detectorAnper.setTpBufferIn(modelAnper.getTpBufferIn());
		detectorAnper.setTpBufferOut(modelAnper.getTpBufferOut());
		detectorAnper.setTpref(modelAnper.getTpref());
		detectorAnper.setTprefA(modelAnper.getTprefA());
		detectorAnper.setTprefB(modelAnper.getTprefB());
	}

	private void setDetectorPixel(ChipPixel detectorPixel, PixelModel modelPixel) throws Exception {
		detectorPixel.setGainMode(modelPixel.getGainMode());
		detectorPixel.setMask(modelPixel.getMask());
		detectorPixel.setTest(modelPixel.getTest());
		detectorPixel.setThresholdA(modelPixel.getThresholdA());
		detectorPixel.setThresholdB(modelPixel.getThresholdB());
	}

}
