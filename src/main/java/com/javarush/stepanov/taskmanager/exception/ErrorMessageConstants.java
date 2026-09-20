package com.javarush.stepanov.taskmanager.exception;

public interface ErrorMessageConstants {
    String INVALID_LOGIN_OR_PASSWORD = "Неверное имя пользователя или пароль";
    String INVALID_AUTH_TOKEN = "Неверный токен авторизации";
    String UNAUTHORIZED = "Необходима авторизация";

    String ACCESS_DENIED_VIEW = "Отсутствуют права на просмотр этой задачи";
    String ACCESS_DENIED_UPDATE = "Отсутствуют права на обновление этой задачи";
    String ACCESS_DENIED_DELETE = "Отсутствуют права на удаление этой задачи";

    String USER_NOT_FOUND = "Авторизованный пользователь не найден в базе данных";
    String TASK_NOT_FOUND = "Задача с таким ID не существует";

    String USERNAME_ALREADY_TAKEN = "Пользователь с таким именем уже существует";
    String EMAIL_ALREADY_TAKEN = "Пользователь с таким Email уже существует";
    String USER_ALREADY_EXISTS = "Пользователь с таким именем или Email уже существует";
}
