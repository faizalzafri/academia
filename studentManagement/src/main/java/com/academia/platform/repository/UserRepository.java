package com.academia.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.academia.platform.model.ApprovalStatus;
import com.academia.platform.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findById(String id);
	Optional<User> findByEmail(String email);
	List<User> findByApprovalStatus(ApprovalStatus approvalStatus);
	List<User> findByApprovalStatusOrderByRegistrationDateDesc(ApprovalStatus approvalStatus);
	long countByApprovalStatus(ApprovalStatus approvalStatus);
}
