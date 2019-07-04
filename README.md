eQuATIC Tool
============

The eQuATIC tool addresses the growing need for quality in international cooperation.

It exposes and visualizes strengths and weaknesses, supporting policy makers and practitioners
in evaluating international cooperation.

Documentation
--------------
The acronym eQuATIC stands for Onlin**e** **Qu**ality **A**ssessment **T**ool for **I**nternational **C**ooperation.

More information can be found on the [eQuATIC homepage](http://www.equatic.ugent.be/ "eQuATIC Homepage")
under [Tool](http://www.equatic.ugent.be/tool/ "eQuATIC - Tool").

Building from Source
--------------------
The eQuaTIC Tool source code is hosted on [GitHub](https://github.com/equatic/equatic-tool
 "equatic-tool on github.com"). 

Clone the equatic-tool repository to your local development machine using your GitHub credentials.

This projects works fine with [OpenJDK 8](https://openjdk.java.net/projects/jdk8/ "OpenJDK8 Homepage")

You will need [Maven v3.5.0 or above](https://maven.apache.org/run-maven/index.html) to build the project.

Use the `mvn` command to build the differrent artifacts or check the vulnerabilities.

### Setting up the servers

You will first need to setup the credentials of the different servers you will use.

Consider using encrypted passwords, create a master password by running:

----
    $ mvn -emp
----

or

----
    $ mvn --encrypt-master-password your_master_password
----

Maven prints out an encrypted copy of the password to standard out.

Copy this encrypted password and paste it into your ~/.m2/settings-security.xml file.

```xml
  <settingsSecurity>
    <master>put_your_encrypted_master_password_here</master>
  </settingsSecurity>
```

Next encrypt the passwords for your servers

----
    $ mvn -ep your_server_password
----

Use the result in your ~/.m2/settings.xml file.

Find the <servers> section in your./m2/settings.xml file or create it if it does not yet exist.

Put all the necessary servers (see below) in the <servers> section.

#### Setting up the Oracle db test server

**Remark :** This is not needed if you skip the tests in your build by using -D maven.test.skip=true

Add a property equatic-test-db.url for your own Oracle test db server in your ~/.m2/settings.xml file to
override the default (if you do not have access to the UGent Oracle test db server).

```xml
  <equatic-test-db.url>put_your_oracle_test_db_url_here</equatic-test-db.url>
```

Add an Oracle test database server:

```xml
  <server>
    <id>equatic-test-db</id>
    <username>put_your_oracle_test_db_user_here</username>
    <password>put_your_encrypted_oracle_test_db_password_here</password>
  </server>
```

#### Setting up the Maven Oracle Repository server
 
Add the Maven Oracle Repository server.

```xml
  <server>
    <id>maven.oracle.com</id>
    <username>put_your_maven_oracle_user_here</username>
    <password>put_your_encrypted_maven_oracle_password_here</password>
    <configuration>
      <basicAuthScope>
        <host>ANY</host>
          <port>ANY</port>
          <realm>OAM 11g</realm>
      </basicAuthScope>
      <httpConfiguration>
        <all>
          <params>
            <property>
              <name>http.protocol.allow-circular-redirects</name>
              <value>%b,true</value>
            </property>
          </params>
        </all>
      </httpConfiguration>
    </configuration>
  </server>
```

#### Setting up the UGent eQuATIC servers
 
**Remark:**  This is only necessary if you have access to the UGent servers
 
Add the UGent eQuATIC servers:
 
```xml
  <server>
    <id>equatic-releases</id>
    <username>equatic</username>
    <password>put_the_encrypted_equatic_password_here</password>
  </server>    
```
 
and

```xml
  <server>
    <id>equatic-test-db</id>
    <username>equatic</username>
    <password>put_the_encrypted_equatic_test_db_password_here</password>
  </server>
```
 
### Building the war

----
	$ mvn clean install
----

The result can be found in the target directory.

### Building the site documentation

----
	$ mvn site
----

The result can be found in the target/site directory.

### Checking vulnerabilities

We use the [OWASP dependency check plugin](https://jeremylong.github.io/DependencyCheck/index.html)
to check vulnerabilities in the dependency tree.

The pom.xml contains two distinct profiles which activate the dependency checks.
The result can be seen in the output log of the build.
Look for the line (if present):

>`One or more dependencies were identified with known vulnerabilities in eQuATIC:`

The lines following this contain the vulnerabilities.

#### Profile security
 
This uses the suppression list /src/main/resources/security/project-suppression.xml to suppress
some (possible false-positive) errors or vulnerabilities in libraries on which there is no
 further developement.

----
	$ mvn -Psecurity dependency-check:check
----

#### Profile full-security
 
 This is the full security check. No suppresions are applied.
 
----
	$ mvn -Pfull-security dependency-check:check
----