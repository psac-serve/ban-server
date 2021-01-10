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
    public static HashMap<String, Object> error(String cause)
    {
        return new HashMap<String, Object>()
        {{
            put("success", false);
            put("cause", cause);
        }};
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

    public static <T> HashMap<String, Object> successWithObject(String entName, T obj)
    {
        return new HashMap<String, Object>()
        {{
            put("success", true);
            put(entName, obj);
        }};
    }
}
