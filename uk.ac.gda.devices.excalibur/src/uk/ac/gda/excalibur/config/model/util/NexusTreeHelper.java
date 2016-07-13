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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.NXDetectorData;
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
 *
 */
public class NexusTreeHelper {

	private static final String SUMMARY_NODE = "SummaryNode";
	private static final String MST = "mst";
	private static final String FIX = "fix";
	private static final String GAP = "gap";
	private static final String PIXEL = "pixel";
	private static final String ANPER = "anper";
	private static final String CHIP_REG8 = "chipReg8";
	private static final String CHIP_REG7 = "chipReg7";
	private static final String CHIP_REG6 = "chipReg6";
	private static final String CHIP_REG5 = "chipReg5";
	private static final String CHIP_REG4 = "chipReg4";
	private static final String CHIP_REG3 = "chipReg3";
	private static final String CHIP_REG2 = "chipReg2";
	private static final String CHIP_REG1 = "chipReg1";
	private static final String FEM = "fem";
	private static final ExcaliburConfigPackage EXCONF_PKG = ExcaliburConfigPackage.eINSTANCE;
	public static NexusTreeHelper INSTANCE = new NexusTreeHelper();

	/**
	 * @param nexusTree
	 * @return {@link ExcaliburConfig} from the {@link INexusTree} provided. Iterates through the nexus tree and creates
	 *         model objects for each of the tree nodes.
	 */
	public ExcaliburConfig createModelFromNexus(INexusTree nexusTree) {
		ExcaliburConfig excaliburConfig = ExcaliburConfigFactory.eINSTANCE.createExcaliburConfig();
		INexusTree rootNode = nexusTree.getChildNode(0);
		// expecting 6 child nodes to be present
		for (int count = 1; count <= 6; count++) {
			ReadoutNode readoutNode = ExcaliburConfigFactory.eINSTANCE.createReadoutNode();
			INexusTree readoutTreeNode = rootNode.getChildNode(Integer.toString(count), EXCONF_PKG.getReadoutNode()
					.getName());
			readoutNode.setId(Integer.parseInt(readoutTreeNode.getName()));

			INexusTree femNode = readoutTreeNode.getChildNode(FEM, EXCONF_PKG.getReadoutNodeFemModel().getName());

			ReadoutNodeFemModel readoutNodeFemModel = ExcaliburConfigFactory.eINSTANCE.createReadoutNodeFemModel();
			readoutNodeFemModel.setCounterDepth((Integer) femNode
					.getChildNode(EXCONF_PKG.getReadoutNodeFemModel_CounterDepth().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());

			readoutNodeFemModel.setCounterSelect((Integer) femNode
					.getChildNode(EXCONF_PKG.getReadoutNodeFemModel_CounterSelect().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());

			readoutNodeFemModel.setOperationMode((Integer) femNode
					.getChildNode(EXCONF_PKG.getReadoutNodeFemModel_OperationMode().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());

			readoutNodeFemModel.setDacExternal((Integer) femNode
					.getChildNode(EXCONF_PKG.getReadoutNodeFemModel_DacExternal().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());
			readoutNodeFemModel.setDacSense((Integer) femNode
					.getChildNode(EXCONF_PKG.getReadoutNodeFemModel_DacSense().getName(), NexusExtractor.SDSClassName)
					.getData().getFirstValue());

			//
			INexusTree chip1RegTreeNode = femNode.getChildNode(CHIP_REG1, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel1 = createChipRegModel(chip1RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg1(chipRegModel1);
			//
			INexusTree chip2RegTreeNode = femNode.getChildNode(CHIP_REG2, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel2 = createChipRegModel(chip2RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg2(chipRegModel2);
			//
			INexusTree chip3RegTreeNode = femNode.getChildNode(CHIP_REG3, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel3 = createChipRegModel(chip3RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg3(chipRegModel3);
			//
			INexusTree chip4RegTreeNode = femNode.getChildNode(CHIP_REG4, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel4 = createChipRegModel(chip4RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg4(chipRegModel4);
			//
			INexusTree chip5RegTreeNode = femNode.getChildNode(CHIP_REG5, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel5 = createChipRegModel(chip5RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg5(chipRegModel5);
			//
			INexusTree chip6RegTreeNode = femNode.getChildNode(CHIP_REG6, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel6 = createChipRegModel(chip6RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg6(chipRegModel6);
			//
			INexusTree chip7RegTreeNode = femNode.getChildNode(CHIP_REG7, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel7 = createChipRegModel(chip7RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg7(chipRegModel7);
			//
			INexusTree chip8RegTreeNode = femNode.getChildNode(CHIP_REG8, EXCONF_PKG.getMpxiiiChipRegModel().getName());
			MpxiiiChipRegModel chipRegModel8 = createChipRegModel(chip8RegTreeNode);
			readoutNodeFemModel.setMpxiiiChipReg8(chipRegModel8);

			readoutNode.setReadoutNodeFem(readoutNodeFemModel);

			//
			GapModel gapModel = ExcaliburConfigFactory.eINSTANCE.createGapModel();
			INexusTree gapTreeNode = readoutTreeNode.getChildNode(GAP, EXCONF_PKG.getGapModel().getName());

			gapModel.setGapFillConstant((Integer) gapTreeNode
					.getChildNode(EXCONF_PKG.getGapModel_GapFillConstant().getName(), NexusExtractor.SDSClassName)
					.getData().getFirstValue());
			gapModel.setGapFillMode((Integer) gapTreeNode
					.getChildNode(EXCONF_PKG.getGapModel_GapFillMode().getName(), NexusExtractor.SDSClassName)
					.getData().getFirstValue());
			gapModel.setGapFillingEnabled(Boolean.parseBoolean(((String) gapTreeNode
					.getChildNode(EXCONF_PKG.getGapModel_GapFillingEnabled().getName(), NexusExtractor.SDSClassName)
					.getData().getFirstValue())));
			readoutNode.setGap(gapModel);

			//
			FixModel fixModel = ExcaliburConfigFactory.eINSTANCE.createFixModel();
			INexusTree fixTreeNode = readoutTreeNode.getChildNode(FIX, EXCONF_PKG.getFixModel().getName());
			fixModel.setScaleEdgePixelsEnabled(Boolean.parseBoolean(((String) fixTreeNode
					.getChildNode(EXCONF_PKG.getFixModel_ScaleEdgePixelsEnabled().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue())));
			fixModel.setStatisticsEnabled(Boolean.parseBoolean(((String) fixTreeNode
					.getChildNode(EXCONF_PKG.getFixModel_StatisticsEnabled().getName(), NexusExtractor.SDSClassName)
					.getData().getFirstValue())));
			readoutNode.setFix(fixModel);
			//
			MasterModel mstModel = ExcaliburConfigFactory.eINSTANCE.createMasterModel();
			INexusTree mstTreeNode = readoutTreeNode.getChildNode(MST, EXCONF_PKG.getMasterModel().getName());
			mstModel.setFrameDivisor((Integer) mstTreeNode
					.getChildNode(EXCONF_PKG.getMasterModel_FrameDivisor().getName(), NexusExtractor.SDSClassName)
					.getData().getFirstValue());
			readoutNode.setMst(mstModel);
			//
			excaliburConfig.getReadoutNodes().add(readoutNode);
		}

		INexusTree summaryTreeNode = rootNode.getChildNode(SUMMARY_NODE, EXCONF_PKG.getSummaryNode().getName());
		if (summaryTreeNode != null) {
			SummaryNode summaryNode = ExcaliburConfigFactory.eINSTANCE.createSummaryNode();
			SummaryAdbaseModel summFem = ExcaliburConfigFactory.eINSTANCE.createSummaryAdbaseModel();
			summFem.setCounterDepth((Integer) summaryTreeNode
					.getChildNode(EXCONF_PKG.getSummaryAdbaseModel_CounterDepth().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());
			summFem.setFrameDivisor((Integer) summaryTreeNode
					.getChildNode(EXCONF_PKG.getSummaryAdbaseModel_FrameDivisor().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());
			summFem.setGapFillConstant((Integer) summaryTreeNode
					.getChildNode(EXCONF_PKG.getSummaryAdbaseModel_GapFillConstant().getName(),
							NexusExtractor.SDSClassName).getData().getFirstValue());
			summaryNode.setSummaryFem(summFem);
			excaliburConfig.setSummaryNode(summaryNode);
		}

		return excaliburConfig;
	}

	/**
	 * @param chip1RegTreeNode
	 * @return {@link MpxiiiChipRegModel}
	 */
	protected MpxiiiChipRegModel createChipRegModel(INexusTree chip1RegTreeNode) {
		MpxiiiChipRegModel chipRegModel = ExcaliburConfigFactory.eINSTANCE.createMpxiiiChipRegModel();

		chipRegModel.setChipDisable((Boolean) chip1RegTreeNode
				.getChildNode(EXCONF_PKG.getMpxiiiChipRegModel_ChipDisable().getName(), NexusExtractor.SDSClassName)
				.getData().getFirstValue());
		//
		INexusTree anperTreeNode = chip1RegTreeNode.getChildNode(ANPER, EXCONF_PKG.getAnperModel().getName());

		AnperModel anperModel = ExcaliburConfigFactory.eINSTANCE.createAnperModel();

		anperModel.setCas((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Cas().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setDacPixel((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_DacPixel().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setDelay((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Delay().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setDisc((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Disc().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setDiscls((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Discls().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setFbk((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Fbk().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setGnd((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Gnd().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setIkrum((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Ikrum().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setPreamp((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Preamp().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setRpz((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Rpz().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setShaper((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Shaper().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold0((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold0().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold1((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold1().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold2((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold2().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold3((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold3().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold4((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold4().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold5((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold5().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold6((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold6().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThreshold7((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Threshold7().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setThresholdn((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_Thresholdn().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setTpBufferIn((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_TpBufferIn().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setTpBufferOut((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_TpBufferOut().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setTprefA((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_TprefA().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		anperModel.setTprefB((Integer) anperTreeNode
				.getChildNode(EXCONF_PKG.getAnperModel_TprefB().getName(), NexusExtractor.SDSClassName).getData()
				.getFirstValue());

		chipRegModel.setAnper(anperModel);
		//
		INexusTree pixelTreeNode = chip1RegTreeNode.getChildNode(PIXEL, EXCONF_PKG.getPixelModel().getName());

		PixelModel pixelModel = ExcaliburConfigFactory.eINSTANCE.createPixelModel();

		pixelModel.setGainMode((short[]) pixelTreeNode
				.getChildNode(EXCONF_PKG.getPixelModel_GainMode().getName(), NexusExtractor.SDSClassName).getData()
				.getBuffer());

		pixelModel.setMask((short[]) pixelTreeNode
				.getChildNode(EXCONF_PKG.getPixelModel_Mask().getName(), NexusExtractor.SDSClassName).getData()
				.getBuffer());
		pixelModel.setTest((short[]) pixelTreeNode
				.getChildNode(EXCONF_PKG.getPixelModel_Test().getName(), NexusExtractor.SDSClassName).getData()
				.getBuffer());
		pixelModel.setThresholdA((short[]) pixelTreeNode
				.getChildNode(EXCONF_PKG.getPixelModel_ThresholdA().getName(), NexusExtractor.SDSClassName).getData()
				.getBuffer());
		pixelModel.setThresholdB((short[]) pixelTreeNode
				.getChildNode(EXCONF_PKG.getPixelModel_ThresholdB().getName(), NexusExtractor.SDSClassName).getData()
				.getBuffer());
		chipRegModel.setPixel(pixelModel);
		return chipRegModel;
	}

	/**
	 * @param detectorName
	 * @param excaliburConfig
	 * @return {@link NXDetectorData} - creates the {@link INexusTree} from the {@link ExcaliburConfig}
	 */
	public NXDetectorData saveToDetectorData(String detectorName, ExcaliburConfig excaliburConfig) {
		// Create a resource set
		//
		NXDetectorData data = new NXDetectorData();
		INexusTree detTree = data.getDetTree(detectorName);
		for (ReadoutNode node : excaliburConfig.getReadoutNodes()) {
			// ReadoutNode
			NexusTreeNode nxnode = new NexusTreeNode(Integer.toString(node.getId()), EXCONF_PKG.getReadoutNode()
					.getName(), null, null);

			ReadoutNodeFemModel fem = node.getReadoutNodeFem();
			NexusTreeNode nxfem = new NexusTreeNode(FEM, EXCONF_PKG.getReadoutNodeFemModel().getName(), nxnode);

			nxfem.addChildNode(new NexusTreeNode(EXCONF_PKG.getReadoutNodeFemModel_CounterDepth().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(fem.getCounterDepth())));

			nxfem.addChildNode(new NexusTreeNode(EXCONF_PKG.getReadoutNodeFemModel_CounterSelect().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(fem.getCounterSelect())));

			nxfem.addChildNode(new NexusTreeNode(EXCONF_PKG.getReadoutNodeFemModel_OperationMode().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(fem.getOperationMode())));

			nxfem.addChildNode(new NexusTreeNode(EXCONF_PKG.getReadoutNodeFemModel_DacExternal().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(fem.getDacExternal())));

			nxfem.addChildNode(new NexusTreeNode(EXCONF_PKG.getReadoutNodeFemModel_DacSense().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(fem.getDacSense())));

			//addChipRegNode(nxfem, fem.getMpxiiiChipReg1(), CHIP_REG1);	// kw thu 17 dec 2015
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg2(), CHIP_REG2);
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg3(), CHIP_REG3);
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg4(), CHIP_REG4);
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg5(), CHIP_REG5);
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg6(), CHIP_REG6);
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg7(), CHIP_REG7);
			//addChipRegNode(nxfem, fem.getMpxiiiChipReg8(), CHIP_REG8);

			nxnode.addChildNode(nxfem);

			// Gap
			GapModel gapModel = node.getGap();
			NexusTreeNode gapTreeNode = new NexusTreeNode(GAP, EXCONF_PKG.getGapModel().getName(), nxnode);
			gapTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getGapModel_GapFillConstant().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(gapModel.getGapFillConstant())));
			gapTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getGapModel_GapFillMode().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(gapModel.getGapFillMode())));
			gapTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getGapModel_GapFillingEnabled().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(Boolean.toString(gapModel
							.isGapFillingEnabled()))));
			nxnode.addChildNode(gapTreeNode);
			// Mst
			MasterModel masterModel = node.getMst();
			NexusTreeNode mstTreeNode = new NexusTreeNode(MST, EXCONF_PKG.getMasterModel().getName(), nxnode);
			mstTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getMasterModel_FrameDivisor().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(masterModel.getFrameDivisor())));
			nxnode.addChildNode(mstTreeNode);
			// Fix
			FixModel fixModel = node.getFix();
			NexusTreeNode fixTreeNode = new NexusTreeNode(FIX, EXCONF_PKG.getFixModel().getName(), nxnode);
			fixTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getFixModel_ScaleEdgePixelsEnabled().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(Boolean.toString(fixModel
							.isScaleEdgePixelsEnabled()))));
			fixTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getFixModel_StatisticsEnabled().getName(),
					NexusExtractor.SDSClassName, nxfem, new NexusGroupData(Boolean.toString(fixModel
							.isStatisticsEnabled()))));
			nxnode.addChildNode(fixTreeNode);

			//
			detTree.addChildNode(nxnode);
		}
		SummaryNode summaryNode = excaliburConfig.getSummaryNode();
		if (summaryNode != null) {
			// Summary
			SummaryAdbaseModel summaryFem = summaryNode.getSummaryFem();

			NexusTreeNode summaryTreeNode = new NexusTreeNode(SUMMARY_NODE, EXCONF_PKG.getSummaryNode().getName(),
					null, null);
			summaryTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getSummaryAdbaseModel_CounterDepth().getName(),
					NexusExtractor.SDSClassName, summaryTreeNode, new NexusGroupData(summaryFem.getCounterDepth())));
			summaryTreeNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getSummaryAdbaseModel_FrameDivisor().getName(),
					NexusExtractor.SDSClassName, summaryTreeNode, new NexusGroupData(summaryFem.getFrameDivisor())));
			summaryTreeNode.addChildNode(new NexusTreeNode(
					EXCONF_PKG.getSummaryAdbaseModel_GapFillConstant().getName(), NexusExtractor.SDSClassName,
					summaryTreeNode, new NexusGroupData(summaryFem.getGapFillConstant())));
			detTree.addChildNode(summaryTreeNode);
		}
		return data;
	}

	/**
	 * @param nxfem
	 * @param mpxiiiChipReg
	 */
	protected void addChipRegNode(NexusTreeNode nxfem, MpxiiiChipRegModel mpxiiiChipReg, String chipRegName) {
		NexusTreeNode chipReg = new NexusTreeNode(chipRegName, EXCONF_PKG.getMpxiiiChipRegModel().getName(), nxfem);

		chipReg.addChildNode(new NexusTreeNode(EXCONF_PKG.getMpxiiiChipRegModel_ChipDisable().getName(),
				NexusExtractor.SDSClassName, chipReg, new NexusGroupData(
						Boolean.toString(mpxiiiChipReg.isChipDisable()))));

		AnperModel anperModel = mpxiiiChipReg.getAnper();
		NexusTreeNode anperNode = new NexusTreeNode(ANPER, EXCONF_PKG.getAnperModel().getName(), chipReg);
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Cas().getName(), NexusExtractor.SDSClassName,
				anperNode, new NexusGroupData(anperModel.getCas())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_DacPixel().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getDacPixel())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Delay().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getDelay())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Disc().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getDisc())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Discls().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getDiscls())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Fbk().getName(), NexusExtractor.SDSClassName,
				anperNode, new NexusGroupData(anperModel.getFbk())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Gnd().getName(), NexusExtractor.SDSClassName,
				anperNode, new NexusGroupData(anperModel.getGnd())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Ikrum().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getIkrum())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Preamp().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getPreamp())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Rpz().getName(), NexusExtractor.SDSClassName,
				anperNode, new NexusGroupData(anperModel.getRpz())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Shaper().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getShaper())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold0().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold0())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold1().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold1())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold2().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold2())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold3().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold3())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold4().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold4())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold5().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold5())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold6().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold6())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Threshold7().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThreshold7())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Thresholdn().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getThresholdn())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_TpBufferIn().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getTpBufferIn())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_TpBufferOut().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getTpBufferOut())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_Tpref().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getTpref())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_TprefA().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getTprefA())));
		anperNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getAnperModel_TprefB().getName(),
				NexusExtractor.SDSClassName, anperNode, new NexusGroupData(anperModel.getTprefB())));

		chipReg.addChildNode(anperNode);
		//
		PixelModel pixelModel = mpxiiiChipReg.getPixel();
		NexusTreeNode pixelNode = new NexusTreeNode(PIXEL, EXCONF_PKG.getPixelModel().getName(), chipReg);

		pixelNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getPixelModel_GainMode().getName(),
				NexusExtractor.SDSClassName, pixelNode, new NexusGroupData(new int[] { 256, 256 }, pixelModel.getGainMode())));

		pixelNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getPixelModel_Mask().getName(),
				NexusExtractor.SDSClassName, pixelNode, new NexusGroupData(new int[] { 256, 256 }, pixelModel.getMask())));

		pixelNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getPixelModel_Test().getName(),
				NexusExtractor.SDSClassName, pixelNode, new NexusGroupData(new int[] { 256, 256 }, pixelModel.getTest())));

		pixelNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getPixelModel_ThresholdA().getName(),
				NexusExtractor.SDSClassName, pixelNode, new NexusGroupData(new int[] { 256, 256 }, pixelModel.getThresholdA())));

		pixelNode.addChildNode(new NexusTreeNode(EXCONF_PKG.getPixelModel_ThresholdB().getName(),
				NexusExtractor.SDSClassName, pixelNode, new NexusGroupData(new int[] { 256, 256 }, pixelModel.getThresholdB())));
		chipReg.addChildNode(pixelNode);
		//
		nxfem.addChildNode(chipReg);
	}
}