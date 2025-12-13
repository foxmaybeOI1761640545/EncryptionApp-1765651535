package com.encryption.ui;

import com.encryption.crypto.CryptoUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;

import java.security.GeneralSecurityException;
import java.util.Arrays;

public class MainController {

    @FXML private PasswordField passwordField;
    @FXML private TextArea plainTextArea;
    @FXML private TextArea cipherTextArea;
    @FXML private TextArea decryptedTextArea;
    @FXML private Label statusLabel;

    @FXML
    private void onEncrypt() {
        decryptedTextArea.clear();

        String pwd = passwordField.getText();
        char[] password = (pwd == null) ? new char[0] : pwd.toCharArray();

        try {
            String plain = plainTextArea.getText();
            String cipher = CryptoUtil.encryptToBase64(plain, password);
            cipherTextArea.setText(cipher);
            setStatus("✅ 加密成功（已输出 Base64 密文）。");
        } catch (GeneralSecurityException e) {
            setStatus("❌ 加密失败：" + safeMsg(e));
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @FXML
    private void onDecrypt() {
        decryptedTextArea.clear();

        String pwd = passwordField.getText();
        char[] password = (pwd == null) ? new char[0] : pwd.toCharArray();

        try {
            String cipher = cipherTextArea.getText();
            String plain = CryptoUtil.decryptFromBase64(cipher, password);
            decryptedTextArea.setText(plain);
            setStatus("✅ 解密成功（已恢复原字符串）。");
        } catch (GeneralSecurityException e) {
            setStatus("❌ 解密失败：" + safeMsg(e));
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    @FXML
    private void onClear() {
        plainTextArea.clear();
        cipherTextArea.clear();
        decryptedTextArea.clear();
        setStatus("已清空。");
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    private String safeMsg(Exception e) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? e.getClass().getSimpleName() : m;
    }
}
