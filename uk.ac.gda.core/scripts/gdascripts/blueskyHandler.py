###
# Copyright (c) 2018 Diamond Light Source Ltd.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
#
###

import uk.ac.diamond.daq.bluesky.api.BlueskyCommands as BlueskyCommands
import uk.ac.diamond.daq.bluesky.api.BlueskyController as BlueskyController
import uk.ac.diamond.daq.blueapi.model.RunPlan as RunPlan
import uk.ac.gda.core.GDACoreActivator as GDACoreActivator
import gda.jython.GdaBuiltinManager as GdaBuiltinManager
import java.util.concurrent.TimeUnit as TimeUnit
from java.lang import InterruptedException

from gda.jython.commands.GeneralCommands import alias

print("NOTE: Bluesky is still an experimental component and its interfaces"
      " are subject to change, use with caution!")


def run_plan(name, **kwargs):
    """
    Runs a Bluesky plan remotely
    """
    
    executor = GDACoreActivator.getService(BlueskyController).orElseThrow()
    task = RunPlan().name(name).params(kwargs)
    future = executor.runTask(task)
    try:
        return future.get()
    except (KeyboardInterrupt, InterruptedException):
        abort_plan()
        future.cancel(False)


def abort_plan(timeout=10.0):
    """
    Triggers a safe abort of the currently running bluesky plan and wait 
    for it to shut down cleanly.
    
    Arguments:
        timeout (float): The number of seconds to wait for the plan to shut
            down cleanly before raising a TimeoutError. Defaults to 10.0.
    """
    
    executor = GDACoreActivator.getService(BlueskyController).orElseThrow()
    timeout_milliseconds = int(timeout * 1000)
    future = executor.abort()
    return future.get(timeout_milliseconds, TimeUnit.MILLISECONDS)


def get_plans():
    """
    Gets plans that can be run
    """
    
    executor = GDACoreActivator.getService(BlueskyController).orElseThrow()
    return executor.getPlans()


def get_devices():
    """
    Gets devices that can be used in plans
    """
    
    executor = GDACoreActivator.getService(BlueskyController).orElseThrow()
    return executor.getDevices()

GdaBuiltinManager.registerBuiltinsFrom(BlueskyCommands)
