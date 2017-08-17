package com.superluli.infra.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.superluli.infra.jpa.UserEntity;
import com.superluli.infra.jpa.UserRepo;

@Service
public class UserService {
	
	@Autowired
	private UserRepo userRepo;
	
	public UserModel createUser(UserModel userModelReq){
		
		UserEntity saved = userRepo.save(userModelReq.toEntity());
		return UserModel.fromEntity(saved);
	}
}
