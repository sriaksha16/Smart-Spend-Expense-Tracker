package com.example.smartexpense.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartexpense.model.Notification;
import com.example.smartexpense.model.UserReg;


@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFalse(UserReg user);

	

	



	List<Notification> findByUser_IdAndReadFalse(Long userid);
}