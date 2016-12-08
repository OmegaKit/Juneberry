package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.DataBuffer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
//import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.metadata.IIOMetadata;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;

import java.util.Iterator;

import java.io.File;
import java.io.IOException;

import the.treevotee.Attribute;
import the.treevotee.Node;
import the.treevotee.Leaf;

import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

//import the.treevotee.Constant;
//import the.treevotee.store.read.Reader;
//import the.treevotee.store.read.ReaderException;

import the.treevotee.SimpleLogger;

import jpl.mipl.wiio.util.Util;

/**
 * @author Xing
 *
 * This class webifies metadata via imageio image reader as
 * node: ""
 * leaf: ""/..
 * node: ""/../..
 *
 */

public class MetadataExposer {

    private ImageReader imageReader = null;
    private int imageIndex = -1;

    private final SimpleLogger logger = SimpleLogger.getLogger(MetadataExposer.class);

    // this one is needed to make sure subclass can call main()
    protected MetadataExposer() {}

    public MetadataExposer(ImageReader imageReader, int imageIndex) {
        this.imageReader = imageReader;
        this.imageIndex = imageIndex;
    }

    // parse indexer in the form of
    // lineStart:lineEnd, sampleStart:sampleEnd, bandStart:bandEnd
    // or
    // lineStart:lineEnd:lineStep, sampleStart:sampleEnd:sampleStep, bandStart:bandEnd:bandStep
    // into [lineStart, lineEnd, lineStep, sampleStart, sampleEnd, sampleStep, bandStart, bandEnd, bandStep]
    private int[] parse_range_indexer(String indexer) throws MetadataExposerException {
        String[] tmp = indexer.split(",");
        if (tmp.length != 3)
            throw new MetadataExposerException("Invalid indexer "+indexer);
        String[] la = tmp[0].split(":");
        String[] sa = tmp[1].split(":");
        String[] ba = tmp[2].split(":");
        if (la.length < 2 || la.length > 3 || sa.length < 2 || sa.length > 3 || ba.length < 2 || ba.length > 3)
            throw new MetadataExposerException("Wrong indexer format "+indexer);

        int lineStart  = Integer.parseInt(la[0]);
        int lineEnd    = Integer.parseInt(la[1]);
        int lineStep   = 1;
        if (la.length == 3)
            lineStep   = Integer.parseInt(la[2]);
        //
        int sampleStart = Integer.parseInt(sa[0]);
        int sampleEnd   = Integer.parseInt(sa[1]);
        int sampleStep  = 1;
        if (sa.length == 3)
            sampleStep  = Integer.parseInt(sa[2]);
        //
        int bandStart  = Integer.parseInt(ba[0]);
        int bandEnd    = Integer.parseInt(ba[1]);
        int bandStep   = 1;
        if (ba.length == 3)
            bandStep   = Integer.parseInt(ba[2]);

        return new int[] {lineStart, lineEnd, lineStep, sampleStart, sampleEnd, sampleStep, bandStart, bandEnd, bandStep};
    }

    private int[] parse_indexer(String indexer) throws MetadataExposerException {
        if (indexer.startsWith("("))
            throw new MetadataExposerException("Tile indexer is not supported");
        return this.parse_range_indexer(indexer);
    }

    // a util to slice an array
    private String[] slice_array(String[] names, int offset, int length) {
        String[] foos = new String[length];
        System.arraycopy(names, offset, foos, 0, length);
        return foos;
    }

    // iio meta (standard, native, extra) as a map,
    // in which, each member is keyed by formatName
    private Map get_iiometa_as_map(IIOMetadata meta) throws MetadataExposerException {

        LinkedHashMap map = new LinkedHashMap();
        if (meta == null)
            return map;

        // standard and native formats, which should never be null
        String[] names = meta.getMetadataFormatNames();
      // in dcm4che imageio for dicom, this is null ;-(
      if (names != null) {
        for (int i=0; i<names.length; i++) {
            String formatName = names[i];
            if (map.containsKey(formatName))
                throw new MetadataExposerException("Internal inconsistency: repeated metadata format name: "+formatName);
            org.w3c.dom.Node tree = meta.getAsTree(formatName);
            if (tree == null)
                continue;
            LinkedHashMap x = new LinkedHashMap();
            Util util = new Util();
            util.node_to_map(tree, x);
            map.put(formatName, x);
        }
      }

        // extra formats, which could be null
        names = meta.getExtraMetadataFormatNames();
      if (names != null) {
        for (int i=0; i<names.length; i++) {
            String formatName = names[i];
            if (map.containsKey(formatName))
                throw new MetadataExposerException("Internal inconsistency: repeated iio metadata format name: "+formatName);
            org.w3c.dom.Node tree = meta.getAsTree(formatName);
            if (tree == null)
                continue;
            LinkedHashMap x = new LinkedHashMap();
            Util util = new Util();
            util.node_to_map(tree, x);
            map.put(formatName, x);
        }
      }

        return map;
    }

    private Map get_node_top_meta() throws MetadataExposerException {

        IIOMetadata meta;
      try {
        meta = this.imageReader.getImageMetadata(this.imageIndex);
      } catch (IOException ioe) {
        throw new MetadataExposerException(ioe);
      }
        Map map = this.get_iiometa_as_map(meta);

        Node node = new Node("");
        //node.attrs.add(new Attribute("test", org.json.simple.JSONValue.toJSONString(map)));

        // leaves
        // currently no leaf
        //node.leaves.add(new Leaf("foo"));

        // metadata formats are subnodes
        String[] names = meta.getMetadataFormatNames();
        for (int i=0; i<names.length; i++) {
            node.nodes.add(new Node(names[i]));
        }

        // extra metadata formats are subnodes, but not supported yet.
        String[] extraNames = meta.getExtraMetadataFormatNames();
        if (extraNames != null) {
            node.nodes.add(new Node("UnsupportedExtraMetadataFormatName"));
        }

        //String formatName = names[0];
        //org.w3c.dom.Node tree = meta.getAsTree(formatName);

        return node.to_map();
    }

    private Map[] iio_metadata_to_map_list(String[] names) throws MetadataExposerException {
        IIOMetadata meta;
      try {
        meta = this.imageReader.getImageMetadata(this.imageIndex);
      } catch (IOException ioe) {
        throw new MetadataExposerException(ioe);
      }

        // names[0] should be "", namely empty string
        String metadataFormatName = names[1];
        org.w3c.dom.Node tree = meta.getAsTree(metadataFormatName);
        if (tree == null)
            throw new MetadataExposerException("Unknown node "+metadataFormatName);
        Map metaMap = null;
        Map dataMap = null;
      try {
        // fixme: this is bad, wiio should not depend on juneberry. Fix later.
        //jpl.mipl.juneberry.util.Util2.node2xml(tree);
        Map[] tmp = jpl.mipl.juneberry.util.Util2.node_to_map_list(tree);
        metaMap = tmp[0];
        //System.out.print(org.json.simple.JSONValue.toJSONString(metaMap));
        dataMap = tmp[1];
        //System.out.print(org.json.simple.JSONValue.toJSONString(dataMap));
      } catch (IOException ioe) {
        throw new MetadataExposerException(ioe);
      }

        return new Map[]{metaMap, dataMap};

    }

    private Map get_node_level1_meta(String[] names) throws MetadataExposerException {

        Map[] tmp = this.iio_metadata_to_map_list(names);
        Map metaMap = tmp[0];
        //Map dataMap = tmp[1];

        Node node = new Node(names[1]);

        // attributes
        //node.attrs.add(new Attribute("foo", "bar"));
        // none for now

        // leaves
        //node.leaves.add(new Leaf("foo"));
        // none for now

        // subnodes
        // node.nodes.add(new Node("foo"));
        List list = (List)metaMap.get("/");
        String nodeName = null;
        for (int i=0; i<list.size(); i++) {
            nodeName = (String)list.get(i);
            node.nodes.add(new Node(nodeName));
        }

        // furthermore, should look for anything inside extra metadata formats?
        // names = meta.getExtraMetadataFormatNames();
        //if (names != null) {
        //    node.nodes.add(new Node("UnsupportedExtraMetadataFormatName"));
        //}

        return node.to_map();
    }

    private Map get_node_level2_meta(String[] names) throws MetadataExposerException {

        Map[] tmp = this.iio_metadata_to_map_list(names);
        Map metaMap = tmp[0];
        //System.out.print(org.json.simple.JSONValue.toJSONString(metaMap));
        //Map dataMap = tmp[1];
        //System.out.print(org.json.simple.JSONValue.toJSONString(dataMap));

        Node node = new Node(names[2]);

        // attributes
        //node.attrs.add(new Attribute("foo", "bar"));
        // none for now

        // leaves
        //node.leaves.add(new Leaf("foo"));
        String identifier = "/" + names[2] + "/";
        List list = (List)metaMap.get(identifier);
        String leafName = null;
        for (int i=0; i<list.size(); i++) {
            leafName = (String)list.get(i);
            node.leaves.add(new Leaf(leafName));
        }

        // subnodes
        // node.nodes.add(new Node("foo"));
        // none for now

        // furthermore, should look for anything inside extra metadata formats?
        // names = meta.getExtraMetadataFormatNames();
        //if (names != null) {
        //    node.nodes.add(new Node("UnsupportedExtraMetadataFormatName"));
        //}

        return node.to_map();
    }

    private Map get_leaf_level3_meta(String[] names) throws MetadataExposerException {

        Map[] tmp = this.iio_metadata_to_map_list(names);
        Map metaMap = tmp[0];
        //System.out.print(org.json.simple.JSONValue.toJSONString(metaMap));

        Leaf leaf = new Leaf(names[3]);

        // attributes
        //node.attrs.add(new Attribute("foo", "bar"));
        // none for now

        return leaf.to_map();
    }

    private Map get_leaf_level3_data(String[] names, String indexer) throws MetadataExposerException {
        Map[] tmp = this.iio_metadata_to_map_list(names);
        Map dataMap = tmp[1];

        String identifier = "/" + names[2] + "/" + names[3] + "[]";
        System.out.println(identifier);
        String value = (String)dataMap.get(identifier);

        Map map = new HashMap();
        map.put("data", value);
        System.out.print(org.json.simple.JSONValue.toJSONString(map));

        return map;
    }

    public Map get(String[] names, boolean traverse, String indexer) throws MetadataExposerException {

        if (traverse)
            throw new MetadataExposerException("Traverse is not supported for metadata store");

        String entityName = names[0];
        // sanity check
        if (!entityName.equals(""))
            throw new MetadataExposerException("Internal inconsistency");

        if (names.length == 1) {
            if (indexer == null)
                return this.get_node_top_meta();
            throw new MetadataExposerException("Data unsupported for node "+entityName);
        }

        if (names.length == 2) {
            if (indexer == null)
                return this.get_node_level1_meta(names);
            entityName = names[1];
            throw new MetadataExposerException("Data unsupported for node "+entityName);
        }

        if (names.length == 3) {
            if (indexer == null)
                return this.get_node_level2_meta(names);
            entityName = names[2];
            throw new MetadataExposerException("Data unsupported for node "+entityName);
        }

        if (names.length == 4) {
            if (indexer == null)
                return this.get_leaf_level3_meta(names);
            return this.get_leaf_level3_data(names, indexer);
        }


        String[] foos = this.slice_array(names, 1, names.length-1);

        throw new MetadataExposerException("Unknown w10n entity: "+foos[0]);
    }

    public static void main(String[] args) throws MetadataExposerException {
        if (args.length != 7) {
            System.err.println("Usage: MetadataExposer inputPath inputFormat imageReaderIndex imageIndex entityName traverse(yes|no) indexer(null|...)");
            System.exit(-1);
        }

        String inputPath = args[0];
        String inputFormat = args[1];
        int readerIndex = Integer.parseInt(args[2]);
        int imageIndex = Integer.parseInt(args[3]);
        //
        String entityName = args[4];
        //
        boolean traverse = false;
        if (args[5].equals("yes"))
            traverse = true;
        //
        String indexer = null;
        if (!args[6].equals("null"))
            indexer = args[6];

        Iterator readers = null;
        ImageReader imageReader = null;
        ImageInputStream iis = null;
        ImageReadParam imageReadParam = null;

        Map map = null;

        int count = 0;
        try {
            iis = ImageIO.createImageInputStream(new File(inputPath));
            if (iis == null)
                throw new MetadataExposerException("file non-existent: "+inputPath);

            readers = ImageIO.getImageReadersByFormatName(inputFormat);
            while (readers.hasNext()) {
                Object obj = readers.next();
                System.err.println("found image reader "+count+" "+obj.getClass().getName());
                ImageReader reader = (ImageReader)obj;
                System.err.println("                   canReadRaster="+reader.canReadRaster());
                if (count == readerIndex)
                    imageReader = reader;
                count += 1;
            }
            if (imageReader == null) {
                throw new MetadataExposerException("no image reader with index "+readerIndex);
            } else {
                System.err.println("use image reader "+readerIndex+" "+imageReader.getClass().getName());
            }

            imageReader.setInput(iis);

            MetadataExposer rasterExposer = new MetadataExposer(imageReader, imageIndex);
            String[] names = entityName.split("/");
            map = rasterExposer.get(names, traverse, indexer);
        } catch (IOException ioe) {
            throw new MetadataExposerException(ioe);
        } finally {
            if (iis != null) try {
                System.err.println("close image input stream");
                iis.close();
            } catch (IOException ioe) {
                System.err.println("warning: "+ioe);
            }
            if (imageReader != null) {
                System.err.println("dispose image reader "+imageReader.getClass().getName());
                imageReader.dispose();
            }
        }

        if (map == null)
            return;

        try {
            Writer writer = null;
            // meta
            if (indexer == null) {
                writer = new the.treevotee.output.Json();
            // data
            } else {
                writer = new jpl.mipl.wiio.output.Array2Json();
            }
            Output output = writer.write(map);
            System.err.println(output.mimeType);
            System.out.println(new String(output.data));
        } catch (WriterException we) {
            throw new MetadataExposerException(we);
        }
    }
}
