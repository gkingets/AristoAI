package com.magic.chatai;

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class x_ChatGPT {
    private OpenAiService openAiService;

    public x_ChatGPT() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        openAiService = new OpenAiService("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", Duration.ofSeconds(30));
    }

    public String chatGPT(String question) {
        String answer;
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        chatMessages.add(new ChatMessage("user", question));

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(chatMessages)
                .maxTokens(4000)
                .build();

        try {
            ChatCompletionChoice choice = openAiService.createChatCompletion(completionRequest).getChoices().get(0);
            answer = choice.getMessage().getContent();
        } catch (Exception e) {
            answer = "error!";
        }
        return answer;
    }
}
