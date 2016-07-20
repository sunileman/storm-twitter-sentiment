package com.teradata.storm;

import java.util.regex.Pattern;

/**
 * Created by sm186102 on 11/11/15.
 */
public class regtest {


    public static void main(String... args) throws Exception {

        String string = "004-SunileIsAwesome-034556";
        String[] parts = string.split(Pattern.quote("-SunileIsAwesome-"));
        String part1 = parts[0]; // 004
        String part2 = parts[1]; // 034556


        System.out.println(part1);
        System.out.println(part2);

    }
}
