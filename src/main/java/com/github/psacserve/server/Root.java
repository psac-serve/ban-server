package com.github.psacserve.server;

import com.github.psacserve.BanServer;
import com.github.psacserve.Response.Result;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Scanner;

public class Root implements HttpHandler
{

    @Override
    public void handle(HttpExchange s) throws IOException
    {
        StringBuilder body = new StringBuilder();

        if (!s.getRequestHeaders().containsKey("Token"))
        {
            String message = QuickResult.error("Missing one or more header(s) [Token]");
            s.sendResponseHeaders(403, message.getBytes().length);
            s.getResponseBody().write(message.getBytes());
            return;
        }

        if (s.getRequestHeaders().get("Token").size() != 1 ||
                !s.getRequestHeaders().get("Token").get(0).equals(BanServer.token))
        {
            String message = QuickResult.error("Invalid Token");
            s.sendResponseHeaders(403, message.getBytes().length);
            s.getResponseBody().write(message.getBytes());
        }

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

        Result result = Parser.parse(s.getRequestMethod(), s.getRequestMethod().equals("GET") && s.getRequestURI().toString().contains("?") ? uri[0]: s.getRequestURI().toString(), req);

        OutputStream rB = s.getResponseBody();
        s.sendResponseHeaders(result.code, result.body.equals("") ? 0: result.body.getBytes().length);
        if (result.body.length() != 0)
            rB.write(result.body.getBytes());
        rB.close();
    }
}
