# university-academic-gateway

Módulo responsable del registro, descubrimiento y comunicación de los microservicios del sistema académico distribuido. Actúa como **servidor de descubrimiento** centralizado, permitiendo que los demás módulos (Auth, Student, Teacher, University, etc.) se registren y se comuniquen entre sí sin necesidad de conocer sus ubicaciones físicas o puertos específicos.

---

## Descripción del repositorio

Este repositorio contiene la implementación del **Eureka Server**, que forma parte de la arquitectura distribuida del sistema de notas universitarias.

Responsabilidades principales:

* Servir como **registro de servicios (Service Registry)** para los demás microservicios.
* Permitir el **descubrimiento dinámico** de instancias de servicios activos.
* Mantener actualizada la información de disponibilidad mediante *heartbeats*.
* Facilitar la **escalabilidad horizontal**, al permitir múltiples instancias registradas bajo un mismo servicio.
* Integrarse con los módulos `Student`, `Teacher`, `University` y `Auth`.

Tecnologías principales:

* Java 21  
* Spring Boot 3.5.5  
* Spring Cloud Netflix Eureka Server  
* Build: Maven  
* Tests: JUnit 5  
* Contenerización prevista (Docker)

---

## Estructura del repositorio

```
/
├─ src/
│  ├─ main/
│  │  ├─ java/         → código fuente (configuración Eureka)
│  │  └─ resources/    → application.properties o application.yml
│  └─ test/            → pruebas unitarias
├─ pom.xml
├─ Dockerfile
├─ .gitignore
└─ README.md
```

---

## Políticas de rama y flujo de trabajo

Ramas principales en el proyecto:

* **main**
  Contiene únicamente código listo para producción. Al inicio, tendrá solo el proyecto base de Spring Initializr.
  No se permite commit directo; todo se integra vía PR desde ramas `release/*` o `hotfix/*`.

* **release/** (ej. `release.s.2025.09`)
  Ramas de estabilización previas a producción. Se usan para pruebas y preparación antes de hacer merge a `main`.

* **qa**
  Rama utilizada como fuente para despliegues en el entorno de QA. Se actualiza desde `develop`.

* **develop**
  Rama de integración diaria. Aquí se mergean PRs desde `feature/*` y `bugfix/*`.

Flujo típico:
`feature/*` → PR → `develop` → merge a `qa` → validación → `release/*` → `main`.

---

## Configuración básica de Eureka

Archivo `application.yml` (ejemplo para entorno local):

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
  instance:
    hostname: localhost
```

Para cada microservicio cliente (por ejemplo `student-service`):

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## Ejecución local

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/<ORG>/eureka-server.git
   ```
2. Compilar el proyecto:
   ```bash
   mvn clean install
   ```
3. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
4. Acceder a la consola de Eureka:
   ```
   http://localhost:8761
   ```

---

## Dockerización (concepto)

Ejemplo de `Dockerfile`:

```dockerfile
FROM openjdk:21-jdk
WORKDIR /app
COPY target/eureka-server.jar eureka-server.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/app/eureka-server.jar"]
```

Para construir y ejecutar:

```bash
docker build -t eureka-server .
docker run -p 8761:8761 eureka-server
```

---

## Monitoreo y despliegue

El servicio expone un *health endpoint* en `/actuator/health` y puede integrarse con herramientas de monitoreo (Prometheus, Grafana, etc.) para validar disponibilidad.

---

## Enlaces relevantes

* Base repo [university-academic-tracker](https://github.com/IAndresPH/university-academic-tracker.git)
* Documentación de Spring Cloud Netflix Eureka: [https://spring.io/projects/spring-cloud-netflix](https://spring.io/projects/spring-cloud-netflix)

---

## Colaboración y convención de commits

* Convencional commits: `feat(student): add student summary endpoint`
* PRs deben:

  * Referenciar HU o issue en Jira.
  * Incluir descripción clara.
  * Ser revisadas antes de merge.