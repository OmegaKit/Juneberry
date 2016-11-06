package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.awt.image.BufferedImage;

import org.apache.commons.codec.binary.Base64;

import the.treevotee.Constant;
import the.treevotee.store.read.Reader;

import the.treevotee.Attribute;
import the.treevotee.Node;
import the.treevotee.Leaf;

import jpl.mipl.wiio.proc.Processor;
import jpl.mipl.wiio.proc.ProcessorException;

/**
 * @author Xing
 */
// 20111111, it should be a subclass of Reader?
//public class BufferedImageStretched extends Reader {
public class BufferedImageStretched {

    private BufferedImage bufferedImage = null;
    private int width = -1;
    private int height = -1;

    ImageSlicer slicer = new ImageSlicer();

    Processor stretcher = null;

    /*
    public BufferedImageStretched(String sid, String path) {
        super(sid, path);
    }
    */

    public BufferedImageStretched(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BufferedImageStretched(int[] size) {
        this.width = size[0];
        this.height = size[1];
    }

    public BufferedImageStretched(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
    }

    public void set_stretcher(Processor stretcher) {
        this.stretcher = stretcher;
    }

    public void set_buffered_image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
    }

    public Map get(String[] names, boolean traverse, String indexer) throws BufferedImageStretchedException {
        if (!names[0].equals(""))
            throw new BufferedImageStretchedException("Internal inconsistency: names[0] is not \"\"");
        
        if (names.length > 2)
            throw new BufferedImageStretchedException("Invalid w10n entity "+names[2]);

        if (names.length == 1)
            return this.get_node_top(traverse, indexer);

        // now, names.length == 2
        return this.get_leaf(names[1], indexer);
        //throw new BufferedImageStretchedException("Invalid w10n entity "+names[1]);
    }

    private Map get_node_top(boolean traverse, String indexer) throws BufferedImageStretchedException {
        if (indexer == null)
            return this.get_node_top_meta(traverse);
        throw new BufferedImageStretchedException("Data unsupported for node \"\"");
    }

    private Map get_node_top_meta(boolean traverse) throws BufferedImageStretchedException {
        Node node = new Node("");

        // attributes
        // (*) size = [w, h]
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, this.get_size(this.width, this.height));
        node.attrs.add(attr);

        // leaves
        Leaf leaf;
        Object obj;
        try {
            ArrayList al = this.stretcher.get_list();
            for (int i=0; i<al.size(); i++) {
                Map m = (Map)al.get(i);
                // leaf name based on method name
                leaf = new Leaf("proc:stretch:"+(String)m.get("name"));
                // optional description
                obj = m.get("description");
                if (obj != null) {
                    attr = new Attribute("description", obj);
                    leaf.attrs.add(attr);
                }
                // input
                obj = m.get("input");
                attr = new Attribute("input", obj);
                leaf.attrs.add(attr);
                // add this as leaf
                node.leaves.add(leaf);
            }
        } catch (ProcessorException pe) {
            throw new BufferedImageStretchedException(pe);
        }

        return node.to_map();
    }

    private Map get_leaf(String name, String indexer) throws BufferedImageStretchedException {
        if (indexer == null)
            return this.get_leaf_meta(name);
        return this.get_leaf_data(name, indexer);
    }

    private ArrayList get_size(int w, int h) {
        ArrayList size = new ArrayList(2);
        size.add(new Integer(w));
        size.add(new Integer(h));
        return size;
    }

    private Map get_leaf_meta(String name) throws BufferedImageStretchedException {
        // it is a leaf
        Leaf leaf = new Leaf(name);
        // set attribute
        try {
            String decoded = new String(Base64.decodeBase64(name.getBytes()));
            Attribute attr = new Attribute("input", this.stretcher.get_usage(decoded));
            leaf.attrs.add(attr);
        } catch (ProcessorException pe) {
            throw new BufferedImageStretchedException(pe);
        }
        return leaf.to_map();
    }
    private Map get_leaf_data(String name, String indexer) throws BufferedImageStretchedException {
        HashMap map = new HashMap();
        try {
            String decoded = new String(Base64.decodeBase64(name.getBytes()));
            //System.err.println("decoded: "+decoded);
            map.put(Constant.DATA, this.slicer.slice(this.stretcher.process(this.bufferedImage, decoded), indexer));
        } catch (ProcessorException pe) {
            throw new BufferedImageStretchedException(pe);
        } catch (ImageSlicerException ise) {
            throw new BufferedImageStretchedException(ise);
        }
        return map;
    }
}
