package com.vive.auth.service;

import com.vive.auth.entity.User;
import com.vive.auth.repository.UserRepository;
import com.vive.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // LOCAL 계정으로 먼저 찾기 (로그인은 로컬 계정만 가능)
        User user = userRepository.findByEmailAndProvider(email, User.Provider.LOCAL)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 비활성화된 사용자는 로그인 불가
        if (!user.getActive()) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        return UserPrincipal.create(user);
    }
}