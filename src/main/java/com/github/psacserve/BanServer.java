package com.github.psacserve;

import com.zaxxer.hikari.HikariDataSource;
import develop.p2p.lib.FileConfiguration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class BanServer
{
    public static Logger logger;
    public static FileConfiguration config;
    public static HikariDataSource log;
    public static HikariDataSource bans;

    public static void main(String[] args)
    {
        logger = Logger.getLogger("PeyangBanManager");
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %5$s%6$s%n");
        start();
    }

    private static void start()
    {
        logger.info("サーバーを開始しています...");
        logger.info("設定ファイルを読み込んでいます...");
        config = new FileConfiguration("config.yml");
        config.saveDefaultConfig();
        Init.editCheck();

        logger.info("暗号化キーを探しています...");
        Init.searchAndGenerateKey();

        logger.info("データベースに接続してます...");
        Init.connectDB();

        logger.info("データベースにテーブルを追加しています...");
        Init.initDatabase();

        logger.info("サーバーをスタートします。");
        Init.startServer(config.get("con.port"));
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
            logger.info("LOGデータベースを停止しています...");
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
