/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.sample.plate.management.ui.configurables.PathscanBuilderParam;
import uk.ac.diamond.daq.sample.plate.management.ui.enums.ManagingMode;
import uk.ac.diamond.daq.sample.plate.management.ui.enums.ParamType;
import uk.ac.diamond.daq.sample.plate.management.ui.enums.ValueType;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.CollectedParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.IParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.PresetParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.factory.SetParamBuilder;
import uk.ac.diamond.daq.sample.plate.management.ui.models.AbstractParam;

public class ParamComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(ParamComposite.class);

	private boolean hasAnalyser;

	private String analyser;

	private Map<PathscanBuilderParam, AbstractMultiStateInput> paramWidgetMap = new HashMap<>();

	private List<PathscanBuilderParam> configurableParameters;

	public ParamComposite(Composite parent, int style, boolean hasAnalyser, String analyser) {
		super(parent, style);
		this.setLayout(GridLayoutFactory.swtDefaults().numColumns(6).create());
		this.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true,  true).span(4,1).applyTo(this);
		this.hasAnalyser = hasAnalyser;
		this.analyser = analyser;
		configurableParameters = Finder.listLocalFindablesOfType(PathscanBuilderParam.class);
		initParamsGroup(this);
	}

	private AbstractParam processPresetParam(PathscanBuilderParam configParam, IParamBuilder paramBuilder) {
		if (configParam.getParamType() == ParamType.PRESET) {
			if (configParam.getManagingMode() == ManagingMode.USER && !paramWidgetMap.get(configParam).isOff()) {
				if (configParam.getValueType() == ValueType.NUMBER) {
					List<Double> stateValues = new ArrayList<>();
					Arrays.stream(paramWidgetMap.get(configParam).getCurrentStateValues())
						.forEach(val -> stateValues.add(Double.valueOf(val)));
					return paramBuilder.build(paramWidgetMap.get(configParam).getPVName(),
							stateValues.toArray(new Double[0]));
				} else if (configParam.getValueType() == ValueType.ENUM) {
					return paramBuilder.build(paramWidgetMap.get(configParam).getPVName(),
							paramWidgetMap.get(configParam).getCurrentStateValues());
				}
			} else if (configParam.getManagingMode() == ManagingMode.AUTO) {
				if (configParam.getValueType() == ValueType.NUMBER) {
					return paramBuilder.build(configParam.getScannableName(), new Double[] {Double.valueOf(configParam.getDefaultValue())});
				} else if (configParam.getValueType() == ValueType.ENUM) {
					return paramBuilder.build(configParam.getScannableName(), new String[] {configParam.getDefaultValue()});
				} else if (configParam.getValueType() == ValueType.NONE) {
					return paramBuilder.build(configParam.getScannableName(), new Double[] {});
				}
			}
		}
		return null;
	}

	private AbstractParam processSetParam(PathscanBuilderParam configParam, IParamBuilder paramBuilder) {
		if (configParam.getParamType() == ParamType.SET && !paramWidgetMap.get(configParam).isOff()) {
			List<Double> stateValues = new ArrayList<>();
			Arrays.stream(paramWidgetMap.get(configParam).getCurrentStateValues())
				.forEach(val -> stateValues.add(Double.valueOf(val)));
			return paramBuilder.build(paramWidgetMap.get(configParam).getPVName(),
				stateValues.toArray(new Double[0]));
		}
		return null;
	}

	private AbstractParam processCollectedParam(PathscanBuilderParam configParam, IParamBuilder paramBuilder) {
		if (configParam.getParamType() == ParamType.COLLECTED) {
			if (configParam.getManagingMode() == ManagingMode.USER && !paramWidgetMap.get(configParam).isOff()) {
				try {
					List<Double> stateValues = new ArrayList<>();
					Arrays.stream(paramWidgetMap.get(configParam).getCurrentStateValues())
						.forEach(val -> stateValues.add(Double.valueOf(val)));
					return paramBuilder.build(paramWidgetMap.get(configParam).getPVName(),
						stateValues.toArray(new Double[0]));
				} catch (NumberFormatException e) {
					return paramBuilder.build(paramWidgetMap.get(configParam).getPVName(),
						paramWidgetMap.get(configParam).getCurrentStateValues());
				}
			} else if (configParam.getManagingMode() == ManagingMode.AUTO) {
				if (configParam.getValueType() == ValueType.NUMBER) {
					return paramBuilder.build(configParam.getScannableName(), new Double[] {Double.valueOf(configParam.getDefaultValue())});
				} else if (configParam.getValueType() == ValueType.ENUM) {
					return paramBuilder.build(configParam.getScannableName(), new String[] {configParam.getDefaultValue()});
				} else if (configParam.getValueType() == ValueType.NONE) {
					return paramBuilder.build(configParam.getScannableName(), new Double[] {});
				}
			}
		}
		return null;
	}

	public List<AbstractParam> getParams(IParamBuilder paramBuilder) {
		List<AbstractParam> params = new ArrayList<>();
		for (PathscanBuilderParam configParam: configurableParameters) {
			AbstractParam param = null;
			if (paramBuilder.getClass().equals(PresetParamBuilder.class)) {
				param = processPresetParam(configParam, paramBuilder);
			} else if (paramBuilder.getClass().equals(SetParamBuilder.class)) {
				param = processSetParam(configParam, paramBuilder);
			} else if (paramBuilder.getClass().equals(CollectedParamBuilder.class)) {
				param = processCollectedParam(configParam, paramBuilder);
			}
			if (param != null) {
				params.add(param);
			}
		}
		return params;
	}

	private Group initParamsGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);

		if (hasAnalyser) {
			addLabel(group, "Configuring scan parameters for analyser: \n" + analyser, span(1));
		} else {
			addLabel(group, "Configuring scan parameters - no analyser", span(1));
		}

		Composite presetComposite = new Composite(group, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(presetComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(presetComposite);

		addLabel(presetComposite, "Preset parameters:", span(2).grab(true, false));
		for (PathscanBuilderParam configParam: configurableParameters) {
			if (configParam.getParamType() == ParamType.PRESET && configParam.getManagingMode() == ManagingMode.USER) {
				if (configParam.getValueType() == ValueType.NUMBER) {
					paramWidgetMap.put(configParam, new TextMultiStateInput(presetComposite, configParam.getLabelName(),
							configParam.getDefaultValue(),
							configParam.getScannableName(),
							configParam.getStatesMap()));
				} else if (configParam.getValueType() == ValueType.ENUM) {
					String[] stateNames;
					Optional<Scannable> optionalScannable = Finder.findOptionalOfType(configParam.getScannableName(), Scannable.class);
					if (optionalScannable.isEmpty()) {
						logger.warn("Could not get scannable '{}' for Pathscan Builder parameter", configParam.getScannableName());
						return null;
					}
					Scannable scannable = optionalScannable.get();
					if (scannable instanceof EnumPositioner) {
						try {
							EnumPositioner positioner = (EnumPositioner) scannable;
							stateNames = positioner.getPositions();
							paramWidgetMap.put(configParam, new ComboMultiStateInput(presetComposite, configParam.getLabelName(),
									Integer.valueOf(configParam.getDefaultValue()),
									configParam.getScannableName(),
									stateNames));
						} catch (DeviceException e) {
							logger.error("Error determining the current position of {}", scannable.getName(), e);
						}
					}
				}
			}
		}
		Composite setComposite = new Composite(group, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(setComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(setComposite);

		addLabel(setComposite, "Set parameters:", span(2).grab(true, false));
		for (PathscanBuilderParam configParam: configurableParameters) {
			if (configParam.getParamType() == ParamType.SET && configParam.getManagingMode() == ManagingMode.USER) {
				paramWidgetMap.put(configParam, new TextMultiStateInput(setComposite, configParam.getLabelName(),
						configParam.getDefaultValue(),
						configParam.getScannableName(),
						configParam.getStatesMap()));
			}
		}
		Composite collectedComposite = new Composite(group, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(collectedComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(collectedComposite);

		addLabel(collectedComposite, "Collected parameters:", span(2).grab(true, false));
		for (PathscanBuilderParam configParam: configurableParameters) {
			if (configParam.getParamType() == ParamType.COLLECTED && configParam.getManagingMode() == ManagingMode.USER) {
				paramWidgetMap.put(configParam, new TextMultiStateInput(collectedComposite, configParam.getLabelName(),
						configParam.getDefaultValue(),
						configParam.getScannableName(),
						configParam.getStatesMap()));
			}
		}

		return group;
	}

	private Label addLabel(Composite parent, String labelText, GridDataFactory layout) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		layout.applyTo(label);
		return label;
	}

	private GridDataFactory span(int span) {
		return GridDataFactory.swtDefaults().span(span, 1);
	}
}