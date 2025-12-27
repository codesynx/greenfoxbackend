package com.greenfox.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration for Google Cloud credentials.
 * Credentials are hardcoded directly in this configuration to avoid file management issues on the server.
 * This config provides a GoogleCredentials bean for dependency injection.
 */
@Slf4j
@Configuration
public class GoogleCloudConfig {

    /**
     * Hardcoded GCP service account credentials JSON.
     * This eliminates the need for external credentials files or environment variables.
     */
    private static final String GCP_CREDENTIALS_JSON = """
            {
              "type": "service_account",
              "project_id": "agile-outlook-463407-t4",
              "private_key_id": "1ce8216ae413c932e5afcb6d631aafb59b942ce6",
              "private_key": "-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCquLsG3d5FVFPw\\nloQLjSUDYzC8deXtO18wdrHNukdbRi9uCquv1uHyFk++lxYAJLDIiLFTJDrdOFZZ\\nqx5jWu97zMNErrL7UuVehBlMaPzQyBMJ2h97jkqkHSb5/uhxsjcHWSiAt6XaCtV7\\n+PzWRL3/995zROXq8Mo28oLiUDlOAgEIZEDhN0sPq9VcDK+e4AYCXCjphWK/24ur\\nVWaurH99gCbfr1DRM4GhzhBfLLCZjaoX7hdQ8un/dWZIi0Nf67C3k2sGDXlMdhis\\nZDweJHkil6CrwzcbYd4e5Z5jvDHyhZGEpIwmrZWtqwP9MYOWOywRlYpWLVyWG3da\\nkyDOYJMXAgMBAAECggEATIsBs8zzFKN/9Q0PC/zO/QtSdRTpL2mgvqJhCsu7pYU8\\ny7o2CuMtr7RoMZwc7ZiFuIts8cl7KOdS8+YgfExvtlJytE8diUg79ZoFm7gwrc0e\\nmBq3ZdbAyX8WlBzBjARq3r0PR1LPpgiYefqDJfBxPZwdHRJlgWZPS76Eh0KPxtSs\\nD/U26RlkZhjqQFv2779rpOG2olI5+tw66TlMIryyOQ70qpmSE8KTTWGtxM5pIljx\\nThCdg8Z82sFHnDMMbHmnq32+4sXj0QSqBahAA1/eSFk2glZJVp0i6JVFrMseXiZX\\n5BfoB1uEV0hsFPkM2I1ZObJPGtvBk2JEKN5bVnS+BQKBgQDjartvKkobtSMjM0cA\\nzXBJBx0yDh26NTk0l4ZBVr2vSAeVdyKaMcJ1VCIgyauas32bViA09d0sa3q2DIXT\\niY3CpSdz7CCkoK5RtyQH+f6U/rdg4lxNNbvWB4Jux4gp/wkB+7b6HlGfwGZfSy1S\\nScJd0wdOd2M3sAd/rmlQvdYU/QKBgQDALctmVhBmgMhshNV+RniVC67expefKFXA\\nbYOoo34MpzEns2DPaBlqA5llBuPNNYguuQFkHDC2gX6qI+0Af1V/3i3Bb++Eii5o\\nmIpB4kZZBgBUUSa5RjDJ/NbagdQN3U6wj0bYVbhn6Zxq1UP24IPjxJPmDUwns/Gr\\nu/sCEibuowKBgQCsv+vnctFeoMczOwVpl6T+5FeKKWztgPaWe6+xPGnUlzujjx8K\\n0Fet9p4CGA+x8Tyt3cuTT1yWHILXfUW/PdMgk4IXrbvSMniYfCzznjSEC8VS5EBH\\nKlUoLQYojothk2jNR99judo5Jtbc9mdUBdGOofn/4fLNX3siw2MewUW5JQKBgB1J\\n3WnnS/CTR/hh9jecfdwZP7lsAsea2hbNmfclk5xSDsfCKsrxTo49pi8jcaxgsDSO\\ntYuQUExASBGeEAy3W0x1i/ujF5nXhWMjfE62MYp3w60WARTTgbQG+KesiajjRY7b\\nBy0JQ2Vy0QfnIMiLRw57cx7of+a9IePObl7ObWiJAoGBAKfwXPlssLmJiTSyO2Rj\\nqsWU7TxeYCYbtDMI4W0tAm5/EHh/UWXaC00bUB75zHifVs3+UWee0BJbQgZ6CDPT\\nwd3ieM2X7K1EuMB+1dRC8A0PoOKIFgVdMTzR5mzU1SPO7VW9Kzf785hgIdByNsLJ\\nLJo+bYu7MHD/LZlri9vcTCHd\\n-----END PRIVATE KEY-----\\n",
              "client_email": "millenium@agile-outlook-463407-t4.iam.gserviceaccount.com",
              "client_id": "115744708747698320137",
              "auth_uri": "https://accounts.google.com/o/oauth2/auth",
              "token_uri": "https://oauth2.googleapis.com/token",
              "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
              "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/millenium%40agile-outlook-463407-t4.iam.gserviceaccount.com",
              "universe_domain": "googleapis.com"
            }
            """;

    /**
     * Provide Google Cloud credentials bean using hardcoded service account JSON.
     * Credentials are loaded from the static JSON string above using ByteArrayInputStream.
     */
    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(GCP_CREDENTIALS_JSON.getBytes(StandardCharsets.UTF_8))
            );
            log.info("✓ Google Cloud credentials bean created successfully from hardcoded JSON");
            return credentials;
        } catch (IOException e) {
            log.error("Failed to load Google Cloud credentials from hardcoded JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to load GCP credentials", e);
        }
    }
}
