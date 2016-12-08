package jpl.mipl.juneberry;

/*
 * Copyright (c) 2011 - 2016, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import jpl.mipl.juneberry.store.read.ReaderClassFactory;
import jpl.mipl.juneberry.output.WriterClassFactory;

import jpl.mipl.juneberry.cache.LocalEhcache;
import jpl.mipl.juneberry.cache.RemoteEhcache;

import jpl.mipl.juneberry.util.SoftLinkManager;
import jpl.mipl.juneberry.util.SoftLinkManagerException;

import the.treevotee.W10n;

import the.treevotee.SimpleLogger;

import the.treevotee.output.Output;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.io.File;

/**
 * @author Xing
 */
public class WebifyServlet extends the.treevotee.WebifyServlet {

    private final SimpleLogger logger = SimpleLogger.getLogger(WebifyServlet.class);

    private String contextPath = null;

    private String webappsDirPath = null;

    // default: w10n of fs dir IS by cgi servlet
    //private boolean w10nOfFsDirNotByCGIServlet = false;
    // as of 20150822, cgi is no longer needed. so this is always true.
    private boolean w10nOfFsDirNotByCGIServlet = true;

    private String pathPrefix = null;

    private String fsDirStoreRoot = null;

    private String[] entryList = null;

    public void init(javax.servlet.ServletConfig config) throws ServletException {
        super.init(config);

        Runtime rt = Runtime.getRuntime();
        this.logger.info("init");
        this.logger.info("Runtime: "+rt.maxMemory()+" "+rt.freeMemory()+" "+rt.totalMemory());

        // check where important classes are loaded from
        // ref: http://stackoverflow.com/questions/1983839/determine-which-jar-file-a-class-is-from
        this.logger.debug("Check where important classes are loaded from");
        Class klass = WebifyServlet.class;
        this.logger.debug(""+klass.getResource("/javax/imageio/ImageIO.class"));
        this.logger.debug(""+klass.getResource("/javax/media/jai/JAI.class"));

        W10n w10n = new W10n();
        //w10n.application += "/"+Constant.APP_NAME+"-"+Constant.APP_VERSION;
        w10n.application = Constant.APP_NAME+"-"+Constant.APP_VERSION;

        this.storeReaderClassFactory = new ReaderClassFactory();
        this.outputWriterClassFactory = new WriterClassFactory();
        this.w10n = w10n;

        // readCache
        //this.readCache = new LocalEhcache(Constant.READ_CACHE);
        //this.readCache.init();

        // outputCache
        //this.outputCache = new LocalEhcache(Constant.OUTPUT_CACHE);
        //this.outputCache = new RemoteEhcache("http://localhost:8080/ehcache-server-1.0.0/rest/"+Constant.OUTPUT_CACHE);
        //this.outputCache.init();

        // a hack to figure out contextPath if servlet api is old (2.4 and -)
        // ServletContext.getContextPath() was added in servlet api 2.5
        int majorVersion = getServletContext().getMajorVersion();
        int minorVersion = getServletContext().getMinorVersion();
        this.logger.info("servlet api is "+majorVersion+"."+minorVersion);
        if (majorVersion < 2 || (majorVersion == 2 && minorVersion < 5)) {
            this.logger.warn("a hack is used to figure out contextPath");
            String x = getServletContext().getRealPath("");
            int idx = x.lastIndexOf("/");
            if (idx == -1)
                throw new ServletException("Internal inconsistency: strange context real path"+x);
            this.contextPath = x.substring(idx);
        }

        // figure out various paths

        if (this.contextPath == null)
            this.contextPath = getServletContext().getContextPath();
        this.logger.debug("contextPath: "+this.contextPath);

        String contextRealPath = getServletContext().getRealPath("");
        this.logger.debug("contextRealPath: "+contextRealPath);

        //int idx = contextRealPath.lastIndexOf(this.contextPath);
        //if (idx == -1)
        //    throw new ServletException("Internal inconsistency: context path, "+this.contextPath+", is not a part of context real path, "+contextRealPath);
        //this.webappsDirPath = contextRealPath.substring(0, idx);
        //this.logger.debug("webappsDirPath: "+this.webappsDirPath);

        if (get_w10n_of_fs_dir_not_by_cgi_servlet())
            this.w10nOfFsDirNotByCGIServlet = true;

        this.pathPrefix = get_path_prefix();
        this.logger.debug("pathPrefix: "+this.pathPrefix);

        this.fsDirStoreRoot = get_fs_dir_store_root();
        this.logger.debug("fsDirStoreRoot: "+this.fsDirStoreRoot);

        this.entryList = get_entry_list();
        for (int i=0; i<this.entryList.length; i++) {
            this.logger.debug("entry "+i+": "+this.entryList[i]);
        }

        if (!this.w10nOfFsDirNotByCGIServlet)
            create_soft_links();
    } 

    public void destroy() {
        //this.readCache.end();

        //this.outputCache.end();

        if (!this.w10nOfFsDirNotByCGIServlet)
            remove_soft_links();

        this.logger.info("destroy");

        super.destroy();
    }

    // init-param in web.xml takes precedence over system property
    // and default is false
    private boolean get_w10n_of_fs_dir_not_by_cgi_servlet() {
        //String x = config.getInitParameter(Constant.INIT_PARAM_W10N_OF_FS_DIR_BY_CGI_SERVLET);
        String x = getServletConfig().getInitParameter(Constant.INIT_PARAM_W10N_OF_FS_DIR_NOT_BY_CGI_SERVLET);
        if (x == null)
            x = System.getProperty(Constant.SYSTEM_PROPERTY_W10N_OF_FS_DIR_NOT_BY_CGI_SERVLET);
        if (x != null && x.trim().equalsIgnoreCase("true"))
            return true;
        return false;
    }

    // init-param in web.xml takes precedence over system property
    // and default is ""
    private String get_path_prefix() {
        //String x = config.getInitParameter(Constant.INIT_PATH_PREFIX);
        String x = getServletConfig().getInitParameter(Constant.INIT_PARAM_PATH_PREFIX);
        if (x == null)
            x = System.getProperty(Constant.SYSTEM_PROPERTY_PATH_PREFIX);
        if (x == null)
            x = "";
        return x.trim();
    }

    // init-param in web.xml takes precedence over system property
    // and default is Constant.FS_DIR_STORE_ROOT=/var/www/html
    private String get_fs_dir_store_root() {
        //String x = config.getInitParameter(Constant.INIT_PARAM_FS_DIR_STORE_ROOT);
        String x = getServletConfig().getInitParameter(Constant.INIT_PARAM_FS_DIR_STORE_ROOT);
        if (x == null || x.trim().equals(""))
            x = System.getProperty(Constant.SYSTEM_PROPERTY_FS_DIR_STORE_ROOT);
        if (x == null || x.trim().equals(""))
            x = Constant.FS_DIR_STORE_ROOT;
        return x.trim();
    }

    // init-param in web.xml takes precedence over system property
    // and default is new String[]{}
    private String[] get_entry_list() {
        //String x = config.getInitParameter(Constant.INIT_PARAM_ENTRY_LIST);
        String x = getServletConfig().getInitParameter(Constant.INIT_PARAM_ENTRY_LIST);
        if (x == null || x.trim().equals(""))
            x = System.getProperty(Constant.SYSTEM_PROPERTY_ENTRY_LIST);
        if (x == null || x.trim().equals(""))
            return new String[]{};
        // comma separated
        return x.trim().split("\\s*,\\s*");
    }

    private void create_soft_links() {
        // create soft links
        String dir = (new File(this.webappsDirPath, this.contextPath)).getPath();
        SoftLinkManager softLinkManager = new SoftLinkManager(dir);
        String target = null;
        for (int i=0; i<this.entryList.length; i++) {
          try {
            target = (new File(this.fsDirStoreRoot, this.entryList[i])).getPath();
            softLinkManager.create(target);
            this.logger.info("under "+dir+", create soft link to "+target);
          } catch (SoftLinkManagerException slme) {
            this.logger.error("under "+dir+", error when creating soft link to "+target);
          }
        }
    }

    private void remove_soft_links() {
        // remove soft links
        String dir = (new File(this.webappsDirPath, this.contextPath)).getPath();
        SoftLinkManager softLinkManager = new SoftLinkManager(dir);
        String link = null;
        for (int i=0; i<this.entryList.length; i++) {
          try {
            link = this.entryList[i];
            softLinkManager.remove(link);
            this.logger.info("under "+dir+", remove soft link "+link);
          } catch (SoftLinkManagerException slme) {
            this.logger.error("under "+dir+", error when removing soft link "+link);
          }
        }
    }

    // override the.treevotee.WebifyServlet's
    protected Output getOutput(HttpServletRequest request) throws ServletException {
        // figure out w10n type, path and identifier from http request.
        String[] tpi = this.getTypePathIdentifier(request);
        String type = tpi[0];
        String path = tpi[1];
        String id7rString = tpi[2];

        // figure out pathPrefix and dirPath
        String pathPrefix = this.contextPath;
        String dirPath = this.webappsDirPath;
        if (this.w10nOfFsDirNotByCGIServlet) {
            pathPrefix = this.pathPrefix;
            dirPath = this.fsDirStoreRoot;
        }

        return this.getOutput(request, type, pathPrefix+path, id7rString, dirPath);
    }
}
