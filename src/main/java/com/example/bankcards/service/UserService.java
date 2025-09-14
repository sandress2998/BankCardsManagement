package com.example.bankcards.service;

import com.example.bankcards.dto.RoleRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Находит пользователя по логину.
     *
     * @param login логин пользователя
     * @return объект пользователя User или null, если не найден
     * @throws com.example.bankcards.exception.NotFoundException если пользователь не был найден
     * @throws IllegalArgumentException если длина логина больше позволенного
     */
    User findByLogin(String login);

    /**
     * Проверяет, существует ли пользователь с данным логином.
     *
     * @param login логин пользователя
     * @throws com.example.bankcards.exception.BadRequestException если пользователь с переданным логином существует
     * @throws IllegalArgumentException если длина логина больше позволенного
     */
    void checkIfNotExistsByLogin(String login);

    /**
     * Сохраняет пользователя в базе данных.
     *
     * @param user объект пользователя для сохранения
     * @return сохранённый объект пользователя User с установленным идентификатором
     * @throws IllegalArgumentException если переданные данные пользователя не соответствуют нормам
     */
    User save(User user);

    /**
     * Запрос на получение роли администратора (ADMIN) для текущего пользователя.
     *
     * Нюансы:
     * - Пользователь должен быть аутентифицирован.
     * - Проверяется переданный "секретный" пароль (secret) с захешированным значением из настроек.
     * - При успешной проверке обновляет роль пользователя в базе и возвращает новый JWT с ролью ADMIN.
     * - При неверном пароле выбрасывается UnauthorizedException.
     * - При отсутствии пользователя выбрасывается NotFoundException.
     *
     * @param request объект с секретом для получения роли ADMIN
     * @return объект AuthResponse с новым JWT токеном
     * @throws org.springframework.security.access.AccessDeniedException если пароль неправильный
     */
    JwtResponse requestRole(RoleRequest request);

    /**
     * Получает список всех пользователей с пагинацией.
     *
     * Нюансы:
     * - Доступно только пользователям с ролью ROLE_ADMIN.
     *
     * @param page номер страницы (0-основанный)
     * @param size размер страницы
     * @return список DTO объектов UserInfoResponse с базовой информацией о пользователях
     */
    List<UserInfoResponse> getAll(int page, int size);

    /**
     * Находит пользователя по UUID.
     *
     * @param id UUID пользователя
     * @return объект пользователя (если не найден, выбрасывает исключение)
     * @throws com.example.bankcards.exception.NotFoundException
     */
    UserInfoResponse getUserInfoById(UUID id);
}

/*
public interface UserService {

    User findByLogin(String login);

    User findById(UUID id);

    User save(User user);

    AuthResponse requestAdmin(AdminRequest secret);

    List<UserInfoResponse> getAll(int page, int size);
}
 */
