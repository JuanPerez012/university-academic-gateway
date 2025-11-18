package com.gateway;

import com.gateway.config.CorsGlobalConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GatewayApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context, "El ApplicationContext no debería ser nulo");
    }

    @DisplayName("Todos los beans clave del módulo GATEWAY deben estar presentes en el contexto")
    @ParameterizedTest(name = "Bean presente por tipo: {0}")
    @MethodSource("beanTypes")
    void beansShouldBePresentByType(Class<?> type) {
        Object bean = assertDoesNotThrow(
                () -> context.getBean(type),
                () -> "No se pudo resolver bean de tipo: " + type.getName()
        );

        assertNotNull(bean, "El bean no debería ser nulo");
        assertTrue(
                type.isInstance(bean) || type.isAssignableFrom(bean.getClass()),
                "El bean obtenido no es del tipo esperado (puede ser un proxy)"
        );
    }

    static Stream<Class<?>> beanTypes() {
        return Stream.of(
                // Aplicación principal
                GatewayApplication.class,

                // Configuración CORS
                CorsGlobalConfig.class,
                CorsWebFilter.class
        );
    }
}
