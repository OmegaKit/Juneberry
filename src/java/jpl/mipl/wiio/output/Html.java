package jpl.mipl.wiio.output;

/*
 * Copyright (c) 2011 - 2013, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import the.treevotee.Constant;
import the.treevotee.W10n;

import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.io.InputStream;
//import java.io.ByteArrayOutputStream;

/**
 * @author Xing
 */
public class Html extends Writer {

    //private String metaSymbol = "&#62;&#62;";
    private String metaSymbol = "*";

    private String mimeType = "text/html";

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public Html() {}

    private String get_js_text(String name) throws WriterException {
        InputStream is = null;
        byte[] buf = null;
      try {
        //is = (InputStream)Html.class.getResourceAsStream(name);
        is = this.getClass().getResourceAsStream(name);
        int length = is.available();
        buf = new byte[length];
        is.read(buf);
        //is.close();
      } catch (IOException ioe) {
        throw new WriterException(ioe);
      } finally {
        if (is != null) try {
            is.close();
        } catch (Exception ignored) {
            // ignored
        }
      }
        if (buf == null)
            return null;
        return new String(buf);
    }

    private String get_proc_leaf_name(Map leaf) throws WriterException {
        // figure out proc method
        // leaf name has a format like proc:stretcher:method
        String name = (String)leaf.get(Constant.NAME);
        String[] tmp = name.split(":");
        if (tmp.length != 3 || !tmp[0].equals("proc") || tmp[2].equals(""))
            throw new WriterException("Invalid w10n leaf name for proc: "+name);
        String method = tmp[2];

        // find attribute "input"
        ArrayList attrs = (ArrayList)leaf.get(Constant.ATTRS);
        Map attr;
        String key = null;
        Object value = null;
        for (int i=0; i<attrs.size(); i++) {
            attr = (Map)attrs.get(i);
            key = (String)attr.get(Constant.NAME);
            if (key != null && key.equals("input")) {
                value = attr.get(Constant.VALUE);
                break;
            }
        }

        if (value == null)
            throw new WriterException("Invalid w10n leaf: proc expected");

        if (!(value instanceof Map))
            throw new WriterException("Missing w10n leaf attribute: input expected");

        Map input = (Map)value;
        //input.put("method", method);

        // packed as a map and serilized into json inside an html textarea
        Map lhm = new LinkedHashMap(); // for ordered dictionary
        lhm.put("name", method);
        lhm.put("input", input);

        return org.json.simple.JSONValue.toJSONString(lhm);
    }

    public Output write(Map map) throws WriterException {
        //System.out.println(leaves.size());

        ArrayList al = (ArrayList)map.get(Constant.W10N);
        if (al == null)
            throw new WriterException("Internal inconsistency.");
        W10n w10n = new W10n();
        w10n.fromArrayList(al);

        String txt = "<html><head>\n";
        txt += "<title>"+w10n.path+w10n.identifier+"</title>\n";
        // 20140104, xing, disabled
        // may never be needed if we decide not to embed widgets here.
        //if (w10n.identifier.equals("/")) {
        //    txt += "<link href='/wiio/css/modular.css' type='text/css' rel='stylesheet' />\n";
        //    txt += "<script type='text/javascript' src='/wiio/js/jquery-1.6.1.js'></script>\n";
        //    txt += "<script type='text/javascript' src='/wiio/js/processing-1.3.0.js'></script>\n";
        //    txt += "<script type='text/javascript' src='/wiio/js/modular/histogram.js'></script>\n";
        //}
        //txt += "<script type='text/javascript' src='/js/base64.js?"+this.dateFormat.format(new Date())+"'></script>\n";
        //txt += "<script type='text/javascript' src='/js/wiio.js?"+this.dateFormat.format(new Date())+"'></script>\n";
        txt += "<script type='text/javascript'>\n";
        txt += this.get_js_text("Html.wiio.js");
        txt += "</script>\n";
        txt += "</head><body>\n";
        txt += "<h1>Index of "+w10n.path+w10n.identifier+"</h1>\n";
        txt += "<table border='0'>\n";
        txt += "<tr><th>Name</th><th>View-As</th><th>Size</th><th>Metadata</th></tr>\n";
        txt += "<tr><td colspan='4'><hr/></td></tr>\n";
        txt += "<tr><td><a href='..'>Parent Directory</a></td></tr>\n";
        // leaves
        ArrayList l = (ArrayList)map.get(Constant.LEAVES);
        if (l == null) {
            throw new WriterException("Apparently not meta of a w10n node.");
            //throw new WriterException(this.getClass().getName()+" can only output meta of a w10n node.");
        }
        //String prefix = w10n.path + w10n.identifier;
        String prefix = "";
        for (int i=0; i<l.size(); i++) {
            Map leaf = (Map)l.get(i);
            String name = (String)leaf.get(Constant.NAME);
            //String type = leaf.get(Constant.TYPE);
            String nameHTML = name;
            String gifLink = "<a style='text-decoration: none;' href='"+name+"[]?output=gif'>gif</a>";
            String meta = "<a style='text-decoration: none;' href='"+name+"/'>"+this.metaSymbol+"</a>";
            // 20120522, xing, temp disabled
            if (name.startsWith("regex:^w")) {
                nameHTML = "<input id='wiio_leaf_by_w' name='by_w' type='text' alt='by_w' value='w64'/>";
                gifLink = "<span style='color: blue;' onclick='wiio.get_leaf(\"wiio_leaf_by_w\",\"data\", \""+prefix+"\");'>gif</span>";
                meta = "<span style='color: blue;' onclick='wiio.get_leaf(\"wiio_leaf_by_w\",\"meta\", \""+prefix+"\");'>"+this.metaSymbol+"</span>";
            } else if (name.startsWith("regex:^h")) {
                nameHTML = "<input id='wiio_leaf_by_h' name='by_h' type='text' alt='by_h' value='h48'/>";
                gifLink = "<span style='color: blue;' onclick='wiio.get_leaf(\"wiio_leaf_by_h\",\"data\", \""+prefix+"\");'>gif</span>";
                meta = "<span style='color: blue;' onclick='wiio.get_leaf(\"wiio_leaf_by_h\",\"meta\", \""+prefix+"\");'>"+this.metaSymbol+"</span>";
            } else if (name.startsWith("regex:^") && name.indexOf('x') != -1) {
                nameHTML = "<input id='wiio_leaf_by_w_and_h' name='by_w_and_h' type='text' alt='by_w_and_h' value='64x48'/>";
                gifLink = "<span style='color: blue;' onclick='wiio.get_leaf(\"wiio_leaf_by_w_and_h\",\"data\", \""+prefix+"\");'>gif</span>";
                meta = "<span style='color: blue;' onclick='wiio.get_leaf(\"wiio_leaf_by_w_and_h\",\"meta\", \""+prefix+"\");'>"+this.metaSymbol+"</span>";
            }
            if (name.startsWith("proc:")) {
                continue;
            //    String elementId = "wiio_leaf_proc_" + i;
            //    nameHTML = "<textarea id='"+elementId+"' name='"+elementId+"' style='width:300px; height:150px;'>"+this.get_proc_leaf_name(leaf)+"</textarea>";
            //    gifLink = "<span style='color: blue;' onclick='wiio.get_leaf_proc(\""+elementId+"\",\"data\", \""+prefix+"\");'>gif</span>";
            //    meta = "<span style='color: blue;' onclick='wiio.get_leaf_proc(\""+elementId+"\",\"meta\", \""+prefix+"\");'>"+this.metaSymbol+"</span>";
            }
            if (name.equals("data")) {
                gifLink = "<a style='text-decoration: none;' href='"+name+"[]?output=json'>json</a> <a style='text-decoration: none;' href='"+name+"[]?output=big-endian'>big-endian</a> <a style='text-decoration: none;' href='"+name+"[]?output=little-endian'>little-endian</a>";
            }
            txt += "<tr><td>"+nameHTML+"</td><td align='center'>"+gifLink+"</td><td align='right'>-</td><td align='center'>"+meta+"</td>\n";
        }
        // nodes
        l = (ArrayList)map.get(Constant.NODES);
        for (int i=0; i<l.size(); i++) {
            Map node = (Map)l.get(i);
            String name = (String)node.get(Constant.NAME);
            String link = "<a style='text-decoration: none;' href='"+name+"/?output=html'>"+name+"</a>";
            String meta = "<a style='text-decoration: none;' href='"+name+"/'>"+this.metaSymbol+"</a>";
            txt += "<tr><td>"+link+"</td><td align='center'>-</td><td align='right'>-</td><td align='center'>"+meta+"</td></tr>\n";
            // 20120522, xing, temp disabled
            //if (w10n.identifier.equals("/")) {
            //    txt += "<tr><td colspan='4'>\n";
            //    txt += "<h3>original</h3>\n";
            //    txt += "<div><img src='"+name+"/resized/h256[]?output=gif'/></div>\n";
            //    txt += "<h3>targeted one</h3>\n";
            //    txt += "<div><img class='histogram_target' src='"+name+"/resized/h256[]?output=gif'/></div>\n";
            //    txt += "<h3>targeted two</h3>\n";
            //    txt += "<div><img class='histogram_target' src='"+name+"/resized/h256[]?output=gif'/></div>\n";
            //    txt += "</td></tr>\n";
            //    //txt += "<tr><td colspan='4'><img class='histogram_target' src='"+name+"/resized/h256[]?output=gif'></td></tr>\n";
            //}
        }
        txt += "<tr><td colspan='4'><hr/></td></th>\n";
        txt += "</table>\n";
        txt += "<em>application:"+w10n.application+", spec:"+w10n.spec+", type:"+w10n.type+"</em>\n";
        txt += "</body>\n";

        return new Output(txt.getBytes(),this.mimeType);
    }
}
