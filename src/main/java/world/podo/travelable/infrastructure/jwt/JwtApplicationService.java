package world.podo.travelable.infrastructure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import world.podo.travelable.application.TokenApplicationService;

import java.util.Optional;

@Slf4j
@Service
public class JwtApplicationService implements TokenApplicationService<Long> {
    private static final String CLAIM_NAME_MEMBER_ID = "memberId";

    private final String tokenIssuer;
    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    public JwtApplicationService(@Value("${jwt.token-issuer}") String tokenIssuer,
                                 @Value("${jwt.token-signing-key}") String tokenSigningKey) {
        this.tokenIssuer = tokenIssuer;
        this.algorithm = Algorithm.HMAC256(tokenSigningKey);
        this.jwtVerifier = JWT.require(algorithm).build();
    }

    @Override
    public String encode(Long memberId) {
        Assert.notNull(memberId, "'memberId' must not be null");

        return JWT.create()
                .withIssuer(tokenIssuer)
                .withClaim(CLAIM_NAME_MEMBER_ID, memberId)
                .sign(algorithm);
    }

    @Override
    public Optional<Long> decode(String token) {
        try {
            return Optional.ofNullable(jwtVerifier.verify(token))
                    .map(Payload::getClaims)
                    .map(claims -> claims.get(CLAIM_NAME_MEMBER_ID))
                    .map(Claim::asLong);
        } catch (JWTVerificationException ex) {
            log.warn("Invalid access Token. token:" + token, ex);
            return Optional.empty();
        }
    }
}
