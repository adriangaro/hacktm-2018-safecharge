#include <BluetoothSerial.h>
#include <EEPROM.h>
#include <SSD1306.h>
#include <HTTPClient.h>
#define header "HTTP/1.1 200 OK\n"
#define ON 0
#define OFF !ON

void  write_eeprom(void);
void setup_eeprom(void);

SSD1306 display(0x3c, 5, 18);
WiFiServer server(80);
BluetoothSerial SerialBT;

String device_name;
const int relay = 32;
int percent, state, conn;

void setup() {
  Serial.begin(9600);
  delay(1000);
  Serial.println("btdvfcbtgdfvfc\n");
  pinMode(relay, OUTPUT);
  SerialBT.begin("SafeCharger");
  display.init();
  display.flipScreenVertically();
  state = 0;
  digitalWrite(relay, OFF);
  //  WiFi.mode(WIFI_STA);
  //  if (ssid_len) {
  //    WiFi.begin(ssid_name, pass_name);
  //    while (millis() < 15000 and WiFi.status() != WL_CONNECTED)
  //      delay(500);
  //    server.begin();
  //  }
  Serial.println("start");
}

void loop() {
  if (SerialBT.available()) {
    conn = 1;
    char msg[200] = {0};
    char sep[20] = ",";
    int msg_len = 0;
    while (SerialBT.available()) {
      msg[msg_len++] = SerialBT.read();
    }
    Serial.println(msg);
    //--------------------------------------------------------------------------------------------------------------------
    if (strstr(msg, "turnOn")) {
      Serial.println("ON");
      digitalWrite(relay, ON);
      state = 1;
    }
    if (strstr(msg, "turnOff")) {
      Serial.println("OFF");
      digitalWrite(relay, OFF);
      state = 0;
    }
    if (msg[0] == '*') {
      strcpy(msg, msg + 1);
      char *p = strtok(msg, sep);
      percent = atoi(p);
      if (percent < 10)
        strcpy(msg, msg + 2);
      else if (percent < 100)
        strcpy(msg, msg + 3);
      else
        strcpy(msg, msg + 4);
      device_name = msg;
    }
  }
  draw();
  delay(100);
  //  WiFiClient Client = server.available();
  //  if (!Client) {
  //    return;
  //  }
  //  while (!Client.available()) {
  //    delay(1);
  //  }
  //  String request = Client.readStringUntil('\r');
  //  Client.flush();
  //  Client.println(header);
  //  Client.flush();
}

bool keep_alive;

void draw() {
  display.clear();
  display.setColor(WHITE);
  display.setTextAlignment(TEXT_ALIGN_CENTER);
  if (!conn) {
    display.setFont(Crushed_50);
    display.drawString(64, 10, "NC");
    display.display();
    return;
  }
  if (device_name.length() > 15)
    display.setFont(Lato_Regular_12);
  else
    display.setFont(Lato_Regular_14);
  display.drawString(64, 0, device_name);
  display.setFont(SansSerif_plain_35);
  String to_draw;
  if (state)
    to_draw += '*';
  to_draw += String(percent);
  to_draw += '%';
  display.drawString(64, 17, to_draw);
  display.display();
}
