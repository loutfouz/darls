package darls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import y.io.BadVersionException;
import y.util.YVersion;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.YLabel;

/**
 * NodeRealizer implementation that represents a UML Class Node.
 */
public class ClassNodeRealizer extends NodeRealizer {
  NodeLabel aLabel; //attributeLabel
  NodeLabel mLabel; //methodLabel
  boolean clipContent;
  boolean omitDetails;
  boolean use3DEffect = true;
 //static NodeLabel constraintLabel = new NodeLabel();
 //static NodeLabel stereotypeLabel = new NodeLabel();
  NodeLabel constraintLabel = null;	//introduced this variable by shumon to color the stereotype and constraint labels because the static variables are never used
  NodeLabel stereotypeLabel = null;

  String stereotype = "";
  String constraint = "";

  public ClassNodeRealizer()
  {
    init();
  }

  void init()
  {
    getLabel().setModel(NodeLabel.INTERNAL);
    getLabel().setPosition(NodeLabel.TOP);

    getLabel().setFontSize(13);
    getLabel().setFontStyle(Font.BOLD);

    aLabel = new NodeLabel();
    aLabel.bindRealizer(this);
    aLabel.setAlignment(YLabel.ALIGN_LEFT);
    aLabel.setModel(NodeLabel.FREE);

    mLabel = new NodeLabel();
    mLabel.bindRealizer(this);
    mLabel.setAlignment(YLabel.ALIGN_LEFT);
    mLabel.setModel(NodeLabel.FREE);

    clipContent = true;
    omitDetails = false;
    
    //added by shumon for being able to color
    constraintLabel = new NodeLabel();
    stereotypeLabel = new NodeLabel();
  }

  public ClassNodeRealizer(NodeRealizer r)
  {
    super(r);
    if(r instanceof ClassNodeRealizer)
    {
      ClassNodeRealizer cnr = (ClassNodeRealizer)r;
      aLabel = (NodeLabel)cnr.aLabel.clone();
      aLabel.bindRealizer(this);
      mLabel = (NodeLabel)cnr.mLabel.clone();
      mLabel.bindRealizer(this);
      constraint = cnr.constraint;
      stereotype = cnr.stereotype;
      clipContent = cnr.clipContent;
      omitDetails = cnr.omitDetails;
      use3DEffect = cnr.use3DEffect;
      
      //added by shumon for being able to color
      constraintLabel = new NodeLabel();
      stereotypeLabel = new NodeLabel();
    }
    else
      init();
  }


  public NodeRealizer createCopy(NodeRealizer r)
  {
    return new ClassNodeRealizer(r);
  }


  //////////////////////////////////////////////////////////////////////////////
  // SETTER & GETTER ///////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void setConstraint(String constraint)
  {
    this.constraint = constraint;
  }

  public void setStereotype(String stereotype)
  {
    this.stereotype = stereotype;
  }

  public String getConstraint()
  {
    return constraint;
  }

  public String getStereotype()
  {
    return stereotype;
  }

  public NodeLabel getMethodLabel()
  {
    return mLabel;
  }

  public NodeLabel getAttributeLabel()
  {
    return aLabel;
  }
  public NodeLabel getStereotypeLabel()
  {
    return stereotypeLabel;
  }
  public NodeLabel getConstraintLabel()
  {
    return constraintLabel;
  }

  public boolean getClipContent()
  {
    return clipContent;
  }

  public void setClipContent(boolean clipping)
  {
    clipContent = clipping;
  }


  public void setOmitDetails(boolean b)
  {
    omitDetails = b;
  }

  public boolean getOmitDetails()
  {
    return omitDetails;
  }

  public void setUse3DEffect(boolean b)
  {
    use3DEffect = b;
  }

  public boolean getUse3DEffect()
  {
    return use3DEffect;
  }

  void addToLabel(NodeLabel l, String s)
  {
    if(l.getText().length() > 0)
      l.setText(l.getText() + '\n' + s);
    else
      l.setText(s);
  }

  public void addMethod(String method)
  {
    addToLabel(mLabel,method);
  }

  public void addAttribute(String attr)
  {
    addToLabel(aLabel,attr);
  }

  //added by shumon
  public void fitContentWithOmittedDetalis()
  {
    double width = getLabel().getWidth() + 10.0;
    if (stereotype.length() > 0)
    	width = Math.max(width, 90);
    
    double height =
      getLabel().getHeight()+ 20.0;
    if (stereotype.length() > 0)
    	height = height * 1.5;
    setSize(width,height);
  }
  
  public void fitContent()
  {
    double width =
      Math.max(Math.max(getLabel().getWidth(),
                        aLabel.getWidth()),
               mLabel.getWidth()) + 10.0;
    
    if (stereotype.length() > 0)	//added by shumon
    	width = Math.max(width, 90);
    
    double height =
      getLabel().getHeight() +
      aLabel.getHeight() +
      mLabel.getHeight() + 20.0;
    
    if (stereotype.length() > 0)	//added by shumon
    	height += 20 ;
    
    setSize(width,height);
  }


  //////////////////////////////////////////////////////////////////////////////
  // GRAPHICS  /////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////


  public void paint(Graphics2D gfx)
  {
    if (!isVisible())
    {
      return;
    }

    Color fill = getFillColor();
    Color line = getLineColor();

    if(isSelected())
    {
      paintHotSpots(gfx);
      if(fill != null) fill = fill.darker();
    }

    final Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
    if(fill != null)
    {
      gfx.setColor(fill);
      if (use3DEffect){
        gfx.fill3DRect((int)x,(int)y,(int)width,(int)height,true);
      } else {
        gfx.fill(rect);
      }
    }

    if (line != null && !use3DEffect){
      gfx.setColor(line);
      Stroke s = gfx.getStroke();
      gfx.setStroke(getLineType());
      gfx.draw(rect);
      gfx.setStroke(s);
    }


    Shape oldClip = null;
    if(clipContent)
    {
      oldClip = gfx.getClip();
      gfx.clipRect((int)x,(int)y,(int)width,(int)height);
    }


    double yoff = 3.0;

    if(stereotype.length() > 0)
    {
      //NodeLabel l = new NodeLabel();
      NodeLabel l = stereotypeLabel;		//added by shumon, to be able to color the stereotype
      l.setText("<<" + getStereotype() + ">>");
      l.setModel(NodeLabel.FREE);
      l.setOffset( (getWidth()-l.getWidth())/2.0, yoff);
      l.bindRealizer( this );
      l.paint(gfx);
      yoff += l.getHeight() + 5;
    }

    NodeLabel label = getLabel();
    label.setOffset((getWidth()-label.getWidth())/2.0,yoff);
    label.paint(gfx);
    yoff += label.getHeight()+3;

    if(constraint.length() > 0)
    {
      //NodeLabel l = new NodeLabel();
      NodeLabel l = constraintLabel; //added by shumon, to be able to color 
      
      l.setText("{" + getConstraint() + "}");
      l.setModel(NodeLabel.FREE);
      l.setOffset( getWidth()-l.getWidth() - 5.0, yoff);
      l.bindRealizer( this );
      l.paint(gfx);
      yoff += l.getHeight() + 5;
    }

    if(!omitDetails && !(aLabel.getText().equals("") && mLabel.getText().equals("")))
    {
      if(line != null)
      {
        gfx.setColor(line);
        gfx.drawLine((int)x+1,(int)(y+yoff),(int)(x+width-1),(int)(y+yoff));
      }

      yoff += 3;
      aLabel.setOffset(3,yoff);
      aLabel.paint(gfx);
      yoff += aLabel.getHeight()+3;

      if(line != null)
      {
        gfx.setColor(line);
        gfx.drawLine((int)x+1,(int)(y+yoff),(int)(x+width-1),(int)(y+yoff));
      }

      yoff += 3;
      mLabel.setOffset(3,yoff);
      mLabel.paint(gfx);
    }

    if(clipContent)
    {
      gfx.setClip(oldClip);
    }
  }

  protected void paintNode(Graphics2D g)
  {}

  //////////////////////////////////////////////////////////////////////////////
  // SERIALIZATION /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public void write(ObjectOutputStream out) throws IOException {
    out.writeByte(YVersion.VERSION_4);
    super.write(out);
    aLabel.write( out );
    mLabel.write( out );
    out.writeBoolean(clipContent);
    out.writeBoolean(omitDetails);
    out.writeBoolean(use3DEffect);
    out.writeObject(getStereotype());
    out.writeObject(getConstraint());
  }

  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    switch(in.readByte()) {
    case YVersion.VERSION_1:
      super.read(in);
      init();
      aLabel.read(in);
      mLabel.read(in);
      clipContent = in.readBoolean();
      omitDetails = false;
      break;
    case YVersion.VERSION_2:
      super.read(in);
      init();
      aLabel.read(in);
      mLabel.read(in);
      clipContent = in.readBoolean();
      omitDetails = in.readBoolean();
      break;
    case YVersion.VERSION_3:
      super.read(in);
      init();
      aLabel.read(in);
      mLabel.read(in);
      clipContent = in.readBoolean();
      omitDetails = in.readBoolean();
      stereotype = (String)in.readObject();
      constraint = (String)in.readObject();
      break;
    case YVersion.VERSION_4:
      super.read(in);
      init();
      aLabel.read(in);
      mLabel.read(in);
      clipContent = in.readBoolean();
      omitDetails = in.readBoolean();
      use3DEffect = in.readBoolean();
      stereotype = (String)in.readObject();
      constraint = (String)in.readObject();
      break;
    default:
      throw new BadVersionException();
    }
  }
}
