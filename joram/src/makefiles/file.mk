# Copyright (C) 1996 - 2000 BULL
# Copyright (C) 1996 - 2000 INRIA
# 
# This makefile is included in general.mk whenever the SOURCES or CLASSES
# variable is defined.
# For example, if you have a package 'foo' containing 'a.java' and 'b.java'
# your Makefile should look like this:
# ----------
# SOURCES = a.java b.java
# include $(GENERAL_MK)
# ----------
# It is also indirectly included in library.mk.
#
# This file defines the following targets:
# all:   to build the class files from the java files.
# clean: to clean all sub packages
# coldoc: to build the appropriate documentation files from the source
# It also defines phony targets named by the class name, allowing
# to compile just the class

# either SOURCES or CLASSES is defined
ifndef CLASSES
CLASSES =
endif
_CLASSES_ = $(filter-out @% %@,$(subst @,@ @,$(CLASSES)))
ifdef SOURCES
_CLASSES_ += $(foreach s,$(SOURCES:$(_JAVA_SOURCE_)=$(_JAVA_CLASS_)), \
	$(subst S,$s,$(filter S,S$(filter $s,$(CLASSES)))))
endif

_JCUPCLASSES_ = $(JCUP)
_JCUPSYMBOLS_ = $(JCUP:$(_JAVA_CLASS_)=$(_JCUP_SYMBOLS_))
_JCUPPARSER_ = $(JCUP:$(_JAVA_CLASS_)=$(_JCUP_PARSER_))
_SOURCES_ = $(_CLASSES_:$(_JAVA_CLASS_)=$(_JAVA_SOURCE_))
_OBJECTS_ = $(_CLASSES_:$(_JAVA_CLASS_)=$(_JAVA_OBJECT_))

all:: $(_CLASSES_) $(_JCUPCLASSES_)

_OBJECT_LIBS_ = $(@:$(_JAVA_OBJECT_)=$(_JAVA_CLASS_))_LIBS
_OBJECT_LIBS_ORIGIN_ =\
	$(patsubst x%,defined,\
	  $(patsubst xundefined, undefined,\
	    $(patsubst %,x%,\
	      $(origin $(_OBJECT_LIBS_)))))
_LIBS_ =\
	$(patsubst defined,$($(_OBJECT_LIBS_)),\
	  $(patsubst undefined,$(LIBS),\
	    $(_OBJECT_LIBS_ORIGIN_)))
_CLASSPATH_ = $(OBJDIR://$(DRIVE)%=$(DRIVE):%)$(subst :,$(PATH_SEP)$(LIBDIR://$(DRIVE)%=$(DRIVE):%)/,$(_LIBS_:%=:%))$(CLASSPATH:%=$(PATH_SEP)%)

# Add a second space before all names, circumventing a known bug in make B20.1
# with no found solution
$(_OBJECTS_:%= %): $(_JAVA_OBJECT_): $(_JAVA_SOURCE_)
	$(JAVAC) -g -deprecation -classpath '$(_CLASSPATH_)' -d $(OBJDIR://$(DRIVE)%=$(DRIVE):%) \
	  $(JFLAGS) $<

colldoc::
	echo $(PACKAGE) >> $(OBJDIR)/doc_packages

clean::
	-rm -f $(OBJDIR)/$(RELPATH)/*.class $(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JAVA_SOURCE_)) $(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JCUP_PARSER_SOURCE_)) $(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JCUP_SYMBOLS_SOURCE_))

$(_CLASSES_): $(_JAVA_CLASS_): $(_JAVA_OBJECT_)

$(_JCUPCLASSES_): $(_JAVA_CLASS_): $(_JAVA_OBJECT_) $(_JCUP_PARSER_OBJECT_) $(_JCUP_SYMBOLS_OBJECT_)

$(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JAVA_OBJECT_)): $(_JAVA_OBJECT_): $(_JFLEX_SOURCE_) $(_JCUP_SYMBOLS_OBJECT_)
	$(JAVA) -classpath '$(_CLASSPATH_)' JFlex.Main $<
	sed s/sym.EOF/$(<:$(_JFLEX_SOURCE_)=$(_JCUP_SYMBOLS_)).EOF/g $(<:$(_JFLEX_SOURCE_)=$(_JAVA_SOURCE_)) \
	| sed s/class\ $(JCUP)/public\ class\ $(JCUP)/g \
	| sed s/$(JCUP)\(/public\ $(JCUP)\ \(/g > $(<:$(_JFLEX_SOURCE_)=$(_JAVA_SOURCE_).bad)
	mv $(<:$(_JFLEX_SOURCE_)=$(_JAVA_SOURCE_).bad) $(<:$(_JFLEX_SOURCE_)=$(_JAVA_SOURCE_))
	$(JAVAC) -g -deprecation -classpath '$(_CLASSPATH_)' -d $(OBJDIR://$(DRIVE)%=$(DRIVE):%)/. \
	$(JFLAGS) $(<:$(_JFLEX_SOURCE_)=$(_JAVA_SOURCE_))

$(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JFLEX_SOURCE_)): $(_JFLEX_SOURCE_): 


$(_JCUPPARSER_:$(_JAVA_CLASS_)=$(_JAVA_OBJECT_)): $(_JCUP_PARSER_OBJECT_): $(_JCUP_PARSER_SOURCE_)
	$(JAVAC) -g -deprecation -classpath '$(_CLASSPATH_)' -d $(OBJDIR://$(DRIVE)%=$(DRIVE):%)/. \
	$(JFLAGS) $(<:$(_JCUP_SOURCE_)=$(_JCUP_PARSER_SOURCE_))

$(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JCUP_PARSER_SOURCE_)): $(_JCUP_PARSER_SOURCE_): $(_JCUP_SOURCE_)
	$(JAVA) -classpath '$(_CLASSPATH_)' java_cup.Main -symbols $(<:$(_JCUP_SOURCE_)=$(_JCUP_SYMBOLS_)) -parser $(<:$(_JCUP_SOURCE_)=$(_JCUP_PARSER_)) < $<

$(_JCUPSYMBOLS_:$(_JAVA_CLASS_)=$(_JAVA_OBJECT_)): $(_JCUP_SYMBOLS_OBJECT_): $(_JCUP_SYMBOLS_SOURCE_)
	$(JAVAC) -g -deprecation -classpath '$(_CLASSPATH_)' -d $(OBJDIR://$(DRIVE)%=$(DRIVE):%)/$(RELPATH)/. \
	$(JFLAGS) $(<:$(_JCUP_SOURCE_)=$(_JCUP_SYMBOLS_SOURCE_))

$(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JCUP_SYMBOLS_SOURCE_)): $(_JCUP_SYMBOLS_SOURCE_): $(_JCUP_SOURCE_)
	$(JAVA) -classpath '$(_CLASSPATH_)' java_cup.Main -symbols $(<:$(_JCUP_SOURCE_)=$(_JCUP_SYMBOLS_)) -parser $(<:$(_JCUP_SOURCE_)=$(_JCUP_PARSER_)) < $<

$(_JCUPCLASSES_:$(_JAVA_CLASS_)=$(_JCUP_SOURCE_)): $(_JCUP_SOURCE_):

.PHONY: $(_CLASSES_)
