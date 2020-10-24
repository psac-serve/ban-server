package com.github.psacserve;

import com.zaxxer.hikari.HikariDataSource;
import develop.p2p.lib.FileConfiguration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BanServer
{
    public static Logger logger;
    public static FileConfiguration config;
    public static HikariDataSource log;
    public static HikariDataSource bans;
    public static String token;
    public static final String dirPath = new File(BanServer.class.getProtectionDomain().getCodeSource().getLocation().toString().replaceFirst("file:([/\\\\])", "")).getParent() +
            System.getProperty("file.separator") +
            "PeyangBanServer" +
            System.getProperty("file.separator");

    public static void main(String[] args)
    {
        logger = Logger.getLogger("PeyangBanManager");

        if (Double.parseDouble(System.getProperty("java.specification.version")) != 1.8)
            logger.warning("PeyangBanServer は、JDK1.8.0(u221) で開発/確認しております。" +
                    "\nJava8以外のバージョンを使用する場合、意図しない動作または動作しない といったバグが発生する可能性がございます。");

        Logger.getLogger("com.zaxxer.hikari.HikariDataSource").setLevel(Level.OFF);
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS [%4$s] %5$s%6$s%n");
        Locale.setDefault(Locale.US);
        start();
    }

    private static void start()
    {
        long start = System.currentTimeMillis();
        logger.info("サーバーを開始しています...");
        logger.info("設定ファイルを読み込んでいます...");
        config = new FileConfiguration("config.yml");
        Init.injectConfig();
        config.saveDefaultConfig();
        Init.editCheck();

        Init.token();

        logger.info("データベースに接続してます...");
        Init.connectDB();

        logger.info("データベースにテーブルを追加しています...");
        Init.initDatabase();

        Init.antiLag();

        logger.info("サーバーをスタートします。");
        Init.startServer(config.get("con.port"));

        long end = System.currentTimeMillis() - start;
        double time = (double) end / 1000;

        logger.info(time + "秒で起動が完了しました！");
    }

    public static void stop(int code)
    {
        logger.info("サーバーを停止しています...");

        if (log != null)
        {
            logger.info("LOGデータベースを停止しています...");
            log.close();
            log = null;
        }

        if (bans != null)
        {
            logger.info("BANデータベースを停止しています...");
            bans.close();
            bans = null;
        }

        System.exit(code);
    }

    public static void printStackTrace(Exception e)
    {
        try(StringWriter sw = new StringWriter();
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
