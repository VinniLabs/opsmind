package com.vinnilabs.opsmind.adapter.rest;

import com.vinnilabs.opsmind.adapter.rest.mapper.FeedbackMapper;
import com.vinnilabs.opsmind.adapter.rest.request.FeedbackRequest;
import com.vinnilabs.opsmind.adapter.rest.response.FeedbackResponse;
import com.vinnilabs.opsmind.application.usecase.SubmitFeedbackUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final SubmitFeedbackUseCase submitFeedbackUseCase;
    private final FeedbackMapper feedbackMapper;

    @PostMapping
    public ResponseEntity<FeedbackResponse> submit(@Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse response = feedbackMapper.toResponse(
                submitFeedbackUseCase.submit(
                        request.getAnalysisId(),
                        request.getUseful(),
                        request.getComment()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
