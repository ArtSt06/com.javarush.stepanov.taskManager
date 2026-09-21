package com.javarush.stepanov.taskmanager.configuration.constants;

public interface OpenApiConstants {
    String AUTH_TAG = "Аутентификация пользователя";
    String TASK_TAG = "Управление задачами";

    String REGISTER_SUMMARY = "Регистрация нового пользователя";
    String REGISTER_DESCRIPTION = "Создает новый аккаунт в системе. По умолчанию любому пользователю присваивается роль USER.";
    String LOGIN_SUMMARY = "Вход в систему (Аутентификация)";
    String LOGIN_DESCRIPTION = "Проверяет учетные данные и возвращает JWT-токен для доступа к защищенным эндпоинтам.";

    String CREATE_TASK_SUMMARY = "Создать новую задачу";
    String CREATE_TASK_DESCRIPTION = "Создает задачу и автоматически назначает текущего авторизованного пользователя её владельцем.";
    String GET_ALL_TASKS_SUMMARY = "Получить все мои задачи";
    String GET_ALL_TASKS_DESCRIPTION = "Возвращает список всех задач, принадлежащих текущему авторизованному пользователю.";
    String GET_TASK_SUMMARY = "Получить задачу по ID";
    String GET_TASK_DESCRIPTION = "Возвращает задачу по её идентификатору, если текущий пользователь является её владельцем или администратором.";
    String UPDATE_TASK_SUMMARY = "Обновить задачу";
    String UPDATE_TASK_DESCRIPTION = "Изменяет поля и статус существующей задачи, если текущий пользователь является её владельцем или администратором.";
    String DELETE_TASK_SUMMARY = "Удалить задачу";
    String DELETE_TASK_DESCRIPTION = "Навсегда удаляет задачу из системы, если текущий пользователь является её владельцем или администратором.";

    String STATUS_200_DESCRIPTION = "Запрос успешно обработан. Данные возвращены.";
    String STATUS_201_DESCRIPTION = "Ресурс успешно создан.";
    String STATUS_204_DESCRIPTION = "Ресурс успешно удален. Содержимое ответа отсутствует.";
    String STATUS_400_DESCRIPTION = "Ошибка валидации — переданы некорректные, пустые или нарушающие ограничения поля JSON.";
    String STATUS_401_DESCRIPTION = "Ошибка аутентификации — неверный логин/пароль, либо токен отсутствует/невалиден.";
    String STATUS_403_DESCRIPTION = "Отказ в доступе — у текущего пользователя недостаточно прав для взаимодействия с этим ресурсом.";
    String STATUS_404_DESCRIPTION = "Ресурс не найден — задача или пользователь с указанным идентификатором отсутствует в системе.";
    String STATUS_409_DESCRIPTION = "Конфликт данных — попытка регистрации дубликата уникального имени пользователя или email.";
}
