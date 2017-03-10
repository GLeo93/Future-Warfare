#include <IRremote.h>          //Library for the IR Receiver
#include <SoftwareSerial.h>    //Library for theBluetooth

SoftwareSerial BT(10, 9);   
IRrecv irrecv(11);             // Instance of class IRrecv used to receive
decode_results results;        // Buffer containing received signal il segnale ricevuto

int first_led=5;               //Pin of first Led
int second_led=3;              //Pin of second led 
int piezo=8;                   //Pin of Piezo
int trigger=4;                 //Pin of trigger
char mode;                     //Used to deal with differents Android Input
bool stop = false;


void setup() {
    BT.begin(9600);            //Start Bluethoot
    Serial.begin(9600);        //Start Serial
    inizializeEnvironment();   //Funcion used to inizialize the whole Environment
    delay(200);
}


void loop() {
    //*******************************BLUETOOTH*****************************//
    if (BT.available()){                            //Waiting for bluetooth connection
      mode=(BT.read());                             //Bluetooth input
      switch(mode){
        case '1':  
          stop = false;
          break;
        case '2':  
          stop = true;
          break;
      }
    }
    //**********************************GAME********************************//
    if(!stop){
      if (irrecv.decode(&results)) {                              //If signal is present on buffer, player has been hit
        dead();                                                   //Function dead() is called to manage hit
      }
      irrecv.resume();                                            //Refresh buffer and start receiving the next value
      
      int shot= digitalRead(trigger);                             
      if(shot==HIGH)
        shoot();                                                  //Function shoot is called to manage shoot 
    }
    delay(40);
}

void shoot(){                                                   
    BT.println("shot");   
    tone(piezo,250);                                            //Start piezo 
    digitalWrite(second_led,HIGH);                              //Led high       
    delay(300);
    noTone(piezo);                                              //Stop piezo after 300 ms
    digitalWrite(second_led,LOW);                               //Led low
}

void dead(){
    BT.println("hit");   
    tone(piezo,2000,2000);                                    //Start piezo 
      
    for(int i=0;i<5;i++){                                     //Start animation of led
      digitalWrite(first_led,LOW); 
      delay(300);
      digitalWrite(first_led,HIGH);
      delay(300);
    }
    noTone(piezo);                                            //Stop piezo
    digitalWrite(first_led,LOW);                              //Stop animation
}  

void inizializeEnvironment(){
    irrecv.enableIRIn();                                    // Start the receiver
    tone(piezo,1000);                                         //Start piezo
    pinMode(first_led,OUTPUT);                                //Set the right pin
    pinMode(second_led,OUTPUT);                               //Set the right pin
    pinMode(trigger,INPUT);                                   //Set the right pin
    
    digitalWrite(first_led,HIGH);                             //Start animation
    digitalWrite(second_led,HIGH);
    delay(3000);
    noTone(piezo);
    
    for(int i=0; i<3; i++){
      digitalWrite(first_led,LOW);
      digitalWrite(second_led,LOW);
      delay(300);
      digitalWrite(first_led,HIGH);   
      digitalWrite(second_led,HIGH);
      delay(300);
    }
    digitalWrite(first_led,LOW);
    digitalWrite(second_led,LOW);                             //Stop Animation
}
