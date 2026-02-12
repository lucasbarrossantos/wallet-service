package com.globo.wallet.adapter.http.exception.exceptionhandler;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.globo.wallet.core.exception.BusinessException;
import com.globo.wallet.core.exception.InsufficientBalanceException;
import com.globo.wallet.core.exception.InvalidTransactionAmountException;
import com.globo.wallet.core.exception.UserNotFoundException;
import com.globo.wallet.core.exception.WalletAlreadyExistsException;
import com.globo.wallet.core.exception.WalletNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String detail = ex.getMessage();

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<Object> handleWalletAlreadyExists(WalletAlreadyExistsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        ProblemType problemType = ProblemType.DUPLICATE_ENTITY;
        String detail = ex.getMessage();

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Object> handleWalletNotFound(WalletNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ProblemType problemType = ProblemType.USER_NOT_FOUND;
        String detail = ex.getMessage();

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(InvalidTransactionAmountException.class)
    public ResponseEntity<Object> handleInvalidTransactionAmount(InvalidTransactionAmountException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.BUSINESS_ERROR;
        String detail = ex.getMessage();

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Object> handleInsufficientBalance(InsufficientBalanceException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.BUSINESS_ERROR;
        String detail = ex.getMessage();

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.BUSINESS_ERROR;
        String detail = ex.getMessage();

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemType problemType = ProblemType.VALIDATION_ERROR;
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";
        
        BindingResult bindingResult = ex.getBindingResult();
        
        List<Problem.Field> problemFields = bindingResult.getAllErrors().stream()
                .map(objectError -> {
                    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
                    
                    String name = objectError.getObjectName();
                    
                    if (objectError instanceof FieldError) {
                        name = ((FieldError) objectError).getField();
                    }
                    
                    return new Problem.Field(name, message);
                })
                .toList();
        
        Problem problem = createProblem(status, problemType, detail, detail, problemFields);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        
        ProblemType problemType = ProblemType.INVALID_DATA;
        String detail = "O corpo da requisição está inválido. Verifique erro de sintaxe.";
        
        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        
        ProblemType problemType = ProblemType.METHOD_NOT_ALLOWED;
        String detail = String.format("O método '%s' não é suportado para esta URL.", ex.getMethod());

        Problem problem = createProblem(status, problemType, detail, "Método de requisição não suportado.", null);

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.INTERNAL_SERVER_ERROR;
        String detail = "Ocorreu um erro interno inesperado no sistema. Tente novamente e se o problema persistir, entre em contato com o administrador do sistema.";

        log.error(ex.getMessage(), ex);

        Problem problem = createProblem(status, problemType, detail, detail, null);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        
        if (body == null) {
            body = new Problem(status.value(), null, status.toString(), null,
                "Ocorreu um erro interno inesperado no sistema.", OffsetDateTime.now(), null, null);
        } else if (body instanceof String) {
            body = new Problem(status.value(), null, (String) body, null,
                "Ocorreu um erro interno inesperado no sistema.", OffsetDateTime.now(), null, null);
        }
        
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
    
    private Problem createProblem(HttpStatusCode status, ProblemType problemType, String detail, String userMessage, List<Problem.Field> fields) {

        return new Problem(status.value(), problemType.getUri(), problemType.getTitle(), detail, userMessage, OffsetDateTime.now(), null, fields);
    }
}
