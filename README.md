# GEPRAC

**Software para la Gestión de Prácticas Académicas**

Universidad de Investigación y Desarrollo · Ingeniería de Sistemas · Periodo II-2026
Proyecto Integrador de quinto semestre

## Equipo

| Integrante | Frente |
|---|---|
| Oscar Iván Blanco Díaz | Arquitectura y microservicio de Gestión Académica |
| Darien Asdrwal Pesca Ojeda | Datos y microservicio de Seguimiento y Evaluación |
| José Fernando Rincón Barrios | Cliente, integración de identidad y despliegue |

## Arquitectura

Dos microservicios independientes, cada uno con su propia base de datos.

| Componente | Tecnología | Despliegue |
|---|---|---|
| Cliente web | React 18 · Vite · Bootstrap 5 | Vercel |
| Microservicio 1 · Gestión Académica | Java 17 · Spring Boot 3 | Oracle Cloud |
| Microservicio 2 · Seguimiento y Evaluación | Java 17 · Spring Boot 3 | Oracle Cloud |
| Identidad | Keycloak 26 (OIDC) | Oracle Cloud |
| Datos | Oracle Autonomous Database x2 | Oracle Cloud Free Tier |

## Estructura

    backend/geprac-academico/     CU-01 a CU-06
    backend/geprac-seguimiento/   CU-07 a CU-13
    frontend/geprac-web/          interfaz de los seis roles
    infra/                        Keycloak y scripts de base de datos
    docs/                         documentos de las tres entregas

## Roles del software

Director del Programa · Coordinador de Práctica · Docente Asesor
Estudiante Practicante · Institución Receptora · Superusuario

## Puesta en marcha

Cada componente tiene su propio README con los pasos de instalación.
Ninguna credencial vive en este repositorio: todas viajan como variables de entorno.

## Entregas

| Avance | Fecha |
|---|---|
| Primer avance · Propuesta | 30 de agosto de 2026 |
| Segundo avance · Prototipo funcional | 11 de octubre de 2026 |
| Entrega final · Aplicación | 15 de noviembre de 2026 |