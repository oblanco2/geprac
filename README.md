# GEPRAC

**Software para la Gestión de Prácticas Académicas**

Universidad de Investigación y Desarrollo · Ingeniería de Sistemas · Proyecto Integrador de quinto semestre · Periodo II-2026 · Versión 1.0.0

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

1. Crear el archivo `src/main/resources/application-local.yml` con la contraseña de la base de MS-02, como en MS-01; Git también lo ignora. MS-02 le pide a MS-01 el expediente del estudiante y el rol del tutor: sin la dirección local se los pide al MS-01 de Render, y con ella, al que corre en el mismo equipo:

   ```yaml
   spring:
     datasource:
       password: escriba-aqui-la-contraseña
   geprac:
     identidad:
       url: http://localhost:8080/api
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
| Render · MS-01 | `TZ` | `America/Bogota`: las fechas que registra el servicio quedan con la hora de Colombia |
| Render · MS-01 | `PORT` | La asigna Render |
| Render · MS-02 | `SPRING_DATASOURCE_URL` | Cadena JDBC del *Session pooler* del proyecto de Supabase de MS-02, sin credenciales; reemplaza la de `application.yml` |
| Render · MS-02 | `SPRING_DATASOURCE_USERNAME` | Usuario del pooler: `postgres.` seguido del código del proyecto; reemplaza el de `application.yml` |
| Render · MS-02 | `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de MS-02 |
| Render · MS-02 | `TZ` | `America/Bogota`: las fechas de las inscripciones, las revisiones, los avales y los formatos quedan con la hora de Colombia |
| Render · MS-02 | `IDENTIDAD_URL` | Opcional: la dirección de MS-01; sin ella, MS-02 usa la de Render |
| Render · MS-02 | `PORT` | La asigna Render |
| Vercel · cliente | `VITE_SUPABASE_URL`, `VITE_SUPABASE_ANON_KEY`, `VITE_API_ACADEMICO`, `VITE_API_LEGALIZACION` | Las mismas del `.env` local, con las direcciones públicas de MS-01 y MS-02 |

## Despliegue

- **MS-01:** Render construye la imagen con el `Dockerfile` de dos etapas de `backend/geprac-academico` —Maven compila el proyecto y una imagen mínima de Java 21 ejecuta el binario con un usuario sin privilegios— y la despliega con cada commit en la rama `develop`.
- **MS-02:** igual que MS-01, con el `Dockerfile` de `backend/geprac-legalizacion`. Un filtro de construcción hace que solo se vuelva a desplegar cuando un commit en `develop` cambia esa carpeta.
- **Cliente web:** Vercel, también desde la rama `develop`.
- **Ramas:** `develop` integra los cambios de los tres y es la que despliegan Render y Vercel; `main` guarda la versión entregada, con su etiqueta: `v1.0.0` para la entrega final.
- **Token de acceso:** la migración V4 de MS-01 crea la función `identidad.claims_del_token`, que añade al token el rol, el programa y el identificador del estudiante; con eso cada microservicio autoriza sin consultar al otro. Se activa una sola vez en Supabase, en el proyecto de MS-01: *Authentication → Hooks → Customize Access Token (JWT) Claims*, tipo Postgres, esquema `identidad`, función `claims_del_token`.
- El plan gratuito de Render suspende el servicio tras un rato sin uso. La primera petición puede tardar unos 50 segundos mientras despierta, y el cliente la espera hasta 60.

| Componente | Dirección |
|---|---|
| Cliente web | https://geprac-geprac.vercel.app |
| MS-01 | https://geprac-academico.onrender.com/api |
| MS-02 | https://geprac-legalizacion.onrender.com/api |

## Datos de la demostración

La demostración final recorre los nueve casos de uso con las cuentas de los tres integrantes. Los dos archivos de `infra/db` dejan su punto de partida, en este orden:

1. **`semilla_identidad.sql`**, en el *SQL Editor* del proyecto de Supabase de MS-01. Antes, las tres cuentas deben existir en *Authentication → Users*. Asigna los roles —Oscar, director de LEI; Darien, tutor académico; José, estudiante—, registra al estudiante con su hoja de vida y añade una cuenta ficticia sin rol, la de Tatiana Lizcano Rojas, que no existe en Supabase Auth y solo aparece en la preparación del semestre. Su última columna entrega tres líneas.
2. **`semilla_legalizacion.sql`**, en el *SQL Editor* del proyecto de Supabase de MS-02. En las líneas marcadas se copian las tres que entregó el archivo anterior. Carga los semestres 2025-2, 2026-1 y 2026-2, solo el último abierto, con el tutor de cada práctica, salvo la 8 del 2026-2; las ocho prácticas de LEI; ocho instituciones receptoras, y trece inscripciones en los cinco estados: las de las prácticas 1 y 2 de José, avaladas, y once de estudiantes ficticios, sin cuenta, que solo aparecen en las bandejas y en el historial. La de la práctica 3 la crea José en la demostración.

Los dos archivos se pueden ejecutar las veces que haga falta sin duplicar nada, y ninguno lleva contraseñas. Para repetir la demostración desde el principio, se ejecutan otra vez, en el mismo orden, con `volver_a_empezar` en `true`: borran lo que la demostración creó y devuelven a su punto de partida el perfil de José, el catálogo, los tutores y el semestre.

| Cuenta | Rol | Lo que encuentra al ingresar |
|---|---|---|
| Oscar Iván Blanco Díaz | Director del Programa de LEI | Tres inscripciones por avalar (P-17); el semestre 2026-2 abierto, la cuenta de Tatiana sin rol y la práctica 8 sin tutor (P-16); las ocho prácticas del catálogo (P-12), las instituciones (P-14) y el historial del programa (P-19) |
| Darien Asdrwal Pesca Ojeda | Tutor académico | Cuatro inscripciones por revisar (P-10) y el historial de sus prácticas (P-19) |
| José Fernando Rincón Barrios | Estudiante Practicante | Su perfil completo (P-03 y P-04) y sus ocho prácticas (P-02): la 1 y la 2 avaladas, cuyos formatos puede emitir (P-09), y la 3 por inscribir (P-05 a P-08) |

El recorrido: Oscar registra una institución (CU-03), añade una actividad a la práctica 3 (CU-02), le asigna a Tatiana el rol de tutora y le designa la práctica 8 (CU-04); José añade una referencia a su hoja de vida (CU-01) e inscribe la práctica 3 en la institución nueva (CU-05); Darien la devuelve, José la corrige y la reenvía, y Darien la aprueba (CU-06); Oscar la avala (CU-07); José emite y descarga sus cuatro formatos (CU-08), y Oscar y Darien la encuentran en sus historiales (CU-09).

## Estado del desarrollo

La versión 1.0.0, de la entrega final, opera de punta a punta los nueve casos de uso:

| Caso de uso | Pantallas | Rutas |
|---|---|---|
| CU-01 Mantener perfil del estudiante | P-03, P-04 | MS-01: `GET` y `PUT /estudiantes/me`, `GET` y `PUT /estudiantes/me/hoja-vida`, `POST /estudiantes/me/{bloque}`, `PUT` y `DELETE /estudiantes/me/{bloque}/{id}`, con `formaciones`, `experiencias` o `referencias` como bloque |
| CU-02 Gestionar el catálogo de prácticas | P-12, P-13 | MS-02: `GET` y `POST /practicas`, `PUT` y `DELETE /practicas/{id}` |
| CU-03 Gestionar instituciones receptoras | P-14, P-15 | MS-02: `GET` y `POST /instituciones`, `PUT` y `DELETE /instituciones/{id}` |
| CU-04 Preparar el semestre | P-16 | MS-01: `GET /usuarios`, `PUT /usuarios/{id}/rol`. MS-02: `GET /semestres/preparacion`, `POST /semestres`, `POST /semestres/{id}/cierre`, `POST` y `PUT /semestres/abierto/practicas/{practicaId}/tutor` |
| CU-05 Inscribir la práctica del periodo | P-05 a P-08 | MS-02: `POST /inscripciones`, `PUT /inscripciones/{id}`, `POST /inscripciones/{id}/envio`, `GET /inscripciones/actual` |
| CU-06 Revisar la inscripción | P-10, P-11 | MS-02: `GET /revisiones`, `GET /revisiones/pendientes`, `POST /revisiones/{id}` |
| CU-07 Avalar la inscripción | P-17, P-18 | MS-02: `GET /avales/pendientes`, `POST /avales/{id}` |
| CU-08 Emitir los formatos institucionales | P-09 | MS-02: `GET` y `POST /formatos?inscripcion={id}`, `GET /formatos/{id}/archivo` |
| CU-09 Consultar el historial de prácticas | P-02, P-19 | MS-02: `GET /historial` |

- **MS-01:** además, `GET /usuarios/me`, la cuenta de quien ingresa, que se registra en su primer ingreso; `GET /programas`, y `GET /usuarios/{id}/rol`, que consulta MS-02. Cinco migraciones; la V4 añade al token el rol, el programa y el estudiante.
- **MS-02:** cada ruta se abre solo al rol que le corresponde según el token. Al iniciar la inscripción le pide a MS-01 el expediente del estudiante, y al designar un tutor, su rol, con el token de quien hace la petición. Tres migraciones; la V2 pone en vigencia las plantillas de los cuatro formatos, que se emiten en PDF.
- **Cliente web:** las diecinueve pantallas del prototipo de alta fidelidad, con el ingreso (P-01) y el menú de cada rol.
- **Pruebas:** `mvnw test` en cada microservicio, sin base de datos —59 pruebas en MS-01 y 143 en MS-02—, y `npm run lint` y `npm run build` en el cliente.

## Entregas

| Avance | Fecha |
|---|---|
| Primer avance · Propuesta | 30 de agosto de 2026 |
| Segundo avance · Prototipo funcional | 11 de octubre de 2026 |
| Entrega final · Aplicación · versión 1.0.0 | 15 de noviembre de 2026 |
