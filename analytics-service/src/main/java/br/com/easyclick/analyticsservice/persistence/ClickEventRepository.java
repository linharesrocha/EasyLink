package br.com.easyclick.analyticsservice.persistence;

import br.com.easyclick.analyticsservice.domain.ClickEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClickEventRepository extends MongoRepository<ClickEvent, String> {
}
