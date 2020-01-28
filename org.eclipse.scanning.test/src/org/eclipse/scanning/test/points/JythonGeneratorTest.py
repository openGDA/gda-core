# ##
# Copyright (c) 2016 Diamond Light Source Ltd.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Gary Yendell - initial API and implementation and/or initial documentation
#    Charles Mita - initial API and implementation and/or initial documentation
#
# ##
from java.util import Iterator

from scanpointgenerator import CompoundGenerator

from jython_spg_interface import MapPositionWrapper

from jython_spg_interface import GeneratorWrapper

from JythonGeneratorExamples import FixedValueGenerator
from JythonGeneratorExamples import MultipliedValueGenerator

from org.eclipse.scanning.points import PPointGenerator

# # Logging
import logging


# logging.basicConfig(level=logging.DEBUG)


# < -- Example/TestGenerators
class FixedValueWrapper(MapPositionWrapper):
    """
    Create a fixed series of points
    """
    def __init__(self, axes, units, value, size, alternate=False, continuous=False):
        fixedval = FixedValueGenerator(axes, units, value, size, alternate)
        self.generator = CompoundGenerator([fixedval], [], [], -1, continuous)
        self.generator.prepare()
        super(FixedValueWrapper, self).__init__(self.generator)

class MultipliedValueWrapper(MapPositionWrapper):
    """
    Create a series of points linear in an axis relative to its index
    """
    def __init__(self, axes, units, value, size, alternate=False, continuous=False):
        multival = MultipliedValueGenerator(axes, units, value, size, alternate)
        self.generator = CompoundGenerator([multival], [], [], -1, continuous)
        self.generator.prepare()
        super(MultipliedValueWrapper, self).__init__(self.generator)

# /ExampleGenerators -->
class ExceptionGenerator(GeneratorWrapper):
    """
    Create a fixed series of points
    """

    def __init__(self, scannableName, shape):
        super(ExceptionGenerator, self).__init__(scannableName, shape)

    def __iter__(self):
        raise Exception("Cannot iterate ExceptionGenerator!")
