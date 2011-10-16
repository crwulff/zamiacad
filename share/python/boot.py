
from java.io import File
from org.zamia import ERManager, FSCache, SourceFile, SourceLocation, ZamiaException, ZamiaProject, ZamiaLogger, ExceptionLogger
import sys

logger = ZamiaLogger.getInstance()
el = ExceptionLogger.getInstance()

def printf(format, *args):
    sys.stdout.write(format % args)

def help():
  print "zamiaCAD builtin python functions:"
  print "=================================="
  print ""
  print "Builtins:"
  print "---------"
  print "zamia_source(uri)"
  print ""
  print "Builder:"
  print "--------"
  print "clean()"
  print "rebuild()"
  print ""
  print "Markers:"
  print "--------"
  print "marker_add(path,line,column,msg,is_error)"
  print "marker_list()"
  print "marker_clean(path)"
  print ""

def zamia_source(uri):
  project.getZCJ().evalFile(uri)

#
# Builder
#

def clean():
  project.clean()

def rebuild():
  builder = project.getBuilder()
  builder.build(True, True, None)

  n = project.getERM().getNumErrors()
  printf ('python: Build finished. Found %d errors.\n', n)

#
# Markers
#

def marker_add(path,line,column,msg,is_error):

  erm = project.getERM()
                        
  # make path absolute
  f = File(path)
  if not f.isAbsolute():
    f = File(project.getBasePath()+File.separator+path)
  
  sf = SourceFile(f)

  if not FSCache.getInstance().exists(sf, True):
    logger.error("python: script tried to add error to non-existant file: %s", sf.getAbsolutePath())
  else:
    location = SourceLocation(sf, line, column)
    erm.addError(ZamiaException(ZamiaException.ExCat.EXTERNAL, is_error, msg, location))
    
def marker_list():

  erm = project.getERM()

  n = erm.getNumErrors()

  for i in range(n):
    em = erm.getError(i)
    printf('%s\n', em.toString())


def marker_clean(path):
                        
  erm = project.getERM()
                        
  sf = SourceFile(File (path))
                        
  erm.removeErrors(sf, ZamiaException.ExCat.EXTERNAL)





