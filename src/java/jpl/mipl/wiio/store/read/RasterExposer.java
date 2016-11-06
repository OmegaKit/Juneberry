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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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

/**
 * @author Xing
 *
 * This class webifies raster via imageio image reader as
 * node: ""
 * leaf: ""/data
 * node: ""/buffer
 * Please check doc/raster.txt for more.
 *
 */

public class RasterExposer {

    private ImageReader imageReader = null;
    private int imageIndex = -1;

    private boolean getRasterViaBufferedImage = false;

    private final SimpleLogger logger = SimpleLogger.getLogger(RasterExposer.class);

    // this one is needed to make sure subclass can call main()
    protected RasterExposer() {}

    public RasterExposer(ImageReader imageReader, int imageIndex) {
        this(imageReader, imageIndex, false);
    }

    public RasterExposer(ImageReader imageReader, int imageIndex, boolean getRasterViaBufferedImage) {
        this.imageReader = imageReader;
        this.imageIndex = imageIndex;
        this.getRasterViaBufferedImage = getRasterViaBufferedImage;
    }

    // parse indexer in the form of
    // lineStart:lineEnd, sampleStart:sampleEnd, bandStart:bandEnd
    // or
    // lineStart:lineEnd:lineStep, sampleStart:sampleEnd:sampleStep, bandStart:bandEnd:bandStep
    // into [lineStart, lineEnd, lineStep, sampleStart, sampleEnd, sampleStep, bandStart, bandEnd, bandStep]
    private int[] parse_range_indexer(String indexer) throws RasterExposerException {
        String[] tmp = indexer.split(",");
        if (tmp.length != 3)
            throw new RasterExposerException("Invalid indexer "+indexer);
        String[] la = tmp[0].split(":");
        String[] sa = tmp[1].split(":");
        String[] ba = tmp[2].split(":");
        if (la.length < 2 || la.length > 3 || sa.length < 2 || sa.length > 3 || ba.length < 2 || ba.length > 3)
            throw new RasterExposerException("Wrong indexer format "+indexer);

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

    private int[] parse_indexer(String indexer) throws RasterExposerException {
        if (indexer.startsWith("("))
            throw new RasterExposerException("Tile indexer is not supported");
        return this.parse_range_indexer(indexer);
    }

    // a util to slice an array
    private String[] slice_array(String[] names, int offset, int length) {
        String[] foos = new String[length];
        System.arraycopy(names, offset, foos, 0, length);
        return foos;
    }

    // for given sample model, use its dataType (int) to get w10n data type (string)
    //
    // there are various ways to obtain data type.
    // however, the api doc is no clear on what the differences are.
    // here they are assumed to be the same, but do check on the assumption.
    //
    // must be in sync with get_type_shape_array() below
    private String get_type(SampleModel sampleModel) throws RasterExposerException {
        // there are different types, are they same?
        int transferType = sampleModel.getTransferType();
        this.logger.debug("trasferType: "+transferType);
        //
        int dataType = sampleModel.getDataType();
        this.logger.debug("dataType: "+dataType);
        // from data buffer
        //int dataType = raster.getDataBuffer().getDataType();
        //
        // sanity check
        if (transferType != dataType)
            throw new RasterExposerException("internal inconsistency: transfer type "+transferType+" is not the same as data type"+dataType);

        String type = null;
        switch (dataType) {
            case DataBuffer.TYPE_DOUBLE:
                type = "float64";
                break;
            // java.awt.image.DataBuffer does not support TYPE_LONG
            //case DataBuffer.TYPE_LONG:
            //  break;
            case DataBuffer.TYPE_FLOAT:
                type = "float32";
                break;
            case DataBuffer.TYPE_INT:
                type = "int32";
                break;
            case DataBuffer.TYPE_SHORT:
                type = "int16";
                break;
            case DataBuffer.TYPE_USHORT:
                // java has no ushort
                type = "uint16";
                break;
            case DataBuffer.TYPE_BYTE:
                // java byte is an 8-bit signed two's complement integer
                // however, byte is usually interpreted as uint8 for image
                type = "uint8";
                break;
            case DataBuffer.TYPE_UNDEFINED:
                throw new RasterExposerException("TYPE_UNDEFINED is not implemented");
            default:
                throw new RasterExposerException("unknown data type: "+dataType);
        }
        return type;
    }

    // for given Raster, return [type, shape, array]
    // in which,
    // type is w10n data type, a string
    // shape is w10n data shape, an int[]
    // array is one of
    //      double[] for type == float64
    //      float[] for type == float32
    //      int[] for type == int32, int16, uint16, uint8
    //
    // must be in sync with get_type() above
    private Object[] get_type_shape_array(Raster raster) throws RasterExposerException {

        // type
        // there are various ways to obtain data type.
        // however, the api doc is no clear on what the differences are.
        // here they are assumed to be the same, but do check on the assumption.
        //
        // from raster
        int transferType = raster.getTransferType();
        this.logger.debug("raster trasferType: "+transferType);
        // from sample model
        int dataType = raster.getSampleModel().getDataType();
        this.logger.debug("raster sample model dataType: "+dataType);
        // from data buffer
        //int dataType = raster.getDataBuffer().getDataType();
        //
        // sanity check
        if (transferType != dataType)
            throw new RasterExposerException("internal inconsistency: raster transfer type "+transferType+" is not the same as raster sample model data type"+dataType);

        // shape
        int width = raster.getWidth();
        int height = raster.getHeight();
        int bands = raster.getSampleModel().getNumBands();
        int[] shape = new int[3];
        shape[0] = height; // == lines
        shape[1] = width; // == samples
        shape[2] = bands;

        // array
        Object array = null;
        String type = null;
        // these are for picking the right method
        double[] nullDoubleArray = null;
        float[] nullFloatArray = null;
        int[] nullIntArray = null;
        switch (dataType) {
            case DataBuffer.TYPE_DOUBLE:
                array = raster.getPixels(0, 0, width, height, nullDoubleArray);
                type = "float64";
                break;
            // java.awt.image.DataBuffer does not support TYPE_LONG
            //case DataBuffer.TYPE_LONG:
            //  break;
            case DataBuffer.TYPE_FLOAT:
                array = raster.getPixels(0, 0, width, height, nullFloatArray);
                type = "float32";
                break;
            case DataBuffer.TYPE_INT:
                array = raster.getPixels(0, 0, width, height, nullIntArray);
                type = "int32";
                break;
            case DataBuffer.TYPE_SHORT:
                array = raster.getPixels(0, 0, width, height, nullIntArray);
                type = "int16";
                break;
            case DataBuffer.TYPE_USHORT:
                // java has no ushort
                array = raster.getPixels(0, 0, width, height, nullIntArray);
                type = "uint16";
                break;
            case DataBuffer.TYPE_BYTE:
                array = raster.getPixels(0, 0, width, height, nullIntArray);
                // java byte is an 8-bit signed two's complement integer
                // however, byte is usually interpreted as uint8 for image
                type = "uint8";
                break;
            case DataBuffer.TYPE_UNDEFINED:
                throw new RasterExposerException("TYPE_UNDEFINED is not implemented");
            default:
                throw new RasterExposerException("unknown data type: "+dataType);
        }
        this.logger.debug("type: "+type);
        this.logger.debug("shape: "+Arrays.toString(shape));
        return new Object[] {type, shape, array};
    }

    // meta of leaf called "data"
    private Map get_leaf_data_meta(String[] names) throws RasterExposerException {
        String entityName = names[0];

        ImageReadParam param = null;
        RenderedImage renderedImage = null;
        SampleModel sampleModel = null;
        int width = -1;
        int height = -1;
        int numberOfBands = -1;
        String type = null;
        try {
            renderedImage = this.imageReader.readAsRenderedImage(this.imageIndex, param);
            sampleModel = renderedImage.getSampleModel();;
            //
            //width = this.imageReader.getWidth(imageIndex);
            width = renderedImage.getWidth();
            // as observed, this one does not give correct width
            //width = sampleModel.getWidth();
            //
            //height = this.imageReader.getHeight(imageIndex);
            height = renderedImage.getHeight();
            // as observed, this one does not give correct height
            //height = sampleModel.getHeight();
            //
            // this is the only way to obtain number of bands
            numberOfBands = sampleModel.getNumBands();

            type = this.get_type(sampleModel);
        } catch (IOException ioe) {
            throw new RasterExposerException(ioe);
        }

        Leaf leaf = new Leaf(entityName);
        // no attibutes for now
        Map map = leaf.to_map();

        // type
        map.put(jpl.mipl.wiio.Constant.TYPE, type);

        // shape = [h, w, b]
        // row-major order http://en.wikipedia.org/wiki/Row-major_order,
        // be consistent with what is returned in Raster.getPixels()
        ArrayList shape = new ArrayList(3);
        shape.add(new Integer(height));
        shape.add(new Integer(width));
        shape.add(new Integer(numberOfBands));
        //
        map.put(jpl.mipl.wiio.Constant.SHAPE, shape);

        return map;
    }

    // data of leaf called "data"
    private Map get_leaf_data_data(String[] names, String indexer) throws RasterExposerException {
        if (indexer == null)
            throw new RasterExposerException("Internal inconsistency: indexer is null");

        Rectangle sourceRegion = null;
        int[] subsamplings = null;
        int[] bandList = null;
        // if subset
        if (!indexer.equals("")) {
            int x = -1, y = -1, w = -1, h = -1;

            int[] tmp = this.parse_indexer(indexer);
            //
            int lineStart   = tmp[0];
            int lineEnd     = tmp[1];
            int lineStep    = tmp[2];
            //
            int sampleStart = tmp[3];
            int sampleEnd   = tmp[4];
            int sampleStep  = tmp[5];
            //
            int bandStart   = tmp[6];
            int bandEnd     = tmp[7];
            int bandStep    = tmp[8];

            x = sampleStart;
            y = lineStart;
            w = sampleEnd - sampleStart;
            h = lineEnd - lineStart;
            sourceRegion = new Rectangle(x, y, w, h);
            this.logger.debug("source region: "+sourceRegion);
            //
            subsamplings = new int[] {sampleStep, lineStep};
            this.logger.debug("x subsampling: "+subsamplings[0]);
            this.logger.debug("y subsampling: "+subsamplings[1]);

            bandList = new int[bandEnd - bandStart];
            int count = 0;
            for (int i=bandStart; i<bandEnd; i++) {
                if ((i-bandStart)%bandStep != 0)
                    continue;
                bandList[count] = i;
                count++;
            }
            bandList = Arrays.copyOfRange(bandList, 0, count);
            this.logger.debug("band list: "+Arrays.toString(bandList));
        }

        Raster raster = this.get_raster(this.imageReader, this.imageIndex, this.getRasterViaBufferedImage, sourceRegion, subsamplings);
        if (bandList != null)
            raster = raster.createChild(0, 0, raster.getWidth(), raster.getHeight(), 0, 0, bandList);

        Object[] objs = this.get_type_shape_array(raster);

        Map map = new HashMap();
        map.put(jpl.mipl.wiio.Constant.TYPE, objs[0]);
        map.put(jpl.mipl.wiio.Constant.SHAPE, objs[1]);
        map.put(the.treevotee.Constant.DATA, objs[2]);

        return map;
    }

    // meta of node called "buffer"
    private Map get_node_buffer_meta(String[] names) throws RasterExposerException {
        String entityName = names[0];
        Node node = new Node(entityName);
        node.attrs.add(new Attribute(jpl.mipl.wiio.Constant.NOTE, "not implemented") );
        return node.to_map();
    }

    private Map get_node_top_meta() throws RasterExposerException {
        Node node = new Node("");
        // leaves
        node.leaves.add(new Leaf("data"));
        // subnodes
        node.nodes.add(new Node("buffer"));

        return node.to_map();
    }

    public Map get(String[] names, boolean traverse, String indexer) throws RasterExposerException {

        if (traverse)
            throw new RasterExposerException("Traverse is not supported for raster store");

        String entityName = names[0];
        // sanity check
        if (!entityName.equals(""))
            throw new RasterExposerException("Internal inconsistency");

        if (names.length == 1) {
            if (indexer == null)
                return this.get_node_top_meta();
            throw new RasterExposerException("Data unsupported for node "+entityName);
        }

        String[] foos = this.slice_array(names, 1, names.length-1);
        if (foos[0].equals("data")) {
            if (indexer == null)
                return this.get_leaf_data_meta(foos);
            return this.get_leaf_data_data(foos, indexer);
        }

        if (foos[0].equals("buffer")) {
            if (indexer == null)
                return this.get_node_buffer_meta(foos);
            throw new RasterExposerException("Data unsupported for node "+foos[0]);
        }

        throw new RasterExposerException("Unknown w10n entity: "+foos[0]);
    }

    private Raster get_raster(ImageReader imageReader, int imageIndex,  boolean getRasterViaBufferedImage, Rectangle sourceRegion, int[] subsamplings) throws RasterExposerException {
        Raster raster = null;
      try {
        // http://stackoverflow.com/questions/18466513/read-region-from-very-large-image-file-in-java
        ImageReadParam imageReadParam = imageReader.getDefaultReadParam();
        if (sourceRegion != null)
            imageReadParam.setSourceRegion(sourceRegion);
        if (subsamplings != null)
            imageReadParam.setSourceSubsampling(subsamplings[0], subsamplings[1], 0, 0);
        /*
        if (bandList != null) {
            imageReadParam.setSourceBands(bandList);
            imageReadParam.setDestinationBands(bandList);
            //imageReadParam.setDestinationBands(new int[]{0,1});
        }
        */

        if (getRasterViaBufferedImage) {
            //BufferedImage bufferedImage = imageReader.read(imageIndex);
            raster = imageReader.read(imageIndex, imageReadParam).getData();
            this.logger.debug("get raster via buffered image");
            return raster;
        }

        if (!imageReader.canReadRaster())
            throw new RasterExposerException("image reader "+imageReader.getClass().getName()+" can not read raster directly, please get raster via buffered image");
        raster = imageReader.readRaster(imageIndex, imageReadParam);
        this.logger.debug("get raster via image reader "+imageReader.getClass().getName());
      } catch (IOException ioe) {
        throw new RasterExposerException(ioe);
      }
        return raster;
    }

    public static void main(String[] args) throws RasterExposerException {
        if (args.length != 8) {
            System.err.println("Usage: RasterExposer inputPath inputFormat imageReaderIndex imageIndex getRasterViaBufferedImage(yes|no) entityName traverse(yes|no) indexer(null|...)");
            System.exit(-1);
        }

        String inputPath = args[0];
        String inputFormat = args[1];
        int readerIndex = Integer.parseInt(args[2]);
        int imageIndex = Integer.parseInt(args[3]);
        //
        boolean getRasterViaBufferedImage = false;
        if (args[4].equals("yes"))
            getRasterViaBufferedImage = true;
        //
        String entityName = args[5];
        //
        boolean traverse = false;
        if (args[6].equals("yes"))
            traverse = true;
        //
        String indexer = null;
        if (!args[7].equals("null"))
            indexer = args[7];

        Iterator readers = null;
        ImageReader imageReader = null;
        ImageInputStream iis = null;
        ImageReadParam imageReadParam = null;

        Map map = null;

        int count = 0;
        try {
            iis = ImageIO.createImageInputStream(new File(inputPath));
            if (iis == null)
                throw new RasterExposerException("file non-existent: "+inputPath);

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
                throw new RasterExposerException("no image reader with index "+readerIndex);
            } else {
                System.err.println("use image reader "+readerIndex+" "+imageReader.getClass().getName());
            }

            imageReader.setInput(iis);

            RasterExposer rasterExposer = new RasterExposer(imageReader, imageIndex, getRasterViaBufferedImage);
            String[] names = entityName.split("/");
            map = rasterExposer.get(names, traverse, indexer);
        } catch (IOException ioe) {
            throw new RasterExposerException(ioe);
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
            throw new RasterExposerException(we);
        }
    }
}
