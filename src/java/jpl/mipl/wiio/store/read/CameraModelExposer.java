package jpl.mipl.wiio.store.read;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
//import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;

import jpl.mipl.mars.pig.PigCameraModel;
import jpl.mipl.mars.pig.PigCameraModelFactory;
import the.treevotee.Attribute;
import the.treevotee.Leaf;
import the.treevotee.Node;

//import the.treevotee.Constant;
//import the.treevotee.store.read.Reader;
//import the.treevotee.store.read.ReaderException;

import the.treevotee.SimpleLogger;
import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

/**
 * @author Toole
 *
 * This class webifies camera model via imageio image reader 
 *
 */

public class CameraModelExposer {

    private ImageReader imageReader = null;
    private int imageIndex = -1;
    
    private PigCameraModel cameraModel = null;
    private boolean cameraModelLoadAttempted = false;

    private final SimpleLogger logger = SimpleLogger.getLogger(CameraModelExposer.class);

    

    //---------------------------------------------------------------------
    // this one is needed to make sure subclass can call main()
    protected CameraModelExposer() {}


    //---------------------------------------------------------------------
    
    public CameraModelExposer(ImageReader imageReader, int imageIndex) 
    {
        this.imageReader = imageReader;
        this.imageIndex  = imageIndex;
    }


    //---------------------------------------------------------------------
    
//    private int[] parse_indexer(String indexer) throws CameraModelExposerException {
//        if (indexer.startsWith("("))
//            throw new CameraModelExposerException("Tile indexer is not supported");
//        return this.parse_range_indexer(indexer);
//    }

    // a util to slice an array
    

    //---------------------------------------------------------------------
    
    private String[] slice_array(String[] names, int offset, int length) {
        String[] foos = new String[length];
        System.arraycopy(names, offset, foos, 0, length);
        return foos;
    }

    //---------------------------------------------------------------------
    
    protected AttributeCollection getCameraModelParametersInfo(PigCameraModel cmod)
    {
        AttributeCollection info = new AttributeCollection(
                                   CmodConstants.KEY_CAMERA_SETTINGS);        
        CameraModelInfoExtractor extractor = new CameraModelInfoExtractor(cmod);
        Map<String,Object> cameraKvMap = extractor.getCameraParameters();              
        List<Attribute> attrs = convertKeyValMapToAttributes(cameraKvMap);
        info.addAttributes(attrs);        
        return info;
    }
    

    
    //---------------------------------------------------------------------
    
    protected AttributeCollection getCameraPointingParametersInfo(PigCameraModel cmod)
    {
        AttributeCollection info = new AttributeCollection(
                                   CmodConstants.KEY_POINTING_SETTINGS);        
        CameraModelInfoExtractor extractor = new CameraModelInfoExtractor(cmod);
        Map<String,Object> pointingKvMap = extractor.getPointingParameters();                
        List<Attribute> attrs = convertKeyValMapToAttributes(pointingKvMap);
        info.addAttributes(attrs);
        return info;
    }

    //---------------------------------------------------------------------
    
    protected AttributeCollection getRootParametersInfo(PigCameraModel cmod)
    {
        AttributeCollection info = new AttributeCollection(
                                   CmodConstants.KEY_CMOD);        
        CameraModelInfoExtractor extractor = new CameraModelInfoExtractor(cmod);
        Map<String,Object> rootKvMap = extractor.getRootParameters();                
        List<Attribute> attrs = convertKeyValMapToAttributes(rootKvMap);
        info.addAttributes(attrs);
        return info;
    }
    
    
    //---------------------------------------------------------------------
    
    protected PigCameraModel getCameraModel() throws CameraModelExposerException
    {
        //not thread safe, but do we need to be??
        if (cameraModel == null)
        {
            //if null and we have already attempted to load it, then quit now
            if (cameraModelLoadAttempted)
            {
                throw new CameraModelExposerException("No camera model found in image");
            }
            
            //this is our sole attempt, mark it as such
            this.cameraModelLoadAttempted = true;
            
            IIOMetadata meta = null;            
            try {
                meta = this.imageReader.getImageMetadata(this.imageIndex);
            } catch (IOException ioe) {
                throw new CameraModelExposerException(ioe);
            }
            
            PigCameraModel cmod = PigCameraModelFactory.createFromIioMetadata(meta);
            if (cmod == null)
            {
                throw new CameraModelExposerException("No camera model found in image");
            }
            
            this.cameraModel = cmod;
        }
        
        return cameraModel;
    }
    
    //---------------------------------------------------------------------
    
    
    
    //---------------------------------------------------------------------

    private Map get_node_top_meta(boolean traverse) throws CameraModelExposerException 
    {
        //retrieve the camera model
        PigCameraModel cmod = getCameraModel();
        
        //create root node 
        final Node topNode = new Node(CmodConstants.KEY_CMOD);
   
        //--------------
        
        //get the root level attributes
        AttributeCollection rootInfo   = getRootParametersInfo(cmod);
        topNode.attrs.addAll(rootInfo.getAttributes());
        
        //--------------
        
        //get the camera settings node
        AttributeCollection cameraInfo   = getCameraModelParametersInfo(cmod);
        Leaf cameraNode = new Leaf(cameraInfo.getName());
        cameraNode.attrs.addAll(cameraInfo.getAttributes());
        topNode.leaves.add(cameraNode);
            
        //get the pointing node
        AttributeCollection pointingInfo = getCameraPointingParametersInfo(cmod);
        Leaf pointingNode = new Leaf(pointingInfo.getName());
        pointingNode.attrs.addAll(pointingInfo.getAttributes());
        topNode.leaves.add(pointingNode);

        //--------------
        
        return topNode.to_map();
    }



    //---------------------------------------------------------------------
    
    /**
     * Returns attributes for one of the supported leaves: camera settings or
     * pointing settings.
     * @param names String array of the w10n path call
     * @return Map translation of the leaf node extracted by names param
     * @throws CameraModelExposerException
     */
    private Map get_node_level1_meta(String[] names) throws CameraModelExposerException 
    {
        final String leafName = names[1];

        //leaf with the same name as passed in
        final Leaf topLeaf = new Leaf(leafName);        

        final PigCameraModel cmod = getCameraModel();
        
        
        AttributeCollection info = null;        
        if (leafName.equalsIgnoreCase(CmodConstants.KEY_CAMERA_SETTINGS))
        {
            info = this.getCameraModelParametersInfo(cmod);
        }
        else if (leafName.equalsIgnoreCase(CmodConstants.KEY_POINTING_SETTINGS))
        {
            info = this.getCameraPointingParametersInfo(cmod);
        } 
        else
        {
            throw new CameraModelExposerException("Internal inconsistency");
        }
        
        if (info != null)
        {
            topLeaf.attrs.addAll(info.getAttributes());
        }
        
        
        return topLeaf.to_map();
    }

    //---------------------------------------------------------------------

    //We are a leaf so we have to return something, such as, the empty string
    private Map get_leaf_level1_data(String[] names, String indexer) throws CameraModelExposerException 
    {

        String value = "";
        Map map = new HashMap();
        map.put("data", value);


        return map;
    }


    //---------------------------------------------------------------------
    
    /**
     * Entry point into this extractor
     * @param names Request path string array
     * @param traverse Flag indicating if traverse setting is enabled
     * @param indexer Data indexer string
     * @return Map of either metatada or data information
     * @throws CameraModelExposerException
     */
    public Map get(String[] names, boolean traverse, String indexer) throws CameraModelExposerException {

        if (traverse)
            throw new CameraModelExposerException("Traverse is not supported for cmod store");

        String entityName = names[0];
        
        // sanity check
        if (!entityName.equals(""))
            throw new CameraModelExposerException("Internal inconsistency");

        if (names.length == 1) 
        {
            if (indexer == null)
                return this.get_node_top_meta(traverse);
            throw new CameraModelExposerException("Data unsupported for node "+entityName);
        }

        if (names.length == 2) {
            if (indexer == null)
                return this.get_node_level1_meta(names);
            entityName = names[1];
            return this.get_leaf_level1_data(names, indexer);
        }
        

        String[] foos = this.slice_array(names, 1, names.length-1);

        throw new CameraModelExposerException("Unknown w10n entity: "+foos[0]);
    }
    

    //---------------------------------------------------------------------


    //---------------------------------------------------------------------
    
    /**
     * Accepts a key-value map and returns a list of Attributes containing
     * same information.  Values of the input map should be either primitives,
     * primitive array or Strings.
     * @param keyValMap Input key val map
     * @return List of w10n Attributes
     */
    protected List<Attribute> convertKeyValMapToAttributes(Map keyValMap)
    {
        List<Attribute> attrList = new ArrayList<Attribute>();
        
        Iterator<String> camSetIt = keyValMap.keySet().iterator();
        
        while (camSetIt.hasNext())
        {
            String camSetKey = camSetIt.next();
            Object value     = keyValMap.get(camSetKey);
            String valueStr  = valueToString(value);
           
            Attribute attr = new Attribute(camSetKey, valueStr);
            attrList.add(attr);                       
        }
        
        return attrList;
    }

    //---------------------------------------------------------------------
    
    protected static String valueToString(Object value)
    {
        if (value == null)
            return null;
        
        if (value instanceof String)
            return ((String) value);
        
        String valueStr  = value.toString();
        if (value instanceof Object[])
        {
            Object[] valueArray = (Object[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        else if (value instanceof int[])
        {
            int[] valueArray = (int[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        else if (value instanceof double[])
        {
            double[] valueArray = (double[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        else if (value instanceof float[])
        {
            float[] valueArray = (float[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        else if (value instanceof short[])
        {
            short[] valueArray = (short[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        else if (value instanceof char[])
        {
            char[] valueArray = (char[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        else if (value instanceof long[])
        {
            long[] valueArray = (long[]) value;
            valueStr = Arrays.toString(valueArray);
        }
        return valueStr;
    }
    
    //---------------------------------------------------------------------

    public static void main(String[] args) throws CameraModelExposerException {
        if (args.length != 7) {
            System.err.println("Usage: CameraModelExposer inputPath inputFormat imageReaderIndex imageIndex entityName traverse(yes|no) indexer(null|...)");
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
                throw new CameraModelExposerException("file non-existent: "+inputPath);

            StringBuffer buffer = new StringBuffer();
            String[] readerNames = ImageIO.getReaderFormatNames();
            for (String readerName : readerNames)
            {
                buffer.append(readerName).append(" ");
            }
            System.out.println("Reader names: "+buffer.toString());
            
            
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
                throw new CameraModelExposerException("no image reader with index "+readerIndex);
            } else {
                System.err.println("use image reader "+readerIndex+" "+imageReader.getClass().getName());
            }

            imageReader.setInput(iis);

            CameraModelExposer cmodExposer = new CameraModelExposer(imageReader, imageIndex);
            String[] names = new String[] {entityName};
            if (entityName.equals("/"))
                names = new String[] {""};
            else if (entityName.contains("/"))
                names = entityName.split("/");
            
            map = cmodExposer.get(names, traverse, indexer);
        } catch (IOException ioe) {
            throw new CameraModelExposerException(ioe);
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
            throw new CameraModelExposerException(we);
        }
    }

    //---------------------------------------------------------------------

    class AttributeCollection
    {
        protected String name;
        protected List<Attribute> attributes;
        public AttributeCollection(String name) 
        { 
            this.name = name; 
            this.attributes = new ArrayList<Attribute>(); 
        }
        public String getName()
        {
            return this.name;
        }
        public List<Attribute> getAttributes()
        {
            return this.attributes;
        }
        public void addAttribute(Attribute attr)
        {
            this.attributes.add(attr);
        }
        public void addAttributes(Collection<Attribute> attrs)
        {
            this.attributes.addAll(attrs);
        }
    }
    
    //---------------------------------------------------------------------
}
