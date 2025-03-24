package dev.kapiaszczyk.mpp.tokens;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory storage for blacklisted JWT tokens.
 */
@Component
public class JWTBlacklist {

    private final Map<String, Long> blacklist;

    public JWTBlacklist() {
        this.blacklist = new HashMap<>();
    }

    public void addToBlacklist(String token, Long expirationTime) {
        removeExpiredTokens();
        blacklist.put(token, expirationTime);
    }

    public boolean isTokenBlacklisted(String token) {
        removeExpiredTokens();
        return blacklist.containsKey(token);
    }

    private void removeExpiredTokens() {
        blacklist.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis() / 1000);
    }

}
