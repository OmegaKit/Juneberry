package jpl.mipl.juneberry.output;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import the.treevotee.Constant;
import the.treevotee.W10n;

import the.treevotee.output.Output;
import the.treevotee.output.Writer;
import the.treevotee.output.WriterException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

//import java.io.ByteArrayOutputStream;
//import java.io.IOException;

/**
 * @author Xing
 */
public class Html extends Writer {

    private String metaSymbol = "&#62;&#62;";

    private String mimeType = "text/html";

    public Html() {}

    public Output write(Map map) throws WriterException {

        ArrayList al = (ArrayList)map.get(Constant.W10N);
        if (al == null)
            throw new WriterException("Internal inconsistency.");
        W10n w10n = new W10n();
        w10n.fromArrayList(al);

        String txt = "<html><head>\n";
        txt += "<link rel='stylesheet' type='text/css' href='/juneberry/css/style.css'/>";
        txt += "<script type='text/javascript' src='/juneberry/js/load_script.js'></script>\n";
        txt += "<script type='text/javascript' src='/juneberry/js/json2.js'></script>\n";
        txt += "<script type='text/javascript' src='/juneberry/js/main.js'></script>\n";
        txt += "</head><body>\n";
        txt += "<h3><nobr><span id='j8aPageTitle'>Content of "+w10n.path+w10n.identifier+"</span></nobr></h3>\n";

        txt += "<div id='j8aLinks'><a href='..'>Parent URL</a></div>\n";

        txt += "<div class='w10n_attributes'>\n"; // w10n_attributes start
        txt += "<div class='title' onclick='juneberry.toggle(\"attributes\")' style='color:blue;'>Attributes</div>\n"; // w10n_attributes title start and end
        txt += "<div class='content' id='attributes' style='display:\"\";'>\n"; // w10n_attributes content start
        List attrList = (List)map.get(Constant.ATTRS);
        if (attrList == null)
            attrList = new ArrayList();
        Map attr;
        for (int i=0; i<attrList.size(); i++) {
            attr = (Map)attrList.get(i);
            String name = (String)attr.get(Constant.NAME);
            Object value = attr.get(Constant.VALUE);
            txt += "<div class='w10n_attribute'>\n"; // w10n_attribute start
            txt += "<span class='name'>"+name+":</span> <tt class='value'>"+value+"</tt>\n";
            txt += "</div>\n"; // w10n_attribute end
        }
        txt += "</div>\n"; // w10n_attributes content end
        //txt += ""+attrList.size();
        txt += "</div>\n"; // w10n_attributes end

        //txt += "<div >Leaves</h3>\n";

        txt += "<div class='w10n_leaves'>\n"; // w10n_leaves start

        //txt += "<table border='0'>\n";
        //txt += "<tr><th>Name</th><th>View-as-GIF</th><th>Size</th><th>Metadata</th></tr>\n";

        //txt += "<tr><td colspan='3'><hr/></td></tr>\n";

        List leafList = (List)map.get(Constant.LEAVES);
        if (leafList == null)
            leafList = new ArrayList();
        Map leaf;
        for (int i=0; i<leafList.size(); i++) {
            leaf = (Map)leafList.get(i);
            String name = (String)leaf.get(Constant.NAME);
            //String type = leaf.get(Constant.TYPE);
            String metaLinks = "[meta: ";
            metaLinks += "<a style='text-decoration: none;' href='"+name+"/'>json</a>";
            metaLinks += "]";
            String dataLinks = "[data: ";
            dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=gif'>gif</a>, ";
            dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=png'>png</a>, ";
            dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=jpeg'>jpeg</a>";
            //dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=json'>json</a>, ";
            //dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=pdf'>pdf</a>, ";
            //dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=png'>png</a>, ";
            //dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=jpg'>jpg</a>, ";
            //dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=csv'>csv</a>, ";
            //dataLinks += "<a style='text-decoration: none;' href='"+name+"[]?output=html'>html</a>";
            dataLinks += "]";
            //txt += "<tr><td>"+name+"</td><td>"+metaLinks+"</td><td>"+dataLinks+"</td>\n";
            txt += "<div class='w10n_leaf'>\n"; // w10n_leaf start
            txt += "<div class='title'>\n"; // title start
            txt += name+" "+metaLinks+" "+dataLinks;
            txt += "</div>\n"; // title end
            txt += "<div class='content'>\n"; // content start
            txt += "</div>\n"; // content end
            txt += "</div>\n"; // w10n_leaf end
        }

        //txt += "<tr><td colspan='3'><hr/></td></th>\n";

        //txt += "</table>\n";

        txt += "</div>\n"; // w10n_leaves end

        //txt += "<h3>Nodes</h3>\n";

        List nodeList = (List)map.get(Constant.NODES);
        if (nodeList == null)
            nodeList = new ArrayList();
        if (nodeList.size() == 0) {
            //txt += "<div>none</div>\n";
        } else {
            txt += "<div>node display not implemented</div>\n";
        }

        txt += "<div id='j8aFooter'><span>application:"+w10n.application+", spec:"+w10n.spec+", type:"+w10n.type+"</span></div>\n";
        txt += "</body>\n";

        return new Output(txt.getBytes(),this.mimeType);
    }
}
