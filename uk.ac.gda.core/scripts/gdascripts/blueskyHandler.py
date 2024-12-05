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
import uk.ac.diamond.osgi.services.ServiceProvider as ServiceProvider
import gda.jython.GdaBuiltinManager as GdaBuiltinManager
import java.util.concurrent.TimeUnit as TimeUnit
from java.lang import InterruptedException

from gda.jython.commands.GeneralCommands import alias
from java.lang import System
from java.util.concurrent import ExecutionException

def athena_help():
    help_message = """
The Athena programme is still experimental and its components and interfaces are 
subject to change, please use with caution! 
    """
    link = System.getProperty("GDA/athena.graylogLink") or System.getProperty("athena.graylogLink")
    if link is not None:
        help_message += """

If you have access to graylog, you can access the logs from the athena services for 
this beamline with the following link:

        """
        help_message += link
    
    help_message += """
To see this message again, type athena_help()
    """
    print(help_message)


def run_plan(name, **kwargs):
    """
    Runs a Bluesky plan remotely
    """

    executor = ServiceProvider.getService(BlueskyController)
    task = RunPlan().name(name).params(kwargs)
    
    try:
        future = executor.runTask(task)
        return future.get()
    except (KeyboardInterrupt, InterruptedException):
        abort_plan()
        future.cancel(False)
    except ExecutionException:
        athena_help()
        raise


def abort_plan(timeout=10.0):
    """
    Triggers a safe abort of the currently running bluesky plan and wait
    for it to shut down cleanly.

    Arguments:
        timeout (float): The number of seconds to wait for the plan to shut
            down cleanly before raising a TimeoutError. Defaults to 10.0.
    """

    executor = ServiceProvider.getService(BlueskyController)
    timeout_milliseconds = int(timeout * 1000)
    future = executor.abort()
    athena_help()
    return future.get(timeout_milliseconds, TimeUnit.MILLISECONDS)


def get_plans():
    """
    Gets plans that can be run
    """

    executor = ServiceProvider.getService(BlueskyController)
    return executor.getPlans()


def get_devices():
    """
    Gets devices that can be used in plans
    """

    executor = ServiceProvider.getService(BlueskyController)
    return executor.getDevices()


GdaBuiltinManager.registerBuiltinsFrom(BlueskyCommands)
athena_help()
