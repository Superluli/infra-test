package com.superluli.infra.rest;

import com.superluli.infra.commons.resources.AbstractRestResource;
import com.superluli.infra.jpa.UserEntity;

public class UserModel extends AbstractRestResource{
	
	public UserModel() {
		super();
	}
	
	public UserModel(String name) {
		super();
		this.name = name;
	}
	
	public UserModel(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public static UserModel fromEntity(UserEntity entity){
		
		return new UserModel(entity.getId(), entity.getName());
	}
	
	public UserEntity toEntity(){
		return new UserEntity(this.name);
	}
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserModel other = (UserModel) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
