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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDPluginBase;

public class FfmpegStreamSimulator implements FfmpegStream {

	NDPluginBase nDPluginBase;
	private double quality;
	private int false_col;
	private int always_on;
	private double http_port;
	private String host;
	private int clients;
	private String jpg_url="";
	private String mjpg_url="";
	private int maxw;
	private int maxh;
	@Override
	public NDPluginBase getPluginBase() {
		return nDPluginBase;
	}
	

	@Override
	public double getQUALITY() throws Exception {
		return quality;
	}

	@Override
	public void setQUALITY(double quality) throws Exception {
		this.quality = quality;
	}

	@Override
	public double getQUALITY_RBV() throws Exception {
		return quality;
	}

	@Override
	public short getFALSE_COL() throws Exception {
		return (short) false_col;
	}

	@Override
	public void setFALSE_COL(int false_col) throws Exception {
		this.false_col = false_col;
	}

	@Override
	public short getFALSE_COL_RBV() throws Exception {
		return (short) false_col;
	}

	@Override
	public short getALWAYS_ON() throws Exception {
		return (short) always_on;
	}

	@Override
	public void setALWAYS_ON(int always_on) throws Exception {
		this.always_on = always_on;

	}

	@Override
	public short getALWAYS_ON_RBV() throws Exception {
		return (short) always_on;
	}

	@Override
	public double getHTTP_PORT_RBV() throws Exception {
		return http_port;
	}

	@Override
	public String getHOST_RBV() throws Exception {
		return host;
	}

	@Override
	public int getCLIENTS_RBV() throws Exception {
		return clients;
	}

	@Override
	public String getJPG_URL_RBV() throws Exception {
		return jpg_url;
	}

	@Override
	public String getMJPG_URL_RBV() throws Exception {
		return mjpg_url;
	}

	@Override
	public void reset() throws Exception {
	}


	public void setPluginBase(NDPluginBase pluginBase) {
		this.nDPluginBase = pluginBase;
	}


	public void setQuality(double quality) {
		this.quality = quality;
	}


	public void setFalse_col(int false_col) {
		this.false_col = false_col;
	}


	public void setAlways_on(int always_on) {
		this.always_on = always_on;
	}


	public void setHttp_port(double http_port) {
		this.http_port = http_port;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public void setClients(int clients) {
		this.clients = clients;
	}


	public void setJpg_url(String jpg_url) {
		this.jpg_url = jpg_url;
	}


	public void setMjpg_url(String mjpg_url) {
		this.mjpg_url = mjpg_url;
	}


	@Override
	public void setMAXW(int maxw) throws Exception {
		this.maxw = maxw;
	}


	@Override
	public void setMAXH(int maxh) throws Exception {
		this.maxh = maxh;
		
	}


	@Override
	public int getMAXW_RBV() throws Exception {
		return maxw;
	}


	@Override
	public int getMAXH_RBV() throws Exception {
		return maxh;
	}

}
