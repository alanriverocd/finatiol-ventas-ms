package com.finatiol.ventas.exception;

import com.finatiol.common.constants.ventas.ErrorCodes;
import com.finatiol.common.exception.ResourceNotFoundException;
import com.finatiol.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    manejarNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ErrorResponse(
                                ErrorCodes.VENTA_NO_ENCONTRADA,
                                ex.getMessage(),
                                404));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    manejarGeneral(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ErrorResponse(
                                ErrorCodes.ERROR_INTERNO,
                                ex.getMessage(),
                                500));
    }
}
