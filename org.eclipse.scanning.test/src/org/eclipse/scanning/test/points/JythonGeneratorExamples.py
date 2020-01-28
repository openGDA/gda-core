from annotypes import Anno, Union, Array, Sequence

from scanpointgenerator.core import Generator, UAxes, UUnits, ASize, AAlternate

from scanpointgenerator.compat import np


with Anno("The value or values of the point to be generated, e.g. 1.0 or [2.0, 3.0]"):
    AValue = Array[float]
UValue = Union[AValue, Sequence[float], float]

@Generator.register_subclass(
    "scanpointgenerator:generator/FixedValueGenerator:1.0")
class FixedValueGenerator(Generator):
    """Generate a line of equal N-dimensional points"""

    def __init__(self, axes, units, value, size, alternate=False):
        # type: (UAxes, UUnits, ASize, UValue, AAlternate) -> None
        super(FixedValueGenerator, self).__init__(axes, units, size, alternate)
        self.value = AValue(value)
        
        # Validate
        if len(self.value) != 1 and len(self.value) != len(self.axes):
            raise ValueError(
                "Dimensions of Value does not match number of axes and is not 1!")

    def prepare_arrays(self, index_array):
        _length = len(index_array)
        arrays = {}
        if len(self.value) > 1:
            for _ in range(len(self.value)):
                arrays[self.axes[_]] = np.full((_length), self.value[_])
        else:
            for axis in self.axes:
                arrays[axis] = np.full((_length), self.value[0])
        return arrays


@Generator.register_subclass(
    "scanpointgenerator:generator/MultipliedValueGenerator:1.0")
class MultipliedValueGenerator(Generator):
    """Generate a line of equally spaced N-dimensional points"""

    def __init__(self, axes, units, value, size, alternate=False):
        # type: (UAxes, UUnits, ASize, UValue, AAlternate) -> None
        super(MultipliedValueGenerator, self).__init__(axes, units, size, alternate)
        self.value = AValue(value)
        
        # Validate
        if len(self.value) != 1 and len(self.value) != len(self.axes):
            raise ValueError(
                "Dimensions of Value does not match number of axes and is not 1!")

    def prepare_arrays(self, index_array):
        _length = len(index_array)
        arrays = {}
        if len(self.value) > 1:
            for _ in range(len(self.axes)):
                arrays[self.axes[_]] = index_array * self.value[_]
        else:
            for axis in self.axes:
                arrays[axis] = index_array * self.value[0]
        return arrays
