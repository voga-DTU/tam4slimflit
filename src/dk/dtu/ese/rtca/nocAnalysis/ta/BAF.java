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
import java.util.Map;
import java.util.HashMap;

public class BAF {

public static boolean IS_ONE_REGISTER = false;

private static boolean IS_BLOCKING__AWARE = true;

private List<String> nodes; // [nodeID]
private List<String> links; // [linkID]
private Map<String,ArrayList<String>> link2nodes; // {linkID:[nodeID,nodeID]}

private Double pt;

public BAF( List<String> nodes, List<String> links, Map<String,ArrayList<String>> link2nodes, Double pt) {
this.nodes = nodes;
this.links = links;
this.link2nodes = link2nodes;
this.pt = pt;
} // BAF

public Double runAnalysis( String flow, List<String> path) {
Double delay = 0.0;
int size = path.size();

for ( int i = 0; i < size; i++) {
String sLink = path.get( i);

// WCAT + WCBT + PT
delay += this.wcat( sLink);

if ( BAF.IS_BLOCKING__AWARE &&i < size - 2) {
delay += this.wcbt( path.get( i + 1));
} // if

delay += this.pt; 
} // for

return delay;
} // runAnalysis 

private int wcat( String link) {

if ( this.isXDirection( link)) {
if ( BAF.IS_ONE_REGISTER) {
return 1;
} else {
// 2 registers
return 3;
} // if
} else {
// is Y direction or at destination towards local
if ( BAF.IS_ONE_REGISTER) {
return 3;
} else {
// 2 registers
return 7;
} // if
} //if

} // wcat

private boolean isXDirection( String link) {
String src, tgt;
int srcX, srcY, tgtX, tgtY;

src = this.link2nodes.get( link).get( 0);
tgt = this.link2nodes.get( link).get( 1);

if ( src.startsWith( "R") && tgt.startsWith( "N")) {
// as Y direction - at destination towards NI
return false;
} // if

String[] sSrc = src.substring( 1).split( "\\.");

srcX = Integer.parseInt( sSrc[ 0]);
srcY = Integer.parseInt( sSrc[ 1]);

String[] sTgt = tgt.substring( 1).split( "\\.");

tgtX = Integer.parseInt( sTgt[ 0]);
tgtY = Integer.parseInt( sTgt[ 1]);

if ( srcX == tgtX && (
srcY + 1 == tgtY ||
tgtY + 1 == srcY)) {
// is Y direction
return false;
} // if

return true;
} // isXDirection

private int wcbt( String link) {

if ( this.isXDirection( link)) {
return 2;
} else {
// is Y direction 
return 4;
} //if

} // wcbt

} 