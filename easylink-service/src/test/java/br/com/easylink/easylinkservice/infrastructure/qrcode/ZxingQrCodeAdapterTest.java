package br.com.easylink.easylinkservice.infrastructure.qrcode;

import com.google.zxing.WriterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ZxingQrCodeAdapterTest {

    private ZxingQrCodeAdapter qrCodeAdapter;

    @BeforeEach
    void setUp() {
        qrCodeAdapter = new ZxingQrCodeAdapter();
    }

    @Test
    @DisplayName("Deve gerar um array de bytes não nulo e não vazio para texto válido")
    void generate_comTextoValido_deveRetornarArrayDeBytesNaoNuloENaoVazio() {
        String text = "https://www.example.com";
        int width = 250;
        int height = 250;
        byte[] qrCodeBytes = null;

        try {
            qrCodeBytes = qrCodeAdapter.generate(text, width, height);
        } catch (WriterException | IOException e) {
            fail("Não deveria lançar exceção para input válido", e);
        }

        assertNotNull(qrCodeBytes);
        assertTrue(qrCodeBytes.length > 0);
    }

    @Test
    @DisplayName("Deve gerar QR Code válido quando largura é zero (usará tamanho padrão)")
    void generate_comLarguraZero_deveGerarImagemValida() {
        String text = "test";
        int width = 0;
        int height = 250;
        byte[] qrCodeBytes = null;

        try {
            qrCodeBytes = qrCodeAdapter.generate(text, width, height);
        } catch (WriterException | IOException e) {
            fail("Não deveria lançar exceção para largura zero, ZXing deve usar um padrão.", e);
        }
        assertNotNull(qrCodeBytes);
        assertTrue(qrCodeBytes.length > 0);
    }

    @Test
    @DisplayName("Deve gerar QR Code válido quando altura é zero (usará tamanho padrão)")
    void generate_comAlturaZero_deveGerarImagemValida() {
        String text = "test";
        int width = 250;
        int height = 0;
        byte[] qrCodeBytes = null;

        try {
            qrCodeBytes = qrCodeAdapter.generate(text, width, height);
        } catch (WriterException | IOException e) {
            fail("Não deveria lançar exceção para altura zero, ZXing deve usar um padrão.", e);
        }
        assertNotNull(qrCodeBytes);
        assertTrue(qrCodeBytes.length > 0);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para largura negativa")
    void generate_comLarguraNegativa_deveLancarIllegalArgumentException() {
        String text = "test";
        int width = -1;
        int height = 250;

        assertThrows(IllegalArgumentException.class, () -> {
            qrCodeAdapter.generate(text, width, height);
        });
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para altura negativa")
    void generate_comAlturaNegativa_deveLancarIllegalArgumentException() {
        String text = "test";
        int width = 250;
        int height = -1;

        assertThrows(IllegalArgumentException.class, () -> {
            qrCodeAdapter.generate(text, width, height);
        });
    }
}