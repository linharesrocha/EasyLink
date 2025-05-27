package br.com.easylink.apigateway.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${api.security.token.secret}")
    private String secret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Pular validação para os endpoints de autenticação
            if (path.matches("/api/v1/auth/.*") || !path.startsWith("/api/v1/")) {
                return chain.filter(exchange);
            }

            // 1. Pega o cabeçalho de autorização
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 2. Valida o cabeçalho
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Cabeçalho de autorização ausente ou inválido", HttpStatus.UNAUTHORIZED);
            }

            // 3. Extrai e valida o token
            String token = authHeader.substring(7);
            DecodedJWT decodedJWT; // Variável para o token decodificado
            try {
                Algorithm algorithm = Algorithm.HMAC256(secret);
                decodedJWT = JWT.require(algorithm) // Decodifica e verifica
                        .withIssuer("EasyLink API")
                        .build()
                        .verify(token);
            } catch (Exception e) {
                return onError(exchange, "Token inválido ou expirado", HttpStatus.FORBIDDEN);
            }

            // Se o token for válido, extraímos o username (subject)
            String username = decodedJWT.getSubject();

            // Adicionando o username como um header na requisição original
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Username", username)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            return chain.filter(mutatedExchange); // Continua com a requisição MUTADA
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Classe de configuração vazia, necessária para o factory
    }
}