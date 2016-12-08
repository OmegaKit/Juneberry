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

import java.util.Iterator;

/**
 * @author Xing
 */

public class PDS3Kind {

    private Map<String, Object> metaMap = null;
    private Map<String, Object> dataMap = null;

    private PDS3 pds3 = null;

    public PDS3Kind() {
        this.metaMap = new HashMap<String, Object>();
        this.dataMap = new HashMap<String, Object>();

        this.pds3 = new PDS3();
    };

    private List collect_level1(String prefix, Map map) {
        List<String> list = new ArrayList<String>();

        Iterator iterator = map.entrySet().iterator();

        String key;
        Object value;
        String pfx;
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            key = (String)pair.getKey();
            value = pair.getValue();
            //
            pfx = prefix + "/" + key;
            //
            list.add(key);
            //
            this.metaMap.put(pfx+"/", null);
            if (value instanceof String) {
                this.dataMap.put(pfx+"[]", (String)value);
            } else {
                this.dataMap.put(pfx+"[]", "toBeImplemented");
            }
        }

        return list;
    }

    // dom node to map list of metaMap and dataMap
    public Map[] node_to_map_list(String prefix, Node root) {

        List l = this.pds3.node_to_list(root);

        List<String> list = new ArrayList<String>();

        String name = null;
        String pfx = null;
        Map map = null;
        for (int i=0; i<l.size(); i++) {
            name = "group";
            if (i != 0)
                name = name + i;
            //
            list.add(name);
            //
            pfx = prefix + "/" + name;
            map = (Map)l.get(i);
            this.metaMap.put(pfx+"/", this.collect_level1(pfx, map));
        }

        this.metaMap.put(prefix+"/", list);

        return new Map[] {this.metaMap, this.dataMap};
    }
}
