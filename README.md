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
| Cliente web | Interfaz de los tres actores | React 19 · Vite 8 · React Router 7 | Vercel |
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

## Datos de la demostración

Los cinco casos de uso del prototipo funcional parten de datos que, en el software, crean casos de uso que se programan para la entrega final: el rol de cada cuenta y los semestres con su tutor designado (CU-04), el registro del estudiante (CU-01), el catálogo de prácticas (CU-02) y las inscripciones enviadas (CU-05). Los dos archivos de `infra/db` los cargan, en este orden:

1. **`semilla_identidad.sql`**, en el *SQL Editor* del proyecto de Supabase de MS-01. Antes, las tres cuentas deben existir en *Authentication → Users*. Asigna los roles —Oscar, director de LEI; Darien, tutor académico; José, estudiante— y registra al estudiante con su hoja de vida. Su última columna entrega tres líneas.
2. **`semilla_legalizacion.sql`**, en el *SQL Editor* del proyecto de Supabase de MS-02, ya desplegado con la migración V2. En las líneas marcadas se copian las tres que entregó el archivo anterior. Carga los semestres 2025-2, 2026-1 y 2026-2, solo el último abierto; las ocho prácticas de LEI; ocho instituciones receptoras, y catorce inscripciones en los cinco estados: tres de José y once de estudiantes ficticios, sin cuenta, que solo aparecen en las bandejas y en el historial.

Los dos archivos se pueden ejecutar las veces que haga falta sin duplicar nada, y ninguno lleva contraseñas. Para repetir la demostración desde el principio se ejecuta de nuevo `semilla_legalizacion.sql` con `volver_a_empezar` en `true`.

| Cuenta | Rol | Lo que encuentra al ingresar |
|---|---|---|
| Oscar Iván Blanco Díaz | Director del Programa de LEI | Tres inscripciones por avalar (P-17), el catálogo de instituciones (P-14) y el historial del programa (P-19) |
| Darien Asdrwal Pesca Ojeda | Tutor académico | Cinco inscripciones por revisar (P-10), entre ellas la de José, y el historial de sus prácticas (P-19) |
| José Fernando Rincón Barrios | Estudiante Practicante | Sus ocho prácticas (P-02): la 1 y la 2 avaladas, con sus formatos (P-09), y la 3 en revisión |

## Estado del desarrollo

Al segundo avance, el prototipo funcional opera de punta a punta cinco de los nueve casos de uso:

| Caso de uso | Pantallas | Rutas de MS-02 |
|---|---|---|
| CU-03 Gestionar instituciones receptoras | P-14, P-15 | `GET` y `POST /instituciones`, `PUT` y `DELETE /instituciones/{id}` |
| CU-06 Revisar la inscripción | P-10, P-11 | `GET /revisiones/pendientes`, `POST /revisiones/{id}` |
| CU-07 Avalar la inscripción | P-17, P-18 | `GET /avales/pendientes`, `POST /avales/{id}` |
| CU-08 Emitir los formatos institucionales | P-09 | `GET` y `POST /formatos?inscripcion={id}`, `GET /formatos/{id}/archivo` |
| CU-09 Consultar el historial de prácticas | P-02, P-19 | `GET /historial` |

- **MS-01:** además del estado del servicio y de `GET /programas`, expone `GET /usuarios/me`, la cuenta de quien ingresa, que se registra en su primer ingreso, y `GET /usuarios`, la lista de cuentas con la que la Dirección ve el nombre del tutor que aprobó cada inscripción. La migración V4 añade al token el rol, el programa y el estudiante.
- **MS-02:** las rutas de la tabla, cada una abierta solo al rol que le corresponde según el token. La migración V2 pone en vigencia las plantillas de los cuatro formatos, que se emiten en PDF.
- **Cliente web:** las pantallas de la tabla y el ingreso (P-01), con la hoja de estilos del prototipo de alta fidelidad.
- CU-01, CU-02, CU-04 y CU-05 se programan para la entrega final; en el prototipo, lo que producen lo aportan los datos de la demostración.

## Entregas

| Avance | Fecha |
|---|---|
| Primer avance · Propuesta | 30 de agosto de 2026 |
| Segundo avance · Prototipo funcional | 11 de octubre de 2026 |
| Entrega final · Aplicación | 15 de noviembre de 2026 |
