package com.DBP.ticketing_backend.domain.auth.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpHostRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpUserRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.LoginResponseDto;
import com.DBP.ticketing_backend.domain.auth.entity.RefreshToken;
import com.DBP.ticketing_backend.domain.auth.entity.TokenBlacklist;
import com.DBP.ticketing_backend.domain.auth.repository.RefreshTokenRepository;
import com.DBP.ticketing_backend.domain.auth.repository.TokenBlacklistRepository;
import com.DBP.ticketing_backend.domain.host.entity.Host;
import com.DBP.ticketing_backend.domain.host.enums.HostStatus;
import com.DBP.ticketing_backend.domain.host.repository.HostRepository;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.domain.users.enums.UsersRole;
import com.DBP.ticketing_backend.domain.users.repository.UsersRepository;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final HostRepository hostRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    /** User 회원가입 */
    public void saveUser(SignUpUserRequestDto signUpUserRequestDto) {
        if (usersRepository.findByEmail(signUpUserRequestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_MEMBER_EMAIL);
        }

        Users users =
                Users.builder()
                        .email(signUpUserRequestDto.getEmail())
                        .password(passwordEncoder.encode(signUpUserRequestDto.getPassword()))
                        .name(signUpUserRequestDto.getName())
                        .phoneNumber(signUpUserRequestDto.getPhoneNumber())
                        .role(UsersRole.ROLE_USER)
                        .build();

        usersRepository.save(users);
    }

    /** Host 회원가입 */
    public void saveHost(SignUpHostRequestDto signUpHostRequestDto) {
        if (usersRepository.findByEmail(signUpHostRequestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_MEMBER_EMAIL);
        }

        if (hostRepository
                .findByBusinessNumber(signUpHostRequestDto.getBusinessNumber())
                .isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        Users users =
                Users.builder()
                        .email(signUpHostRequestDto.getEmail())
                        .password(passwordEncoder.encode(signUpHostRequestDto.getPassword()))
                        .name(signUpHostRequestDto.getName())
                        .phoneNumber(signUpHostRequestDto.getPhoneNumber())
                        .role(UsersRole.ROLE_HOST)
                        .build();

        usersRepository.save(users);

        Host host =
                Host.builder()
                        .users(users)
                        .companyName(signUpHostRequestDto.getCompanyName())
                        .businessNumber(signUpHostRequestDto.getBusinessNumber())
                        .status(HostStatus.PENDING)
                        .build();

        hostRepository.save(host);
    }

    /** 로그인 (Access Token + Refresh Token 발급) */
    public LoginResponseDto login(String email, String password) {
        Users user =
                usersRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () -> new CustomException(ErrorCode.INVALID_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_OR_PASSWORD);
        }

        // HOST인 경우 승인 상태 확인
        if (user.getRole() == UsersRole.ROLE_HOST) {
            Host host =
                    hostRepository
                            .findByUsers(user)
                            .orElseThrow(() -> new CustomException(ErrorCode.HOST_NOT_FOUND));

            if (host.getStatus() == HostStatus.REJECTED) {
                throw new IllegalStateException("승인이 거부된 계정입니다. 고객센터에 문의해주세요.");
            }

            if (host.getStatus() == HostStatus.SUSPENDED) {
                throw new IllegalStateException("정지된 계정입니다. 고객센터에 문의해주세요.");
            }

            if (host.getStatus() == HostStatus.PENDING) {
                throw new IllegalStateException("승인 대기 중입니다. 승인 후 로그인해주세요.");
            }
        }
        // Access Token 생성
        String accessToken =
                jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());

        // Refresh Token 생성
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // 기존 Refresh Token 삭제 후 새로 저장
        refreshTokenRepository.deleteByUser(user);
        RefreshToken newRefreshToken =
                RefreshToken.builder()
                        .user(user)
                        .token(refreshToken)
                        .expiryDate(jwtTokenProvider.getRefreshTokenExpiryDate())
                        .build();
        refreshTokenRepository.save(newRefreshToken);

        return LoginResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /** Access Token 재발급 */
    public String refreshAccessToken(String refreshToken) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // DB에서 Refresh Token 조회
        RefreshToken storedToken =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_NOT_FOUND));

        // 만료 확인
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        // 새로운 Access Token 발급
        Users user = storedToken.getUser();
        return jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());
    }

    /** 로그아웃 (Access Token 블랙리스트 + Refresh Token 삭제) */
    public void logout(String accessToken) {
        // Access Token 블랙리스트 추가
        String email = jwtTokenProvider.getEmailFromToken(accessToken);
        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateFromToken(accessToken);

        TokenBlacklist blacklist =
                TokenBlacklist.builder()
                        .token(accessToken)
                        .email(email)
                        .expiryDate(expiryDate)
                        .build();
        tokenBlacklistRepository.save(blacklist);

        // Refresh Token 삭제
        Users user =
                usersRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }

    /** 토큰이 블랙리스트에 있는지 확인 */
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    /** Spring Security UserDetailsService 구현 */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> userOptional = usersRepository.findByEmail(username);
        Users findUsers =
                userOptional.orElseThrow(
                        () -> new UsernameNotFoundException("이메일 " + username + " 을 찾을 수 없습니다."));

        return new UsersDetails(findUsers);
    }
}
