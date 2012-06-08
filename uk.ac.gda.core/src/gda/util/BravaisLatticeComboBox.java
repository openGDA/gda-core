/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is a JComboBox which allows selection of a space group based on bravais lattice.
 */
public class BravaisLatticeComboBox extends JComboBox implements IObservable, ItemListener {
	
	private static final Logger logger = LoggerFactory.getLogger(BravaisLatticeComboBox.class);
	
	private String unknownString = "unknown";

	private Group unknown = new Group(unknownString, unknownString);

	private Group P213 = new Group("P23/P213", "P213");

	private Group P432 = new Group("P432/P4232/P4332/P4132", "P4132");

	private Group I213 = new Group("I23/I213", "I213");

	private Group I4132 = new Group("I432/I4132", "I4132");

	private Group F23 = new Group("F23", "F23");

	private Group F4132 = new Group("F432/F4132", "F4132");

	private Group R3 = new Group("R3", "R3");

	private Group R32 = new Group("R32", "R32");

	private Group P31 = new Group("P3/P31/P32", "P31");

	private Group P3112 = new Group("P312/P3112/P3212", "P3112");

	private Group P3121 = new Group("P321/P3121/P3221", "P3121");

	private Group P61 = new Group("P6/P61/P65/P62/P64/P63", "P61");

	private Group P6122 = new Group("P622/P6122/P6522/P6222/P6422/P6322", "P6122");

	private Group P41 = new Group("P4/P41/P42/P43", "P41");

	private Group P41212 = new Group("P422/P4212/P4122/P4322/P4222/P42212/P41212/P43212", "P41212");

	private Group I41 = new Group("I4/I41", "I41");

	private Group I4122 = new Group("I422/I4122", "I4122");

	private Group P212121 = new Group("P222/P2221/P21212/P212121", "P212121");

	private Group C2221 = new Group("C2221/C222", "C2221");

	private Group I212121 = new Group("I222/I212121", "I212121");

	private Group F222 = new Group("F222", "F222");

	private Group P21 = new Group("P2/P21", "P21");

	private Group C2 = new Group("C2", "C2");

	private Group P1 = new Group("P1", "P1");

	private String selection;

	private ObservableComponent observableComponent = new ObservableComponent();

	/**
	 * BravaisLatticeComboBox
	 */
	public BravaisLatticeComboBox() {
		addItemListener(this);

		addItem(unknown);
		addItem(P213);
		addItem(P432);
		addItem(I213);
		addItem(I4132);
		addItem(F23);
		addItem(F4132);
		addItem(R3);
		addItem(R32);
		addItem(P31);
		addItem(P3112);
		addItem(P3121);
		addItem(P61);
		addItem(P6122);
		addItem(P41);
		addItem(P41212);
		addItem(I41);
		addItem(I4122);
		addItem(P212121);
		addItem(C2221);
		addItem(I212121);
		addItem(F222);
		addItem(P21);
		addItem(C2);
		addItem(P1);
	}

	/**
	 * Set Menu to show parameter spaceGroup as selected item
	 * 
	 * @param spaceGroup
	 *            String spaceGroup to be shown as selected
	 */
	public void setSpaceGroup(String spaceGroup) {
		logger.info("Setting space group to " + spaceGroup);

		for (int i = 0; i < getItemCount(); i++) {
			if (((Group) getItemAt(i)).getDefault().equals(spaceGroup)) {
				setSelectedIndex(i);
				break;
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() == ItemEvent.SELECTED) {
			Group g = (Group) ie.getItem();
			selection = g.getDefault();
			if (!selection.equals(unknownString))
				observableComponent.notifyIObservers(this, selection);
			else
				observableComponent.notifyIObservers(this, null);
		}
	}

	// IObservable

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}