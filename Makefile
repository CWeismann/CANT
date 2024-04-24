# Define Java compiler
JAVAC = javac

# Define Java source directory
SRCDIR = .

# Define Java source files
SOURCES := $(shell find $(SRCDIR) -name '*.java')

# Define output directory
OUTDIR = .

# Define Java flags
JFLAGS = -d $(OUTDIR) 

# Define target for compiling Java source files
all: $(SOURCES)
	$(JAVAC) $(JFLAGS) $(SOURCES)

# Define target for cleaning up generated .class files
clean:
	find $(OUTDIR) -name '*.class' -delete
	find $(OUTDIR) -name '*.db' -delete
	find $(OUTDIR) -name '*_messages.txt' -delete
.PHONY: all clean