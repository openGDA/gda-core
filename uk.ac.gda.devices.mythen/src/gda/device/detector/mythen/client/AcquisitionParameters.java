/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen.client;

import static gda.device.detector.mythen.client.BigDecimalUtils.isLess;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import org.springframework.util.StringUtils;

public class AcquisitionParameters {
	
	public static class Builder {
		
		private Trigger trigger;
		private Integer frames;
		private BigDecimal delayBeforeFrames = BigDecimal.ZERO;
		private BigDecimal exposureTime=null;
		private BigDecimal delayAfterFrames = BigDecimal.ZERO;
		private String filename;
		private Integer startIndex;
		private boolean gating = false;
		private int gates = 0;
		
		public Builder trigger(Trigger trigger) {
			this.trigger = trigger;
			return this;
		}
		
		public Builder frames(int frames) {
			this.frames = frames;
			return this;
		}
		
		public Builder delayBeforeFrames(BigDecimal delayBeforeFrames) {
			this.delayBeforeFrames = delayBeforeFrames;
			return this;
		}
		
		public Builder exposureTime(BigDecimal exposureTime) {
			this.exposureTime = exposureTime;
			return this;
		}
		
		public Builder delayAfterFrames(BigDecimal delayAfterFrames) {
			this.delayAfterFrames = delayAfterFrames;
			return this;
		}
		
		public Builder filename(String filename) {
			this.filename = filename;
			return this;
		}
		
		public Builder startIndex(int startIndex) {
			this.startIndex = startIndex;
			return this;
		}
		
		public Builder gating(boolean gating) {
			this.gating = gating;
			return this;
		}
		
		public Builder gates(int gates) {
			this.gates = gates;
			return this;
		}
		
		public AcquisitionParameters build() {
			
			AcquisitionParameters params = new AcquisitionParameters(this);
			
			if (frames == null || frames <= 0) {
				throw new IllegalStateException("Number of frames must be > 0");
			}
			
			if (delayBeforeFrames == null || isLess(delayBeforeFrames, ZERO)) {
				throw new IllegalStateException("Delay before frames must be ≥ 0");
			}
			
			if (exposureTime == null || isLess(exposureTime, ZERO)) {
				throw new IllegalStateException("Exposure time must be ≥ 0");
			}
			
			if (delayAfterFrames == null || isLess(delayAfterFrames, ZERO)) {
				throw new IllegalStateException("Delay after frames must be ≥ 0");
			}
			
			if (!StringUtils.hasText(filename)) {
				throw new IllegalStateException("Filename must be specified");
			}
			
			return params;
		}
	}
	
	private AcquisitionParameters(Builder builder) {
		this.trigger = builder.trigger;
		this.frames = builder.frames;
		this.delayBeforeFrames = builder.delayBeforeFrames;
		this.exposureTime = builder.exposureTime;
		this.delayAfterFrames = builder.delayAfterFrames;
		this.filename = builder.filename;
		this.startIndex = builder.startIndex;
		this.gating = builder.gating;
		this.gates = builder.gates;
	}
	
	private final Trigger trigger;
	private final Integer frames;
	private final BigDecimal delayBeforeFrames;
	private final BigDecimal exposureTime;
	private final BigDecimal delayAfterFrames;
	private final String filename;
	private final Integer startIndex;
	private final boolean gating;
	private final int gates;
	
	public Trigger getTrigger() {
		return trigger;
	}
	
	public int getFrames() {
		return frames;
	}
	
	public BigDecimal getDelayBeforeFrames() {
		return delayBeforeFrames;
	}
	
	public BigDecimal getExposureTime() {
		return exposureTime;
	}
	
	public BigDecimal getDelayAfterFrames() {
		return delayAfterFrames;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public Integer getStartIndex() {
		return startIndex;
	}
	
	public boolean getGating() {
		return gating;
	}
	
	public int getGates() {
		return gates;
	}
	
	@Override
	public String toString() {
		return String.format("AcquisitionParameters(trigger=%s, frames=%d, exposureTime=%s, delayAfterFrames=%s, filename=%s, startIndex=%d, gating=%s, gates=%d)",
			trigger,
			frames,
			exposureTime.toString(),
			delayAfterFrames.toString(),
			filename,
			startIndex,
			gating,
			gates);
	}
	
}
