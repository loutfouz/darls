package darls;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import y.io.graphml.NamespaceConstants;
import y.io.graphml.graph2d.AbstractNodeRealizerSerializer;
import y.io.graphml.graph2d.GraphicsSerializationToolkit;
import y.io.graphml.input.GraphMLParseException;
import y.io.graphml.input.GraphMLParseContext;
import y.io.graphml.output.GraphMLWriteException;
import y.io.graphml.output.GraphMLWriteContext;
import y.io.graphml.output.XmlWriter;
import y.view.NodeRealizer;

/**
 * GraphML serializer and deserializer for {@link ClassNodeRealizer}.  
 */
public class ClassNodeRealizerSerializer extends AbstractNodeRealizerSerializer {
  public String getName() {
    return "UMLClassNode";
  }

  public Class getRealizerClass() {
    return ClassNodeRealizer.class;
  }

  public String getNamespaceURI() {
    return NamespaceConstants.YFILES_JAVA_NS;
  }

  public void write(NodeRealizer realizer, XmlWriter writer, GraphMLWriteContext context) throws
      GraphMLWriteException {
    super.write(realizer, writer, context);
    ClassNodeRealizer cnr = (ClassNodeRealizer) realizer;
    writer.writeStartElement("UML", NamespaceConstants.YFILES_JAVA_NS).
        writeAttribute("clipContent", cnr.getClipContent() ? "true" : "false").
        writeAttribute("omitDetails", cnr.getOmitDetails() ? "true" : "false").
        writeAttribute("use3DEffect", cnr.getUse3DEffect() ? "true" : "false").
        writeAttribute("stereotype", cnr.getStereotype()).
        writeAttribute("constraint", cnr.getConstraint()).
        writeStartElement("AttributeLabel", NamespaceConstants.YFILES_JAVA_NS).
        writeText(cnr.getAttributeLabel().getText()).
        writeEndElement().
        writeStartElement("MethodLabel", NamespaceConstants.YFILES_JAVA_NS).
        writeText(cnr.getMethodLabel().getText()).
        writeEndElement().
        writeEndElement();
  }

  public void parse(NodeRealizer realizer, Node domNode, GraphMLParseContext context) throws GraphMLParseException {
    ClassNodeRealizer result = (ClassNodeRealizer) realizer;
    super.parse(result, domNode, context);

    NodeList children = domNode.getChildNodes();
    if (children != null) {
      for (int i = 0; i < children.getLength(); i++) {
        Node n = children.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
          String name = n.getLocalName();
          if ("UML".equals(name)) {
            NodeList umlNodes = n.getChildNodes();
            if (umlNodes != null) {
              for (int j = 0; j < umlNodes.getLength(); j++) {
                Node umlNode = umlNodes.item(j);
                if (umlNode.getNodeType() == Node.ELEMENT_NODE) {
                  String umlName = umlNode.getLocalName();
                  if ("AttributeLabel".equals(umlName)) {
                    result.getAttributeLabel().setText(GraphicsSerializationToolkit.parseText(umlNode));
                  } else if ("MethodLabel".equals(umlName)) {
                    result.getMethodLabel().setText(GraphicsSerializationToolkit.parseText(umlNode));
                  }
                }
              }
            }
            NamedNodeMap nm = n.getAttributes();
            Node a;
            a = nm.getNamedItem("omitDetails");
            if (a != null) {
              result.setOmitDetails("true".equalsIgnoreCase(a.getNodeValue()));
            }
            a = nm.getNamedItem("clipContent");
            if (a != null) {
              result.setClipContent("true".equalsIgnoreCase(a.getNodeValue()));
            }
            a = nm.getNamedItem("use3DEffect");
            if (a != null) {
              result.setUse3DEffect("true".equalsIgnoreCase(a.getNodeValue()));
            }
            a = nm.getNamedItem("stereotype");
            if (a != null) {
              result.setStereotype(a.getNodeValue());
            }
            a = nm.getNamedItem("constraint");
            if (a != null) {
              result.setConstraint(a.getNodeValue());
            }
          }
        }
      }
    }
  }
}
