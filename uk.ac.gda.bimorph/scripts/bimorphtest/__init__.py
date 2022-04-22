
class Float(float):
    """Helper class for comparing calls with float arguments"""
    def __new__(self, value, tol=1e-8):
        return float.__new__(self, value)
    def __init__(self, value, tol=1e-8):
        float.__init__(self, value)
        self.value = value
        self.tol = tol
    def __eq__(self, other):
        if other is None: return False
        if not isinstance(other, float): return False
        return abs(other - self.value) < self.tol

def roughly(arg, tol=1e-8):
    """Create float(ish) objects for comparisons in tests"""
    try:
        return [roughly(i, tol) for i in arg]
    except TypeError:
        return Float(arg, tol=tol)
