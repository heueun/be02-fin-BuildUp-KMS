package com.example.bootshelf.review.exception;

import com.example.bootshelf.common.error.ErrorCode;
import com.example.bootshelf.common.error.exception.BusinessException;
import lombok.Getter;

@Getter
public class ReviewException extends BusinessException {
    private ErrorCode errorCode;
    private String message;

    public ReviewException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
        this.message = message;
    }
}