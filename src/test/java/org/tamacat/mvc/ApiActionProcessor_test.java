package org.tamacat.mvc;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tamacat.mvc.oauth.provider.jwt.JsonWebToken;

public class ApiActionProcessor_test {

	@Test
	public void testIntrospect() {

		ApiActionProcessor api = new ApiActionProcessor();
		String accessToken = "97ce1c5081e5ab81de6b2fe4172d7fdf097ad0bdb17202bcaad9c462a3be543c";
		JsonWebToken jwt = api.introspect(accessToken);
		System.out.println("tid="+jwt.getTid());
		System.out.println("client_id="+jwt.getClientId());
		assertEquals("7f5a3914-5699-414f-91ac-c90d7753715a", jwt.getClientId());
	}
}
