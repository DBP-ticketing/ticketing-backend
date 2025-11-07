package com.DBP.ticketing_backend.domain.auth.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpHostRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpUserRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.LoginResponseDto;
import com.DBP.ticketing_backend.domain.host.entity.Host;
import com.DBP.ticketing_backend.domain.host.enums.HostStatus;
import com.DBP.ticketing_backend.domain.host.repository.HostRepository;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.domain.users.enums.UsersRole;
import com.DBP.ticketing_backend.domain.users.repository.UsersRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final HostRepository hostRepository;
    private final PasswordEncoder passwordEncoder;

    // User 회원가입
    public void saveUser(SignUpUserRequestDto signUpUserRequestDto) {
        // 이메일 중복 체크
        if (usersRepository.findByEmail(signUpUserRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Users users =
                Users.builder()
                        .email(signUpUserRequestDto.getEmail())
                        .password(passwordEncoder.encode(signUpUserRequestDto.getPassword()))
                        .name(signUpUserRequestDto.getName())
                        .phoneNumber(signUpUserRequestDto.getPhoneNumber())
                        .role(UsersRole.USER)
                        .build();

        usersRepository.save(users);
    }

    // Host 회원가입
    public void saveHost(SignUpHostRequestDto signUpHostRequestDto) {
        // 이메일 중복 체크
        if (usersRepository.findByEmail(signUpHostRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 사업자번호 중복 체크
        if (hostRepository
                .findByBusinessNumber(signUpHostRequestDto.getBusinessNumber())
                .isPresent()) {
            throw new IllegalArgumentException("이미 등록된 사업자번호입니다.");
        }

        // User 생성
        Users users =
                Users.builder()
                        .email(signUpHostRequestDto.getEmail())
                        .password(passwordEncoder.encode(signUpHostRequestDto.getPassword()))
                        .name(signUpHostRequestDto.getName())
                        .phoneNumber(signUpHostRequestDto.getPhoneNumber())
                        .role(UsersRole.HOST)
                        .build();

        usersRepository.save(users);

        // Host 생성
        Host host =
                Host.builder()
                        .users(users)
                        .companyName(signUpHostRequestDto.getCompanyName())
                        .businessNumber(signUpHostRequestDto.getBusinessNumber())
                        .status(HostStatus.PENDING)
                        .build();

        hostRepository.save(host);
    }

    // 로그인
    public LoginResponseDto login(String email, String password) {
        Users user = usersRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return LoginResponseDto.builder()
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole().name())
            .build();
    }

    // 로그인 -> Spring Security UserDetailsService 구현
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> userOptional = usersRepository.findByEmail(username);
        Users findUsers =
                userOptional.orElseThrow(
                        () -> new UsernameNotFoundException("이메일 " + username + " 을 찾을 수 없습니다."));

        return new UsersDetails(findUsers);
    }
}
