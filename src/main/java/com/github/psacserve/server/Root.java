package com.github.psacserve.server;

import com.github.psacserve.BanServer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jdk.nashorn.internal.ir.CatchNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Root implements HttpHandler
{

    @Override
    public void handle(HttpExchange s) throws IOException
    {
        StringBuilder body = new StringBuilder();

        InputStream is = s.getRequestBody();

        try
        {
            Scanner scanner = new Scanner(is);
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
            uri = s.getRequestURI().toString().split("\\?");
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
