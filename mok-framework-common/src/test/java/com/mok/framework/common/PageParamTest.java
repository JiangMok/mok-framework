package com.mok.framework.common;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PageParamTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsPageSizeAboveMaximum() {
        PageParam param = PageParam.of(1, PageParam.MAX_PAGE_SIZE + 1);

        assertFalse(validator.validate(param).isEmpty());
    }

    @Test
    void validateClampsProgrammaticPageSize() {
        PageParam param = PageParam.of(1, PageParam.MAX_PAGE_SIZE + 1).validate();

        assertEquals(PageParam.MAX_PAGE_SIZE, param.getPageSize());
    }
}

