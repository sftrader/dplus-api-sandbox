## Disney Activation and Entitlement Sandbox

The Disney Activation and Entitlement Sandbox ("aesandbox") code implements a sandbox environment to illustrate concepts from the Disney Activation and Entitlement API. The
runtime code is designed to use as little third-party software as possible in order to reduce any licensing conflicts.
The third-party runtime code consists of:
- [The JDK platform itself (OpenJDK 8 or higher)](https://openjdk.java.net/projects/jdk8/)
- [Micronaut (Basic HTTP server functionality)](https://micronaut.io)
- [Jackson Databind ObjectMapper (Java object to JSON conversion)](https://github.com/FasterXML/jackson-databind)

All security-related code used for JWT signing is included in the JDK.

#Functionality
- An interactive shell which can perform specific tasks illustrating parts of the Disney activation and entitlement
capabilities.
- A web endpoint for displaying a JSON Web Key Set (JWKS) and for forcibly rotating keys.  The JWKS endpoint is published at http://localhost:8484/jwks, and an endpoint for forcibly rotating keys is published at http://localhost:8484/jwks/rotate.  Note that the port value of 8484 is configurable (see Running below) and that the protocol is http, not https, for this demo.

Code of particular interest includes:
- Code in com.disney.aesandbox.commandline.tasks, which represent the individual tasks available in the shell.
- com.disney.aesandbox.token.TokenUtils.java, code for creating, signing, decoding, and verifying JSON Web Tokens (JWTs) in plain Java code.
- com.disney.aesandbox.keymgmt code.  DemoOnlyTransientKeyManager does the actual key management (except for storage
and retrieval of keys from disk or a database). TimeBasedKeyRotationPolicy abstracts out the key rotation policy
time spans. VerificationKeys shows how to create a Java key from the JWK representation.

#Running
The easiest way to run is to either clone or copy the Dockerfile in the repo and run that.  Assuming you already have Docker installed, open a terminal window, cd into the directory containing the Dockerfile, and
- % docker build -t dss-api .
- % docker run -p 8080:8484 -it dss-api

The "-it" flags are important since console access for input and output are required.

Startup takes a while, more than a whole minute on a typical laptop with good connectivity, due to the script in the container causing gradle to download some artifacts.  Be patient.

In this example, dss-api is just a name given to the docker image that will be built locally and run (you can use any name you like).  In the "-p" argument to docker run, 8080 is required (this is the internal port for the docker image), but 8484 can be changed to and valid port value (this is the externally-visible port on the system that will be used).

MacOS user note: some of the tasks ask for a JWT to be input on the command line.  In MacOS the length of the JWT may be longer than the shell accepts -- you will see that not all of the input line can be input or pasted in, and a tone will sound by default.  This can be worked around by inputting/pasting a portion of the line, typing CTRL-D, and then continuing.  There is a good discussion of this in https://unix.stackexchange.com/questions/204815/terminal-does-not-accept-pasted-or-typed-lines-of-more-than-1024-characters -- it is a MacOS/OSX quirk.

One task ("Test key rotation semantics by using input JWKS and token values") asks for a token and JWKS endpoint to be input for validation.  An easy way to see this task in action (if you don't already have the JWT/JWKS infrastructure in place) is to simply run two different Docker images of this environment on different ports, and use the JWKS endpoint from one environment to validate on the other. 

# The Command Shell

After startup, a numbered list of task options should be displayed.  Choose the task to run by number, you will be guided for any input needed.