package com.peredereevin.authservice.service;

import com.peredereevin.authservice.io.ProfileRequest;
import com.peredereevin.authservice.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);
}
