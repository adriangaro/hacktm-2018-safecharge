#include <BluetoothSerial.h>
#include <EEPROM.h>
#include <HTTPClient.h>
#define header "HTTP/1.1 200 OK\n"
#define ON 0
#define OFF !ON

void  write_eeprom(void);
void setup_eeprom(void);

WiFiServer server(80);
BluetoothSerial SerialBT;

char module_name[95] = {'N', 'e', 'w', 'S', 'a', 'f', 'e', 'C', 'h', 'a', 'r', 'g', 'e'};
char ssid_name[95], pass_name[95];
int name_len, ssid_len, pass_len;
int relay = 32;

void setup() {
  Serial.begin(9600);
  delay(1000);
  Serial.println("btdvfcbtgdfvfc\n");
  setup_eeprom();
  pinMode(relay, OUTPUT);
  Serial.println("done setup");
  SerialBT.begin(module_name);
  Serial.println("bt on");
  WiFi.mode(WIFI_STA);
  Serial.println("wifi on");
  if (ssid_len) {
    WiFi.begin(ssid_name, pass_name);
    while (millis() < 15000 and WiFi.status() != WL_CONNECTED)
      delay(500);
    server.begin();
  }
  Serial.println("start");
}

void loop() {
  //String ip = WiFi.localIP().toString();
  if (SerialBT.available()) {
    char msg[200] = {0};
    int msg_len = 0;
    while (SerialBT.available()) {
      msg[msg_len++] = SerialBT.read();
    }
    Serial.println(msg);
    if (strstr(msg, "turnOn")) {
      Serial.println("ON");
      digitalWrite(relay, ON);
    }
    if (strstr(msg, "turnOff")) {
      Serial.println("OFF");
      digitalWrite(relay, OFF);
    }
    if (strstr(msg, "len1=")) {
      int i = ((int)strstr(msg, "len1=") - (int) * msg);
      i += 5;
      int len1 = msg[i] - '0';
      i++;
      while (msg[i] <= '9' and msg[i] >= '0')
        len1 = len1 * 10 + msg[i++] - '0';
      i += 6;
      for (int j = i; j <= i + len1; j++)
        ssid_name[j - i] = msg[j];
      ssid_len = len1;
      i += len1;
      i++;
      i += 5;
      int len2 = msg[i] - '0';
      i++;
      while (msg[i] <= '9' and msg[i] >= '0')
        len2 = len2 * 10 + msg[i++] - '0';
      i += 6;
      for (int j = i; j <= i + len2; j++)
        pass_name[j - i] = msg[j];
      pass_len = len2;
      write_eeprom();
      ESP.restart();
    }
    if (strstr(msg, "name=")) {
      int i = ((int)strstr(msg, "name=") - (int) * msg);
      i += 5;
      int str_len = strlen(msg);
      while (msg[str_len] == '\0' or msg[str_len] == '\r' or msg[str_len] == '\n')
        str_len--;
      str_len++;
      for (int j = i; j < str_len; j++) {
        module_name[j - i] = msg[j];
      }
      name_len = str_len - i;
      write_eeprom();
      ESP.restart();
    }
  }
  WiFiClient Client = server.available();
  if (!Client) {
    return;
  }
  while (!Client.available()) {
    delay(1);
  }
  String request = Client.readStringUntil('\r');
  Client.flush();
  Client.println(header);
  Client.flush();
}

void write_eeprom() {
  EEPROM.begin(300);
  EEPROM.write(0, (byte)name_len);
  for (int i = 0; i < name_len; i++)
    EEPROM.write(i + 1, (byte)module_name[i]);
  EEPROM.write(100, (byte)ssid_len);
  for (int i = 0; i < ssid_len; i++)
    EEPROM.write(i + 101, (byte)ssid_name[i]);
  EEPROM.write(200, (byte)ssid_len);
  for (int i = 0; i < pass_len; i++)
    EEPROM.write(i + 201, (byte)pass_name[i]);
  EEPROM.commit();
}

void setup_eeprom() {
  EEPROM.begin(300);
  name_len = EEPROM.read(0);
  if (name_len == 255 or !name_len) {
    EEPROM.end();
    EEPROM.begin(300);
    name_len = 13;
    EEPROM.write(0, (byte)name_len);
    for (int i = 0; i < name_len; i++)
      EEPROM.write(i + 1, (byte)module_name[i]);
    EEPROM.commit();
    EEPROM.begin(300);
  } else {
    for (int i = 0; i < name_len; i++)
      module_name[i] = (char)EEPROM.read(i + 1);
  }
  ssid_len = EEPROM.read(100);
  if (ssid_len == 255) {
    ssid_len = 0;
    EEPROM.end();
    EEPROM.begin(300);
    EEPROM.write(100, (byte)ssid_len);
    EEPROM.commit();
    EEPROM.begin(300);
  } else {
    for (int i = 0; i < ssid_len; i++)
      ssid_name[i] = (char)EEPROM.read(i + 101);
  }
  pass_len = EEPROM.read(200);
  if (pass_len == 255) {
    pass_len = 0;
    EEPROM.end();
    EEPROM.begin(300);
    EEPROM.write(200, (byte)pass_len);
    EEPROM.commit();
    EEPROM.begin(300);
  } else {
    for (int i = 0; i < pass_len; i++)
      pass_name[i] = (char)EEPROM.read(i + 201);
  }
  EEPROM.commit();
}
