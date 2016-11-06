package jpl.mipl.wiio.util;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Xing
 */
public class Util {

    public Util() {};

    // convert dom node into a map that is ready for json_simple
    // map is a new map
    public void node_to_map(Node node, Map map) {
    
        // node attributes
        ArrayList attrList = null;
        NamedNodeMap nnMap = node.getAttributes();
        if (nnMap != null && nnMap.getLength() > 0) {
            attrList = new ArrayList();
            for (int i = 0; i < nnMap.getLength(); i++) {
                Node attr = nnMap.item(i);
                LinkedHashMap m = new LinkedHashMap();
                m.put(attr.getNodeName(),attr.getNodeValue());
                attrList.add(m);
            }
        }
        if (attrList != null)
            map.put("@attributes",attrList);
    
        // node children
        Node child = node.getFirstChild();
        if (child != null) {
            while (child != null) {
                //System.out.println("node name: "+child.getNodeName());
                if (child.hasChildNodes()) {
                    LinkedHashMap m = new LinkedHashMap();
                    this.node_to_map(child, m);
                    ArrayList l = null;
                    if (map.get(child.getNodeName()) == null) {
                        l = new ArrayList();
                        map.put(child.getNodeName(),l);
                    }
                    l = (ArrayList)map.get(child.getNodeName());
                    l.add(m);
                    //map.put(child.getNodeName(),m);
                } else if (child.getAttributes() != null) {
                    LinkedHashMap m = new LinkedHashMap();
                    this.node_to_map(child, m);
                    ArrayList l = null;
                    if (map.get(child.getNodeName()) == null) {
                        l = new ArrayList();
                        map.put(child.getNodeName(),l);
                    }
                    l = (ArrayList)map.get(child.getNodeName());
                    l.add(m);
                    //map.put(child.getNodeName(),m);
                } else {
                    ArrayList l = null;
                    if (map.get(child.getNodeName()) == null) {
                        l = new ArrayList();
                        map.put(child.getNodeName(),l);
                    }
                    l = (ArrayList)map.get(child.getNodeName());
                    l.add(child.getNodeValue());
                    //map.put(child.getNodeName(),child.getNodeValue());
                }
                child = child.getNextSibling();
            }
        }
    }
}
