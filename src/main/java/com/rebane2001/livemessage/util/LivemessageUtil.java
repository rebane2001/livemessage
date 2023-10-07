package com.rebane2001.livemessage.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LivemessageUtil {
    public static final List<Pattern> FROM_PATTERNS = new ArrayList<>(
            Arrays.asList(
                    Pattern.compile("^From (\\w{3,16}): (.*)"),
                    Pattern.compile("^from (\\w{3,16}): (.*)"),
                    Pattern.compile("^(\\w{3,16}) whispers: (.*)"),
                    Pattern.compile("^\\[(\\w{3,16}) -> me\\] (.*)"),
                    Pattern.compile("^(\\w{3,16}) whispers to you: (.*)")
            )
    );

    public static final List<Pattern> TO_PATTERNS = new ArrayList<>(
            Arrays.asList(
                    Pattern.compile("^To (\\w{3,16}): (.*)"),
                    Pattern.compile("^to (\\w{3,16}): (.*)"),
                    Pattern.compile("^\\[me -> (\\w{3,16})\\] (.*)"),
                    Pattern.compile("^You whisper to (\\w{3,16}): (.*)")
            )
    );
}
