package com.buzzanalysis.application.auth;

import com.buzzanalysis.application.auth.dto.AuthResult;
import com.buzzanalysis.application.auth.dto.LoginCommand;
import com.buzzanalysis.application.auth.dto.RegisterCommand;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 認証（登録/ログイン/トークンリフレッシュ）に関するアプリケーションサービス。
 * ユースケースの調整のみを行い、実際のパスワードハッシュ化やJWT発行はポート経由でinfrastructure層に委譲する。
 */
@Service
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProvider tokenProvider;

    public AuthApplicationService(UserRepository userRepository, PasswordEncoderPort passwordEncoderPort,
                                   TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResult register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessRuleViolationException("Email is already registered: " + command.email());
        }
        String hashed = passwordEncoderPort.encode(command.rawPassword());
        User user = User.createNew(command.email(), hashed, command.displayName());
        User saved = userRepository.save(user);
        return issueTokens(saved);
    }

    @Transactional(readOnly = true)
    public AuthResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid email or password"));
        if (!passwordEncoderPort.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new BusinessRuleViolationException("Invalid email or password");
        }
        return issueTokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResult refresh(String refreshToken) {
        UUID userId = tokenProvider.validateRefreshTokenAndGetUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> EntityNotFoundException.of("User", userId));
        return issueTokens(user);
    }

    private AuthResult issueTokens(User user) {
        TokenProvider.IssuedToken access = tokenProvider.generateAccessToken(user);
        TokenProvider.IssuedToken refresh = tokenProvider.generateRefreshToken(user);
        return new AuthResult(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                access.token(),
                access.expiresInSeconds(),
                refresh.token(),
                refresh.expiresInSeconds()
        );
    }
}
