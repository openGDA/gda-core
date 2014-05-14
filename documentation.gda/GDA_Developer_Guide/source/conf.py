# -*- coding: utf-8 -*-

# get standard configurations settings
import os
conf_common_path = os.path.join(os.path.dirname(__file__), '..', '..', 'common', 'conf_common.py')
if not os.path.isfile(conf_common_path):
    raise Exception, 'File %s not found' % (conf_common_path,)
execfile(conf_common_path)

# General information about the project.
project = u'GDA Developer Guide'

# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title, author, documentclass [howto/manual]).
latex_documents = [
  ('contents', 'GDA_Developer_Guide.tex', u'GDA Developer Guide',
   _author_diamond, 'manual'),
]

