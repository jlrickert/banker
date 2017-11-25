PROJECT = banker
MAIN = banker

JFLAGS = -g
JAVA = java
JAVAC = javac
JAR = jar

SRCDIR = src
BUILDDIR = target

SOURCES := $(shell find src -name  "*.java")
CLASSES := $(SOURCES:src/%.java=$(BUILDDIR)/%.class)

all: run

doc:

run: build
	@cd target
	@$(JAVA) -cp $(BUILDDIR) Program $(filter-out $@, $(MAKECMDGOALS))

build: _target $(CLASSES)

clean:
	@rm -rf ./target

.PHONY: clean
.SUFFIXES: .java .class

$(CLASSES): $(BUILDDIR)/%.class : $(SRCDIR)/%.java
	$(JAVAC) -sourcepath src -d $(BUILDDIR) $<

_target:
	@mkdir -p $(BUILDDIR)

%:
	@true
