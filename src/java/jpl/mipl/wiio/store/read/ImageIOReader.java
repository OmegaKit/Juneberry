package jpl.mipl.wiio.store.read;

/*
 * Copyright (c) 2011 - 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import jpl.mipl.wiio.util.Util;

//import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;

import javax.imageio.stream.ImageInputStream;
//import javax.imageio.ImageWriter;
//import javax.imageio.stream.ImageOutputStream;

import javax.imageio.ImageReadParam;

import javax.imageio.spi.IIORegistry;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import java.io.File;
import java.io.IOException;

//import jpl.mipl.wiio.Constant;

import javax.media.jai.*;

import java.awt.image.DataBuffer;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import the.treevotee.Constant;
import the.treevotee.store.read.Reader;
//import the.treevotee.store.read.ReaderException;

import the.treevotee.Attribute;
import the.treevotee.Node;
import the.treevotee.Leaf;

import the.treevotee.SimpleLogger;

import jpl.mipl.wiio.proc.ProcessorClassFactory;

import jpl.mipl.wiio.SimpleConvertBandSelect;

/**
 * @author Xing
 */
public abstract class ImageIOReader extends Reader {

    protected ImageReader reader = null;

    private ProcessorClassFactory processorClassFactory = null;

    private final SimpleLogger logger = SimpleLogger.getLogger(ImageIOReader.class);

    //private String path = null;

    public ImageIOReader(String sid, String path, java.util.Map config) {
    //public ImageIOReader() {
        super(sid, path, config);
        //this.path = path;

        /*
        ImageInputStream iis = null;
        try {
            iis = ImageIO.createImageInputStream(new File(this.path));
            Iterator readers = ImageIO.getImageReaders(iis);
            this.reader = (ImageReader)readers.next();
            this.reader.setInput(iis);
        } catch (IOException ioe) {
            //throw new ImageIOReaderException(ioe);
            this.reader = null;
        }
        */

        this.processorClassFactory = new ProcessorClassFactory();
    }

    public void set_processor_class_factory(ProcessorClassFactory factory) {
        this.processorClassFactory = factory;
    }

    // get size of the i-th image
    // can be overridden by subclass
    protected int[] get_size1(int i) throws ImageIOReaderException {
        try {
            System.out.println("dsd: "+this.reader.getRawImageType(i));
            return new int[] {
                this.reader.getWidth(i),
                this.reader.getHeight(i),
                this.reader.getRawImageType(i).getNumBands()
                };
        } catch (IOException ioe) {
            throw new ImageIOReaderException(ioe);
        } 
    }

    protected int[] get_size(int i) throws ImageIOReaderException {
      try {
        RenderedImage ri = this.reader.readAsRenderedImage(i, this.reader.getDefaultReadParam());
        return new int[] {ri.getWidth(), ri.getHeight(), ri.getSampleModel().getNumBands()};
      } catch (IOException ioe) {
        throw new ImageIOReaderException(ioe);
      }
    }

    // get metadata of i-th image.
    // can be overridden by subclass
    protected Object get_metadata(int i) throws ImageIOReaderException {
        IIOMetadata meta;
      try {
        meta = this.reader.getImageMetadata(i);
      } catch (IOException ioe) {
        throw new ImageIOReaderException(ioe);
      }
        return this.get_iiometa_as_map(meta);
    }

    // get BufferedImage of the i-th image
    // can be overridden by subclass
    protected BufferedImage get_buffered_image(int i) throws ImageIOReaderException {
        try {
            return this.reader.read(i, null);
        } catch (IOException ioe) {
            throw new ImageIOReaderException(ioe);
        }
    }

    // get BufferedImage of the i-th image on selected bands
    // can be overridden by subclass
    protected BufferedImage get_buffered_image(int i, int[] bands) throws ImageIOReaderException {
       try {
        ImageReadParam param = this.reader.getDefaultReadParam();
        RenderedImage renderedImage = this.reader.readAsRenderedImage(i, param);

        //SampleModel sm = renderedImage.getSampleModel();
        //RenderedImage bandSelectImg = null;

        ParameterBlockJAI bandSelectPB = new ParameterBlockJAI("BandSelect");
        bandSelectPB.addSource(renderedImage);
        bandSelectPB.setParameter("bandIndices", bands);
        RenderedImage bandSelectImg = JAI.create("BandSelect", bandSelectPB);

        // tmp hack -- start
        int w = bandSelectImg.getWidth();
        int h = bandSelectImg.getHeight();
        int[] data = new int[w*h];
        //int[] data = bandSelectImg.getData().getSamples(0, 0, w, h, 0, null);
        bandSelectImg.getData().getSamples(0, 0, w, h, 0, data);
        for (int k=0; k<data.length; k++) {
            System.out.print(data[k]+",");
        }
        // tmp hack -- end

        SimpleConvertBandSelect scbs = new SimpleConvertBandSelect();
        int outputDataType = DataBuffer.TYPE_BYTE;
        boolean rescaleOnFormat = true;
        RenderedImage ri = scbs.processFormat(bandSelectImg, outputDataType, rescaleOnFormat);
        return ((RenderedOp)ri).getAsBufferedImage();
       } catch (IOException ioe) {
        throw new ImageIOReaderException(ioe);
       }
    }

    // iio meta (standard, native, extra) as a map,
    // in which, each member is keyed by formatName
    private Map get_iiometa_as_map(IIOMetadata meta) throws ImageIOReaderException {

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
                throw new ImageIOReaderException("Internal inconsistency: repeated metadata format name: "+formatName);
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
                throw new ImageIOReaderException("Internal inconsistency: repeated iio metadata format name: "+formatName);
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

    // a util to slice an array
    private String[] slice_array(String[] names, int offset, int length) {
        String[] foos = new String[length];
        System.arraycopy(names, offset, foos, 0, length);
        return foos;
    }

    // name should be w10n full name such as "", ""/a, ""/a/b, ""/a/b/c
    public Map getMeta(String name) throws ImageIOReaderException {
        String[] names = name.split("/");
        boolean traverse = false;
        return this.get_node_top(names, traverse, null);
    }
    // name should be w10n full name such as "", ""/a, ""/a/b, ""/a/b/c
    public Map getData(String name, String indexer) throws ImageIOReaderException {
        String[] names = name.split("/");
        return this.get_node_top(names, false, indexer);
    }

    // imageio and friends are not so kind when it comes to clean up resources.
    // ref: http://info.michael-simons.eu/2012/01/25/the-dangers-of-javas-imageio/
    // do imageio resource cleanup
    // via implementing parent class' abstract method dispose()
    public void dispose() {
        if (this.reader == null) {
            this.logger.debug("image reader is null, no imageio resource to clean up");
            // parent's, maybe unnecessary.
            this.sid = null;
            this.path = null;
            this.input = null;
            return;
        }

        // ImageReader caches some data in files.
        // It is essential to dispose the reader AND the underlying
        // ImageInputStream after use if it's not needed anymore like so
        //
        // If it isn't closed and disposed,
        // the temporary cache files are either deleted not at all or
        // maybe at the next garbage collection.
        // I was able to bring down a VM by
        // "java.io.FileNotFoundException: (Too many open files)" several times
        // because i didn't close a reader in a loop.
        // Even the classloader wasn't able to load any new classes
        // after the ImageReader going mad on the file handle.
        Object inputObj = this.reader.getInput();
      try {
        if (inputObj != null && inputObj instanceof ImageInputStream) {
            ImageInputStream iis = (ImageInputStream)inputObj;
            iis.close();
            this.logger.debug(iis.getClass().getName()+" closed");
        } else {
            if (inputObj == null) {
                this.logger.debug("image reader input object is null. no need to clean up");
            } else {
                this.logger.warn(inputObj.getClass().getName()+" is not cleaned");
            }
        }
      } catch (IOException ioe) {
            this.logger.warn("exception ignored "+ioe);
      }
        this.reader.dispose();
        this.logger.debug(this.reader.getClass().getName()+" disposed");
        this.reader = null;

        // The other side is the javax.imageio.ImageWriter.
        // There is an issue mentioned in the tomcat wiki
        // http://wiki.apache.org/tomcat/FAQ/KnownIssues#ImageIOIssues
        // We have not used javax.imageio.ImageWriter yet,
        // but keep our fingers x'ed.

        // parent's, maybe unnecessary.
        this.sid = null;
        this.path = null;
        this.input = null;
    }

    private Map get_node_top(String[] names, boolean traverse, String indexer) throws ImageIOReaderException {
        String entityName = names[0];
        // sanity check
        if (!entityName.equals(""))
            throw new ImageIOReaderException("Internal inconsistency");

        if (names.length == 1) {
            if (indexer == null)
                return this.get_node_top_meta(traverse);
            throw new ImageIOReaderException("Data unsupported for node "+entityName);
        } 

        String[] foos = this.slice_array(names, 1, names.length-1);
        return this.get_node_imgnum(foos, traverse, indexer);
    }

    // get meta of top node
    private Map get_node_top_meta(boolean traverse) throws ImageIOReaderException {
        Node node = new Node("");

        // 20120102, xing, tmp hack for non-imageio reader
        if (this.reader == null) {
            Attribute attr;
            attr = new Attribute("plugin", "non-imageio reader");
            node.attrs.add(attr);
            // assume there is only one image
            Node x = new Node(Integer.toString(0));
            node.nodes.add(x);
            return node.to_map();
        }

      try {
        // attibutes
        Attribute attr;
        // plugin used
        attr = new Attribute("plugin", this.reader.getClass().getName());
        node.attrs.add(attr);
        // stream metadata
        IIOMetadata meta = this.reader.getStreamMetadata();
        attr = new Attribute("streammetadata", this.get_iiometa_as_map(meta));
        node.attrs.add(attr);

        // leaves
        // none now

        // nodes
        int n = this.reader.getNumImages(true);
        for (int i=0; i<n; i++) {
            Node x = new Node(Integer.toString(i));
            node.nodes.add(x);
        }
      } catch (IOException ioe) {
        new ImageIOReaderException(ioe);
      }

        return node.to_map();
    }

    private Map get_node_imgnum(String[] names, boolean traverse, String indexer) throws ImageIOReaderException {
        String entityName = names[0];
        int i = 0;
        try {
            i = Integer.parseInt(entityName);
        } catch (java.lang.NumberFormatException nfe) {
            throw new ImageIOReaderException("Unknown w10n entity: "+entityName);
        }

        // /imgnum/metadata as node
        // names looks like  ["0", "metadata", ...]
        if (names.length > 1 && names[1].equals(jpl.mipl.wiio.Constant.NODE_METADATA)) {
            // get a list of names acceptable to MetadataExposer.get()
            // foos looks like ["metadata", ...]
            String[] foos = this.slice_array(names, 1, names.length-1);
            foos[0] = "";
            // now foos looks like ["", ...]
            int imageIndex = i;
            MetadataExposer metadataExposer = new MetadataExposer(this.reader, i);
            try {
                // meda == meta or data
                Map meda = metadataExposer.get(foos, traverse, indexer);
                // if data
                if (indexer != null)
                    return meda;
                // otherwise meta, reset name if necessary
                if (foos.length == 1)
                    meda.put(Constant.NAME, jpl.mipl.wiio.Constant.NODE_METADATA);
                return meda;
            } catch (MetadataExposerException ree) {
                throw new ImageIOReaderException(ree);
            }
        }

        // /imgnum/raster as node
        // names looks like  ["0", "raster", ...]
        if (names.length > 1 && names[1].equals(jpl.mipl.wiio.Constant.NODE_RASTER)) {
            // get a list of names acceptable to Rasterexposer.get()
            // foos looks like ["raster", ...]
            String[] foos = this.slice_array(names, 1, names.length-1);
            foos[0] = "";
            // now foos looks like ["", ...]
            int imageIndex = i;
            boolean getRasterViaBufferedImage = true;
            RasterExposer rasterExposer = new RasterExposer(this.reader, i, getRasterViaBufferedImage);
            try {
                // meda == meta or data
                Map meda = rasterExposer.get(foos, traverse, indexer);
                // if data
                if (indexer != null)
                    return meda;
                // otherwise meta, reset name if necessary
                if (foos.length == 1)
                    meda.put(Constant.NAME, jpl.mipl.wiio.Constant.NODE_RASTER);
                return meda;
            } catch (RasterExposerException ree) {
                throw new ImageIOReaderException(ree);
            }
        }
        
       
        //===============================================
        
        // /imgnum/cmod as node
        // names looks like  ["0", "cmod", ...]
        if (names.length > 1 && names[1].equals(jpl.mipl.wiio.Constant.NODE_CMOD)) {
            // get a list of names acceptable to CameraModelExposer.get()
            // foos looks like ["cmod", ...]
            String[] foos = this.slice_array(names, 1, names.length-1);
            foos[0] = "";
            // now foos looks like ["", ...]
            int imageIndex = i;
            CameraModelExposer cmodExposer = new CameraModelExposer(this.reader, i);
            try {
                // meda == meta or data
                Map meda = cmodExposer.get(foos, traverse, indexer);
                
                // if data
                if (indexer != null)
                    return meda;
                
                // otherwise meta, reset name if necessary
                if (foos.length == 1)
                    meda.put(Constant.NAME, jpl.mipl.wiio.Constant.NODE_CMOD);
                return meda;
            } catch (CameraModelExposerException  cmee) {
                throw new ImageIOReaderException(cmee);
            }
        }
        

        //==================================================
        
        
        // handle single band or combo bands with name like
        // "0", "1", "0-23-45"
        if (names.length > 1) {
            String subEntity = names[1];
            char c = subEntity.charAt(0);
            if (c >= '0' && c <= '9') {
                String[] foos = this.slice_array(names, 1, names.length-1);
                return this.get_node_band_selected(i, foos, traverse, indexer);
            }
        }

        names[0] = "";
        // meta of itself or any sub leaf or node
        if (indexer == null) {
            ImageExposer imageExposer = new ImageExposer(this.get_size(i));
            imageExposer.set_processor_class_factory(this.processorClassFactory);
            if (names.length == 1) {
                Attribute attr = new Attribute("metadata", this.get_metadata(i));
                imageExposer.add_attribute(attr);
            }
            Map meta = null;
            try {
                meta = imageExposer.get(names, traverse, null);
            } catch (ImageExposerException iee) {
                throw new ImageIOReaderException(iee);
            }
            if (names.length == 1)
                meta.put(Constant.NAME, entityName);
            return meta;
        }

        // data of any sub leaf
        ImageExposer imageExposer = new ImageExposer(this.get_buffered_image(i));
        imageExposer.set_processor_class_factory(this.processorClassFactory);
        Map data = null;
        try {
            data = imageExposer.get(names, false, indexer);
        } catch (ImageExposerException iee) {
            throw new ImageIOReaderException(iee);
        }
        return data;
    }

    private Map get_node_band_selected(int i, String[] names, boolean traverse, String indexer) throws ImageIOReaderException {
        String entityName = names[0];
        String[] a = entityName.split("-");
        int[] bands = new int[a.length];
        try {
            for (int k=0; k<bands.length; k++) {
                bands[k] = Integer.parseInt(a[k]);
            }
            System.err.println("bands: "+bands);
        } catch (java.lang.NumberFormatException nfe) {
            throw new ImageIOReaderException("Unknown w10n entity: "+entityName);
        }

        names[0] = "";
        int[] size = this.get_size(i);
        size[2] = bands.length;
        // meta of itself or any sub leaf or node
        if (indexer == null) {
            ImageExposer imageExposer = new ImageExposer(size);
            imageExposer.set_processor_class_factory(this.processorClassFactory);
            // no extra meta for this
            //if (names.length == 1) {
            //    Attribute attr = new Attribute("metadata", this.get_metadata(i));
            //    imageExposer.add_attribute(attr);
            //}
            Map meta = null;
            try {
                meta = imageExposer.get(names, traverse, null);
            } catch (ImageExposerException iee) {
                throw new ImageIOReaderException(iee);
            }
            if (names.length == 1)
                meta.put(Constant.NAME, entityName);
            return meta;
        }

        // data of any sub leaf
        ImageExposer imageExposer = new ImageExposer(this.get_buffered_image(i, bands));
        imageExposer.set_processor_class_factory(this.processorClassFactory);
        Map data = null;
        try {
            data = imageExposer.get(names, false, indexer);
        } catch (ImageExposerException iee) {
            throw new ImageIOReaderException(iee);
        }
        return data;
    }

    /*
    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: ImageIOReader sid path identifier");
            System.exit(1);
        }

        String sid = args[0];
        String path = args[1];
        String identifier = args[2];

        ImageIOReader imageIOReader = new ImageIOReader(sid, path);
        Map obj = imageIOReader.get(identifier);
        System.out.print(org.json.simple.JSONValue.toJSONString(obj));
    }
    */
}
