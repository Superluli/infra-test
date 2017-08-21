package com.superluli.infra.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.superluli.infra.exception.NestedServerRuntimeException;
import com.superluli.infra.jpa.UserEntity;
import com.superluli.infra.jpa.UserRepo;

@Service
public class UserService {

	@Autowired
	private UserRepo userRepo;

	public UserModel getUserById(String id) {

		UserEntity found = userRepo.findOne(id);
		if (found == null) {
			throw new NestedServerRuntimeException(HttpStatus.NOT_FOUND, "user with id " + id + " not found");
		}
		return UserModel.fromEntity(found);
	}

	public UserModel createUser(UserModel userModelReq) {

		UserEntity saved = userRepo.save(userModelReq.toEntity());
		return UserModel.fromEntity(saved);
	}

	public List<UserModel> queryUsersByName(String name) {

		List<UserEntity> findByName = userRepo.findByName(name);
		return findByName.stream().map(UserModel::fromEntity).collect(Collectors.toList());
	}
}
