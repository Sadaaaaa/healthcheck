package com.example.healthcheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.HttpURLConnection;
import java.net.URL;

@Component
@Slf4j
public class HealthChecker extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${backend.url}")
    private String backendUrl;

    @Value("${telegram.id}")
    private String telegramId;

    private boolean isServerDown = false;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() &&
                update.getMessage().getText().equals("/healthcheck@plankit_healthcheck_bot")) {
            log.info("{} pressed /healthcheck button", update.getMessage().getFrom().getUserName());
            performHealthCheck(update.getMessage().getChatId().toString(), true);
        }
    }

    @Scheduled(fixedRateString = "${healthcheck.rate:60000}")
    public void scheduledHealthCheck() {
        performHealthCheck(telegramId, false);
    }

    private void performHealthCheck(String chatId, boolean isButton) {
        boolean isFrontendHealthy = checkUrl(frontendUrl);
        boolean isBackendHealthy = checkUrl(backendUrl);

        updateStatus(chatId, isFrontendHealthy && isBackendHealthy, isButton);
    }

    private boolean checkUrl(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateStatus(String chatId, boolean isHealthy, boolean isButton) {
        if (isButton && isHealthy) {
            sendMessage(chatId, "Сервер работает! 🎉");
            isServerDown = false;
            return;
        } else if (isButton) {
            sendMessage(chatId, "Сервер недоступен! ❌");
            isServerDown = true;
        }

        if (isHealthy && isServerDown) {
            sendMessage(chatId, "Сервер снова работает! 🎉");
            isServerDown = false;
        } else if (!isHealthy && !isServerDown) {
            sendMessage(chatId, "Сервер недоступен! ❌");
            isServerDown = true;
        }
    }

    private void sendMessage(String chatId, String message) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(message);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}