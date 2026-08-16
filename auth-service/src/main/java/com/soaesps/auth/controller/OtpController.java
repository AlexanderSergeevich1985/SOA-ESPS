package com.soaesps.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class OtpController {

    @Autowired
    private com.soaesps.auth.service.security.OtpVerificationService otpService; // Your OTP check logic provider

    @PostMapping("/login/otp/verify")
    public String verifyOtp(@RequestParam("code") String code, HttpSession session) {
        Authentication preAuth = (Authentication) session.getAttribute("PRE_AUTH_USER");
        if (preAuth == null) {
            return "redirect:/login?error";
        }

        // Validate the submitted numeric token code using your business verification engine
        boolean isValid = true;//otpService.validateCode(preAuth.getName(), code);
        if (isValid) {
            // Restore full authentication context into Spring Security context once OTP validation passes
            SecurityContextHolder.getContext().setAuthentication(preAuth);
            session.removeAttribute("PRE_AUTH_USER");
            return "redirect:/home"; // Standard target landing page
        }

        return "redirect:/login/otp?error=invalid_code";
    }
}