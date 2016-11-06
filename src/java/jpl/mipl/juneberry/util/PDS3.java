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

public class PDS3 {

    public PDS3() {};

    private Map unknown_to_map(Node node) {
        Map map = new HashMap();
        map.put(java.util.UUID.randomUUID().toString(), "Unknown node "+node.getNodeName());
        return map;
    }
    
    private Map group_to_map(Node node) {
        NamedNodeMap nnMap = node.getAttributes();
        if (nnMap == null)
            return this.unknown_to_map(node);
        int len = nnMap.getLength();
        if (len != 1)
            return this.unknown_to_map(node);
        if (nnMap.item(0).getNodeName() != "name")
            return this.unknown_to_map(node);

        String name = nnMap.item(0).getNodeValue();

        Map m = new LinkedHashMap();
        // loop through items under this GROUP
        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
                Node x = nodeList.item(i);
                Node firstChild = x.getFirstChild();
                if (firstChild == null) {
                    m.putAll(this.unknown_to_map(x));
                    continue;
                }
                if (firstChild.getNodeType() == Node.TEXT_NODE) {
                    m.putAll(this.item_to_map(x));
                    continue;
                }
                if (firstChild.getNodeType() == Node.ELEMENT_NODE) {
                    m.putAll(this.item_with_subitem_to_map(x));
                    continue;
                }
                m.putAll(this.unknown_to_map(x));
        }

        Map map = new HashMap();
        map.put(name, m);
        return map;
    }

    private Map object_to_map(Node node) {
        NamedNodeMap nnMap = node.getAttributes();
        if (nnMap == null)
            return this.unknown_to_map(node);
        int len = nnMap.getLength();
        if (len != 1)
            return this.unknown_to_map(node);
        if (nnMap.item(0).getNodeName() != "name")
            return this.unknown_to_map(node);

        String name = nnMap.item(0).getNodeValue();

        Map m = new LinkedHashMap();
        // loop through items under this OBJECT
        NodeList nodeList = node.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Node x = nodeList.item(i);
            String nodeName = x.getNodeName();
            if (nodeName.equals("item")) {
                m.putAll(this.item_to_map(x));
                continue;
            }
            if (nodeName.equals("OBJECT")) {
                m.putAll(this.object_to_map(x));
                continue;
            }
            m.putAll(this.unknown_to_map(x));
        }

        Map map = new HashMap();
        map.put(name, m);
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
            if (x.getNodeName().equals("key"))
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
        if (len < 2 || len > 3)
            return this.unknown_to_map(node);

        String key = null;
        String quoted = null;
        String units = null;
        for (int i=0; i<len; i++) {
            Node x = nnMap.item(i);
            if (x.getNodeName().equals("key"))
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
        ArrayList al = new ArrayList();
        for (int i=0; i<len; i++) {
            Node x = nodeList.item(i);
            al.add(x.getFirstChild().getNodeValue().trim()+((units==null)?"":" <"+units+">"));
        }

        Map map = new HashMap();
        map.put(key, al);
        return map;
    }

    public ArrayList node_to_list(Node root) {
        ArrayList list = new ArrayList();

        Node node = root.getFirstChild();
        String nodeName = node.getNodeName();
        // 20131205, xing, just a temp hack for MSL and need to revisit
        if (!nodeName.equals("PDS3") && !nodeName.equals("ODL3")) {
            list.add(this.unknown_to_map(node));
            return list;
        }
        Map m = new HashMap();
        // 20131205, xing, just a temp hack for MSL and need to revisit
        //m.put("PDS_VERSION_ID", "PDS3");
        m.put("PDS_VERSION_ID", nodeName);
        list.add(m);

        NodeList children = root.getChildNodes();
        m = new LinkedHashMap();
        for (int i = 1; i < children.getLength(); i++) {
            node = children.item(i);
            nodeName = node.getNodeName();

            // at this level, each node is supposed to have children.
            if (!node.hasChildNodes()) {
                m.putAll(this.unknown_to_map(node));
                continue;
            }

            // encounter "COMMENT"
            if (nodeName.equals("COMMENT")) {
                if (node.getChildNodes().getLength() != 1) {
                    m.putAll(this.unknown_to_map(node));
                } else {
                    // time to save current map and start a new map
                    //if (!m.isEmpty()) {
                    if (m.size() > 1) {
                        list.add(m);
                        m = new LinkedHashMap();
                    }
                    String value = node.getChildNodes().item(0).getNodeValue();
                    // accoumulate if there is more than one line comment
                    ArrayList comments = new ArrayList();
                    if (m.containsKey("COMMENT"))
                        comments = (ArrayList)m.get("COMMENT");
                    comments.add(value);
                    m.put("COMMENT", comments);
                }
                continue;
            }
            // encounter "item"
            if (nodeName.equals("item")) {
                Node firstChild = node.getFirstChild();
                if (firstChild == null) {
                    m.putAll(this.unknown_to_map(node));
                    continue;
                }
                if (firstChild.getNodeType() == Node.TEXT_NODE) {
                    m.putAll(this.item_to_map(node));
                    continue;
                }
                if (firstChild.getNodeType() == Node.ELEMENT_NODE) {
                    m.putAll(this.item_with_subitem_to_map(node));
                    continue;
                }
                m.putAll(this.unknown_to_map(node));
                continue;
            }

            // encounter "OBJECT"
            if (nodeName.equals("OBJECT")) {
                m.putAll(this.object_to_map(node));
                continue;
            }

            // encounter "GROUP"
            if (nodeName.equals("GROUP")) {
                m.putAll(this.group_to_map(node));
                continue;
            }

            m.putAll(this.unknown_to_map(node));
        }
        list.add(m);

        return list;
    }
}
