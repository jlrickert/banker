JFLAGS = -g
JAVA = java
JAVAC = javac
JAR = jar

SRCDIR = src
BUILDDIR = target

SOURCES := $(shell find src -name  "*.java")
CLASSES := $(SOURCES:src/%.java=$(BUILDDIR)/%.class)

all: run

run: build
	@cd target
	@$(JAVA) -cp $(BUILDDIR) Program $(filter-out $@, $(MAKECMDGOALS))

build: _target $(CLASSES)

docs: _target
	javadoc -sourcepath $(SRCDIR) -d $(BUILDDIR)/docs $(SOURCES)

clean:
	@rm -rf ./target

.PHONY: clean
.SUFFIXES: .java .class

$(CLASSES): $(BUILDDIR)/%.class : $(SRCDIR)/%.java
	$(JAVAC) -sourcepath src -d $(BUILDDIR) $<

_target:
	@mkdir -p $(BUILDDIR)
	@mkdir -p $(BUILDDIR)/docs

%:
	@true
