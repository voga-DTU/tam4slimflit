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

import dk.dtu.ese.rtca.nocAnalysis.nc.UCBuilder;

import de.uni_kl.cs.discodnc.network.Network;
import de.uni_kl.cs.discodnc.network.Link;
import de.uni_kl.cs.discodnc.network.Flow;

import de.uni_kl.cs.discodnc.nc.AnalysisConfig;

import de.uni_kl.cs.discodnc.nc.analyses.TotalFlowAnalysis;
import de.uni_kl.cs.discodnc.nc.analyses.SeparateFlowAnalysis;
import de.uni_kl.cs.discodnc.nc.analyses.PmooAnalysis;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;

public class StartNC {

private static String ucName;
private static double ROUTER_LATENCY;

public static void main( String[] args) {
StartNC.readRunConfig();

StartNC.redirectOutput();

UCBuilder ucb = new UCBuilder( StartNC.ucName);

Network net = ucb.buildNetwork();
System.out.println( "Successfully built the network!");
//System.out.println( network);

ucb.buildRoutes();
System.out.println( "Successfully built the routes!");

ucb.buildFlows( net);
System.out.println( "Successfully build the flows!");
//System.out.println( "Flows: " + ucb.getFlows());

AnalysisConfig aConfig = ucb.buildConfig();

//StartNC.runTFA( net, aConfig, ucb.getFlows());
//System.out.println( "");

//StartNC.runSFA( net, aConfig, ucb.getFlows());
//System.out.println( "");

StartNC.runPMOO( net, aConfig, ucb.getFlows());
System.out.println( "");

} // main

private static void readRunConfig() {
// build the file name
String sRoot = System.getProperty( "user.dir");

// Uncomment the following line if you want to compile and run using batch files
//sRoot = sRoot.substring( 0, sRoot.length() -4); //for removing the \bin part

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

StartNC.ucName = sVal.toLowerCase();
} else if ( flg && line.startsWith( "Use-Case")) {
System.err.println( "ERROR: Only a single use-case can be active for an execution!");
return;
} else if ( line.startsWith( "ROUTER_LATENCY")) {
String[] sLine = line.split( ":");
String sVal = sLine[ 1].replace( " ", "");

StartNC.ROUTER_LATENCY = Double.parseDouble( sVal);
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

// Uncomment the following line if you want to compile and run using batch files
//sOut = sOut.substring( 0, sOut.length() -4); // for removing the \bin part

sOut = sOut + System.getProperty( "file.separator") + "out";
String fOut = sOut + System.getProperty( "file.separator") + StartNC.ucName + ".txt";
String fErr = sOut + System.getProperty( "file.separator") + StartNC.ucName + "_err.txt";

try { 
System.setOut( new PrintStream( new FileOutputStream( fOut)));
System.setErr( new PrintStream( new FileOutputStream( fErr)));
} catch( FileNotFoundException e) {
e.printStackTrace();
} // try
} // redirectOutput

private static void runTFA( Network net, AnalysisConfig aConfig, Map<String,Flow> flows) {
System.out.println("--- Total Flow Analysis (TFA) ---");
TotalFlowAnalysis tfa = new TotalFlowAnalysis( net, aConfig);

try {
for ( String sFlow : flows.keySet()) {
tfa.performAnalysis( flows.get( sFlow));

System.out.println( "---" + sFlow + "---");
System.out.println("delay bound     : " + tfa.getDelayBound());
System.out.println("backlog bound   : " + tfa.getBacklogBound());

//System.out.println( "Delay per server : " + tfa.getServerDelayBoundMapString());
//System.out.println( "Backlog per server : " + tfa.getServerBacklogBoundMapString());

//System.out.println("alpha per server: " + tfa.getServerAlphasMapString());
} // for
} catch (Exception e) {
System.err.println("TF analysis failed");
e.printStackTrace();
} // try
} // runTFA

private static void runSFA( Network net, AnalysisConfig aConfig, Map<String,Flow> flows) {
System.out.println( "--- Separated Flow Analysis (SFA)---");
SeparateFlowAnalysis sfa = new SeparateFlowAnalysis( net, aConfig);

try {
for ( String sFlow : flows.keySet()) {
sfa.performAnalysis( flows.get( sFlow));

System.out.println( "---" + sFlow + "---");

//System.out.println("e2e SFA SCs     : " + sfa.getLeftOverServiceCurves());
//System.out.println("     per server : " + sfa.getServerLeftOverBetasMapString());
//System.out.println("xtx per server  : " + sfa.getServerAlphasMapString());

System.out.println("delay bound     : " + sfa.getDelayBound());
System.out.println("backlog bound   : " + sfa.getBacklogBound());
} // for
        } catch (Exception e) {
System.err.println("SFA analysis failed");
e.printStackTrace();
        } // try
} // runSFA

private static void runPMOO( Network net, AnalysisConfig aConfig, Map<String,Flow> flows) {
System.out.println("--- Pay Multiplexing Only Once (PMOO) Analysis ---");
PmooAnalysis pmoo = new PmooAnalysis( net, aConfig);

try {
for ( String sFlow : flows.keySet()) {
pmoo.performAnalysis( flows.get( sFlow));

System.out.println( "---" + sFlow + "---");
System.out.println("delay bound     : " + pmoo.getDelayBound());
System.out.println("backlog bound   : " + pmoo.getBacklogBound());

//System.out.println("alpha per server: " + pmoo.getServerAlphasMapString());
//System.out.println("e2e PMOO left over SCs    : " + pmoo.getLeftOverServiceCurves());
} // for
} catch (Exception e) {
System.err.println("PMOO analysis failed");
e.printStackTrace();
} // try
} //runPMOO

}
