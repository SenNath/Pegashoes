import time
import RPi.GPIO as GPIO
import paho.mqtt.client as mqtt
from datetime import datetime
import sched, time
import googlemaps
now = datetime.now()

gmaps = googlemaps.Client(key='AIzaSyAQrJGXhzUZijjRFTiCLRiOJiIlkVnWaLE')

directions_result = gmaps.directions('Rubin Campus Center, 100 Institute Rd, Worcester, MA ', 'Worcester Art Museum, 55 Salisbury St, Worcester, MA', mode="walking", departure_time=now)

def loc_job():
   
   loc=open('location.txt', 'r') 
   line=loc.readline()

   #print(line)
   points = line.split(",")
   #print(   points)
   new_lat=points[0]
   new_long=points[1]
   loc.close()
   
   return new_long,new_lat








#gmaps = googlemaps.Client(key='AIzaSyAQrJGXhzUZijjRFTiCLRiOJiIlkVnWaLE')



# Setup callback functions that are called when MQTT events happen like
def on_connect(client, userdata, flags, rc):
   print("Connected with result code " + str(rc))
   # Subscribing in on_connect() means that if we lose the connection and
   # reconnect then subscriptions will be renewed.
 #  client.subscribe("pi")
   client.subscribe("mag")
   client.subscribe("vib")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
#   print(msg.topic+" "+str( msg.payload))
    pass
   # Check if this is a message for the Pi LED.

# Create MQTT client and connect to localhost, i.e. the Raspberry Pi running
# this script and the MQTT server.
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect('192.168.43.4', 1883, 60)
# Connect to the MQTT server and process messages in a background thread.
client.loop_start()
# Main loop to listen for button presses.
print('Script is running, press Ctrl-C to quit...')



i=1
for leg in directions_result[0]['legs']:
   for step in leg['steps']:
      if i==1:
         print('Step'+ str(i))
         print(step[ 'html_instructions'])
         print(step[ 'distance'])
         print(step[ 'duration'])

         client.publish('nodemcu', '100')


      else:
         if 'maneuver' in step.keys():
            print('Step'+ str(i))
            print('     ') 
            start_loc=step['start_location']
            print("starts at"+str(start_loc))
            current_long,current_lat=loc_job()

            client.publish('nodemcu', '100')
            time.sleep(2)
            client.publish('nodemcu', '000')

            while str(current_long)!=str(start_loc['lng']):
               # client.publish('nodemcu', '100')

               #time.sleep(1)
               current_long,current_lat=loc_job()
            maneuver=step['maneuver']
            end_loc=step['end_location']
            distance=step['distance']
            duration=step['duration']
            if maneuver in ('turn-right','turn-slight-right','turn-sharp-right','keep-right'):
               maneuver='turn-right'
               client.publish('nodemcu', '001')
               time.sleep(2)
               client.publish('nodemcu', '000')

            else:
               maneuver='turn-left'
               client.publish('nodemcu', '010')
               time.sleep(2)
               client.publish('nodemcu', '000')

            print(maneuver)
            print("ends at"+str(end_loc))

      i=i+1


if end_loc['lng']==current_long and end_loc['lat']==current_lat:
      maneuver='end'
      print(maneuver)


      client.publish('nodemcu', '111')
      time.sleep(2)
      client.publish('nodemcu', '000')





while True:
   # Look for a change from high to low value on the button input to
   # signal a button press.
   #button_first = GPIO.input(BUTTON_PIN)
   time.sleep(0.02)  # Delay for about 20 milliseconds to debounce.
   #button_second = GPIO.input(BUTTON_PIN)
#   if button_first == GPIO.HIGH and button_second == GPIO.LOW:
#   print('Button pressed!')
       # Send a toggle message to the ESP8266 LED topic.
   client.publish('nodemcu', 'TOGGLE')
