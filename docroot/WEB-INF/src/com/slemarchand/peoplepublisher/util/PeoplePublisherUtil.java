package com.slemarchand.peoplepublisher.util;

public class PeoplePublisherUtil {

	private static PeoplePublisher _peoplePublisher = new PeoplePublisherImpl();

	public static PeoplePublisher getPeoplePublisher() {
		return _peoplePublisher;
	}
}