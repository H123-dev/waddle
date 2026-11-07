package com.h123.waddle.services.impls;


import com.h123.waddle.dtos.WordDto;
import com.h123.waddle.entities.Word;
import com.h123.waddle.exceptions.ResourceConflictException;
import com.h123.waddle.repos.WordRepo;
import com.h123.waddle.services.interfaces.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final WordRepo wordRepo;

    public AdminServiceImpl(WordRepo wordRepo) {
        this.wordRepo = wordRepo;
    }

    @Override
    public void addWord(WordDto word) throws ResourceConflictException
    {
        if(wordRepo.findByWord(word.word()).isPresent())
            throw new ResourceConflictException("word already present in db");
        Word w = new Word(word.word());
        wordRepo.save(w);
    }
}
