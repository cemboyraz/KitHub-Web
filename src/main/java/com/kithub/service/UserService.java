package com.kithub.service;

import com.kithub.repository.AIRecommendationRepository;
import com.kithub.repository.BookRepository;
import com.kithub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final AIRecommendationRepository aiRecommendationRepository;


}
