package com.github.psacserve.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.psacserve.BanServer;

import java.util.Arrays;
import java.util.HashMap;

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

    public static boolean isMissingFields(HashMap<String, String> require, String... provided)
    {
        return Arrays.stream(provided).parallel().noneMatch(require::containsKey);
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
