package com.github.psacserve.Response;

import java.util.*;

public class Result
{
    public HashMap<String, Object> body;
    public int code;

    public Result(HashMap<String, Object> body, int code)
    {
        this.body = body;
        this.code = code;
    }
}
