package in.bm.netflix_gateway_service.Service;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Component
public class TokenValidation {

    private static final String PublicKey=
            System.getenv("JWT_PUBLIC_KEY");

    private final PublicKey publicKey;

    public TokenValidation() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public boolean validateToken(String token){
      try{
          Jwts
                  .parser()
                  .verifyWith(publicKey)
                  .build()
                  .parseSignedClaims(token);
          return true;
      }catch (Exception e){
          return false;
      }
    }

    public String extractUserId(String token){
        return Jwts
                .parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

}
