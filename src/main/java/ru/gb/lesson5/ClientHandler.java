package ru.gb.lesson5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

class ClientHandler implements Runnable {

    private final Socket client;
    private final Scanner in;
    private final PrintWriter out;
    private final Map<String, ClientHandler> clients;
    private String clientLogin;
    private ObjectMapper objectMapper;
    private boolean working = true;

    public ClientHandler(Socket client, Map<String, ClientHandler> clients, ObjectMapper objectMapper) throws IOException {
        this.client = client;
        this.clients = clients;
        this.objectMapper = objectMapper;

        this.in = new Scanner(client.getInputStream());
        this.out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        System.out.println("Подключен новый клиент");

        try {
            String loginRequest = in.nextLine();
            LoginRequest request = objectMapper.reader().readValue(loginRequest, LoginRequest.class);
            this.clientLogin = request.getLogin();
        } catch (IOException e) {
            System.err.println("Не удалось прочитать сообщение от клиента [" + clientLogin + "]: " + e.getMessage());
            String unsuccessfulResponse = createLoginResponse(false);
            out.println(unsuccessfulResponse);
            doClose();
            return;
        }

        System.out.println("Запрос от клиента: " + clientLogin);
        // Проверка, что логин не занят
        if (clients.containsKey(clientLogin)) {
            String unsuccessfulResponse = createLoginResponse(false);
            out.println(unsuccessfulResponse);
            doClose();
            return;
        }

        clients.put(clientLogin, this);
        String successfulLoginResponse = createLoginResponse(true);
        out.println(successfulLoginResponse);

        while (working) {
            String msgFromClient = in.nextLine();

            final String type;
            try {
                AbstractRequest request = objectMapper.reader().readValue(msgFromClient, AbstractRequest.class);
                type = request.getType();
            } catch (IOException e) {
                System.err.println("Не удалось прочитать сообщение от клиента [" + clientLogin + "]: " + e.getMessage());
                sendMessage("Не удалось прочитать сообщение: " + e.getMessage());
                continue;
            }

            if (SendMessageRequest.TYPE.equals(type)) {
                // Клиент прислал SendMessageRequest

                final SendMessageRequest request;
                try {
                    request = objectMapper.reader().readValue(msgFromClient, SendMessageRequest.class);
                } catch (IOException e) {
                    System.err.println("Не удалось прочитать сообщение от клиента [" + clientLogin + "]: " + e.getMessage());
                    sendMessage("Не удалось прочитать сообщение SendMessageRequest: " + e.getMessage());
                    continue;
                }

                ClientHandler clientTo = clients.get(request.getRecipient());
                if (clientTo == null) {
                    sendMessage("Клиент с логином [" + request.getRecipient() + "] не найден");
                    continue;
                }
                clientTo.sendMessage("[" + clientLogin + "]: " + request.getMessage());
            } else if (BroadcastRequest.TYPE.equals(type)) {
                // Клиент прислал BroadcastRequest

                final BroadcastRequest request;
                try {
                    request = objectMapper.reader().readValue(msgFromClient, BroadcastRequest.class);
                } catch (IOException e) {
                    System.err.println("Не удалось прочитать сообщение от клиента [" + clientLogin + "]: " + e.getMessage());
                    sendMessage("Не удалось прочитать сообщение BroadcastRequest: " + e.getMessage());
                    continue;
                }
                for (ClientHandler clientTo : clients.values()) {
                    if (clientTo == this) {
                        continue;
                    }
                    clientTo.sendMessage("[" + clientLogin + "]: " + request.getMessage());
                }

            } else if (UsersRequest.TYPE.equals(type)) {
                // Клиент прислал GetUsersRequest

                this.sendMessage("Список юзеров: " + String.join(", ", clients.keySet()));

            } else if (DisconnectRequest.TYPE.equals(type)) {
                // Клиент прислал DisconnectRequest

                for (ClientHandler clientTo : clients.values()) {
                    if (clientTo == this) {
                        continue;
                    }
                    clientTo.sendMessage("Клиент [" + clientLogin + "] отключился");
                }
                working = false;
                doClose();
            } else {
                System.err.println("Неизвестный тип сообщения: " + type);
                sendMessage("Неизвестный тип сообщения: " + type);
                continue;
            }
        }
    }

    private void doClose() {
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            System.err.println("Ошибка во время отключения клиента: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private String createLoginResponse(boolean success) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setConnected(success);
        try {
            return objectMapper.writer().writeValueAsString(loginResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось создать loginResponse: " + e.getMessage());
        }
    }

}
