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

package gda.device.detector.areadetector.v17.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.NDOverlay;
import gda.device.detector.areadetector.v17.NDPluginBase;

public class DummyNDOverlay implements NDOverlay {

	private static final String portName_RBV = "DummyNDOverlay_port";

	private static final Logger logger = LoggerFactory.getLogger(DummyNDOverlay.class);

	private NDPluginBase pluginBase;

	private String name0;
	private String name_RBV0;
	private int use0;
	private int positionXLink0;
	private int positionX0;
	private int positionYLink0;
	private int positionY0;
	private int sizeXLink0;
	private int sizeX0;
	private int sizeYLink0;
	private int sizeY0;
	private int shape0;
	private int drawMode0;
	private int red0;
	private int green0;
	private int blue0;

	private String name1;
	private String name_RBV1;
	private int use1;
	private int positionXLink1;
	private int positionX1;
	private int positionYLink1;
	private int positionY1;
	private int sizeXLink1;
	private int sizeX1;
	private int sizeYLink1;
	private int sizeY1;
	private int shape1;
	private int drawMode1;
	private int red1;
	private int green1;
	private int blue1;

	private String name2;
	private String name_RBV2;
	private int use2;
	private int positionXLink2;
	private int positionX2;
	private int positionYLink2;
	private int positionY2;
	private int sizeXLink2;
	private int sizeX2;
	private int sizeYLink2;
	private int sizeY2;
	private int shape2;
	private int drawMode2;
	private int red2;
	private int green2;
	private int blue2;

	private String name3;
	private String name_RBV3;
	private int use3;
	private int positionXLink3;
	private int positionX3;
	private int positionYLink3;
	private int positionY3;
	private int sizeXLink3;
	private int sizeX3;
	private int sizeYLink3;
	private int sizeY3;
	private int shape3;
	private int drawMode3;
	private int red3;
	private int green3;
	private int blue3;

	private String name4;
	private String name_RBV4;
	private int use4;
	private int positionXLink4;
	private int positionX4;
	private int positionYLink4;
	private int positionY4;
	private int sizeXLink4;
	private int sizeX4;
	private int sizeYLink4;
	private int sizeY4;
	private int shape4;
	private int drawMode4;
	private int red4;
	private int green4;
	private int blue4;

	private String name5;
	private String name_RBV5;
	private int use5;
	private int positionXLink5;
	private int positionX5;
	private int positionYLink5;
	private int positionY5;
	private int sizeXLink5;
	private int sizeX5;
	private int sizeYLink5;
	private int sizeY5;
	private int shape5;
	private int drawMode5;
	private int red5;
	private int green5;
	private int blue5;

	private String name6;
	private String name_RBV6;
	private int use6;
	private int positionXLink6;
	private int positionX6;
	private int positionYLink6;
	private int positionY6;
	private int sizeXLink6;
	private int sizeX6;
	private int sizeYLink6;
	private int sizeY6;
	private int shape6;
	private int drawMode6;
	private int red6;
	private int green6;
	private int blue6;

	private String name7;
	private String name_RBV7;
	private int use7;
	private int positionXLink7;
	private int positionX7;
	private int positionYLink7;
	private int positionY7;
	private int sizeXLink7;
	private int sizeX7;
	private int sizeYLink7;
	private int sizeY7;
	private int shape7;
	private int drawMode7;
	private int red7;
	private int green7;
	private int blue7;

	// -------------------------------------------------------------------------------------------------------------

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	@Override
	public String getPortName_RBV() throws Exception {
		return portName_RBV;
	}

	@Override
	public String getName0() throws Exception {
		return name0;
	}

	@Override
	public void setName0(String name0) throws Exception {
		this.name0 = name0;
	}

	@Override
	public String getName_RBV0() throws Exception {
		return name_RBV0;
	}

	@Override
	public void setName_RBV0(String name_rbv0) throws Exception {
		this.name_RBV0 = name_rbv0;
	}

	@Override
	public short getUse0() throws Exception {
		return (short) use0;
	}

	@Override
	public void setUse0(int use0) throws Exception {
		this.use0 = use0;
	}

	@Override
	public short getUse0_RBV() throws Exception {
		return (short) use0;
	}

	@Override
	public int getPositionXLink0() throws Exception {
		return positionXLink0;
	}

	@Override
	public void setPositionXLink0(int positionxlink0) throws Exception {
		this.positionXLink0 = positionxlink0;
	}

	@Override
	public int getPositionX0() throws Exception {
		return positionX0;
	}

	@Override
	public void setPositionX0(int positionx0) throws Exception {
		this.positionX0 = positionx0;
	}

	@Override
	public int getPositionX0_RBV() throws Exception {
		return positionX0;
	}

	@Override
	public int getPositionYLink0() throws Exception {
		return positionYLink0;
	}

	@Override
	public void setPositionYLink0(int positionylink0) throws Exception {
		this.positionYLink0 = positionylink0;
	}

	@Override
	public int getPositionY0() throws Exception {
		return positionY0;
	}

	@Override
	public void setPositionY0(int positiony0) throws Exception {
		this.positionY0 = positiony0;
	}

	@Override
	public int getPositionY0_RBV() throws Exception {
		return positionY0;
	}

	@Override
	public int getSizeXLink0() throws Exception {
		return sizeXLink0;
	}

	@Override
	public void setSizeXLink0(int sizexlink0) throws Exception {
		this.sizeXLink0 = sizexlink0;
	}

	@Override
	public int getSizeX0() throws Exception {
		return sizeX0;
	}

	@Override
	public void setSizeX0(int sizex0) throws Exception {
		this.sizeX0 = sizex0;
	}

	@Override
	public int getSizeX0_RBV() throws Exception {
		return sizeX0;
	}

	@Override
	public int getSizeYLink0() throws Exception {
		return sizeYLink0;
	}

	@Override
	public void setSizeYLink0(int sizeylink0) throws Exception {
		this.sizeYLink0 = sizeylink0;
	}

	@Override
	public int getSizeY0() throws Exception {
		return sizeY0;
	}

	@Override
	public void setSizeY0(int sizey0) throws Exception {
		this.sizeY0 = sizey0;
	}

	@Override
	public int getSizeY0_RBV() throws Exception {
		return sizeY0;
	}

	@Override
	public short getShape0() throws Exception {
		return (short) shape0;
	}

	@Override
	public void setShape0(int shape0) throws Exception {
		this.shape0 = shape0;
	}

	@Override
	public short getShape0_RBV() throws Exception {
		return (short) shape0;
	}

	@Override
	public short getDrawMode0() throws Exception {
		return (short) drawMode0;
	}

	@Override
	public void setDrawMode0(int drawmode0) throws Exception {
		this.drawMode0 = drawmode0;
	}

	@Override
	public short getDrawMode0_RBV() throws Exception {
		return (short) drawMode0;
	}

	@Override
	public int getRed0() throws Exception {
		return red0;
	}

	@Override
	public void setRed0(int red0) throws Exception {
		this.red0 = red0;
	}

	@Override
	public int getRed0_RBV() throws Exception {
		return red0;
	}

	@Override
	public int getGreen0() throws Exception {
		return green0;
	}

	@Override
	public void setGreen0(int green0) throws Exception {
		this.green0 = green0;
	}

	@Override
	public int getGreen0_RBV() throws Exception {
		return green0;
	}

	@Override
	public int getBlue0() throws Exception {
		return blue0;
	}

	@Override
	public void setBlue0(int blue0) throws Exception {
		this.blue0 = blue0;
	}

	@Override
	public int getBlue0_RBV() throws Exception {
		return blue0;
	}

	@Override
	public String getName1() throws Exception {
		return name1;
	}

	@Override
	public void setName1(String name1) throws Exception {
		this.name1 = name1;
	}

	@Override
	public String getName_RBV1() throws Exception {
		return name_RBV1;
	}

	@Override
	public void setName_RBV1(String name_rbv1) throws Exception {
		this.name_RBV1 = name_rbv1;
	}

	@Override
	public short getUse1() throws Exception {
		return (short) use1;
	}

	@Override
	public void setUse1(int use1) throws Exception {
		this.use1 = use1;
	}

	@Override
	public short getUse1_RBV() throws Exception {
		return (short) use1;
	}

	@Override
	public int getPositionXLink1() throws Exception {
		return positionXLink1;
	}

	@Override
	public void setPositionXLink1(int positionxlink1) throws Exception {
		this.positionXLink1 = positionxlink1;
	}

	@Override
	public int getPositionX1() throws Exception {
		return positionX1;
	}

	@Override
	public void setPositionX1(int positionx1) throws Exception {
		this.positionX1 = positionx1;
	}

	@Override
	public int getPositionX1_RBV() throws Exception {
		return positionX1;
	}

	@Override
	public int getPositionYLink1() throws Exception {
		return positionYLink1;
	}

	@Override
	public void setPositionYLink1(int positionylink1) throws Exception {
		this.positionYLink1 = positionylink1;
	}

	@Override
	public int getPositionY1() throws Exception {
		return positionY1;
	}

	@Override
	public void setPositionY1(int positiony1) throws Exception {
		this.positionY1 = positiony1;
	}

	@Override
	public int getPositionY1_RBV() throws Exception {
		return positionY1;
	}

	@Override
	public int getSizeXLink1() throws Exception {
		return sizeXLink1;
	}

	@Override
	public void setSizeXLink1(int sizexlink1) throws Exception {
		this.sizeXLink1 = sizexlink1;
	}

	@Override
	public int getSizeX1() throws Exception {
		return sizeX1;
	}

	@Override
	public void setSizeX1(int sizex1) throws Exception {
		this.sizeX1 = sizex1;
	}

	@Override
	public int getSizeX1_RBV() throws Exception {
		return sizeX1;
	}

	@Override
	public int getSizeYLink1() throws Exception {
		return sizeYLink1;
	}

	@Override
	public void setSizeYLink1(int sizeylink1) throws Exception {
		this.sizeYLink1 = sizeylink1;
	}

	@Override
	public int getSizeY1() throws Exception {
		return sizeY1;
	}

	@Override
	public void setSizeY1(int sizey1) throws Exception {
		this.sizeY1 = sizey1;
	}

	@Override
	public int getSizeY1_RBV() throws Exception {
		return sizeY1;
	}

	@Override
	public short getShape1() throws Exception {
		return (short) shape1;
	}

	@Override
	public void setShape1(int shape1) throws Exception {
		this.shape1 = shape1;
	}

	@Override
	public short getShape1_RBV() throws Exception {
		return (short) shape1;
	}

	@Override
	public short getDrawMode1() throws Exception {
		return (short) drawMode1;
	}

	@Override
	public void setDrawMode1(int drawmode1) throws Exception {
		this.drawMode1 = drawmode1;
	}

	@Override
	public short getDrawMode1_RBV() throws Exception {
		return (short) drawMode1;
	}

	@Override
	public int getRed1() throws Exception {
		return red1;
	}

	@Override
	public void setRed1(int red1) throws Exception {
		this.red1 = red1;
	}

	@Override
	public int getRed1_RBV() throws Exception {
		return red1;
	}

	@Override
	public int getGreen1() throws Exception {
		return green1;
	}

	@Override
	public void setGreen1(int green1) throws Exception {
		this.green1 = green1;
	}

	@Override
	public int getGreen1_RBV() throws Exception {
		return green1;
	}

	@Override
	public int getBlue1() throws Exception {
		return blue1;
	}

	@Override
	public void setBlue1(int blue1) throws Exception {
		this.blue1 = blue1;
	}

	@Override
	public int getBlue1_RBV() throws Exception {
		return blue1;
	}

	@Override
	public String getName2() throws Exception {
		return name2;
	}

	@Override
	public void setName2(String name2) throws Exception {
		this.name2 = name2;
	}

	@Override
	public String getName_RBV2() throws Exception {
		return name_RBV2;
	}

	@Override
	public void setName_RBV2(String name_rbv2) throws Exception {
		this.name_RBV2 = name_rbv2;
	}

	@Override
	public short getUse2() throws Exception {
		return (short) use2;
	}

	@Override
	public void setUse2(int use2) throws Exception {
		this.use2 = use2;
	}

	@Override
	public short getUse2_RBV() throws Exception {
		return (short) use2;
	}

	@Override
	public int getPositionXLink2() throws Exception {
		return positionXLink2;
	}

	@Override
	public void setPositionXLink2(int positionxlink2) throws Exception {
		this.positionXLink2 = positionxlink2;
	}

	@Override
	public int getPositionX2() throws Exception {
		return positionX2;
	}

	@Override
	public void setPositionX2(int positionx2) throws Exception {
		this.positionX2 = positionx2;
	}

	@Override
	public int getPositionX2_RBV() throws Exception {
		return positionX2;
	}

	@Override
	public int getPositionYLink2() throws Exception {
		return positionYLink2;
	}

	@Override
	public void setPositionYLink2(int positionylink2) throws Exception {
		this.positionYLink2 = positionylink2;
	}

	@Override
	public int getPositionY2() throws Exception {
		return positionY2;
	}

	@Override
	public void setPositionY2(int positiony2) throws Exception {
		this.positionY2 = positiony2;
	}

	@Override
	public int getPositionY2_RBV() throws Exception {
		return positionY2;
	}

	@Override
	public int getSizeXLink2() throws Exception {
		return sizeXLink2;
	}

	@Override
	public void setSizeXLink2(int sizexlink2) throws Exception {
		this.sizeXLink2 = sizexlink2;
	}

	@Override
	public int getSizeX2() throws Exception {
		return sizeX2;
	}

	@Override
	public void setSizeX2(int sizex2) throws Exception {
		this.sizeX2 = sizex2;
	}

	@Override
	public int getSizeX2_RBV() throws Exception {
		return sizeX2;
	}

	@Override
	public int getSizeYLink2() throws Exception {
		return sizeYLink2;
	}

	@Override
	public void setSizeYLink2(int sizeylink2) throws Exception {
		this.sizeYLink2 = sizeylink2;
	}

	@Override
	public int getSizeY2() throws Exception {
		return sizeY2;
	}

	@Override
	public void setSizeY2(int sizey2) throws Exception {
		this.sizeY2 = sizey2;
	}

	@Override
	public int getSizeY2_RBV() throws Exception {
		return sizeY2;
	}

	@Override
	public short getShape2() throws Exception {
		return (short) shape2;
	}

	@Override
	public void setShape2(int shape2) throws Exception {
		this.shape2 = shape2;
	}

	@Override
	public short getShape2_RBV() throws Exception {
		return (short) shape2;
	}

	@Override
	public short getDrawMode2() throws Exception {
		return (short) drawMode2;
	}

	@Override
	public void setDrawMode2(int drawmode2) throws Exception {
		this.drawMode2 = drawmode2;
	}

	@Override
	public short getDrawMode2_RBV() throws Exception {
		return (short) drawMode2;
	}

	@Override
	public int getRed2() throws Exception {
		return red2;
	}

	@Override
	public void setRed2(int red2) throws Exception {
		this.red2 = red2;
	}

	@Override
	public int getRed2_RBV() throws Exception {
		return red2;
	}

	@Override
	public int getGreen2() throws Exception {
		return green2;
	}

	@Override
	public void setGreen2(int green2) throws Exception {
		this.green2 = green2;
	}

	@Override
	public int getGreen2_RBV() throws Exception {
		return green2;
	}

	@Override
	public int getBlue2() throws Exception {
		return blue2;
	}

	@Override
	public void setBlue2(int blue2) throws Exception {
		this.blue2 = blue2;
	}

	@Override
	public int getBlue2_RBV() throws Exception {
		return blue2;
	}

	@Override
	public String getName3() throws Exception {
		return name3;
	}

	@Override
	public void setName3(String name3) throws Exception {
		this.name3 = name3;
	}

	@Override
	public String getName_RBV3() throws Exception {
		return name_RBV3;
	}

	@Override
	public void setName_RBV3(String name_rbv3) throws Exception {
		this.name_RBV3 = name_rbv3;
	}

	@Override
	public short getUse3() throws Exception {
		return (short) use3;
	}

	@Override
	public void setUse3(int use3) throws Exception {
		this.use3 = use3;
	}

	@Override
	public short getUse3_RBV() throws Exception {
		return (short) use3;
	}

	@Override
	public int getPositionXLink3() throws Exception {
		return positionXLink3;
	}

	@Override
	public void setPositionXLink3(int positionxlink3) throws Exception {
		this.positionXLink3 = positionxlink3;
	}

	@Override
	public int getPositionX3() throws Exception {
		return positionX3;
	}

	@Override
	public void setPositionX3(int positionx3) throws Exception {
		this.positionX3 = positionx3;
	}

	@Override
	public int getPositionX3_RBV() throws Exception {
		return positionX3;
	}

	@Override
	public int getPositionYLink3() throws Exception {
		return positionYLink3;
	}

	@Override
	public void setPositionYLink3(int positionylink3) throws Exception {
		this.positionYLink3 = positionylink3;
	}

	@Override
	public int getPositionY3() throws Exception {
		return positionY3;
	}

	@Override
	public void setPositionY3(int positiony3) throws Exception {
		this.positionY3 = positiony3;
	}

	@Override
	public int getPositionY3_RBV() throws Exception {
		return positionY3;
	}

	@Override
	public int getSizeXLink3() throws Exception {
		return sizeXLink3;
	}

	@Override
	public void setSizeXLink3(int sizexlink3) throws Exception {
		this.sizeXLink3 = sizexlink3;
	}

	@Override
	public int getSizeX3() throws Exception {
		return sizeX3;
	}

	@Override
	public void setSizeX3(int sizex3) throws Exception {
		this.sizeX3 = sizex3;
	}

	@Override
	public int getSizeX3_RBV() throws Exception {
		return sizeX3;
	}

	@Override
	public int getSizeYLink3() throws Exception {
		return sizeYLink3;
	}

	@Override
	public void setSizeYLink3(int sizeylink3) throws Exception {
		this.sizeYLink3 = sizeylink3;
	}

	@Override
	public int getSizeY3() throws Exception {
		return sizeY3;
	}

	@Override
	public void setSizeY3(int sizey3) throws Exception {
		this.sizeY3 = sizey3;
	}

	@Override
	public int getSizeY3_RBV() throws Exception {
		return sizeY3;
	}

	@Override
	public short getShape3() throws Exception {
		return (short) shape3;
	}

	@Override
	public void setShape3(int shape3) throws Exception {
		this.shape3 = shape3;
	}

	@Override
	public short getShape3_RBV() throws Exception {
		return (short) shape3;
	}

	@Override
	public short getDrawMode3() throws Exception {
		return (short) drawMode3;
	}

	@Override
	public void setDrawMode3(int drawmode3) throws Exception {
		this.drawMode3 = drawmode3;
	}

	@Override
	public short getDrawMode3_RBV() throws Exception {
		return (short) drawMode3;
	}

	@Override
	public int getRed3() throws Exception {
		return red3;
	}

	@Override
	public void setRed3(int red3) throws Exception {
		this.red3 = red3;
	}

	@Override
	public int getRed3_RBV() throws Exception {
		return red3;
	}

	@Override
	public int getGreen3() throws Exception {
		return green3;
	}

	@Override
	public void setGreen3(int green3) throws Exception {
		this.green3 = green3;
	}

	@Override
	public int getGreen3_RBV() throws Exception {
		return green3;
	}

	@Override
	public int getBlue3() throws Exception {
		return blue3;
	}

	@Override
	public void setBlue3(int blue3) throws Exception {
		this.blue3 = blue3;
	}

	@Override
	public int getBlue3_RBV() throws Exception {
		return blue3;
	}

	@Override
	public String getName4() throws Exception {
		return name4;
	}

	@Override
	public void setName4(String name4) throws Exception {
		this.name4 = name4;
	}

	@Override
	public String getName_RBV4() throws Exception {
		return name_RBV4;
	}

	@Override
	public void setName_RBV4(String name_rbv4) throws Exception {
		this.name_RBV4 = name_rbv4;
	}

	@Override
	public short getUse4() throws Exception {
		return (short) use4;
	}

	@Override
	public void setUse4(int use4) throws Exception {
		this.use4 = use4;
	}

	@Override
	public short getUse4_RBV() throws Exception {
		return (short) use4;
	}

	@Override
	public int getPositionXLink4() throws Exception {
		return positionXLink4;
	}

	@Override
	public void setPositionXLink4(int positionxlink4) throws Exception {
		this.positionXLink4 = positionxlink4;
	}

	@Override
	public int getPositionX4() throws Exception {
		return positionX4;
	}

	@Override
	public void setPositionX4(int positionx4) throws Exception {
		this.positionX4 = positionx4;
	}

	@Override
	public int getPositionX4_RBV() throws Exception {
		return positionX4;
	}

	@Override
	public int getPositionYLink4() throws Exception {
		return positionYLink4;
	}

	@Override
	public void setPositionYLink4(int positionylink4) throws Exception {
		this.positionYLink4 = positionylink4;
	}

	@Override
	public int getPositionY4() throws Exception {
		return positionY4;
	}

	@Override
	public void setPositionY4(int positiony4) throws Exception {
		this.positionY4 = positiony4;
	}

	@Override
	public int getPositionY4_RBV() throws Exception {
		return positionY4;
	}

	@Override
	public int getSizeXLink4() throws Exception {
		return sizeXLink4;
	}

	@Override
	public void setSizeXLink4(int sizexlink4) throws Exception {
		this.sizeXLink4 = sizexlink4;
	}

	@Override
	public int getSizeX4() throws Exception {
		return sizeX4;
	}

	@Override
	public void setSizeX4(int sizex4) throws Exception {
		this.sizeX4 = sizex4;
	}

	@Override
	public int getSizeX4_RBV() throws Exception {
		return sizeX4;
	}

	@Override
	public int getSizeYLink4() throws Exception {
		return sizeYLink4;
	}

	@Override
	public void setSizeYLink4(int sizeylink4) throws Exception {
		this.sizeYLink4 = sizeylink4;
	}

	@Override
	public int getSizeY4() throws Exception {
		return sizeY4;
	}

	@Override
	public void setSizeY4(int sizey4) throws Exception {
		this.sizeY4 = sizey4;
	}

	@Override
	public int getSizeY4_RBV() throws Exception {
		return sizeY4;
	}

	@Override
	public short getShape4() throws Exception {
		return (short) shape4;
	}

	@Override
	public void setShape4(int shape4) throws Exception {
		this.shape4 = shape4;
	}

	@Override
	public short getShape4_RBV() throws Exception {
		return (short) shape4;
	}

	@Override
	public short getDrawMode4() throws Exception {
		return (short) drawMode4;
	}

	@Override
	public void setDrawMode4(int drawmode4) throws Exception {
		this.drawMode4 = drawmode4;
	}

	@Override
	public short getDrawMode4_RBV() throws Exception {
		return (short) drawMode4;
	}

	@Override
	public int getRed4() throws Exception {
		return red4;
	}

	@Override
	public void setRed4(int red4) throws Exception {
		this.red4 = red4;
	}

	@Override
	public int getRed4_RBV() throws Exception {
		return red4;
	}

	@Override
	public int getGreen4() throws Exception {
		return green4;
	}

	@Override
	public void setGreen4(int green4) throws Exception {
		this.green4 = green4;
	}

	@Override
	public int getGreen4_RBV() throws Exception {
		return green4;
	}

	@Override
	public int getBlue4() throws Exception {
		return blue4;
	}

	@Override
	public void setBlue4(int blue4) throws Exception {
		this.blue4 = blue4;
	}

	@Override
	public int getBlue4_RBV() throws Exception {
		return blue4;
	}

	@Override
	public String getName5() throws Exception {
		return name5;
	}

	@Override
	public void setName5(String name5) throws Exception {
		this.name5 = name5;
	}

	@Override
	public String getName_RBV5() throws Exception {
		return name_RBV5;
	}

	@Override
	public void setName_RBV5(String name_rbv5) throws Exception {
		this.name_RBV5 = name_rbv5;
	}

	@Override
	public short getUse5() throws Exception {
		return (short) use5;
	}

	@Override
	public void setUse5(int use5) throws Exception {
		this.use5 = use5;
	}

	@Override
	public short getUse5_RBV() throws Exception {
		return (short) use5;
	}

	@Override
	public int getPositionXLink5() throws Exception {
		return positionXLink5;
	}

	@Override
	public void setPositionXLink5(int positionxlink5) throws Exception {
		this.positionXLink5 = positionxlink5;
	}

	@Override
	public int getPositionX5() throws Exception {
		return positionX5;
	}

	@Override
	public void setPositionX5(int positionx5) throws Exception {
		this.positionX5 = positionx5;
	}

	@Override
	public int getPositionX5_RBV() throws Exception {
		return positionX5;
	}

	@Override
	public int getPositionYLink5() throws Exception {
		return positionYLink5;
	}

	@Override
	public void setPositionYLink5(int positionylink5) throws Exception {
		this.positionYLink5 = positionylink5;
	}

	@Override
	public int getPositionY5() throws Exception {
		return positionY5;
	}

	@Override
	public void setPositionY5(int positiony5) throws Exception {
		this.positionY5 = positiony5;
	}

	@Override
	public int getPositionY5_RBV() throws Exception {
		return positionY5;
	}

	@Override
	public int getSizeXLink5() throws Exception {
		return sizeXLink5;
	}

	@Override
	public void setSizeXLink5(int sizexlink5) throws Exception {
		this.sizeXLink5 = sizexlink5;
	}

	@Override
	public int getSizeX5() throws Exception {
		return sizeX5;
	}

	@Override
	public void setSizeX5(int sizex5) throws Exception {
		this.sizeX5 = sizex5;
	}

	@Override
	public int getSizeX5_RBV() throws Exception {
		return sizeX5;
	}

	@Override
	public int getSizeYLink5() throws Exception {
		return sizeYLink5;
	}

	@Override
	public void setSizeYLink5(int sizeylink5) throws Exception {
		this.sizeYLink5 = sizeylink5;
	}

	@Override
	public int getSizeY5() throws Exception {
		return sizeY5;
	}

	@Override
	public void setSizeY5(int sizey5) throws Exception {
		this.sizeY5 = sizey5;
	}

	@Override
	public int getSizeY5_RBV() throws Exception {
		return sizeY5;
	}

	@Override
	public short getShape5() throws Exception {
		return (short) shape5;
	}

	@Override
	public void setShape5(int shape5) throws Exception {
		this.shape5 = shape5;
	}

	@Override
	public short getShape5_RBV() throws Exception {
		return (short) shape5;
	}

	@Override
	public short getDrawMode5() throws Exception {
		return (short) drawMode5;
	}

	@Override
	public void setDrawMode5(int drawmode5) throws Exception {
		this.drawMode5 = drawmode5;
	}

	@Override
	public short getDrawMode5_RBV() throws Exception {
		return (short) drawMode5;
	}

	@Override
	public int getRed5() throws Exception {
		return red5;
	}

	@Override
	public void setRed5(int red5) throws Exception {
		this.red5 = red5;
	}

	@Override
	public int getRed5_RBV() throws Exception {
		return red5;
	}

	@Override
	public int getGreen5() throws Exception {
		return green5;
	}

	@Override
	public void setGreen5(int green5) throws Exception {
		this.green5 = green5;
	}

	@Override
	public int getGreen5_RBV() throws Exception {
		return green5;
	}

	@Override
	public int getBlue5() throws Exception {
		return blue5;
	}

	@Override
	public void setBlue5(int blue5) throws Exception {
		this.blue5 = blue5;
	}

	@Override
	public int getBlue5_RBV() throws Exception {
		return blue5;
	}

	@Override
	public String getName6() throws Exception {
		return name6;
	}

	@Override
	public void setName6(String name6) throws Exception {
		this.name6 = name6;
	}

	@Override
	public String getName_RBV6() throws Exception {
		return name_RBV6;
	}

	@Override
	public void setName_RBV6(String name_rbv6) throws Exception {
		this.name_RBV6 = name_rbv6;
	}

	@Override
	public short getUse6() throws Exception {
		return (short) use6;
	}

	@Override
	public void setUse6(int use6) throws Exception {
		this.use6 = use6;
	}

	@Override
	public short getUse6_RBV() throws Exception {
		return (short) use6;
	}

	@Override
	public int getPositionXLink6() throws Exception {
		return positionXLink6;
	}

	@Override
	public void setPositionXLink6(int positionxlink6) throws Exception {
		this.positionXLink6 = positionxlink6;
	}

	@Override
	public int getPositionX6() throws Exception {
		return positionX6;
	}

	@Override
	public void setPositionX6(int positionx6) throws Exception {
		this.positionX6 = positionx6;
	}

	@Override
	public int getPositionX6_RBV() throws Exception {
		return positionX6;
	}

	@Override
	public int getPositionYLink6() throws Exception {
		return positionYLink6;
	}

	@Override
	public void setPositionYLink6(int positionylink6) throws Exception {
		this.positionYLink6 = positionylink6;
	}

	@Override
	public int getPositionY6() throws Exception {
		return positionY6;
	}

	@Override
	public void setPositionY6(int positiony6) throws Exception {
		this.positionY6 = positiony6;
	}

	@Override
	public int getPositionY6_RBV() throws Exception {
		return positionY6;
	}

	@Override
	public int getSizeXLink6() throws Exception {
		return sizeXLink6;
	}

	@Override
	public void setSizeXLink6(int sizexlink6) throws Exception {
		this.sizeXLink6 = sizexlink6;
	}

	@Override
	public int getSizeX6() throws Exception {
		return sizeX6;
	}

	@Override
	public void setSizeX6(int sizex6) throws Exception {
		this.sizeX6 = sizex6;
	}

	@Override
	public int getSizeX6_RBV() throws Exception {
		return sizeX6;
	}

	@Override
	public int getSizeYLink6() throws Exception {
		return sizeYLink6;
	}

	@Override
	public void setSizeYLink6(int sizeylink6) throws Exception {
		this.sizeYLink6 = sizeylink6;
	}

	@Override
	public int getSizeY6() throws Exception {
		return sizeY6;
	}

	@Override
	public void setSizeY6(int sizey6) throws Exception {
		this.sizeY6 = sizey6;
	}

	@Override
	public int getSizeY6_RBV() throws Exception {
		return sizeY6;
	}

	@Override
	public short getShape6() throws Exception {
		return (short) shape6;
	}

	@Override
	public void setShape6(int shape6) throws Exception {
		this.shape6 = shape6;
	}

	@Override
	public short getShape6_RBV() throws Exception {
		return (short) shape6;
	}

	@Override
	public short getDrawMode6() throws Exception {
		return (short) drawMode6;
	}

	@Override
	public void setDrawMode6(int drawmode6) throws Exception {
		this.drawMode6 = drawmode6;
	}

	@Override
	public short getDrawMode6_RBV() throws Exception {
		return (short) drawMode6;
	}

	@Override
	public int getRed6() throws Exception {
		return red6;
	}

	@Override
	public void setRed6(int red6) throws Exception {
		this.red6 = red6;
	}

	@Override
	public int getRed6_RBV() throws Exception {
		return red6;
	}

	@Override
	public int getGreen6() throws Exception {
		return green6;
	}

	@Override
	public void setGreen6(int green6) throws Exception {
		this.green6 = green6;
	}

	@Override
	public int getGreen6_RBV() throws Exception {
		return green6;
	}

	@Override
	public int getBlue6() throws Exception {
		return blue6;
	}

	@Override
	public void setBlue6(int blue6) throws Exception {
		this.blue6 = blue6;
	}

	@Override
	public int getBlue6_RBV() throws Exception {
		return blue6;
	}

	@Override
	public String getName7() throws Exception {
		return name7;
	}

	@Override
	public void setName7(String name7) throws Exception {
		this.name7 = name7;
	}

	@Override
	public String getName_RBV7() throws Exception {
		return name_RBV7;
	}

	@Override
	public void setName_RBV7(String name_rbv7) throws Exception {
		this.name_RBV7 = name_rbv7;
	}

	@Override
	public short getUse7() throws Exception {
		return (short) use7;
	}

	@Override
	public void setUse7(int use7) throws Exception {
		this.use7 = use7;
	}

	@Override
	public short getUse7_RBV() throws Exception {
		return (short) use7;
	}

	@Override
	public int getPositionXLink7() throws Exception {
		return positionXLink7;
	}

	@Override
	public void setPositionXLink7(int positionxlink7) throws Exception {
		this.positionXLink7 = positionxlink7;
	}

	@Override
	public int getPositionX7() throws Exception {
		return positionX7;
	}

	@Override
	public void setPositionX7(int positionx7) throws Exception {
		this.positionX7 = positionx7;
	}

	@Override
	public int getPositionX7_RBV() throws Exception {
		return positionX7;
	}

	@Override
	public int getPositionYLink7() throws Exception {
		return positionYLink7;
	}

	@Override
	public void setPositionYLink7(int positionylink7) throws Exception {
		this.positionYLink7 = positionylink7;
	}

	@Override
	public int getPositionY7() throws Exception {
		return positionY7;
	}

	@Override
	public void setPositionY7(int positiony7) throws Exception {
		this.positionY7 = positiony7;
	}

	@Override
	public int getPositionY7_RBV() throws Exception {
		return positionY7;
	}

	@Override
	public int getSizeXLink7() throws Exception {
		return sizeXLink7;
	}

	@Override
	public void setSizeXLink7(int sizexlink7) throws Exception {
		this.sizeXLink7 = sizexlink7;
	}

	@Override
	public int getSizeX7() throws Exception {
		return sizeX7;
	}

	@Override
	public void setSizeX7(int sizex7) throws Exception {
		this.sizeX7 = sizex7;
	}

	@Override
	public int getSizeX7_RBV() throws Exception {
		return sizeX7;
	}

	@Override
	public int getSizeYLink7() throws Exception {
		return sizeYLink7;
	}

	@Override
	public void setSizeYLink7(int sizeylink7) throws Exception {
		this.sizeYLink7 = sizeylink7;
	}

	@Override
	public int getSizeY7() throws Exception {
		return sizeY7;
	}

	@Override
	public void setSizeY7(int sizey7) throws Exception {
		this.sizeY7 = sizey7;
	}

	@Override
	public int getSizeY7_RBV() throws Exception {
		return sizeY7;
	}

	@Override
	public short getShape7() throws Exception {
		return (short) shape7;
	}

	@Override
	public void setShape7(int shape7) throws Exception {
		this.shape7 = shape7;
	}

	@Override
	public short getShape7_RBV() throws Exception {
		return (short) shape7;
	}

	@Override
	public short getDrawMode7() throws Exception {
		return (short) drawMode7;
	}

	@Override
	public void setDrawMode7(int drawmode7) throws Exception {
		this.drawMode7 = drawmode7;
	}

	@Override
	public short getDrawMode7_RBV() throws Exception {
		return (short) drawMode7;
	}

	@Override
	public int getRed7() throws Exception {
		return red7;
	}

	@Override
	public void setRed7(int red7) throws Exception {
		this.red7 = red7;
	}

	@Override
	public int getRed7_RBV() throws Exception {
		return red7;
	}

	@Override
	public int getGreen7() throws Exception {
		return green7;
	}

	@Override
	public void setGreen7(int green7) throws Exception {
		this.green7 = green7;
	}

	@Override
	public int getGreen7_RBV() throws Exception {
		return green7;
	}

	@Override
	public int getBlue7() throws Exception {
		return blue7;
	}

	@Override
	public void setBlue7(int blue7) throws Exception {
		this.blue7 = blue7;
	}

	@Override
	public int getBlue7_RBV() throws Exception {
		return blue7;
	}

	@Override
	public void reset() throws Exception {
		logger.debug("DummyNDOverlay reset");
	}
}