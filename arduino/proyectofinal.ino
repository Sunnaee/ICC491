#include <Wire.h>
#include <WiFi.h>
#include <HTTPClient.h>

#define SENSOR_PIN WB_IO6 // PIR sensor conectado a WB_IO6
#define WIFI_SSID "Sabri"
#define WIFI_PASSWORD "*"
#define SERVER_URL "http://54.210.65.125:8081/estacionamiento"

int gCurrentStatus = 0;
int gLastStatus = 0;

void setup() {
   pinMode(SENSOR_PIN, INPUT);
   Serial.begin(115200);
   WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

   while (WiFi.status() != WL_CONNECTED) {
      delay(1000);
      Serial.println("Conectando a WiFi...");
   }
   Serial.println("WiFi conectado.");
}

void loop() {
   gCurrentStatus = digitalRead(SENSOR_PIN);
   if (gLastStatus != gCurrentStatus) {
      if (gCurrentStatus == 0) {
         Serial.println("Estacionamiento ocupado.");
         enviarDatosServidor(0); // 0 indica ocupado
      } else {
         Serial.println("Estacionamiento libre.");
         enviarDatosServidor(1); // 1 indica libre
      }
      gLastStatus = gCurrentStatus;
   }
   delay(1000); // Medición cada 1 segundo
}

void enviarDatosServidor(int status) {
   if (WiFi.status() == WL_CONNECTED) {
      HTTPClient http;
      http.begin(SERVER_URL);
      http.addHeader("Content-Type", "application/json");

      String payload = "{\"sensor\": \"PIR\", \"status\": " + String(status) + "}";
      int httpResponseCode = http.POST(payload);
      if (httpResponseCode > 0) {
         Serial.println("Datos enviados exitosamente.");
      } else {
         Serial.println("Error enviando datos.");
      }
      http.end();
   } else {
      Serial.println("WiFi desconectado.");
   }
}
