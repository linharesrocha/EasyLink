package br.com.easylink.easylinkservice.application.ports;

public interface QrCodeGeneratorPort {
    byte[] generate(String text, int width, int height) throws Exception;
}
