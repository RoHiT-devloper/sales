package com.salesSavvy.service;

import com.salesSavvy.entity.Users;
import java.util.List;

public interface UsersService {
    void signUp(Users user);
    Users getUser(String username);
    Users getPassword(String password);
    boolean validate(String username, String password);
    List<Users> getAllUsers();
    String deleteUser(Long id);
    Users saveUser(Users user);
}