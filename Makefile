# Define Java compiler
JAVAC = javac

# Define Java source directory
SRCDIR = ./src

# Define Java source files
SOURCES := $(shell find $(SRCDIR) -name '*.java')

# Define output directory
OUTDIR = ./target

# Define Java flags
JFLAGS = -d $(OUTDIR) 

# Define target for compiling Java source files
all: $(SOURCES)
	$(JAVAC) $(JFLAGS) $(SOURCES)

run_server:
	java -classpath ".:dbDrivers/sqlite-jdbc-3.45.1.0.jar:dbDrivers/slf4j-api-1.7.36.jar:$(OUTDIR)" Server

run_client: # Maybe not need the DB? 
	java -classpath ".:$(OUTDIR)" Client

# Define target for cleaning up generated .class files. Logs are not in target or log directory yet...
clean:
	find $(OUTDIR) -name '*.class' -delete
	find $(OUTDIR) -name '*_messages.txt' -delete
	find . -name '*.log' -delete
	find . -name '*.db' -delete	

.PHONY: all clean