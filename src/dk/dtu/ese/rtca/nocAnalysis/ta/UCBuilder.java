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

package dk.dtu.ese.rtca.nocAnalysis.ta;

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

private List<String> nodes; // [nodeID]
private List<String> links; // [linkID]
private List<ArrayList<String>> routes; // [[linkID]]
private List<String> flows; // [flowID]

//auxiliary  structures for building a use-case
private Map<String,ArrayList<String>> flow2path; // {flowID: [linkID]}
private Map<String,Double> flow2rate; // {flowID:rate}
private Map<String,ArrayList<String>> link2flows; // { linkId: [flowId]}
private Map<String,Double> linkLoad; // { linkId: sum flow.rate}

private Map<String,ArrayList<String>> link2nodes; // {linkID:[nodeID,nodeID]}
private Map<String, ArrayList<String>> innerEdges; // map( node: list( link)) incl. external and internal links
private Map<String, ArrayList<String>> outerEdges; // map( node: list( link)) incl. external and internal links

public UCBuilder(String ucName) {
this.ucName = ucName;

this.nodes = new ArrayList<String>(); // String>(); // [nodeID]
this.links = new ArrayList<String>(); // [linkID]
this.routes = new ArrayList<ArrayList<String>>(); // [[linkID]]
this.flows = new ArrayList<String>(); // [flowID]

this.flow2path = new HashMap<String,ArrayList<String>>(); // {flowID: [linkID]}
this.flow2rate = new HashMap<String,Double>(); // {flowID:rate}
this.link2flows = new HashMap<String,ArrayList<String>>(); // { linkId: [flowId]}
this.linkLoad = new HashMap<String,Double>(); // { linkId: sum flow.rate}

this.link2nodes = new HashMap<String,ArrayList<String>>(); // {linkID:[nodeID]}
this.innerEdges = new HashMap<String, ArrayList<String>>();
this.outerEdges = new HashMap<String, ArrayList<String>>();
} // UCBuilder

public String getUCName() {
return this.ucName;
} // getUCName

public List<String> getNodes() {
return this.nodes;
} // getNodes

public List<String> getLinks() {
return this.links;
} // getLinks

public Map<String,ArrayList<String>> getLinkToNodes() {
return this.link2nodes;
} // getLinkToNodes

public List<ArrayList<String>> getRoutes() {
return this.routes;
} // getRoutes

public List<String> getFlows() {
return this.flows;
} // getFlows

public Double getFlowRate( String flow) {
if ( this.flow2rate.keySet().contains( flow)) {
return this.flow2rate.get( flow);
} else {
return -1.0;
}
} // getFlowRate

public ArrayList<String> getFlowPath( String flow) {
if ( this.flow2path.keySet().contains( flow)) {
return this.flow2path.get( flow);
} else {
return null;
} // if
} // getFlowPath

public Double getLinkLoad( String link) {
if ( this.linkLoad.keySet().contains( link)) {
return this.linkLoad.get( link);
} else {
return -1.0;
} // if
} // getLinkLoad

public Map<String,Double> getLinksLoad() {
return this.linkLoad;
} // getLinksLoad

public void buildNetwork() {
//build path to the network file
String sRoot = System.getProperty( "user.dir");
//sRoot = sRoot.substring( 0, sRoot.length() -4); //for removing the \bin part
String sFile = sRoot + System.getProperty( "file.separator") + "in" + System.getProperty( "file.separator") + this.ucName + System.getProperty( "file.separator") + this.networkName;
 
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
return;
}

for (String sNode : lLine) {
this.nodes.add( sNode);
this.nodes.add( sNode.replace( "N", "R"));
} //for 
} else if ( nodeMode && line.startsWith( ",")) {
nodeMode = false;

if ( counter != size) {
System.err.println( "Error: the number of node lines is not correct! There must be " + size + "node lines.");
return;
}
} else if ( line.startsWith( "Links")) {
linkMode = true;

this.addEgressLocalLinks();
} else if ( linkMode && line.startsWith( "L")) {
String[] lLine = line.split( ",");

if ( 3 != lLine.length) {
System.err.println( "Error: ill-formated link line! The line has " + lLine.length + "elements - only 3 must be provided.");
return;
} 

if ( this.nodes.contains( lLine[ 1]) && this.nodes.contains( lLine[ 2])) {
this.links.add( lLine[ 0]);

ArrayList<String> l2n = new ArrayList<String>();
l2n.add( lLine[ 1].replace( "N", "R"));
l2n.add( lLine[ 2].replace( "N", "R"));

this.link2nodes.put( lLine[ 0], l2n);
} // if
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
} // buildNetwork

private void addEgressLocalLinks() {
ArrayList<String> l2n;
String sNode, aux, sLink;

for ( String sRouter: this.nodes) {
if ( sRouter.startsWith( "R")) {
sNode = sRouter.replace( "R", "N");

if ( ! this.nodes.contains( sNode)) {
System.err.println( "ERROR: The router " + sRouter + " does not have an equivalent node!");
return;
} //if

aux = sRouter.replace( "R", "");
aux = aux.replace( ".", "");

sLink = "L_" + sRouter + "_" + sNode;
this.links.add( sLink);

l2n = new ArrayList<String>();
l2n.add( sRouter);
l2n.add( sNode);

this.link2nodes.put( sLink, l2n);
} // if
} // for

} // addEgressLocalLinks

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
ArrayList<String> path = new ArrayList<String>();

int k = 0;

for (String sLink: sLine) {
if ( this.links.contains( sLink)) {
path.add( sLink);
} else {
System.err.println( "ERROR: The parsed route contains a link that does not exist! The non-existing link is " + sLink + ".");
} // if
k++;
} // for

// add the local egress link
String sLink = sLine[ k -1];
String[] aux = sLink.split( "_");
sLink = "L_" + aux[ 2] + "_" + aux[ 2];

if ( ! this.links.contains( sLink)) {
String sRouter;
ArrayList<String> l2n = new ArrayList<String>();
sRouter = "R" + aux[ 2].substring( 0, 1) + "." + aux[ 2].substring( 1);
l2n.add( sRouter);
l2n.add( sRouter.replace( "R", "N"));

this.links.add( sLink);
this.link2nodes.put( sLink, l2n);
} // if

path.add( sLink);

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

public void buildFlows() {
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

srcNode = sLine[ 1];
//srcNode = sLine[ 1].replace( ".", "");
srcNode = srcNode.replace( "N", "R");

tgtNode = sLine[ 2];
//tgtNode = sLine[ 2].replace( ".", "");
//tgtNode = tgtNode.replace( "N", "");

ArrayList<String> path = this.findRoute( srcNode, tgtNode);

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

this.flows.add( sLine[ 0]);

this.populateInternalFlowStructures( sLine[ 0], path, rate);
} // if
} // if

} // while
} catch ( FileNotFoundException e) {
e.printStackTrace();
} catch ( Exception e) {
e.printStackTrace();
}

} // buildFlows

private ArrayList<String> findRoute( String srcNode, String tgtNode) {
String l1, l2;
String srcNode_, tgtNode_;

for ( ArrayList<String> path: this.routes) {
l1 = path.get( 0);
l2 = path.get( path.size() -1);

srcNode_ = this.link2nodes.get( l1).get( 0);
tgtNode_ = this.link2nodes.get( l2).get( 1);

//if ( path.get( 0) == fromSrc && path.get( path.size() -1) == toTgt) {
//if ( srcLink.contains( srcNode) && !srcLink.endsWith( srcNode) && tgtLink.endsWith( tgtNode)) {
if ( srcNode_.equals( srcNode) && tgtNode_.equals( tgtNode)) {
return path;
} // if
} // for

// being here means that a path was not found
return null;
} // findRoute

private void populateInternalFlowStructures( String flow, ArrayList<String> path, Double rate) {
Double tmpRate;

this.flow2path.put( flow, path);
this.flow2rate.put( flow, rate);

for ( String link: path) {

if ( ! this.link2flows.keySet().contains( link)) {
this.link2flows.put( link, new ArrayList<String>());
} // if

this.link2flows.get( link).add( flow);

if ( ! this.linkLoad.keySet().contains( link)) {
this.linkLoad.put( link, 0.0);
} // if

tmpRate = this.linkLoad.get( link);
this.linkLoad.put( link, tmpRate + rate);
} // for

} // populateInternalFlowStructures

public String findMostLoadedLink() {
return this.findMostLoadedLink( ((ArrayList)this.links));
} // findMostLoadedLink

public String findMostLoadedLink( ArrayList<String> links) {
String result = "";
Double load = -1.0;

for ( String link: links) {
if ( this.linkLoad.keySet().contains( link) && this.linkLoad.get( link) > load) {
load = this.linkLoad.get( link);
result = link;
} // if
} // for

return result;
} // findMostLoadedLink

public boolean verifyRateConstraint() {
String mostLoadedLink;
Double flowRate, rateBound;
int k;

for ( String flow: this.flows) {
mostLoadedLink = this.findMostLoadedLink( flow2path.get( flow));

k = this.link2flows.get( mostLoadedLink).size();

if ( BAF.IS_ONE_REGISTER) {
rateBound = 1.0 / (2 * k);
} else {
rateBound = 1.0 / k;
} // if

flowRate = this.flow2rate.get( flow);
if ( rateBound < flowRate) {
System.err.println( "RATE-CONSTRAINT VIOLATION: For flow " + flow + " the input rate is " + flowRate + " and the rate upper bound is " + rateBound + "!");
return false;
} // if
} // for

//reaching here means that the constraint is met
return true;
} // verifyRateConstraint

}
