package ru.tech.smarttest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import ru.tech.smarttest.dto.*;
import ru.tech.smarttest.service.RoundService;

@Controller
public class AnswerController {

    @Autowired
    private RoundService roundService;

    @MessageMapping("/playerAnswer")
    public void handlePlayerAnswer(PlayerAnswer playerAnswer) {
        roundService.processPlayerAnswer(playerAnswer);
    }


    @MessageMapping("/round1/additional/answer")
    public void handleAdditionalRoundAnswer(@Payload AdditionalRoundAnswer answer) {
        roundService.processAdditionalRoundAnswer(answer);
    }


    @MessageMapping("/decoder/answer")
    public void handleDecoderAnswer(DecoderAnswer decoderAnswer) {
        roundService.processDecoderAnswer(decoderAnswer);
    }


    @MessageMapping("/decoder/progress")
    public void handleDecoderProgress(DecoderTypingProgress progress) {
        roundService.trackDecoderTypingProgress(progress);
    }

    @MessageMapping("/round2/additional/answer")
    public void handleAdditionalRoundTwoAnswer(@Payload AdditionalRoundAnswer answer) {
        roundService.processAdditionalRoundTwoAnswer(answer);
    }

    @MessageMapping("/decoder2/answer")
    public void handleDecoderTwoAnswer(DecoderAnswer decoderAnswer) {
        roundService.processDecoderTwoAnswer(decoderAnswer);
    }

    @MessageMapping("/decoder2/progress")
    public void handleDecoderTwoProgress(DecoderTypingProgress progress) {
        roundService.trackDecoderTwoTypingProgress(progress);
    }


    @MessageMapping("/round3/select")
    public void handleQuestionSelection(@Payload QuestionSelection selection) {
        roundService.processQuestionSelection(selection);
    }

    @MessageMapping("/round3/answer")
    public void handleRound3Answer(Round3Answer answer) {
        roundService.processRound3Answer(answer);
    }



    @MessageMapping("/round2/select")
    public void handleRound2Selection(@Payload Round2Selection selection) {
        roundService.processRound2Selection(selection);
    }

}
