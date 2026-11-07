package com.h123.waddle.services.interfaces;

import com.h123.waddle.dtos.WordDto;
import jakarta.validation.constraints.NotNull;

public interface AdminService {
    public void addWord(WordDto word);
}
