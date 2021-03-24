package com.hisense.gateway.library.model.base.eureka;

import com.hisense.api.library.model.DefineMode;

import javax.persistence.AttributeConverter;

public class StoreTypeConverter implements AttributeConverter<DefineMode, String> {
    @Override
    public String convertToDatabaseColumn(DefineMode defineMode) {
        return defineMode.getName();
    }

    @Override
    public DefineMode convertToEntityAttribute(String s) {
        return DefineMode.fromString(s);
    }
}
