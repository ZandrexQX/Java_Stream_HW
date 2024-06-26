package ru.gb.lesson5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class ChatClient {

  private static ObjectMapper objectMapper = new ObjectMapper();
  private static boolean working = true;

  public static void main(String[] args) {
//    String clientLogin = "User_" + UUID.randomUUID().toString();
//    String clientLogin = "nagibator";
    Scanner console = new Scanner(System.in);
    String clientLogin = console.nextLine();

    // 127.0.0.1 или localhost
    try (Socket server = new Socket("localhost", 8888)) {
      System.out.println("Успешно подключились к серверу");

      try (PrintWriter out = new PrintWriter(server.getOutputStream(), true)) {
        Scanner in = new Scanner(server.getInputStream());

        String loginRequest = createLoginRequest(clientLogin);
        out.println(loginRequest);

        String loginResponseString = in.nextLine();
        if (!checkLoginResponse(loginResponseString)) {
          // TODO: Можно обогатить причиной, чтобы клиент получал эту причину
          // (логин уже занят, ошибка аутентификации\авторизации, ...)
          System.out.println(loginResponseString + " занят. Закрываем соединение");
          server.close();
          return;
        }

        new Thread(() -> {
          while (working) {
            String msgFromServer = in.nextLine();
            System.out.println("Сообщение от сервера: " + msgFromServer);
          }
        }).start();


        while (working) {
          System.out.println("Что хочу сделать?");
          System.out.println("1. Послать сообщение другу");
          System.out.println("2. Послать сообщение всем");
          System.out.println("3. Получить список логинов");
          System.out.println("4. Выход");

          String command = console.nextLine();
          if (command.equals("1")) {
            String message = console.nextLine();

            SendMessageRequest request = new SendMessageRequest();
            request.setMessage(message);
            request.setRecipient(message.split(" ")[0]);

            String sendMsgRequest = objectMapper.writeValueAsString(request);
            out.println(sendMsgRequest);
          } else if (command.equals("2")) {

            BroadcastRequest request = new BroadcastRequest();
            request.setMessage(console.nextLine());
            String sendMsgRequest = objectMapper.writeValueAsString(request);
            out.println(sendMsgRequest);

          } else if (command.equals("3")) {
            UsersRequest request = new UsersRequest();
            String sendMsgRequest = objectMapper.writeValueAsString(request);
            out.println(sendMsgRequest);
          } else if(command.equals("4")) {
            DisconnectRequest request = new DisconnectRequest();
            String sendMsgRequest = objectMapper.writeValueAsString(request);
            out.println(sendMsgRequest);
            server.close();
            working = false;
          } else {
            System.out.println("Неизвестная команда");
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Ошибка во время подключения к серверу: " + e.getMessage());
    }

    System.out.println("Отключились от сервера");
  }

  private static String createLoginRequest(String login) {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setLogin(login);

    try {
      return objectMapper.writeValueAsString(loginRequest);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Ошибка JSON: " + e.getMessage());
    }
  }

  private static boolean checkLoginResponse(String loginResponse) {
    try {
      LoginResponse resp = objectMapper.reader().readValue(loginResponse, LoginResponse.class);
      return resp.isConnected();
    } catch (IOException e) {
      System.err.println("Ошибка чтения JSON: " + e.getMessage());
      return false;
    }
  }
}
