package com.github.psacserve;

import com.github.psacserve.server.Root;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.Statement;

import static com.github.psacserve.BanServer.bans;
import static com.github.psacserve.BanServer.config;
import static com.github.psacserve.BanServer.log;
import static com.github.psacserve.BanServer.logger;
import static com.github.psacserve.BanServer.stop;

public class Init
{

    public static void token()
    {
        if (!((boolean) config.get("security.token")))
        {
            logger.warning("");
            logger.warning("警告：トークンを使用しない場合、第三者にアクセスされデータベースを書き換えられる恐れがあります。");
            logger.warning("");
            return;
        }

        logger.info("トークンを読み込んでいます...");
        if (!Token.exists())
        {
            logger.info("トークンが見つかりませんでした。");
            logger.info("トークンを生成します...");
            Token.genToken();
        }

        BanServer.token = Token.getToken();
    }


    public static void startServer(int port)
    {
        logger.info("コアサーバをバインドしています...");
        HttpServer server;
        try
        {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new Root());
        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
            return;
        }

        new Thread(() -> {
            logger.info("コアサーバを" + port + "番でバインドしました。");
            server.start();
        }).start();
    }

    public static void editCheck()
    {
        if (!(boolean)config.get("edit"))
        {
            logger.severe("設定ファイルが一回も開かれていないないし編集がされていません！");
            logger.info("NOTE: 開いたことがあるまたは編集したことがある場合、'edit'キーをtrueにセットしてください！");
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
        try(Connection banC = BanServer.bans.getConnection();
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
                    "STAFF integer)");
            logS.execute("CREATE TABLE IF NOT EXISTS ban(" +
                    "UUID text," +
                    "BANID text," +
                    "DATE text," +
                    "REASON text," +
                    "UNBANDATE text," +
                    "STAFF integer)");
            logS.execute("CREATE TABLE IF NOT EXISTS kick(" +
                    "PLAYER text," +
                    "UUID text," +
                    "KICKID text," +
                    "DATE text," +
                    "REASON text," +
                    "STAFF integer)");

        }
        catch(Exception e)
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
