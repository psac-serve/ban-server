package com.github.psacserve;

import com.github.psacserve.server.Root;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.github.psacserve.BanServer.bans;
import static com.github.psacserve.BanServer.config;
import static com.github.psacserve.BanServer.log;
import static com.github.psacserve.BanServer.logger;
import static com.github.psacserve.BanServer.stop;

public class Init
{

    public static void injectConfig()
    {
        try
        {
            Field cfg = config.getClass().getDeclaredField("cfg");

            cfg.setAccessible(true);

            cfg.set(config, new File(BanServer.dirPath + "config.yml"));
            if (!((File) cfg.get(config)).exists())
            {
                new File(BanServer.dirPath).mkdirs();
                Files.copy(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream("config.yml")), new File(BanServer.dirPath + "config.yml").toPath());
            }

        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
            BanServer.stop(1);
        }
    }

    public static void antiLag()
    {
        if (!((boolean) config.get("con.antilag.enable")))
        {
            logger.warning("");
            logger.warning("WARNING： If you don't use anti-lag system, connections can take a considerable amount of time.");
            logger.warning("");
            return;
        }

        logger.info("Setting up anti-lag system timer...");
        new Timer().scheduleAtFixedRate(new TimerTask()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                try
                                                {
                                                    Class.forName("com.github.psacserve.server.Parser");
                                                    Class.forName("com.github.psacserve.server.QuickResult");
                                                    Class.forName("com.github.psacserve.server.Root");
                                                    Class.forName("com.github.psacserve.Response.BanEntry");
                                                    Class.forName("com.github.psacserve.Moderate.Ban");
                                                    Class.forName("com.github.psacserve.Response.Result");
                                                }
                                                catch (Exception e)
                                                {
                                                    BanServer.printStackTrace(e);
                                                }
                                            }
                                        }, 1000L, Math.multiplyExact(
                Math.multiplyExact(
                        Long.parseLong(config.get("con.antilag.exe").toString()), 60L), 1000L)
        );
    }

    public static void token()
    {
        if (!((boolean) config.get("con.token")))
        {
            logger.warning("");
            logger.warning("WARNING： If you don't use the token, databases can access and write from third party.");
            logger.warning("");
            return;
        }

        logger.info("Reading the token...");
        if (!Token.exists())
        {
            logger.info("The token not found.");
            logger.info("Generating a token...");
            Token.genToken();
        }

        BanServer.token = Token.getToken();
    }

    public static void startServer(int port)
    {
        logger.info("Binding root server...");
        HttpServer server;
        try
        {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new Root());
        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
            BanServer.stop(1);
            return;
        }

        new Thread(() -> {
            logger.info("Root server successfully bound at port " + port + ".");
            server.start();
        }).start();
    }

    public static void editCheck()
    {
        if (!(boolean) config.get("edit"))
        {
            logger.severe("The configuration file doesn't changed!");
            logger.info("NOTE: If the configuration file was changed, set the key 'edit' to the value 'true'.");
            stop(1);
        }
    }

    public static void connectDB()
    {
        bans = Init.getHikariConfig(config.getString("database.abuse.driver"), config.getString("database.abuse.jdbcUrl"));
        log = Init.getHikariConfig(config.getString("database.log.driver"), config.getString("database.log.jdbcUrl"));
    }

    public static void initDatabase()
    {
        try (Connection banC = BanServer.bans.getConnection();
             Statement banS = banC.createStatement();
             Connection logC = BanServer.log.getConnection();
             Statement logS = logC.createStatement())
        {
            banS.execute("CREATE TABLE IF NOT EXISTS ban(" +
                    "UUID text," +
                    "BANID text," +
                    "DATE text," +
                    "REASON text," +
                    "EXPIRE text," +
                    "STAFF integer," +
                    "BANNEDBY text" +
                    ")");
            logS.execute("CREATE TABLE IF NOT EXISTS ban(" +
                    "UUID text," +
                    "BANID text," +
                    "DATE text," +
                    "REASON text," +
                    "EXPIRE text," +
                    "UNBANDATE text," +
                    "STAFF integer," +
                    "BANNEDBY text," +
                    "UNBANNEDBY text," +
                    "UNBANREASON text" +
                    ")");

        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
        }
    }

    public static HikariDataSource getHikariConfig(String method, String url)
    {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setDriverClassName(method);
        return new HikariDataSource(config);
    }
}
