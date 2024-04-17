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
	java -classpath ".:sqlite-jdbc-3.45.1.0.jar:slf4j-api-1.7.36.jar" CantServer

connect:
	java -classpath ".:sqlite-jdbc-3.45.1.0.jar:slf4j-api-1.7.36.jar" CantClient $(USER) $(PASS) 

register:
	java -classpath ".:sqlite-jdbc-3.45.1.0.jar:slf4j-api-1.7.36.jar" CantClient $(USER) $(PASS) 1


run_client: # delete this
	java -classpath ".:sqlite-jdbc-3.45.1.0.jar:slf4j-api-1.7.36.jar" CantClient

# Define target for cleaning up generated .class files
clean:
	find $(OUTDIR) -name '*.class' -delete
	find $(OUTDIR) -name '*.db' -delete
	find $(OUTDIR) -name '*_conversations.txt' -delete
.PHONY: all clean
