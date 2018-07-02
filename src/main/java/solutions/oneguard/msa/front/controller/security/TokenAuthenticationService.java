/*
 * This file is part of the OneGuard Micro-Service Architecture Front Controller service.
 *
 * (c) OneGuard <contact@oneguard.email>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package solutions.oneguard.msa.front.controller.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;

@Component
public class TokenAuthenticationService {
    private final long expirationTime;
    private final String secret;
    private final String tokenType;
    private final String headerName;

    public TokenAuthenticationService(
        @Value("${oneguard.msa.front-controller.jwt.expriation-time}") long expirationTime,
        @Value("${oneguard.msa.front-controller.jwt.secret}") String secret,
        @Value("${oneguard.msa.front-controller.jwt.token-type}") String tokenType,
        @Value("${oneguard.msa.front-controller.jwt.header-name}") String headerName
    ) {
        this.expirationTime = expirationTime;
        this.secret = secret;
        this.tokenType = tokenType;
        this.headerName = headerName;
    }

    public void addAuthentication(HttpServletResponse response, String username) {
        String jwt = Jwts.builder()
            .setSubject(username)
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 1000))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
        response.addHeader(headerName, tokenType + " " + jwt);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return null;
        }

        // parse the token.
        String user = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
        if (user == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(headerName);
        String headerPrefix = tokenType + ' ';
        if (token != null && token.startsWith(headerPrefix)) {
            return token.substring((headerPrefix).length());
        }

        token = request.getParameter("access_token");
        if (token != null) {
            return token;
        }

        return null;
    }
}
