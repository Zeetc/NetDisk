package com.catchiz.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    private static final String SECRET = "6A50A18D70FA63636645C65459F1D78A";
    private static final long EXPIRE = 30 * 60 * 1000;//有效时间，30分钟

    public static String generate(Map<String, Object> claims) {
        Date nowDate = new Date();
        JwtBuilder builder = Jwts.builder();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            builder.claim(entry.getKey(),entry.getValue());
        }
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + EXPIRE);
        return builder
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public static Claims getClaim(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    private String refreshToken(Claims claim) {
        Date nowDate = new Date();
        Date expiration = claim.getExpiration();
        //如果还有有效时间的话，刷新token
        if ((expiration.getTime() - nowDate.getTime()) >= 0) {
            //要刷新token
            Object user = claim.get("user");
            Map<String, Object> map = new HashMap<>();
            map.put("user", user);
            return JwtUtils.generate(map);
        }
        return null;
    }

}