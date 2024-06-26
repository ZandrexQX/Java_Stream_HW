package ru.gb.lesson5;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

  private final static ObjectMapper objectMapper = new ObjectMapper();

  // Socket - абстракция, к которой можно подключиться
  // ip-address + port - socket
  // network - сеть - набор соединенных устройств
  // ip-address - это адрес устройства в какой-то сети
  // 8080 - http
  // 443 - https
  // 35 - smtp
  // 21 - ftp
  // 5432 - стандартный порт postgres
  // клиент подключается к серверу

  /**
   * Порядок взаимодействия:
   * 1. Клиент подключается к серверу
   * 2. Клиент посылает сообщение, в котором указан логин. Если на сервере уже есть подключеный клиент с таким логином, то соедение разрывается
   * 3. Клиент может посылать 3 типа команд:
   * 3.1 list - получить логины других клиентов
   * <p>
   * 3.2 send @login message - отправить личное сообщение с содержимым message другому клиенту с логином login
   * 3.3 send message - отправить сообщение всем с содержимым message
   */

  // 1324.132.12.3:8888
  public static void main(String[] args) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    try (ServerSocket server = new ServerSocket(8888)) {
      System.out.println("Сервер запущен");

      while (true) {
        System.out.println("Ждем клиентского подключения");
        Socket client = server.accept();
        ClientHandler clientHandler = new ClientHandler(client, clients, objectMapper);
        new Thread(clientHandler).start();
      }
    } catch (IOException e) {
      System.err.println("Ошибка во время работы сервера: " + e.getMessage());
    }
  }
}
