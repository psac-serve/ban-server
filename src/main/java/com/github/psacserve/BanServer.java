package com.github.psacserve;

import com.zaxxer.hikari.HikariDataSource;
import develop.p2p.lib.FileConfiguration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BanServer
{
    public static final String dirPath = new File(BanServer.class.getProtectionDomain().getCodeSource().getLocation().toString().replaceFirst("file:([/\\\\])", "")).getParent() +
            System.getProperty("file.separator") +
            "PeyangBanServer" +
            System.getProperty("file.separator");
    public static Logger logger;
    public static FileConfiguration config;
    public static HikariDataSource log;
    public static HikariDataSource bans;
    public static String token;

    public static void main(String[] args)
    {
        logger = Logger.getLogger("PeyangBanManager");

        if (Double.parseDouble(System.getProperty("java.specification.version")) != 1.8)
            logger.warning("PeyangBanServer is developing / running in JDK 1.8.0 update 221. If you running it in different version, it may occur unintended behavior or not starting.");

        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tH:%1$tM:%1$tS %4$s] %5$s%6$s%n");
        Locale.setDefault(Locale.US);
        start();
    }

    private static void start()
    {
        long start = System.currentTimeMillis();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        logger.info("--- STARTING SERVER ON " + format.format(new Date()) + " ---");
        logger.info("Loading configuration file...");
        config = new FileConfiguration("config.yml");
        Init.injectConfig();
        Init.editCheck();

        Init.token();

        logger.info("Connecting to database...");
        Init.connectDB();

        logger.info("Initializing tables of connected database...");
        Init.initDatabase();

        Init.antiLag();
        Init.startServer(config.get("con.port"));

        long end = System.currentTimeMillis();

        logger.info("--- SERVER STARTED ON " + format.format(new Date()) + " ---");

        long timeDifference = end - start;
        double time = (double) timeDifference / 1000;

        logger.info("    IN " + time + "s");
    }

    public static void stop(int code)
    {
        stop(code, true);
    }

    public static void stop(int code, boolean real)
    {
        logger.info("--- STOPPING SERVER ---");

        if (log != null)
        {
            logger.info("Stopping logging database...");
            log.close();
            log = null;
        }

        if (bans != null)
        {
            logger.info("Stopping ban database...");
            bans.close();
            bans = null;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        System.out.println("--- SERVER STOPPED ON " + format.format(new Date()) + " ---");

        if (real)
            System.exit(code);
    }

    public static void printStackTrace(Exception e)
    {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw))
        {
            e.printStackTrace(pw);
            sw.flush();
            logger.warning("");
            logger.warning("!!!AN EXCEPTION HAS OCCURRED!!!");
            logger.warning(sw.toString());
            logger.warning("");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
