package jpl.mipl.juneberry.store.read;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import jpl.mipl.io.plugins.ISISImageReaderSpi;
// 20110918, xing, not to have vicario handle fits for now
//import jpl.mipl.io.plugins.FITSImageReaderSpi;
import jpl.mipl.io.plugins.PDSImageReadParam;
import jpl.mipl.io.plugins.PDSImageReaderSpi;
import jpl.mipl.io.plugins.VicarImageReaderSpi;
import jpl.mipl.juneberry.SimpleConvertBandSelect;
import jpl.mipl.juneberry.util.Util;
import jpl.mipl.wiio.proc.ProcessorClassFactory;
import jpl.mipl.wiio.store.read.ImageIOReader;

/*
 * Copyright (c) 2011 - 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */


//accurev is so fun!

/**
 * @author Xing
 * @author nttoole 2016.02.02
 */
public class VicarIO extends ImageIOReader {

    public static final String  PREFERRED_FORMAT           = "PDS_LABEL";
    public static final boolean EXTRA_METADATA_IS_ERROR    = false;
    public static final boolean MULTIPLE_METADATA_IS_ERROR = false;
    
    static {
        IIORegistry.getDefaultInstance().registerServiceProvider(new PDSImageReaderSpi());
        IIORegistry.getDefaultInstance().registerServiceProvider(new VicarImageReaderSpi());
        IIORegistry.getDefaultInstance().registerServiceProvider(new ISISImageReaderSpi());
        // 20110918, xing, not to have vicario handle fits for now
        //IIORegistry.getDefaultInstance().registerServiceProvider(new FITSImageReaderSpi());
    }

    //private final SimpleLogger logger = SimpleLogger.getLogger(VicarIO.class);

    public VicarIO(String sid, String path, java.util.Map config) throws VicarIOException {
        super(sid, path, config);

        ImageInputStream iis = null;
        try {
            File file = new File(this.path);
            if (!file.exists())
                throw new VicarIOException("file does not exist: "+this.path);
            iis = ImageIO.createImageInputStream(new File(this.path));
            if (iis == null)
                throw new VicarIOException("failed to create image input stream for "+this.path);
            Iterator iter = ImageIO.getImageReaders(iis);
            while (iter.hasNext()) {
                this.reader = (ImageReader)iter.next();
                if (this.reader.getClass().getName().startsWith("jpl.mipl.io.plugins.")) {
                    break;
                } else {
                    this.reader = null;
                }
            }
            if (this.reader == null)
                throw new IOException("no image reader plugin jpl.mipl.io.plugins.* found for "+this.path);
            this.reader.setInput(iis);
        } catch (java.util.NoSuchElementException nsee) {
            throw new VicarIOException(nsee+": there seems no imageio plugin available for "+this.path);

        } catch (IOException ioe) {
            throw new VicarIOException(ioe);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VicarIOException(e);
        }

        this.set_processor_class_factory(new ProcessorClassFactory());
    }

    // this is unnecessary
    public void dispose() {
        super.dispose();
    }

    // Is IIOImage lazy loading?
    private IIOImage get_iioimage(int i) throws IOException {
        
        //if (reader != null)
        //{
        //System.err.println("DEBUG::VicarIO::get_iioimage("+i+"): Reader class is "+reader.getClass().getName());
        //}  

        ImageReadParam param = this.reader.getDefaultReadParam();
        
        //if (param != null)
        //{
        //System.err.println("DEBUG::VicarIO::get_iioimage("+i+"): Param class is "+param.getClass().getName());
        //}     

        if (!(param instanceof PDSImageReadParam))
            return this.reader.readAll(i, param);

        PDSImageReadParam pdsImageReadParam = (PDSImageReadParam)param;
        int idx = this.path.lastIndexOf(File.separator);
        pdsImageReadParam.setDirectoryPath(this.path.substring(0, idx));
        return this.reader.readAll(i, pdsImageReadParam);
    }

    private RenderedImage get_rendered_image(int i) throws IOException {
        ImageReadParam param = this.reader.getDefaultReadParam();
        if (!(param instanceof PDSImageReadParam))
            return this.reader.readAsRenderedImage(i, param);

        PDSImageReadParam pdsImageReadParam = (PDSImageReadParam)param;
        int idx = this.path.lastIndexOf(File.separator);
        pdsImageReadParam.setDirectoryPath(this.path.substring(0, idx));
        return this.reader.readAsRenderedImage(i, pdsImageReadParam);
    }

    // override get_size() in parent class
    // this is not very efficient!
    // vicario does not seem to implement ImageReader.getRawImageType(i),
    // so get_size() has to be overridden as follows.
    protected int[] get_size(int i) throws VicarIOException {
//        IIOImage iioImage;
//      try {
//        iioImage = this.get_iioimage(i);
//      } catch (IOException ioe) {
//        throw new VicarIOException(ioe);
//      }
//        RenderedImage ri = iioImage.getRenderedImage();
//        return new int[] {ri.getWidth(), ri.getHeight(), ri.getSampleModel().getNumBands()};

      try {
        RenderedImage ri = this.get_rendered_image(i);
        return new int[] {ri.getWidth(), ri.getHeight(), ri.getSampleModel().getNumBands()};
      } catch (IOException ioe) {
        throw new VicarIOException(ioe);
      }

        /* It can also be done as follows. Not efficient either!
        ImageReadParam param = this.reader.getDefaultReadParam();
        if (!(param instanceof PDSImageReadParam)) try {
            return new int[] {this.reader.getWidth(i), this.reader.getHeight(i)};
        } catch (IOException ioe) {
            throw new VicarIOException(ioe);
        }

        PDSImageReadParam pdsImageReadParam = (PDSImageReadParam)param;
        int idx = this.path.lastIndexOf(File.separator);
        pdsImageReadParam.setDirectoryPath(this.path.substring(0, idx));
        try {
            RenderedImage ri = this.reader.readAsRenderedImage(i, pdsImageReadParam);
            return new int[] {ri.getWidth(), ri.getHeight()};
        } catch (IOException ioe) {
            throw new VicarIOException(ioe);
        }

        //return new int[] {-1, -1};
        */
    }

    // override get_metadata() in parent class
    protected Object get_metadata(int i) throws VicarIOException {
        if (this.reader instanceof jpl.mipl.io.plugins.ISISImageReader) {
            Map map = new HashMap();
            map.put("warning", "not available due to lack of implementation");
            return map;
        }

        IIOMetadata meta;
      try {
        meta = this.get_iioimage(i).getMetadata();
      } catch (IOException ioe) {
        throw new VicarIOException(ioe);
      }
      
        //if (meta != null)
        //{
        //System.err.println("DEBUG::VicarIO::get_metadata("+i+"): Metatype class is "+meta.getClass().getName());
        //}
      
        // there is one and only one standard or native formats for vicarIO
        String[] names = meta.getMetadataFormatNames();
        if (names == null || names.length == 0)
            throw new VicarIOException("Internal inconsistency: vicario does not produces any native meta.");

        if (MULTIPLE_METADATA_IS_ERROR && names.length > 1)
        {
            String allNames = "";
            for (String curName : names)
                allNames = allNames + " " + curName;                    
            throw new VicarIOException("Internal inconsistency: vicario produces meta in more than one format, not supposed to happen: "+allNames);
        }
        
        final String formatName = names.length == 1 ? names[0] : 
                                  select_single_format(names);
        
        org.w3c.dom.Node tree = meta.getAsTree(formatName);
        if (tree == null)
            throw new VicarIOException("Internal inconsistency: vicario produces null tree for meta, not supposed to happen.");

        Object obj;
        try {
            obj = Util.node_to_object(tree);
        } catch (IOException ioe) {
            throw new VicarIOException(ioe);
        }
        
        if (EXTRA_METADATA_IS_ERROR)
        {
            // extra formats, which should be null for vicarIO
            names = meta.getExtraMetadataFormatNames();
            if (names != null) 
            {            
                //String allNames = "";
                //for (String curName : names)
                //    allNames = allNames + " " + curName;
                //
                //throw new VicarIOException ("Internal inconsistency: vicario produces extra meta, not supposed to happen: "+allNames);            
                throw new VicarIOException ("Internal inconsistency: vicario produces extra meta, not supposed to happen.");
            }
        }

        return obj;
    }

    // override get_buffered_image() in parent class
    protected BufferedImage get_buffered_image(int i) throws VicarIOException {

      if (this.reader instanceof jpl.mipl.io.plugins.ISISImageReader) {
       try {
        ImageReadParam param = reader.getDefaultReadParam();
        RenderedImage renderedImage = reader.readAsRenderedImage(0, param);

        //SampleModel sm = renderedImage.getSampleModel();
        //RenderedImage bandSelectImg = null;

        ParameterBlockJAI bandSelectPB = new ParameterBlockJAI("BandSelect");
        bandSelectPB.addSource(renderedImage);
        //int[] bandList = {0};
        //int[] bandList = {0, 1, 2};
        int[] bandList = {27, 19, 22};
        bandSelectPB.setParameter("bandIndices", bandList);
        RenderedImage bandSelectImg = JAI.create("BandSelect", bandSelectPB);

        SimpleConvertBandSelect scbs = new SimpleConvertBandSelect();
        int outputDataType = DataBuffer.TYPE_BYTE;
        boolean rescaleOnFormat = true;
        RenderedImage ri = scbs.processFormat(bandSelectImg, outputDataType, rescaleOnFormat);
        return ((RenderedOp)ri).getAsBufferedImage();
       } catch (IOException ioe) {
        throw new VicarIOException(ioe);
       }
      }

        IIOImage iioImage;
      try {
        iioImage = this.get_iioimage(i);
      } catch (IOException ioe) {
        throw new VicarIOException(ioe);
      }
        return (BufferedImage)iioImage.getRenderedImage();
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.err.println("Usage: VicarIO sid path identifier");
            System.exit(1);
        }

        String sid = args[0];
        String path = args[1];
        String identifier = args[2];

        Map config = null;
        VicarIO reader = new VicarIO(sid, path, config);
        Map obj = reader.get(identifier);
        System.out.print(org.json.simple.JSONValue.toJSONString(obj));
    }
    
    protected String select_single_format(String[] formats)
    {        
        if (formats == null || formats.length == 0)
            return null;
        
        String selected = null;
        
        //check for preferred format first
        final String preferred = get_preferred_format();       
        if (preferred != null)
        {
            for (String format : formats)
                if (preferred.equals(format))
                {                 
                    selected = preferred;
                }
        }
        
        //choose quasi-arbitrarily (first non-null entry)
        if (selected == null)
        {
            int selIndex = 0;
            selected = formats[selIndex];
            
            //ensure selection is non-null
            while (selected == null && selIndex < formats.length)
            {
                ++selIndex;
                selected = formats[selIndex];
            }
        }
        
        return selected;
    }
      
    protected String get_preferred_format()
    {
        return VicarIO.PREFERRED_FORMAT;
    }
}
