#!/usr/bin/env python
# -*- coding: utf-8 -*-

""" This script is only run when exporting a Development GDA Server product from the uk.ac.diamond.server.site using Buckminster
Do not attempt to use it for other purposes as the destination of the links it creates will likely not be present.

The script finds all the bundles that contain python script locations defined using the uk.ac.diamond.daq.jython.api.scriptLocations
extension point (i.e. those that should be available to all configurations). These folders are copied to their associated bundle
folders by the basic export process, however for a development export they must be replaced by links back to the corresponding
folders under workspace_git. In order to  do this, the script uses the exported plugins folder supplied by the calling Ant task
plus the script location value(s) parsed from the identified file to create a relative path to the copied folder that includes the
generated bundle version for the exported product. This folder is then deleted and replaced by a symlink that points back to the
source folder in workspace_git. The utilities folder is processed in the same way to handle script sources that are not plugins but
nevertheless need to be available (e.g. diffcalc)

Arguments:
1                The path to the exported plugins directory (usually supplied by Ant)

The following folder structure is assumed for the plugin scripts:

     ./gda_versions/gda-9.6/ (some workspace parent)
        ├──servers (servers root)
        │   └──server_20180117-1709_linux64  (the server being exported)
        │       └──plugins (plugins folder (passed in))
        │           └──uk.ac.gda.core_9.7.0.v20180117-1156 (exported versioned plugin AA root)
        └──workspace_git (some repo parent)
            ├──daq-platform.git (this plugin's repo)
            │   └──uk.ac.diamond.daq.server.site (this plugin)
            └──gda-core.git (plugin AA's repo)
                └──uk.ac.gda.core (plugin AA source root)
                    └──??? (0..n intermediate folders)
                        └──scripts (a scripts folder)


when complete there will be a scripts folder link(s) at the same relative path below < exported versioned plugin AA root > that point
back to each corresponding < a scripts folder > beneath < plugin AA source root > for each plugin that uses the extension point."""

import subprocess
import os
import sys
import glob
import shutil
import xml.etree.ElementTree

up_one = ".." + os.sep

""" Removes the script folder at root/exported_path and replaces it with a link to
<git_repo_parent>/path where <git_repo_parent> is at the same level as the servers
folder containing the exported server build. The number of levels between this and
the script folder is specified by depth whilst root specifies whether it is below.
the plugins or utilities folder."""

def write_link(root, path, exported_path, depth):
    target_match = up_one * 2 + path                                            # make the matcher for the basic workspace script folder path relative to here
    target_test = glob.glob(target_match)                                       # resolve any wildcards, e.g. to include the git repo name for plugins
    if len(target_test) == 0:
        print "WARNING: could not find {0} in workspace, this script folder will be in the exported product".format(path)  # handle plugins not in workspace_git e.g. TP ones
        return

    target_test = target_test[0]                                                # get the matching entry
    if os.path.exists(target_test) and os.path.isdir(target_test):              # so its existence can be checked
        path_offset = up_one * (depth + 2)                                      # calculate the offset from the exported script folder's parent to the git parent
        ws_git_name = os.path.basename(os.path.realpath(up_one * 2)) + os.sep   # read the name of the git repositories parent
        link_target = path_offset + ws_git_name + target_test[6:]               # build the equivalent relative path from the exported plugin folder
        lfrom = os.path.join(root, exported_path)                               # build the full path of the exported script folder/link

        shutil.rmtree(lfrom, True)                                              # remove the exported script folder and its subfolders
        if os.path.islink(lfrom):                                               # in case it wasn't there get rid of any links that might be instead:
             os.remove(lfrom)
        os.symlink(link_target, lfrom)                                          # make the replacement symlink
        print "\nLink written from: {0}\n             to:   {1}".format(lfrom, link_target)
    else:
        print "WARNING: could not find {0} in workspace, this script folder will be in the exported product".format(target_test[6:])     # handle bad path resolution

if len(sys.argv) != 2:                                                          # script name plus plugins path
    print "ERROR: plugins.dir argument must be supplied"
    sys.exit(1)

plugins_folder = sys.argv[1]
roots = [plugins_folder, plugins_folder + os.sep + up_one + "utilities"]

# Grep for the plugin.xml files of all the bundles that define script locations via the corresponding extension point
process = subprocess.Popen('grep -Rl --include="plugin.xml" "jython.api.scriptLocations" ' + 2 * up_one, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
stdout, stderr = process.communicate()
plugins_file_list = stdout.split('\n')

""" Iterate over the plugin list building the required links to link back
from the exported product's plugin/utilities folder to the relevant script folder,
then delete the exported folder and replace it with the link."""

for plugin_file in plugins_file_list:
    if len(plugin_file) > 0:
        xml_plugin_root = xml.etree.ElementTree.parse(plugin_file).getroot()                        # parse the current plugin.xml file
        workspace_script_folder_paths = []
        for location_tag in xml_plugin_root.findall("./extension/location"):
            workspace_script_folder_paths.append(location_tag.get("folder"))

        for workspace_script_folder_path in workspace_script_folder_paths:
            tokens = workspace_script_folder_path.split(os.sep)
            for root in roots:
                if root == plugins_folder and len(tokens) > 1:                                      # for plugins, token[0] is the plugin name
                    path_matcher = os.path.join(root, tokens[0] + "_*")                             # build matcher for versioned plugin path from the plugin name
                    versioned_list = glob.glob(path_matcher)                                        # and get a list of matching folders (will match 0 or 1)
                    if len(versioned_list) > 0:
                        versioned = versioned_list[0]                                               # resolve the path of the exported versioned folder
                        tokens[0] = versioned.split(os.sep)[-1]                                     # use the last path element to replace the unversioned folder name
                        product_versioned_script_folder = str(os.path.join(*tokens[:]))             # create the new relative path.
                        script_depth = len(plugin_file.split(os.sep)) - 4          # Depth of script directory from git_parent
                        workspace_script_folder_path = ("*" + os.sep)*script_depth + workspace_script_folder_path  # Add prefix to allow matching of the git repository

                        write_link(root, workspace_script_folder_path, product_versioned_script_folder, len(tokens))
                elif os.path.exists(os.path.join(root, workspace_script_folder_path)):
                    write_link(root, workspace_script_folder_path, workspace_script_folder_path, len(tokens))
