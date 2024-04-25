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

run_server:
	java -classpath ".:sqlite-jdbc-3.45.1.0.jar:slf4j-api-1.7.36.jar" Server

run_client: # Maybe not need the DB? 
	java Client 

# Define target for cleaning up generated .class files
clean:
	find $(OUTDIR) -name '*.class' -delete
	find $(OUTDIR) -name '*.db' -delete
	find $(OUTDIR) -name '*_messages.txt' -delete
	find $(OUTDIR) -name '*.log' -delete
	
.PHONY: all clean