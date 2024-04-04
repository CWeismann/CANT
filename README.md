# CANT

Runs on Mac, and should run on other UNIX systems.

cant (noun):
1. An argot, the jargon of a particular class or subgroup.
2. A secure messaging app.

Needs to have the jdbc driver installed.
# https://github.com/xerial/sqlite-jdbc

Commands to run this software: 
1. `make`: Build the project
2. `make run_server`: Run the server
3. `make register USER=<username> PASS=<password>`: Register a username and password
4. `make connect USER=<username> PASS=<password>`: Login to the server
5. `python3 dumpDB.py`: Dump the entire messages and login database. For testing purposes. 