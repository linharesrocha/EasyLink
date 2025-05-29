package br.com.easylink.easylinkservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnableDiscoveryClient
@ActiveProfiles("test")
class EasylinkServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
