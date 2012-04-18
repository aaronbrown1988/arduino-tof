/*

Time of flight/ Bounce counter sketch!
 
 */

int sensorPin = A0;    // select the input pin for the potentiometer
int ledPin = 13;      // select the pin for the LED
int sensorValue = 0;  // variable to store the value coming from the sensor

int midpoint = 932;
int high;
int value;
int broken = 1;
void setup() {
  // declare the ledPin as an OUTPUT:
  pinMode(ledPin, OUTPUT);
  Serial.begin(115200);
  high = 0;
}

void loop() {
  unsigned long  time = 0;
  
  value = analogRead(sensorPin);
  //Serial.println(value);
  
  if(value > midpoint){
    high++;
    
  }else{
    high = 0;
  }
  
  if (broken == 1 && high == 0) {
    digitalWrite(ledPin, HIGH);
    broken = 0;
    Serial.print("1 ");
    time = millis();
    Serial.print(time);
    Serial.println(" 1");
  }
  
  if(high > 2 && broken == 0){
    digitalWrite(ledPin, LOW);
    Serial.print("1 ");
    time = millis();
    Serial.print(time);
    Serial.println(" 0");
    broken = 1;
   // Serial.println("Beam Broken");
  }
  if (Serial.available() > 0 ) {
   handleData();
  }
  delayMicroseconds(10);
};



void handleData() {
 
 // WOrk out if hand shake or command or what
 
 
}