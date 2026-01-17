package com.jobboard.service;

import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionException;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class BaseService {

    private static final Logger logger = LoggerFactory.getLogger(BaseService.class);

    protected <T> T executeWithErrorHandling(Supplier<T> operation, String operationName) {
        try {
            return operation.get();
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation during {}", operationName, e);
            throw e; // Let global handler deal with it
        } catch (DataAccessException e) {
            logger.error("Database error during {}", operationName, e);
            throw new ServiceUnavailableException("Database service temporarily unavailable", e);
        } catch (TransactionException e) {
            logger.error("Transaction error during {}", operationName, e);
            throw new ServiceUnavailableException("Transaction service temporarily unavailable", e);
        } catch (Exception e) {
            logger.error("Unexpected error during {}", operationName, e);
            throw new RuntimeException("Operation failed: " + operationName, e);
        }
    }

    protected <T> T findEntityOrThrow(Optional<T> optional, String entityName, Object id) {
        return optional.orElseThrow(() -> new ResourceNotFoundException(entityName + " not found with ID: " + id));
    }

    protected <T> T findEntityOrThrow(Optional<T> optional, String message) {
        return optional.orElseThrow(() -> new ResourceNotFoundException(message));
    }
}