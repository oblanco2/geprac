# GEPRAC

**Software para la Gestión de Prácticas Académicas**

Universidad de Investigación y Desarrollo · Ingeniería de Sistemas · Proyecto Integrador de quinto semestre · Periodo II-2026

En los programas de licenciatura de la UDI, cada estudiante realiza una práctica pedagógica por semestre, de primero a octavo, y para cada una diligencia los mismos cuatro formatos institucionales. GEPRAC elimina esa repetición: el estudiante se registra una sola vez, selecciona la práctica que va a cursar y el software genera sus formatos ya diligenciados, con los datos de su registro y el contenido de esa práctica tomado del catálogo. El historial de las ocho prácticas queda disponible en cualquier momento.

## Equipo

| Integrante | Frente de trabajo |
|---|---|
| Oscar Iván Blanco Díaz | Arquitectura general y microservicio de Identidad y Perfil Académico |
| Darien Asdrwal Pesca Ojeda | Modelo de datos y microservicio de Legalización de Prácticas |
| José Fernando Rincón Barrios | Cliente web, integración de la autenticación y despliegue |

## Arquitectura

Dos microservicios con base de datos independiente, consumidos por un cliente web único. La identidad y el perfil del estudiante son permanentes y cambian rara vez; la legalización es transaccional y se repite cada periodo. Por eso cada conjunto vive en su propio servicio y en su propia base.

| Componente | Responsabilidad | Tecnología | Despliegue |
|---|---|---|---|
| Cliente web | Interfaz de los tres actores | React 19 · Vite 8 · Bootstrap 5.3 | Vercel |
| MS-01 Identidad y Perfil Académico | Usuarios y sus roles, programas académicos, estudiantes y hoja de vida | Java 21 · Spring Boot 4.1.1 · Spring Data JPA · Flyway | Render (Docker) |
| MS-02 Legalización de Prácticas | Catálogo de prácticas, instituciones, periodos, inscripciones, formatos generados y revisiones | Java 21 · Spring Boot 4.1.1 · Spring Data JPA · Flyway | Render (Docker) |
| Bases de datos | Esquema `identidad` (6 tablas) para MS-01 y esquema `legalizacion` (18 tablas) para MS-02, en proyectos separados | PostgreSQL 17 | Supabase |
| Autenticación | Proveedor de identidad único para todo el software | Supabase Auth · JWT firmado con ES256 | Supabase |

- El cliente obtiene el token en Supabase Auth y lo envía en cada petición (`Authorization: Bearer`). Cada microservicio lo valida por su cuenta contra las llaves públicas del proveedor, sin consultar al otro.
- Los dos servicios se comunican en dos puntos, siempre de MS-02 hacia MS-01: al iniciar la inscripción, MS-02 le solicita el expediente del estudiante (sus datos personales y su hoja de vida), y al designar el tutor de una práctica, le consulta el rol del usuario designado. Las pantallas que reúnen datos de los dos servicios las compone el cliente web.
- MS-02 no declara claves foráneas hacia MS-01: guarda como valores los identificadores que cruzan la frontera.

## Estructura del repositorio

```
backend/
  geprac-academico/      MS-01 Identidad y Perfil Académico
  geprac-legalizacion/   MS-02 Legalización de Prácticas
frontend/
  geprac-web/            cliente web
infra/
  db/                    scripts de la base de datos
docs/                    documentos de las entregas
```

## Actores y casos de uso

| Caso de uso | Actor |
|---|---|
| CU-01 Mantener perfil del estudiante | Estudiante Practicante |
| CU-02 Gestionar el catálogo de prácticas | Director del Programa |
| CU-03 Gestionar instituciones receptoras | Director del Programa |
| CU-04 Preparar el semestre | Director del Programa |
| CU-05 Inscribir la práctica del periodo | Estudiante Practicante |
| CU-06 Revisar la inscripción | Tutor Académico |
| CU-07 Avalar la inscripción | Director del Programa |
| CU-08 Emitir los formatos institucionales | Estudiante Practicante |
| CU-09 Consultar el historial de prácticas | Los tres actores, cada uno con su alcance |

## Puesta en marcha local

Requisitos: JDK 21, Node.js 20.19 o 22.12 en adelante, y Git. Maven no hace falta instalarlo: cada microservicio trae su Maven Wrapper (`mvnw`).

### MS-01 · `backend/geprac-academico`

1. Crear el archivo `src/main/resources/application-local.yml` con la contraseña de la base. Git lo ignora, así que nunca se sube al repositorio:

   ```yaml
   spring:
     datasource:
       password: escriba-aqui-la-contraseña
   ```

2. Arrancar el servicio con el perfil `local`:
   - **NetBeans:** *Run Project*. El archivo `nbactions.xml` ya activa el perfil.
   - **Consola en Windows:** `mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`
   - **Consola en Linux o macOS:** `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`

3. El servicio queda en `http://localhost:8080/api`. Su estado se consulta sin token en `http://localhost:8080/api/actuator/health`; las demás rutas exigen el token de Supabase Auth.

### MS-02 · `backend/geprac-legalizacion`

1. Crear el archivo `src/main/resources/application-local.yml` con la conexión a la base de MS-02. A diferencia de MS-01, aquí van los tres datos, porque MS-02 no trae ninguno en `application.yml`. Git también ignora este archivo:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres
       username: postgres.zuwyslwrrbzuwvsfcdna
       password: escriba-aqui-la-contraseña
   ```

2. Arrancar el servicio con el perfil `local`, igual que MS-01: *Run Project* en NetBeans, o por consola desde `backend/geprac-legalizacion`.

3. El servicio queda en `http://localhost:8081/api`, para que pueda correr al mismo tiempo que MS-01. Su estado se consulta en `http://localhost:8081/api/actuator/health`.

### Cliente web · `frontend/geprac-web`

1. Copiar `.env.example` como `.env` y completar sus cuatro variables. Git también ignora este archivo.

   | Variable | Valor |
   |---|---|
   | `VITE_SUPABASE_URL` | URL del proyecto de Supabase |
   | `VITE_SUPABASE_ANON_KEY` | Llave pública (*anon*) del proyecto |
   | `VITE_API_ACADEMICO` | URL base de MS-01: `http://localhost:8080/api` en local |
   | `VITE_API_LEGALIZACION` | URL base de MS-02: `http://localhost:8081/api` en local |

2. Instalar las dependencias y arrancar:

   ```
   npm install
   npm run dev
   ```

3. El cliente queda en `http://localhost:5173`, origen que la configuración CORS de los dos microservicios ya admite.

## Variables de entorno en producción

Ninguna credencial vive en este repositorio: todas viajan como variables de entorno.

| Dónde | Variable | Contenido |
|---|---|---|
| Render · MS-01 | `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de MS-01 |
| Render · MS-01 | `SPRING_DATASOURCE_URL` | Cadena JDBC del *pooler* de Supabase, sin credenciales; reemplaza la de `application.yml` |
| Render · MS-01 | `PORT` | La asigna Render |
| Render · MS-02 | `SPRING_DATASOURCE_URL` | Cadena JDBC del *Session pooler* del proyecto de Supabase de MS-02, sin credenciales |
| Render · MS-02 | `SPRING_DATASOURCE_USERNAME` | Usuario del pooler: `postgres.` seguido del código del proyecto |
| Render · MS-02 | `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de MS-02 |
| Render · MS-02 | `PORT` | La asigna Render |
| Vercel · cliente | `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY`, `VITE_API_ACADEMICO`, `VITE_API_LEGALIZACION` | Las mismas del `.env` local, con las direcciones públicas de MS-01 y MS-02 |

## Despliegue

- **MS-01:** Render construye la imagen con el `Dockerfile` de dos etapas de `backend/geprac-academico` —Maven compila el proyecto y una imagen mínima de Java 21 ejecuta el binario con un usuario sin privilegios— y la despliega con cada commit en la rama `develop`.
- **MS-02:** igual que MS-01, con el `Dockerfile` de `backend/geprac-legalizacion`. Un filtro de construcción hace que solo se vuelva a desplegar cuando un commit en `develop` cambia esa carpeta.
- **Cliente web:** Vercel.
- **Token de acceso:** la migración V4 de MS-01 crea la función `identidad.claims_del_token`, que añade al token el rol, el programa y el identificador del estudiante; con eso cada microservicio autoriza sin consultar al otro. Se activa una sola vez en Supabase, en el proyecto de MS-01: *Authentication → Hooks → Customize Access Token (JWT) Claims*, tipo Postgres, esquema `identidad`, función `claims_del_token`.
- El plan gratuito de Render suspende el servicio tras un rato sin uso. La primera petición puede tardar unos 50 segundos mientras despierta, y el cliente la espera hasta 60.

| Componente | Dirección |
|---|---|
| Cliente web | https://geprac-geprac.vercel.app |
| MS-01 | https://geprac-academico.onrender.com/api |
| MS-02 | https://geprac-legalizacion.onrender.com/api |

## Estado del desarrollo

Al 5 de octubre de 2026:

- **MS-01:** desplegado en Render. Flyway montó el esquema `identidad` del documento, con sus 6 tablas y los programas LEI y LLC. Valida el token de Supabase Auth y expone el estado del servicio y una consulta de prueba (`GET /api/programas`).
- **MS-02:** desplegado en Render, con su propio proyecto de Supabase. Flyway montó el esquema `legalizacion` del documento, con sus 18 tablas. Por ahora expone solo el estado del servicio.
- **Cliente web:** inicio de sesión con Supabase Auth y llamada de prueba a MS-01.

## Entregas

| Avance | Fecha |
|---|---|
| Primer avance · Propuesta | 30 de agosto de 2026 |
| Segundo avance · Prototipo funcional | 11 de octubre de 2026 |
| Entrega final · Aplicación | 15 de noviembre de 2026 |
