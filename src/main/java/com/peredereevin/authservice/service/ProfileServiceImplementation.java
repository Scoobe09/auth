package com.peredereevin.authservice.service;

import com.peredereevin.authservice.entity.User;
import com.peredereevin.authservice.io.ProfileRequest;
import com.peredereevin.authservice.io.ProfileResponse;
import com.peredereevin.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImplementation implements ProfileService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        User newProfile = convertToUser(request);
        if (!userRepository.existsByEmail(request.getEmail())){
            newProfile = userRepository.save(newProfile);
            return convertToProfileResponse(newProfile);
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Данный email уже существует");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким email не найден: " + email));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        User existingEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(1000000));

        long expiryTime = System.currentTimeMillis() + (15 * 60 * 1000);

        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpireAt(expiryTime);

        userRepository.save(existingEntity);

        try {
            emailService.sendResetOtpEmail(existingEntity.getEmail(), otp);
        } catch (Exception exception) {
            throw new RuntimeException("Не получилось отправить сообщение");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Неверный одноразовый код");
        }

        if (existingUser.getResetOtpExpireAt() < System.currentTimeMillis()) {
            throw new RuntimeException("Одноразовый код истёк");
        }

        existingUser.setPassword(newPassword);
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);

        userRepository.save(existingUser);
    }

    private ProfileResponse convertToProfileResponse(User newProfile){
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
                    .password(passwordEncoder.encode(request.getPassword()))
                    .isAccountVerified(false)
                    .resetOtpExpireAt(0L)
                    .verifyOtp(null)
                    .verifyOtpExpireAt(0L)
                    .resetOtp(null)
                    .build();
    }
}