package jpl.mipl.juneberry.util;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import java.io.StringWriter;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

/**
 * @author Xing
 */

public class Util {

    public Util() {};

    public static void node2xml(Node node) {
        try {
      // Set up the output transformer
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");

      // Print the DOM node

      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(node);
      trans.transform(source, result);
      String xmlString = sw.toString();

      System.out.println(xmlString);
      System.out.println("===========================");

      //System.out.println(org.json.XML.toJSONObject(xmlString).toString(4));
      //System.out.println("===========================");
    
        //} catch (org.json.JSONException je) {
      //je.printStackTrace();
        } catch (TransformerException e) {
      e.printStackTrace();
        }
    }

    public static Object node_to_object(Node root) throws IOException {
        NodeList children = root.getChildNodes();
        if (children.getLength() != 1)
            throw new IOException("Unexpected dom");

        Node node = children.item(0);
        String nodeName = node.getNodeName();
        if (nodeName.equals("PDS_LABEL")) {
            PDS3 pds3 = new PDS3();
            return pds3.node_to_list(node);
        } else if (nodeName.equals("VICAR_LABEL")) {
            Vicar vicar = new Vicar();
            return vicar.node_to_map(node);
        } else {
            throw new IOException("Unsupported dom node "+nodeName);
        }
    }

}
