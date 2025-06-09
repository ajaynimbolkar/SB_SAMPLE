package com.ril.SB_SAMPLE.components;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.ril.SB_SAMPLE.dto.request.SortRequestDto;

@Component
public class StringToSortDtoConverter implements Converter<String, SortRequestDto> {

	@Override
	public SortRequestDto convert(String source) {
		String[] parts = source.split(",");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid sort format: expected 'field,ASC|DESC'");
		}

		SortRequestDto dto = new SortRequestDto();
		dto.setField(parts[0].trim());
		dto.setOrder(parts[1].trim().toUpperCase());
		return dto;
	}
}