package com.soaesps.schedulerservice.Utils.convertor;

import com.soaesps.core.Utils.convertor.json.JsonHConvertor;
import com.soaesps.schedulerservice.dto.FailedDTO;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class FailedDTOConvertor extends JsonHConvertor<FailedDTO> implements AttributeConverter<FailedDTO, String> {
}