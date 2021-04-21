/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents an am scan command.
 *
 * <p>
 *  Actually the response contains only a text string. Further content will be added case by case.
 * </p>
 */
@JsonDeserialize(builder = MscanRequest.Builder.class)
public class MscanRequest {

	private final String command;
	private final boolean block;

	private MscanRequest(String command, boolean block) {
		this.command = command;
		this.block = block;
	}

	public String getCommand() {
		return command;
	}

	public boolean isBlock() {
		return block;
	}

	@JsonPOJOBuilder
	public static class Builder {

		private String command;
		private boolean block;

		public Builder withCommand(String command) {
			this.command = command;
			return this;
		}

		public Builder withBlock(boolean block) {
			this.block = block;
			return this;
		}


		public MscanRequest build() {
			return new MscanRequest(command, block);
		}
	}
}
