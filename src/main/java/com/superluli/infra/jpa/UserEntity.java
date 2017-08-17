package com.superluli.infra.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "infra_test_user")
public class UserEntity {
	
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.AUTO)
    private String id;

	@Column(name = "nm")
    private String name;

    public UserEntity() {
		super();
	}
    
    public UserEntity(String name) {
		super();
		this.name = name;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
