# ElBuenSabor
Trabajo final de TUP

El "Modo Dev" que hemos configurado te permite desarrollar y probar tus endpoints de API de forma más ágil sin tener que obtener y usar constantemente tokens JWT de Auth0, especialmente cuando estás trabajando en la lógica de negocio interna de los controladores y servicios.

**Cómo Funciona:**

1.  **Perfiles de Spring Boot:**
    * Spring Boot permite definir diferentes "perfiles". Un perfil es un conjunto de configuraciones que se pueden activar o desactivar según el entorno (desarrollo, pruebas, producción, etc.).
    * Tú has creado dos configuraciones de seguridad principales:
        * **`SecurityConfig.java`**: Esta es tu configuración de seguridad "real" o "de producción". Incluye toda la lógica para validar tokens JWT de Auth0, extraer roles y aplicar las reglas de autorización específicas que hemos estado definiendo. Está anotada con `@Profile("!dev")`. Esto significa que esta configuración se activará cuando el perfil "dev" **NO** esté activo. Si no se especifica ningún perfil activo, Spring Boot usa un perfil "default", y como "default" no es "dev", `SecurityConfig` se activa.
        * **`DevSecurityConfig.java`**: Esta es tu configuración de seguridad para "desarrollo". Está anotada con `@Profile("dev")`. Solo se activa cuando el perfil "dev" está explícitamente activo. Esta clase contiene una configuración muy permisiva:
            ```java
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests.anyRequest().permitAll() // Permite TODO en dev
            )
            ```
            Esto significa que, cuando el perfil "dev" está activo, todas las peticiones a cualquier endpoint son permitidas sin necesidad de autenticación ni token.

2.  **Activación del Perfil "dev":**
    Hay varias formas de activar un perfil en Spring Boot:
    * **En `application.properties`:** Puedes añadir la línea `spring.profiles.active=dev`. Sin embargo, esto lo activaría siempre por defecto. Para cambiar, tendrías que modificar el archivo. (Tú comentaste esta línea, lo cual es correcto para probar Auth0).
    * **Como Argumento JVM:** Al ejecutar tu aplicación desde la línea de comandos o configurando tu IDE, puedes pasar `-Dspring.profiles.active=dev`.
    * **En la Configuración de Ejecución del IDE (Recomendado para cambiar fácilmente):** En IntelliJ IDEA (o similar), puedes editar tu configuración de ejecución para la aplicación Spring Boot y añadir `dev` al campo "Active profiles". Si dejas este campo vacío en el IDE, y no hay `spring.profiles.active` en `application.properties`, se usará el perfil "default" (y por lo tanto `SecurityConfig` con `@Profile("!dev")` se activará).

**Propósito del Modo Dev:**
* **Agilidad:** Cuando estás desarrollando un nuevo endpoint y solo quieres probar su lógica interna (si recibe bien los datos, si el servicio funciona, si la respuesta es correcta), no quieres preocuparte por obtener un token válido para cada prueba en Postman. Con el perfil "dev" activo, puedes llamar directamente a tus endpoints.
* **Pruebas Unitarias/Integración Aisladas:** A veces, para ciertos tests automatizados, quieres probar la lógica de negocio sin la capa de seguridad.
* **Facilidad para el Frontend:** Si el frontend está trabajando en una nueva funcionalidad que consume un endpoint que aún no está completamente definido o que el backend quiere probar sin seguridad, el modo `dev` lo facilita.

**Importante:**
* **NUNCA** uses el perfil `dev` (con `anyRequest().permitAll()`) en un entorno de producción o staging. La seguridad estaría completamente desactivada.
* Cuando quieras probar la integración real con Auth0, los roles, y las reglas de autorización específicas, **DEBES** ejecutar la aplicación sin el perfil `dev` activo (para que se cargue tu `SecurityConfig.java` principal).

