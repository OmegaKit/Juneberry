package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.awt.image.BufferedImage;

import the.treevotee.Constant;
import the.treevotee.store.read.Reader;

import the.treevotee.Attribute;
import the.treevotee.Node;
import the.treevotee.Leaf;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import jpl.mipl.wiio.proc.Processor;
import jpl.mipl.wiio.proc.ProcessorClassFactory;

/**
 * @author Xing
 */

//public class ImageExposer extends Reader {
public class ImageExposer {

    private BufferedImage bufferedImage = null;
    private int width = -1;
    private int height = -1;
    private int numberOfBands = -1;

    private ArrayList attrList = new ArrayList();

    private ProcessorClassFactory processorClassFactory = null;

    public ImageExposer(int width, int height) {
        this(width, height, 1);
    }

    public ImageExposer(int width, int height, int numberOfBands) {
        this.width = width;
        this.height = height;
        this.numberOfBands = numberOfBands;
    }

    public ImageExposer(int[] size) {
        this(size[0], size[1], size[2]);
    }

    public ImageExposer(BufferedImage bufferedImage) {
        this(bufferedImage.getWidth(),
            bufferedImage.getHeight(),
            bufferedImage.getSampleModel().getNumBands()
            );
        //this.width = bufferedImage.getWidth();
        //this.height = bufferedImage.getHeight();
        //this.numberOfBands = bufferedImage.getSampleModel().getNumBands();

        this.bufferedImage = bufferedImage;
    }

    public void set_processor_class_factory(ProcessorClassFactory factory) {
        this.processorClassFactory = (ProcessorClassFactory)factory;
    }

    public void add_attribute(Attribute attribute) {
        this.attrList.add(attribute);
    }

    // a util to slice an array
    private String[] slice_array(String[] names, int offset, int length) {
        String[] foos = new String[length];
        System.arraycopy(names, offset, foos, 0, length);
        return foos;
    }

    // ref:
    // http://blog.julipedia.org/2005/11/java-dynamic-class-loading-part-2.html
    private Processor get_processor(String type) {
        Class clazz = this.processorClassFactory.getClass(type);
        if (clazz == null)
            return null;
        //System.out.println(clazz.getName());
        // currently, there is no argument to class constructor.
        //Class[] proto = new Class[0];
        //proto[0] = String.class;
        //proto[1] = String.class;
        //Object[] params = new Object[2];
        //params[0] = sid;
        //params[1] = path;
        Processor processor = null;
        try {
            //processor = (Processor)clazz.getConstructor(proto).newInstance(params);
            processor = (Processor)clazz.newInstance();
        } catch (Exception e) {
            // nothing
            e.printStackTrace();
        }
        return processor;
    }

    public Map get(String[] names, boolean traverse, String indexer) throws ImageExposerException {
        String entityName = names[0];
        // sanity check
        if (!entityName.equals(""))
            throw new ImageExposerException("Internal inconsistency");

        if (names.length == 1) {
            if (indexer == null)
                return this.get_node_top_meta(traverse);
            throw new ImageExposerException("Data unsupported for node "+entityName);
        }

        String[] foos = this.slice_array(names, 1, names.length-1);
        if (foos[0].equals(jpl.mipl.wiio.Constant.LEAF_IMAGE)) {
            if (foos.length != 1)
                throw new ImageExposerException("W10n leaf "+foos[0]+" does not have sub-entity"+foos[1]);
            return this.get_leaf_image(indexer);
        }
        if (foos[0].equals(jpl.mipl.wiio.Constant.NODE_RESIZED))
            return this.get_node_resized(foos, traverse, indexer);
        if (foos[0].equals(jpl.mipl.wiio.Constant.NODE_STRETCHED))
            return this.get_node_stretched(foos, traverse, indexer);

        if (foos[0].startsWith("band"))
            return this.get_node_band(foos, traverse, indexer);

        throw new ImageExposerException("Unknown w10n entity: "+foos[0]);
    }

    private Map get_node_top_meta(boolean traverse) throws ImageExposerException {
        Node node = new Node("");

        // attibutes
        // (*) size = [w, h]
        ArrayList size = new ArrayList(2);
        size.add(new Integer(this.width));
        size.add(new Integer(this.height));
        size.add(new Integer(this.numberOfBands));
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, size);
        node.attrs.add(attr);
        // more attributes
        node.attrs.addAll(attrList);
        //for (int i=0; i<attrList.size(); i++) {
        //    node.attrs.add(attrList.get(i));
        //}

        // leaves
        node.leaves.add(new Leaf(jpl.mipl.wiio.Constant.LEAF_IMAGE));

        // nodes
        node.nodes.add(new Node(jpl.mipl.wiio.Constant.NODE_METADATA));
        node.nodes.add(new Node(jpl.mipl.wiio.Constant.NODE_RESIZED));
        node.nodes.add(new Node(jpl.mipl.wiio.Constant.NODE_RASTER));
        // 20131204, xing, "stretched" disabled
        //node.nodes.add(new Node(jpl.mipl.wiio.Constant.NODE_STRETCHED));
        // 20131204, xing, "bands" disabled too
        // bands as sub nodes
        //for (int i=0; i<this.numberOfBands; i++) {
        //    node.nodes.add(new Node(""+i));
        //}

        
        //THIS SHOULD REALLY BE SOMEWHERE IN JUNEBERRY... (ntt, 071716)
        node.nodes.add(new Node(jpl.mipl.wiio.Constant.NODE_CMOD));
        
        
        
        return node.to_map();
    }

    private Map get_leaf_image(String indexer) throws ImageExposerException {
        if (indexer == null)
            return this.get_leaf_image_meta();
        return this.get_leaf_image_data(indexer);
    }

    private Map get_leaf_image_meta() throws ImageExposerException {
        Leaf leaf = new Leaf(jpl.mipl.wiio.Constant.LEAF_IMAGE);

        // attributes
        // (*) size = [w, h]
        ArrayList size = new ArrayList(2);
        size.add(new Integer(this.width));
        size.add(new Integer(this.height));
        size.add(new Integer(this.numberOfBands));
        Attribute attr = new Attribute(jpl.mipl.wiio.Constant.SIZE, size);
        leaf.attrs.add(attr);

        return leaf.to_map();
    }

    private Map get_leaf_image_data(String indexer) throws ImageExposerException {
        ImageSlicer slicer = new ImageSlicer();
        HashMap map = new HashMap();
        try {
            map.put(Constant.DATA, slicer.slice(this.bufferedImage, indexer));
        } catch (ImageSlicerException ise) {
            throw new ImageExposerException(ise);
        }
        return map;
    }

    private Map get_node_resized(String[] names, boolean traverse, String indexer) throws ImageExposerException {
        String entityName = names[0];
        // sanity check
        if (!entityName.equals(jpl.mipl.wiio.Constant.NODE_RESIZED))
            throw new ImageExposerException("Internal inconsistency");

        BufferedImageResized bir;
        if (indexer == null) {
            bir = new BufferedImageResized(this.width, this.height);
        } else {
            bir = new BufferedImageResized(this.bufferedImage);
        }
        //String[] foos = slice_array(names, 0, names.length);
        //foos[0] = "";
        names[0] = "";
        Map map = null;
        try {
            map = bir.get(names, traverse, indexer);
        } catch (BufferedImageResizedException bire) {
            throw new ImageExposerException(bire);
        }
        // if meta
        if (indexer == null && names.length == 1)
            map.put(Constant.NAME, entityName);

        return map;
    }

    private Map get_node_stretched(String[] names, boolean traverse, String indexer) throws ImageExposerException {
        String entityName = names[0];
        // sanity check
        if (!entityName.equals(jpl.mipl.wiio.Constant.NODE_STRETCHED))
            throw new ImageExposerException("Internal inconsistency");

        BufferedImageStretched bis;
        if (indexer == null) {
            bis = new BufferedImageStretched(this.width, this.height);
        } else {
            bis = new BufferedImageStretched(this.bufferedImage);
        }

        // figure out and set up the stretcher class that does the real job.
        Processor stretcher = this.get_processor(Processor.TYPE_STRETCH);
        if (stretcher == null)
            throw new ImageExposerException("Internal inconsistency: failed to get class for stretcher");
        bis.set_stretcher(stretcher);

        //String[] foos = slice_array(names, 0, names.length);
        //foos[0] = "";
        names[0] = "";
        Map map = null;
        try {
            map = bis.get(names, traverse, indexer);
        } catch (BufferedImageStretchedException bise) {
            throw new ImageExposerException(bise);
        }
        // if meta
        if (indexer == null && names.length == 1)
            map.put(Constant.NAME, entityName);

        return map;
    }

    private Map get_node_band(String[] names, boolean traverse, String indexer) throws ImageExposerException {
        String entityName = names[0];
        // sanity check
        if (!entityName.startsWith("band"))
            throw new ImageExposerException("Internal inconsistency");

        BufferedImageResized bir;
        if (indexer == null) {
            bir = new BufferedImageResized(this.width, this.height);
        } else {
            bir = new BufferedImageResized(this.bufferedImage);
        }
        //String[] foos = slice_array(names, 0, names.length);
        //foos[0] = "";
        names[0] = "";
        Map map = null;
        try {
            map = bir.get(names, traverse, indexer);
        } catch (BufferedImageResizedException bire) {
            throw new ImageExposerException(bire);
        }
        // if meta
        if (indexer == null && names.length == 1)
            map.put(Constant.NAME, entityName);

        return map;
    }
}
