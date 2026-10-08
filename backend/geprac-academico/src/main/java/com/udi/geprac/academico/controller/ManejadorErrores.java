package com.udi.geprac.academico.controller;

import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas con el formato estándar de problema
 * (RFC 9457): el estado HTTP que corresponde y un detalle que el cliente web
 * muestra al usuario, en lugar de un error 500 genérico.
 *
 * Los errores propios de Spring MVC —datos que no pasan la validación, ruta o
 * método que no existen— los resuelve la clase base con el mismo formato.
 *
 * @author Oscar Iván Blanco Díaz
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    /** Un dato que no existe: 404. */
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail noEncontrado(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Datos que no cumplen una regla del caso de uso: 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail datosInvalidos(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Una operación que el estado actual no permite: 409. */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail conflicto(IllegalStateException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Una escritura que viola una restricción de la base: 409, sin el detalle técnico. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail restriccion(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
            "La operación no se puede completar porque contradice datos ya registrados.");
    }
}
