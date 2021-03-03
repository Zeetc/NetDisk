package com.catchiz.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt
 */
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 密钥
     */
    private static final String secret = "NoMatterWhichWorldLineWhichTimeWhichPlaceID9D973DE3899A8F7";

    /**
     * 从数据声明生成令牌
     * @param claims 数据声明
     * @return 令牌
     */
    private static String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + 3600L * 1000);
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 生成令牌
     * @param username 用户
     * @return 令牌
     */
    public static String generateToken(String username,boolean isManager) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("sub", username);
        claims.put("created", new Date());
        claims.put("isManager",isManager);
        String token = generateToken(claims);
        System.out.println("token is " + token);
        return token;
    }

    /**
     * 从令牌中获取用户名
     * @param token 令牌
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if(claims==null)return null;
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断令牌是否过期
     * @param token 令牌
     * @return 是否过期
     */
    public static Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if(claims==null)return false;
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新令牌
     * @param token 原令牌
     * @return 新令牌
     */
    public static String refreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if(claims==null)return null;
            claims.put("created", new Date());
            return generateToken(claims);
        } catch (Exception e) {
            return null;
        }
    }

}