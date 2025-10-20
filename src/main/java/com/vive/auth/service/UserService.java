package com.vive.auth.service;

import com.vive.auth.dto.UpdateProfileRequest;
import com.vive.auth.dto.UserResponse;
import com.vive.auth.entity.User;
import com.vive.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // name과 picture 모두 업데이트 (null이면 기존 값 유지)
        String newName = request.getName() != null ? request.getName() : user.getName();
        String newPicture = request.getPicture() != null ? request.getPicture() : user.getPicture();

        user.update(newName, newPicture);
        User updatedUser = userRepository.save(user);

        return UserResponse.from(updatedUser);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.deactivate();
        userRepository.save(user);
    }
}
