/****************************************************************************
 **
 ** This file is part of yFiles-2.7.0.1. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package darls;

import java.sql.*;

import y.layout.Layouter;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.module.HierarchicLayoutModule;

//import demo.view.advanced.InactiveLayerDemo.MyPopupMode;
//import demo.view.advanced.InactiveLayerDemo.Graph2DDrawable;
//import demo.view.realizer.YLabelConfigurationDemo.MyPainter;
//import demo.view.realizer.YLabelConfigurationDemo.PointPathProjector;
//import demo.view.realizer.YLabelConfigurationDemo.PointPathProjector.CustomPathIterator;

import y.anim.AnimationEvent;
import y.anim.AnimationFactory;
import y.anim.AnimationListener;
import y.anim.AnimationObject;
import y.anim.AnimationPlayer;
import y.anim.CompositeAnimationObject;
import y.base.DataProvider;
import y.base.EdgeCursor;
import y.base.EdgeList;
import y.base.Node;
import y.base.NodeCursor;
import y.base.NodeList;
import y.geom.AffineLine;
import y.geom.YPoint;
import y.geom.YRectangle;
import y.geom.YVector;
import y.io.IOHandler;
import y.option.OptionHandler;
import y.util.D;
import y.util.DataProviderAdapter;
import y.util.GraphHider;
import y.view.Drawable;

import y.view.Graph2D;
//import y.view.Graph2DView;
import y.view.Arrow;
import y.view.BackgroundRenderer;
import y.view.DefaultBackgroundRenderer;
import y.view.DefaultGraph2DRenderer;
import y.view.DefaultLabelConfiguration;
import y.view.EdgeLabel;
import y.view.Graph2DRenderer;
//import y.view.Graph2DSelectionEvent;
import y.view.Graph2DCanvas;
import y.view.Graph2DView;
import y.view.Graph2DViewRepaintManager;
import y.view.NodeLabel;
import y.view.PopupMode;
import y.view.Port;
import y.view.PortAssignmentMoveSelectionMode;
import y.view.Selections;
import y.view.ShapeNodeRealizer;
import y.view.ViewAnimationFactory;
import y.view.ViewMode;
import y.view.YLabel;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

//Added by Shumon and Ashish, June 22, 2010
//import y.view.Graph2DListener;
//import y.view.Graph2DEvent;
import y.base.GraphListener;
import y.base.GraphEvent;
//import y.base.Graph;
import y.base.Edge;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;
import java.io.*;

//list stuff
//import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID; //this is for the universal unique ids

//screenshot stuff
//added by ashish and shumon
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.multi.MultiSplitPaneUI;
import javax.imageio.*;

import y.layout.random.RandomLayouter;
import y.layout.router.OrthogonalEdgeRouter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import y.anim.AnimationPlayer;


//added to implement mouse listener
import y.view.AbstractMouseInputEditor;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.Mouse2DEvent;
import y.view.MouseInputEditor;
import y.view.NodeRealizer;
import y.view.MouseInputEditorProvider;
import y.view.hierarchy.DefaultNodeChangePropagator;
import y.view.hierarchy.GroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;


import y.util.DataProviderAdapter;

//uml node realizer
/**
 * <p>
 * Demonstrates how to create a node property editor for nodes.
 * This demo makes use of the "value-undefined" state of option items.
 * <p>
 * A node property editor can either be diplayed for a single node
 * by double-clicking on the node or for multiple nodes by first
 * selecting the nodes and then clicking on the "Edit Node Properties" 
 * toolbar button.
 * <p>
 * The property editor will be initialized by the current settings
 * of the selected nodes. If the value of a specific property differs for two
 * selected nodes the editor will display the value as undefined. 
 * Upon closing the editor dialog, only well-defined values will be 
 * commited to the selected nodes.
 * </p>
 */

/**
 * THIS IS ALL BUBBLE STUFF BELOW
 * A simple YLabel.Painter implementation that reuses most of the default painting behavior from
 * DefaultLabelConfiguration and just changes the way the background is painted.
 */
final class MyPainter extends DefaultLabelConfiguration {
  /** Overwrite the painting of the background only. */
  public void paintBox(YLabel label, Graphics2D gfx, double x, double y, double width, double height) {

    // calculate the bubble
    Shape shape = new RoundRectangle2D.Double(x, y, width, height, Math.min(width / 3, 10), Math.min(height / 3, 10));

    double cx = x + width * 0.5d;
    double cy = y + height * 0.5d;

    if (label instanceof NodeLabel) {
      // calculate a wedge connecting the node and the rounded rectangle around the label text
      NodeRealizer labelRealizer = ((NodeLabel) label).getRealizer();
      Node node = ((NodeLabel) label).getNode();
      Graph2D graph2D = ((Graph2D) node.getGraph());
      NodeRealizer nodeRealizer = graph2D.getRealizer(node);

      double tx = graph2D.getCenterX(node);
      double ty = graph2D.getCenterY(node);

      // calculate an offset for the tip of the wedge
      if(!nodeRealizer.contains(cx, cy)) {
        double dirX = cx - labelRealizer.getCenterX();
        double dirY = cy - labelRealizer.getCenterY();
        Point2D result = new Point2D.Double();
        nodeRealizer.findIntersection(tx, ty, cx, cy, result);
        double l0 = Math.sqrt(dirX * dirX + dirY * dirY);
        if(l0 > 0) {
          double halfNodeWidth = nodeRealizer.getWidth() * 0.5 + 5;
          halfNodeWidth = (dirX > 0) ? halfNodeWidth : -1.0 * halfNodeWidth;
          tx = result.getX() + 5 * dirX / l0;
          ty = result.getY() + 5 * dirY / l0;
        }
      }

      // add the wedge to the bubble shape
      double dx = cx - tx;
      double dy = cy - ty;
      double l = Math.sqrt(dx * dx + dy * dy);
      if (l > 0) {
        double size = Math.min(width, height) * 0.25;
        GeneralPath p = new GeneralPath();
        p.moveTo((float) tx, (float) ty);
        p.lineTo((float) (cx + dy * size / l), (float) (cy - dx * size / l));
        p.lineTo((float) (cx - dy * size / l), (float) (cy + dx * size / l));
        p.closePath();
        Area area = new Area(shape);
        area.add(new Area(p));
        shape = area;
      }

    } else if (label instanceof EdgeLabel) {
      // calculate an anchor line connecting the edge and the rounded rectangle around the label text
      Edge edge = ((EdgeLabel) label).getEdge();
      Graph2D graph2D = ((Graph2D) edge.getGraph());
      EdgeRealizer edgeRealizer = graph2D.getRealizer(edge);
      GeneralPath path = edgeRealizer.getPath();
      double[] result = PointPathProjector.calculateClosestPathPoint(path, cx, cy);
      double dx = cx - result[0];
      double dy = cy - result[1];
      double l = Math.sqrt(dx * dx + dy * dy);

      // draw the anchor line with an offset to the edge
      if (l > 0) {
        double tx = result[0] + 5 * dx / l;
        double ty = result[1] + 5 * dy / l;
        Line2D line = new Line2D.Double(cx, cy, tx, ty);
        gfx.setColor(new Color(0, 0, 0, 64));
        gfx.draw(line);
      }
    }

    // paint the bubble using the colors of the label
    Color backgroundColor = label.getBackgroundColor();
    if (backgroundColor != null) {
      // shadow
      gfx.setColor(new Color(0, 0, 0, 64));
      gfx.translate(5, 5);
      gfx.fill(shape);
      gfx.translate(-5, -5);
      // and background
      gfx.setColor(backgroundColor);
      gfx.fill(shape);
    }

    // line
    Color lineColor = label.getLineColor();
    if (lineColor != null) {
      gfx.setColor(lineColor);
      gfx.draw(shape);
    }
  }

}

/** Helper class that provides diverse services related to working with points on a path. */
class PointPathProjector {
  private PointPathProjector() {
  }

  /**
   * Calculates the point on the path which is closest to the given point. Ties are broken arbitrarily.
   *
   * @param path where to look for the closest point
   * @param px   x coordinate of query point
   * @param py   y coordinate of query point
   * @return double[6] <ul> <li>x coordinate of the closest point</li> <li>y coordinate of the closest point</li>
   *         <li>distance of the closest point to given point</li> <li>index of the segment of the path including the
   *         closest point (as a double starting with 0.0, segments are computed with a path iterator with flatness
   *         1.0)</li> <li>ratio of closest point on the the including segment (between 0.0 and 1.0)</li> <li>ratio of
   *         closest point on the entire path (between 0.0 and 1.0)</li> </ul>
   */
  static double[] calculateClosestPathPoint(GeneralPath path, double px, double py) {
    double[] result = new double[6];
    YPoint point = new YPoint(px, py);
    double pathLength = 0;

    CustomPathIterator pi = new CustomPathIterator(path, 1.0);
    double[] curSeg = new double[4];
    double minDist;
    if (pi.ok()) {
      curSeg = pi.segment();
      minDist = YPoint.distance(px, py, curSeg[0], curSeg[1]);
      result[0] = curSeg[0];
      result[1] = curSeg[1];
      result[2] = minDist;
      result[3] = 0.0;
      result[4] = 0.0;
      result[5] = 0.0;
    } else {
      // no points in GeneralPath: should not happen in this context
      throw new IllegalStateException("path without any coordinates");
    }

    int segmentIndex = 0;
    double lastPathLength = 0.0;
    do {
      YPoint segmentStart = new YPoint(curSeg[0], curSeg[1]);
      YPoint segmentEnd = new YPoint(curSeg[2], curSeg[3]);
      YVector segmentDirection = new YVector(segmentEnd, segmentStart);
      double segmentLength = segmentDirection.length();
      pathLength += segmentLength;
      segmentDirection.norm();

      AffineLine currentSegment = new AffineLine(segmentStart, segmentDirection);
      AffineLine throughPoint = new AffineLine(point, YVector.orthoNormal(segmentDirection));
      YPoint crossing = AffineLine.getCrossing(currentSegment, throughPoint);
      YVector crossingVector = new YVector(crossing, segmentStart);

      YVector segmentVector = new YVector(segmentEnd, segmentStart);
      double indexEnd = YVector.scalarProduct(segmentVector, segmentDirection);
      double indexCrossing = YVector.scalarProduct(crossingVector, segmentDirection);

      double dist;
      double segmentRatio;
      YPoint nearestOnSegment;
      if (indexCrossing <= 0.0) {
        dist = YPoint.distance(point, segmentStart);
        nearestOnSegment = segmentStart;
        segmentRatio = 0.0;
      } else if (indexCrossing >= indexEnd) {
        dist = YPoint.distance(point, segmentEnd);
        nearestOnSegment = segmentEnd;
        segmentRatio = 1.0;
      } else {
        dist = YPoint.distance(point, crossing);
        nearestOnSegment = crossing;
        segmentRatio = indexCrossing / indexEnd;
      }

      if (dist < minDist) {
        minDist = dist;
        result[0] = nearestOnSegment.getX();
        result[1] = nearestOnSegment.getY();
        result[2] = minDist;
        result[3] = segmentIndex;
        result[4] = segmentRatio;
        result[5] = segmentLength * segmentRatio + lastPathLength;
      }

      segmentIndex++;
      lastPathLength = pathLength;
      pi.next();
    } while (pi.ok());

    if (pathLength > 0) {
      result[5] = result[5] / pathLength;
    } else {
      result[5] = 0.0;
    }
    return result;
  }

  /** Helper class used by PointPathProjector. */
  static class CustomPathIterator {
    private double[] cachedSegment;
    private boolean moreToGet;
    private PathIterator pathIterator;

    public CustomPathIterator(GeneralPath path, double flatness) {
      // copy the path, thus the original may safely change during iteration
      pathIterator = (new GeneralPath(path)).getPathIterator(null, flatness);
      cachedSegment = new double[4];
      getFirstSegment();
    }

    public boolean ok() {
      return moreToGet;
    }

    public final double[] segment() {
      if (moreToGet) {
        return cachedSegment;
      } else {
        return null;
      }
    }

    public void next() {
      if (!pathIterator.isDone()) {
        float[] curSeg = new float[2];
        cachedSegment[0] = cachedSegment[2];
        cachedSegment[1] = cachedSegment[3];
        pathIterator.currentSegment(curSeg);
        cachedSegment[2] = curSeg[0];
        cachedSegment[3] = curSeg[1];
        pathIterator.next();
      } else {
        moreToGet = false;
      }
    }

    private void getFirstSegment() {
      float[] curSeg = new float[2];
      if (!pathIterator.isDone()) {
        pathIterator.currentSegment(curSeg);
        cachedSegment[0] = curSeg[0];
        cachedSegment[1] = curSeg[1];
        pathIterator.next();
        moreToGet = true;
      } else {
        moreToGet = false;
      }
      if (!pathIterator.isDone()) {
        pathIterator.currentSegment(curSeg);
        cachedSegment[2] = curSeg[0];
        cachedSegment[3] = curSeg[1];
        pathIterator.next();
        moreToGet = true;
      } else {
        moreToGet = false;
      }
    }
  }
}
/*
 * BUBBLE STUFF ENDS HERE
 */





/*this is the base class for mynode and myedge.
*it also implements static methods to find deleted, new and in-use objects
*/
class DarlsUtil
{
	public static String[] getSourceAndTargetLabelText(Edge e, Graph2D graph)
		{
			String[] ret = new String[2];		  
			Node node = e.source();
			
			NodeRealizer node_realizer = graph.getRealizer(node);
			String str_source_node_label_text = node_realizer.getLabelText();
			//now target
			node = e.target();
			node_realizer = graph.getRealizer(node);
			String str_target_node_label_text = node_realizer.getLabelText();
			
			ret[0]=str_source_node_label_text;
			ret[1]=str_target_node_label_text;
			return ret;		
			
		}
}	
class VObject
{
	public UUID uuid;	
	//L[0] - stores those are both in new and old version
	//L[1] - stores those are only in the new version
	//L[2] - stores those are only in the old version
	static List<UUID>[] find_uuid(List v1, List v2)
	{
		List<UUID>[] L = new ArrayList[3];
		L[0]=new ArrayList<UUID>();
		L[1]=new ArrayList<UUID>();
		L[2]=new ArrayList<UUID>();
		
		/*System.out.print("V2 before comparing:");
		for (int i = 0; i<v2.size();i++)
			{
			System.out.print(((VObject) v2.get(i)).vid);
			System.out.print(",");
			}
		System.out.print("V1 before comparing:");
		for (int i = 0; i<v1.size();i++)
			{System.out.print(((VObject) v1.get(i)).vid);
			System.out.print(",");
			}
		System.out.print("\nend\n");
		 */
		
		for (int i = 0; i < v2.size(); i++)
		{	
			Boolean found_v2i = false;
			VObject vobj2 = (VObject)v2.get(i);
			UUID uuid2 = vobj2.uuid;
			for (int j = 0; j < v1.size(); j++)
			{
				VObject vobj1 = (VObject)v1.get(j);
				UUID uuid1 = vobj1.uuid;
				if ( uuid2.equals(uuid1))
				{
					L[0].add(uuid2);
					found_v2i = true;					
				}
			}
			if (!found_v2i)
				L[1].add(uuid2);
		}

		for (int i = 0; i < v1.size(); i++)		
		{	
			Boolean found_v1i = false;
			for (int j = 0; j < v2.size(); j++)
			{
				if ( ((VObject) v1.get(i)).uuid.equals(((VObject) v2.get(j)).uuid))
					found_v1i = true;
			}
			if (!found_v1i)
				L[2].add(((VObject) v1.get(i)).uuid);
		}
		return L;
	}
	
	static List[] find_vobject(List v1, List v2)
	{
		List[] L = new List[3];
		L[0]=new ArrayList();
		L[1]=new ArrayList();
		L[2]=new ArrayList();
		

		for (int i = 0; i < v2.size(); i++)
		{	
			Boolean found_v2i = false;
			VObject vobj2 = (VObject)v2.get(i);
			UUID uuid2 = vobj2.uuid;
			for (int j = 0; j < v1.size(); j++)
			{
				VObject vobj1 = (VObject)v1.get(j);
				UUID uuid1 = vobj1.uuid;
				if ( uuid2.equals(uuid1))
				{
					L[0].add(vobj2);
					found_v2i = true;					
				}
			}
			if (!found_v2i)
				L[1].add(vobj2);
		}

		for (int i = 0; i < v1.size(); i++)		
		{	
			Boolean found_v1i = false;
			for (int j = 0; j < v2.size(); j++)
			{
				if ( ((VObject) v1.get(i)).uuid.equals(((VObject) v2.get(j)).uuid))
					found_v1i = true;
			}
			if (!found_v1i)
				L[2].add(v1.get(i));
		}
		return L;
	}
	//this probably doesnt even work
	/*static List[] find_object(List v1, List v2)
	{
		List[] L = new List[3];
		L[0]=new ArrayList();
		L[1]=new ArrayList();
		L[2]=new ArrayList();
		

		for (int i = 0; i < v2.size(); i++)
		{	
			Boolean found_v2i = false;
			VObject vobj2 = (VObject)v2.get(i);
			UUID uuid2 = vobj2.uuid;
			for (int j = 0; j < v1.size(); j++)
			{
				VObject vobj1 = (VObject)v1.get(j);
				UUID uuid1 = vobj1.uuid;
				if ( uuid2.equals(uuid1))
				{
					if (VNode.class.isInstance(vobj2))
						L[0].add(((VNode)(vobj2)).node);
					else if (VEdge.class.isInstance(vobj2))
						L[0].add(((VEdge)(vobj2)).edge);
					else 
						System.out.println("Warning! Unexpected type!");
						
					found_v2i = true;					
				}
			}
			if (!found_v2i)
			{
				if (VNode.class.isInstance(vobj2))
					L[1].add(((VNode)(vobj2)).node);
				else if (VEdge.class.isInstance(vobj2))
					L[1].add(((VEdge)(vobj2)).edge);
				else 
					System.out.println("Warning! Unexpected type!");
			}
		}

		for (int i = 0; i < v1.size(); i++)		
		{	
			Boolean found_v1i = false;
			for (int j = 0; j < v2.size(); j++)
			{
				if ( ((VObject) v1.get(i)).uuid.equals(((VObject) v2.get(j)).uuid))
					found_v1i = true;
			}
			if (!found_v1i)
			{
				if (VNode.class.isInstance(v1.get(i)))
					L[2].add(((VNode)(v1.get(i))).node);
				else if (VEdge.class.isInstance(v1.get(i)))
					L[2].add(((VEdge)(v1.get(i))).edge);
				else 
					System.out.println("Warning! Unexpected type!");				
			}
		}
		return L;
	}*/
}
class VNode extends VObject
{
	static int count = 0;
	public Node node; 
	public VNode(Node n)
	{
		node=n;
		uuid = UUID.randomUUID();
		count++;
	}
	public VNode(Node n, UUID _vid)
	{
		node=n;
		this.uuid = _vid;	
		
	}
	public static Node getNodeFromNodeList(UUID vid,List nodelist)
	  {
		  Node n = null; 
		  for (int i = 0; i < nodelist.size();i++)
		   {
			   VNode vn = (VNode)nodelist.get(i);
			   if (vid.equals(vn.uuid))
			   {
				   n = vn.node;
				   break;
			   }
		   }
		  return n;
	  }
	public static Node[] getNodesFromVid(UUID vid, List vnl_1, List vnl_2)
	  {
		   Node n1 = null;
		   Node n2 = null;
		   
		   n1 = getNodeFromNodeList(vid,vnl_1);
		   n2 = getNodeFromNodeList(vid,vnl_2);
		   
		   Node [] na = {n1,n2}; 
		   return na;

	  }
	public static UUID getNodeVid(Node n, List list)
	  {
		  for (int i = 0; i < list.size();i++)
		  {
			  VNode vn = (VNode) list.get(i);
			  
			  if (n == vn.node )
			  {
				return vn.uuid;  
			  }
		  }
		  return null;
		  
	  }
	static public boolean node_vid_exists(UUID vid, List<VNode> vnode_list)
	{
		for (int i = 0; i < vnode_list.size(); i++)
			if (vid.equals(((VNode)vnode_list.get(i)).uuid))
				return true;
		return false;
	}
	
	static public boolean node_exists(Node n, List<VNode> vnode_list)
	{
		for (int i = 0; i < vnode_list.size(); i++)
			if (n ==((VNode)vnode_list.get(i)).node)
				return true;
		return false;
	}
	
	/*static List[] find_(List v1, List v2)
	{
		List[] L = new List[3];
		L[0]=new ArrayList();
		L[1]=new ArrayList();
		L[2]=new ArrayList();
		

		for (int i = 0; i < v2.size(); i++)
		{	
			Boolean found_v2i = false;
			VObject vobj2 = (VObject)v2.get(i);
			UUID uuid2 = vobj2.uuid;
			for (int j = 0; j < v1.size(); j++)
			{
				VObject vobj1 = (VObject)v1.get(j);
				UUID uuid1 = vobj1.uuid;
				if ( uuid2.equals(uuid1))
				{
					L[0].add(vobj2);
					found_v2i = true;					
				}
			}
			if (!found_v2i)
				L[1].add(vobj2);
		}

		for (int i = 0; i < v1.size(); i++)		
		{	
			Boolean found_v1i = false;
			for (int j = 0; j < v2.size(); j++)
			{
				if ( ((VObject) v1.get(i)).uuid.equals(((VObject) v2.get(j)).uuid))
					found_v1i = true;
			}
			if (!found_v1i)
				L[2].add(v1.get(i));
		}
		return L;
	}*/

}

class VEdge extends VObject
{
	static int count = 0;
	public Edge edge; 
	public VEdge(Edge e)
	{
		edge=e;
		uuid = UUID.randomUUID();
		count++;
	}
	public VEdge(Edge e, UUID _vid)
	{
		edge=e;
		this.uuid = _vid;		
	}
	public static Edge getEdgeFromEdgeList(UUID vid,List edgelist)
	  {
		  Edge e = null; 
		  for (int i = 0; i < edgelist.size();i++)
		   {
			   VEdge ve = (VEdge)edgelist.get(i);
			   if (vid.equals(ve.uuid))
			   {
				   e = ve.edge;
				   break;
			   }
		   }
		  return e;
	  }
	public static Edge[] getEdgesFromVid(UUID vid, List vel_1, List vel_2)
	  {
		   Edge e1 = null;
		   Edge e2 = null;
		   
		   e1 = getEdgeFromEdgeList(vid,vel_1);
		   e2 = getEdgeFromEdgeList(vid,vel_2);
		   
		   Edge [] ea = {e1,e2}; 
		   return ea;
		  
	  }
	public static UUID getEdgeVid(Edge n, List list)
	  {
		  for (int i = 0; i < list.size();i++)
		  {
			  VEdge ve = (VEdge) list.get(i);
			  
			  if (n == ve.edge )
			  {
				return ve.uuid;  
			  }
		  }
		  return null;
		  
	  }
	static public boolean edge_vid_exists(UUID vid, List<VEdge> vedge_list)
	{
		for (int i = 0; i < vedge_list.size(); i++)
			if (vid.equals(((VEdge)vedge_list.get(i)).uuid))
				return true;
		return false;
	}
	
	static public boolean edge_exists(Edge e, List<VEdge> vedge_list)
	{
		for (int i = 0; i < vedge_list.size(); i++)
			if (e ==((VEdge)vedge_list.get(i)).edge)
				return true;
		return false;
	}

}

class Darls extends DarlsBase 
{

  private HierarchicLayoutModule layoutModule;

  //a new menu for settings
  JMenu settingMenu;
  JMenuItem menuItem;
  //animationMenu.addActionListener(new MenuListener());
  private GraphEvent lastGraphEvent = null; //this is used when drawing selection rectangle, see drawSelectdionRectangle(Rectangle sel_rect)

  private static final long PREFERRED_DURATION = 2000;
  private static final long DEFAULT_DURATION = 5000;
  private static final long REPETITION_DURATION = 3000;
  private static final long TRAVERSE_NEW_EDGE_DURATION = 1000;
  NodePropertyEditorAction nodePropertyEditorAction;
  

  //public static int screenshot_re = 0;
  
  List<Graph2D> graphs = null;
  List v_nodes = null;
  List v_edges = null;
  
  private JButton b_forward;
  private JButton b_backward;
  JPanel panel_button;
  
  private JButton b_load_comic_left;
  private JButton b_load_comic_right;
  JPanel panel_comic;
  JPanel panel_views;
  JPanel split_panel;
  JPanel experiment_panel;	
  private JButton button_comparison;
  
  private JButton clear_selection_button;
  private JButton submit_button;
  private JButton see_previous_version_button;
  
  
  final int n_comics = 10;

  //for loading graphs from comic views to main views
  int first_graph = 0;
  int selected_comic_view_index=-1;
  
  public Graph2DView[] comic_view=new Graph2DView[n_comics];
  public JSplitPane[] comic_pane=new JSplitPane[n_comics-1];
  
 // public JPopupMenu popup;
  
  //Versioning lists for nodes
  List<VNode> v_node_list_1;
  List<VNode> v_node_list_2;
  //Versioning lists for edges
  List<VEdge> v_edge_list_1;
  List<VEdge> v_edge_list_2;
  
    
  GraphListener graphListener = null;
  
 
  final String str_comic_empty = "Empty";
  String str_right_view_text = "Version B";
  String str_left_view_text = "Version A";
  
  CompareAction compare_action = null; 
  BubbleLabels bubble_labels = new BubbleLabels(); 

  ViewMouseMotionListener viewMotionMouseListener = null;
  private void init_comic_strip()
  {
	Graph2D g = new Graph2D();
	
	for(int i=0;i<n_comics;i++)
	{
		Graph2DView gv = new Graph2DView(g);
		MyBackgroundRenderer.newInstance(gv, Color.gray, false).setText(str_comic_empty);
		comic_view[i] =  gv;
		
		//comic_view[i].fitContent();		
		//comic_view[i].updateView();
	}	  
  }
  public Darls() {
      
	//Added by Shumon...........
	v_node_list_1 = new ArrayList<VNode>();
	v_node_list_2 = new ArrayList<VNode>();
	
	v_edge_list_1 = new ArrayList<VEdge>();
	v_edge_list_2 = new ArrayList<VEdge>();
	

	//.........................///////////
	URL imageURL_forward = ClassLoader.getSystemResource("versioning/resource/forward.png");
	ImageIcon for_icon=new ImageIcon(imageURL_forward);
	URL imageURL_backword = ClassLoader.getSystemResource("versioning/resource/backward.png");
	ImageIcon back_icon=new ImageIcon(imageURL_backword);
	b_forward=new JButton(for_icon);
	b_backward=new JButton(back_icon);
	b_forward.setActionCommand("b_forward");
	b_backward.setActionCommand("b_backward");
	
	ButtonListener bl = new ButtonListener();
	b_forward.addActionListener(bl);
	b_backward.addActionListener(bl);
	
	//buttons for loading comic graph in views
	URL imageURL_load_right = ClassLoader.getSystemResource("versioning/resource/load_right.jpg");
	ImageIcon load_right_icon=new ImageIcon(imageURL_load_right);
	URL imageURL_load_left = ClassLoader.getSystemResource("versioning/resource/load_left.jpg");
	ImageIcon load_left_icon=new ImageIcon(imageURL_load_left);
	b_load_comic_left=new JButton(load_left_icon);
	b_load_comic_right=new JButton(load_right_icon);
	b_load_comic_left.setActionCommand("load_left_view");
	b_load_comic_right.setActionCommand("load_right_view");
	b_load_comic_left.addActionListener(bl);
	b_load_comic_right.addActionListener(bl);
	URL imageURL_comparison = ClassLoader.getSystemResource("versioning/resource/comparison.jpg");
	ImageIcon compare_icon=new ImageIcon(imageURL_comparison);
	button_comparison = new JButton(compare_icon);
	compare_action = new CompareAction(this);
	button_comparison.addActionListener(compare_action);
	//panel_comic=new JPanel();
	//panel_comic.add(b_load_comic_left);
	//panel_comic.add(b_load_comic_right);
	
	////
	init_comic_strip();
		
	experiment_panel = new JPanel();
	submit_button = new JButton("Submit");
	submit_button.setVisible(false); //we are using keyboard now
	submit_button.setActionCommand("submit_results");
	submit_button.addActionListener(bl);
	submit_button.addMouseListener(new MouseListener()
	{

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("SUBMIT_BUTTON","MOUSE_PRESSED",e);

			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("SUBMIT_BUTTON","MOUSE_RELEASED",e);

			}
		}
		
	});
	
	experiment_panel.add(submit_button);
	
	clear_selection_button = new JButton("Clear");
	clear_selection_button.setActionCommand("clear_exp_selection");
	clear_selection_button.addActionListener(bl);
	clear_selection_button.addMouseListener(new MouseListener()
	{

		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("CLEAR_SELECTION_BUTTON","MOUSE_PRESSED",arg0);

			}
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("CLEAR_SELECTION_BUTTON","MOUSE_RELEASED",arg0);
			}
		}
	});
	//for experiment logging only, the following listener was added
	
	experiment_panel.add(clear_selection_button);
	
	see_previous_version_button = new JButton("Previous");
	see_previous_version_button.setVisible(false); // we are using keyboards now
	see_previous_version_button.setActionCommand("see_prev_version");
	
	see_previous_version_button.addMouseListener(new MouseListener()
	{
		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			try
			{
				if (user_study !=null && user_study.userStudyLevel1Log !=null)
				{
					user_study.userStudyLevel1Log.log("PREVIOUS_BUTTON","MOUSE_PRESSED",arg0);

				}
				user_study.onSeePreviousPress();
			}
			catch (Exception ex) 
			{
				System.out.println(ex+" user_study object is a null!");
			}
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			try
			{
				if (user_study !=null && user_study.userStudyLevel1Log !=null)
				{
					user_study.userStudyLevel1Log.log("PREVIOUS_BUTTON","MOUSE_RELEASED",arg0);

				}
				user_study.onSeePreviousRelease();
			}
			catch (Exception ex) 
			{
				System.out.println(ex+" user_study object is a null!");
			}

		}
	});
	experiment_panel.add(see_previous_version_button);
	
 	 //first we will put an empty graph in the version view
	 Graph2D graph = new Graph2D(); 
	 left_view = new Graph2DView(graph);
	 
    
     split_panel=new JPanel();
     split_panel.setLayout(new GridLayout(1,n_comics,5,2));
     for(int i=0;i<n_comics;i++)
     {
    	 split_panel.add(comic_view[i]);
     }
     
     //for setting the grid layout of two views
     panel_views=new JPanel();
     panel_views.setLayout(new GridLayout(1,2,5,2));
     panel_views.add(left_view);
     panel_views.add(view);

     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     int width=screenSize.width;
     //JSplitPane comic_version_pane = new JSplitPane(
     //        JSplitPane.VERTICAL_SPLIT, panel_comic,toggle_panel);
     //JSplitPane toggle_version_pane = new JSplitPane(
       //      JSplitPane.VERTICAL_SPLIT, toggle_panel,panel_views);
     //lockSplitPaneDivider(comic_version_pane);
     JSplitPane panelPane1 = new JSplitPane(
             JSplitPane.HORIZONTAL_SPLIT, b_backward, split_panel);
     panelPane1.setDividerLocation(20);

     jtb.setFloatable(true);
     jtb.setRollover(true);
     for (int i = 0; i < n_comics; i++)
     {
    	 comic_view[i].setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    	 comic_view[i].setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
     }

     JSplitPane panelPane2 = new JSplitPane(
             JSplitPane.HORIZONTAL_SPLIT, panelPane1, b_forward);
     panelPane2.setDividerLocation(1880);
     JSplitPane panel_version = new JSplitPane(
             JSplitPane.VERTICAL_SPLIT, panelPane2, panel_views);
     lockSplitPaneDivider(panel_version);
     panel_version.setDividerLocation(150);    //this for the height of the panel which contains the views of the versions
     JPanel tool_panel =  new JPanel();
     tool_panel.setLayout(new GridLayout(1,3));
     tool_panel.add(jtb);
     JPanel mid_panel = new JPanel();
     mid_panel.add(b_load_comic_left);
     mid_panel.add(button_comparison);
     mid_panel.add(b_load_comic_right);
     tool_panel.add(mid_panel);
     tool_panel.add(experiment_panel);
     JSplitPane toolbarPane = new JSplitPane(
             JSplitPane.VERTICAL_SPLIT, tool_panel, panel_version);
     lockSplitPaneDivider(toolbarPane);
     contentPane.add(toolbarPane);
    
    MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(str_right_view_text);
    MyBackgroundRenderer.newInstance(left_view, Color.WHITE, true).setText(str_left_view_text);
    
    //register mouse and mouse motion listener that we will use for that version selection thing.
    viewMotionMouseListener=new ViewMouseMotionListener();
    view.getCanvasComponent().addMouseListener(viewMotionMouseListener);
    view.getCanvasComponent().addMouseMotionListener(viewMotionMouseListener);
    view.getCanvasComponent().addKeyListener(viewMotionMouseListener);
    
   
    
    setBottomViewsPolicy();
    
    registerComicViewEditModes();

	//open property editor upon double-clicking on a node
    view.addViewMode(new ViewMode() {
      public void mouseClicked(MouseEvent ev) {
        if(ev.getClickCount() == 2) {
          Node v = getHitInfo(ev).getHitNode();
          if(v != null) {
            nodePropertyEditorAction.actionPerformed(null);
          }
        }
      }
    });
    /*view.getGraph2D().addGraph2DListener(new Graph2DListener(){
        public void onGraph2DEvent(Graph2DEvent e) {
      	  System.out.println("Graph2D Event="+e);
        }
      });*/  
    view.getGraph2D().addGraphListener(graphListener = new GraphListener(){
    	private void updateViewsOnGraphListenerEvent() 
		{
    		//we need to check for null so that it doesn't crash the first time
    		if(LeftBackGroundDrawer != null) 
	    	{
    			LeftBackGroundDrawer.clear();
    			LeftBackGroundDrawer.draw(overlay_graph_in_left_view);
    			view.updateView();
    			left_view.updateView();
	    	}
		}
    	public void onGraphEvent(GraphEvent e)
    	{
    		/*
    		 *if mouse there is a graph event, we don't want to draw
    		 *the background selection rectangle
    		 *so we memorize the last graph event 
    		 */
    		lastGraphEvent = e;
    		    		

    		bubble_labels.clear();
        	if(GraphEvent.EDGE_CREATION == e.getType())
        	{
        		Edge edge = (Edge) e.getData();
    			VEdge vedge = new VEdge(edge);
    			v_edge_list_2.add(vedge);
    			/*System.out.println("Edges after insertion:");
    			for (int i = 0; i <v_edge_list_2.size(); i++) 
    				System.out.println(((VEdge)v_edge_list_2.get(i)).uuid);*/
        	}
        	
        	else if(GraphEvent.PRE_EDGE_REMOVAL == e.getType())
        	{
        		System.out.println("PRE_EDGE");
    			Edge edge = (Edge) e.getData();
    			System.out.println("Considering to delete edge:"+edge);
    			//System.out.println("Edges After deletion:");
    			for (int i = 0; i <v_edge_list_2.size(); i++)
    			{	
    				if (((VEdge) v_edge_list_2.get(i)).edge == edge)
    				{
    					v_edge_list_2.remove(i);
    					//System.out.println("Deleted edge:"+i+"="+v_edge_list_2.get(i).vid+" , "+edge);
    				}
    			
    			}
    			/*for (int i = 0; i <v_edge_list_2.size(); i++) 
    				System.out.println(((VEdge)v_edge_list_2.get(i)).vid);*/
    			

    		}
        	
        	else if(GraphEvent.NODE_CREATION == e.getType())
    		{

    			Node n = (Node) e.getData();
    		    NodeRealizer nr = view.getGraph2D().getRealizer(n);
    		    
    		    
    		    
    			VNode m = new VNode(n);
    			v_node_list_2.add(m);
    			//nr.setLabelText(m.vid+""); //set label to the same value as vid
    			nr.setLabelText(v_node_list_2.size()-1+""); //set label to value equal the number of nodes in the graph 
    			
    			
    			/*System.out.println("Nodes After insertion:");
    			for (int i = 0; i <v_node_list_2.size(); i++)
    			{
    				UUID vid = ((VNode)v_node_list_2.get(i)).uuid;    				
    				System.out.println(vid);
    			}*/
    		    
    			for(int i=0;i<=4;i++)
    			{
    				comic_view[i].fitContent();
    				comic_view[i].updateView();
    			}
    			
    		    //Rectangle r1 = left_view.getBounds();
    		    //Rectangle r2 = view.getBounds(); 
    		    
    			updateViewsOnGraphListenerEvent();
    			
    		}
        	else if (GraphEvent.POST_NODE_REMOVAL == e.getType())
        	{
        		//we need to update left view after deleting the node
        		//it can't be updated in pre_node_removal
        		updateViewsOnGraphListenerEvent();
        	}
        	
    		else if(GraphEvent.PRE_NODE_REMOVAL == e.getType())
    		{
    			Node n = (Node) e.getData();
    			System.out.println("PRE_NODE");
    			System.out.println("Considering to delete node:"+n);
    			
    			System.out.println("After node deletion:");
    			updateVersionList(n);
     		}
    	}
    });
    
    layoutModule = new HierarchicLayoutModule();
    
	

  }
  
  protected void updateVersionList(Node n) //for a node
  {
	  for (EdgeCursor ec = n.edges(); ec.ok();ec.next())
		{
			Edge e = ec.edge();
			for (int i = 0; i < v_edge_list_2.size();i++)
			{
				VEdge vedge = ((VEdge)v_edge_list_2.get(i));
				Edge temp_e = vedge.edge;
				if (e == temp_e)
					v_edge_list_2.remove(i);				
			}
			
		}
		//remove the corresponding vnode as well.
		for (int i = 0; i < v_node_list_2.size();i++)
		{
			VNode vnode = ((VNode)v_node_list_2.get(i));
			Node temp_n = vnode.node;
			if (n == temp_n)
				v_node_list_2.remove(i);				
		}
  }
  

  protected void registerViewModes() {
	    EditMode editMode = new EditMode();
	    editMode.setPopupMode(new MyPopupMode(this));
	    view.addViewMode(editMode);
	     
	    //disabled hierarchical layout inc mode for experiment
	  /*EditMode editMode = rihlm.editMode;
	  //add stuff to this editMode
	  editMode.setPopupMode(new MyPopupMode());
	  view.addViewMode(editMode);*/
}
  ComicPopupMenu[] comic_popup_menus = null;
  private void registerComicViewEditModes()
  {
	  comic_popup_menus = new ComicPopupMenu[comic_view.length];
	  for (int i = 0; i < comic_view.length; i++)
	  {
		  comic_view[i].getGlassPane().add(comic_popup_menus[i] = new ComicPopupMenu(i));
	  }
	  
  }
  class ComicPopupMenu extends PopupMenu
  {
	  ComicPopupMenu(int _view_no)
	  {
		MenuItem left_menu_item = new MenuItem("<--");
		MenuItem right_menu_item = new MenuItem("-->");
		ActionListener left_listener = (new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				loadGraphToLeftView();
			}
		});
		ActionListener right_listener = (new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				loadGraphToRightView();
			}
		});
		
		left_menu_item.addActionListener(left_listener);
		right_menu_item.addActionListener(right_listener);
		
		this.add(left_menu_item);
		this.add(right_menu_item);
		
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		
	  }
  }
  class MyPopupMode extends PopupMode {
	  
	  	Darls darls = null;
	  	MyPopupMode(Darls _darls)
	  	{
	  		darls = _darls;
	  	}
	    public JPopupMenu getEdgePopup(final Edge e) {
		      JPopupMenu pm = new JPopupMenu();            
		      pm.add(new AbstractAction("Reject") {
		        public void actionPerformed(ActionEvent e) {
		        	new Reject(darls);         
		        }
		      });
		      return pm;
		    }  
	   public JPopupMenu getNodePopup(final Node v) {
	      JPopupMenu pm = new JPopupMenu();            
	      pm.add(new AbstractAction("Reject") { 
	        public void actionPerformed(ActionEvent e) {
	        	new Reject(darls);         
	        }
	      });
	      return pm;
	    }
	    
	    public JPopupMenu getSelectionPopup(double x, double y) {
	      JPopupMenu pm = new JPopupMenu();      
	      pm.add(new AbstractAction("Reject") { 
	        public void actionPerformed(ActionEvent e) {
	        	new Reject(darls);
	        }
	      });      
	      return pm;
	    }

	    
	    public JPopupMenu getPaperPopup(double x, double y) {
	      JPopupMenu pm = new JPopupMenu();
	      pm.add(new AbstractAction("Reject") {
	        
	    	 public void actionPerformed(ActionEvent e) {
	    		 new Reject(darls);

	        }
	      });
	      return pm;	    	
	    }
	  }
  public static void lockSplitPaneDivider(JSplitPane sp)
  {
	 Component dividerComponent = getDividerComponent(sp);
 	 MouseListener[] dividerMouseListeners = dividerComponent.getMouseListeners();
 	 dividerComponent.removeMouseListener(dividerMouseListeners[0]);
  }
  public static Component getDividerComponent(JSplitPane sp) {
	  Component[] comps = sp.getComponents();
	  for(int i = 0; i < comps.length; i++) {
		  if(comps[i] instanceof javax.swing.plaf.basic.BasicSplitPaneDivider) {
			  return comps[i];
		  }
		  if(comps[i] == sp.getTopComponent() ||
				  comps[i] == sp.getBottomComponent() ||
				  comps[i] == sp.getLeftComponent() ||
				  comps[i] == sp.getRightComponent()) {
			  continue;
		  } else {
			  return comps[i];
		  }
	  	}
	  return null;
	}
  /**
   * <code>BackgroundRenderer</code> that displays a short text message.
   */
  public static final class MyBackgroundRenderer
          extends DefaultBackgroundRenderer {
    private String text;
    private Color textColor;
    private Color bgColor;
    private final Rectangle r;
    private Rectangle sel_rect = null;
    private Color sel_rect_color = new Color(0,0,0,100);
    private boolean draw_text_on_top = false; //added to distinguish between the comic view and the bottom views for painting text

    MyBackgroundRenderer( final Graph2DView view, Color _bgColor, boolean _draw_text_on_top ) {
      super(view);
      draw_text_on_top = _draw_text_on_top; 
      bgColor = _bgColor;
      textColor = new Color(192, 192, 192);
      r = new Rectangle(0, 0, -1, 1);
    }

    void paintBackground(Graphics2D gfx,Color c)
    {
    	if (c != null) {
            Color oldColor = gfx.getColor();
            

            undoWorldTransform(gfx);

            gfx.setColor(c);
            view.getBounds(r);
            r.setLocation(0, 0);            
            gfx.fillRect(0, 0, r.width,r.height );

            redoWorldTransform(gfx);


            gfx.setColor(oldColor);
          }
    }
    
    void paintBackgroundRegion(Graphics2D gfx)
    {
    	if (sel_rect != null) {
            Color oldColor = gfx.getColor();
            undoWorldTransform(gfx);
            gfx.setColor(sel_rect_color);
                        
            gfx.fillRect(sel_rect.x, sel_rect.y, sel_rect.width,sel_rect.height );
            redoWorldTransform(gfx);


            gfx.setColor(oldColor);
          }
    }
    String getText() {
      return text;
    }

    void setText( final String text ) {
      this.text = text;
    }

    Color getTextColor() {
      return textColor;
    }

    void setTextColor( final Color color ) {
      this.textColor = color;
    }

    public void paint(
            final Graphics2D gfx,
            final int x,
            final int y,
            final int w,
            final int h ) {
      super.paint(gfx, x, y, w, h);
      paintBackground(gfx,bgColor);
      paintBackgroundRegion(gfx);
      paintText(gfx);

    }

    private void paintText( final Graphics2D gfx ) {
      if (text != null && textColor != null) {
        final Color oldColor = gfx.getColor();
        final Font oldFont = gfx.getFont();

        undoWorldTransform(gfx);

        gfx.setColor(textColor);
        gfx.setFont(oldFont.deriveFont(30.0f));

        view.getBounds(r);
        r.setLocation(0, 0);
        final FontMetrics fm = gfx.getFontMetrics();
        final Rectangle2D bnds = fm.getStringBounds(text, gfx);
        float textX,textY;
        if (draw_text_on_top)
        {
        	//draw text on the top
            textX = (float) r.x; 
            textY = (float) r.y+fm.getMaxAscent();        	
        }
        else
        {	//draw text in the middle
	        textX = (float) (r.x + (r.width - bnds.getWidth()) * 0.5);
	        textY = (float) (r.y + (r.height - bnds.getHeight()) * 0.5 + fm.getMaxAscent());
        }
        

        
        gfx.drawString(text, textX, textY);

        redoWorldTransform(gfx);

        gfx.setFont(oldFont);
        gfx.setColor(oldColor);
      }
    }


    static MyBackgroundRenderer newInstance( final Graph2DView view, Color bgColor, boolean text_on_top ) {
      final MyBackgroundRenderer mbr = new MyBackgroundRenderer(view, bgColor, text_on_top);
      view.setBackgroundRenderer(mbr);
      return mbr;
    }
  }
  
  


  protected JToolBar createToolBar() {
    JToolBar bar = super.createToolBar();
    //bar.addSeparator();
    //add node property action to toolbar
    bar.add(nodePropertyEditorAction = new NodePropertyEditorAction());
    bar.addSeparator();
    
    setSelectionModeAction 	= new SetSelectionModeAction();
    setEditModeAction 			= new SetEditModeAction();
    setExperimentModeAction = new SetExperimentModeAction();
    
    setSelectionModeAction.setDependentActions(setEditModeAction,setExperimentModeAction);
    setEditModeAction.setDependentActions(setSelectionModeAction,setExperimentModeAction);
    setExperimentModeAction.setDependentActions(setSelectionModeAction,setEditModeAction);
    
    bar.add(setSelectionModeAction);
    bar.add(setEditModeAction);
    bar.add(setExperimentModeAction);
    
    //initially we are in selection mode - can be changed for experiment, so initially its the experiment mode
    setSelectionModeAction.actionPerformed(null);
    
    /*bar.add(new LayoutAction());
    bar.add(new OptionAction());
    bar.addSeparator();
    bar.add(new ToggleClassDetails());
    bar.addSeparator();
    bar.add(new IncrementalHierarchicLayouterAction());
    //bar.add(new GraphSavingAction());
    //bar.add(new GraphLoadingAction());
    //bar.add(new GraphComparingAction());
    
    //bar.addSeparator();
    //bar.add(new Screenshot());*/
    return bar;
    
  }
  //toggled action is to switch between selection, edit and experiment modes
  //when one of these three actions is selected the button should get disabled, while the remaining should get enabled
  public class AbstractToggledAction extends AbstractAction
  {
		protected AbstractAction dependentAction1 = null;
		protected AbstractAction dependentAction2 = null;
	   	
	   	AbstractToggledAction(String str)
	   	{
	   	 super(str);
	   	}
		
	   	public void setDependentActions(AbstractAction aa1, AbstractAction aa2)
		{
			dependentAction1 = aa1;
			dependentAction2 = aa2;
		}

	   	private void toggle()
	   	{
	   		setEnabled(false);
	   		dependentAction1.setEnabled(true);
	   		dependentAction2.setEnabled(true);
	   	}
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			toggle();
		}
  }
  public class SetSelectionModeAction extends AbstractToggledAction {

	   	public SetSelectionModeAction() {
	      super("Selection Mode");
	      final URL imageURL = ClassLoader.getSystemResource("versioning/resource/selection_mode.png");
	      if (imageURL != null) {
	        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
	      }
	      this.putValue(Action.SHORT_DESCRIPTION, "Set Selection Mode");
	    }

	    public void actionPerformed( final ActionEvent e ) {

	    	super.actionPerformed(e);
	    	setAllowNodeCreation(false);


	    }
  }
  
  public class SetEditModeAction extends AbstractToggledAction {


	    public SetEditModeAction() {
	      super("Edit Mode");
	      final URL imageURL = ClassLoader.getSystemResource("versioning/resource/edit_mode.png");
	      if (imageURL != null) {
	        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
	      }
	      this.putValue(Action.SHORT_DESCRIPTION, "Set Edit Mode");
	    }

	    public void actionPerformed( final ActionEvent e ) {

	    	super.actionPerformed(e);
	    	setAllowNodeCreation(true);
	    }
  }
  
  UserStudyBase user_study = null;
  protected void startExperiment(int participant, int _current_trial, String _study)
  {
	  System.out.println("!!user study!!!");
	      
	  setExperimentModeAction.actionPerformed(null);
	  System.out.println("this="+this);
	  user_study = new UserStudy(this,participant,_current_trial, _study);
	  //view.getComponent().requestFocus();  //This is that enter press can be registered!
  }
  
  public class SetExperimentModeAction extends AbstractToggledAction {

	  //ExpMouseMotionListener expMouseMotionListener = null;
	  class ExpMouseMotionListener extends ViewMouseMotionListener
	  {
		private Graph2DView listeners_view = null;
		
		ExpMouseMotionListener(final Graph2DView v)
		{
			listeners_view = v;
		}
		
		private void getClickedNodesNoSelecting(double x, double y, Graph2D graph, NodeList nl) {
			for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
			{
				Node n = nc.node();
				NodeRealizer nr = graph.getRealizer(n);
				
				if (nr.contains(x,y))
				{
					if (nl != null)
						nl.add(n);			
				}
			}
		}
		private void getClickedEdgesNoSelecting(double x, double y, Graph2D graph, EdgeList el) {
			for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				
				EdgeRealizer er = graph.getRealizer(e);
				er.getPath(); //?????????????????????????????????????????????????????
				if (er.contains(x, y))
				{	
					
					if (el != null)
						el.add(e);
				}
			}
		}
		private void set_objects_selected_on_right_background_and_on_the_left(int xx, int yy)
		{
			double x = view.toWorldCoordX( xx);
			double y = view.toWorldCoordY( yy);
			
			Graph2D right_backGraph = RightBackGroundDrawer.backgroundGraph;
			
			//on the right background
			NodeList nl = new NodeList();
			getClickedNodesNoSelecting(x, y, right_backGraph, nl);
			for (NodeCursor nc = nl.nodes(); nc.ok(); nc.next())
			{
				Node n = nc.node();
				NodeRealizer nr = right_backGraph.getRealizer(n);
				nr.setSelected(!nr.isSelected());				
			}
			
			//on the left graph
			nl = new NodeList();
			getClickedNodesNoSelecting(x, y, left_view.getGraph2D(), nl);
			for (NodeCursor nc = nl.nodes(); nc.ok(); nc.next())
			{
				Node n = nc.node();
				NodeRealizer nr = left_view.getGraph2D().getRealizer(n);
				nr.setSelected(!nr.isSelected());				
			}

			//on the right back ground
			EdgeList el = new EdgeList();
			getClickedEdgesNoSelecting(x, y, right_backGraph, el);			
			for (EdgeCursor ec = el.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				EdgeRealizer er = right_backGraph.getRealizer(e);
				er.setSelected(!er.isSelected());				
			}
			//on the left graph
			el = new EdgeList();
			getClickedEdgesNoSelecting(x, y, left_view.getGraph2D(), el);			
			for (EdgeCursor ec = el.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				EdgeRealizer er = left_view.getGraph2D().getRealizer(e);
				er.setSelected(!er.isSelected());				
			}
			left_view.updateView();				
			view.updateView();
		}
		
	  	private void set_objects_selected(int xx, int yy,boolean select) 
	  	{
			double x = view.toWorldCoordX( xx);
			double y = view.toWorldCoordY( yy);
			
			Graph2D left_backGraph = LeftBackGroundDrawer.backgroundGraph;
			
			//first the ones on the left back
			NodeList nl = new NodeList();
			getClickedNodes(x, y, left_backGraph, nl,select);
			LeftBackGroundDrawer.redraw_selected_missing_nodes(nl,select);
			
			//on the left graph
			nl = new NodeList();
			getClickedNodes(x, y, left_view.getGraph2D(), nl,select);
			
			//first the ones on the left
			EdgeList el = new EdgeList();
			getClickedEdges(x, y, left_backGraph, el,select);		
			LeftBackGroundDrawer.redraw_selected_missing_edges(el,select);
			
			//on the left graph
			el = new EdgeList();
			getClickedEdges(x, y, left_view.getGraph2D(), el,select);
			
			left_view.updateView();				
			view.updateView();
		}
		public void mouseMoved(MouseEvent e)
		{
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("VIEW","MOUSE_MOVED",e);
	
			}

		}
		public void mouseReleased(MouseEvent e) 
		{
			if (was_dragged)
				{
					was_dragged = false; 
				}
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("VIEW","MOUSE_RELEASED",e);


			}
			// we will check if either a node or an edge was selected
	  		// if so, we will take action
	  		

	  		final double x = listeners_view.toWorldCoordX(e.getX());
			final double y = listeners_view.toWorldCoordY(e.getY());
			
	  		Graph2D graph = listeners_view.getGraph2D();
	  		//the oreder matters! first this, then the one by selecting object itself
	  		if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null &&
	  			LeftBackGroundDrawer != null && LeftBackGroundDrawer.backgroundGraph != null)
	  			set_objects_selected_on_right_background_and_on_the_left(e.getX(),e.getY());
	  		for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
	  		{
	  			Node n = nc.node();
	  			NodeRealizer nr = graph.getRealizer(n);
	  			
	  			if (nr.contains(x,y))
	  			{
		  			if (!nr.isSelected())
		  			{
		  				nr.setSelected(true);
		  		  		if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null &&
		  			  			LeftBackGroundDrawer != null && LeftBackGroundDrawer.backgroundGraph != null)
		  		  			set_objects_selected(e.getX(),e.getY(), true); //if it exists
		  				
		  				if (user_study != null && user_study.userStudyLevel2Log != null)
		  					user_study.userStudyLevel2Log.log("selected", n.toString());
		  				
		  			}
		  			else
		  			{
		  				nr.setSelected(false);
		  		  		if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null &&
		  			  			LeftBackGroundDrawer != null && LeftBackGroundDrawer.backgroundGraph != null)
		  		  			set_objects_selected(e.getX(),e.getY(), false); //if it exists
		  				
		  				if (user_study != null && user_study.userStudyLevel2Log != null)
		  					user_study.userStudyLevel2Log.log("deselected", n.toString());
		  				
		  			}
	  			}
	  		}
	  		
	  		for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
	  		{
	  			Edge edge = ec.edge();
	  			
	  			EdgeRealizer er = graph.getRealizer(edge);
	  			er.getPath(); //?????????????????????????????????????????????????????
	  			if (er.contains(x, y))
	  			{	
	  				if (!er.isSelected())
		  			{
		  				er.setSelected(true);
		  		  		if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null &&
		  			  			LeftBackGroundDrawer != null && LeftBackGroundDrawer.backgroundGraph != null)
		  		  			set_objects_selected(e.getX(),e.getY(), true); //if it exists
		  				
		  				if (user_study != null && user_study.userStudyLevel2Log != null)
		  				{
		  					user_study.userStudyLevel2Log.log("selected", edge.toString());
		  					System.out.println("edge selected");
		  					
		  				}
		  				
		  			}
	  				else
	  				{
		  				er.setSelected(false);
		  		  		if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null &&
		  			  			LeftBackGroundDrawer != null && LeftBackGroundDrawer.backgroundGraph != null)
		  		  			set_objects_selected(e.getX(),e.getY(), false); //if it exists
		  				
		  				if (user_study != null && user_study.userStudyLevel2Log != null)
		  				{
		  					user_study.userStudyLevel2Log.log("deselected", edge.toString());
		  					System.out.println("edge deselected");
		  				}
		  				
	  				}
	  			}
	  		}
	  		
	  		listeners_view.updateView();
	  		//System.out.println((listeners_view == view ? "right" : "left"));
		}
		public void mousePressed(MouseEvent e) 
		{
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("VIEW","MOUSE_PRESSED",e);
				

			}
		}
		
		public void mouseDragged(MouseEvent e) 
		{
			if (user_study !=null && user_study.userStudyLevel1Log !=null)
			{
				user_study.userStudyLevel1Log.log("VIEW","MOUSE_DRAGGED",e);
			}
		}

	  	public void mouseClicked(final MouseEvent e) {
	  	
	  	}
	  
	  }
	  
	  public SetExperimentModeAction() {
	      super("Experiment Mode");
	      final URL imageURL = ClassLoader.getSystemResource("versioning/resource/experiment_mode.png");
	      if (imageURL != null) {
	        this.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
	      }
	      this.putValue(Action.SHORT_DESCRIPTION, "Set Experiment Mode");
	      

	    }



		boolean first_time = true;
	    public void actionPerformed( final ActionEvent e ) {

	    	super.actionPerformed(e);
	    	setAllowNodeCreation(false);
	    	
	        if (first_time)
	        {
		    	//----------------
		        //user study stuff
		        final EditMode editMode = new EditMode();
		        left_view.addViewMode(editMode);
		        
		        final ExpMouseMotionListener leftExpMouseMotionListener = new ExpMouseMotionListener(left_view);
		        final ExpMouseMotionListener rightExpMouseMotionListener = new ExpMouseMotionListener(view);
		        
		        setupListeners(left_view,leftExpMouseMotionListener);	        
		        setupListeners(view,rightExpMouseMotionListener);


		    	first_time = false;
	        }
	    }

		private void setupListeners(final Graph2DView v, final ViewMouseMotionListener listener) {
			//first remove listeners. then add them
	        final MouseListener [] ml = v.getCanvasComponent().getMouseListeners();	        
	        final MouseMotionListener [] mml = v.getCanvasComponent().getMouseMotionListeners();
	        final KeyListener [] l_kl = v.getCanvasComponent().getKeyListeners();
	        
	        for (int i = 0; i < ml.length; i++)
	        	v.getCanvasComponent().removeMouseListener(ml[i]);
	        
	        for (int i = 0; i < mml.length; i++)
	        	v.getCanvasComponent().removeMouseMotionListener(mml[i]);
	        
	        for (int i = 0; i < l_kl.length; i++)
	        	v.getCanvasComponent().removeKeyListener(l_kl[i]);

	        //now register
	        v.getCanvasComponent().addMouseListener(listener);
	        v.getCanvasComponent().addMouseMotionListener(listener);
	        v.getCanvasComponent().addKeyListener(listener);
		}
  }
  

  class ToggleClassDetails extends AbstractAction
  {
	  ToggleClassDetails() {
		      super("Class Details");
	  }

	boolean on = false;
	private void resetUMLClassDiagramPorts()
	{
		Graph2D graph = view.getGraph2D();
	    for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) 
 		{
	    	Edge e = ec.edge();
	     	EdgeRealizer er = graph.getRealizer(e);

			Port target_port = er.getTargetPort();
			Port source_port = er.getSourcePort();

			target_port.setOffsets(0, 0);
			source_port.setOffsets(0, 0);
		}
	}
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		//System.out.println("Before ports:");
		//updateUMLClassDiagramPorts(); 
 	   if (uml_diag_in_versions)
 	    {
 	    	Graph2D graph = view.getGraph2D();
 	    	for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) 
 			{
 	    		Node n = nc.node();
 				NodeRealizer realizer = graph.getRealizer(n);
 				ClassNodeRealizer cnr = (ClassNodeRealizer) realizer;
 				if (!on)
 				{
 					cnr.setOmitDetails(true);
 					cnr.fitContentWithOmittedDetalis(); 					
 				}
 				else
 				{
 					cnr.setOmitDetails(false);
 					cnr.fitContent();
 				}
 				
 				
 				
 			}
 	    	//System.out.println("After ports:");
 	    	
 	    	/*//recalculate edges
	    	for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
 	    	{
 	    		Edge e = ec.edge();
 	    		EdgeRealizer er = graph.getRealizer(e);
 	    		er.setDirty();
 	    		er.repaint();
 	    		Node source = e.source();
 	    		Node target = e.target();
 	    		System.out.println("s:"+source.index()+" t:"+target.index());
 	    		

 	    	}*/
	    	resetUMLClassDiagramPorts();
 	    	
 	    	
 	    	on = !on;
 	    	graph.updateViews();		
 	    }
	}
	  
  }






class CompareAction extends AbstractAction {


	Darls darls = null;
	Compare compare_obj = null; 
	CompareAction(Darls _darls)
	{
		darls = _darls;
	}
	
	public void post_compare_action_restore()
	{
		if (compare_obj == null) return;
		
		//restore the color of nodes and edges that were highlighted
		compare_obj.setNodesHighlight(false,true);
		compare_obj.setEdgesHighlight(false,true);
		
		if (uml_diag_in_versions) //alternatively isinstance CompareUML
		{
			((CompareUML) compare_obj).restore_edge_color();
		}
		else
			bubble_labels.clear();

		
	}
  	


	  public void actionPerformed(ActionEvent ev) 
	  {
		  if(enable_animation_comparison == false)
			{
				JOptionPane.showMessageDialog(frame,"you have disabled animation comparison, first enable it", "Warning",JOptionPane.OK_OPTION);
				return;
			}
		  else if (user_study != null)
			  return;
		 
		if (uml_diag_in_versions == false)
		{
			compare_obj = new CompareGraphs(darls);
			compare_obj.animateComparison();
			
		}
		else
		{
			compare_obj = new CompareUML(darls);
			compare_obj.animateComparison();
			//do the same thing as above here
		}
		//else
			//System.out.println("UML IN VERSIONS! NOT COMPARING YET");
		//compare uml diagrams
	  }



  };


	


 
  class GraphSavingAction extends AbstractAction {
	  
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GraphSavingAction()
	  {
		  super("saving");
		  putValue(Action.SMALL_ICON, new ImageIcon(DarlsBase.class.getResource("resource/redo.png")));
		  
		  
	  }
	  
	  public void actionPerformed(ActionEvent ev) 
	  {
	      /*
	       * THIS IS AN EXTREMELY UGLY HACK. BUT IT SEEMS TO DO THE JOB.
	       * */
	       
		  Graph2D g2 = view.getGraph2D();
	      //g1=(Graph2D)g2.createCopy();
		  Graph2D g1 = new Graph2D(); //Create an empty Graph
	      
	      //create vnode list 1
	      for (int i = 0; i < v_node_list_2.size(); i++)
	      {
	       	  VNode old_vnode = v_node_list_2.get(i);
	       	  Node new_node = old_vnode.node.createCopy(g1);
	       	  VNode new_vnode = new VNode(new_node,old_vnode.uuid);	       	  
	       	  v_node_list_1.add(new_vnode);
		  }	  

	      for (int i = 0; i <  v_node_list_1.size(); i++)
	    	  System.out.println("G1 node:"+v_node_list_1.get(i).uuid);
	      
	      //create vedge list 1
	      for (int i = 0; i < v_edge_list_2.size(); i++)
	      {
	       	  VEdge old_vedge = v_edge_list_2.get(i);
	       	  Edge old_edge = old_vedge.edge;
	       	  
	       	  Node old_source_node = old_edge.source();
	       	  Node old_target_node = old_edge.target();
	       	  
	       	  Node new_source_node = null;
	       	  Node new_target_node = null;
	       	  
	       	  //now we need to find new source and target nodes in g1
	       	  for (int j = 0; j < v_node_list_2.size(); j++)
	       	  {
	       		  if (v_node_list_2.get(j).node == old_source_node)
	       		  {
	       			  UUID new_node_vid = v_node_list_2.get(j).uuid;
	       			  
	       			  for (int k=0; k < v_node_list_1.size();k++)
	       			  {
	       				if (new_node_vid.equals(v_node_list_1.get(k).uuid))
	  	       			  	new_source_node = v_node_list_1.get(k).node;
	       			  }
	       			  break;
	       		  }	       			  
	       	  }
	       	  for (int j = 0; j < v_node_list_2.size(); j++)
	       	  {
	       		  if (v_node_list_2.get(j).node == old_target_node)
	       		  {
	       			  UUID new_node_vid = v_node_list_2.get(j).uuid;
	       			  
	       			  for (int k=0; k < v_node_list_1.size();k++)
	       			  {
	       				if (new_node_vid.equals(v_node_list_1.get(k).uuid))
	  	       			  	new_target_node = v_node_list_1.get(k).node;
	       			  }
	       			  break;
	       		  }	       			  
	       	  }

	       	  System.out.println("new source and target nodes:"+new_source_node+","+new_target_node);

	       	  Edge new_edge = old_edge.createCopy(g1,new_source_node,new_target_node);
	       	  VEdge new_vedge = new VEdge(new_edge,old_vedge.uuid);	       	  
	       	  v_edge_list_1.add(new_vedge);
		  }	  

	      for (int i = 0; i <  v_edge_list_1.size(); i++)
	    	  System.out.println("G1 edge:"+v_edge_list_1.get(i).uuid);
	      
	  }
  };


class BackgroundGraphDrawer
{
	  class Graph2DDrawable implements Drawable {
		    Graph2D graph;
		    Graph2DRenderer renderer;

		    Graph2DDrawable(Graph2D g) {
		      this.graph = g;
		      renderer = new DefaultGraph2DRenderer();
		    }

		    public void paint(Graphics2D gfx) {
		      renderer.paint(gfx, graph);
		    }

		    public Rectangle getBounds() {
		      return graph.getBoundingBox();
		    }
	}
	final private Color c_fill = new Color(255, 255, 255, 0);
	final private Color c_line = new Color(190,190,190);
	//final Color c_missing_fill = new Color(0, 0, 0, 100); //highlighting color for node
	//final Color c_missing_line = new Color(0,0,0,100);   //highlight color for edge
	
	//final Color c_common_fill = new Color(0, 100, 0, 255); //highlighting color for node
	//final Color c_common_line = new Color(0,0,100,255);   //highlight color for edge
		  
	private Graph2DDrawable gd = null;
  	  	
  	Graph2D backgroundGraph = null;
  	private Graph2DView backgroundView = null;
  	private Graph2DView sourceView = null;
  	
  	BackgroundGraphDrawer(Graph2DView backgroundView, Graph2DView sourceView)
  	{
  		this.backgroundView = backgroundView;
  		this.sourceView = sourceView;
  	}
  	public void clear()
  	{
  		//remove gray nodes after animation is over
  		backgroundView.removeBackgroundDrawable(gd);
  		backgroundView.updateView();
  	}
	private void color_edge(EdgeRealizer e, Color c_line)
	{
		e.setSelected(false);
	  	e.setLineColor(c_line);
	}
  	private void color_node(NodeRealizer r, Color fill_color, Color line_color)
	{
		r.setSelected(false);
        r.setFillColor(fill_color);	//use alpha color here somehow
        //in older version the next line was disabled
        r.setFillColor2(fill_color);	//use alpha color here somehow
        r.setLineColor(line_color);
        
	}
  	private void dim_node_color(NodeRealizer nr)
	{
		nr.setSelected(false);
		Color line_color = nr.getLineColor();
		Color fill_color1 = nr.getFillColor();
		Color fill_color2 = nr.getFillColor2();
		
		final int alpha = 110;
		
		if (line_color != null)
		{
			int r = line_color.getRed();
			int g = line_color.getGreen();
			int b = line_color.getBlue();
			
			nr.setLineColor(new Color(r,g,b,alpha));
		}
		
		if (fill_color1 != null)
		{
		
			int r = fill_color1.getRed();
			int g = fill_color1.getGreen();
			int b = fill_color1.getBlue();
			
			nr.setFillColor(new Color(r,g,b,alpha));
		}
		
		if (fill_color2 != null)
		{		
			int r = fill_color2.getRed();
			int g = fill_color2.getGreen();
			int b = fill_color2.getBlue();
			
			nr.setFillColor2(new Color(r,g,b,alpha));
		}


        //r.setFillColor(fill_color);	//use alpha color here somehow
        //r.setLineColor(line_color);
	}

  	public void redraw_selected_missing_nodes(NodeList nl,boolean b) //redraws selected nodes in the other graph
  	{
  		if (backgroundGraph != null && nl != null)
  		{
        	  for (NodeCursor nc = nl.nodes(); nc.ok(); nc.next()) 
        	  {
        		  NodeRealizer r = backgroundGraph.getRealizer(nc.node());
        		  //System.out.println("missing nodes stuff:"+r.getCenterX()+","+r.getCenterY());
        		  //commented out December 5, 2010, this is useless - it makes things worse!
        		  //color_node(r,c_missing_fill,c_missing_line);
        		  r.setSelected(b);
              }	 
  		}
  	}
  	 public void redraw_selected_missing_edges(EdgeList el,boolean b) //redraws selected nodes in the other graph
  	 {
  		if (backgroundGraph != null && el != null)
  		{
        	  for (EdgeCursor ec = el.edges(); ec.ok(); ec.next()) 
        	  {
        		  EdgeRealizer e = backgroundGraph.getRealizer(ec.edge());
        		  //System.out.println("missing nodes stuff:"+r.getCenterX()+","+r.getCenterY());
        		  //commented out December 5, 2010, this is useless - it makes things worse!
        		  //color_edge(e,c_missing_line);
        		  e.setSelected(b);
              }	 
  		}
  	 }
  		
  	public void redraw_selected_missing_nodes_left(NodeList missingNodesinLeftGraph, Graph2DView right_view)
  	{
  		backgroundGraph = new Graph2D();
  		NodeCursor nc = right_view.getGraph2D().nodes();
  		EdgeCursor ec = right_view.getGraph2D().edges();
  		
  		//do the edges first
		Hashtable h_new_and_old_nodes = new Hashtable();
  		for(;ec.ok();ec.next())
		{
			Edge edge = ec.edge();
			Node n1 = edge.source();
			Node n2 = edge.target();

			Node new_n1 = null;
			Node new_n2 = null;
			
			//if it was drawn previously we don't want to redraw it again
			if (h_new_and_old_nodes.containsKey(n1))
			{
				new_n1 = (Node) h_new_and_old_nodes.get(n1);
				//System.out.println("Node "+n1+" already exists");
			}
			else
			{
				NodeRealizer nr1 = right_view.getGraph2D().getRealizer(n1).createCopy();
				color_node(nr1,c_fill,c_line);
   			    nr1.getLabel().setTextColor(c_line); //this is dimming the text color

				if (missingNodesinLeftGraph.contains(n1))
				{
					//color_node(nr1,c_missing_fill,c_missing_line);
				}
				new_n1 = backgroundGraph.createNode(nr1);
				
			}
			//if it was drawn previously we don't want to redraw it again
			if (h_new_and_old_nodes.containsKey(n2))
			{
				new_n2 = (Node) h_new_and_old_nodes.get(n2);
				//System.out.println("Node "+n1+" already exists");
			}
			else
			{
				NodeRealizer nr2 = right_view.getGraph2D().getRealizer(n2).createCopy();
				color_node(nr2,c_fill,c_line);
   			    nr2.getLabel().setTextColor(c_line); //this is dimming the text color
	  			//if part of the missing nodes, then color it red
				if (missingNodesinLeftGraph.contains(n2))
				{
					//color_node(nr2,c_missing_fill,c_missing_line);
				}

				new_n2 = backgroundGraph.createNode(nr2);				
			}

			
			EdgeRealizer e = right_view.getGraph2D().getRealizer(edge).createCopy();
			color_edge(e,c_line);
			backgroundGraph.createEdge(new_n1, new_n2,e);
			
			h_new_and_old_nodes.put(n1, new_n1);
			h_new_and_old_nodes.put(n2, new_n2);
			
		}
		
  		//now continue populating the graph with the remaining nodes that have no edges
  		for(;nc.ok();nc.next())
  		{	
  			Node n = nc.node();
  			if (!h_new_and_old_nodes.containsKey(n)) //if it was drawn previously, we dont want to redraw it again
  			{
	  			NodeRealizer r = right_view.getGraph2D().getRealizer(n).createCopy();
	  			color_node(r,c_fill,c_line);
   			    r.getLabel().setTextColor(c_line); //this is dimming the text color

	  			//if part of the missing nodes, then color it red
				if (missingNodesinLeftGraph.contains(n))
				{
					//color_node(r,c_missing_fill,c_missing_line);				
				}
	  			backgroundGraph.createNode(r);
  			}
  		}

  		clear();
		gd = new Graph2DDrawable(backgroundGraph);
  		backgroundView.addBackgroundDrawable(gd);
  		  
  		backgroundView.updateView();
  	}
  	/*
  	 * Note: 01/11/2010
  	 * There is an issue with redrawing the shadow after relayout for some reason
  	 * I was not able to find a solution, but I suspect the issue is that there is
  	 * a bug in the yFiles library.
  	 * My workaround solution is to disable shadow drawing in the DarlsDefaults.java
  	 */
  	public void draw(boolean enable) //the argument takes the missing nodes. we redraw them in different color. see onNewSelectionRectangle
  	{
  		if(enable == false)
  			return;
  		
	  	  NodeList background_node_list = null;
	  	  EdgeList background_edge_list = null;

    	  //we create a copy of v1 graph and name it background graph
	  	  Graph2D source_graph = (Graph2D) sourceView.getGraph2D();
	  	 
  		  backgroundGraph = (Graph2D) sourceView.getGraph2D().createCopy();
  		  
  		  gd = new Graph2DDrawable(backgroundGraph);
  		  backgroundView.addBackgroundDrawable(gd);
    	  background_node_list = new NodeList(backgroundGraph.nodes());
    	  background_edge_list = new EdgeList(backgroundGraph.edges());
          
    	  //change the style of background node list
    	  for (NodeCursor nc = background_node_list.nodes(), nc_source = source_graph.nodes(); nc.ok(); nc.next(), nc_source.next()) 
    	  {
    		  NodeRealizer r = backgroundGraph.getRealizer(nc.node());
    		  
    		  //color common in different color than deleted, a beautiful hack hehe
    		
    		  if  (!uml_diag_in_versions)
    		  {
    			  List<VNode> source_graph_vnode_list = v_node_list_2;
    			  List<VNode> back_graph_vnode_list = v_node_list_1;
    			  
	    		  if (sourceView == left_view) //this is right drawer
	    		  {
	    			  source_graph_vnode_list = v_node_list_1;
	    			  back_graph_vnode_list = v_node_list_2;
	    		  }
	    			  //for right drawer we need to matches nodes that that exist in the right view
	    			  UUID uuid = VNode.getNodeVid(nc_source.node(), source_graph_vnode_list);
	    			  if (uuid != null)
	    			  {
	    				  //is it also in the right?
	    				  Node n = VNode.getNodeFromNodeList(uuid, back_graph_vnode_list);
	    				  if (n != null)
	    				  {
	    				    //color_node(r,c_common_fill,c_common_line);
	    					  r.getLabel().setTextColor(c_line); //this is dimming the text color
	    					  dim_node_color(r);
	    				  }
	    				  else
	    				  {
	    					  r.getLabel().setTextColor(c_line);
	    					  color_node(r,c_fill,c_line);
	    				  }
	    			  }
	    	    	  else
	    	    	  { 
	    	    	 	 r.getLabel().setTextColor(c_line);
	    	    		 color_node(r,c_fill,c_line);
	    	    	  }
    		  }


    		  else     		  //if this is a class diagram, then color the node's text
    		  {
    			  //System.out.println("uml_diag_in_versions:"+uml_diag_in_versions);
    			  color_node(r,c_fill,c_line);
    			  
    			  ClassNodeRealizer cnr = (ClassNodeRealizer) r;
    			  NodeLabel class_label = cnr.getLabel();
    			  NodeLabel attrib_label = cnr.getAttributeLabel();
    			  NodeLabel method_label = cnr.getMethodLabel();      			  
    			  class_label.setTextColor(c_line);
    			  attrib_label.setTextColor(c_line);
    			  method_label.setTextColor(c_line);
    			  cnr.getStereotypeLabel().setTextColor(c_line);
    			  cnr.getConstraintLabel().setTextColor(c_line);
    			  cnr.setUse3DEffect(false);
    		  }
          }	    	  
    	  //do the same thing for edges here at some point
    	  for (EdgeCursor ec = background_edge_list.edges(); ec.ok(); ec.next()) 
    	  {
    		  EdgeRealizer e = backgroundGraph.getRealizer(ec.edge());
    		  color_edge(e,c_line);	
          }
    	  //...............................................
    	  //missing nodes

    	  backgroundView.updateView();
  	}
}
BackgroundGraphDrawer RightBackGroundDrawer = null;
BackgroundGraphDrawer LeftBackGroundDrawer = null;

void clearLeftBackGroundDrawer()
{
	LeftBackGroundDrawer.clear();
}
void clearRightBackGroundDrawer()
{
	RightBackGroundDrawer.clear();
}
BackgroundGraphDrawer init_background_drawer(BackgroundGraphDrawer backGroundDrawer, Graph2DView backgroundView, Graph2DView sourceView, boolean enable)
{
	//System.out.println("inside v == versionView");
	if (backGroundDrawer != null)
	{
		backGroundDrawer.clear();
		//backGroundDrawer = null;
	}
	backGroundDrawer = new BackgroundGraphDrawer(backgroundView,sourceView); 
	backGroundDrawer.draw(enable);
	return backGroundDrawer;
}

static private void recalc_union_world_rect(Graph2DView view1, Graph2DView view2)
{
	//this doesnt work yet
	Graph2D g1 = view1.getGraph2D();
	Graph2D g2 = view2.getGraph2D();
	
	Rectangle bbox1 = g1.getBoundingBox();
	Rectangle bbox2 = g2.getBoundingBox();
	
	//Rectangle wrect1 = view1.getBounds();
	//Rectangle wrect2 = view2.getBounds();
	//System.out.println("bounds1:"+wrect1+" bounds2:"+wrect2);
	
	//Rectangle wrect1 = view1.getWorldRect();
	//Rectangle wrect2 = view2.getWorldRect();
	//Rectangle uw = wrect1.union(wrect2);
	


	Rectangle u = (Rectangle) bbox1.createUnion(bbox2);
	


	view1.setViewPoint(u.x,u.y);
	
	//commenting the following two lines fits the graph into the world
	//if left uncommented there will be extra space on the graph
	view1.setWorldRect(u.x, u.y, u.width, u.height);
	view2.setWorldRect(u.x, u.y, u.width, u.height);
	
	view1.setCenter(u.x+(u.width)/2,u.y+(u.height)/2);
	view2.setCenter(u.x+(u.width)/2,u.y+(u.height)/2);
	
	view1.updateWorldRect();
	view2.updateWorldRect();
	
	
	view1.updateView();
	view2.updateView();
	
	//double zoom_level_vertical = (double)uw.height/ (double)u.height;
	//double zoom_level_horizontal = (double)uw.width/ (double)u.width;
	//double zoom_level = Math.min(zoom_level_vertical, zoom_level_horizontal);
	

	//view1.setZoom(zoom_level);
	//view2.setZoom(zoom_level);
	
	
}

private void adjustView(Graph2DView v)
{
	 if (v == left_view) //for right background
	    { 
	    	RightBackGroundDrawer = init_background_drawer(RightBackGroundDrawer,view,left_view,overlay_graph_in_right_view);
	    	recalc_union_world_rect(view,left_view);
	    }
	    else if (v == view) //for left background
	    {
	    
	    	LeftBackGroundDrawer=init_background_drawer(LeftBackGroundDrawer,left_view,view,overlay_graph_in_left_view);
	    	recalc_union_world_rect(view,left_view);
	    	
	    }
	    //this is to deselect everything when changing views
	    if (v == left_view || v == view)
	    {
	    	deselectEverything();
	    	deselectBoth(left_view.getGraph2D());
	    	deselectBoth(view.getGraph2D());
	    }
}
private void setViewToGraph(Graph2D graph, Graph2DView v)
{
    v.setGraph2D(graph);
    v.repaint(); 
    //new!
    adjustView(v);
    
   
}
class GraphLoadingAction extends AbstractAction {
	  
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GraphLoadingAction()
	  {
		  super("saving");
		  putValue(Action.SMALL_ICON, new ImageIcon(DarlsBase.class.getResource("resource/undo.png")));
		  
		  
	  }
	  
	  public void actionPerformed(ActionEvent ev) {

		  //setViewToGraph(graph_v1, view);
	      //We may want to think how to implement transition back
	        
	    }
  };
  
  class NodePropertyEditorAction extends AbstractAction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	NodePropertyHandler nodePropertyHandler;
    
    NodePropertyEditorAction() {
      super("Node Properties");
      putValue(Action.SMALL_ICON,
          new ImageIcon( DarlsBase.class.getResource("resource/properties.png")));
      putValue(Action.SHORT_DESCRIPTION, "Edit Node Properties");
   
      Selections.SelectionStateObserver sso = new Selections.SelectionStateObserver() {
        protected void updateSelectionState(Graph2D graph) 
        {
          setEnabled(view.getGraph2D().selectedNodes().ok());
        } 
        /*public void onGraph2DSelectionEvent(Graph2DSelectionEvent e) 
        {
        	System.out.println("is it a node selection"+e.getSource());
        }*/
      };
      
      view.getGraph2D().addGraph2DSelectionListener(sso);
      view.getGraph2D().addGraphListener(sso);
      
      setEnabled(false);
    
      nodePropertyHandler = new NodePropertyHandler();
    }
    
    public void actionPerformed(ActionEvent ev) {
      Graph2D graph = view.getGraph2D();
      if(!Selections.isNodeSelectionEmpty(graph)) {
        nodePropertyHandler.updateValuesFromSelection(graph);
        if(nodePropertyHandler.showEditor(view.getFrame())) {
          nodePropertyHandler.commitNodeProperties(graph);
          graph.updateViews();
        }
      }
    }
  }
  
  public static class NodePropertyHandler extends OptionHandler 
  {
    static final String[] shapeTypes = { "Rectangle", "Rounded Rectangle", "Ellipse" };
    public NodePropertyHandler() {
      super("Node Properties");
      addString("Label", "").setValueUndefined(true);
      addEnum("Shape Type", shapeTypes, 0).setValueUndefined(true);
      addColor("Color", null, true).setValueUndefined(true);
      addDouble("Width", 0.0).setValueUndefined(true);
      addDouble("Height", 0.0).setValueUndefined(true);
    }
  
    /**
     * Retrieves the values from the set of selected nodes (actually node 
     * realizers) and stores them in the respective option items. 
     */
    public void updateValuesFromSelection(Graph2D graph)
    {
      NodeCursor nc = graph.selectedNodes();
      NodeRealizer nr = graph.getRealizer(nc.node());
      
      // Get the initial values from the first selected node. 
      String label = nr.getLabelText();
      boolean sameLabels = true;
      byte shapeType = 0;
      boolean onlyShapeNR = true;
      boolean sameShapeType = true;
      if (nr instanceof ShapeNodeRealizer)
        shapeType = ((ShapeNodeRealizer)nr).getShapeType();
      else
      {
        onlyShapeNR = false;
        sameShapeType = false;
      }
      Color color = nr.getFillColor();
      boolean sameColor = true;
      double width = nr.getWidth();
      boolean sameWidth = true;
      double height = nr.getHeight();
      boolean sameHeight = true;
      
      // Get all further values from the remaining set of selected node 
      // realizers. 
      if (nc.size() > 1)
      {
        for (nc.next(); nc.ok(); nc.next())
        {
          nr = graph.getRealizer(nc.node());
          
          if (sameLabels && !label.equals(nr.getLabelText()))
            sameLabels = false;
          if (sameShapeType && onlyShapeNR)
          {
            if (nr instanceof ShapeNodeRealizer)
            {
              if (shapeType != ((ShapeNodeRealizer)nr).getShapeType())
                sameShapeType = false;
            }
            else
            {
              onlyShapeNR = false;
              sameShapeType = false;
            }
          }
          if (sameColor && color != nr.getFillColor())
            sameColor = false;
          if (sameWidth && width != nr.getWidth())
            sameWidth = false;
          if (sameHeight && height != nr.getHeight())
            sameHeight = false;
          
          if (!(sameLabels | sameShapeType | sameColor | sameWidth | sameHeight))
            break;
        }
      }
      
      // If, for a single property, there are multiple values present in the set 
      // of selected node realizers, then the respective option item is set to 
      // indicate an "undefined value" state. 
      // Note that property "valueUndefined" for an option item is set *after* 
      // its value has actually been modified! 
      set("Label", label);
      getItem("Label").setValueUndefined(!sameLabels);
      
      set("Shape Type", shapeTypes[shapeType]);
      getItem("Shape Type").setValueUndefined(!sameShapeType);
      getItem("Shape Type").setEnabled(onlyShapeNR);

      set("Color", color);
      getItem("Color").setValueUndefined(!sameColor);
      
      set("Width", new Double(width));
      getItem("Width").setValueUndefined(!sameWidth);
      
      set("Height", new Double(height));
      getItem("Height").setValueUndefined(!sameHeight);
    }
   
    public void commitNodeProperties(Graph2D graph) 
    {
      for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next())
      {
        Node n = nc.node();
        NodeRealizer nr = graph.getRealizer(n);
        
        if (!getItem("Label").isValueUndefined())
          nr.setLabelText(getString("Label"));
        if (!getItem("Shape Type").isValueUndefined() && nr instanceof ShapeNodeRealizer)
          ((ShapeNodeRealizer)nr).setShapeType((byte)getEnum("Shape Type"));
        if (!getItem("Color").isValueUndefined())
          nr.setFillColor((Color)get("Color"));
        if (!getItem("Width").isValueUndefined())
          nr.setWidth(getDouble("Width"));
        if (!getItem("Height").isValueUndefined())
          nr.setHeight(getDouble("Height"));
      }
    }
  }
  
  /** Launches this demo. */
  public static void main(String[] args) {
	  System.out.println("this is inside the main of animated verioning");
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        initLnF();
        (new Darls()).start();
        //System.out.println("in");
      }
    });
  }

  
  
  

  protected void load_graph_version_data (URL resource, String str_dir, String str_file)
	{
	  	uml_diag_in_versions = false; //graph in versions

		init_comic_strip_variables();
		
		str_file = str_file.replace(".graphml","");
		
		int last_index_slash=str_file.lastIndexOf('\\');
		int last_index_underscore = str_file.lastIndexOf('_');
		str_file=str_file.substring(last_index_slash+1,last_index_underscore);
		
		
		System.out.println("STR_FILE:"+str_file);
		FilenameFilter filter1 = new FilenameFilter() {
		    public boolean accept(File dir, String name) {	    	
		        return name.endsWith(".graphml");
		    }
		};
		/*
		FilenameFilter filter2 = new FilenameFilter() {
		    public boolean accept(File dir, String name) {	    	
		        return name.endsWith(".nv");
		    }
		};

		FilenameFilter filter3 = new FilenameFilter() {
		    public boolean accept(File dir, String name) {	    	
		        return name.endsWith(".ev");
		    }
		};
		 */
		
		File dir = new File(str_dir);

		String[] graphml_files_unsorted = dir.list(filter1);
		String[] graphml_files = new String[graphml_files_unsorted.length];
		for (int i = 1; i <= graphml_files.length; i++) 
			graphml_files[i-1] = str_file+"_"+i;
		
		/*String[] nv_files = dir.list(filter2);
		String[] ev_files = dir.list(filter3);*/
		
		//set the current version count to the number of versions, so that none are overwritten
		//if a version in the middle is delete, this will not work
		//you will need to fix it to work with deleting versions
		version_num = graphml_files.length+1;

		graphs =  new ArrayList<Graph2D>();
		v_nodes = new ArrayList<List>();
		v_edges = new ArrayList<List>();
		
		for (int i = 0; i < graphml_files.length; i++) System.out.println("graph files:"+graphml_files[i]);
		for (int i = 0; i < graphml_files.length; i++)
		{
			//first load the graph
			Graph2D graph = new Graph2D();
			//windows
			//File f = new File(str_dir+"\\"+graphml_files[i]+".graphml");
			//mac
			File f = new File(graphml_files[i]+".graphml");
			
			URL url = null;
			try 
			{
				url = f.toURI().toURL();
		    } 
			catch (MalformedURLException urlex) 
			{
				urlex.printStackTrace();
	        }		
			loadGraph(url,graph);		
			graphs.add(graph);
		    //System.out.println("size="+graphs.size()+"graph loaded:"+graph+" res= "+url);

			//load nodes and edges
			List <VNode> nl =  new ArrayList<VNode>();
			List <VEdge> el =  new ArrayList<VEdge>();
	    	//System.out.println("node file name="+nodeFileName);
			
			//windows
			//loadVersionEdgesAndNodesData(str_dir+"\\"+graphml_files[i]+".nv",str_dir+"\\"+graphml_files[i]+".ev",graph, nl, el);
			
			//mac
			loadVersionEdgesAndNodesData(graphml_files[i]+".nv",graphml_files[i]+".ev",graph, nl, el);
			
			v_nodes.add(nl);
			v_edges.add(el);
			
		}	

		/*
		load_comic_strip_views();
		
		//setting the view-only view
		Graph2D graph_v1 = graphs.get(0);
		setViewToGraph(graph_v1,left_view);	

		Graph2D last_graph = graphs.get(graphs.size()-1);
		setEditableVersionView(last_graph);
		
		//adjust zoom level and pan for left and right views
		adjustBottomViewsZoomLevel();
		*/
		updateViewOnFileAction();
		
		
	    



	}

  boolean uml_diag_in_versions = false;
  protected void load_uml_verison_data (URL resource, String str_dir, String str_file)
	{
	  	uml_diag_in_versions = true;
		init_comic_strip_variables();
		
		str_file = str_file.replace(".graphml","");
		
		int last_index_slash=str_file.lastIndexOf('\\');
		int last_index_underscore = str_file.lastIndexOf('_');
		str_file=str_file.substring(last_index_slash+1,last_index_underscore);
		
		
		System.out.println("STR_FILE:"+str_file);
		FilenameFilter filter1 = new FilenameFilter() {
		    public boolean accept(File dir, String name) {	    	
		        return name.endsWith(".graphml");
		    }
		};

		
		File dir = new File(str_dir);

		String[] graphml_files_unsorted = dir.list(filter1);
		String[] graphml_files = new String[graphml_files_unsorted.length];
		for (int i = 1; i <= graphml_files.length; i++) 
			graphml_files[i-1] = str_file+"_"+i;
		
		
		//set the current version count to the number of versions, so that none are overwritten
		//if a version in the middle is delete, this will not work
		//you will need to fix it to work with deleting versions
		version_num = graphml_files.length+1;

		graphs =  new ArrayList<Graph2D>();

		
		for (int i = 0; i < graphml_files.length; i++) System.out.println("graph files:"+graphml_files[i]);
		for (int i = 0; i < graphml_files.length; i++)
		{
			//first load the graph
			Graph2D graph = new Graph2D();
			File f = new File(str_dir+"\\"+graphml_files[i]+".graphml");
			URL url = null;
			try 
			{
				url = f.toURI().toURL();
		    } 
			catch (MalformedURLException urlex) 
			{
				urlex.printStackTrace();
	        }		
			loadGraph(url,graph);		
			graphs.add(graph);
		    System.out.println("size="+graphs.size()+"graph loaded:"+graph+" res= "+url);
		}	

		updateViewOnFileAction();
	}
private void updateViewOnFileAction() {
	//init_comic_strip();
	UserStudyBase.fix_for_color_blind(graphs);	
	load_comic_strip_views();
	
	//setting the view-only view
	Graph2D graph_v1 = graphs.get(0);
	setViewToGraph(graph_v1,left_view);
	str_left_view_text = "Version "+1;
	MyBackgroundRenderer.newInstance(left_view, Color.WHITE, true).setText(str_left_view_text);

	Graph2D last_graph = graphs.get(graphs.size()-1);
	setEditableVersionView(last_graph);
	str_right_view_text = "Version "+graphs.size();
	MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(str_right_view_text);
	
	v_node_list_1 = (List <VNode>)v_nodes.get(0);
	v_node_list_2 = (List <VNode>)v_nodes.get(v_nodes.size()-1);

	v_edge_list_1 = (List <VEdge>)v_edges.get(0);
	v_edge_list_2 = (List <VEdge>)v_edges.get(v_edges.size()-1);
	
	//adjust zoom level and pan for left and right views
	adjustBottomViewsZoomLevel();
}

static int version_num = 1; //when load a graph this needs to be updated, if new document set it to 1

protected Action createGraphLoadVersionAction()
{
    return new LoadGraphVersionAction();

}
protected Action createGraphCommitVersionAction()
{
	return new CommitGraphVersionAction();
}

@Override
protected Action createUMLCommitVersionAction() {
	// TODO Auto-generated method stub
	return new CommitUMLVersionAction();
}
@Override
protected Action createUMLLoadVersionAction() {
	// TODO Auto-generated method stub
	return new LoadUMLVersionAction();
}

protected class LoadUMLVersionAction extends LoadVersionAction
{

	public LoadUMLVersionAction() {
        super("Load UML Versions...");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void load(URL resource, String strDir, String strFile) {
		// TODO Auto-generated method stub
		
		//save these for the experiment
		last_resource = resource;
		last_str_dir = strDir;
		last_str_file = strFile;
		
		load_uml_verison_data(resource, strDir, strFile);
		
	}
}
protected class CommitUMLVersionAction extends SaveVersionAction {

	public CommitUMLVersionAction() {
		super("Commit UML Versions...");;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void load(URL resource, String strDir, String strFile) {
		// TODO Auto-generated method stub
		load_uml_verison_data(resource, strDir, strFile);
		
	}

	@Override

	 protected void saveVersionData(String name) {
    	String new_name = name.replace(".graphml","");

        version_num++;
        if (graphs== null)
        	graphs = new ArrayList<Graph2D>();
        
        graphs.add(view.getGraph2D());
        //load_comic_strip_views(); //update comic strip views after committing
    }

}


URL last_resource;
String last_str_dir;
String last_str_file;


protected class LoadGraphVersionAction extends LoadVersionAction 
{

    public LoadGraphVersionAction() {
        super("Load Graph Versions...");
      }

	@Override
	protected void load(URL resource, String strDir, String strFile) {
		// TODO Auto-generated method stub
		
		//save arguments for the experiment
		last_resource = resource;
		last_str_dir = strDir;
		last_str_file = strFile;
		
		load_graph_version_data(resource,strDir,strFile);
	}


	
}
protected abstract class LoadVersionAction extends AbstractAction
{ 
	JFileChooser chooser;
	//each load version class implements its own load function (uml and graph)
	
	protected abstract void load (URL resource, String str_dir, String str_file);
	
	public LoadVersionAction(String menu_title) {
	  super(menu_title);
	  chooser = null;
	}
	
	public void actionPerformed(ActionEvent e) {
	  if (chooser == null) {
	    chooser = new JFileChooser();
	    chooser.setAcceptAllFileFilterUsed(false);
	    chooser.addChoosableFileFilter(new FileFilter() {
	      public boolean accept(File f) {
	        return f.isDirectory() || f.getName().endsWith(".graphml");
	      }
	
	      public String getDescription() {
	        return "GraphML Format (.graphml)";
	      }
	    });
	  }
	  if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
	    
		URL resource = null;
		String parent = null;
	    try {
	      resource = chooser.getSelectedFile().toURI().toURL();
	      File f = chooser.getSelectedFile();
	      parent = f.getParent();
	    } catch (MalformedURLException urlex) {
	      urlex.printStackTrace();
	    }
	    load (resource, parent, chooser.getSelectedFile().toString());
	  }
	}
	}
protected abstract class SaveVersionAction extends AbstractAction
{
	JFileChooser chooser;

	protected abstract void saveVersionData(String name);
    //this one is for reloading the graphs and everything upon committing

	protected abstract void load (URL resource, String str_dir, String str_file);

	
	public SaveVersionAction(String menu_title) {		
	  super(menu_title);
	  chooser = null;
	}

public void actionPerformed(ActionEvent e) {
	URL url = null;
  if (chooser == null) {
    chooser = new JFileChooser();
    url = view.getGraph2D().getURL();
    if (url != null && "file".equals(url.getProtocol())) {
      try {
        chooser.setSelectedFile(new File(new URI(url.toString())));
      } catch (URISyntaxException e1) {
        // ignore
      }
    }
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".graphml");
      }

      public String getDescription() {
        return "GraphML Format (.graphml)";
      }
    });
  }
  if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
    String name = chooser.getSelectedFile().toString();
    if(!name.endsWith(".graphml")) {
      name += "_"+version_num+".graphml";
    }
    IOHandler ioh = createGraphMLIOHandler();

    try {
    	bubble_labels.restoreDefaultNodeRealizer(view.getGraph2D());
      //view.repaint();//delete bubble labels
      ioh.write(view.getGraph2D(), name);          
      saveVersionData(name);   
      //reload the graphs and everything upon committing
      load(url, chooser.getSelectedFile().getParent().toString(), name);
      
    } catch (IOException ioe) {
      D.show(ioe);
    }
  }
}
}

protected class CommitGraphVersionAction extends SaveVersionAction {

    public CommitGraphVersionAction() {
      super("Commit Graph Versions...");
    }

    //save graph version data
    protected void saveVersionData(String name) {
    	String new_name = name.replace(".graphml","");
    	System.out.println(new_name);
        OutputStream nodeFile;
        OutputStream edgeFile;
        Properties version_data_nodes = new Properties();    
        Properties version_data_edges = new Properties();

        Node[] na = view.getGraph2D().getNodeArray();
        Edge[] ea = view.getGraph2D().getEdgeArray();
        
        for (int i=0; i < na.length; i++ )
        	for (int j = 0; j < v_node_list_2.size(); j++)
        	{
        		Node n = ( (VNode) (v_node_list_2.get(j))).node;
        		UUID vid = ( (VNode) (v_node_list_2.get(j))).uuid;
        		if ( n == na[i])
        		{
        			version_data_nodes.setProperty(""+i,vid+"");
        		}
        	}
        for (int i=0; i < ea.length; i++ )
        	for (int j = 0; j < v_edge_list_2.size(); j++)
        	{
        		Edge e = ( (VEdge) (v_edge_list_2.get(j))).edge;
        		UUID vid = ( (VEdge) (v_edge_list_2.get(j))).uuid;
        		if ( e == ea[i])
        		{
        			version_data_edges.setProperty(""+i,vid+"");
        		}
        	}
        
        try {
            nodeFile = new FileOutputStream(new_name+".nv");
            version_data_nodes.store(nodeFile, "Properties File to Animated Versioning (Nodes)");
            nodeFile.close();
        } catch (IOException ioe) {
            System.out.println("I/O Exception.could not save version data (nodes)");
            ioe.printStackTrace();
            System.exit(0);
        }
        try {
            edgeFile = new FileOutputStream(new_name+".ev");
            version_data_edges.store(edgeFile, "Properties File to Animated Versioning (Edges)");
            edgeFile.close();
        } catch (IOException ioe) {
            System.out.println("I/O Exception.could not save version data (edges)");
            ioe.printStackTrace();
            System.exit(0);
        }
        version_num++;
        System.out.println("VIEW.GETGRAPH2D: "+view.getGraph2D());
        if (graphs== null)
        	graphs = new ArrayList<Graph2D>();
        
        graphs.add(view.getGraph2D());

        
        //load_comic_strip_views(); //update comic strip views after committing
    }

	@Override
	protected void load(URL resource, String strDir, String strFile) {
		// TODO Auto-generated method stub
	      //reload the graphs and everything upon committing
		load_graph_version_data(resource,strDir,strFile);

	}
    
    
  }

  /**
   * Action that loads the current graph from a file in GraphML format.
   */

  private void load_comic_strip_views()
  {
	  	//init_comic_strip();
		//setting comic strip views
	  
		int i_condition = Math.max(graphs.size(),n_comics);
		int k_condition = Math.min(n_comics, graphs.size());
		int i; int k;
		
		if (n_comics > graphs.size())
			i = Math.min(i_condition-1,k_condition-1);
		else
			i = i_condition - 1;
			
		
		for (k = k_condition-1; i >= Math.max(0,graphs.size()-n_comics); i--,k--)
		{
			System.out.println("i="+i+",k="+k);
			Graph2D comic_graph = (Graph2D) graphs.get(i).createCopy();//changed from graphs.get(i) on November 17, 2010 to prevent comic view updating when working on graph
			setViewToGraph(comic_graph,comic_view[k]);
			
			MyBackgroundRenderer.newInstance(comic_view[k], Color.WHITE, false).setText(""+(i+1));
			comic_view[k].fitContent();		
			comic_view[k].updateView();
			ComicMouseListener trigger= new ComicMouseListener(k);
			comic_view[k].getGlassPane().addMouseListener(trigger);
			
		}
		first_graph = i+1; 
		
		//clear the empty one (this is if you load a file with fewer versions than before)
		Graph2D empty_graph = new Graph2D();
		for (i = graphs.size(); i < n_comics; i++)
		{
				setViewToGraph(empty_graph, comic_view[i]);
				MyBackgroundRenderer.newInstance(comic_view[i], Color.gray, false).setText(str_comic_empty);
				comic_view[i].updateView();			
		}
 }
private void init_comic_strip_variables()
{
	first_graph = 0; //reset the first graph
	selected_comic_view_index=-1; //reset the current view selected index
}


private void setEditableVersionView(Graph2D graph)
{
	/*
	 * This function makes sure you can still edit the view properly after loading, or clearing bubbles
	 * This happens because the node realizer is not default
	 */
	
	NodeRealizer nr = view.getGraph2D().getDefaultNodeRealizer();
	EdgeRealizer er = view.getGraph2D().getDefaultEdgeRealizer();
	
	
	
	
	graph.setDefaultNodeRealizer(nr);
	graph.setDefaultEdgeRealizer(er);
	graph.addGraphListener(graphListener);

	setViewToGraph(graph,view);

	
	//view.fitContent();
	//versionView.fitContent();
	view.updateView();
	left_view.updateView();
}

protected void loadVersionEdgesAndNodesData(String nodeFileName,String edgeFileName,Graph2D graph, List nodes, List edges ) 
{
	//String nodeFileName = name.replace(".graphml","")+".nv";
	//String edgeFileName = name.replace(".graphml","")+".ev";
	
	System.out.println(nodeFileName);
	//continue here
	InputStream nodesFile, edgesFile;
    Properties nodeProp = new Properties();
    Properties edgeProp = new Properties();

    try {
    	//System.out.println("node file name="+nodeFileName);
        nodesFile = new FileInputStream(nodeFileName);
        nodeProp.load(nodesFile);
        nodesFile.close();
    } catch (IOException ioe) {
        System.out.println("I/O Exception.version data is missing for nodes");
        ioe.printStackTrace();
        System.exit(0);
    }
    try {
        edgesFile = new FileInputStream(edgeFileName);
        edgeProp.load(edgesFile);
        edgesFile.close();
    } catch (IOException ioe) {
        System.out.println("I/O Exception.version data is missing for edges");
        ioe.printStackTrace();
        System.exit(0);
    }

    //return tempProp;
    //nodeProp.list(System.out);
    //edgeProp.list(System.out);
    Node[] na = graph.getNodeArray();
    Edge[] ea = graph.getEdgeArray();
    
    //add nodes to nodes list and edges to edges list first with dummy vids
    for (int i = 0; i < na.length; i++)
    {
    	VNode vnode = new VNode((na[i]));
    	nodes.add(vnode);
    }
    for (int i = 0; i < ea.length; i++)
    {
    	VEdge vedge = new VEdge((ea[i]));
    	edges.add(vedge);
    }
    
   /* int max_node_vid = 0;	//to set the right static vids after loading.
    int max_edge_vid = 0;*/
    for (int i=0; i< nodeProp.size(); i++)
    {
    	for (int j = 0; j < nodes.size();j++)
    	{
    		VNode vnode = (VNode) nodes.get(j);
    		if (na[i] == vnode.node )
    		{
    				String str_uuid = nodeProp.getProperty(i+"");
    				UUID uuid = UUID.fromString(str_uuid);
    				vnode.uuid = uuid;
    				
    				/*vnode.vid = Integer.parseInt(nodeProp.getProperty(i+""));
    				if (vnode.vid > max_node_vid)
    					max_node_vid = vnode.vid;*/
    		}
    		
    	}
    	//System.out.println(tempProp.getProperty(i+""));   	
    	
    }
    for (int i=0; i< edgeProp.size(); i++)
    {
    	for (int j = 0; j < edges.size();j++)
    	{
    		VEdge vedge = (VEdge) edges.get(j);
    		if (ea[i] == vedge.edge )
    		{
    				String str_uuid = edgeProp.getProperty(i+"");
    				UUID uuid = UUID.fromString(str_uuid);
    				vedge.uuid = uuid;
    				/*vedge.vid = Integer.parseInt(edgeProp.getProperty(i+""));
    				if (vedge.vid > max_edge_vid)
    					max_edge_vid = vedge.vid;*/
    		}
    	}
    	//System.out.println(tempProp.getProperty(i+""));   	
    	
    }

   /* //make the static variables be equal the max vid+1
    VEdge.count = max_edge_vid+1;
    VNode.count = max_node_vid+1;
    
    for(int j=0;j<nodes.size();j++ )
    {
    	VNode vnode = (VNode) nodes.get(j);
    	System.out.println(vnode.vid);
    	System.out.println("------"+vnode.node);
    }
    for(int j=0;j<edges.size();j++ )
    {
    	VEdge vedge = (VEdge) edges.get(j);
    	System.out.print(vedge.vid);
    	System.out.println("------"+vedge.edge);
    }*/
    //--------------------------------------------
    //load graphs from other versions
    

}
protected void loadGraph(URL resource, Graph2D graph_v) {

    if (resource == null) {
      String message = "Resource \"" + resource + "\" not found in classpath";
      D.showError(message);
      throw new RuntimeException(message);
    }

    try {
      IOHandler ioh = createGraphMLIOHandler();
      graph_v.clear();
      ioh.read(graph_v, resource);
    } catch (IOException e) {
      String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
      D.bug(message);
      throw new RuntimeException(message, e);
    }
    graph_v.setURL(resource);
    
    /*versionView.fitContent();
    versionView.updateView();
    
    view.fitContent();
    view.updateView();
     */
  }


class ButtonListener implements ActionListener {



	  public void actionPerformed(ActionEvent e) {
	    if (e.getActionCommand().equals("b_forward")) {
		    System.out.println("Button1 has been clicked");
		    //setting comic strip views
		    if(((first_graph+1)+n_comics)>graphs.size()) //so that the last comic is not repeated
		    {
		    	
		    	return;
		    }
		    first_graph++;
		  	for (int i = first_graph,k=0; i < Math.min(graphs.size(),first_graph+n_comics); i++,k++)
			  	{
			  		System.out.println("i="+i+",k="+k+",graphs_size="+graphs.size());
		  			Graph2D comic_graph = (Graph2D) graphs.get(i).createCopy(); //changed from graphs.get(i) on November 17, 2010 to prevent comic view updating when working on graph
			  		setViewToGraph(comic_graph,comic_view[k]);
			  		
			  		MyBackgroundRenderer.newInstance(comic_view[k], Color.WHITE, false).setText(""+(i+1));
			  		comic_view[k].fitContent();		
			  		comic_view[k].updateView();
			  	}
	    }
	    else if (e.getActionCommand().equals("b_backward")) {
	    	System.out.println("Button2 has been clicked");
		    if((first_graph-1)<0)		    		
		    	return;
		    --first_graph;
		    int bounds=Math.min(first_graph+n_comics, graphs.size());
	  	    for (int i = first_graph,k=0; i < bounds; i++,k++)
		  	{
		  		//System.out.println ("i="+i+"k="+k);
	  	    	Graph2D comic_graph = (Graph2D) graphs.get(i).createCopy(); //changed from graphs.get(i) on November 17, 2010 to prevent comic view updating when working on graph
		  		setViewToGraph(comic_graph,comic_view[k]);
		  		
		  		MyBackgroundRenderer.newInstance(comic_view[k], Color.WHITE, false).setText(""+(i+1));
		  		comic_view[k].fitContent();		
		  		comic_view[k].updateView();
		  	} 
	    }
	    
	    else if(e.getActionCommand().equals("load_left_view"))
	    {
	    	loadGraphToLeftView();
	    	
	    }
	    
	    else if(e.getActionCommand().equals("load_right_view"))
	    {
	    	loadGraphToRightView();
	    }
	    else if (e.getActionCommand().equals("submit_results"))
	    {
	    	System.out.println("Submit!");
	    	try_submit();
	    }
	    else if (e.getActionCommand().equals("clear_exp_selection"))
	    {
	    	user_study.onClearSelectionClick();

	    }

	    /*if(e.getActionCommand().equals("edit_mode"))
	    {
	    	setAllowNodeCreation(true);
	    }
	    if(e.getActionCommand().equals("selection_mode"))
	    {
	    	setAllowNodeCreation(false);
	    }*/
	  }
}



	

private void setAllowNodeCreation(boolean flag)
{
	
	Iterator ir = view.getViewModes();
	while(ir.hasNext())
	{
		ViewMode vm = (ViewMode) ir.next();
		if(vm instanceof EditMode )
		{	
			EditMode em = (EditMode) vm;
			em.allowNodeCreation(flag);
			vm.activate(true);
		}
	}
	
}


protected void adjustBottomViewsZoomLevel() //this function needs to be called whenever relayouting happens because the world rectangle changes as well 
{
	/*
	 * This function sets the right zoom level so that it's easy to compare the graphs after they were relayouted for example.
	 * This solves the navigation issue
	 * The only issue is that the graphs can get smaller
	 */
	Graph2D g1 = left_view.getGraph2D();
	Graph2D g2 = view.getGraph2D();
	
	Rectangle bbox1 = g1.getBoundingBox();
	Rectangle bbox2 = g2.getBoundingBox();
	
	Rectangle union = bbox1.union(bbox2);
	left_view.zoomToArea(union.x, union.y, union.width, union.height);
	view.zoomToArea(union.x, union.y, union.width, union.height);
	view.updateView();
	left_view.updateView();	
}
private void setBottomViewsPolicy()
{
	view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	view.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	left_view.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	left_view.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

	//Because yfiles doesn't provide access to scollbar directly
	//Here we extract all the view components and figure out which ones are scrollbar
	//then once we know which components are scollbars, we sync them between two views
	
	final List<JScrollBar> left_scroll_bar = new ArrayList<JScrollBar>();
	final List<JScrollBar> right_scroll_bar = new ArrayList<JScrollBar>();
	
	Component[] left_view_components = left_view.getComponents();
	for(int i=0 ; i<left_view_components.length ; i++)
	{
			if(left_view_components[i] instanceof JScrollBar)
				{
					JScrollBar jsb = (JScrollBar)left_view_components[i];
					left_scroll_bar.add(jsb);
				}
	}
	Component[] right_view_components = view.getComponents();
	for(int i=0 ; i<right_view_components.length ; i++)
	{
		if(right_view_components[i] instanceof JScrollBar)
		{
			JScrollBar jsb = (JScrollBar)right_view_components[i];
			right_scroll_bar.add(jsb);
		}
	}
	
	for (int i = 0; i < left_scroll_bar.size(); i++)
	{
		right_scroll_bar.get(i).setModel(left_scroll_bar.get(i).getModel());	
		final JScrollBar right = right_scroll_bar.get(i);
		final JScrollBar left = left_scroll_bar.get(i);
		
		//-------------------
		right.addAdjustmentListener(new AdjustmentListener() {
			 
	        public void adjustmentValueChanged(AdjustmentEvent e) {
	            if (!e.getValueIsAdjusting()) {
	                return;
	            }
	            
	            int range1 = right.getMaximum() - right.getMinimum()
	                    - right.getModel().getExtent();
	            int range2 = left.getMaximum() - left.getMinimum()
	                    - left.getModel().getExtent();

	            float percent = (float) (right.getValue()) / range1;

	            int newVal = (int) (percent * range2);

	            left.setValue(newVal);
	        }

	    });

		left.addAdjustmentListener(new AdjustmentListener() {

	        public void adjustmentValueChanged(AdjustmentEvent e) {
	            if (!e.getValueIsAdjusting()) {
	                return;
	            }

	            int range1 = right.getMaximum() - right.getMinimum()
	                    - right.getModel().getExtent();
	            int range2 = left.getMaximum() - left.getMinimum()
	                    - left.getModel().getExtent();

	            float percent = (float) left.getValue() / range2;

	            int newVal = (int) (percent * range1);

	            right.setValue(newVal);
	        }

	    });
	}

}



//we created this class to be able to animate rejections
//because otherwise things interfere with each other
private class Reject extends Animation
{
	private List<UUID> animatemorph_node_vids = new ArrayList<UUID>(); //this is for animating changes
	
	private NodeList fade_in_nodes = new NodeList();
	private EdgeList fade_in_edges = new EdgeList();

	private NodeList fade_out_nodes = new NodeList();
	private EdgeList fade_out_edges = new EdgeList();
	
	Reject(Darls darls)
	{
		super(darls);
		reject_and_animate();
	}
	
	public void reject_and_animate()
	{
		if(enable_animation_rejection == false)
		{
			JOptionPane.showMessageDialog(frame,"you have disabled animation rejection, first enable it", "Warning",JOptionPane.OK_OPTION);
			return;
		}
		//first we reject
		rejectCreation(); 
		rejectDeletedSelection();
		rejectMoveColorLabel();
		
		//then we animate
		//animateRejection();
	}
	private void animateRejection()
	{
		//fade out animations
		for (NodeCursor nc = fade_out_nodes.nodes(); nc.ok(); nc.next())
		{
			addFadeOutNode(nc.node(), duration_fade_out_rejection);
		}
		for (EdgeCursor ec = fade_out_edges.edges(); ec.ok(); ec.next())
		{
			addFadeOutEdge(ec.edge(), duration_fade_out_rejection);
		}
		
		//fade in animations
		for (NodeCursor nc = fade_in_nodes.nodes(); nc.ok(); nc.next())
		{
			addFadeInNode(nc.node(), duration_fade_in_rejection);
		}
		for (EdgeCursor ec = fade_in_edges.edges(); ec.ok(); ec.next())
		{
			addFadeInEdge(ec.edge(), duration_fade_in_rejection);
		}
		
		//morph animations
		System.out.println("animate_molist:"+animatemorph_node_vids.get(0));
		addGraphMorph(animatemorph_node_vids, duration_morph_rejection);
		
		//animate rejection

	}
	private void rejectCreation() //nodes and edges
	{
		Graph2D graph = view.getGraph2D();
		//-------------------------------------------
		//Nodes
		//--------------------------------------------
		NodeList nodes = getMissingNodesInLeftGraph();
		for (NodeCursor nc = nodes.nodes(); nc.ok(); nc.next())
		{
			Node n = nc.node();
			updateVersionList(n);
			
			//finally remove the node itself
			graph.removeNode(n);
			
			//add nodes for animation
			fade_out_nodes.add(n);
			//-------------------------------------------
			//Edges
			//--------------------------------------------
			EdgeList edges = getMissingEdgesInLeftGraph();
			for (EdgeCursor ec = edges.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				
				//this is for updatng edgelist after deletion of edge
				UUID vid = VEdge.getEdgeVid(e, v_edge_list_2);
				v_edge_list_2.remove(vid);
	
				//finally remove the node itself
				graph.removeEdge(e);
				
				//add edges for animation
				fade_out_edges.add(e);
			}
			
			view.updateView();
			//update the left graph
			LeftBackGroundDrawer.clear();
			LeftBackGroundDrawer.draw(overlay_graph_in_left_view);
			left_view.updateView();
		}
		
	}
	private void rejectMoveColorLabel()
	{
		System.out.println("this is inside the rejection of move");
		Graph2D sourceGraph = left_view.getGraph2D();
		Graph2D targetGraph = view.getGraph2D();
		//targetGraph.removeGraphListener(graphListener);
	    

		for (NodeCursor nc = sourceGraph.selectedNodes(); nc.ok(); nc.next())
	    {
		 
	      Node n = nc.node();
	      System.out.println("Node "+n);
	      UUID vid = VNode.getNodeVid(n,v_node_list_1);
	      
		  //we do for both that exist in both
	      
	      if (VNode.node_vid_exists(vid,v_node_list_2))
	      {
	    	  animatemorph_node_vids.add(vid);
		      NodeRealizer nr = sourceGraph.getRealizer(n);
		      NodeRealizer new_nr = nr.createCopy();
		      Node node_in_right = VNode.getNodeFromNodeList(vid,v_node_list_2);
		      //targetGraph.createNode(new_nr);
		      targetGraph.setRealizer(node_in_right, new_nr);
		      new_nr.setLabelText(nr.getLabelText());
	      }
	    }
		
		
	    view.updateView();
	    left_view.updateView();

	}

	private void rejectDeletedSelection()
	{
		Graph2D left_graph = left_view.getGraph2D();
		Graph2D right_graph = view.getGraph2D();
		//move the old nodes to the new graph
			
		moveSelectedSubGraph(left_graph, right_graph);
		//set their realizer to default

		//reinsert the missing edges into this graph

	    view.updateView();
		
	}
	private void moveSelectedSubGraph(Graph2D sourceGraph, Graph2D targetGraph)
	{

		Graph2D graph = view.getGraph2D();
		graph.removeGraphListener(graphListener);
		


		EdgeCursor edges = null;
		for (NodeCursor nc = sourceGraph.selectedNodes(); nc.ok(); nc.next())
	    {
	      Node n = nc.node();
	      UUID vid = VNode.getNodeVid(n,v_node_list_1);
	      
	      //if this vid already exists in the right graph, we are not going to add this node
	      if (!VNode.node_vid_exists(vid,v_node_list_2))
	      {
		      //first add it to the new graph
		      //we need to create a copy of the realizer, otherwise they stick together
		      NodeRealizer nr = sourceGraph.getRealizer(n);
		      NodeRealizer new_nr = nr.createCopy();
		      
		      //we need to change the label to match the one in the source graph
		      //when you create a new node it gives it a new label
		      ///---------possibly more stuff is needed to be done here -----------
		      Node new_node = targetGraph.createNode(new_nr);
		      new_nr.setLabelText(nr.getLabelText());
		      view.updateView();
		      //then add it to the v_node_list_2
		      VNode new_vnode = new VNode(new_node,vid);
		      v_node_list_2.add(new_vnode);
		      
		      
		      
		      
		      //identify incoming and outgoing edges
		      
		      edges = n.edges();
	    	  
		      reInsertEdgesInRightGraph(edges);
		      
		      fade_in_nodes.add(new_node);
	      }
	    }
		
		//for edges
		for (EdgeCursor ec = sourceGraph.selectedEdges(); ec.ok(); ec.next())
	    {
	      Edge e = ec.edge();
	      UUID vid = VEdge.getEdgeVid(e,v_edge_list_1);
	      
	      //if this vid already exists in the right graph, we are not going to add this node
	      if (!VEdge.edge_vid_exists(vid,v_edge_list_2))
	      {
		      //first add it to the new graph
		      //we need to create a copy of the realizer, otherwise they stick together
		      EdgeRealizer er = sourceGraph.getRealizer(e);
		      EdgeRealizer new_er = er.createCopy();
		      UUID source_vid = VNode.getNodeVid(e.source(), v_node_list_1);
		      UUID target_vid = VNode.getNodeVid(e.target(), v_node_list_1);
		      Node source_node = VNode.getNodeFromNodeList(source_vid, v_node_list_2);
		      Node target_node = VNode.getNodeFromNodeList(target_vid, v_node_list_2);
		      //we need to change the label to match the one in the source graph
		      //when you create a new node it gives it a new label
		      ///---------possibly more stuff is needed to be done here -----------
		      Edge new_edge = targetGraph.createEdge(source_node, target_node,new_er);
		      view.updateView();
		      //then add it to the v_node_list_2
		      VEdge new_vedge = new VEdge(new_edge,vid);
		      v_edge_list_2.add(new_vedge); 
		      
		      fade_in_edges.add(new_edge);
	      }
	    }
		
		graph.addGraphListener(graphListener);


	}
	private void reInsertEdgesInRightGraph(EdgeCursor ec)
	{
		System.out.println("this is inside the reinsertion of edge");
		Graph2D right_graph = view.getGraph2D();
		System.out.println("-----------INITIAL VIDS---------");
		for (int i = 0; i < v_edge_list_2.size();i++)
		{
			UUID v = ((VEdge) v_edge_list_2.get(i)).uuid;
			System.out.println("vid "+i+"="+v);
		}
		System.out.println("-----------END---------");
		
		if (ec != null)
		{
			for (; ec.ok(); ec.next())
			{
				Edge edge = ec.edge();
				UUID vid = VEdge.getEdgeVid(edge,v_edge_list_1);
				UUID vid_node_source = VNode.getNodeVid(edge.source(),v_node_list_1);
				UUID vid_node_target = VNode.getNodeVid(edge.target(),v_node_list_1);
				Node source_node,target_node;
				source_node = VNode.getNodeFromNodeList(vid_node_source,v_node_list_2);
				target_node = VNode.getNodeFromNodeList(vid_node_target,v_node_list_2);
				if (source_node == null)
				{
					System.out.println("for edge "+edge+" source is null, so continuing");
					for (int i = 0; i < v_edge_list_2.size();i++)
					{
						UUID v = ((VEdge) v_edge_list_2.get(i)).uuid;
						System.out.println("vid "+i+"="+v);
					}
					continue;
				}
				 if (target_node == null)
				 {
						System.out.println("for edge "+edge+" target is null, so continuing");
					 continue;
				 }
				System.out.println("source node:"+source_node+"target:"+target_node);
				//getNodeFromNodeList
				//boolean c1 = !VEdge.edge_vid_exists(vid,v_edge_list_2);
				//boolean c2 = right_graph.contains(source_node);
				//boolean c3 = right_graph.contains(target_node);
				
				//System.out.println("c1:"+c1+"c2:"+c2+"c3"+c3+"vid:"+vid);
				//print all vids
				/*for (int i = 0; i < v_edge_list_2.size();i++)
				{
					int v = ((VEdge) v_edge_list_2.get(i)).vid;
					System.out.println("vid "+i+"="+v);
				}*/
				if(!VEdge.edge_vid_exists(vid,v_edge_list_2) && right_graph.contains(source_node) && right_graph.contains(target_node)) 
				{
					EdgeRealizer er = left_view.getGraph2D().getRealizer(edge).createCopy();
					Edge new_edge = right_graph.createEdge(source_node,target_node,er);
					VEdge new_vedge = new VEdge(new_edge,vid);
					v_edge_list_2.add(new_vedge);
					right_graph.updateViews();
				}
			}	
		}
		view.updateView();
	}
}


private void deselectEverything()
{
	deselectBoth(left_view.getGraph2D());
	
	if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null)
	{
		deselectBoth(RightBackGroundDrawer.backgroundGraph);		
		RightBackGroundDrawer.clear(); //clear the old background graph
		RightBackGroundDrawer.draw(overlay_graph_in_left_view);	//redraw it with gray color
	}
	
	 if (LeftBackGroundDrawer != null && LeftBackGroundDrawer.backgroundGraph != null)
	 {
		deselectBoth(LeftBackGroundDrawer.backgroundGraph);
		LeftBackGroundDrawer.clear(); //clear the old background graph
		LeftBackGroundDrawer.draw(overlay_graph_in_left_view);	//redraw it with gray color
	 }
	
}

static void selectBoth(Graph2D graph)
{
	  NodeCursor nc = graph.nodes(); 
	  graph.setSelected(nc, true);
	  EdgeCursor ec = graph.edges(); 
	  graph.setSelected(ec, true);
	  System.out.println("SELECTED ALL!");
}
static void deselectBoth(Graph2D graph) //nodes and edges
{
	deselectAllNodes(graph);
	deselectAllEdges(graph);

}
static private void deselectAllNodes(Graph2D graph)
{
	  NodeCursor nc = graph.selectedNodes(); 
	  graph.setSelected(nc, false);
	  
}
static private void deselectAllEdges(Graph2D graph)
{
	  EdgeCursor ec = graph.selectedEdges(); 
	  graph.setSelected(ec, false);
}
private NodeList getMissingNodesInLeftGraph()
{
	//selected nodes on the right graph
	NodeCursor nc_selected = view.getGraph2D().selectedNodes();
	NodeList missingNodesinLeftGraph=new NodeList();
	//color the gray background (hollow) nodes on the left
	for(;nc_selected.ok();nc_selected.next())
	{
		UUID vid = VNode.getNodeVid(nc_selected.node(),v_node_list_2);
		Node n = VNode.getNodeFromNodeList(vid,v_node_list_1);
		if(n != null)
		{
			left_view.getGraph2D().setSelected(n, true);
			left_view.updateView();
		}
		if(n == null)
		{
			missingNodesinLeftGraph.add(nc_selected.node());
			//System.out.println("collection------"+c.);
			
		}
	}
	System.out.println("Missing nodes:"+ missingNodesinLeftGraph.size());
	return missingNodesinLeftGraph;
}
private EdgeList getMissingEdgesInLeftGraph()
{
	//selected nodes on the right graph
	EdgeCursor ec_selected = view.getGraph2D().selectedEdges();
	EdgeList missingEdgeInLeftGraph=new EdgeList();
	//color the gray background (hollow) nodes on the left
	for(;ec_selected.ok();ec_selected.next())
	{
		UUID vid = VEdge.getEdgeVid(ec_selected.edge(),v_edge_list_2);
		Edge e = VEdge.getEdgeFromEdgeList(vid,v_edge_list_1);
		if(e != null)
		{
			left_view.getGraph2D().setSelected(e, true);
			left_view.updateView();
		}
		if(e == null)
		{
			missingEdgeInLeftGraph.add(ec_selected.edge());
			//System.out.println("collection------"+c.);
			
		}
	}
	System.out.println("Missing edges:"+ missingEdgeInLeftGraph.size());
	return missingEdgeInLeftGraph;
}


private void checkSelectedThisGraph(int x_click_point, int y_click_point)
{
	/*
	 * if something real is selected on this graph
	 * we need to mark it selected in the other graph as well for gray objects
	 */
	//we need to check it if it's not equal to null because it will crash if the graph is empty
	if (LeftBackGroundDrawer != null)
	{
		double x = view.toWorldCoordX( x_click_point);
		double y = view.toWorldCoordY( y_click_point);
		Graph2D backGraph = LeftBackGroundDrawer.backgroundGraph;
	
		NodeList nl = new NodeList();
		getClickedNodes(x, y, backGraph, nl,true);
		EdgeList el = new EdgeList();
		getClickedEdges(x, y, backGraph, el,true);
	
		LeftBackGroundDrawer.redraw_selected_missing_edges(el,true);
		LeftBackGroundDrawer.redraw_selected_missing_nodes(nl,true);
		left_view.updateView();
	}
	

}
private void getClickedNodes(double x, double y, Graph2D graph, NodeList nl, boolean b) {
	for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
	{
		Node n = nc.node();
		NodeRealizer nr = graph.getRealizer(n);
		
		if (nr.contains(x,y))
		{
			nr.setSelected(b);
			if (nl != null)
				nl.add(n);			
		}
	}
}
private void getClickedEdges(double x, double y, Graph2D graph, EdgeList el, boolean b) {
	for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
	{
		Edge e = ec.edge();
		
		EdgeRealizer er = graph.getRealizer(e);
		er.getPath(); //?????????????????????????????????????????????????????
		if (er.contains(x, y))
		{	er.setSelected(b);
			
			if (el != null)
				el.add(e);
		}
	}
}

//checks whether one gray node or one edge was selected by a mouse click
private void checkSelectedOtherGraph(int x_click_point, int y_click_point)
{
	//we need to check it if it's not equal to null because it will crash if the graph is empty
	if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null)
	{
		double x = view.toWorldCoordX( x_click_point);
		double y = view.toWorldCoordY( y_click_point);
		
		Graph2D left_graph = left_view.getGraph2D();
	
		//deselectBoth(left_graph);	
		
		//first nodes on the left graph
		//we need to make sure everything is deselected on the left
		//before we do this
		//since this is triggered by a mouse click, it's ok that other objects are deselected
		getClickedNodes(x, y, left_graph, null,true);
		
		//now check edges on the left graph
		getClickedEdges(x, y, left_graph, null,true);
	
	
		NodeList nl = new NodeList();
		//RightBackGroundDrawer.clear();
	    //RightBackGroundDrawer.draw();
		Graph2D backGraph = RightBackGroundDrawer.backgroundGraph;
		//deselectBoth(backGraph);	
		
		getClickedNodes(x, y, backGraph, nl, true);
		
		EdgeList el = new EdgeList();	
		getClickedEdges(x, y, backGraph, el, true);
		
		RightBackGroundDrawer.redraw_selected_missing_edges(el,true);
		RightBackGroundDrawer.redraw_selected_missing_nodes(nl,true);
		
		view.updateView();
		left_view.updateView();
	}
}


private void onNewSelectionRectangle(Rectangle sel_rect)
{
	//first we need to translate this selection rectangle to world coordinates
	
	if (RightBackGroundDrawer != null && RightBackGroundDrawer.backgroundGraph != null)
	{
		double x = view.toWorldCoordX(sel_rect.x);
		double y = view.toWorldCoordY(sel_rect.y);
		
		Rectangle world_rect = new Rectangle(sel_rect);
		world_rect.setLocation((int)x,(int) y);
	
		//first de-select all previous nodes
		
		deselectEverything();

		//deselectAll(getGraph2D());

		//select newly selected nodes
		RightBackGroundDrawer.backgroundGraph.selectBoxContent(world_rect);
		//we need to select these nodes on the actual graph as well for copying it to the new graph
		left_view.getGraph2D().selectBoxContent(world_rect);
		
		//this is non-rectangular box method where we simply change the color of the hollow gray nodes
		//first we do it for the nodes that are missing in the new graph but are present in the old one
		 NodeList missingNodesInCopiedGraph = new NodeList(RightBackGroundDrawer.backgroundGraph.selectedNodes());
		 RightBackGroundDrawer.redraw_selected_missing_nodes(missingNodesInCopiedGraph,true);		 
	}
	
	//we need to check for null so that it doesn't crash the first time
	if (LeftBackGroundDrawer != null)
	{
		LeftBackGroundDrawer.redraw_selected_missing_nodes_left(getMissingNodesInLeftGraph(),view);
	}

}
private void drawSelectionRectangle(Rectangle sel_rect)
{
	if(enable_static_rectangle == false)
		return;
	/*
	 * Passing null into this argument clear the selection rectangle
	 */
	MyBackgroundRenderer mbr = MyBackgroundRenderer.newInstance(view, Color.WHITE, true);
	mbr.setText(str_right_view_text);
	mbr.sel_rect = sel_rect;
}
private void updateLeftTextOnLoadFromComicStrip()
{
  	////////////////////set the text////////////////////////
  	int index = first_graph + selected_comic_view_index + 1;
  	str_left_view_text = "Version " + index; 
  	MyBackgroundRenderer.newInstance(left_view, Color.WHITE, true).setText(str_left_view_text);
  	////////////////////////////////////////////////////////
}
private void updateRightTextOnLoadFromComicStrip()
{
  	////////////////////set the text////////////////////////
  	int index = first_graph + selected_comic_view_index + 1;
  	str_right_view_text = "Version " + index;
  	MyBackgroundRenderer.newInstance(view, Color.WHITE, true).setText(str_right_view_text);
  	////////////////////////////////////////////////////////
}

private void loadGraphToLeftView() {
	/*
	 * Clear bubbles
	 */
	if((selected_comic_view_index > 10) || (selected_comic_view_index < 0))
	{
		return;
	}
	clear_before_load_view();
	//also reload the comic views sometime in future
	
	int selected_graph_index = selected_comic_view_index + first_graph;
	//Graph2D graph=comic_view[selected_comic_view_index].getGraph2D();
	updateLeftTextOnLoadFromComicStrip();
	
	setViewToGraph(graphs.get(selected_graph_index),left_view);
	adjustBottomViewsZoomLevel();

	if (!uml_diag_in_versions)
	{
		
		v_node_list_1 = (List <VNode>)v_nodes.get(selected_graph_index);
		v_edge_list_1 = (List <VEdge>)v_edges.get(selected_graph_index);
	}
	//we need to update view so that dimmed nodes become transparent if applicable
	Graph2D right_graph = view.getGraph2D();
	setViewToGraph(right_graph,view);	
	
	System.out.println("Loaded version to left:"+selected_graph_index);
}

public void clear_before_load_view() {
	if (compare_action != null)
	{
		compare_action.post_compare_action_restore();
	}
	//also reload the comic views sometime in future
	bubble_labels.restoreDefaultNodeRealizer(view.getGraph2D());
}

private void loadGraphToRightView() {
	/*
	 * Clear bubbles
	 */
	if((selected_comic_view_index > 10) || (selected_comic_view_index < 0))
	{
		return;
	}
	clear_before_load_view();
	//also reload the comic views sometime in future

	//Graph2D graph=comic_view[selected_comic_view_index].getGraph2D();
	int selected_graph_index = selected_comic_view_index + first_graph;
	Graph2D graph=graphs.get(selected_graph_index);
	
	updateRightTextOnLoadFromComicStrip();
	setEditableVersionView(graph);
	
	setViewToGraph(graph,view);

	adjustBottomViewsZoomLevel();
	
	

	if (!uml_diag_in_versions)
	{
		v_node_list_2 = (List <VNode>)v_nodes.get(selected_graph_index);
		v_edge_list_2 = (List <VEdge>)v_edges.get(selected_graph_index);
	}
	
	//we need to update left view so that dimmed nodes become transparent if applicable
	Graph2D left_graph = left_view.getGraph2D();
	setViewToGraph(left_graph,left_view);
	System.out.println("Loaded version to left:"+selected_graph_index);

}

class ViewMouseMotionListener implements MouseMotionListener, MouseListener, KeyListener
{
	public Rectangle sel_rect = null;
	int x1,y1;
	boolean was_dragged = false;
	boolean shift_pressed = false;
	
	public void mousePressed(MouseEvent e)		
	{	
		//System.out.println("it pressed,"+"x="+e.getX()+"y="+e.getY());
		//System.out.println(e.paramString());



		x1 = e.getX(); 
		y1 = e.getY();
	}
	public void mouseReleased(MouseEvent e) 
	{
		//System.out.println("is released,"+"x="+e.getX()+"y="+e.getY());


		if (was_dragged)
		{
			int x2 = e.getX(); 
			int y2 = e.getY();
			sel_rect = new Rectangle();
			sel_rect.setFrameFromDiagonal(x1, y1, x2, y2);
			was_dragged = false; //reset it
			onNewSelectionRectangle(sel_rect);
			
			//exception here sometimes
			if ((lastGraphEvent != null) &&(lastGraphEvent.getType() == GraphEvent.POST_EVENT || lastGraphEvent.getType() == GraphEvent.PRE_EVENT))
				drawSelectionRectangle(sel_rect);
			
		}
		else
		{
			sel_rect = null;
		}
		
		if (compare_action != null)// && compare_action.compare_obj != null)
		{
			/*compare_action.compare_obj.setNewNodesHighlight(false);
			compare_action.compare_obj.setNewEdgesHighlight(false);*/
			compare_action.post_compare_action_restore();
			
		}
	}
	public void mouseDragged(MouseEvent e)
	{
		//System.out.println("it dragged,"+"x="+e.getX()+"y="+e.getY());

		
		was_dragged = true;
		
	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		//System.out.println("is moved,"+"x="+e.getX()+"y="+e.getY()+"mod:"+e.getModifiersExText(e.getModifiers()));

	}
	@Override
	public void mouseClicked(MouseEvent e) {
		// we will check if either a node or an edge was selected
		// if so, we will take action
		int x = e.getX(); 
		int y = e.getY();
		if (shift_pressed)
			;
		else
			deselectEverything();		
		checkSelectedThisGraph(x,y);
		checkSelectedOtherGraph(x,y);
		
		//if mouse is clicked we can remove the selection rectangle
		drawSelectionRectangle(null);
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		shift_pressed = e.isShiftDown();
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		shift_pressed = false;
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}

 class ComicMouseListener extends MouseAdapter
 {
	 	private int comic_view_index;
	 	
	    ComicMouseListener(int index)
	    {
	    	comic_view_index = index;
	    }
	 

	    public void mouseClicked(MouseEvent e) {
	    	
	    	//super.mouseClicked(e);
	        /*System.out.println("Mouse clicked (# of clicks: "
	                     + e.getClickCount() + ")" + "MouseEvent:"+ e);*/
	    	/*
	    	 * We are going to set the back ground color for the selected view
	    	 * First, we unload the old background color from all comic strips by
	    	 * setting them all to white
	    	 */
	    	if (e.getButton() == 3)
	    	{
	    		PopupMenu popup = comic_popup_menus[comic_view_index];
	    		popup.show(e.getComponent(), e.getX(), e.getY());
	    	}
	    	else
	    	{
		    	selected_comic_view_index = comic_view_index;
		    	System.out.println("First_graph="+first_graph);
		    	/////////////////////////////////////////
		    	//clear all before the comic_view_index
		    	for (int i = 0; i < Math.min(n_comics,graphs.size()); i++)
		    	{
		    		MyBackgroundRenderer.newInstance(comic_view[i], Color.WHITE, false).setText(""+(i+first_graph+1));
			  		comic_view[i].updateView();
		    	}
		    	
		       	//highlight selected
		    	int selected_graph_index = comic_view_index + first_graph;
		    	MyBackgroundRenderer.newInstance(comic_view[comic_view_index], Color.YELLOW, false).setText(""+(selected_graph_index+1));
		  		comic_view[comic_view_index].updateView();
	    	}
	     }
 	 }
	 class BubbleLabels
	 {
		  private List <NodeLabel> bubble_labels_list = null; //this is to display bubble labels from the older version, this needs to be freed before committing
		  
		  //no constructor
	
		  void restoreDefaultNodeRealizer(Graph2D graph)
		  {
			  clear();  
			  setEditableVersionView(graph); //realizers become messed up if this is not called
		  }
		  void clear()
		  {
			  
			//System.out.println("bubble list:"+bubble_labels);
			if (bubble_labels_list == null) return;
			for (int i = 0; i < bubble_labels_list.size(); i++)
			{
				NodeLabel label = bubble_labels_list.get(i);		
				NodeRealizer nr = label.getRealizer();
				//System.out.println("realizer:"+nr.hashCode()+"bubble:"+label.getText());
				nr.removeLabel(label);
			}
			bubble_labels_list = null; //delete it
		  }
		  public void display() //for old version text in graph labels
		  {
	  	      List[] L = VObject.find_uuid(v_node_list_1, v_node_list_2);
	 	      System.out.println("find L"+L[0].size());
		      //new bubble labels which need to be removed afterwards.
		      bubble_labels_list = new ArrayList<NodeLabel>();
		      
	    	  Graph2D graph_v1 = left_view.getGraph2D();
	    	  Graph2D graph_v2 = view.getGraph2D();
	
		      for (int i = 0; i < L[0].size();i++)
	    	  {
	    		  Node[] node_array = null;
	    		  UUID vid = (UUID) L[0].get(i);
	    		  node_array = VNode.getNodesFromVid(vid,v_node_list_1,v_node_list_2);
	    		  NodeRealizer nr1 = graph_v1.getRealizer(node_array[0]); //may want to add g1 as argument later
	    		  NodeRealizer nr2 = graph_v2.getRealizer(node_array[1]);//may want to add view as argument later
	
	    		  String labelText1 = nr1.getLabelText();
	    		  String labelText2 = nr2.getLabelText();
	    		  System.out.println("Label 1,2: " + labelText1 + " " + labelText2);
	    		  if (!(labelText1.equals(labelText2)))
	    		  {
	    			  System.out.println("Label 1 <> Label 2! " + labelText1 + " " + labelText2);
	    			  /*String str = "("+nr1.getLabelText()+")";
	    			  NodeLabel nl = new NodeLabel(str);
	    			  nl.setFontStyle(Font.BOLD | Font.ITALIC);	    			  
	    			  nr2.addLabel(nl);
	    			  nl.setOffset(0, 20);
	    			  nr2.repaint();
	    			  //nr2.removeLabel(nl);
	    			   * 
	    			   */
	
	    			  NodeLabel label = new NodeLabel(labelText1);
	    			  nr2.addLabel(label);
	    		      label.setModel(NodeLabel.FREE);
	    		      label.setOffset(50, 50);
	    		      label.setConfiguration("Bubble");
	    		      label.setLineColor(Color.DARK_GRAY);
	    		      label.setBackgroundColor(new Color(202,227,255));
	    		      nr2.repaint();
	    		      bubble_labels_list.add(label);
	    		  }    		  
	    	  }			
	  	}
	 }
	@Override
	public void layoutAction() {
		// TODO Auto-generated method stub
	      //for(EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
	      //{
	      //  Edge e = ec.edge();
	      //  EdgeRealizer er = graph.getRealizer(e);
	      //if(er.labelCount() >= 2)
	      //{
	      //  er.getLabel(0).setPreferredPlacement((byte)(EdgeLabel.PLACE_AT_SOURCE | EdgeLabel.PLACE_RIGHT_OF_EDGE));
	      //  er.getLabel(1).setPreferredPlacement((byte)(EdgeLabel.PLACE_AT_TARGET | EdgeLabel.PLACE_RIGHT_OF_EDGE));
	      //}
	      //}
	      //layoutModule.start(graph);
	    	
	      
	    	
	      if (graphs.size() > 0)
	      {
		      for (int i = 0; i < graphs.size(); i++)	    	  
		    	  layoutModule.start(graphs.get(i));
		      
		      /*
		      ///the following lines were CODE DUPLICATED from LOADVERSION stuff
		      load_comic_strip_views();
			  //setting the view-only view
			  Graph2D graph_v1 = graphs.get(0);
			  setViewToGraph(graph_v1,left_view);	
				
			  Graph2D last_graph = graphs.get(graphs.size()-1);
			  setEditableVersionView(last_graph);
				
			  //adjust zoom level and pan for left and right views			  
			  adjustBottomViewsZoomLevel();*/	
			  updateViewOnFileAction();
	      }

	}
	@Override
	public void layoutOptionAction() {
		// TODO Auto-generated method stub
	      layoutModule.getOptionHandler().showEditor();

		
	}
	@Override
	public void reLayoutAction(String policy) {
		// TODO Auto-generated method stub
		  /**
		   * before doing the right view, we need to create a new graph
		   * which will be based on incrementally re-layed out first graph
		   * by adding/deleting nodes from it and then we can incrementally
		   * relayout the right graph 
		   */
		  

  	      //Switch edit mode here
  	   
	  	  //layout the left view first
		  //IncrementalHierarchicLayouterModule lihlm = new IncrementalHierarchicLayouterModule(left_view);
		  
		  //IncrementalHierarchicLayouterModule rihlm = new IncrementalHierarchicLayouterModule(view);


		  /*//causes crash if you dont mess with these variables
		  overlay_graph_in_left_view = false;
		  overlay_graph_in_right_view = false;
		  */
		  
		  //////////////////////////////////////////////////////////////////////////////////////////
		  Graph2D left_graph = left_view.getGraph2D();
		  final Graph2D right_graph = view.getGraph2D();
		      
		  if (!uml_diag_in_versions) //graphs
		  {
			  //L[0] - stores those are both in new and old version
			  //L[1] - stores those are only in the new version
			  //L[2] - stores those are only in the old version
			  
		    		  

			  List[] L = VObject.find_uuid(v_node_list_1, v_node_list_2);
			  List[] E = VObject.find_uuid(v_edge_list_1, v_edge_list_2);
			  //add missing nodes
			  NodeList missing_nodes_added_to_right_graph = new NodeList();
			  List<VNode> missing_v_nodes_added_to_right_graph = new ArrayList<VNode>();
			  for (int i = 0; i < L[2].size(); i++)
	  		  {
				  UUID uuid = (UUID) L[2].get(i);
				  Node n = VNode.getNodeFromNodeList(uuid, v_node_list_1);
				  Node new_node = n.createCopy(right_graph);
				  NodeRealizer nr = left_graph.getRealizer(n);
				  right_graph.setRealizer(new_node, nr.createCopy());
				  missing_nodes_added_to_right_graph.add(new_node);
				  VNode new_v_node = new VNode(new_node,uuid);
				  missing_v_nodes_added_to_right_graph.add(new_v_node);
				  v_node_list_2.add(new_v_node); //all this needs to be undone
				  //System.out.println("NEW IN RIHGT:"+right_graph.getRealizer(new_node).getLabelText());
	  		  }
	  		  
			  //add missing edges
			  EdgeList missing_edges_added_to_right_graph = new EdgeList();
			  List<VEdge> missing_v_edges_added_to_right_graph = new ArrayList<VEdge>();

			  for (int i = 0; i < E[2].size(); i++)
	  		  {
				  UUID uuid = (UUID) E[2].get(i);
				  Edge e = VEdge.getEdgeFromEdgeList(uuid, v_edge_list_1);
				  Node source = e.source();
				  Node target = e.target();
				  UUID source_uuid = VNode.getNodeVid(source, v_node_list_1);
				  UUID target_uuid = VNode.getNodeVid(target, v_node_list_1);
				  
				  Node new_source = VNode.getNodeFromNodeList(source_uuid, v_node_list_2);
				  Node new_target = VNode.getNodeFromNodeList(target_uuid, v_node_list_2);
				  Edge new_edge = e.createCopy(right_graph, new_source, new_target); //: Both endpoints must reside in this graph.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! if opened from fresh, no problem
				  EdgeRealizer er = left_graph.getRealizer(e);
				  EdgeRealizer new_er = er.createCopy();
				  right_graph.setRealizer(new_edge,new_er);
				  missing_edges_added_to_right_graph.add(new_edge);
				  VEdge new_v_edge = new VEdge(new_edge,uuid);
				  v_edge_list_2.add(new_v_edge);
				  missing_v_edges_added_to_right_graph.add(new_v_edge);
	  		  }

			  if (policy.equals(DarlsBase.groupPolicy[2]))
			  {
					  
				  layoutModule.start(right_graph);
				  
			  }
			  else //relative
			  {
				  
				  IncrementalHierarchicLayouterModule rihlm = new IncrementalHierarchicLayouterModule(view);
				  
				  NodeList common = new NodeList();
				  for (Iterator iter = L[0].iterator();iter.hasNext();)
				  {
					  UUID uuid = (UUID)iter.next();
					  Node n = VNode.getNodeFromNodeList(uuid, v_node_list_2);
					  //right_graph.setSelected(n, true);
					  common.add(n);
				  }
				  
				  //fix common and new nodes (belonging to this graph) and calculate layout first
				  NodeList new_nodes = new NodeList();
				  for (Iterator iter = L[1].iterator();iter.hasNext();)
				  {
					  UUID uuid = (UUID)iter.next();
					  Node n = VNode.getNodeFromNodeList(uuid, v_node_list_2);
					  new_nodes.add(n);

				  }
				  //---------------------------
				  
				  /*NodeList nodes_from_left = new NodeList();
				  for (Iterator iter = missing_v_nodes_added_to_right_graph.iterator();iter.hasNext();)
				  {
					  VNode vn = (VNode) iter.next();
					  nodes_from_left.add(vn.node);
				  }*/
				  
				  //GraphHider gh = new GraphHider(right_graph);
				  //gh.hide(nodes_from_left.nodes());

				  rihlm.fixNodes(common.nodes());
				  rihlm.fixNodes(new_nodes.nodes());
				  rihlm.calcIncLayout();
				  //rihlm.optimizeNodes(nodes_from_left.nodes());

				  
				  
				  
				  //unhide nodes added from the left graph and optimize them
				  //gh.unhideAll();
				  

				  
				  //rihlm.calcIncLayout();
				  
				  
				  
				  /*OptionHandler oh = layoutModule.getOptionHandler();
				  String group_layout_policy = (String) oh.get("GROUP_LAYOUT_POLICY");
				  oh.set("GROUP_LAYOUT_POLICY", DarlsBase.groupPolicy[0]);
				  
				  
				  HierarchyManager hierarchy = new HierarchyManager(right_graph);
				  hierarchy.addHierarchyListener(new GroupNodeRealizer.StateChangeListener());
				  
				  //right_graph.firePreEvent();
				  
				  NodeList common = new NodeList();
				  for (Iterator iter = L[0].iterator();iter.hasNext();)
				  {
					  UUID uuid = (UUID)iter.next();
					  Node n = VNode.getNodeFromNodeList(uuid, v_node_list_2);
					  //right_graph.setSelected(n, true);
					  common.add(n);
				  }
				  
				  //NodeList subNodes = new NodeList(right_graph.selectedNodes());
			      Node nca = hierarchy.getNearestCommonAncestor(common);
			      Node groupNode = hierarchy.createGroupNode(nca);
			      hierarchy.groupSubgraph(common, groupNode);
			      //right_graph.unselectAll();
			      
				  
				  layoutModule.start(right_graph);
				  hierarchy.ungroupSubgraph(common);
				  hierarchy.removeGroupNode(groupNode);
				  
				  //restore the policy
				  oh.set("GROUP_LAYOUT_POLICY",group_layout_policy);
				*/  
				
				  
			  }
			  
			  //copy the graph to the left view and put edges
			  Graph2D graph = new Graph2D();
			  
			  v_node_list_1.clear();
			  v_edge_list_1.clear();
			  
			  for (Iterator vnli = v_node_list_2.iterator(); vnli.hasNext();)
			  {
				  VNode vn = (VNode) vnli.next();
				  //create new node and vnode and add to the graph on the left
				  
				  if (!L[1].contains(vn.uuid))
				  {
					  Node n = vn.node;
					  NodeRealizer nr = right_graph.getRealizer(n);
					  Node new_n = graph.createNode(nr.createCopy());
					  VNode new_vn = new VNode(new_n, vn.uuid);
					  v_node_list_1.add(new_vn);
				  }
			  }
			  //now lets deal with the adjacent edges
			  for (Iterator veli = v_edge_list_2.iterator(); veli.hasNext();)			  
			  {
				  VEdge ve = (VEdge) veli.next();
				  Edge e = ve.edge;
				  if (!E[1].contains(ve.uuid))
				  {
					  Node right_source_node = e.source();
					  Node right_target_node = e.target();
					  
					  UUID source_uuid = VNode.getNodeVid(right_source_node, v_node_list_2);
					  UUID target_uuid = VNode.getNodeVid(right_target_node, v_node_list_2);
					  
					  Node left_source_node = VNode.getNodeFromNodeList(source_uuid, v_node_list_1);
					  Node left_target_node = VNode.getNodeFromNodeList(target_uuid, v_node_list_1);
					  
					  EdgeRealizer er = right_graph.getRealizer(ve.edge);
					  Edge new_e = graph.createEdge(left_source_node, left_target_node, er.createCopy()); //NullPointerException
					  VEdge new_ve = new VEdge(new_e,ve.uuid);
					  v_edge_list_1.add(new_ve);
				  }
			  }
			  
			  
			  left_view.setGraph2D(graph);
			  
			  //remove added nodes and edges from the right graph
			  for (Iterator mvn = missing_v_nodes_added_to_right_graph.iterator(); mvn.hasNext();)
			  {
				  VNode vn = (VNode) mvn.next();
				  Node n = vn.node;
				  right_graph.removeNode(n);
				  v_node_list_2.remove(vn);
			  }
			  //EdgeCursor ec = right_graph.edges();
			  for (Iterator mve = missing_v_edges_added_to_right_graph.iterator(); mve.hasNext();)
			  {
				  VEdge ve = (VEdge) mve.next();
				  Edge e = ve.edge;
				  //edge may not exist in the graph, if at least a source or a target node was deleted in the previous step
				  if (right_graph == (Graph2D) e.getGraph())
				  {
					  right_graph.removeEdge(e); 
					  					
				  }
				  v_edge_list_2.remove(ve);
				  
			  } 
		  }
		  else //class diagram
		  {
	 			//appear only either in left or right graph. objects are taken from only in right graph for animation
	  			NodeList n_in_left = new NodeList();
	  			NodeList n_in_right = new NodeList();
	  			
	  			//appear in both, but we need objects in both graphs
	  			NodeList n_in_right_both = new NodeList();
	  			NodeList n_in_left_both = new NodeList();

	  			//----------------------------------------------------------------------------
	  			//Nodes
	  			//----------------------------------------------------------------------------
	  			//determine only in left
	  			for (NodeCursor left_nc = left_graph.nodes(); left_nc.ok(); left_nc.next()) 
	  			{
	  				  //---------------------------------------------------
	  				  NodeRealizer left_realizer = left_graph.getRealizer(left_nc.node());
	  				  NodeLabel left_node_label = left_realizer.getLabel();

	  				  boolean found = false;
	  				  for (NodeCursor right_nc = right_graph.nodes(); right_nc.ok(); right_nc.next()) 
	  				  {
	  					  NodeRealizer realizer = right_graph.getRealizer(right_nc.node());
	  					  //---------------------------------------------------
	  					  String str_label_text = realizer.getLabelText();
	  					  String str_left_label_text = left_realizer.getLabelText();
	  					  if (str_label_text.equals(str_left_label_text))
	  					  {
	  						  n_in_right_both.add(right_nc.node());
	  						  n_in_left_both.add(left_nc.node());
	  						  found = true;
	  					  }
	  					  
	  				  }
	  				  if (!found)
	  					  n_in_left.add(left_nc.node());
	  				
	  			}
	  			//now determine in right only
	  			
	  			for (NodeCursor nc = right_graph.nodes(); nc.ok(); nc.next()) 
	  			{
	  				NodeRealizer realizer = right_graph.getRealizer(nc.node());
	  				boolean found = false;
	  				for (NodeCursor left_nc = left_graph.nodes(); left_nc.ok(); left_nc.next()) 
	  				{
	  					NodeRealizer left_realizer = left_graph.getRealizer(left_nc.node());				
	  					String str_label_text = realizer.getLabelText();
	  				    String str_left_label_text = left_realizer.getLabelText();
	  				    if (str_label_text.equals(str_left_label_text))
	  					  {
	  						  
	  						  found = true;
	  					  }
	  				
	  				}
	  	 		    if (!found)
	  	 		    {
	  	 		       Node n = nc.node();
	  				   n_in_right.add(n);


	  	 		    }
	  			}
	  			//----------------------------------------------------------------------------
	  			//Edges
	  			//----------------------------------------------------------------------------
	  			//appear only either in left or right graph. objects are taken from only in right graph for animation
	  			EdgeList e_in_left = new EdgeList();
	  			EdgeList e_in_right = new EdgeList();
	  			
	  			//appear in both, but we need objects in both graphs
	  			EdgeList e_in_right_both = new EdgeList();
	  			EdgeList e_in_left_both = new EdgeList();
	  			
	  			//determine only in left
	  			for (EdgeCursor left_ec = left_graph.edges(); left_ec.ok(); left_ec.next()) 
	  			{
	  				  //---------------------------------------------------
	  				  //EdgeRealizer left_realizer = left_graph.getRealizer(left_ec.edge());
	  				  //NodeLabel left_node_label = left_realizer.getLabel();
	  				  
	  				  String [] left_node_text = DarlsUtil.getSourceAndTargetLabelText(left_ec.edge(),left_view.getGraph2D());
	  				  
	  				  boolean found = false;
	  				  for (EdgeCursor ec = right_graph.edges(); ec.ok(); ec.next()) 
	  				  {
	  					  String [] right_node_text = DarlsUtil.getSourceAndTargetLabelText(ec.edge(),view.getGraph2D());
	  					  
	  					  if (left_node_text[0].equals(right_node_text[0]) && left_node_text[1].equals(right_node_text[1]))
	  					  {
	  						  e_in_right_both.add(ec.edge());
	  						  e_in_left_both.add(left_ec.edge());
	  						  found = true;
	  					  }
	  					  
	  				  }
	  				  if (!found)
	  					  e_in_left.add(left_ec.edge());
	  				
	  			}
	
	  			for (EdgeCursor ec = right_graph.edges(); ec.ok(); ec.next()) 
	  			{
	  				//NodeRealizer realizer = graph.getRealizer(nc.node());
	  	 		    String [] right_node_text = DarlsUtil.getSourceAndTargetLabelText(ec.edge(),view.getGraph2D());
	  				boolean found = false;
	  				for (EdgeCursor left_ec = left_graph.edges(); left_ec.ok(); left_ec.next()) 
	  				{
	  	  			    String [] left_node_text = DarlsUtil.getSourceAndTargetLabelText(left_ec.edge(),left_view.getGraph2D());

	  	  			  if (left_node_text[0].equals(right_node_text[0]) && left_node_text[1].equals(right_node_text[1]))
	  				  {
	  						  found = true;
	  				  }
	  				
	  				}
	  	 		    if (!found)
	  	 		    {
	  	 		       Edge e = ec.edge();
	  				   e_in_right.add(e);
	  	 		    }
	  			}
	  			 //add missing nodes to the right
				 //System.out.println("size:"+n_in_left.size());
	  			  List<Node> added_nodes = new ArrayList<Node>();
	  			  for (Iterator iter = n_in_left.iterator(); iter.hasNext();)
	  			  {
	  				  Node n = (Node) iter.next();
	  				  NodeRealizer nr = left_graph.getRealizer(n);
	  				  Node new_node = n.createCopy(right_graph);
	  				  NodeRealizer new_nr = nr.createCopy();
	  				  right_graph.setRealizer(new_node, new_nr);
	  				  added_nodes.add(new_node);
	  				  //System.out.println("left:"+nr.getLabelText());
	  			  }
	  			  
	  			  //add missing edges to the right
	  			  List<Edge> added_edges = new ArrayList<Edge>();
	  			  for (Iterator iter = e_in_left.iterator(); iter.hasNext();)
	  			  {
	  				  Edge e = (Edge) iter.next();
	  				  EdgeRealizer er = left_graph.getRealizer(e);
	  				  
	  				  Node source_node = e.source();
	  				  Node target_node = e.target();
	  				  
	  				  NodeRealizer source_node_realizer = left_graph.getRealizer(source_node);
	  				  NodeRealizer target_node_realizer = left_graph.getRealizer(target_node);
	  				  
	  				  String source_node_text = source_node_realizer.getLabelText();
	  				  String target_node_text = target_node_realizer.getLabelText();
	  				  
	  				  Node new_source_node = null;
	  				  Node new_target_node = null;
	  				  
	  				  for (NodeCursor nc = right_graph.nodes(); nc.ok(); nc.next())
	  				  {
	  					 Node n = nc.node();
	  					  
	  					 NodeRealizer realizer = right_graph.getRealizer(n);				
						 String str_label_text = realizer.getLabelText();
						
						 boolean found_source = false;
						 boolean found_target = false;
						 
						 if (str_label_text.equals(source_node_text))
						 {
							 found_source = true;
							 new_source_node = n;
						 }
						 else if (str_label_text.equals(target_node_text))
						 {
							 found_target = true;
							 new_target_node = n;
						 }
						 if (found_source && found_target)
							 break;
	  				  }
	  				  Edge new_edge = e.createCopy(right_graph, new_source_node, new_target_node);
	  				  added_edges.add(new_edge);
	  			  }
	  			  
  			
	  			/////////////////////////////////////////////////////////////////////
	  			//LAYOUT STEP
	  			if (policy.equals(DarlsBase.groupPolicy[2])) //complete relayout
				{
						  
					layoutModule.start(right_graph);
					  
				}
				else //relative relayout
				{
					IncrementalHierarchicLayouterModule rihlm = new IncrementalHierarchicLayouterModule(view);

					rihlm.fixNodes(n_in_right_both.nodes());
					rihlm.fixNodes(n_in_right.nodes());
					rihlm.calcIncLayout();
				}
	  			/////////////////////////////////////////////////////////////////////
	  			Graph2D new_left_graph = (Graph2D) right_graph.createCopy();
	  			/////////////////////////////////////////////////////
	  			//remove added stuff from the right graph after layout
	  			for (Iterator iter = added_edges.iterator(); iter.hasNext();)
	  			{
	  				right_graph.removeEdge((Edge)iter.next());
	  			}
	  			
	  			for (Iterator iter = added_nodes.iterator(); iter.hasNext();)
	  			{
	  				right_graph.removeNode((Node)iter.next());
	  			}
	  			
	  			///////////////////////////////////////////////////////
	  			//remove added stuff from the left graph
	  			//edges
	  			for (EdgeCursor ec = new_left_graph.edges(); ec.ok(); ec.next())	  				
	  			{
	  				Edge e = ec.edge();
	  				Node source = e.source();
	  				Node target = e.target();
	  				
	  				NodeRealizer source_realizer = new_left_graph.getRealizer(source);
	  				NodeRealizer target_realizer = new_left_graph.getRealizer(target);
	  				
	  				String source_text = source_realizer.getLabelText();
	  				String target_text = target_realizer.getLabelText();

	  				for (Iterator iter = e_in_right.iterator(); iter.hasNext();)
					{
	  					Edge right_e = (Edge) iter.next();
						
	  					Node right_source = right_e.source();
	  					Node right_target = right_e.target();
	  					
	  					NodeRealizer right_realizer_source = right_graph.getRealizer(right_source);
						String str_label_text_right_source = right_realizer_source.getLabelText();
						
	  					NodeRealizer right_realizer_target = right_graph.getRealizer(right_target);
						String str_label_text_right_target = right_realizer_target.getLabelText();
						
						boolean source_found = false;
						boolean target_found = false;
						
						if (str_label_text_right_source.equals(source_text))
						{
							source_found = true;
						}
						
						if(str_label_text_right_target.equals(target_text))
						{
							target_found = true;
						}
						if (source_found && target_found)
						{
							new_left_graph.removeEdge(e);
							break;
						}
					}
	  				
	  			}
	  			//nodes
	  			for (NodeCursor nc = new_left_graph.nodes(); nc.ok();nc.next())
	  			{
	  				Node n = nc.node();
					NodeRealizer realizer = new_left_graph.getRealizer(n);				
					String str_label_text = realizer.getLabelText();
	  				
					boolean found = false;
					for (Iterator iter = n_in_right.iterator(); iter.hasNext();)
					{
		  				Node right_n = (Node) iter.next();
						NodeRealizer right_realizer = right_graph.getRealizer(right_n);
						String str_label_text_right = right_realizer.getLabelText();
						System.out.println("left_text:"+str_label_text+",right_text:"+str_label_text_right);
						if (str_label_text_right.equals(str_label_text))
						{
							found = true;
							break;
						}
					}
					if (found)
					{
						new_left_graph.removeNode(n);
					}					
	  			}
	  			left_view.setGraph2D(new_left_graph);
		  }
		  //////////////////////////////////////////////////////////////////////////////////////////
  	  
	 
	  adjustView(view);
	  adjustView(left_view);

	  //these line makes the graphs dissapear	  
	  adjustBottomViewsZoomLevel();
	}
	private void try_submit() {
		//try
		//{
			user_study.submit();			
		//}
		//catch (Exception ex) 
		//{
			//System.out.println(ex+" user_study object is a null!");

		//}
	}
	@Override
	protected void give_up_trial() {
		// TODO Auto-generated method stub
		if (user_study != null)
		{
			System.out.println("giving up on trial");
			user_study.resultSubmitted(true);
		}
	}	
};


abstract class Compare extends Animation
	{

		Graph2DView view = null;
		Graph2DView left_view = null;
		Darls darls = null;
		
		Compare(Darls _darls)
		{
			super(_darls);
			darls = _darls;
			view = darls.view;
			left_view = darls.left_view;
			
		}

		
		public void post_animation_cleanup()
		{

		}
		protected abstract void compare();
		
		NodeList newNodes = null;
		EdgeList newEdges = null;
		List<Color> newNodeColors = null;
		List<Color> newEdgeColors = null;
		
		NodeList commonNodes = null;
		EdgeList commonEdges = null;
		List<Color> commonNodeColors = null;
		List<Color> commonEdgeColors = null;
		final static Color HIGHLIGHT_COLOR = Color.BLUE;  
		
		public void setNodesHighlight(boolean highlight, boolean b_new_nodes) // true new false common
		{
			NodeList node_list = null;
			List<Color> nodeColorList = null;
			
			if (b_new_nodes)
			{
				node_list = newNodes;
	  			nodeColorList = newNodeColors;
			}
			else
			{
				node_list = commonNodes;
	  			nodeColorList = commonNodeColors;
			}
				
			//if (newNodes == null) return;
			Graph2D graph = view.getGraph2D();
			int i = 0;
			for (NodeCursor nc = node_list.nodes(); nc.ok(); nc.next()) 
		    {	
				Node n = nc.node();
		    	NodeRealizer nr = graph.getRealizer(n);
		    	if (highlight)
		    	{
		    		nr.setFillColor(HIGHLIGHT_COLOR);
		    		//for experiment only
		    		nr.setFillColor2(HIGHLIGHT_COLOR); 
		    		nr.setLineColor(Color.BLACK);
		    		nr.getLabel().setTextColor(Color.WHITE);		    		
		    	}
		    	else
		    	{
		    		nr.setFillColor(nodeColorList.get(i++));
		    	}
		    }
			view.updateView();
		}

		public void setEdgesHighlight(boolean highlight, boolean b_new_edges) // true new false common)
		{
			//if (newEdges == null) return;
			EdgeList edge_list = null;
			List<Color> edgeColorList = null;
			
			if (b_new_edges)
			{
				edge_list = newEdges;
				edgeColorList = newEdgeColors;
			}
			else
			{
				edge_list = commonEdges;
				edgeColorList = commonEdgeColors;
			}
			
			Graph2D graph = view.getGraph2D();
			int i = 0;
			for (EdgeCursor ec = edge_list.edges(); ec.ok(); ec.next()) 
		    {	
				Edge e = ec.edge();
		    	EdgeRealizer er = graph.getRealizer(e);
		    	if (highlight)
		    	{
		    		//Arrow a = er.getArrow();
		    		er.setLineColor(HIGHLIGHT_COLOR);
		    	}
		    	else
		    	{
		    		er.setLineColor(edgeColorList.get(i++));
		    	}
		    }
			view.updateView();
		}

			
		protected void animateComparison() {
			//create sequence of morph, fade out and fade in			
			//CompositeAnimationObject animation_sequence = AnimationFactory.createSequence();

			CompositeAnimationObject animation_concurrency = AnimationFactory.createConcurrency();
			
			if (!fade_in_comp_anim_obj.isEmpty())
				animation_concurrency.addAnimation(fade_in_comp_anim_obj);
			if (!fade_out_comp_anim_obj.isEmpty())
				animation_concurrency.addAnimation(fade_out_comp_anim_obj);			
			if (!morph_comp_anim_obj.isEmpty())
				animation_concurrency.addAnimation(morph_comp_anim_obj);
				
			/*if (!fade_in_comp_anim_obj.isEmpty()) 
			{
				animation_concurrency.addAnimation(fade_in_comp_anim_obj);
				if (!fade_out_comp_anim_obj.isEmpty()) 
				{
					animation_concurrency.addAnimation(fade_out_comp_anim_obj);
				}
				animation_sequence.addAnimation(animation_concurrency);
			}
			else
			{
				if (!fade_out_comp_anim_obj.isEmpty())
				{
					animation_sequence.addAnimation(fade_out_comp_anim_obj);
				}
			}
			

			if (!morph_comp_anim_obj.isEmpty()) {
			animation_sequence.addAnimation(morph_comp_anim_obj);
			System.out.println("morph empty="+morph_comp_anim_obj.isEmpty());
			}*/


			//player.animate(animation_sequence);  			
			player.animate(animation_concurrency);

		}


	}
	
	class CompareUML extends Compare
	{
		private List<String> preserved_attributes_list = null;
	  	private List<String> preserved_methods_list = null;
	  	
	  	Hashtable edge_realizers_color_edges = null;
	  	
		CompareUML(Darls darls)
		{
			super(darls);
			compare();
		}
		
		private void addUMLMorph(NodeList nl_left, NodeList nl_right, long DURATION) {
			  
			  Graph2D graph_v2 =  view.getGraph2D(); //preserving the current graph
			  Graph2D graph_v1 = left_view.getGraph2D();
			  //System.out.println("+++++++the size of list: "+L.size());
			  for (int i = 0; i < nl_left.size(); i++)
			  {
				
				  NodeRealizer nr2 = graph_v2.getRealizer((Node)nl_right.get(i));
				  NodeRealizer nr1 = graph_v1.getRealizer((Node)nl_left.get(i));

				  //nr1.getV
				  NodeRealizer nr2_copy = nr2.createCopy();
				  
				  //the following two lines to fit the content of text versioned realizer. 
				  //this assumes version animation on class diagrams is done in detailed mode.
				  ClassNodeRealizer cnr2_copy = (ClassNodeRealizer) nr2_copy;
				  cnr2_copy.fitContent();
				  nr2.moveBy(nr1.getX(), nr1.getY());
				  nr2.setSize(nr1.getWidth(),nr1.getHeight());
				  nr2.setFillColor(nr1.getFillColor());
				  nr2.setCenter(nr1.getCenterX(),nr1.getCenterY());
  			  if (nr2.getHeight() != nr2_copy.getHeight() ||
  					  nr2.getWidth() != nr2_copy.getWidth() ||
  					  nr2.getCenterX() != nr2_copy.getCenterX() ||
  					  nr2.getCenterY() != nr2_copy.getCenterY())
  			  {
  				  AnimationObject morph_anim_obj = factory.morph(nr2, nr2_copy, ViewAnimationFactory.APPLY_EFFECT,DURATION);
  				  morph_comp_anim_obj.addAnimation(morph_anim_obj);
  			  }
  			  
				 // player.animate(factory.morph(nr2, nr2_copy, ViewAnimationFactory.APPLY_EFFECT,DURATION));
			  }
			  //it seems like there is no need to edge animation at this point.
		  }  
	    //returns source and target label text

		protected void compare()
		{
			Graph2D left_graph = left_view.getGraph2D();
			Graph2D right_graph = view.getGraph2D();
			
			//appear only either in left or right graph. objects are taken from only in right graph for animation
			NodeList n_in_left = new NodeList();
			NodeList n_in_right = new NodeList();
			
			//appear in both, but we need objects in both graphs
			NodeList n_in_right_both = new NodeList();
			NodeList n_in_left_both = new NodeList();

			//----------------------------------------------------------------------------
			//Nodes
			//----------------------------------------------------------------------------
			//determine only in left
			for (NodeCursor left_nc = left_graph.nodes(); left_nc.ok(); left_nc.next()) 
			{
				  //---------------------------------------------------
				  NodeRealizer left_realizer = left_graph.getRealizer(left_nc.node());
				  NodeLabel left_node_label = left_realizer.getLabel();

				  boolean found = false;
				  for (NodeCursor right_nc = right_graph.nodes(); right_nc.ok(); right_nc.next()) 
				  {
					  NodeRealizer realizer = right_graph.getRealizer(right_nc.node());
					  //---------------------------------------------------
					  String str_label_text = realizer.getLabelText();
					  String str_left_label_text = left_realizer.getLabelText();
					  if (str_label_text.equals(str_left_label_text))
					  {
						  n_in_right_both.add(right_nc.node());
						  n_in_left_both.add(left_nc.node());
						  found = true;
					  }
					  
				  }
				  if (!found)
					  n_in_left.add(left_nc.node());
				
			}
			//now determine in right only
			newNodes = new NodeList();
			newNodeColors = new ArrayList<Color>();
			
			for (NodeCursor nc = right_graph.nodes(); nc.ok(); nc.next()) 
			{
				NodeRealizer realizer = right_graph.getRealizer(nc.node());
				boolean found = false;
				for (NodeCursor left_nc = left_graph.nodes(); left_nc.ok(); left_nc.next()) 
				{
					NodeRealizer left_realizer = left_graph.getRealizer(left_nc.node());				
					String str_label_text = realizer.getLabelText();
				    String str_left_label_text = left_realizer.getLabelText();
				    if (str_label_text.equals(str_left_label_text))
					  {
						  
						  found = true;
					  }
				
				}
	 		    if (!found)
	 		    {
	 		       Node n = nc.node();
				   n_in_right.add(n);

				   //this is for coloring new stuff after animation is over
				   newNodes.add(n);
				   NodeRealizer nr = right_graph.getRealizer(n);
				   newNodeColors.add(nr.getFillColor());

	 		    }
			}
			//----------------------------------------------------------------------------
			//Edges
			//----------------------------------------------------------------------------
			//appear only either in left or right graph. objects are taken from only in right graph for animation
			EdgeList e_in_left = new EdgeList();
			EdgeList e_in_right = new EdgeList();
			
			//appear in both, but we need objects in both graphs
			EdgeList e_in_right_both = new EdgeList();
			EdgeList e_in_left_both = new EdgeList();
			
			//determine only in left
			for (EdgeCursor left_ec = left_graph.edges(); left_ec.ok(); left_ec.next()) 
			{
				  //---------------------------------------------------
				  //EdgeRealizer left_realizer = left_graph.getRealizer(left_ec.edge());
				  //NodeLabel left_node_label = left_realizer.getLabel();
				  
				  String [] left_node_text = DarlsUtil.getSourceAndTargetLabelText(left_ec.edge(),left_view.getGraph2D());
				  
				  boolean found = false;
				  for (EdgeCursor ec = right_graph.edges(); ec.ok(); ec.next()) 
				  {
					  String [] right_node_text = DarlsUtil.getSourceAndTargetLabelText(ec.edge(),view.getGraph2D());
					  
					  if (left_node_text[0].equals(right_node_text[0]) && left_node_text[1].equals(right_node_text[1]))
					  {
						  e_in_right_both.add(ec.edge());
						  e_in_left_both.add(left_ec.edge());
						  found = true;
					  }
					  
				  }
				  if (!found)
					  e_in_left.add(left_ec.edge());
				
			}
			//now determine in right only
			newEdges = new EdgeList();
			newEdgeColors = new ArrayList<Color>();
			for (EdgeCursor ec = right_graph.edges(); ec.ok(); ec.next()) 
			{
				//NodeRealizer realizer = graph.getRealizer(nc.node());
			   
				
	 		   String [] right_node_text = DarlsUtil.getSourceAndTargetLabelText(ec.edge(),view.getGraph2D());

				
				boolean found = false;
				for (EdgeCursor left_ec = left_graph.edges(); left_ec.ok(); left_ec.next()) 
				{
	  			    String [] left_node_text = DarlsUtil.getSourceAndTargetLabelText(left_ec.edge(),left_view.getGraph2D());

	  			  if (left_node_text[0].equals(right_node_text[0]) && left_node_text[1].equals(right_node_text[1]))
				  {
						  
						  found = true;
					  }
				
				}
	 		    if (!found)
	 		    {
	 		       Edge e = ec.edge();
				   e_in_right.add(e);
				   
				   //this is for coloring new stuff after animation is over
				   newEdges.add(e);
				   EdgeRealizer er = right_graph.getRealizer(e);
				   newEdgeColors.add(er.getLineColor());
	 		    }
			}
			//------------------------------------------------------------------------------
			//for those nodes which are in both graphs, we do some text versioning on them
			//------------------------------------------------------------------------------
			saveUMLClassFieldsList(); //save the fields. restore them when animation ends		
			
			for (int i = 0; i < n_in_right_both.size();i++)
			{
				Node right_node = (Node) n_in_right_both.get(i);
				Node left_node = (Node) n_in_left_both.get(i);
				

				
				List <String> right_node_attribute_list = getAttributeList (right_node,right_graph);
				List <String> right_node_method_list = getMethodList (right_node,right_graph);
	    		//node_label_atrribute.setText("<html>"+str+"<br /><s>this is a test</s></html>");

				List <String> left_node_attribute_list = getAttributeList (left_node,left_graph);
				List <String> left_node_method_list = getMethodList (left_node,left_graph);
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				List<String> str_new_attribute_list = getUMLClassLabelTextExcluded(right_node_attribute_list,left_node_attribute_list);
				List<String> str_deleted_attribute_list = getUMLClassLabelTextExcluded(left_node_attribute_list,right_node_attribute_list);
				
				List<String> str_new_method_list = getUMLClassLabelTextExcluded(right_node_method_list,left_node_method_list);
				List<String> str_deleted_method_list = getUMLClassLabelTextExcluded(left_node_method_list,right_node_method_list);
				
				
				/////////////////////////////////////////////////////////////////////////////////
				//For each new or delete attribute we have to modify the label using html tags

				//!!!!I will handle sorting later, stereotypes and resize!!!!!!!!!!!!!!!!1

				ClassNodeRealizer realizer = (ClassNodeRealizer) right_graph.getRealizer(right_node);
				NodeLabel attr_node_label = realizer.getAttributeLabel();

				//Attributes
				List<String> existing_attrib_text_list = getExistingUMLClassText(right_node_attribute_list,left_node_attribute_list);
				String str_attrib_label = createHTMLString(str_new_attribute_list,str_deleted_attribute_list,existing_attrib_text_list);
				attr_node_label.setText(str_attrib_label);
				
				//Methods
				NodeLabel method_node_label = realizer.getMethodLabel();
				List<String> existing_method_text_list = getExistingUMLClassText(right_node_method_list,left_node_method_list);
				String str_method_label = createHTMLString(str_new_method_list,str_deleted_method_list,existing_method_text_list);
				method_node_label.setText(str_method_label);			
				
				System.out.println("Dont forget sorting later, stereotypes and resize (and minimize) on September 3, 2010");
				//first find those are new
			
				
			}
			initCompositeAnimationObjects();
			//--------------------------------------------------------------------------------
			//Animate NODES
			//--------------------------------------------------------------------------------
			//Animate FADE IN,
			//fade-in new nodes
			for (NodeCursor nc = n_in_right.nodes(); nc.ok(); nc.next())
			{
				addFadeInNode( nc.node(), darls.duration_fade_in_comparison );
				//new ones because green
				NodeRealizer nr = right_graph.getRealizer(nc.node());
		    	nr.setFillColor(Color.GREEN);
			}
			//Animate FADE OUT,
			//fade-out missing
			for (NodeCursor nc = n_in_left.nodes(); nc.ok(); nc.next())
			{
				addFadeOutNode( nc.node(), darls.duration_fade_in_comparison );
			}
			//Morph
			addUMLMorph( n_in_left_both, n_in_right_both, darls.duration_morph_comparison);
			
			//--------------------------------------------------------------------------------
			//Animate EDGES
			//--------------------------------------------------------------------------------
			//fade-in new edges
			for (EdgeCursor ec = e_in_right.edges(); ec.ok(); ec.next())
			{
				addFadeInEdge( ec.edge(), darls.duration_fade_in_comparison );
			}
			//Animate FADE OUT,
			//fade-out missing
			for (EdgeCursor ec = e_in_left.edges(); ec.ok(); ec.next())
			{
				addFadeOutEdge( ec.edge(), darls.duration_fade_in_comparison );
			}
			//Morph
			//Fade in edges that changed their type
			edge_realizers_color_edges = new Hashtable();
			for (int i = 0; i < e_in_right_both.size();i++)
			{
				Edge right_edge = (Edge) e_in_right_both.get(i);
				Edge left_edge = (Edge) e_in_left_both.get(i);
				EdgeRealizer right_edge_realizer = view.getGraph2D().getRealizer(right_edge);
				EdgeRealizer left_edge_realizer = left_view.getGraph2D().getRealizer(left_edge);
				Arrow right_arrow = right_edge_realizer.getArrow();
				Arrow left_arrow = left_edge_realizer.getArrow();
				
				if (right_arrow.getType() != left_arrow.getType())
				{	
					//preserve color of the edge
					Color c = right_edge_realizer.getLineColor(); 
					edge_realizers_color_edges.put(right_edge_realizer, c);
					
					right_edge_realizer.setLineColor(Color.red);
					//addFadeInEdge(right_edge, duration_fade_in_comparison );
					//fade in
					EdgeRealizer er = right_graph.getRealizer(right_edge);
					AnimationObject fade_in_anim_obj = factory.fadeIn(er, darls.duration_fade_in_comparison);
					morph_comp_anim_obj.addAnimation(fade_in_anim_obj); //even though this is a fade in it happens during the morph animation
			      
				}
			}
			
			//moved outside the function November 29, 2010
			
			
			 
		}
		/*private void setVisibleEdgeRealizers(EdgeList e_in_left, boolean b) {
			for (EdgeCursor ec = e_in_left.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge(); 
				EdgeRealizer er = view.getGraph2D().getRealizer(e);
				er.setVisible(b);
			}
		}
		private void setVisibleNodeRealizers(NodeList in_left, boolean b) {
			for (NodeCursor nc = in_left.nodes(); nc.ok(); nc.next())
			{
				Node n = nc.node();
				NodeRealizer nr = view.getGraph2D().getRealizer(n);
				nr.setVisible(b);
			}
		}*/
		
		public void restore_edge_color()
		{
			Enumeration keys = edge_realizers_color_edges.keys();
			Graph2D graph = view.getGraph2D();
			while( keys.hasMoreElements() ) 
			{
			  Object key = keys.nextElement();
			  for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next())
			  {
				  Edge e = ec.edge();
				  EdgeRealizer er = graph.getRealizer(e);
				  if (er == key)
				  {
					  Object value = edge_realizers_color_edges.get(key);
					  er.setLineColor((Color) value);
				  }
			  }
			  
			  
			  
			}
		}
		public void post_animation_cleanup() 
		{
			super.post_animation_cleanup();
			// TODO Auto-generated method stub
			restoreUMLClassFieldsList();
			//restore_edge_color();		//instead of calling it here, i moved to compare class 
		}
		//this returns incomplete stuff, you need to append existing list
		private String createHTMLString(List<String> newList, List<String>delList, List<String> existingTextList)
		{
			
			String str_new_label = "<html>"; 
			for (Iterator iterator = newList.iterator();iterator.hasNext(); )
			{
				String str_new = (String) iterator.next();
				str_new_label += "<u><font color = \"#FF0000\">"+ str_new + "</font><br /></u>";
			}
			
			for (Iterator iterator = delList.iterator();iterator.hasNext(); )
			{
				String str_new = (String) iterator.next();
				str_new_label += "<s>"+ str_new + "<br /></s>";
			}
			
			for (Iterator iterator = existingTextList.iterator();iterator.hasNext(); )
			{
				String str_new = (String) iterator.next();
				str_new_label += str_new + "<br />";
			}
			
			str_new_label += "</html>";
			return str_new_label;
		}
		private List<String> getExistingUMLClassText(List<String> searched_list, List<String> excluded_list)
		{
			List<String> returnable = new ArrayList<String>();
			for (Iterator itr = searched_list.iterator();itr.hasNext();)
			{
				String str_searched_object = (String) itr.next();
				for (Iterator itr2 = excluded_list.iterator();itr2.hasNext();)
				{
					String str_excluded_object = (String) itr2.next();
					if (str_searched_object.equals(str_excluded_object))
					{
						returnable.add(str_searched_object);
					}
				}
			}
			return returnable;
		}
		
		private List<String> getUMLClassLabelTextExcluded(List<String> searched_list, List<String> excluded_list)
		{
			List<String> returnable = new ArrayList<String>();
			for (Iterator itr = searched_list.iterator();itr.hasNext();)
			{
				String str_searched_object = (String) itr.next();
				boolean found = false;
				
				for (Iterator itr2 = excluded_list.iterator();itr2.hasNext();)
				{
					String str_excluded_object = (String) itr2.next();
					if (str_searched_object.equals(str_excluded_object))
					{
						found = true;
					}
					
				}
				if (!found) //this was found in right but not in left, so it's new
				{
					//bla bla
					returnable.add(str_searched_object);
				}
				
			}
		
			return returnable;
			
		}
		private void restoreUMLClassFieldsList()
		{
			  Graph2D graph = view.getGraph2D();
			  
			  int i = 0;
			  for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
			  {
				  ClassNodeRealizer realizer = (ClassNodeRealizer) graph.getRealizer(nc.node());
				  NodeLabel node_label_atrribute = realizer.getAttributeLabel();
				  
				  node_label_atrribute.setText( preserved_attributes_list.get(i) );

				  
				  NodeLabel node_label_method = realizer.getMethodLabel();
				  node_label_method.setText(preserved_methods_list.get(i));

				  i++;
			  }
		}
		
		private void saveUMLClassFieldsList()
		{
			  Graph2D graph = view.getGraph2D();
			  
			  preserved_attributes_list = new ArrayList<String>();
			  preserved_methods_list = new ArrayList<String>();
				  
			  for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next())
			  {
				  ClassNodeRealizer realizer = (ClassNodeRealizer) graph.getRealizer(nc.node());
				  NodeLabel node_label_atrribute = realizer.getAttributeLabel();
				  
				  String attrib_text = node_label_atrribute.getText();
				  preserved_attributes_list.add(new String(attrib_text));
				  
				  NodeLabel node_label_method = realizer.getMethodLabel();
				  String method_text = node_label_method.getText();
				  preserved_methods_list.add(new String(method_text));
			  }
		}
		private List<String> getAttributeList(Node node, Graph2D graph)
		{
			  ClassNodeRealizer realizer = (ClassNodeRealizer) graph.getRealizer(node);
			  NodeLabel node_label_atrribute = realizer.getAttributeLabel();
			  
			  String attrib_text = node_label_atrribute.getText();

			  
			  List<String> node_attribute_list = new ArrayList<String>();
			  
			  
			  //split the text labels by newline character and put them into lists
			  Scanner myScanner = new Scanner(attrib_text);
			  while (myScanner.hasNextLine())
			  {
				  String line = myScanner.nextLine();
				  node_attribute_list.add(line);
			  }
			  return node_attribute_list;
		}
		private List<String> getMethodList(Node node, Graph2D graph)
		{
			  ClassNodeRealizer realizer = (ClassNodeRealizer) graph.getRealizer(node);	
		
			  NodeLabel node_label_method = realizer.getMethodLabel();
			  String method_text = node_label_method.getText();
			  
			  List<String> node_method_list = new ArrayList<String>();
			  Scanner myScanner = new Scanner(method_text);
			  while (myScanner.hasNextLine())
			  {
				  String line = myScanner.nextLine();
				  node_method_list.add(line);
			  }
			return node_method_list;
		}




	}
class CompareGraphs extends Compare
	{
		CompareGraphs(Darls darls)
		{
			super(darls);
			compare();
			//animateComparison();	

		}

		//bubble_clean_up

	

		/*private void animateNewNodesRepeatedly()
		{
			
		    final Graph2D graph = view.getGraph2D();

		    // use a concurrency object to make all selected nodes blink simultaneously
		    CompositeAnimationObject concurrency = AnimationFactory.createConcurrency();
		    for (NodeCursor nc = newNodes.nodes(); nc.ok(); nc.next()) 
		    {	
		        AnimationObject ao = factory.blink(graph.getRealizer(nc.node()),REPETITION_DURATION); 	//duraton
	  	        concurrency.addAnimation(ao);	
		    }
		  for (EdgeCursor ec = newEdges.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				EdgeRealizer er = view.getGraph2D().getRealizer(e);
				EdgeRealizer unvisited = er.createCopy();
				unvisited.setLineColor(Color.red);
				
				AnimationObject ao = factory.traverseEdge(er, unvisited, er, true,
			              ViewAnimationFactory.RESET_EFFECT,
			              REPETITION_DURATION);
				
				concurrency.addAnimation(ao);
			}
		  AnimationObject repetition = AnimationFactory.createRepetition(concurrency, 999, false);  		  
		  player.animate(repetition);  		    
		    
		}
		private void animateNewEdgesRepeatedly()
		{
			
			final CompositeAnimationObject concurrency = AnimationFactory.createConcurrency();

			for (EdgeCursor ec = newEdges.edges(); ec.ok(); ec.next())
			{
				Edge e = ec.edge();
				EdgeRealizer er = view.getGraph2D().getRealizer(e);
				EdgeRealizer unvisited = er.createCopy();
				unvisited.setLineColor(Color.red);
				
				AnimationObject ao = factory.traverseEdge(er, unvisited, er, true,
			              ViewAnimationFactory.RESET_EFFECT,
			            TRAVERSE_NEW_EDGE_DURATION);
				
				concurrency.addAnimation(ao);
			}
			AnimationObject repetition = AnimationFactory.createRepetition(concurrency, 999, false);  		  

			player.animate(repetition);
		}*/
		public void post_animation_cleanup() 
		{  			
			super.post_animation_cleanup();
			//we are no longer highlighting after animation. November 11, 2010
			//setNewNodesHighlight(true);
			//setNewEdgesHighlight(true);
			
			//disabled for experiment
			//bubble_labels = new BubbleLabels();
			//bubble_labels.display();
			
			//animateNewNodesRepeatedly();
			//animateNewEdgesRepeatedly();

		}
		protected void compare()
		{
			initCompositeAnimationObjects();
			
			 Graph2D g2=view.getGraph2D();
				 
			    
			  //First let's compare missing nodes
			 List[] L = VObject.find_uuid(darls.v_node_list_1, darls.v_node_list_2);
			 
			 /*System.out.println("++++++++++++++++");
			 for(int i=0;i<(L[0].size());i++)
			 {
				 System.out.println("++"+L[0].get(i));
			 }
			 System.out.println("++++++++++++++++");
			 System.out.println("vnode1:");
			 //System.out.println(L.length);
			 for (int i = 0; i < darls.v_node_list_1.size();i++)
			 {
				 VNode vn = darls.v_node_list_1.get(i);
				 System.out.println(vn.uuid);
			 }
			 System.out.println("vnode2:");
			 for (int i = 0; i < darls.v_node_list_2.size();i++)
			 {
				 VNode vn = darls.v_node_list_2.get(i);
				 System.out.println(vn.uuid);
			 }*/
			 
			 for (int i = 0; i<L.length; i++)
			 {
				//L[0] - stores those are both in new and old version
				//L[1] - stores those are only in the new version
				//L[2] - stores those are only in the old version			 
				 /*if (i == 0) System.out.print("Both in v1 and v2:");
				 else if (i == 1) System.out.print("only in v2:");
				 else if (i == 2) System.out.print("only in v1:");*/
				/* for (int j = 0; j<L[i].size();j++)
				 {
					 UUID vid = (UUID) L[i].get(j);
					 System.out.print(vid+",");
				 }
				 System.out.print("\n");*/

			 }
			  
			  //Compare Edges
			List[] E = VObject.find_uuid(darls.v_edge_list_1, darls.v_edge_list_2);
			 //System.out.println(L.length);
				//L[0] - stores those are both in new and old version
				//L[1] - stores those are only in the new version
				//L[2] - stores those are only in the old version
			 /*System.out.print("Edges v2:");
				 for (int j = 0; j<E[1].size();j++)
				 {
					 UUID vid = (UUID) E[1].get(j);
					 System.out.print(vid+",");
				 }
				 System.out.print("\n");
		     System.out.print("Edges v1:");
				 for (int j = 0; j<E[2].size();j++)
				 {
					 UUID vid = (UUID) E[2].get(j);
					 System.out.print(vid+",");
				 }
				 System.out.print("\n");*/
			//-----------------------------------------------------------------------------------------
		    //Animate FADE IN
			//fade in new nodes
			newNodes = new NodeList();
			newNodeColors = new ArrayList<Color>();
			for (int i = 0; i < L[1].size(); i++)
			{
				for (int j = 0; j < darls.v_node_list_2.size();j++)
					if ( ((UUID) L[1].get(i)).equals(( (VNode) (darls.v_node_list_2.get(j))).uuid))
					{	
						Node n = ( (VNode) (darls.v_node_list_2.get(j))).node;
						addFadeInNode( n , darls.duration_fade_in_comparison ); //from the top class
						//high light this one
						newNodes.add(n);
						
						//get color of the new node restore it in future 
						NodeRealizer nr = view.getGraph2D().getRealizer(n);
						newNodeColors.add(nr.getFillColor());
					}
			}
			//fade in new edges
			newEdges = new EdgeList();
			newEdgeColors = new ArrayList<Color>();
			for (int i = 0; i < E[1].size(); i++)
			{
				for (int j = 0; j < darls.v_edge_list_2.size();j++)
					if ( ((UUID) E[1].get(i)).equals(( (VEdge) (darls.v_edge_list_2.get(j))).uuid))
					{
						Edge e = ( (VEdge) (darls.v_edge_list_2.get(j))).edge;
						addFadeInEdge(e , darls.duration_fade_in_comparison ); //from the top class
						newEdges.add(e);

						//get color of the new edge restore it in future
						EdgeRealizer er = view.getGraph2D().getRealizer(e);
						newEdgeColors.add(er.getLineColor());
					}
			}
			//-----------------------------------------------------------------------------------------
			//Animate FADE OUT
			//fade out new nodes
			for (int i = 0; i < L[2].size(); i++)
			{
				for (int j = 0; j < darls.v_node_list_1.size();j++)
					if ( ((UUID) L[2].get(i)).equals(( (VNode) (darls.v_node_list_1.get(j))).uuid))
					{
						addFadeOutNode( ( (VNode) (darls.v_node_list_1.get(j))).node, darls.duration_fade_out_comparison ); //from the top class
					}
			}
			for (int i = 0; i < E[2].size(); i++)
			{
				for (int j = 0; j < darls.v_edge_list_1.size();j++)
					if ( ((UUID) E[2].get(i)).equals(( (VEdge) (darls.v_edge_list_1.get(j))).uuid))
					{
						addFadeOutEdge( ( (VEdge) (darls.v_edge_list_1.get(j))).edge, darls.duration_fade_out_comparison); //from the top class
					}
			}
			
			//common nodes
			addGraphMorph(L[0], darls.duration_morph_comparison);
			//System.out.println("L0 size=: "+L[0].size());
			
			//add common nodes to common nodes list and color
			commonNodes = new NodeList();
			commonNodeColors = new ArrayList<Color>();
			for (int i = 0; i < L[0].size(); i++)
			{
				UUID uuid = (UUID) L[0].get(i);
				Node n = VNode.getNodeFromNodeList(uuid, darls.v_node_list_2);
				commonNodes.add(n);
					
				//get color of the new node restore it in future 
				NodeRealizer nr = view.getGraph2D().getRealizer(n);
				commonNodeColors.add(nr.getFillColor());  				
			}
			//same for edges
			commonEdges = new EdgeList();
			commonEdgeColors = new ArrayList<Color>();
			for (int i = 0; i < E[0].size(); i++)
			{
				UUID uuid = (UUID) E[0].get(i);
				Edge e = VEdge.getEdgeFromEdgeList(uuid, darls.v_edge_list_2);				
				commonEdges.add(e);
					
				//get color of the new node restore it in future 
				EdgeRealizer er = view.getGraph2D().getRealizer(e);
				commonEdgeColors.add(er.getLineColor());  				
			}
			
			
			//moved outside function November 29, 2010
			//animateComparison();	
			
		    }
};

class Animation
	{
			Darls darls = null;
			Graph2DView view = null;
			Graph2DView left_view = null;
			protected AnimationPlayer player;
			protected ViewAnimationFactory factory;
			
			protected CompositeAnimationObject fade_in_comp_anim_obj;
			protected CompositeAnimationObject fade_out_comp_anim_obj;
			protected CompositeAnimationObject morph_comp_anim_obj;
			
			protected void initCompositeAnimationObjects()
			{
				fade_in_comp_anim_obj = AnimationFactory.createConcurrency();
				fade_out_comp_anim_obj = AnimationFactory.createConcurrency();
				morph_comp_anim_obj = AnimationFactory.createConcurrency();
			}
			
			private class MyAnimationListener implements AnimationListener 
			{
				    public void animationPerformed(AnimationEvent e) 
				    {
				      	
				      if (e.getHint() == AnimationEvent.BEGIN) 
				      {  
				    	  darls.bubble_labels.clear();
				    	  if (darls.user_study != null)				    	  
				    		  darls.user_study.animation_is_playing = true;		
				    	  

				      }
				      else if (e.getHint() == AnimationEvent.END) {
				    	  if (darls.user_study != null)
				    		  darls.user_study.animation_is_playing = false;
				    	  //display node label changes if any
				    	  YLabel.Factory factory = NodeLabel.getFactory();
						
						  // Retrieve a map that holds the default NodeLabel configuration.
						  // The implementations contained therein can be replaced one by one in order
						  // to create custom configurations...
						  Map implementationsMap = factory.createDefaultConfigurationMap();
						
						  // We will just customize the painting so register our custom painter
						  implementationsMap.put(YLabel.Painter.class, new MyPainter());
						
						  // Add the first configuration to the factory.
						  factory.addConfiguration("Bubble", implementationsMap);
				    	  

			    	     //after text versioning of uml, bubble clean up for graph compare
						  if (darls.user_study == null)
							  darls.compare_action.compare_obj.post_animation_cleanup();

			    	    	
				        System.out.println("Animation is finished!");
				      }
				      else if (e.getHint() == AnimationEvent.SHOW)
				      {
				    	  //This is where we disable the user from pressing compare
				    	  //so that the animation doesn't get messed up	
				    	  
				    	  if (darls.user_study != null)				    	  
				    		  darls.user_study.animation_is_playing = true;				    	  
				      }
				    }


			 }
			Animation(Darls _darls)
			{
				 darls = _darls;
				 view = darls.view;
				 left_view = darls.left_view;
				 factory = new ViewAnimationFactory(new Graph2DViewRepaintManager(view));
				 player = factory.createConfiguredPlayer();
				 player.addAnimationListener(new MyAnimationListener());
			}
			/*protected void play()
			{

			}*/
			public void addFadeInNode(Node node, long DURATION) 
			  {
			      Graph2D graph = view.getGraph2D();
				  NodeRealizer nr = graph.getRealizer(node);
			      
			      AnimationObject fade_in_anim_obj = factory.fadeIn(nr, DURATION);
			      fade_in_comp_anim_obj.addAnimation(fade_in_anim_obj);
			      
			      /*nr.setVisible(false);
			      player.animate(view_anim_factory.fadeIn(nr, DURATION));*/
			      
			      //System.out.println("animation player is: " + player);
			  }
			  
			  
			  public void addFadeInEdge(Edge edge, long DURATION) 
			  {
				  //System.out.println("DURATION of fade of edge "+DURATION);
			      Graph2D graph = view.getGraph2D();
			      //fade in
			      EdgeRealizer er = graph.getRealizer(edge);
			      
			      
			      AnimationObject fade_in_anim_obj = factory.fadeIn(er, DURATION);
			      fade_in_comp_anim_obj.addAnimation(fade_in_anim_obj);
			      
			      /*
			       * er.setVisible(false);
			      player.animate(view_anim_factory.fadeIn(er, DURATION));
			      */ 
			  }
			  
			  public void addFadeOutNode(Node node, long DURATION) 
			  {
				  //System.out.println("------this is inside the fade out-------");
			      NodeRealizer nr = view.getGraph2D().getRealizer(node);

			      // let's create a drawable, so the animation can run no matter
			      // if the node is in the graph or not
			      Drawable dnr = ViewAnimationFactory.createDrawable(nr);
			      
			      AnimationObject fadeOut_anim_obj = factory.fadeOut(dnr, DURATION);
			      fade_out_comp_anim_obj.addAnimation(fadeOut_anim_obj);

			      // let's start the animation with some delay so edges "vanish"
			      // before nodes
			      /*nr.setVisible(false);

			      player.animate(
			          AnimationFactory.createSequence(
			              AnimationFactory.createPause(0), fadeOut_anim_obj));
			      
			      //this is so that the nodes dont dissapear from the left side view
			      nr.setVisible(true);
			      */
			  }
			  
			  public void addFadeOutEdge(Edge edge, long DURATION) 
			  {
			      EdgeRealizer er = view.getGraph2D().getRealizer(edge);

			      // let's create a drawable, so the animation can run no matter
			      // if the node is in the graph or not
			      Drawable dnr = ViewAnimationFactory.createDrawable(er);
			  
			      AnimationObject fadeOut_anim_obj = factory.fadeOut(dnr, DURATION);
			      fade_out_comp_anim_obj.addAnimation(fadeOut_anim_obj);

			      // let's start the animation with some delay so edges "vanish"
			      // before nodes
			      /*er.setVisible(false);

			      player.animate(
			          AnimationFactory.createSequence(
			              AnimationFactory.createPause(0), fadeOut_anim_obj));
			    
			      //this is so that the edges dont dissapear from the left side view
			      er.setVisible(true);
			      */
			  }
		  		protected void addGraphMorph(List L, long DURATION) {
		    		  
		    		  Graph2D graph_v2 =  view.getGraph2D(); //preserving the current graph
		    		  Graph2D graph_v1 = left_view.getGraph2D();
		    		  //System.out.println("+++++++the size of list: "+L.size());
		    		  for (int i = 0; i < L.size();i++)
		    		  {
		    			  Node[] na = null;
		    			  UUID vid = (UUID) L.get(i);
		    			  //System.out.println("+++++++++++++++the vid inside the animate mprph: "+vid);
		    			  na = VNode.getNodesFromVid(vid,darls.v_node_list_1,darls.v_node_list_2);		  
		    			  NodeRealizer nr1 = graph_v1.getRealizer(na[0]); //may want to add g1 as argument later
		    			  NodeRealizer nr2 = graph_v2.getRealizer(na[1]);//may want to add view as argument later
		    			  //nr1.getV
		    			  NodeRealizer nr2_copy = nr2.createCopy();
		    			  nr2.moveBy(nr1.getX(), nr1.getY());
		    			  nr2.setSize(nr1.getWidth(),nr1.getHeight());
		    			  nr2.setFillColor(nr1.getFillColor());
		    			  nr2.setCenter(nr1.getCenterX(),nr1.getCenterY());

		    			  if (nr2.getHeight() != nr2_copy.getHeight() ||
		    					  nr2.getWidth() != nr2_copy.getWidth() ||
		    					  nr2.getCenterX() != nr2_copy.getCenterX() ||
		    					  nr2.getCenterY() != nr2_copy.getCenterY())
		    			  {
		    				  AnimationObject morph_anim_obj = factory.morph(nr2, nr2_copy, ViewAnimationFactory.APPLY_EFFECT,DURATION);
		    				  morph_comp_anim_obj.addAnimation(morph_anim_obj);
		    			  }
		    			  
		    			  //player.animate(view_anim_factory.morph(nr2, nr2_copy, ViewAnimationFactory.APPLY_EFFECT,DURATION));
		    		  }
		    		  //it seems like there is no need to edge animation at this point.
		    	  }
			  
			 
};