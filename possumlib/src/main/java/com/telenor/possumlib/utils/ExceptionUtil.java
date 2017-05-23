package com.telenor.possumlib.utils;

import android.support.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import java.util.Map;

public class ExceptionUtil {
    public static String combineAllStackTraces() {
        return Joiner.on("\n\n").join(
                Iterables.transform(
                        Thread.getAllStackTraces().entrySet(),
                        new Function<Map.Entry<Thread, StackTraceElement[]>, Object>() {
                            @Nullable
                            @Override
                            public Object apply(Map.Entry<Thread, StackTraceElement[]> entry) {
                                return entry.getKey().getName() + "\n" + Joiner.on("\n").join(entry.getValue());
                            }
                        }));
    }
}
