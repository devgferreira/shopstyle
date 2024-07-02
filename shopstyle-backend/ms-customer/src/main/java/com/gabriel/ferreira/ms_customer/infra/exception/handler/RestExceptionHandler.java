package com.gabriel.ferreira.ms_customer.infra.exception.handler;

import com.gabriel.ferreira.ms_customer.infra.exception.ExceptionResponse;
import com.gabriel.ferreira.ms_customer.infra.exception.customer.CustomerCpfInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.gabriel.ferreira.ms_customer.domain.enums.ErrorCodes;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CustomerCpfInvalidoException.class)
    public final ResponseEntity<Object> handleFuncionarioNaoEncontradoExeception(CustomerCpfInvalidoException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ErrorCodes.CUSTOMER_CPF_INVALIDO, ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(exceptionResponse);
    }
}
