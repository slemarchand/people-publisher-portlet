package com.slemarchand.peoplepublisher.model;

import com.liferay.portal.model.User;

import java.util.List;
public class PeopleResults {

	public int getTotal() {
		return _total;
	}

	public List<User> getUsers() {
		return _users;
	}

	public void setTotal(int total) {
		this._total = total;
	}

	public void setUsers(List<User> users) {
		this._users = users;
	}

	private int _total;
	private List<User> _users;

}