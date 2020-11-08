package com.github.psacserve.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psacserve.BanServer;
import com.github.psacserve.Response.Result;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class QuickResult
{
    public static String error(String cause)
    {
        try
        {
            return "{\"success\":false,\"cause\":\"" + cause.replace("\"", "\\\"") + "\"}";
        }
        catch(Exception e)
        {
            BanServer.printStackTrace(e);
            return "";
        }
    }

    public static boolean isMissingFields(HashMap<String, String> provided, String... require)
    {
        return Arrays.stream(require).parallel()
                .filter(s -> !provided.containsKey(s))
                .toArray(String[]::new).length != 0;
    }

    public static Result missing(HashMap<String, String> provided, String... require)
    {
        LinkedList<String> re = new LinkedList<>(Arrays.asList(require));
        Arrays.stream(provided.keySet().toArray(new String[0])).parallel()
                .forEach(re::remove);

        return new Result(QuickResult.error("Missing query(ies): [" +
                String.join(", ", re) + "]"), 400);
    }

    public static <T> String successWithObject(String entName, T obj)
    {
        try
        {
            return new ObjectMapper().writeValueAsString(new HashMap<String, Object>(){{put("success", true); put(entName, obj);}});
        }
        catch (JsonProcessingException e)
        {
            BanServer.printStackTrace(e);
            return error("JSON_PARSE_ERROR");
        }
    }
}
