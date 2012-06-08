/*-
 * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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
package gda.rcp.views;

import org.eclipse.jface.fieldassist.IContentProposal;

public class ResponseContentProposal implements IContentProposal {

	private final String content;
	private final String label;
	private final String description;
	private final int type;
	private int cursorPosition;

	public ResponseContentProposal(String content, String label, String description, int type) {
		this.content = content;
		this.label = label;
		this.description = description;
		this.type = type;

	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public int getCursorPosition() {
		return cursorPosition;
	}

	public void setCursorPosition(int cursorPosition) {
		this.cursorPosition = cursorPosition;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public int getType() {
		return type;
	}

}