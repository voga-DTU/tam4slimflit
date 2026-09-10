/*
 * This file is part of the timing Analysis Methods for SlimFlit (TAM4SlimFlit).
 *
 * Copyright © 2026 Technical University of Denmark
 *
 * This version of the software was developed by Voica Gavrilut, Postdoctoral Researcher, DTU-Compute.
 *
 * Licensed under the Apache License, Version 2.0 (LICENSE-2.0.txt);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://apache.org
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 * Use code with caution.
 */

package dk.dtu.ese.rtca.nocAnalysis;

import dk.dtu.ese.rtca.nocAnalysis.ta.UCBuilder;
import dk.dtu.ese.rtca.nocAnalysis.ta.BAF;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;

public class StartTA {

private static String ucName;
private static double ROUTER_LATENCY;

public static void main( String[] args) {
StartTA.readRunConfig();

StartTA.redirectOutput();

UCBuilder ucb = new UCBuilder( StartTA.ucName);

ucb.buildNetwork();
System.out.println( "Successfully built the network!");

ucb.buildRoutes();
System.out.println( "Successfully built the routes!");

ucb.buildFlows();
System.out.println( "Successfully build the flows!");

String mostLoadedLink = ucb.findMostLoadedLink();
System.err.println( "LOG: The most loaded link is " + mostLoadedLink + " with a load of " + ucb.getLinkLoad( mostLoadedLink));
System.out.println( "The " + StartTA.ucName + ( ucb.verifyRateConstraint() ? " meet": " dosen't meet") + " the rate-constraint!");

StartTA.runBAF( ucb.getNodes(), ucb.getLinks(), ucb.getLinkToNodes(), ucb.getFlows(), ucb);
System.out.println( "");

} // main

private static void readRunConfig() {
// build the file name
String sRoot = System.getProperty( "user.dir");

String sFile = sRoot + System.getProperty( "file.separator") + "config_run.txt"; 
File crIn = new File( sFile);

try {
Scanner myReader = new Scanner( crIn);

Boolean flg = false;

while ( myReader.hasNextLine()) {
String line = myReader.nextLine();

if ( line.startsWith( "#")) {
continue;
} else if ( ! flg && line.startsWith( "USE-CASE")) {
flg = true;

String[] sLine = line.split( ":");
String sVal = sLine[ 1].replace( " ", "");
sVal = sVal.replace( ".", "dot");
sVal = sVal.replace( "-", "dash");

if ( sVal.startsWith( "a")) {
StartTA.ucName = sVal;
} else {
StartTA.ucName = sVal.toLowerCase();
} // if
} else if ( flg && line.startsWith( "Use-Case")) {
System.err.println( "ERROR: Only a single use-case can be active for an execution!");
return;
} else if ( line.startsWith( "ROUTER_LATENCY")) {
String[] sLine = line.split( ":");
String sVal = sLine[ 1].replace( " ", "");

StartTA.ROUTER_LATENCY = Double.parseDouble( sVal);
} // end if
} // end while
} catch ( FileNotFoundException e) {
e.printStackTrace();
} catch ( Exception e) {
e.printStackTrace();
} // end try
} // readRunConfig

private static void redirectOutput() {
// create filenames for fOut and fErr
String sOut = System.getProperty( "user.dir");

sOut = sOut + System.getProperty( "file.separator") + "out";
String fOut = sOut + System.getProperty( "file.separator") + StartTA.ucName + ".txt";
String fErr = sOut + System.getProperty( "file.separator") + StartTA.ucName + ".log";

try { 
System.setOut( new PrintStream( new FileOutputStream( fOut)));
System.setErr( new PrintStream( new FileOutputStream( fErr)));
} catch( FileNotFoundException e) {
e.printStackTrace();
} // try
} // redirectOutput

private static void runBAF( List<String> nodes, List<String> links, Map<String,ArrayList<String>> link2nodes, List<String> flows, UCBuilder ucb) {
System.out.println( "--- Blocking-Aware Function (BAF) ---");
BAF baf = new BAF( nodes, links, link2nodes, StartTA.ROUTER_LATENCY);
Double d;

for ( String flow: flows) {
d = baf.runAnalysis( flow, ucb.getFlowPath( flow));

System.out.println( "---" + flow + "---");
System.out.println( "path length: " + (ucb.getFlowPath( flow).size() - 1));
System.out.println("delay: " + d);
} // for

} // runBAF

}
