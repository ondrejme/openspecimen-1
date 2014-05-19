
package com.krishagni.catissueplus.core.administrative.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.krishagni.catissueplus.core.administrative.domain.Password;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.administrative.domain.factory.UserErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.UserFactory;
import com.krishagni.catissueplus.core.administrative.events.AllUsersEvent;
import com.krishagni.catissueplus.core.administrative.events.CloseUserEvent;
import com.krishagni.catissueplus.core.administrative.events.CreateUserEvent;
import com.krishagni.catissueplus.core.administrative.events.ForgotPasswordEvent;
import com.krishagni.catissueplus.core.administrative.events.PasswordForgottenEvent;
import com.krishagni.catissueplus.core.administrative.events.PasswordUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.PasswordValidatedEvent;
import com.krishagni.catissueplus.core.administrative.events.PatchUserEvent;
import com.krishagni.catissueplus.core.administrative.events.ReqAllUsersEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdatePasswordEvent;
import com.krishagni.catissueplus.core.administrative.events.UpdateUserEvent;
import com.krishagni.catissueplus.core.administrative.events.UserClosedEvent;
import com.krishagni.catissueplus.core.administrative.events.UserCreatedEvent;
import com.krishagni.catissueplus.core.administrative.events.UserDetails;
import com.krishagni.catissueplus.core.administrative.events.UserUpdatedEvent;
import com.krishagni.catissueplus.core.administrative.events.ValidatePasswordEvent;
import com.krishagni.catissueplus.core.administrative.services.UserService;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.email.EmailSender;
import com.krishagni.catissueplus.core.common.errors.CatissueException;
import com.krishagni.catissueplus.core.common.errors.ObjectCreationException;
import com.krishagni.catissueplus.core.common.events.UserSummary;

public class UserServiceImpl implements UserService {

	@Autowired
	private DaoFactory daoFactory;

	@Autowired
	private UserFactory userFactory;

	private final String LOGIN_NAME = "login name";

	private final String EMAIL_ADDRESS = "email address";

	private final String CATISSUE = "catissue";

	@Autowired
	private EmailSender emailSender;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setUserFactory(UserFactory userFactory) {
		this.userFactory = userFactory;
	}

	public void setEmailSender(EmailSender emailSender) {
		this.emailSender = emailSender;
	}

	@Override
	@PlusTransactional
	public AllUsersEvent getAllUsers(ReqAllUsersEvent req) {
		List<User> users = daoFactory.getUserDao().getAllUsers();
		List<UserSummary> result = new ArrayList<UserSummary>();

		for (User user : users) {
			result.add(UserSummary.fromUser(user));
		}

		return AllUsersEvent.ok(result);
	}

	@Override
	@PlusTransactional
	public UserCreatedEvent createUser(CreateUserEvent event) {
		try {
			User user = userFactory.createUser(event.getUserDetails());
			ObjectCreationException exceptionHandler = new ObjectCreationException();
			ensureUniqueLoginNameInDomain(user.getLoginName(), user.getAuthDomain().getName(), exceptionHandler);
			ensureUniqueEmailAddress(user.getEmailAddress(), exceptionHandler);
			exceptionHandler.checkErrorAndThrow();

			user.setPasswordToken(user, event.getUserDetails().getDomainName());
			daoFactory.getUserDao().saveOrUpdate(user);
			emailSender.sendUserCreatedEmail(user);
			return UserCreatedEvent.ok(UserDetails.fromDomain(user));
		}
		catch (ObjectCreationException ce) {
			return UserCreatedEvent.invalidRequest(UserErrorCode.ERRORS.message(), ce.getErroneousFields());
		}
		catch (Exception e) {
			return UserCreatedEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public UserUpdatedEvent updateUser(UpdateUserEvent event) {
		try {
			Long userId = event.getUserDetails().getId();
			User oldUser = daoFactory.getUserDao().getUser(userId);
			if (oldUser == null) {
				return UserUpdatedEvent.notFound(userId);
			}
			User user = userFactory.createUser(event.getUserDetails());

			ObjectCreationException exceptionHandler = new ObjectCreationException();
			validateChangeInUniqueEmail(oldUser, user, exceptionHandler);
			exceptionHandler.checkErrorAndThrow();

			oldUser.update(user);
			daoFactory.getUserDao().saveOrUpdate(oldUser);
			return UserUpdatedEvent.ok(UserDetails.fromDomain(oldUser));
		}
		catch (ObjectCreationException ce) {
			return UserUpdatedEvent.invalidRequest(UserErrorCode.ERRORS.message(), ce.getErroneousFields());
		}
		catch (Exception e) {
			return UserUpdatedEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public UserClosedEvent closeUser(CloseUserEvent event) {
		try {
			User oldUser = daoFactory.getUserDao().getUser(event.getId());
			if (oldUser == null) {
				return UserClosedEvent.notFound(event.getId());
			}
			oldUser.close();
			daoFactory.getUserDao().saveOrUpdate(oldUser);
			return UserClosedEvent.ok();
		}
		catch (Exception e) {
			return UserClosedEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public PasswordUpdatedEvent changePassword(UpdatePasswordEvent event) {
		try {
			Long userId = event.getPasswordDetails().getUserId();
			User user = daoFactory.getUserDao().getUserByIdAndDomainName(userId, CATISSUE);
			if (user == null) {
				return PasswordUpdatedEvent.notFound();
			}
			user.changePassword(event.getPasswordDetails());
			daoFactory.getUserDao().saveOrUpdate(user);
			return PasswordUpdatedEvent.ok();
		}
		catch (CatissueException ce) {
			return PasswordUpdatedEvent.invalidRequest(ce.getMessage());
		}
		catch (Exception e) {
			return PasswordUpdatedEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public PasswordUpdatedEvent setPassword(UpdatePasswordEvent event) {
		try {
			Long userId = event.getPasswordDetails().getUserId();
			User user = daoFactory.getUserDao().getUserByIdAndDomainName(userId, CATISSUE);
			if (user == null) {
				return PasswordUpdatedEvent.notFound();
			}
			user.setPassword(event.getPasswordDetails(), event.getPasswordToken());
			daoFactory.getUserDao().saveOrUpdate(user);
			return PasswordUpdatedEvent.ok();
		}
		catch (CatissueException ce) {
			return PasswordUpdatedEvent.invalidRequest(ce.getMessage());
		}
		catch (Exception e) {
			return PasswordUpdatedEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public PasswordForgottenEvent forgotPassword(ForgotPasswordEvent event) {
		try {
			String loginName = event.getName();
			User user = daoFactory.getUserDao().getUserByLoginNameAndDomainName(loginName, CATISSUE);
			if (user == null) {
				return PasswordForgottenEvent.notFound();
			}
			user.setPasswordToken(UUID.randomUUID().toString());
			daoFactory.getUserDao().saveOrUpdate(user);
			emailSender.sendForgotPasswordEmail(user);
			return PasswordForgottenEvent.ok();
		}
		catch (CatissueException ce) {
			return PasswordForgottenEvent.invalidRequest(ce.getMessage());
		}
		catch (Exception e) {
			return PasswordForgottenEvent.serverError(e);
		}
	}

	@Override
	public PasswordValidatedEvent validatePassword(ValidatePasswordEvent event) {
		try {
			Boolean isValid = Password.isValidPasswordPattern(event.getPassword());
			return PasswordValidatedEvent.ok(isValid);
		}
		catch (CatissueException ce) {
			return PasswordValidatedEvent.invalidRequest(ce.getMessage());
		}
	}

	@Override
	public UserUpdatedEvent patchUser(PatchUserEvent event) {
		try {
			Long userId = event.getUserId();
			User oldUser = daoFactory.getUserDao().getUser(userId);
			if (oldUser == null) {
				return UserUpdatedEvent.notFound(userId);
			}
			User user = userFactory.patchUser(oldUser, event.getUserDetails());
			ObjectCreationException exceptionHandler = new ObjectCreationException();
			ensureUniqueEmailAddress(user.getEmailAddress(), exceptionHandler);
			exceptionHandler.checkErrorAndThrow();

			daoFactory.getUserDao().saveOrUpdate(user);
			return UserUpdatedEvent.ok(UserDetails.fromDomain(user));
		}
		catch (ObjectCreationException ce) {
			return UserUpdatedEvent.invalidRequest(UserErrorCode.ERRORS.message(), ce.getErroneousFields());
		}
		catch (Exception e) {
			return UserUpdatedEvent.serverError(e);
		}
	}

	private void ensureUniqueEmailAddress(String emailAddress, ObjectCreationException exceptionHandler) {
		if (!daoFactory.getUserDao().isUniqueEmailAddress(emailAddress)) {
			exceptionHandler.addError(UserErrorCode.DUPLICATE_EMAIL, EMAIL_ADDRESS);
		}
	}

	private void ensureUniqueLoginNameInDomain(String loginName, String domainName,
			ObjectCreationException exceptionHandler) {
		if (!daoFactory.getUserDao().isUniqueLoginNameInDomain(loginName, domainName)) {
			exceptionHandler.addError(UserErrorCode.DUPLICATE_LOGIN_NAME, LOGIN_NAME);
		}
	}

	private void validateChangeInUniqueEmail(User oldUser, User newUser, ObjectCreationException exceptionHandler) {
		if (!oldUser.getEmailAddress().equals(newUser.getEmailAddress())) {
			ensureUniqueEmailAddress(newUser.getEmailAddress(), exceptionHandler);
		}
	}

}