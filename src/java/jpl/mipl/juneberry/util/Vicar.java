package jpl.mipl.juneberry.util;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Xing
 */

public class Vicar {

    public Vicar() {};

    // unknown node
    private Map unknown_to_map(Node node) {
        Map map = new HashMap();
        map.put(java.util.UUID.randomUUID().toString(), "Unknown node "+node.getNodeName());
        return map;
    }

    // node item without subitems.
    private Map item_to_map(Node node) {
        NamedNodeMap nnMap = node.getAttributes();
        if (nnMap == null)
            return this.unknown_to_map(node);
        //for (int i=0; i<nnMap.getLength(); i++) {
        //    Node attr = nnMap.item(i);
        //    System.out.println(attr.getNodeName()+" "+attr.getNodeValue());
        //}
        int len = nnMap.getLength();
        if (len < 2 || len > 3)
            return this.unknown_to_map(node);

        String key = null;
        String quoted = null;
        String units = null;
        for (int i=0; i<len; i++) {
            Node x = nnMap.item(i);
            if (x.getNodeName().equals("name"))
                key = x.getNodeValue();
            if (x.getNodeName().equals("quoted"))
                quoted = x.getNodeValue();
            if (x.getNodeName().equals("units"))
                units = x.getNodeValue();
        }

        Map map = new HashMap();
        if (units == null) {
            map.put(key, node.getChildNodes().item(0).getNodeValue());
        } else {
            map.put(key, node.getChildNodes().item(0).getNodeValue()+" <"+units+">");
        }
        return map;
    }

    // node item with subitems.
    private Map item_with_subitem_to_map(Node node) {
        NamedNodeMap nnMap = node.getAttributes();
        if (nnMap == null)
            return this.unknown_to_map(node);
        int len = nnMap.getLength();
        if (len < 1 || len > 3)
            return this.unknown_to_map(node);

        String key = null;
        String quoted = null;
        String units = null;
        for (int i=0; i<len; i++) {
            Node x = nnMap.item(i);
            if (x.getNodeName().equals("name"))
                key = x.getNodeValue();
            if (x.getNodeName().equals("quoted"))
                quoted = x.getNodeValue();
            if (x.getNodeName().equals("units"))
                units = x.getNodeValue();
        }
        //if (key == null || quoted == null)
        if (key == null)
            return this.unknown_to_map(node);

        NodeList nodeList = node.getChildNodes();
        len = nodeList.getLength();
        String str = "(";
        ArrayList al = new ArrayList();
        for (int i=0; i<len; i++) {
            Node x = nodeList.item(i);
            al.add(x.getFirstChild().getNodeValue().trim()+((units==null)?"":" <"+units+">"));
        }

        Map map = new HashMap();
        map.put(key, al);
        return map;
    }

    // level 1 node
    private Map level1_to_map(Node root) {
        Map map = new LinkedHashMap();

        NodeList nodeList = root.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!node.getNodeName().equals("item")) {
                map.putAll(this.unknown_to_map(node));
                continue;
            }
            Node firstChild = node.getFirstChild();
            if (firstChild == null) {
                map.putAll(this.unknown_to_map(node));
                continue;
            }
            if (firstChild.getNodeType() == Node.TEXT_NODE) {
                map.putAll(this.item_to_map(node));
                continue;
            }
            if (firstChild.getNodeType() == Node.ELEMENT_NODE) {
                map.putAll(this.item_with_subitem_to_map(node));
                continue;
            }
            map.putAll(this.unknown_to_map(node));
        }
        return map;
    }

    // node to map
    public Map node_to_map(Node root) {
        Map map = new LinkedHashMap();

        map.put("SYSTEM", new ArrayList());
        map.put("PROPERTY", new ArrayList());
        map.put("TASK", new ArrayList());

        NodeList nodeList = root.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals("SYSTEM")) {
                ((ArrayList)map.get("SYSTEM")).add(this.level1_to_map(node));
                continue;
            }
            if (nodeName.equals("PROPERTY")) {
                ((ArrayList)map.get("PROPERTY")).add(this.level1_to_map(node));
                continue;
            }
            if (nodeName.equals("TASK")) {
                ((ArrayList)map.get("TASK")).add(this.level1_to_map(node));
                continue;
            }
            map.putAll(this.unknown_to_map(node));
        }
        return map;
    }
}
