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

from scanpointgenerator import LineGenerator
from scanpointgenerator import ArrayGenerator
from scanpointgenerator import SpiralGenerator
from scanpointgenerator import LissajousGenerator
from scanpointgenerator import StaticPointGenerator
from scanpointgenerator import ZipGenerator
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

    def toDict(self):
        return self.generator.to_dict()

    def getSize(self):
        return self.generator.size
    def getShape(self):
        return self.generator.shape
    def getRank(self):
        return len(self.generator.shape)
    def getIndex(self):
        return self._index
        

class GeneratorWrapper(PPointGenerator):
    """
    Wrapper class for Java Generator
    """
    
    def __init__(self, generator):
        self._index = -1
        self.generator = generator
    
    def __iter__(self):
        return self.generator.iterator()
    
    def __getitem__(self, n):
        self._index = n
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
        self._index = -1
        names = [d.axes for d in self.generator.dimensions]
        axes_ordered = sum(names, [])
        index_locations = {axis:[axis in name for name in names].index(True) for axis in axes_ordered}
        for point in self.generator.iterator():
            self._index += 1
            map_point = MapPosition()
            for axis in axes_ordered:
                index = index_locations[axis]
                value = point.positions[axis]
                map_point.put(axis, value)
                map_point.putIndex(axis, point.indexes[index])
                map_point.setStepIndex(self._index)
            map_point.setDimensionNames(names)
            yield map_point
    
  
class JLineGenerator1D(ScalarPointGeneratorWrapper):
    """
    Create a 1D LineGenerator and wrap the points into Java Scalar objects
    """

    def __init__(self, name, units, start, stop, num_points, alternate_direction=False, continuous=True):

        self.name = name
        line_gen = LineGenerator(name, units, start, stop, num_points, alternate_direction)
        self.generator = CompoundGenerator([line_gen], [], [], -1, continuous)
        self.generator.prepare()
        super(ScalarPointGeneratorWrapper, self).__init__(self.generator)

class JLineGenerator2D(TwoDPointGeneratorWrapper):
    """
    Create a 2D LineGenerator and wrap the points into java Point objects
    """
    
    def __init__(self, names, units, start, stop, num_points, alternate_direction=False, continuous=True):

        self.names = names
        start = start.tolist()  # Convert from array to list
        stop = stop.tolist()
        line_gen = LineGenerator(names, units, start, stop, num_points, alternate_direction)
        self.generator = CompoundGenerator([line_gen], [], [], -1, continuous)
        self.generator.prepare()
        super(TwoDPointGeneratorWrapper, self).__init__(self.generator)

class JArrayGenerator(ScalarPointGeneratorWrapper):
    """
    Create an ArrayGenerator and wrap the points into java Scalar objects
    """
    
    def __init__(self, name, units, points, alternate_direction=False, continuous=True):

        self.name = name
        array_gen = ArrayGenerator(name, units, points, alternate_direction)
        self.generator = CompoundGenerator([array_gen], [], [], -1, continuous)
        self.generator.prepare()
        super(ScalarPointGeneratorWrapper, self).__init__(self.generator)

class JSpiralGenerator(TwoDPointGeneratorWrapper):
    """
    Create a SpiralGenerator and wrap the points into java Point objects
    """

    def __init__(self, names, units, centre, radius, scale=1.0, alternate_direction=False, continuous=True):

        self.names = names
        spiral_gen = SpiralGenerator(names, units, centre, radius, scale, alternate_direction)
        self.generator = CompoundGenerator([spiral_gen], [], [], -1, continuous)
        self.generator.prepare()
        super(TwoDPointGeneratorWrapper, self).__init__(self.generator)

class JLissajousGenerator(TwoDPointGeneratorWrapper):
    """
    Create a LissajousGenerator and wrap the points into java Point objects
    """

    def __init__(self, names, units, box, num_lobes, num_points, alternate_direction=False, continuous=True):
        
        self.names = names
        liss_gen = LissajousGenerator(names, units, box["centre"],
                [box["width"], box["height"]], num_lobes, num_points, alternate_direction)
        self.generator = CompoundGenerator([liss_gen], [], [], -1, continuous)
        self.generator.prepare()
        super(TwoDPointGeneratorWrapper, self).__init__(self.generator)

class JZipGenerator(MapPositionWrapper):
    """
    Wrap a ZipGenerator and produce MapPosition objects
    """
    
    def __init__(self, iterators, alternate_direction=False, continuous=False):
        super(JZipGenerator, self).__init__()
        zip_gen = ZipGenerator(iterators, alternate)
        self.generator = CompoundGenerator([zip_gen], [], [], -1, continuous)
        self.generator.prepare()
        self.names = [d.axes for d in self.generator.dimensions]
        super(MapPositionWrapper, self).__init__(self.generator)
    
class JStaticPointGenerator(StaticPositionGeneratorWrapper):
    """
    Wrap a StaticPointGenerator and produce StaticPosition objects
    """
    
    def __init__(self, size, axes=[]):
        g = StaticPointGenerator(size, axes)
        self.generator = CompoundGenerator([g], [], [])
        self.generator.prepare()
        super(StaticPositionGeneratorWrapper, self).__init__(self.generator)

class JCompoundGenerator(MapPositionWrapper):
    """
    Create a CompoundGenerator and wrap the points into java Point objects
    """

    def __init__(self, iterators, excluders, mutators, duration=-1, continuous=True, delay_after=0):
        try:  # If JavaIteratorWrapper
            generators = [generator for iterator in iterators for generator in iterator.generator.generators]
        except AttributeError:  # Else call get*() of Java iterator
            generators = [iterator.generator for iterator in iterators]
        except AttributeError:  # Else call get*() of Java iterator
            generators = [iterator.getPointIterator().generator for iterator in iterators]

        excluders = [excluder.py_excluder for excluder in excluders]
        mutators = [mutator.py_mutator for mutator in mutators]
        # used to detect duplicated excluders/mutators
        mutator_dicts = [m.to_dict() for m in mutators]
        excluder_dicts = [e.to_dict() for e in excluders]
        extracted_generators = []

        for generator in generators:
            if generator.__class__.__name__ == "CompoundGenerator":
                extracted_generators.extend(generator.generators)
                # extract mutators/excluders we haven't already seen
                # it's possible a mutator/excluder was attached to both us and
                # a compound generator we were given
                extracted_mutators = [m for m in generator.mutators if m.to_dict() not in mutator_dicts]
                mutators.extend(extracted_mutators)
                mutator_dicts.extend([m.to_dict() for m in extracted_mutators])
                extracted_excluders = [e for e in generator.excluders if e.to_dict() not in excluder_dicts]
                excluders.extend(extracted_excluders)
                excluder_dicts.extend([e.to_dict() for e in extracted_excluders])
            else:
                extracted_generators.append(generator)
        generators = extracted_generators

        self.generator = CompoundGenerator(generators, excluders, mutators, duration, continuous, delay_after)
        self.generator.prepare()
        super(MapPositionWrapper, self).__init__(self.generator)


class JSimplePointGen(MapPositionWrapper):
    """
    Create a CompoundGenerator without regions etc., just to apply continuousness and generate points.
    """

    def __init__(self, generators, continuous=True):
        try:  # If JavaIteratorWrapper
            generators = [g for t in iterators for g in t.generator.generators]
        except AttributeError:  # Else call get*() of Java iterator
            generators = [iterator.getPyIterator().generator for iterator in iterators]
        logger.debug("Generators passed to JSimplePointGen: %s", [g.to_dict() for g in generators])

        extracted_generators = []

        for generator in generators:
            if generator.__class__.__name__ == "CompoundGenerator":
                extracted_generators.extend(generator.generators)
            else:
                extracted_generators.append(generator)
        generators = extracted_generators

        self.generator = CompoundGenerator(generators, [], [], -1, continuous)
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
