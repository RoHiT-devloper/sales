package com.salesSavvy.service;

import com.salesSavvy.entity.Users;
import com.salesSavvy.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersServiceImplementation implements UsersService {
    @Autowired
    UsersRepository repo;

    @Override
    public void signUp(Users user) {
        repo.save(user);
    }

    @Override
    public Users getUser(String username) {
        return repo.findByUsername(username);
    }

    @Override
    public Users getPassword(String password) {
        return repo.findByPassword(password);
    }

    @Override
    public boolean validate(String username, String password) {
        Users user = getUser(username);
        String dbPassword = user.getPassword();
        return (password.equals(dbPassword));
    }

    @Override
    public Users saveUser(Users user) {
        return repo.save(user);
    }
    
    @Override
    public List<Users> getAllUsers() {
        return repo.findAll();
    }

    @Override
    public String deleteUser(Long id) {
        repo.deleteById(id);
        return "User deleted Successfully!";
    }
}