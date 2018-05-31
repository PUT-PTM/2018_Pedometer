# Pedometer

<h2>Overview:</h2>
The main goal of this project was to create a pedometer based on a accelerometer.
The output is presented on a android app.

<h2>Description:</h2>
Microcontroller used in this project is STM32F4DISCOVERY. To capture the steps walked the built in  LIS3DSH accelerometer was used. </br>
To communicate with android device we used Bluetooth adapter HC-06. The communication is made through UART. </br>
The android app displays amount of walked steps, meters walked, current and average velocity, calories burned and current mode (standing, walking, running).
We also implemented history of past walks that is saved in a SQLite database. </br>

<h2>Tools:</h2>

- Android Studio 3.1
- System Workbench for STM32
- STM Studio

<h2>How to run:</h2>

<b>Connections:</b><br>
- HC-06 RX  -> STM32 PC10<br>
- HC-06 TX  -> STM32 PC11<br>
- HC-06 VCC -> STM32 5V<br>
- HC-06 GND -> STM32 GND<br>

Android:</br>
- Connect the Bluetooth pins to your STM32 board (you don't need to have the program on your board just yet).
The diode on the Bluetooth adapter should start blinking.
- Go to your phones Bluetooth settings and find HC-06 device.
- Pair it using the pin 1234. The diode on the HC-06 shouldn't blink like it used to. That means that it's paired up correctly.
- Connect your android device trough USB port and run the code in Android Studio. Make sure you have debugging mode turned 'on'.

STM32: </br>
-Connect your STM32 trough USB port and press run in System Workbench for STM32
-Connect the HC-06 Bluetooth adapter if you haven't already

<h2>Future improvements:</h2>

- Charts of meters walked, calories burned etc.

<h2>License:</h2>

- MIT

<h2>Credits:</h2>

- Karol Rosiak 
- Damian Reczulski
- Adam Ostrowicki

<br>
The project was conducted during the Microprocessor Lab course held by the Institute of Control and Information Engineering, Poznan University of Technology.

<h2>Supervisor:</h2>

- Adam Bondyra
