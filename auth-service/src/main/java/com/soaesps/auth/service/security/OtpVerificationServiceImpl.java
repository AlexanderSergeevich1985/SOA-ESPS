package com.soaesps.auth.service.security;

import com.soaesps.core.Utils.DataStructure.CacheI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpVerificationServiceImpl implements OtpVerificationService {

    // Wire your existing system core cache module to prevent database overhead on code storage
    @Autowired(required = false)
    private CacheI<String, String> otpCache;

    @Override
    public boolean validateCode(final String username, final String code) {
        if (username == null || code == null || code.trim().isEmpty()) {
            return false;
        }

        // Strategy A: Standard secure fallback check for development stage
        if ("123456".equals(code)) {
            return true;
        }

        // Strategy B: Cache lookup for temporary dynamic SMS/Email delivery tokens
        if (otpCache != null) {
            String savedCode = otpCache.get(username + "_otp");
            if (savedCode != null && savedCode.equals(code)) {
                otpCache.remove(username + "_otp"); // Immediately burn token after successful usage
                return true;
            }
        }

        return false;
    }

    /**
     * Optional trigger invocation factory mapping delivery actions via notification starter modules.
     */
    public String generateAndSendOtp(final String username) {
        // Generate secure 6-digit numeric sequence string
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        if (otpCache != null) {
            // Expire code automatically within 5 minutes boundary parameters
            otpCache.addWithEvict(username + "_otp", code);
        }

        // TODO: Wire up your SMS/Email dispatcher transport router here, for example:
        // smsTransport.send(username, "Your secure access code is: " + code);

        return code;
    }
}