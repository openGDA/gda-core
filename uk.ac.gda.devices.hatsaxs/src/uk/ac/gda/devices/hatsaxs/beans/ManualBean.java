package uk.ac.gda.devices.hatsaxs.beans;

import static java.util.stream.IntStream.range;

import java.io.Serializable;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

public class ManualBean implements Serializable {
	private static final long serialVersionUID = 7737589386859176861L;
	private static final int CHANNEL_COUNT = 4;
	private String sampleName;
	private double temperature = 22; // Room temp
	private double delay = 0;
	private int channels = 0;
	private double illumination = 0;
	private boolean lightExpose = false;
	private int frames = 21;
	private double timePerFrame = 1.0;
	private String visit;

	public ManualBean() {
		ClientDetails myDetails = InterfaceProvider.getBatonStateProvider().getMyDetails();
		this.visit = myDetails.getVisitID();
	}

	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String name) {
		this.sampleName = name;
	}
	public double getTemperature() {
		return temperature;
	}
	public void setTemperature(double temperature) {
		if (temperature < 4 || temperature > 60) {
			throw new IllegalArgumentException("Temperature must be between 4 and 60");
		}
		this.temperature = temperature;
	}
	public double getDelay() {
		return delay;
	}
	public void setDelay(double delay) {
		this.delay = delay;
	}

	public boolean channelActive(int ch) {
		checkChannel(ch);
		return (getChannels() & (1 << ch)) > 0;
	}

	public int[] getActiveChannels() {
		return range(0, CHANNEL_COUNT).filter(this::channelActive).toArray();
	}
	public void setChannelActive(int ch, boolean active) {
		// Always set bit to 1 so that switching bit again sets to 0 if needed
		setChannels(getChannels() | (1 << ch)); // set bit to 1
		if (!active) {
			setChannels(getChannels() ^ (1 << ch)); // if not active set to 0
		}
	}
	public int getChannels() {
		return channels;
	}
	public void setChannels(int channels) {
		this.channels = channels;
	}
	public double getIllumination() {
		return illumination;
	}
	public void setIllumination(double illumination) {
		this.illumination = illumination;
	}
	public boolean isLightExpose() {
		return lightExpose;
	}
	public void setLightExpose(boolean lightExpose) {
		this.lightExpose = lightExpose;
	}
	public int getFrames() {
		return frames;
	}
	public void setFrames(int frames) {
		this.frames = frames;
	}
	public double getTimePerFrame() {
		return timePerFrame;
	}
	public void setTimePerFrame(double timePerFrame) {
		this.timePerFrame = timePerFrame;
	}
	public String getVisit() {
		return visit;
	}
	public void setVisit(String visit) {
		this.visit = visit;
	}

	private void checkChannel(int ch) {
		if (ch < 0 || ch >= CHANNEL_COUNT) {
			throw new IllegalArgumentException("Channel must be 0-" + CHANNEL_COUNT + "(" + ch + ")");
		}
	}
}
