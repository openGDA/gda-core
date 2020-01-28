###
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
###
from org.eclipse.scanning.api.points import Point
from org.eclipse.scanning.api.points import Scalar
from org.eclipse.scanning.api.points import MapPosition
from org.eclipse.scanning.api.points import StaticPosition
from org.eclipse.scanning.api.points import PPointGenerator
from org.eclipse.scanning.api.points import ScanPointIterator

from org.eclipse.scanning.points import PySerializable

from scanpointgenerator import LineGenerator
from scanpointgenerator import ArrayGenerator
from scanpointgenerator import SpiralGenerator
from scanpointgenerator import LissajousGenerator
from scanpointgenerator import StaticPointGenerator
from scanpointgenerator import ZipGenerator
from scanpointgenerator import ConcatGenerator
from scanpointgenerator import CompoundGenerator
from scanpointgenerator import RandomOffsetMutator
from scanpointgenerator import CircularROI
from scanpointgenerator import EllipticalROI
from scanpointgenerator import PointROI
from scanpointgenerator import PolygonalROI
from scanpointgenerator import RectangularROI
from scanpointgenerator import SectorROI
from scanpointgenerator import Excluder
from scanpointgenerator import ROIExcluder

## Logging
import logging
logger = logging.getLogger(__name__)


class JavaIteratorWrapper(ScanPointIterator):
    """
    A wrapper class to give a python iterator the while(hasNext()) next()
    operation required of Java Iterators
    """

    def __init__(self, iterator, generator):
        self.generator = generator
        self.logger = logger.getChild(self.__class__.__name__)
        self._iterator = iterator
        self._has_next = None
        self._next = None
        self._index = 0

    def next(self):

        if self._has_next:
            result = self._next
        else:
            result = next(self._iterator)
        result.setStepIndex(self._index)
        self._has_next = None
        self._index += 1
        return result

    def hasNext(self):

        if self._has_next is None:

            try:
                self._next = next(self._iterator)
            except StopIteration:
                self._has_next = False
            else:
                self._has_next = True

        return self._has_next

    def getSize(self):
        return self.generator.size
    def getShape(self):
        return self.generator.shape
    def getRank(self):
        return len(self.generator.shape)
    def getIndex(self):
        return self._index
    

class GeneratorWrapper(PPointGenerator, PySerializable):
    """
    Wrapper class for Java Generator
    """
    
    def __init__(self, generator):
        self.generator = generator
    
    def __iter__(self):
        return self.generator.iterator()
    
    def __getitem__(self, n):
        return self.generator.get_point(n)
    
    def __len__(self):
        return self.generator.size
    
    def getPointIterator(self):
        return JavaIteratorWrapper(self.__iter__(), self.generator)
    
    def getSize(self):
        return self.generator.size
    def getShape(self):
        return self.generator.shape
    def getRank(self):
        return len(self.generator.shape)
    def getNames(self):
        return [name for dimension in self.generator.dimensions for name in dimension.axes]
    def getInitialBounds(self):
        return MapPosition(self.__getitem__(0).lower)
    def getFinalBounds(self):
        return MapPosition(self.__getitem__(len(self)-1).upper)
    
    def toDict(self):
        return self.generator.to_dict()


class StaticPositionGeneratorWrapper(GeneratorWrapper):
    """
    Wraps points into StaticPosition objects
    """
    
    def __iter__(self):
        for point in self.generator.iterator():
            yield StaticPosition()

class ScalarPointGeneratorWrapper(GeneratorWrapper):
    """
    Wraps points into Scalar objects
    """
    
    def __iter__(self):
        for point in self.generator.iterator():
            index = point.indexes[0]
            position = point.positions[self.name]
            yield Scalar(self.name, index, position)

class TwoDPointGeneratorWrapper(GeneratorWrapper):
    """
    Wraps points into 2-dimensional Java Point objects
    """
    
    def __iter__(self):
        for point in self.generator.iterator():
            index = point.indexes[0]
            x_name, y_name = self.names
            x_position, y_position = point.positions[x_name], point.positions[y_name]
            yield Point(x_name, index, x_position, y_name, index, y_position, False)

class MapPositionWrapper(GeneratorWrapper):
    """
    Wraps points into n-dimensional MapPosition objects
    """
    
    def __iter__(self):
        names = [d.axes for d in self.generator.dimensions]
        axes_ordered = sum(names, [])
        index_locations = {axis:[axis in name for name in names].index(True) for axis in axes_ordered}
        for point in self.generator.iterator():
            map_point = MapPosition()
            for axis in axes_ordered:
                index = index_locations[axis]
                value = point.positions[axis]
                map_point.put(axis, value)
                map_point.putIndex(axis, point.indexes[index])
            map_point.setDimensionNames(names)
            yield map_point
    
  
class JLineGenerator1D(ScalarPointGeneratorWrapper):
    """
    Create a 1D LineGenerator and wrap the points into Java Scalar objects
    """

    def __init__(self, name, units, start, stop, num_points, alternate_direction=False):

        self.name = name
        line_gen = LineGenerator(name, units, start, stop, num_points, alternate_direction)
        self.generator = CompoundGenerator([line_gen], [], [])
        self.generator.prepare()
        super(ScalarPointGeneratorWrapper, self).__init__(self.generator)

class JLineGenerator2D(TwoDPointGeneratorWrapper):
    """
    Create a 2D LineGenerator and wrap the points into java Point objects
    """
    
    def __init__(self, names, units, start, stop, num_points, alternate_direction=False):

        start = start.tolist()  # Convert from array to list
        stop = stop.tolist()
        line_gen = LineGenerator(names, units, start, stop, num_points, alternate_direction)
        self.generator = CompoundGenerator([line_gen], [], [])
        self.generator.prepare()
        super(TwoDPointGeneratorWrapper, self).__init__(self.generator)

class JArrayGenerator(ScalarPointGeneratorWrapper):
    """
    Create an ArrayGenerator and wrap the points into java Scalar objects
    """
    
    def __init__(self, name, units, points, alternate_direction=False):

        self.name = name
        array_gen = ArrayGenerator(name, units, points, alternate_direction)
        self.generator = CompoundGenerator([array_gen], [], [])
        self.generator.prepare()
        super(ScalarPointGeneratorWrapper, self).__init__(self.generator)

class JSpiralGenerator(TwoDPointGeneratorWrapper):
    """
    Create a SpiralGenerator and wrap the points into java Point objects
    """

    def __init__(self, names, units, centre, radius, scale=1.0, alternate_direction=False):

        spiral_gen = SpiralGenerator(names, units, centre, radius, scale, alternate_direction)
        self.generator = CompoundGenerator([spiral_gen], [], [])
        self.generator.prepare()
        super(TwoDPointGeneratorWrapper, self).__init__(self.generator)

class JLissajousGenerator(TwoDPointGeneratorWrapper):
    """
    Create a LissajousGenerator and wrap the points into java Point objects
    """

    def __init__(self, names, units, box, num_lobes, num_points, alternate_direction=False):
        
        liss_gen = LissajousGenerator(names, units, box["centre"],
                [box["width"], box["height"]], num_lobes, num_points, alternate_direction)
        self.generator = CompoundGenerator([liss_gen], [], [])
        self.generator.prepare()
        super(TwoDPointGeneratorWrapper, self).__init__(self.generator)
    
class JStaticPointGenerator(StaticPositionGeneratorWrapper):
    """
    Wrap a StaticPointGenerator and produce StaticPosition objects
    """
    
    def __init__(self, size, axes=[]):
        static_gen = StaticPointGenerator(size, axes)
        self.generator = CompoundGenerator([static_gen], [], [])
        self.generator.prepare()
        super(StaticPositionGeneratorWrapper, self).__init__(self.generator)
        
class JZipGenerator(MapPositionWrapper):
    """
    Wrap a ZipGenerator and produce MapPosition objects
    """
    
    def __init__(self, iterators, alternating=False):
        generators = [generator for wrapper in iterators for generator in wrapper.generator.generators]
        zip_gen = ZipGenerator(generators, alternating)
        self.generator = CompoundGenerator([zip_gen], [], [], -1, False)
        self.generator.prepare()
        super(MapPositionWrapper, self).__init__(self.generator)
        
class JConcatGenerator(MapPositionWrapper):
    """
    Wrap a ConcatGenerator and produce MapPosition objects
    """
    
    def __init__(self, iterators, alternating=False):
        generators = [generator for wrapper in iterators for generator in wrapper.generator.generators]
        concat_gen = ConcatGenerator(generators, alternating)
        self.generator = CompoundGenerator([concat_gen], [], [], -1, False)
        self.generator.prepare()
        super(MapPositionWrapper, self).__init__(self.generator)

class JCompoundGenerator(MapPositionWrapper):
    """
    Create a CompoundGenerator and wrap the points into java Point objects
    """

    def __init__(self, iterators, excluders, mutators, duration=-1, continuous=True, delay_after=0):
        # All the generators in the CompoundGenerator in a GeneratorWrapper
        generators = [generator for wrapper in iterators for generator in wrapper.generator.generators]
        
        excluders = [excluder.py_excluder for excluder in excluders]
        mutators = [mutator.py_mutator for mutator in mutators]
        # Functionally aping a set
        e_dict = [e.to_dict() for e in excluders]
        m_dict = [m.to_dict() for m in mutators]
        
        excluders.extend([excluder for wrapper in iterators for excluder in wrapper.generator.excluders if excluder.to_dict() not in e_dict])
        mutators.extend([mutator for wrapper in iterators for mutator in wrapper.generator.mutators if mutator.to_dict() not in m_dict])

        self.generator = CompoundGenerator(generators, excluders, mutators, duration, continuous, delay_after)
        self.generator.prepare()
        super(MapPositionWrapper, self).__init__(self.generator)


class JRandomOffsetMutator(object):

    def __init__(self, seed, axes, max_offset):
        self.py_mutator = RandomOffsetMutator(seed, axes, max_offset)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JRandomOffsetMutator: %s', self.py_mutator.to_dict())


class JExcluder(object):

    def __init__(self, rois, scannables):
        py_rois = [roi.py_roi for roi in rois]
        self.py_excluder = ROIExcluder(py_rois, scannables)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JExcluder: %s', self.py_excluder.to_dict())


class JCircularROI(object):

    def __init__(self, centre, radius):
        self.py_roi = CircularROI(centre, radius)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JCircularROI: %s', self.py_roi.to_dict())

class JEllipticalROI(object):

    def __init__(self, centre, semiaxes, angle=0):
        self.py_roi = EllipticalROI(centre, semiaxes, angle)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JEllipticalROI: %s', self.py_roi.to_dict())

class JPointROI(object):

    def __init__(self, point):
        self.py_roi = PointROI(point)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JPointROI: %s', self.py_roi.to_dict())

class JPolygonalROI(object):

    def __init__(self, points_x, points_y):
        self.py_roi = PolygonalROI(points_x, points_y)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JPolygonalROI: %s', self.py_roi.to_dict())

class JRectangularROI(object):

    def __init__(self, start, width, height, angle=0):
        self.py_roi = RectangularROI(start, width, height, angle)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JRectangularROI: %s', self.py_roi.to_dict())

class JSectorROI(object):

    def __init__(self, centre, radii, angles):
        self.py_roi = SectorROI(centre, radii, angles)
        self.logger = logger.getChild(self.__class__.__name__)
        self.logger.debug('Created JSectorROI: %s', self.py_roi.to_dict())
