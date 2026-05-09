package com.peredereevin.authservice.service;

import com.peredereevin.authservice.entity.User;
import com.peredereevin.authservice.io.ProfileRequest;
import com.peredereevin.authservice.io.ProfileResponse;
import com.peredereevin.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImplementation implements ProfileService{

    private final UserRepository userRepository;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        User newProfile = convertToUser(request);
        if (!userRepository.existsByEmail(request.getEmail())){
            newProfile = userRepository.save(newProfile);
            return convertToUser(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Данный email уже существует");
    }

    private ProfileResponse convertToUser(User newProfile){
        return ProfileResponse.builder()
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .name(newProfile.getName())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .build();
    }

    private User convertToUser(ProfileRequest request) {
        return User.builder()
                    .email(request.getEmail())
                    .userId(UUID.randomUUID().toString())
                    .name(request.getName())
                    .password(request.getPassword())
                    .isAccountVerified(false)
                    .resetOtpExpireAt(0L)
                    .verifyOtp(null)
                    .verifyOtpExpireAt(0L)
                    .resetOtp(null)
                    .build();
    }
}