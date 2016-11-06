package jpl.mipl.juneberry.cache;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.util.Properties;

import the.treevotee.SimpleLogger;

/**
 * @author Xing
 */
public class LocalEhcache implements the.treevotee.cache.Cache {

    private final SimpleLogger logger = SimpleLogger.getLogger(LocalEhcache.class);

    private String cacheName = null;
    private Cache ehCache = null;

    private CacheManager cacheManager = null;

    public LocalEhcache(String cacheName) {
        this.cacheName = cacheName;
    }

    public void init() {
        String ehcacheXmlPath = System.getProperties().getProperty("juneberry.ehcache.xml.path");
        if (ehcacheXmlPath == null) {
            this.cacheManager = CacheManager.create();
            this.logger.info("init cache manager using default config");
            this.cacheManager.addCache(this.cacheName);
            this.logger.info("init cache "+this.cacheName+" using default config");
        } else {
            this.cacheManager = CacheManager.create(ehcacheXmlPath);
            this.logger.info("init cache manager using "+ehcacheXmlPath);
            this.logger.info("init cache "+this.cacheName+" using "+ehcacheXmlPath);
        }
        // this.ehCache can't be null by now.
        this.ehCache = this.cacheManager.getCache(this.cacheName);
    }

    public void end() {
        this.logger.info("end cache "+this.cacheName);
        if (this.cacheManager != null) {
            this.cacheManager.shutdown();
            this.logger.info("end cache manager");
        }
    }

    public Object get(String key) {
        Element element = this.ehCache.get(key);
        if (element == null)
            return null;
        return element.getValue();
    }

    public void put(String key, Object value) {
        //Element element = new Element(key, value);
        this.ehCache.put(new Element(key, value));
        return;
    }
}
