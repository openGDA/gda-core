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

import uk.ac.diamond.daq.bluesky.api.BlueskyController as BlueskyController
import uk.ac.diamond.daq.bluesky.api.RunPlan as RunPlan
import uk.ac.gda.core.GDACoreActivator as GDACoreActivator

from gda.jython.commands.GeneralCommands import alias

print("NOTE: Bluesky is still an experimental component and its interfaces"
      " are subject to change, use with caution!")

def run_plan(name, **kwargs):
    """
    Runs a Bluesky plan remotely
    """
    
    executor = GDACoreActivator.getService(BlueskyController).orElseThrow()
    task = RunPlan(name, kwargs)
    result = executor.runTask(task)
    return result.get()

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
