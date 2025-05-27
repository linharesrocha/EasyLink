package br.com.easylink.easylinkservice.application;

import br.com.easylink.easylinkservice.application.ports.UpdateUrlUseCase;
import br.com.easylink.easylinkservice.application.ports.UrlMappingRepositoryPort;
import br.com.easylink.easylinkservice.domain.UrlMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUrlUseCaseImpl implements UpdateUrlUseCase {

    private final UrlMappingRepositoryPort urlMappingRepositoryPort;

    @Override
    @Transactional
    public UrlMapping updateUrl(String shortKey, String newOriginalUrl, String ownerUsername) {
        UrlMapping urlMapping = urlMappingRepositoryPort.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("Link não encontrado com a chave: " + shortKey));

        if(!urlMapping.getOwnerUsername().equals(ownerUsername)) {
            throw new RuntimeException("Usuário não autorizado a editar esse link.");
        }

        urlMapping.setOriginalUrl(newOriginalUrl);

        return urlMappingRepositoryPort.save(urlMapping);
    }
}
