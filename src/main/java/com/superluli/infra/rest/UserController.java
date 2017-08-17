package com.superluli.infra.rest;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.superluli.infra.commons.resources.IndexBasedRestResourceList;

@RestController
@RequestMapping(value = "/users")
public class UserController {

	@Autowired
	private UserService userService;
	
	@RequestMapping(value = "/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserModel getUserById(@PathVariable("userId") String userId) {

		return new UserModel("xxx");
	}

	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public IndexBasedRestResourceList<UserModel> queryUsersByName(@RequestParam("name") String name) {

		return new IndexBasedRestResourceList<UserModel>(null, null, null, 0, 0, Arrays.asList(new UserModel("xxx")));
	}

	@RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserModel createUser(HttpServletRequest request, @RequestParam MultiValueMap<String, String> params,
			@RequestHeader HttpHeaders requestHeaders, @RequestBody UserModel userModelRequest) {

		System.err.println(request.getRequestURI());
		System.err.println(params);
		System.err.println(requestHeaders);
		
		return userService.createUser(userModelRequest);
	}
}
