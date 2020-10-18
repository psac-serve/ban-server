package com.github.psacserve.server;

import com.github.psacserve.BanServer;

import javax.print.attribute.HashAttributeSet;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

public class Parser
{
    public static Result parse(String path, String request)
    {
        switch (path)
        {
            case "/":
                return new Result(JSON.response(false, "Invalid argument."), 400);
            case "/info":
                HashMap<String, String> req = parseRequest(request);
                return new Result(JSON.response(true, "Test Content."), 200);
            default:
                return new Result("No contents.", 204);
        }
    }

    public static HashMap<String, String> parseRequest(String request)
    {
        HashMap<String, String> resp = new HashMap<>();
        if (request.equals(""))
            return resp;
        Arrays.stream(request.split("&"))
                .parallel()
                .forEach(s -> {
                    String[] key = s.split("=");
                    if (key.length != 2)
                        BanServer.printStackTrace(new ParseException("Failed to parsing the parameter(s).", -1));
                    resp.put(key[0], key[1]);
                });
        return resp;
    }
}
