package com.github.psacserve.server;

import com.fasterxml.jackson.databind.*;
import com.github.psacserve.BanServer;
import com.github.psacserve.Response.Result;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.StringUtils;
import org.msgpack.core.*;
import org.msgpack.jackson.dataformat.*;
import org.msgpack.value.*;

import java.awt.image.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Root implements HttpHandler
{

    @Override
    public void handle(HttpExchange s) throws IOException
    {
        StringBuilder body = new StringBuilder();

        s.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        try
        {
            Scanner scanner = new Scanner(s.getRequestBody());
            while (scanner.hasNext())
                body.append(scanner.next());
        }
        catch (Exception e)
        {
            BanServer.printStackTrace(e);
        }

        String req = body.toString();

        String[] uri = new String[0];

        if (s.getRequestMethod().equals("GET") && s.getRequestURI().toString().contains("?"))
        {
            uri = StringUtils.split(s.getRequestURI().toString(), "?");
            req = uri[1];
        }

        HashMap<String, String> querys = parseRequest(req);

        if (!s.getRequestHeaders().containsKey("Token"))
        {
            ObjectMapper mapper = querys.containsKey("raw") && querys.get("raw").equals("true") ?
                    new ObjectMapper(): new ObjectMapper(new MessagePackFactory());
            byte[] bytes = mapper.writeValueAsBytes(QuickResult.error("Missing header: [Token]"));
            s.sendResponseHeaders(403, bytes.length);
            s.getResponseBody().write(bytes);
            return;
        }

        if (s.getRequestHeaders().get("Token").size() != 1 ||
                !s.getRequestHeaders().get("Token").get(0).equals(BanServer.token))
        {
            ObjectMapper mapper = querys.containsKey("raw") && querys.get("raw").equals("true") ?
                    new ObjectMapper(): new ObjectMapper(new MessagePackFactory());
            byte[] bytes = mapper.writeValueAsBytes(QuickResult.error("Invalid Token"));
            s.sendResponseHeaders(403, bytes.length);
            s.getResponseBody().write(bytes);
            return;
        }

        final String method = s.getRequestMethod();
        final String path = s.getRequestMethod().equals("GET") && s.getRequestURI().toString().contains("?") ? uri[0]: s.getRequestURI().toString();
        BanServer.logger.info(method + ":   FROM: " + s.getRemoteAddress().getAddress().toString() + "    " + path + "  " + req);

        Result result = Parser.parse(method, path, querys);

        OutputStream rB = s.getResponseBody();

        if (querys.containsKey("raw") && querys.get("raw").equals("true"))
        {
            String json = new ObjectMapper().writeValueAsString(result.body);
            s.sendResponseHeaders(result.code, json.getBytes(StandardCharsets.UTF_8).length);
            rB.write(json.getBytes(StandardCharsets.UTF_8));
            rB.close();
            return;
        }

        final byte[] b = new ObjectMapper(new MessagePackFactory()).writeValueAsBytes(result.body);
        s.sendResponseHeaders(result.code, b.length);
        rB.write(b);
        rB.close();
    }


    private static HashMap<String, String> parseRequest(String request)
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
