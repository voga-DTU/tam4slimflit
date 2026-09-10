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

package dk.dtu.ese.rtca.nocAnalysis.nc;

import de.uni_kl.cs.discodnc.network.Network;
import de.uni_kl.cs.discodnc.network.Link;
import de.uni_kl.cs.discodnc.network.Server;
import de.uni_kl.cs.discodnc.network.Flow;

import de.uni_kl.cs.discodnc.curves.ArrivalCurve;
import de.uni_kl.cs.discodnc.curves.CurvePwAffine;
import de.uni_kl.cs.discodnc.curves.MaxServiceCurve;
import de.uni_kl.cs.discodnc.curves.ServiceCurve;

import de.uni_kl.cs.discodnc.nc.AnalysisConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

import java.io.File;
import java.io.FileNotFoundException;

public class UCBuilder {

// filenames - the path towards network, routes and flows are built from ucName
private String ucName;
private static String networkName = "net.csv";
private static String routeName = "routes.csv";
private static String flowName = "flows.csv";

//basic structures from files
private Map<String,Server> nodes;
private Map<String,Link> links;
private List<ArrayList<Link>> routes;
private Map<String, Flow> flows;

//auxiliary  structures for building a use-case
private Map<String, ArrayList<String>> innerEdges; // map( node: list( link)) incl. external and internal links
private Map<String, ArrayList<String>> outerEdges; // map( node: list( link)) incl. external and internal links
private Map<Link,String> linksInvertedIndex;

public UCBuilder(String ucName) {
this.ucName = ucName;
this.nodes = new HashMap<String,Server>();
this.links = new HashMap<String,Link>(); // incl. external and internal links
this.routes = new LinkedList<ArrayList<Link>>();
this.flows = new HashMap<String,Flow>();

this.innerEdges = new HashMap<String, ArrayList<String>>();
this.outerEdges = new HashMap<String, ArrayList<String>>();
this.linksInvertedIndex = new HashMap<Link,String>();
} // UCBuilder

public String getUCName() {
return this.ucName;
} // getUCName

public Map<String,Server> getNodes() {
return this.nodes;
} // getNodes

public Map<String,Link> getLinks() {
return this.links;
} // getLinks

public List<ArrayList<Link>> getRoutes() {
return this.routes;
} // getRoutes

public Map<String,Flow> getFlows() {
return this.flows;
} // getFlows


public Network buildNetwork() {
//build path to the network file
String sRoot = System.getProperty( "user.dir");
//sRoot = sRoot.substring( 0, sRoot.length() -4); //for removing the \bin part
String sFile = sRoot + System.getProperty( "file.separator") + "in" + System.getProperty( "file.separator") + this.ucName + System.getProperty( "file.separator") + this.networkName;
 
Network result = new Network();

//ServiceCurve sc = CurvePwAffine.getFactory().createRateLatency( 10.0e6, 0.01); //from DiscoDNC demos
//MaxServiceCurve msc = CurvePwAffine.getFactory().createRateLatencyMSC( 100.0e6, 0.001); //from DiscoDNC demos
//ServiceCurve scNI = CurvePwAffine.getFactory().createRateLatency( 1.0, 1.0E-7); // rate = 1 Flit/CT, latency = EPSILON (1.0E-7)
//ServiceCurve scNI = CurvePwAffine.getFactory().createRateLatency( 1.0, 0.0); // rate = 1 Flit/CT, latency = 0
ServiceCurve scNI = CurvePwAffine.getFactory().createRateLatency( 1.0, 1.0); // port/link rate = 1 Flit/CT, router processing latency = 1.0

ServiceCurve scLEW = CurvePwAffine.getFactory().createRateLatency( 1.0 / 4, 1.0E-7); // service curve for local, east and west ports
ServiceCurve scNS = CurvePwAffine.getFactory().createRateLatency( 1.0 / 2, 1.0E-7); // service curve for north and south ports

//MaxServiceCurve msc = CurvePwAffine.getFactory().createRateLatencyMSC( 1.0, 1.0E-7); // similar with service curve for Network Interface (NI)
MaxServiceCurve msc = CurvePwAffine.getFactory().createRateLatencyMSC( 1.0, 1.0); // similar with service curve for Network Interface (NI)

Boolean routerMode, nodeMode, linkMode;

File netIn = new File( sFile);

try {
routerMode = false;
nodeMode = false;
linkMode = false;

int size = 0;
int counter = 0; //the number of node lines

Scanner myReader = new Scanner( netIn);

while ( myReader.hasNextLine()) {
String line = myReader.nextLine();

if ( line.startsWith( "Router") && routerMode == false) {
routerMode = true;
} else if ( routerMode && line.startsWith( "Type")) {
continue;
} else if ( routerMode) {
continue;
} else if ( routerMode && line.startsWith( ",")) {
routerMode = false;
} else if ( line.startsWith( "Nodes")) {
nodeMode = true;

String[] lLine = line.split( ",");
String[] lSize = lLine[ 1].split( "x");

size = Integer.parseInt( lSize[ 0]);
} else if ( nodeMode && line.startsWith( "N")) {
 counter++;
String[] lLine = line.split( ",");

if ( size != lLine.length) {
System.err.println( "Error: The node line does not have " + size + "number of nodes!");
return null;
}

for (String sNode : lLine) {
this.nodes.put( sNode, result.addServer( scNI, msc));

//this.addRouterPorts( sNode, result, scLEW, scNS, msc); //custom method
} //for 
} else if ( nodeMode && line.startsWith( ",")) {
nodeMode = false;

if ( counter != size) {
System.err.println( "Error: the number of node lines is not correct! There must be " + size + "node lines.");
return null;
}
} else if ( line.startsWith( "Links")) {
linkMode = true;

//this.addInternalLinks( result, auxNodes);
} else if ( linkMode && line.startsWith( "L")) {
String[] lLine = line.split( ",");

if ( 3 != lLine.length) {
System.err.println( "Error: ill-formated link line! The line has " + lLine.length + "elements - only 3 must be provided.");
return null;
} 

Link l = result.addLink( this.nodes.get( lLine[ 1]), this.nodes.get( lLine[ 2]));
this.links.put( lLine[ 0], l);
this.linksInvertedIndex.put( l, lLine[ 0]);
} else if ( linkMode && line.startsWith( ",")) {
linkMode = false;
} else {
continue;
} // end if
} // end while
} catch ( FileNotFoundException e) {
e.printStackTrace();
} catch ( Exception e) {
e.printStackTrace();
}


return result;
} // buildNetwork

private void addRouterPorts( String sNode, Network net, ServiceCurve scX, ServiceCurve scY, MaxServiceCurve msc) {
String sRouter = sNode.replace( "N", "R");

Set<Integer> vals = Set.of( 0, 2, 4); //values of local, east and west ports

for (int i = 0; i < 5; i++) {
if ( vals.contains( i)) {
this.nodes.put( sRouter + "_p" + i, net.addServer( scX, msc));
} else {
this.nodes.put( sRouter + "_p" + i, net.addServer( scY, msc));
} // if
} //for

} // addRouterPorts

private void addInternalLinks( Network net, List<String> nodesNames) {
String sNode, nRouter, sRouter, nPort, sLocalPort, sPort, sLink;

sLocalPort = "p0";

try {

for ( String nNode : nodesNames) {
sNode = nNode.replace( ".", "");
nRouter = nNode.replace( "N", "R");
sRouter = sNode.replace( "N", "R");

nPort = nRouter + "_" + sLocalPort;

// step1: internal links between node and local port

sLink = "L_" + sNode + "_" + sRouter;

this.links.put( sLink, net.addLink( this.nodes.get( nNode), this.nodes.get( nPort)));

sLink = "L_" + sRouter + "_" + sNode;

this.links.put( sLink, net.addLink( this.nodes.get( nPort), this.nodes.get( nNode)));

String nLocalPort, currentSLocal;

nLocalPort = nRouter + "_" + sLocalPort;
currentSLocal = nLocalPort.replace( ".", "");

for ( int i = 1; i < 5; i++) {
// step2: internal link between local port and other router ports
nPort = nRouter + "_p" + i;
sPort = nPort.replace( ".", "");

sLink = "L_" + currentSLocal + "__" + sPort;

this.links.put( sLink, net.addLink( this.nodes.get( nLocalPort), this.nodes.get( nPort)));

sLink = "L_" + sPort + "__" + currentSLocal;

this.links.put( sLink, net.addLink( this.nodes.get( nPort), this.nodes.get( nLocalPort)));

for (int j = i + 1; j < 5; j++) {
// step3: internal link between other router ports
String nOtherPort, sOtherPort;

nOtherPort = nRouter + "_p" + j;
sOtherPort = nOtherPort.replace( ".", "");

sLink = "L_" + sPort + "__" + sOtherPort;

this.links.put( sLink, net.addLink( this.nodes.get( nPort), this.nodes.get( nOtherPort)));

sLink = "L_" + sOtherPort + "__" + sPort;

this.links.put( sLink, net.addLink( this.nodes.get( nOtherPort), this.nodes.get( nPort)));
} // for j
} // for i

} //for nNode
} catch (Exception e) {
e.printStackTrace();
} //try
} //addInternalLinks

public void buildRoutes() {
//build path to the routes file
String sRoot = System.getProperty( "user.dir");
//sRoot = sRoot.substring( 0, sRoot.length() -4); //for removing the \bin part
String sFile = sRoot + System.getProperty( "file.separator") + "in" + System.getProperty( "file.separator") + this.ucName + System.getProperty( "file.separator") + this.routeName;

File routeIn = new File( sFile);
 
try {
Scanner myReader = new Scanner( routeIn);

while ( myReader.hasNextLine()) {
String line = myReader.nextLine();

if ( line.contains( "Routes") || line.startsWith("List")) {
continue;
} else if ( line.startsWith( "L_")) {
String[] sLine = line.split( ",");
ArrayList<Link> path = new ArrayList<Link>();

for (String sLink:  sLine) {
if ( this.links.keySet().contains( sLink)) {
Link l = this.links.get( sLink);
path.add( l);
} else {
System.err.println( "ERROR: The parsed route contains a link that does not exist! The non-existing link is " + sLink + ".");
} // if
} // for

this.routes.add( path);
} else {
System.err.println( line);
System.err.println( "ERROR: wrong route line! A route is represented by a list of links.");
} // if

} // while
} catch ( FileNotFoundException e) {
e.printStackTrace();
} catch ( Exception e) {
e.printStackTrace();
}

} // buildRoutes

public void buildFlows( Network net) {
//build path to the flows file
String sRoot = System.getProperty( "user.dir");
//sRoot = sRoot.substring( 0, sRoot.length() -4); //for removing the \bin part
String sFile = sRoot + System.getProperty( "file.separator") + "in" + System.getProperty( "file.separator") + this.ucName + System.getProperty( "file.separator") + this.flowName;

File flowIn = new File( sFile);
 
try {
Scanner myReader = new Scanner( flowIn);

while ( myReader.hasNextLine()) {
String line = myReader.nextLine();

if ( line.startsWith( "Flows") || line.startsWith( "Id")) {
continue;
} else if ( line.startsWith( "F")) {
String[] sLine = line.split( ",");

String srcNode, tgtNode;

srcNode = sLine[ 1].replace( ".", "");
srcNode = srcNode.replace( "N", "");

tgtNode = sLine[ 2].replace( ".", "");
tgtNode = tgtNode.replace( "N", "");

List<Link> path = this.findRoute( srcNode, tgtNode);

if ( null != path) {
int size, burst, period; 

size  = Integer.parseInt( sLine[ 3]);

String[] sRate = sLine[ 4].split( "/");
burst = Integer.parseInt( sRate[ 0]);
if ( 2 <= sRate.length) {
period = Integer.parseInt( sRate[ 1]);
} else {
// length is 1
period = burst;
}

Double rate = ( burst * 1.0)/ period;

ArrivalCurve ac = CurvePwAffine.getFactory().createTokenBucket( rate, burst * 1.0);

this.flows.put( sLine[ 0], net.addFlow( ac, path));
} // if
} // if

} // while
} catch ( FileNotFoundException e) {
e.printStackTrace();
} catch ( Exception e) {
e.printStackTrace();
}

} // buildFlows

private List<Link> findRoute( String srcNode, String tgtNode) {
Link l1, l2;
String srcLink, tgtLink;

for ( ArrayList<Link> path: this.routes) {
l1 = path.get( 0);
l2 = path.get( path.size() -1);

srcLink = this.linksInvertedIndex.get( l1);
tgtLink = this.linksInvertedIndex.get( l2);

//if ( path.get( 0) == fromSrc && path.get( path.size() -1) == toTgt) {
if ( srcLink.contains( srcNode) && !srcLink.endsWith( srcNode) && tgtLink.endsWith( tgtNode)) {
return path;
} // if
} // for

// being here means that a path was not found
return null;
} // findRoute

public AnalysisConfig buildConfig() {
AnalysisConfig result = new AnalysisConfig();

result.setUseGamma( AnalysisConfig.GammaFlag.GLOBALLY_ON);
result.setUseExtraGamma( AnalysisConfig.GammaFlag.GLOBALLY_ON);

return result;
} // buildConfig

}
