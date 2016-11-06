package jpl.mipl.juneberry.util;

/*
 * Copyright (c) 2011 - 2015, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged. All rights reserved.
 */

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ExecuteWatchdog;
//import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ExecuteException;

import java.io.File;
import java.io.IOException;

// Before jdk 7 (via nio.2), java does not have any built-in way to do this.
// so we use this hack to create and remove soft links.
/**
 * @author Xing
 */
public class SoftLinkManager {

    // default to current work dir
    private String dir = System.getProperty("user.dir");

    public SoftLinkManager() {
        this(null);
    }

    public SoftLinkManager(String dir) {
        if (dir != null)
            this.dir = dir;
    }

    public void create(String target) throws SoftLinkManagerException {
        CommandLine cl = new CommandLine("ln");
        // make symbolic links instead of hard links
        cl.addArgument("-s");
        // remove existing destination files
        cl.addArgument("-f");
        // this option is not available to all implementations of ln
        // specify the DIRECTORY in which to create the links
        //cl.addArgument("-t");
        cl.addArgument(target);
        cl.addArgument(this.dir);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(10000);
        executor.setWatchdog(watchdog);
        int exitValue = 1;
      try {
        exitValue = executor.execute(cl);
      } catch (ExecuteException ee) {
        throw new SoftLinkManagerException(ee);
      } catch (IOException ioe) {
        throw new SoftLinkManagerException(ioe);
      }
        if (exitValue != 0)
            throw new SoftLinkManagerException("exit value is "+exitValue);
    }

    public void remove(String name) throws SoftLinkManagerException {
        String path = (new File(this.dir, name)).getPath();
        CommandLine cl = new CommandLine("rm");
        cl.addArgument(path);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(10000);
        executor.setWatchdog(watchdog);
        int exitValue = 1;
      try {
        exitValue = executor.execute(cl);
      } catch (ExecuteException ee) {
        throw new SoftLinkManagerException(ee);
      } catch (IOException ioe) {
        throw new SoftLinkManagerException(ioe);
      }
        if (exitValue != 0)
            throw new SoftLinkManagerException("exit value is "+exitValue);
    }

    public static void main(String[] args) throws SoftLinkManagerException {
        SoftLinkManager softLinkManger = new SoftLinkManager();
        softLinkManger.create("/etc/passwd");
        System.err.println("soft link to /etc/passwd was created under current dir");
        softLinkManger.remove("passwd");
        System.err.println("soft link to /etc/passwd was removed under current dir");
    }
}
