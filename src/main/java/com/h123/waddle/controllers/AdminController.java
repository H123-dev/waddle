package com.h123.waddle.controllers;

import com.h123.waddle.dtos.WordDto;
import com.h123.waddle.entities.Word;
import com.h123.waddle.services.interfaces.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/waddle/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/word")
    public ResponseEntity<WordDto> addWord(@Valid @RequestBody WordDto word)
    {
        adminService.addWord(word);
        return new ResponseEntity<>(word, HttpStatus.CREATED);
    }
}
