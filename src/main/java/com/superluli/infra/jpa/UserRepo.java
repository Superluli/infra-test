package com.superluli.infra.jpa;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<UserEntity, String>{

	@Cacheable(cacheNames="UserRepo_findByName", cacheManager="longTimeoutCacheManager")
    public List<UserEntity> findByName(String name);
	
	@Query(value = "SELECT u FROM UserEntity u WHERE u.name=?1")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public List<UserEntity> findByNameForUpdate(String name);
}
