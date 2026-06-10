package com.vinnilabs.opsmind.application.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class SolutionKnowledgeBase {

    String id;
    String title;
    String category;
    String symptoms;
    String confirmedRootCause;
    String solution;
    List<String> tags;
    boolean validated;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

