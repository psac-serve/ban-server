package com.github.psacserve.server;

import com.github.psacserve.BanServer;
import com.github.psacserve.Moderate.Ban;
import com.github.psacserve.Response.BanEntry;
import com.github.psacserve.Response.Result;
import develop.p2p.lib.SQLModifier;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class Parser
{
    public static Result parse(final String method, final String path, final String request)
    {

        BanServer.logger.info(method + ":   " + path + "  " + request);

        HashMap<String, String> req = parseRequest(request);


        switch (path)
        {
            case "/unban":
            case "/pardon":
                if (!method.equals("DELETE"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);
                if (QuickResult.isMissingFields(req, "uuid"))
                    return new Result(QuickResult.error("Missing one field [uuid]"), 400);

                new Thread(() -> {
                    try(Connection ban = BanServer.bans.getConnection();
                        PreparedStatement banLp = ban.prepareStatement("SELECT UUID, BANID, DATE, REASON, STAFF FROM ban WHERE UUID=?");
                        Connection log = BanServer.log.getConnection())
                    {
                        banLp.setString(1, req.get("uuid"));
                        ResultSet set = banLp.executeQuery();
                        if (!set.next())
                            return;
                        SQLModifier.insert(log, "ban",
                                set.getString("UUID"),
                                set.getString("BANID"),
                                set.getString("DATE"),
                                set.getString("REASON"),
                                new Date().getTime(),
                                set.getString("STAFF")
                        );
                        SQLModifier.delete(ban, "ban", new HashMap<String, String>(){{put("UUID",req.get("uuid"));}});
                    }
                    catch (Exception e)
                    {
                        BanServer.printStackTrace(e);
                    }
                }).start();

                return new Result(QuickResult.successWithObject("state", "processing"), 202);


            case "/bans":
                if (!method.equals("GET"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);

                if (QuickResult.isMissingFields(req, "uuid"))
                    return new Result(QuickResult.error("Missing one field [uuid]"), 400);

                return new Result(QuickResult.successWithObject("bans", Ban.getBans(req.get("uuid"))),
                        200);
            case "/getban":
                if (!method.equals("GET"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);

                if (QuickResult.isMissingFields(req, "uuid"))
                    return new Result(QuickResult.error("Missing one field [uuid]"), 400);

                final BanEntry entry = Ban.getBan(req.get("uuid"));

                if (entry == null)
                    return new Result(QuickResult.error("This player is not banned."), 404);

                return new Result(QuickResult.successWithObject("ban", entry), 200);
            case "/teapot":
                return new Result("I'm a teapot.", 418);
            default:
                return new Result("<h1>403 Forbidden<h1>", 403);
        }
    }

    public static HashMap<String, String> parseRequest(String request)
    {
        HashMap<String, String> resp = new HashMap<>();
        if (request.equals(""))
            return resp;
        Arrays.stream(StringUtils.split(request, "&"))
                .parallel()
                .forEach(s -> {
                    String[] key = StringUtils.split(s, "=");
                    if (key.length != 2)
                        BanServer.printStackTrace(new ParseException("Failed to parsing the parameter(s).", -1));
                    resp.put(key[0], key[1]);
                });
        return resp;
    }
}
