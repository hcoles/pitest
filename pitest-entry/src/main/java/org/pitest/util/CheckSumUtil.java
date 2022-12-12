package org.pitest.util;
import org.ekstazi.hash.Hasher;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

public class CheckSumUtil {
    private Hasher hasher;
    private Map<URL, String> checkSumMap; // map from URL to checksums, to reduce hashing
    public CheckSumUtil() {
        checkSumMap = new HashMap<>();
        hasher = new Hasher(Hasher.Algorithm.CRC32, 1000, true);
    }
    public String computeSingleCheckSum(URL url) {
        return hasher.hashURL(url.toExternalForm());
    }

    public String getCheckSum(URL url) {
        if (!checkSumMap.containsKey(url)) {
            String value = computeSingleCheckSum(url);
            checkSumMap.put(url, value);
        }
        return checkSumMap.get(url);
    }
}
