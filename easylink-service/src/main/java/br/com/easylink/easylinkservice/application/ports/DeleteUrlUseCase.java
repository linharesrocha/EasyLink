package br.com.easylink.easylinkservice.application.ports;

public interface DeleteUrlUseCase {
    void deleteUrl(String shortKey, String ownerUsername);
}
