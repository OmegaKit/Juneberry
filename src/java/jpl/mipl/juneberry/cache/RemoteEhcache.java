package jpl.mipl.juneberry.cache;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
//import java.net.URLEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

import the.treevotee.SimpleLogger;

/**
 * @author Xing
 */
public class RemoteEhcache implements the.treevotee.cache.Cache {

    private final SimpleLogger logger = SimpleLogger.getLogger(RemoteEhcache.class);

    private String cacheURL = null;

    public RemoteEhcache(String cacheURL) {
        this.cacheURL = cacheURL;
    }

    public void init() {
        this.logger.info("init cache "+this.cacheURL);
    }

    public void end() {
        this.logger.info("end cache "+this.cacheURL);
    }

    // 20100803, xing, for generating id
    // this one should return d41d8cd98f00b204e9800998ecf8427e
    //System.err.println(this.getMD5Hash("".getBytes()));
    private String get_md5sum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        //m.reset();
        m.update(bytes);
        BigInteger bigInteger = new BigInteger(1, m.digest());
        String hash = bigInteger.toString(16);
        while (hash.length() < 32) {
            hash = "0" + hash;
        }
        return hash;
    }

    public Object get(String key) {
        Object object = null;

        ObjectInputStream ois = null;
        HttpURLConnection connection = null;
      try {
        //String encodedKey = URLEncoder.encode(key, "UTF-8");
        //URL url = new URL(cacheURL+"/"+encodedKey);
        //URL url = new URL(cacheURL+"/"+key);
        URL url = new URL(cacheURL+"/"+this.get_md5sum(key.getBytes()));
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        ois = new ObjectInputStream(connection.getInputStream());
        object = ois.readObject();
        int statusCode = connection.getResponseCode();
        if (statusCode != 200) // sanity check
            throw new Exception("Possible ehcache server inconsistency: returned with "+statusCode+" "+connection.getResponseMessage());
      // cache entry does not exist
      } catch (java.io.FileNotFoundException fnfe) {
        try {
            int statusCode = connection.getResponseCode();
            if (statusCode != 404)
                this.logger.error("Unexpected status code "+statusCode);
        } catch (Exception e) {
                this.logger.error("Possible inconsistency: unexpected exception "+e);
        }
        object = null;
      // some other errors
      } catch (Exception e) {
        //e.printStackTrace();
        this.logger.error(""+e);
        object = null;
      } finally {
        if (ois != null) try {
            ois.close();
        } catch (Exception ignore) {
            // ignored
        }
        ois = null;
        if (connection != null) try {
            connection.disconnect();
        } catch (Exception ignore) {
            // ignored
        }
        connection = null;
      }

        return object;
    }

    public void put(String key, Object value) {
        ObjectOutputStream oos = null;
        HttpURLConnection connection = null;
      try {
        //String encodedKey = URLEncoder.encode(key, "UTF-8");
        //URL url = new URL(cacheURL+"/"+encodedKey);
        //URL url = new URL(cacheURL+"/"+key);
        URL url = new URL(cacheURL+"/"+this.get_md5sum(key.getBytes()));
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.connect();
        oos = new ObjectOutputStream(connection.getOutputStream());
        oos.writeObject(value);
        oos.flush();
        int statusCode = connection.getResponseCode();
        if (statusCode != 201) // sanity check
            throw new Exception("Possible ehcache server inconsistency: returned with "+statusCode+" "+connection.getResponseMessage());
      } catch (Exception e) {
        //e.printStackTrace();
        this.logger.error(""+e);
      } finally {
        if (oos != null) try {
            oos.close();
        } catch (Exception ignore) {
            // ignored
        }
        oos = null;
        if (connection != null) try {
            connection.disconnect();
        } catch (Exception ignore) {
            // ignored
        }
        connection = null;
      }
        return;
    }

    public static void main(String[] args) throws Exception {
        String cacheURL = null;
        String key = null;
        String value = null;

        if (args.length == 2) {
            cacheURL = args[0];
            key = args[1];
            RemoteEhcache remoteEhcache = new RemoteEhcache(cacheURL);
            value = (String)remoteEhcache.get(key);
            System.err.println(value);
        } else if (args.length == 3) {
            cacheURL = args[0];
            key = args[1];
            value = args[2];
            RemoteEhcache remoteEhcache = new RemoteEhcache(cacheURL);
            remoteEhcache.put(key, value);
        } else {
            System.err.println("Usage: RemoteEhcache cacheURL key [value]");
            System.exit(1);
        }
    }
}
