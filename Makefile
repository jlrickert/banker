JFLAGS = -g
JAVA = java
JAVAC = javac
JAR = jar

SRCDIR = src
TARGETDIR = target

SOURCES := $(shell find src -name  "*.java")
CLASSES := $(SOURCES:src/%.java=$(TARGETDIR)/%.class)

all: run

run: build
	@cd target
	@$(JAVA) -cp $(TARGETDIR) Program $(filter-out $@, $(MAKECMDGOALS))

build: _target $(CLASSES)

release: build
	cp banker.mf $(TARGETDIR)/banker.mf
	cd $(TARGETDIR)
	jar cmf banker.mf $(TARGETDIR)/banker.jar $(CLASSES)

docs: _target
	javadoc -sourcepath $(SRCDIR) -d $(TARGETDIR)/docs $(SOURCES)

clean:
	rm -rf ./target

.PHONY: clean
.SUFFIXES: .java .class

$(CLASSES): $(TARGETDIR)/%.class : $(SRCDIR)/%.java
	$(JAVAC) -sourcepath src -d $(TARGETDIR) $<

_target:
	mkdir -p $(TARGETDIR)
	mkdir -p $(TARGETDIR)/docs

%:
	@true
