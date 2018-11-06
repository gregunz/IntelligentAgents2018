package utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    // chops a list into N non-view sublists
    public static <T> List<List<T>> chopped(List<T> list, final int N) {
        List<List<T>> parts = new ArrayList<>();

        final int length = list.size();
        final int step = Math.max(1, length / N);

        int fromIdx = 0;
        int toIdx = length;

        for (int i = 0; i < N - 1; i++) {
            fromIdx = Math.min(length - 1, step * i);
            toIdx = Math.min(length, step * (i + 1));
            parts.add(new ArrayList<T>(list.subList(fromIdx, toIdx)));
        }
        parts.add(new ArrayList<T>(list.subList(toIdx, length)));

        assert parts.size() == N;
        return parts;
    }
}
