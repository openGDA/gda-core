"""Jython commands for detectors specific to this beamline.

The commands defined here are intended for use as part of the mscan() syntax.
For instance:
>>> mscan(step(my_scannable, 0, 10, 1), det=mandelbrot(0.1))
"""

from mapping_scan_commands import _fetch_model_for_detector


MANDELBROT_DETECTOR = 'mandelbrot'


def mandelbrot(exposure_time=None):
    """Obtain and possibly update a Mandelbrot model, to be passed to mscan().
    """
    model = _fetch_model_for_detector(MANDELBROT_DETECTOR)

    if exposure_time is not None:
        model.setExposureTime(exposure_time)
    else:
        pass  # Keep the value already set.

    return MANDELBROT_DETECTOR, model
