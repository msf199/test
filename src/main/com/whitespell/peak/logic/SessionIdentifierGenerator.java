package main.com.whitespell.peak.logic;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdentifierGenerator {
    private static SecureRandom random = new SecureRandom();

    public static String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    public static String nextEmailId() {
        return new BigInteger(160, random).toString(32);
    }

    public static String nextResetId() {
        return new BigInteger(180, random).toString(32);
    }

    public static String nextTempPass(){
        return new BigInteger(130, random).toString(6);
    }
}