package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.awt.image.BufferedImage;

import the.treevotee.Constant;
import the.treevotee.store.read.Reader;

import the.treevotee.Attribute;
import the.treevotee.Node;
import the.treevotee.Leaf;

/**
 * @author Xing
 */
// 20110819, it should be a subclass of Reader?
//public class BufferedImageResized extends Reader {
public class BufferedImageResized {

    private BufferedImage bufferedImage = null;
    private int width = -1;
    private int height = -1;

    ImageResizer resizer = new ImageResizer();
    ImageSlicer slicer = new ImageSlicer();

    /*
    public BufferedImageResized(String sid, String path) {
        super(sid, path);
    }
    */

    public BufferedImageResized(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public BufferedImageResized(int[] size) {
        this.width = size[0];
        this.height = size[1];
    }

    public BufferedImageResized(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
    }

    public void set_buffered_image(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        this.width = bufferedImage.getWidth();
        this.height = bufferedImage.getHeight();
    }

    /*
    // name should be w10n full name such as "", ""/w128, ""/h64, ""/128x64
    public Map getMeta(String name) throws BufferedImageResizedException {
        String[] names = name.split("/");
        boolean traverse = false;
        return this.get(names, traverse, null);
    }
    // name should be w10n full name such as "", ""/a, ""/a/b, ""/a/b/c
    public Map getData(String name, String indexer) throws BufferedImageResizedException {
        String[] names = name.split("/");
        return this.get(names, false, indexer);
    }
    */

    // get index of multiplier, which can be 'x' or '*'
    private int get_index_of_multiplier(String str) {
        int idx = str.indexOf('x');
        if (idx != -1)
            return idx;
        return str.indexOf('*');
    }

    public Map get(String[] names, boolean traverse, String indexer) throws BufferedImageResizedException {
        if (!names[0].equals(""))
            throw new BufferedImageResizedException("Internal inconsistency: names[0] is not \"\"");
        
        if (names.length > 2)
            throw new BufferedImageResizedException("Invalid w10n entity "+names[2]);

        if (names.length == 1)
            return this.get_node_top(traverse, indexer);

        // now, names.length == 2
        if (names[1].startsWith("w"))
            return this.get_leaf_by_w(names[1], indexer);

        if (names[1].startsWith("h"))
            return this.get_leaf_by_h(names[1], indexer);

        //int idx = names[1].indexOf("x");
        int idx = this.get_index_of_multiplier(names[1]);
        if (idx != -1)
            return this.get_leaf_by_w_and_h(names[1], indexer);

        throw new BufferedImageResizedException("Invalid w10n entity "+names[1]);
    }

    private Map get_node_top(boolean traverse, String indexer) throws BufferedImageResizedException {
        if (indexer == null)
            return this.get_node_top_meta(traverse);
        throw new BufferedImageResizedException("Data unsupported for node \"\"");
    }

    private Map get_node_top_meta(boolean traverse) throws BufferedImageResizedException {
        Node node = new Node("");

        // attributes
        // (*) size = [w, h]
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, this.get_size(this.width, this.height));
        node.attrs.add(attr);

        // leaves
        Leaf leaf;
        leaf = new Leaf("regex:^\\d+x\\d+$");
        node.leaves.add(leaf);
        leaf = new Leaf("regex:^w\\d+$");
        node.leaves.add(leaf);
        leaf = new Leaf("regex:^h\\d+$");
        node.leaves.add(leaf);

        return node.to_map();
    }

    private Map get_leaf_by_w(String name, String indexer) throws BufferedImageResizedException {
        if (indexer == null)
            return this.get_leaf_by_w_meta(name);
        return this.get_leaf_by_w_data(name, indexer);
    }

    private ArrayList get_size(int w, int h) {
        ArrayList size = new ArrayList(2);
        size.add(new Integer(w));
        size.add(new Integer(h));
        return size;
    }

    private Map get_leaf_by_w_meta(String name) throws BufferedImageResizedException {
        // figure out w and h
        int w = -1;
        try {
            w = Integer.parseInt(name.substring(1));
        } catch (NumberFormatException nfe) {
            throw new BufferedImageResizedException("Unknown w10n entity: "+name);
        }
        double ratio = 1.0 * this.height / this.width;
        int h = (int) Math.ceil(ratio * w);
        // it is a leaf
        Leaf leaf = new Leaf(name);
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, this.get_size(w, h));
        leaf.attrs.add(attr);
        return leaf.to_map();
    }
    private Map get_leaf_by_w_data(String name, String indexer) throws BufferedImageResizedException {
        // figure out w and h
        int w = -1;
        try {
            w = Integer.parseInt(name.substring(1));
        } catch (NumberFormatException nfe) {
            throw new BufferedImageResizedException("Unknown w10n entity: "+name);
        }
        double ratio = 1.0 * this.height / this.width;
        int h = (int) Math.ceil(ratio * w);
        // resize
        HashMap map = new HashMap();
        try {
            map.put(Constant.DATA, this.slicer.slice(this.resizer.resize(this.bufferedImage, w, h), indexer));
        } catch (ImageSlicerException ise) {
            throw new BufferedImageResizedException(ise);
        }
        return map;
    }

    private Map get_leaf_by_h(String name, String indexer) throws BufferedImageResizedException {
        if (indexer == null)
            return this.get_leaf_by_h_meta(name);
        return this.get_leaf_by_h_data(name, indexer);
    }
    private Map get_leaf_by_h_meta(String name) throws BufferedImageResizedException {
        // figure out w and h
        int h = -1;
        try {
            h = Integer.parseInt(name.substring(1));
        } catch (NumberFormatException nfe) {
            throw new BufferedImageResizedException("Unknown w10n entity: "+name);
        }
        double ratio = 1.0 * this.width / this.height;
        int w = (int) Math.ceil(ratio * h);
        // it is a leaf
        Leaf leaf = new Leaf(name);
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, this.get_size(w, h));
        leaf.attrs.add(attr);
        return leaf.to_map();
    }
    private Map get_leaf_by_h_data(String name, String indexer) throws BufferedImageResizedException {
        // figure out w and h
        int h = -1;
        try {
            h = Integer.parseInt(name.substring(1));
        } catch (NumberFormatException nfe) {
            throw new BufferedImageResizedException("Unknown w10n entity: "+name);
        }
        double ratio = 1.0 * this.width / this.height;
        int w = (int) Math.ceil(ratio * h);
        // resize
        HashMap map = new HashMap();
        try {
            map.put(Constant.DATA, this.slicer.slice(this.resizer.resize(this.bufferedImage, w, h), indexer));
        } catch (ImageSlicerException ise) {
            throw new BufferedImageResizedException(ise);
        }
        return map;
    }

    private Map get_leaf_by_w_and_h(String name, String indexer) throws BufferedImageResizedException {
        if (indexer == null)
            return this.get_leaf_by_w_and_h_meta(name);
        return this.get_leaf_by_w_and_h_data(name, indexer);
    }
    private Map get_leaf_by_w_and_h_meta(String name) throws BufferedImageResizedException {
        // figure out w and h
        int w = -1, h = -1;
        //int idx = name.indexOf("x");
        int idx = this.get_index_of_multiplier(name);
        try {
            w = Integer.parseInt(name.substring(0, idx));
            h = Integer.parseInt(name.substring(idx+1));
        } catch (NumberFormatException nfe) {
            throw new BufferedImageResizedException("Unknown w10n entity: "+name);
        }
        // it is a leaf
        Leaf leaf = new Leaf(name);
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, this.get_size(w, h));
        leaf.attrs.add(attr);
        return leaf.to_map();
    }
    private Map get_leaf_by_w_and_h_data(String name, String indexer) throws BufferedImageResizedException {
        // figure out w and h
        int w = -1, h = -1;
        //int idx = name.indexOf("x");
        int idx = this.get_index_of_multiplier(name);
        try {
            w = Integer.parseInt(name.substring(0, idx));
            h = Integer.parseInt(name.substring(idx+1));
        } catch (NumberFormatException nfe) {
            throw new BufferedImageResizedException("Unknown w10n entity: "+name);
        }
        // resize
        HashMap map = new HashMap();
        try {
            map.put(Constant.DATA, this.slicer.slice(this.resizer.resize(this.bufferedImage, w, h), indexer));
        } catch (ImageSlicerException ise) {
            throw new BufferedImageResizedException(ise);
        }
        return map;
    }
}
