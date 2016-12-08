package jpl.mipl.juneberry.util;

/*
 * Copyright (c) 2011 - 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.util.List;
import java.util.ArrayList;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Xing
 */

public class VicarKind {

    private Map<String, Object> metaMap = null;
    private Map<String, Object> dataMap = null;

    public VicarKind() {
        this.metaMap = new HashMap<String, Object>();
        this.dataMap = new HashMap<String, Object>();
    };

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

    // node item without subitems.
    private String collect_item(String prefix, Node node) {

        NamedNodeMap nnMap = node.getAttributes();
        if (nnMap == null)
            //return this.unknown_to_map(node);
            return "unsupportedEntity";
        //for (int i=0; i<nnMap.getLength(); i++) {
        //    Node attr = nnMap.item(i);
        //    System.out.println(attr.getNodeName()+" "+attr.getNodeValue());
        //}
        int len = nnMap.getLength();
        if (len < 2 || len > 3)
            //return this.unknown_to_map(node);
            return "unsupportedEntity";

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

        // fixme: have unit as attribute
        this.metaMap.put(prefix+"/"+key+"/", null);
        this.dataMap.put(prefix+"/"+key+"[]", node.getChildNodes().item(0).getNodeValue());

        /* kept for use by later improvement
        Map map = new HashMap();
        if (units == null) {
            map.put(key, node.getChildNodes().item(0).getNodeValue());
        } else {
            map.put(key, node.getChildNodes().item(0).getNodeValue()+" <"+units+">");
        }
        */

        return key;
    }

    // level 1 node
    private List collect_level1(String prefix, Node root) {
        //jpl.mipl.juneberry.util.Util2.node2xml(root);

        // to be rid of!!!
        Map map = new LinkedHashMap();

        List<String> list = new ArrayList<String>();

        NodeList nodeList = root.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String name = null;
            String pfx = null;
            if (!node.getNodeName().equals("item")) {
                name = "unknownNodeName";
                list.add(name);
                pfx = prefix + "/" + name;
                this.metaMap.put(pfx+"/", new ArrayList());
                continue;
            }
            Node firstChild = node.getFirstChild();
            if (firstChild == null) {
                name = "noFirstChild";
                list.add(name);
                pfx = prefix + "/" + name;
                this.metaMap.put(pfx+"/", new ArrayList());
                continue;
            }
            if (firstChild.getNodeType() == Node.TEXT_NODE) {
                name = this.collect_item(prefix, node);
                list.add(name);
                //pfx = prefix + "/" + name;
                //this.metaMap.put(pfx+"/", new ArrayList());
                continue;
            }
            if (firstChild.getNodeType() == Node.ELEMENT_NODE) {
                map.putAll(this.item_with_subitem_to_map(node));
                list.add(name);
                continue;
            }
            map.putAll(this.unknown_to_map(node));
        }
        return list;
    }

    // node to map list
    public Map[] node_to_map_list(String prefix, Node root) {

        List<String> list = new ArrayList<String>();

        NodeList nodeList = root.getChildNodes();
        int countSystem = 0;
        int countProperty = 0;
        int countTask = 0;
        int countUnknown = 0;
        String nodeName = null;
        String pfx = null;
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            nodeName = node.getNodeName();
            if (nodeName.equals("SYSTEM")) {
                if (countSystem != 0)
                    nodeName = nodeName + countSystem;
                countSystem += 1;
            } else if (nodeName.equals("PROPERTY")) {
                if (countProperty != 0)
                    nodeName = nodeName + countProperty;
                countProperty += 1;
            } else if (nodeName.equals("TASK")) {
                if (countTask != 0)
                    nodeName = nodeName + countTask;
                countTask += 1;
            } else {
                //nodeName = "unknown" + nodeName;
                nodeName = "unknown";
                if (countUnknown != 0)
                    nodeName = nodeName + countUnknown;
                countUnknown += 1;
            }
            list.add(nodeName);
            pfx = prefix + "/" + nodeName;
            this.metaMap.put(pfx+"/", this.collect_level1(pfx, node));
        }

        this.metaMap.put(prefix+"/", list);

        return new Map[] {this.metaMap, this.dataMap};
    }
}
