## Disney Activation and Entitlement Sandbox

The Disney Activation and Entitlement Sandbox ("aesandbox") code implements a sandbox environment to illustrate concepts from the Disney Activation and Entitlement API. The
runtime code is designed to use as little third-party software as possible in order to reduce any licensing conflicts.
The third-party runtime code consists of:
- [The JDK platform itself (OpenJDK 8 or higher)](https://openjdk.java.net/projects/jdk8/)
- [Micronaut (Basic HTTP server functionality)](https://micronaut.io)
- [Jackson Databind ObjectMapper (Java object to JSON conversion)](https://github.com/FasterXML/jackson-databind)

All security-related code used for JWT signing is included in the JDK.

Functionality:
- An interactive shell which can perform specific tasks illustrating parts of the Disney activation and entitlement
capabilities.
- A web endpoint for displaying a JSON Web Key Set (JWKS) and for forcibly rotating keys.

Code of particular interest includes:
- Code in com.disney.aesandbox.commandline.tasks, which represent the individual tasks available in the shell.
- com.disney.aesandbox.token.TokenUtils.java, code for creating, signing, decoding, and verifying JSON Web Tokens (JWTs) in plain Java code.
- com.disney.aesandbox.keymgmt code.  DemoOnlyTransientKeyManager does the actual key management (except for storage
and retrieval of keys from disk or a database). TimeBasedKeyRotationPolicy abstracts out the key rotation policy
time spans. VerificationKeys shows how to create a Java key from the JWK representation.