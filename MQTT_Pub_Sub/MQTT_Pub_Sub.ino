
#include <ESP8266WiFi.h> // Enables the ESP8266 to connect to the local network (via WiFi)
#include <PubSubClient.h> // Allows us to connect to, and publish to the MQTT broker
#include <Wire.h>
#include <SPI.h>
#include <Adafruit_LSM9DS1.h>
#include <Adafruit_Sensor.h>  // not used in this demo but required!

// i2c
Adafruit_LSM9DS1 lsm = Adafruit_LSM9DS1();
float heading, headingDegrees, headingFiltered, declination;

#define LSM9DS1_SCK A5
#define LSM9DS1_MISO 12
#define LSM9DS1_MOSI A4
#define LSM9DS1_XGCS 6
#define LSM9DS1_MCS 5

// You can also use software SPI
//Adafruit_LSM9DS1 lsm = Adafruit_LSM9DS1(LSM9DS1_SCK, LSM9DS1_MISO, LSM9DS1_MOSI, LSM9DS1_XGCS, LSM9DS1_MCS);
// Or hardware SPI! In this case, only CS pins are passed in
//Adafruit_LSM9DS1 lsm = Adafruit_LSM9DS1(LSM9DS1_XGCS, LSM9DS1_MCS);





const char* ssid = "Shafiee Lab Team";
const char* wifi_password = "Hslab2017";

const char* mqtt_server = "192.168.43.4";
const char* mqtt_topic_pub_vib = "vib";
const char* mqtt_topic_pub_mag = "mag";

const char* mqtt_topic_pub_acc = "acc";

const char* mqtt_topic_sub = "nodemcu";
const char* clientID = "ESP8266_1";


void setupSensor()
{
  // 1.) Set the accelerometer range
  lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_2G);
  //lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_4G);
  //lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_8G);
  //lsm.setupAccel(lsm.LSM9DS1_ACCELRANGE_16G);
  
  // 2.) Set the magnetometer sensitivity
  lsm.setupMag(lsm.LSM9DS1_MAGGAIN_4GAUSS);
  //lsm.setupMag(lsm.LSM9DS1_MAGGAIN_8GAUSS);
  //lsm.setupMag(lsm.LSM9DS1_MAGGAIN_12GAUSS);
//  lsm.setupMag(lsm.LSM9DS1_MAGGAIN_16GAUSS);

  // 3.) Setup the gyroscope
  lsm.setupGyro(lsm.LSM9DS1_GYROSCALE_245DPS);
  //lsm.setupGyro(lsm.LSM9DS1_GYROSCALE_500DPS);
  //lsm.setupGyro(lsm.LSM9DS1_GYROSCALE_2000DPS);
}



void ReceivedMessage(char* topic, byte* payload, unsigned int length) {

  
//  Serial.println(String(payload));
//  

for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    int pin = 6+(int)payload[i];
digitalWrite(pin,1);

    

    
  }
//  Serial.println();

  
//Serial.println((char)payload[0]);


//
//              String _payload = "";
//              Serial.println(_payload);
//
//            
//
//              int motor0 = (int)_payload[0];
//              int motor1 = (int)_payload[1];
//              int motor2 = (int)_payload[2];
//              int motor3 = (int)_payload[3];
//              int motor4 = (int)_payload[4];

}





// Initialise the WiFi and MQTT Client objects
WiFiClient wifiClient;
PubSubClient client(mqtt_server, 1883, wifiClient); // 1883 is the listener port for the Broker

void setup() {

pinMode(D6,OUTPUT);
pinMode(D7,OUTPUT);
pinMode(D8,OUTPUT);


pinMode(A0,INPUT);
Serial.begin(115200);
  // Try to initialise and warn if we couldn't detect the chip
  if (!lsm.begin())
  {
    Serial.println("Oops ... unable to initialize the LSM9DS1. Check your wiring!");
    while (1);
  }
  Serial.println("Found LSM9DS1 9DOF");

  // helper to just set the default scaling we want, see above!
  setupSensor();


  
  

  Serial.print("Connecting to ");
  Serial.println(ssid);

  // Connect to the WiFi
  WiFi.begin(ssid, wifi_password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("WiFi connected");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());


  client.setCallback(ReceivedMessage);


  
}

bool Connect() {
  if (client.connect(clientID)) {
      client.subscribe(mqtt_topic_sub);
          Serial.println("Connected to MQTT Broker!");

      return true;
    }
    else {
          Serial.println("Connection to MQTT Broker failed...");

      return false;
  }
}



void loop() {
  
 
  if (!client.connected()) {
    Connect();
  }

 sendpiezo();
 
sendIMU();

//  client.publish(mqtt_topic_pub_mag, "Button pressed!");
  client.loop();

 
}

void sendIMU(){
  
   lsm.read();  /* ask it to read in the data */ 

  /* Get a new sensor event */ 
  sensors_event_t a, m, g, temp;

  lsm.getEvent(&a, &m, &g, &temp); 


    float heading = atan2(m.magnetic.y, m.magnetic.x);
  if(heading == 0){
    heading += 2*PI;}

//  declination = -0.24870941840919197; 
//  heading += declination;


    
  if(heading <0) heading += 2*PI;
  // Correcting due to the addition of the declination angle
  if(heading > 2*PI)heading -= 2*PI;
  headingDegrees = heading * 180/PI; // The heading in Degrees unit

//      Serial.println("headingDegrees");   Serial.print(headingDegrees);

  // Smoothing the output angle / Low pass filter 
//  headingFiltered = headingFiltered*0.85 + headingDegrees*0.15;


  headingFiltered =headingDegrees;
  //Sending the heading value through the Serial Port to Processing IDE
//      Serial.print("headingFiltered");  
//  Serial.println(String(headingFiltered-30));
//  Serial.println();
headingFiltered= headingFiltered+30;
  if((headingFiltered)>359){
    headingFiltered = headingFiltered-360;
    }


    Serial.println(headingFiltered);
String head= String(headingFiltered);

int str_len = head.length() + 1; 

char char_array[str_len];
head.toCharArray(char_array, str_len);

  client.publish(mqtt_topic_pub_mag, char_array);
  
  }

void motors(String pat){
  
  
  
  }

void sendpiezo(){




int a0 = analogRead(A0);

String pie= String(a0);

int str_len = pie.length() + 1; 

char char_array_pi[str_len];
pie.toCharArray(char_array_pi, str_len);



client.publish(mqtt_topic_pub_vib, char_array_pi);
if(a0>100){
//  Serial.print("pizeo:");
//Serial.println(a0);

  

}
  }
