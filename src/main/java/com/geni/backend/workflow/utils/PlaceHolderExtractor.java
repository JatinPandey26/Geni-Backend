package com.geni.backend.workflow.utils;

import com.geni.backend.common.exception.WorkflowValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceHolderExtractor {

    private static final Pattern PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    public static List<String> extract(Object value) {
        List<String> result = new ArrayList<>();

        if(value instanceof String str){
            result.addAll(extractPattern(str));
        }
        else if(value instanceof List<?> list){
            list.stream().forEach(s -> result.addAll(extractPattern(s.toString())));
        }
        else
            throw new WorkflowValidationException("Type of value is not supported");

        return  result;
    }

    private static List<String> extractPattern(String str){
        List<String> result = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(str);

        while (matcher.find()) {
            result.add(matcher.group(1).trim());
        }

        return result;
    }
}
