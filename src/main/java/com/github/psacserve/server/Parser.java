package com.github.psacserve.server;

import com.github.psacserve.BanServer;
import com.github.psacserve.Moderate.Ban;
import com.github.psacserve.Response.BanEntry;
import com.github.psacserve.Response.Result;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Parser
{
    public static Result parse(final String method, final String path, final String request)
    {


        HashMap<String, String> req = parseRequest(request);


        switch (path)
        {
            case "/ban":
                if (!method.equals("PUT"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);
                if (QuickResult.isMissingFields(req, "reason", "expire", "uuid", "by"))
                    return QuickResult.missing(req, "reason", "expire", "uuid", "by");
                final String reason;
                try
                {
                    reason = URLDecoder.decode(req.get("reason"), "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    return new Result("Failed to reason parsing.", 405);
                }

                if (!req.get("expire").matches("^[0-9]+$") && !req.get("expire").equals("_PERM"))
                    return new Result(req.get("expire") + " is not a DateTime.", 405);

                new Thread(() -> {
                    Ban.ban(req.get("uuid").replace("-", ""),
                            req.get("by").replace("-", ""),
                            reason,
                            req.get("expire").equals("_PERM") ? null: new Date(Long.parseLong(req.get("expire"))),
                            req.containsKey("staff") && Boolean.parseBoolean(req.get("staff"))
                    );
                }).start();

                return new Result(QuickResult.successWithObject("state", "Processed."), 202);
            case "/unban":
            case "/pardon":
                if (!method.equals("DELETE"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);
                if (QuickResult.isMissingFields(req, "uuid", "by", "reason"))
                    return QuickResult.missing(req, "uuid", "by", "reason");

                final String pardonReason;
                try
                {
                    pardonReason = URLDecoder.decode(req.get("reason"), "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    return new Result("Failed to reason parsing.", 405);
                }

                new Thread(() -> {
                    Ban.pardon(req.get("uuid").replace("-", ""), req.get("by").replace("-", ""), pardonReason);
                }).start();

                return new Result(QuickResult.successWithObject("state", "Processed."), 202);
            case "/bans":
                if (!method.equals("GET"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);

                if (QuickResult.isMissingFields(req, "uuid"))
                    return new Result(QuickResult.error("Missing one field [uuid]"), 400);

                return new Result(QuickResult.successWithObject("bans", Ban.getBans(req.get("uuid"))), 200);
            case "/getban":
                if (!method.equals("GET"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);

                if (QuickResult.isMissingFields(req, "uuid"))
                    return new Result(QuickResult.error("Missing one field [uuid]"), 400);

                final BanEntry entry = Ban.getBan(req.get("uuid"));

                if (entry == null)
                    return new Result(QuickResult.error("This player is not banned."), 404);

                return new Result(QuickResult.successWithObject("ban", entry), 200);
            case "/weekly":
                if (!method.equals("GET"))
                    return new Result(QuickResult.error(method + " is not allowed."), 405);
                final Calendar calender = Calendar.getInstance();
                calender.setTime(new Date());
                calender.add(Calendar.DAY_OF_MONTH, -7);

                final long btw = Date.from(calender.toInstant()).getTime();
                long staff = 0;
                long other = 0;
                try (final Connection connection = BanServer.bans.getConnection();
                     final Connection l = BanServer.log.getConnection();
                     final PreparedStatement sr = connection.prepareStatement("SELECT STAFF FROM ban WHERE DATE BETWEEN ? AND ?");
                     final PreparedStatement spb = l.prepareStatement("SELECT STAFF FROM main.ban WHERE DATE BETWEEN ? AND ?"))
                {
                    sr.setLong(1, btw);
                    sr.setLong(2, new Date().getTime());
                    spb.setLong(1, btw);
                    spb.setLong(2, new Date().getTime());

                    ResultSet sC = sr.executeQuery();
                    while (sC.next())
                        if (sC.getInt("STAFF") == 0)
                            staff++;
                        else
                            other++;
                    ResultSet oC = spb.executeQuery();
                    while (oC.next())
                        if (oC.getInt("STAFF") == 0)
                            staff++;
                        else
                            other++;
                }
                catch (Exception e)
                {
                    return new Result(QuickResult.error("Error: " + e.getMessage()), 503);
                }

                long finalStaff = staff;
                long finalOther = other;
                return new Result(QuickResult.successWithObject("data", new HashMap<String, Long>(){{put("staff", finalStaff); put("other", finalOther);}}), 200);

            case "/teapot":
                return new Result(QuickResult.error("I'm a teapot."), 418);
            default:
                return new Result(QuickResult.error("Method not found."), 403);
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
                    {
                        resp.put(key[0], "");
                        return;
                    }
                    resp.put(key[0], key[1]);
                });
        return resp;
    }
}
