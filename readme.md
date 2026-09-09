This is the java project for the timing analysis method (TAM) of a Network on Chip (NoC) based on SlimFlit. 

The folder of the project is generically referred as <HOME_TAM4SLIMFLIT>.

(in)=
# Input Files

The input files required for analysis are organised in the so-called use-cases (UC). Only a single use-case can be run (analysed) at a time.

The files of an UC are stored in the folder <HOME_TAM4SLIMFLIT>/in/<USECASE_NAME> where <USECASE_NAME> represents the name of the use-case.

An UC is given by a network, a set of flows and the routes of the flows.

The UC name that is analysed is specified in the file <HOME_TAM4SLIMFLIT>/config_run.txt under the 'Use-Case' field.

The distribution of the project, contains a configuration file with the analysis set for 'UC2.1', 'UC2.2', 'UC2.3' and 'autoLit' as described in the paper.

## Network

The network is represented by a set of nodes and of links that are interconnecting the nodes.

The network must be specified in the file <HOME_TAM4SLIMFLIT>/in/<USECASE_NAME>/net.csv.
The file <HOME_TAM4SLIMFLIT>/in/uc2dot1/net.csv contains an example of a network.

## Flows

The traffic is represented by the set of flows.
A flow is characterized by source node (src), target node (tgt), maximum size in number of flits (size) and the maximum rate expressed in flits per cycle time (rate).

The traffic must be specified in the file <HOME_TAM4SLIMFLIT>/in/<USECASE_NAME>/flows.csv.
The file <HOME_TAM4SLIMFLIT>/in/uc2dot1/flows.csv contains a traffic example.

## Routes

The route (a unicast path) represents a set of links that the flow is crossing from source to target node.
The mapping flow-to-route is given by the order of flows and routes in their respective files.

The traffic must be specified in the file <HOME_TAM4SLIMFLIT>/in/<USECASE_NAME>/routes.csv.
The file <HOME_TAM4SLIMFLIT>/in/uc2dot1/routes.csv contains a routing example.

Note: An explicit flow-to-route mapping is missing and should be implemented in a future version.

(run)=
# Compile and Run

The compilation and the execution can be done in 2 ways that are detailed in the following. 

## Compiling, Running and Archiving with Maven 

The pom.xml file can be found at <HOME_TAM4SLIMFLIT> contains instructions for: 

- compiling all sources (.java files) in dk.dtu.ese.rtca.nocAnalysis package 
- run a specific main class from dk.dtu.ese.rtca.nocAnalysis and
- create the executable java archive (JAR) for this project.

Before compiling or running with maven, please make sure that you have maven installed.

For compilation, it should be used the command:
```
mvn compile
```

Before running a timing analysis, please read the section about the [input files](#in).

For execution, it should be used the command:
```
mvn exec:java
```
Running this command with the provided pom.xml, it is executed the class dk.dtu.ese.rtca.nocAnalysis.StartNC.

For archiving (or packaging) the project into a single jar file, it should be used the command:
```
mvn clean package
```
if the classes are already compiled or
```
mvn clean compile package
```

The resulting jar can be found in <HOME_TAM4SLIMFLIT>/target/tam4slimflit-<VERSIN_ID>.jar.
For executing this jar, please move it a level up in <HOME_TAM4SLIMFLIT>.

Running the above commands with the provided pom.xml, 
it is obtained the JAR <HOME_TAM4SLIMFLIT>/tam4slimflit-1.0-BAF.jar 
with the main class dk.dtu.ese.rtca.nocAnalysis.StartTA.

(out)=
# Reading the Output

Read the output from the file <HOME_TAM4SLIMFLIT>/out/<USECASE_NAME>.txt.

The <USECASE_NAME> represents the name of the use-case as specified in the running configuration file. 
For more details about the configuration file, please see the section about [input files](#in). 

The output file, in case of a successful execution, contains the following:
- confirmation that network, flows and routes were successfully parsed,
- a message if the rate-constrained is met or not by the UC,
- the name of the analysis method, such as Blocking-Aware Function (BAF) or Pay-Multiplexing Only Once (PMOO) and
- the path length and delay for each flow if our BAF method is used
	* or the delay for each flow if network-calculus methods are used.

In case of an error, the error messages can be found in the file <HOME_TAM4SLIMFLIT>/out/<USECASE_NAME>.log.
Please note that this file contains also log message of our method.
