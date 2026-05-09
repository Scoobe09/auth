package com.peredereevin.authservice.entity;

public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String verifyOtp;
    private Boolean isAccountVerified;
    private Long verifyOtpExpireAt;
    private String resetOtp;
    private Long resetOtpExpireAt;
}
