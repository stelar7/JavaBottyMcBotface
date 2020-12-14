package no.stelar7.botty.utils;

import java.util.*;
import java.util.stream.Collectors;

public class GeneralUtils
{
    public static <T> Set<T> intersection(Collection<T> a, Collection<T> b)
    {
        return a.stream().distinct().filter(b::contains).collect(Collectors.toSet());
    }
}
