package com.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class CorsGlobalConfigTest {

    private final CorsGlobalConfig corsGlobalConfig = new CorsGlobalConfig();

    @Test
    void corsWebFilterShouldAllowConfiguredOrigin() {
        // Arrange
        CorsWebFilter corsWebFilter = corsGlobalConfig.corsWebFilter();

        MockServerHttpRequest request = MockServerHttpRequest.options("http://localhost:8080/api/test")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = webExchange -> Mono.empty();

        // Act
        corsWebFilter.filter(exchange, chain).block();

        // Assert
        var headers = exchange.getResponse().getHeaders();

        // Se debe permitir exactamente el origen configurado
        assertThat(headers.getAccessControlAllowOrigin())
                .isEqualTo("http://localhost:5173");

        // Y debe respetar que allowCredentials = true en tu configuración
        assertThat(headers.getAccessControlAllowCredentials())
                .isTrue();
    }

    @Test
    void corsWebFilterShouldNotAllowDifferentOrigin() {
        // Arrange
        CorsWebFilter corsWebFilter = corsGlobalConfig.corsWebFilter();

        MockServerHttpRequest request = MockServerHttpRequest.options("http://localhost:8080/api/test")
                .header("Origin", "http://malicious.com")
                .header("Access-Control-Request-Method", "GET")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = webExchange -> Mono.empty();

        // Act
        corsWebFilter.filter(exchange, chain).block();

        // Assert
        var headers = exchange.getResponse().getHeaders();

        // Lo importante: para orígenes no permitidos NO debe devolver ese origen como permitido
        assertThat(headers.getAccessControlAllowOrigin())
                .isNull();
        // No comprobamos allowCredentials aquí para no depender del detalle interno (null vs false)
    }
}
