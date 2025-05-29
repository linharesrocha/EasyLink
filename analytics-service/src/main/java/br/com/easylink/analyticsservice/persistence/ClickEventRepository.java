package br.com.easylink.analyticsservice.persistence;

import br.com.easylink.analyticsservice.domain.ClickEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClickEventRepository extends MongoRepository<ClickEvent, String> {
}
